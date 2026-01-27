# Como Recriar o Banco de Dados PostgreSQL

Se você está encontrando erros de constraint (como "NULL not allowed for column" ou "transaction_history_transaction_type_check"), o banco de dados pode ter sido criado com a estrutura antiga. Siga estes passos para recriar:

## Erro "transaction_history_transaction_type_check" (Fluxo de Caixa)

Ao cadastrar uma transação no fluxo de caixa, se aparecer:

```
ERROR: new row for relation "transaction_history" violates check constraint "transaction_history_transaction_type_check"
```

a constraint da tabela `transaction_history` não inclui `STORE_TRANSACTION`. Corrija assim:

**Via Docker (na pasta do backend):**
```bash
docker-compose exec -T postgres psql -U shido -d vehicle-sales-manager < scripts/fix-transaction-history-constraint.sql
```

**Ou via psql manualmente:** abra o banco, depois execute:
```sql
ALTER TABLE transaction_history DROP CONSTRAINT IF EXISTS transaction_history_transaction_type_check;
ALTER TABLE transaction_history ADD CONSTRAINT transaction_history_transaction_type_check
  CHECK (transaction_type IN ('PURCHASE', 'SALE', 'EXCHANGE', 'STORE_TRANSACTION'));
```

---

## Opção 1: Recriar via Docker Compose (Recomendado)

1. **Parar e remover o container e volumes:**
```bash
docker-compose down -v
```

2. **Recriar o container:**
```bash
docker-compose up -d
```

3. **Aguardar alguns segundos para o PostgreSQL inicializar**

4. **Reiniciar a aplicação Spring Boot**

O Hibernate criará automaticamente todas as tabelas com a estrutura correta.

## Opção 2: Recriar via SQL

1. **Acessar o banco via psql:**
```bash
docker-compose exec postgres psql -U shido -d vehicle-sales-manager
```

2. **Dropar e recriar a tabela:**
```sql
DROP TABLE IF EXISTS addresses CASCADE;
DROP TABLE IF EXISTS partners CASCADE;
-- Continue para outras tabelas se necessário
```

3. **Sair do psql:**
```sql
\q
```

4. **Reiniciar a aplicação Spring Boot**

O Hibernate criará automaticamente todas as tabelas.

## Opção 3: Usar create-drop temporariamente

1. **Editar `application.properties`:**
```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

2. **Reiniciar a aplicação** (isso apagará todos os dados)

3. **Voltar para `update` após a primeira execução:**
```properties
spring.jpa.hibernate.ddl-auto=update
```

## Verificar se funcionou

Após recriar, verifique se a tabela `addresses` tem a coluna `street_name` como nullable:

```sql
\d addresses
```

A coluna `street_name` deve aparecer como `character varying` sem `NOT NULL`, enquanto `city`, `number`, `state` e `zip_code` devem ter `NOT NULL`.
