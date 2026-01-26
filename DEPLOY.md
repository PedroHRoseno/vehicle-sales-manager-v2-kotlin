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

### 2. Configurar Banco de Dados

**Opção A: PostgreSQL do Railway**

1. No projeto Railway, clique em "New" → "Database" → "PostgreSQL"
2. Railway criará automaticamente e fornecerá as variáveis de ambiente:
   - `DATABASE_URL` (formato: `postgresql://user:password@host:port/database`)
   - Você precisará converter para o formato JDBC

**Opção B: PostgreSQL Externo**

1. Use as credenciais do seu banco externo
2. Configure manualmente as variáveis de ambiente

### 3. Configurar Variáveis de Ambiente

No Railway, vá em "Variables" e adicione:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/database
SPRING_DATASOURCE_USERNAME=seu_usuario
SPRING_DATASOURCE_PASSWORD=sua_senha
CORS_ALLOWED_ORIGINS=https://seu-frontend.com
```

**Nota**: Se usar PostgreSQL do Railway, você pode usar a variável `DATABASE_URL` que o Railway fornece automaticamente. Nesse caso, você precisará parsear a URL ou criar um script de inicialização.

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
