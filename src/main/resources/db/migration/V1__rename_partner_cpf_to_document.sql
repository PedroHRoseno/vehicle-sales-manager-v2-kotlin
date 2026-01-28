-- Migração idempotente: renomear coluna de identificação de parceiro de cpf para document.
-- Executada automaticamente pelo Flyway na subida da aplicação (local e Railway).
-- Só aplica o RENAME se a tabela partners existir e tiver a coluna cpf (evita erro em banco novo).

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'partners'
  )
  AND EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'partners' AND column_name = 'cpf'
  ) THEN
    ALTER TABLE partners RENAME COLUMN cpf TO document;
  END IF;
END $$;
