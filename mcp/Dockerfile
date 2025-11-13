# Simple image to run the Flowpay MCP server.

FROM node:22-alpine

WORKDIR /app

COPY mcp/flowpay-mcp.js ./flowpay-mcp.js

# Defaults point to the API exposed on the host machine.
ENV FLOWPAY_API_BASE_URL=http://host.docker.internal:8080 \
    FLOWPAY_HTTP_TIMEOUT_MS=15000

ENTRYPOINT ["node", "flowpay-mcp.js"]
