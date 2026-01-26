# Deploy no Railway

Este documento descreve como fazer o deploy da aplicação no Railway.

## Pré-requisitos

1. Conta no [Railway](https://railway.app)
2. Projeto criado no Railway
3. Banco de dados PostgreSQL (pode ser criado via Railway ou usar um externo)

## Variáveis de Ambiente

Configure as seguintes variáveis de ambiente no Railway:

### Obrigatórias

- `SPRING_DATASOURCE_URL`: URL completa do banco PostgreSQL
  - Exemplo: `jdbc:postgresql://host:port/database`
  - Se usar PostgreSQL do Railway, será algo como: `jdbc:postgresql://containers-us-west-xxx.railway.app:5432/railway`

- `SPRING_DATASOURCE_USERNAME`: Usuário do banco de dados

- `SPRING_DATASOURCE_PASSWORD`: Senha do banco de dados

### Opcionais (com valores padrão)

- `PORT`: Porta do servidor (Railway define automaticamente, não precisa configurar manualmente)
- `SPRING_JPA_HIBERNATE_DDL_AUTO`: Modo de atualização do schema (padrão: `update`)
  - Valores: `none`, `validate`, `update`, `create`, `create-drop`
- `SPRING_JPA_SHOW_SQL`: Mostrar SQL no console (padrão: `false`)
- `SPRING_JPA_FORMAT_SQL`: Formatar SQL no console (padrão: `false`)
- `CORS_ALLOWED_ORIGINS`: Origens permitidas para CORS (padrão: `http://localhost:3000`)
  - Para produção, configure com o domínio do frontend: `https://seu-dominio.com`
  - Para permitir múltiplas origens, separe por vírgula: `https://app.vercel.app,https://seu-dominio.com`
  - Para permitir qualquer origem (não recomendado para produção), use: `*`
    - **Nota**: Quando usar `*`, as credenciais serão desabilitadas automaticamente

## Passos para Deploy

### 1. Conectar Repositório

1. No Railway, crie um novo projeto
2. Selecione "Deploy from GitHub repo" (ou GitLab/Bitbucket)
3. Conecte seu repositório
4. Selecione o diretório `vehicle-sales-manager-v2-kotlin`

### 2. Configurar Banco de Dados PostgreSQL

**Opção A: PostgreSQL do Railway (Recomendado)**

1. No projeto Railway, clique em **"New"** → **"Database"** → **"PostgreSQL"**
2. Railway criará automaticamente um serviço PostgreSQL e fornecerá variáveis de ambiente
3. Para obter as credenciais:
   - Clique no serviço PostgreSQL criado
   - Vá na aba **"Variables"** ou **"Connect"**
   - Railway fornece as seguintes variáveis:
     - `PGHOST` - Host do banco
     - `PGPORT` - Porta (geralmente 5432)
     - `PGUSER` - Usuário do banco
     - `PGPASSWORD` - Senha do banco
     - `PGDATABASE` - Nome do banco de dados
     - `DATABASE_URL` - URL completa no formato `postgresql://user:password@host:port/database`

**Opção B: PostgreSQL Externo**

1. Use as credenciais do seu banco externo
2. Configure manualmente as variáveis de ambiente conforme abaixo

### 3. Configurar Variáveis de Ambiente

**Opção A: Usar Variáveis PG* Automaticamente (Mais Simples - Recomendado)**

Se o PostgreSQL estiver no mesmo projeto Railway, você pode simplesmente **NÃO definir** as variáveis `SPRING_DATASOURCE_*`. O Spring Boot usará automaticamente as variáveis `PG*` que o Railway compartilha entre serviços do mesmo projeto.

1. No Railway, vá no serviço da sua aplicação (não no PostgreSQL)
2. Clique em **"Variables"**
3. Clique em **"Raw Editor"**
4. Use o arquivo `railway-variables-simple.json` que contém apenas as variáveis opcionais:
   ```json
   {
     "CORS_ALLOWED_ORIGINS": "https://seu-frontend.vercel.app",
     "SPRING_JPA_HIBERNATE_DDL_AUTO": "update",
     "SPRING_JPA_SHOW_SQL": "false",
     "SPRING_JPA_FORMAT_SQL": "false"
   }
   ```
5. Cole o JSON e clique em **"Save"**

**Vantagem:** Não precisa copiar valores manualmente - o Railway compartilha `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE` automaticamente.

**Opção B: Definir SPRING_DATASOURCE_* Explicitamente**

Se preferir definir explicitamente ou se o PostgreSQL estiver em outro projeto:

1. No Railway, vá no serviço da sua aplicação (não no PostgreSQL)
2. Clique em **"Variables"**
3. Clique em **"Raw Editor"**
4. Abra o arquivo `railway-variables.json` do projeto
5. **Substitua os valores pelos dados reais do seu PostgreSQL:**
   - No serviço PostgreSQL do Railway, vá em **"Variables"** e copie os valores REAIS:
     - `PGHOST` (ex: `containers-us-west-123.railway.app` ou `postgres.railway.internal`)
     - `PGPORT` (geralmente `5432`)
     - `PGUSER` (geralmente `postgres`)
     - `PGPASSWORD` (senha gerada pelo Railway)
     - `PGDATABASE` (geralmente `railway`)
   - **Monte a URL:** `jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}` (substitua pelos valores reais)
   - Substitua `CORS_ALLOWED_ORIGINS` pela URL do seu frontend
6. Cole o JSON editado no Raw Editor do Railway
7. Clique em **"Save"** ou **"Update Variables"**

**Arquivos disponíveis:**
- `railway-variables-simple.json` - Apenas variáveis opcionais (usa PG* automaticamente)
- `railway-variables.json` - Template com SPRING_DATASOURCE_* para editar
- `railway-variables.example.json` - Exemplo com valores de referência e instruções

**Opção B: Adicionar Manualmente**

No Railway, vá no serviço da sua aplicação (não no PostgreSQL) → **"Variables"** → **"New Variable"** e adicione:

#### Variáveis Obrigatórias (para conectar ao PostgreSQL)

Se você criou o PostgreSQL no Railway, você pode encontrar essas informações na aba "Variables" do serviço PostgreSQL:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://PGHOST:PGPORT/PGDATABASE
SPRING_DATASOURCE_USERNAME=PGUSER
SPRING_DATASOURCE_PASSWORD=PGPASSWORD
```

**Exemplo prático:**
Se o Railway forneceu:
- `PGHOST=containers-us-west-123.railway.app`
- `PGPORT=5432`
- `PGUSER=postgres`
- `PGPASSWORD=abc123xyz`
- `PGDATABASE=railway`

Configure:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://containers-us-west-123.railway.app:5432/railway
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=abc123xyz
```

#### Variáveis Opcionais

```
CORS_ALLOWED_ORIGINS=https://seu-frontend.com
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

**Dica**: Se você criou o PostgreSQL no mesmo projeto Railway, você pode referenciar as variáveis do PostgreSQL diretamente usando a sintaxe `${PGHOST}`, mas é mais seguro copiar os valores reais.

#### Como Copiar Valores do PostgreSQL no Railway

1. No serviço PostgreSQL, vá em **"Variables"**
2. Copie os valores de `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`
3. No serviço da aplicação, crie as variáveis `SPRING_DATASOURCE_*` com os valores copiados
4. Para `SPRING_DATASOURCE_URL`, monte a URL no formato: `jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}`
   - Substitua `${PGHOST}`, `${PGPORT}`, `${PGDATABASE}` pelos valores reais

### 4. Deploy

1. Railway detectará automaticamente o Dockerfile
2. O build será executado automaticamente
3. A aplicação será iniciada na porta definida pela variável `PORT`

## Verificação

Após o deploy, verifique:

1. **Logs**: Verifique os logs no Railway para confirmar que a aplicação iniciou corretamente
2. **Health Check**: Acesse `https://seu-app.railway.app/actuator/health` (se configurado)
3. **API**: Teste os endpoints da API

## Troubleshooting

### Erro de Conexão com Banco

- Verifique se as variáveis de ambiente estão corretas
- Confirme que o banco está acessível do Railway
- Verifique se o firewall permite conexões do Railway

### Erro na Porta

- Railway define `PORT` automaticamente, não configure manualmente
- A aplicação usa `${PORT:8080}`, então funcionará automaticamente

### CORS Errors

- Configure `CORS_ALLOWED_ORIGINS` com o domínio correto do frontend
- Inclua protocolo (http/https) e porta se necessário

## Estrutura do Dockerfile

O Dockerfile usa multi-stage build:

1. **Stage 1 (build)**: Compila a aplicação usando Gradle
2. **Stage 2 (runtime)**: Usa JRE Alpine (imagem leve) para executar o JAR

Isso resulta em uma imagem final menor e mais eficiente.
