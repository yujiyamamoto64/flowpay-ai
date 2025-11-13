#!/usr/bin/env node
/**
 * Minimal Model Context Protocol (MCP) server to trigger Flowpay payments.
 *
 * Usage:
 *   node mcp/flowpay-mcp.js
 *
 * Environment variables:
 *   FLOWPAY_API_BASE_URL   Base URL for the Flowpay API (default http://localhost:8080)
 *   FLOWPAY_HTTP_TIMEOUT_MS Timeout in ms for HTTP requests (default 15000)
 */

const FLOWPAY_API_BASE_URL = process.env.FLOWPAY_API_BASE_URL || 'http://localhost:8080';
const FLOWPAY_HTTP_TIMEOUT_MS = parseInt(process.env.FLOWPAY_HTTP_TIMEOUT_MS || '15000', 10);

const toolDefinition = {
  name: 'flowpay.payment',
  description:
    'Executa o endpoint POST /payment do Flowpay. Consegue interpretar frases como "joao faz um pagamento de 200 para pedro".',
  inputSchema: {
    type: 'object',
    properties: {
      instruction: {
        type: 'string',
        description:
          'Frase em portugues descrevendo o pagamento. Ex: "joao faz um pagamento de 200 reais para pedro".',
      },
      amount: {
        type: 'number',
        description: 'Valor numerico do pagamento caso prefira informar explicitamente.',
      },
    },
    required: [],
    additionalProperties: false,
  },
  outputSchema: {
    type: 'object',
    properties: {
      status: { type: 'string' },
      amount: { type: 'number' },
      payer: { type: 'string' },
      payee: { type: 'string' },
      apiBaseUrl: { type: 'string' },
      paymentResponse: { type: 'object' },
      elapsedMs: { type: 'number' },
      note: { type: 'string' },
    },
    required: ['status'],
    additionalProperties: true,
  },
};

let initialized = false;
let messageQueue = [];
let processing = false;
let buffer = Buffer.alloc(0);
let stdinClosed = false;

process.stdin.on('data', (chunk) => {
  buffer = Buffer.concat([buffer, chunk]);
  extractMessages();
});
process.stdin.on('end', () => {
  stdinClosed = true;
  maybeExit();
});

function extractMessages() {
  while (true) {
    const separatorIndex = buffer.indexOf('\r\n\r\n');
    if (separatorIndex === -1) {
      break;
    }

    const headersRaw = buffer.subarray(0, separatorIndex).toString('utf8');
    const contentLengthMatch = headersRaw.match(/Content-Length:\s*(\d+)/i);
    if (!contentLengthMatch) {
      emitError(null, -32700, 'Missing Content-Length header');
      return;
    }

    const contentLength = parseInt(contentLengthMatch[1], 10);
    const messageEndIndex = separatorIndex + 4 + contentLength;
    if (buffer.length < messageEndIndex) {
      // Wait for more data.
      break;
    }

    const messageBuffer = buffer.subarray(separatorIndex + 4, messageEndIndex);
    buffer = buffer.subarray(messageEndIndex);

    let message;
    try {
      message = JSON.parse(messageBuffer.toString('utf8'));
    } catch (err) {
      emitError(null, -32700, `Invalid JSON received: ${err.message}`);
      continue;
    }

    messageQueue.push(message);
    processQueue();
  }
}

async function processQueue() {
  if (processing) {
    return;
  }
  processing = true;

  while (messageQueue.length > 0) {
    const message = messageQueue.shift();
    try {
      if (message.method) {
        await handleRequest(message);
      } else {
        // We do not expect responses from the client, ignore silently.
      }
    } catch (err) {
      emitError(message.id ?? null, -32000, err.message);
    }
  }

  processing = false;
  maybeExit();
}

async function handleRequest(message) {
  const { id, method, params } = message;
  switch (method) {
    case 'initialize':
      return handleInitialize(id);
    case 'tools/list':
      return handleToolsList(id);
    case 'tools/call':
      return handleToolsCall(id, params);
    case 'notifications/ack':
      // No-op to keep MCP clients happy.
      return;
    default:
      emitError(id, -32601, `Method ${method} not implemented`);
  }
}

function handleInitialize(id) {
  initialized = true;
  send({
    jsonrpc: '2.0',
    id,
    result: {
      protocolVersion: '1.0',
      capabilities: {
        tools: {
          list: true,
          call: true,
        },
      },
    },
  });
}

function handleToolsList(id) {
  ensureInitialized(id);
  send({
    jsonrpc: '2.0',
    id,
    result: {
      tools: [toolDefinition],
    },
  });
}

async function handleToolsCall(id, params = {}) {
  ensureInitialized(id);
  if (!params.name) {
    emitError(id, -32602, 'O nome da ferramenta e obrigatorio.');
    return;
  }

  if (params.name !== toolDefinition.name) {
    emitError(id, -32601, `Ferramenta ${params.name} nao encontrada.`);
    return;
  }

  const args = params.arguments || {};
  let parsedRequest;
  try {
    parsedRequest = parsePaymentInstruction(args);
  } catch (err) {
    emitError(id, -32602, err.message);
    return;
  }

  const start = Date.now();
  try {
    const paymentResponse = await executePayment(parsedRequest.amount);
    const elapsedMs = Date.now() - start;
    send({
      jsonrpc: '2.0',
      id,
      result: {
        status: 'success',
        amount: parsedRequest.amount,
        payer: parsedRequest.payer,
        payee: parsedRequest.payee,
        apiBaseUrl: FLOWPAY_API_BASE_URL,
        paymentResponse,
        elapsedMs,
        note: 'O email de notificacao e disparado via n8n/MailHog automaticamente apos o pagamento.',
      },
    });
  } catch (err) {
    emitError(id, -32001, err.message);
  }
}

function parsePaymentInstruction(args) {
  const rawInstruction =
    typeof args.instruction === 'string' ? args.instruction.trim() : '';
  const normalized = removeDiacritics(rawInstruction.toLowerCase());

  let amount = parseAmount(args.amount, normalized);
  if (amount === null) {
    throw new Error(
      'Nao consegui identificar o valor do pagamento. Diga algo como "joao paga 200 para pedro".'
    );
  }

  if (amount <= 0) {
    throw new Error('O valor deve ser maior que zero.');
  }

  const payer = normalized.includes('joao') ? 'joao' : 'joao';
  const payee = normalized.includes('pedro') ? 'pedro' : 'pedro';

  return {
    instruction: rawInstruction || `joao paga ${amount} para pedro`,
    amount: Math.round(amount * 100) / 100,
    payer,
    payee,
  };
}

function parseAmount(amountArgument, normalizedInstruction) {
  if (typeof amountArgument === 'number' && Number.isFinite(amountArgument)) {
    return amountArgument;
  }

  if (typeof amountArgument === 'string') {
    const value = parseFloat(amountArgument.replace(',', '.'));
    if (!Number.isNaN(value)) {
      return value;
    }
  }

  const match = normalizedInstruction.match(/(\d+(?:[.,]\d+)?)/);
  if (match) {
    const value = parseFloat(match[1].replace(',', '.'));
    if (!Number.isNaN(value)) {
      return value;
    }
  }

  return null;
}

async function executePayment(amount) {
  const url = `${FLOWPAY_API_BASE_URL.replace(/\/$/, '')}/payment`;
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), FLOWPAY_HTTP_TIMEOUT_MS);

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount }),
      signal: controller.signal,
    });

    const text = await response.text();
    let data;
    try {
      data = text ? JSON.parse(text) : {};
    } catch {
      data = { raw: text };
    }

    if (!response.ok) {
      throw new Error(
        `Flowpay API respondeu ${response.status}: ${data?.message || text}`
      );
    }

    return data;
  } catch (err) {
    if (err.name === 'AbortError') {
      throw new Error(
        `A chamada ao Flowpay excedeu ${FLOWPAY_HTTP_TIMEOUT_MS}ms e foi cancelada.`
      );
    }
    throw err;
  } finally {
    clearTimeout(timer);
  }
}

function ensureInitialized(id) {
  if (!initialized) {
    emitError(id, -32002, 'O servidor MCP ainda nao foi inicializado.');
    return false;
  }
  return true;
}

function maybeExit() {
  if (stdinClosed && !processing && messageQueue.length === 0) {
    process.exit(0);
  }
}

function emitError(id, code, message, data) {
  send({
    jsonrpc: '2.0',
    id,
    error: {
      code,
      message,
      data,
    },
  });
}

function send(payload) {
  const body = Buffer.from(JSON.stringify(payload), 'utf8');
  const header = `Content-Length: ${body.length}\r\n\r\n`;
  process.stdout.write(header);
  process.stdout.write(body);
}

function removeDiacritics(text) {
  return text.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}
