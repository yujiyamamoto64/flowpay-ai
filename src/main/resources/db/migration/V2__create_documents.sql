-- Basic table for RAG content and embeddings
-- Adjust vector dimension to your embedding model (e.g., 384, 768, 1536)
CREATE TABLE IF NOT EXISTS documents (
    id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    embedding VECTOR(768) NOT NULL
);

-- HNSW index for cosine similarity (Postgres 16+ in pgvector 0.6+)
-- If unavailable in your version, use IVFFlat instead
CREATE INDEX IF NOT EXISTS idx_documents_embedding_hnsw
    ON documents USING hnsw (embedding vector_cosine_ops);

