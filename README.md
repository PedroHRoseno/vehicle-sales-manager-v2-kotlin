# Vehicle Sales Manager v2.0 - Kotlin

Sistema de gerenciamento de vendas, compras e trocas de veículos desenvolvido em Kotlin com Spring Boot 3.x.

## Tecnologias

- **Linguagem**: Kotlin
- **Framework**: Spring Boot 3.2.0
- **ORM**: Spring Data JPA
- **Banco de Dados**: PostgreSQL 15
- **Documentação**: SpringDoc OpenAPI
- **Containerização**: Docker Compose
- **JSON Processing**: Jackson com Kotlin Module

## Estrutura do Projeto

```
vehicle-sales-manager-v2-kotlin/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/pedrohroseno/vehiclessalesmanager/
│   │   │       ├── model/          # Entidades e DTOs
│   │   │       │   ├── dtos/        # Data Transfer Objects
│   │   │       │   └── enums/       # Enumeradores (VehicleStatus, VehicleBrand, etc.)
│   │   │       ├── repository/     # Repositories JPA
│   │   │       ├── service/        # Lógica de negócio
│   │   │       ├── controller/     # Endpoints REST
│   │   │       └── config/         # Configurações (CORS, Jackson, JWT, etc.)
│   │   └── resources/
│   │       ├── application.yml      # Config mestre + perfis
│   │       ├── application-local.yml # Perfil local (Docker PostgreSQL)
│   │       └── application-prod.yml  # Perfil prod (Railway)
│   └── test/
├── docker-compose.yml   # Apenas PostgreSQL para desenvolvimento local
├── Dockerfile
└── build.gradle.kts
```

## Principais Mudanças da v1.0

1. **Renomeação de Cliente para Partner**: A entidade `Customer` foi renomeada para `Partner` para centralizar tanto compradores quanto fornecedores.
2. **Status de Veículo**: Substituição de `inStock: Boolean` por enum `VehicleStatus` (DISPONIVEL, VENDIDO).
3. **Entidade Exchange**: Nova entidade para gerenciar trocas de veículos.
4. **API Enxuta**: Remoção de endpoints de busca/filtro, mantendo apenas comandos (POST) e dashboard (GET).
5. **Migração para PostgreSQL**: Substituição do H2 por PostgreSQL com Docker Compose.
6. **Busca e Paginação**: Endpoints GET com suporte a busca e paginação para listagens.

## Regras de Negócio

### Compra
- Ao registrar uma compra, o veículo é automaticamente cadastrado no estoque com status `DISPONIVEL`.
- O veículo deve existir previamente no sistema (cadastrado via endpoint `/vehicles`).
- O veículo pode ser cadastrado durante o processo de compra.

### Venda
- Ao registrar uma venda, o veículo passa para status `VENDIDO` (indisponível).
- A venda é vinculada ao Partner (comprador).
- Apenas veículos com status `DISPONIVEL` podem ser vendidos.

### Troca
- **Moto de Saída** (da loja): Status muda para `VENDIDO`.
- **Moto de Entrada** (do cliente): É cadastrada automaticamente como `DISPONIVEL`.
- A transação calcula e salva a `diferencaValor` entre os dois veículos.
- O CPF do parceiro pode ser omitido se o veículo de entrada foi vendido anteriormente pelo sistema.

### Relatórios Financeiros
- Endpoint `/reports/dashboard` retorna:
  - `totalVendas`: Soma de todas as vendas
  - `totalCompras`: Soma de todas as compras
  - `saldoLiquido`: Diferença entre vendas e compras
  - `quantidadeMotosEstoque`: Quantidade de veículos disponíveis
- Endpoint `/reports/financial` retorna o saldo geral com filtro padrão: últimos 30 dias.
- Permite filtro personalizado via parâmetros `startDate` e `endDate`.

## Endpoints da API

### Veículos
- `GET /vehicles` - Listar todos os veículos (paginado)
- `GET /vehicles/available` - Listar apenas veículos disponíveis (paginado)
- `POST /vehicles` - Cadastrar veículo

### Parceiros (Clientes/Fornecedores)
- `GET /partners` - Listar todos os parceiros (paginado, com busca opcional)
  - Parâmetro `search`: Busca por CPF ou nome (case-insensitive)
- `GET /partners/{cpf}` - Detalhes de um parceiro específico (inclui histórico de operações)
- `POST /partners` - Criar novo parceiro
- `PUT /partners/{cpf}` - Atualizar parceiro existente

### Vendas
- `GET /sales` - Listar todas as vendas (paginado, com busca opcional)
  - Parâmetro `search`: Busca por placa do veículo, CPF ou nome do parceiro
- `POST /sales` - Registrar venda

### Compras
- `GET /purchases` - Listar todas as compras (paginado, com busca opcional)
  - Parâmetro `search`: Busca por placa do veículo, CPF ou nome do parceiro
- `POST /purchases` - Registrar compra

### Trocas
- `GET /exchanges` - Listar todas as trocas (paginado)
- `POST /exchanges` - Realizar troca
- `PUT /exchanges/{id}` - Atualizar troca
- `DELETE /exchanges/{id}` - Cancelar troca

### Fluxo de caixa e transações da loja
- `GET /financial/movements` - Movimentações unificadas (vendas, compras, trocas, custos, loja), paginado, filtros: startDate, endDate, type, category
- `GET /store-transactions` - Listar transações da loja (paginado)
- `POST /store-transactions` - Registrar transação da loja (despesa/receita)
- `PUT /store-transactions/{id}` - Atualizar transação
- `DELETE /store-transactions/{id}` - Cancelar transação

### Autenticação
- `POST /api/auth/login` - Login (retorna JWT)

### Relatórios
- `GET /reports/dashboard` - Dashboard consolidado (totalVendas, totalCompras, saldoLiquido, quantidadeMotosEstoque)
- `GET /reports/financial?startDate=yyyy-MM-dd&endDate=yyyy-MM-dd` - Relatório financeiro com filtro de datas

## Paginação

Todos os endpoints de listagem suportam paginação via Spring `Pageable`:
- `page`: Número da página (inicia em 0)
- `size`: Tamanho da página (padrão: 20)
- `sort`: Campo de ordenação (ex: `saleDate,desc`)

Exemplo: `GET /sales?page=0&size=20&sort=saleDate,desc&search=ABC1234`

## Deploy no Railway

A aplicação está preparada para deploy no Railway. Veja o arquivo `DEPLOY.md` para instruções detalhadas.

### Arquivos de deploy

- **`Dockerfile`** – Build multi-stage (Gradle + JRE); testes e docs excluídos via `.dockerignore`; porta via `PORT` (Railway).
- **`.dockerignore`** – Ignora `src/test`, `build`, docs (`*.md`, `scripts`, etc.) para reduzir contexto de build.
- **`DEPLOY.md`** – Guia passo a passo de deploy.

### Variáveis de ambiente (produção — Railway)

Use o perfil **prod** e defina:

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`: URL JDBC do PostgreSQL (ex: `jdbc:postgresql://host:5432/db`)
- `DB_USER`: Usuário do banco
- `DB_PASSWORD`: Senha do banco
- `CORS_ALLOWED_ORIGINS`: Origens permitidas (ex: `https://seu-app.vercel.app`)
- `PORT`: Porta (Railway define automaticamente)
- Opcional: `JWT_SECRET`, `JWT_EXPIRATION`

## Executando o Projeto Localmente

### Pré-requisitos

- Java 17 ou superior
- Docker e Docker Compose (apenas para o PostgreSQL)
- Gradle (wrapper incluído: `./gradlew`)

### Passo a passo

1. **Subir apenas o PostgreSQL (Docker):**

```bash
# Na raiz do projeto
docker compose up -d
```

Isso sobe **somente** o PostgreSQL (porta 5432, usuário `shido`, senha `12345`, banco `vehicle-sales-manager`). O backend é executado pela IDE ou pelo terminal, não em container.

2. **Rodar o backend** (perfil `local` é o padrão):

```bash
./gradlew bootRun
```

Ou abra o projeto na IDE e rode a classe principal. A aplicação fica em `http://localhost:8080`.

### Gerenciamento do Banco de Dados

**Parar o banco de dados:**
```bash
docker-compose down
```

**Parar e remover os dados (cuidado: apaga todos os dados):**
```bash
docker-compose down -v
```

**Ver logs do PostgreSQL:**
```bash
docker-compose logs -f postgres
```

**Acessar o banco via psql:**
```bash
docker-compose exec postgres psql -U shido -d vehicle-sales-manager
```

### Configuração (YAML e perfis)

A aplicação usa **YAML** e perfis Spring:

- **`application.yml`** – Config mestre: `spring.profiles.active: ${SPRING_PROFILES_ACTIVE:local}`, `server.port: ${PORT:8080}`, Jackson, JPA, CORS, JWT.
- **`application-local.yml`** (perfil `local`, padrão) – Banco em `localhost:5432`; usuário/senha padrão `shido`/`12345` (ou `DB_USER`/`DB_PASSWORD`).
- **`application-prod.yml`** (perfil `prod`) – Banco via `DB_URL`, `DB_USER`, `DB_PASSWORD`; `server.forward-headers-strategy: FRAMEWORK`.

Para rodar em desenvolvimento, não é necessário definir variáveis: o perfil **local** é o padrão e aponta para o PostgreSQL do `docker-compose`.

**Nota**: Na primeira execução, o Hibernate criará automaticamente todas as tabelas (ddl-auto: update).

## Documentação da API

Após iniciar a aplicação, a documentação Swagger estará disponível em:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Compatibilidade com o front-end

Os DTOs estão em `camelCase` para o front Next.js/TypeScript. Endpoints usados pelo front:

- **Autenticação**: `/api/auth/login`
- **Veículos**: `/vehicles`, `/vehicles/available`, `/vehicles/{placa}/history`, `/vehicles/{placa}/costs`
- **Parceiros**: `/partners`, `/partners/{cpf}`
- **Vendas**: `/sales`
- **Compras**: `/purchases`
- **Trocas**: `/exchanges`
- **Fluxo de caixa**: `/financial/movements`
- **Transações da loja**: `/store-transactions`
- **Relatórios**: `/reports/dashboard`, `/reports/financial`

## Estrutura de Dados

### Partner (Parceiro)
- `cpf` (String, PK): CPF único do parceiro
- `name` (String): Nome completo
- `phoneNumber1` (String, opcional): Telefone principal
- `phoneNumber2` (String, opcional): Telefone secundário
- `address` (Address, opcional): Endereço completo (One-to-One)

### Address (Endereço)
- `id` (Long, PK): ID do endereço
- `streetName` (String, opcional): Nome da rua
- `number` (String): Número
- `city` (String): Cidade
- `state` (String): Estado (UF)
- `zipCode` (String): CEP
- `reference` (String, opcional): Ponto de referência

### Vehicle (Veículo)
- `licensePlate` (String, PK): Placa do veículo
- `brand` (VehicleBrand, enum): Marca (HONDA, YAMAHA, etc.)
- `modelName` (String): Nome do modelo
- `manufactureYear` (Int): Ano de fabricação
- `modelYear` (Int): Ano do modelo
- `color` (String): Cor (hexadecimal)
- `kilometersDriven` (Int): Quilometragem
- `status` (VehicleStatus, enum): Status (DISPONIVEL, VENDIDO)

### Sale (Venda)
- `id` (Long, PK): ID da venda
- `vehicle` (Vehicle): Veículo vendido
- `partner` (Partner): Comprador
- `salePrice` (Double): Valor da venda
- `saleDate` (Date): Data da venda

### Purchase (Compra)
- `id` (Long, PK): ID da compra
- `vehicle` (Vehicle): Veículo comprado
- `partner` (Partner): Fornecedor
- `purchasePrice` (Double): Valor da compra
- `purchaseDate` (Date): Data da compra

### Exchange (Troca)
- `id` (Long, PK): ID da troca
- `vehicleEntrada` (Vehicle): Veículo de entrada (do cliente)
- `vehicleSaida` (Vehicle): Veículo de saída (da loja)
- `partner` (Partner): Parceiro envolvido
- `diferencaValor` (Double): Diferença de valor entre os veículos
- `exchangeDate` (Date): Data da troca

## Notas Importantes

1. O veículo pode ser cadastrado durante o processo de compra ou previamente via endpoint `/vehicles`.
2. Para trocas, o CPF do parceiro pode ser omitido se o veículo de entrada foi vendido anteriormente (o sistema busca automaticamente).
3. Todos os endpoints retornam status HTTP apropriados (201 para criação, 200 para sucesso, etc.).
4. A busca é case-insensitive e suporta busca parcial (LIKE).
5. O endereço do parceiro é opcional, mas quando fornecido, os campos `city`, `number`, `state` e `zipCode` são obrigatórios.
6. O campo `streetName` do endereço é opcional para permitir CEPs de cidade que não fornecem nome de rua específico.
