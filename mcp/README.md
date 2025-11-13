# Flowpay MCP Server

Servidor MCP (Model Context Protocol) simples que expoe o endpoint `POST /payment` do Flowpay para qualquer cliente MCP (por exemplo, Claude Desktop). Ele entende instrucoes em portugues como "joao faz um pagamento de 200 para pedro", dispara o pagamento via API e o fluxo existente continua responsavel por enviar o e-mail pelo n8n/MailHog.

## Pre-requisitos
- Docker Compose do projeto em execucao (app, n8n e mailhog precisam estar ativos).
- Node.js 18+ (o ambiente ja traz Node 22).

## Como executar

No diretorio raiz do repositorio:

```powershell
node .\mcp\flowpay-mcp.js
```

> Dica: execute os comandos abaixo a partir da pasta `flowpay-ai`. Se estiver em outro diretório, use o caminho completo (`node C:\Users\Run2biz-0290\flowpay-ai\mcp\flowpay-mcp.js`).

### Rodando via Docker

Build da imagem (no diretório raiz):

```powershell
docker build -t flowpay-mcp -f mcp/Dockerfile .
```

Execução mais comum (API rodando no host; mantém STDIN aberto com `-i`):

```powershell
docker run --rm -i flowpay-mcp
```

Se o Flowpay estiver em outro container da mesma rede (por exemplo, `docker compose up`), junte as redes e ajuste a URL:

```powershell
docker run --rm -i ^
  --network flowpay-ai_default ^
  -e FLOWPAY_API_BASE_URL=http://app:8080 ^
  flowpay-mcp
```

Variaveis de ambiente opcionais:

| Variavel                  | Significado                                | Default                 |
|---------------------------|---------------------------------------------|-------------------------|
| `FLOWPAY_API_BASE_URL`    | Base URL usada para chamar `/payment`       | `http://localhost:8080` |
| `FLOWPAY_HTTP_TIMEOUT_MS` | Timeout das chamadas HTTP em milissegundos  | `15000`                 |

## Teste rapido via PowerShell

```powershell
$init  = '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}'
$call  = '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"flowpay.payment","arguments":{"instruction":"joao faz um pagamento de 200 para pedro"}}}'

$payload = @(
    "Content-Length: $([System.Text.Encoding]::UTF8.GetByteCount($init))`r`n`r`n$init"
    "Content-Length: $([System.Text.Encoding]::UTF8.GetByteCount($call))`r`n`r`n$call"
) -join ''

$payload | node .\mcp\flowpay-mcp.js
```

O segundo response mostrara o retorno da API Flowpay e, com o Docker Compose ativo, o e-mail aparecera no MailHog (`http://localhost:8025`).

## Exemplo de configuracao no Claude Desktop

```jsonc
{
  "mcpServers": [
    {
      "name": "flowpay",
      "command": "docker",
      "args": [
        "run",
        "--rm",
        "-i",
        "-e",
        "FLOWPAY_API_BASE_URL=http://host.docker.internal:8080",
        "flowpay-mcp"
      ]
    }
  ]
}
```

Depois de inicializar o servidor MCP, basta pedir algo como "joao faz um pagamento de 200 para pedro" no cliente MCP. O servidor interpreta a frase, chama `/payment` e o fluxo atual do projeto garante o envio do e-mail.
