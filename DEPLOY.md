# Deploy no Railway

Este documento descreve como fazer o deploy da aplicação no Railway.

## Pré-requisitos

1. Conta no [Railway](https://railway.app)
2. Projeto criado no Railway
3. Banco de dados PostgreSQL (pode ser criado via Railway ou usar um externo)

## Variáveis de Ambiente

A aplicação usa **perfis Spring** (`application.yml` + `application-prod.yml`). Em produção no Railway é **obrigatório** usar o perfil **prod**.

Configure as seguintes variáveis de ambiente no **serviço do backend** (não no PostgreSQL):

### Obrigatórias (perfil prod)

- `SPRING_PROFILES_ACTIVE`: **`prod`** (ativa `application-prod.yml`)

- `DB_URL`: URL JDBC do PostgreSQL  
  - Ex.: `jdbc:postgresql://host:port/database`  
  - Se usar PostgreSQL do Railway: monte com `PGHOST`, `PGPORT`, `PGDATABASE` do serviço Postgres (ex.: `jdbc:postgresql://monolith.proxy.rlwy.net:12345/railway`)

- `DB_USER`: Usuário do banco (ex.: `postgres`)

- `DB_PASSWORD`: Senha do banco

### Recomendadas

- `CORS_ALLOWED_ORIGINS`: Origens permitidas para CORS  
  - Produção: URL do front na Vercel, ex.: `https://almotos-front.vercel.app`  
  - Múltiplas: separar por vírgula, ex.: `https://seu-app.vercel.app,http://localhost:3000`

### Opcionais (com valores padrão no application.yml)

- `PORT`: Railway define automaticamente; a aplicação usa `server.port: ${PORT:8080}`
- `JWT_SECRET`, `JWT_EXPIRATION`: têm default; em produção é melhor definir `JWT_SECRET` forte

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

No **serviço do backend** (não no PostgreSQL), em **Variables**, use os nomes esperados pelo perfil **prod** (`application-prod.yml`). Você pode colar o JSON do arquivo **`railway-variables-prod.json`** no Raw Editor (recomendado; veja opção 1 abaixo).

- **`SPRING_PROFILES_ACTIVE`** = `prod`
- **`DB_URL`** = URL JDBC (ex.: `jdbc:postgresql://host:port/database`)
- **`DB_USER`** = usuário do banco
- **`DB_PASSWORD`** = senha do banco
- **`CORS_ALLOWED_ORIGINS`** = URL do front na Vercel (ex.: `https://almotos-front.vercel.app`)

#### Opção 1: Variáveis por referência (recomendado — não precisa colar senha)

No Railway você pode usar **referências** ao serviço PostgreSQL: o backend não recebe os valores diretamente; o Railway resolve `${{NomeDoServiço.VAR}}` no deploy.

1. No **serviço do backend**, abra **Variables** → **Raw Editor**.
2. Cole o conteúdo do arquivo **`railway-variables-prod.json`** deste repositório.
3. Ajuste **apenas**:
   - **Nome do serviço Postgres:** no JSON aparece `${{Postgres.PGHOST}}`, `${{Postgres.PGUSER}}`, etc. Se o seu serviço de banco tiver outro nome no dashboard (ex.: `postgres`, `PostgreSQL`), troque `Postgres` por esse nome em todas as referências.
   - **CORS:** altere `https://SEU-APP.vercel.app` para a URL real do seu front na Vercel.

Exemplo do JSON (já está em `railway-variables-prod.json`):

```json
{
  "SPRING_PROFILES_ACTIVE": "prod",
  "DB_URL": "jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}",
  "DB_USER": "${{Postgres.PGUSER}}",
  "DB_PASSWORD": "${{Postgres.PGPASSWORD}}",
  "CORS_ALLOWED_ORIGINS": "https://SEU-APP.vercel.app"
}
```

Assim você não copia URL nem senha; o Railway puxa os valores do serviço PostgreSQL na hora do deploy.

#### Opção 2: Valores fixos (copiando do Postgres)

No **serviço PostgreSQL** do Railway, abra **Variables** ou **Connect**. Use `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE` para montar no **serviço do backend**:

```
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://<PGHOST>:<PGPORT>/<PGDATABASE>
DB_USER=<PGUSER>
DB_PASSWORD=<PGPASSWORD>
CORS_ALLOWED_ORIGINS=https://seu-frontend.vercel.app
```

Substitua `<PGHOST>`, `<PGPORT>`, etc. pelos valores exibidos no serviço Postgres.

**Importante:** O perfil prod usa **`DB_*`**, não mais `SPRING_DATASOURCE_*`. Veja também o guia na raiz do monorepo: **`VERCEL_RAILWAY_ATUALIZADO.md`**.

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
