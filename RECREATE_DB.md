# Como Recriar o Banco de Dados PostgreSQL

Se você está encontrando erros de constraint (como "NULL not allowed for column"), o banco de dados pode ter sido criado com a estrutura antiga. Siga estes passos para recriar:

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
