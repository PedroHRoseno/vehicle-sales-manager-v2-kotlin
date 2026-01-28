-- Migração: renomear coluna de identificação de parceiro de CPF para document (CPF/CNPJ)
--
-- Esta migração está AUTOMATIZADA via Flyway ao subir a API (local e Railway).
-- O script aplicado na subida fica em: src/main/resources/db/migration/V1__rename_partner_cpf_to_document.sql
--
-- Use este arquivo só se precisar rodar manualmente no PostgreSQL, por exemplo:
--   psql -U postgres -d vehicle-sales-manager -f rename-partner-cpf-to-document.sql

ALTER TABLE partners RENAME COLUMN cpf TO document;
