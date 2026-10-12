# Coretech

Sistema de e-commerce full-stack: backend **Spring Boot** com API REST e frontend estático
(HTML/CSS/JS puro) servido pelo próprio servidor.

## Stack

| Camada | Tecnologia |
|---|---|
| Backend | Java 21, Spring Boot 4.0.6 (Web, Data JPA, Security, Mail, Validation) |
| Banco de dados | MySQL 8 (perfil `prod`) ou H2 em memória (perfil `test`) |
| Autenticação | JWT (`jjwt`) + Spring Security |
| Frontend | HTML + CSS + JavaScript estáticos (`src/main/resources/static/`) |
| Build | Maven (wrapper incluso: `mvnw` / `mvnw.cmd`) |
| Extras | Lombok, OpenPDF/PDFBox (comprovantes), e-mail SMTP (confirmação de cadastro) |

## Arquitetura

O sistema é uma aplicação **monolítica em camadas**: o frontend estático é servido pelo próprio
Spring Boot, que expõe a API REST e valida cada requisição com Spring Security antes de repassá-la
à camada de serviço, única responsável por tocar o banco.

```
┌─────────────────────────────────────────────────────────────────────┐
│  FRONTEND — HTML + CSS + JS puro (src/main/resources/static/)       │
│  index.html · login.html · cadastro.html · carrinho.html ·          │
│  perfil.html · admin.html   (servidos pelo próprio Spring Boot)     │
└───────────────────────────────┬─────────────────────────────────────┘
                                │  fetch() com JSON
                                │  header: Authorization: Bearer <JWT>
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│  SPRING SECURITY (SegurityConfig + JwtAuthFilter + JwtService)      │
│  • valida o token JWT e carrega o usuário autenticado               │
│  • sessão STATELESS (sem cookies de sessão)                         │
│  • /auth/** e GET /produtos/** ........................ público      │
│  • POST/PUT/DELETE /produtos/** e /admin/** ......... exige ADMIN   │
│  • /carrinho/** e /pedidos/** ............... exige usuário logado  │
└───────────────────────────────┬─────────────────────────────────────┘
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│  API REST — Controllers (@RestController)                           │
│  AuthController · ProdutoController · CarrinhoController ·          │
│  PedidoController · AdminController · ImagemController ·            │
│  UsuarioController                                                  │
└───────────────────────────────┬─────────────────────────────────────┘
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│  SERVIÇOS (@Service) — regras de negócio                            │
│  UsuarioService · ProdutoService · CarrinhoService ·                │
│  PedidoService · VerificacaoService · LoggedUsersService ·          │
│  AdminMasterService   (+ EmailService SMTP e PdfService OpenPDF)    │
└───────────────────────────────┬─────────────────────────────────────┘
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│  REPOSITÓRIOS (Spring Data JPA)                                     │
│  UsuarioRepository · ProdutoRepository · CarrinhoRepository ·       │
│  ItemCarrinhoRepository · PedidoRepository                          │
└───────────────────────────────┬─────────────────────────────────────┘
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│  MySQL 8 — banco `coretech` (perfil `prod`)                         │
│  tabelas: usuarios · produtos · carrinho · itemcarrinho · pedidos   │
│  (perfil `test`: H2 em memória, mesmas tabelas)                     │
└─────────────────────────────────────────────────────────────────────┘
```

**Fluxo de uma requisição** (exemplo: adicionar item ao carrinho):

1. `carrinho.html` chama `fetch("/carrinho/itens", { method: "POST", headers: { Authorization: Bearer <JWT> } })`;
2. o `JwtAuthFilter` valida a assinatura/expiração do token e injeta o usuário autenticado no contexto
   (sem token ou sem permissão → `403`, resposta padrão do Spring Security);
3. o `CarrinhoController` recebe a requisição e delega ao `CarrinhoService`;
4. o `CarrinhoService` aplica as regras (produto existe, estoque, quantidade) e usa o
   `CarrinhoRepository` / `ItemCarrinhoRepository`;
5. o Spring Data JPA grava no MySQL e o objeto `Carrinho` atualizado volta como JSON ao frontend.

Pontos-chave do desenho:

- **Separação de responsabilidades:** controller só recebe/retorna HTTP; serviço concentra a regra de
  negócio; repositório é a única camada que fala com o banco (JPA/Hibernate).
- **Segurança na borda:** as regras de acesso ficam centralizadas em `SegurityConfig`, não nos controllers.
- **Serviços auxiliares:** `EmailService` (SMTP) e `PdfService` (OpenPDF) são acionados pelos serviços —
  código de confirmação no cadastro e comprovante no checkout.
- **Infra:** imagens de produto são salvas em `./uploads/` e servidas em `/uploads/**` (`WebConfig`);
  comprovantes PDF ficam em `./comprovantes/`.

## Pré-requisitos

- **JDK 21** (`java -version`)
- **MySQL 8** rodando na porta 3306 — apenas para o perfil `prod` (padrão)
- Um cliente **SMTP** (Gmail, etc.) — necessário para o código de confirmação por e-mail no cadastro
- Não é preciso instalar o Maven: use o wrapper do projeto (`mvnw.cmd` no Windows / `./mvnw` no Linux/macOS)

## Configuração (`.env`)

O projeto carrega automaticamente as variáveis do arquivo `.env` na raiz
(`spring.config.import=optional:file:.env[.properties]`). Crie esse arquivo com:

```properties
# conexão com o banco
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=coretech
MYSQL_USERNAME=root
MYSQL_PASSWORD=sua_senha

# e-mail (confirmação de cadastro e comprovante)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=seu-email@gmail.com
SMTP_PASSWORD=sua-senha-de-app
SMTP_AUTH=true
SMTP_STARTTLS=true
APP_EMAIL_HABILITADO=true
```

> ⚠️ O `.env` **não é versionado**. Nunca commite credenciais.
> O cadastro de clientes **exige SMTP funcional**: o código de confirmação de 6 dígitos é
> sempre enviado por e-mail real. Sem SMTP configurado, o cadastro falha com mensagem clara.

Variáveis opcionais (com valores padrão definidos em `application.properties`):

| Variável | Padrão | Descrição |
|---|---|---|
| `APP_ADMIN_EMAIL` | `admin@gmail.com` | E-mail do administrador master |
| `APP_ADMIN_SENHA` | `admin123` | Senha do administrador master |
| `APP_ADMIN_NOME` | `admin` | Nome do administrador master |
| `APP_JWT_SECRET` | (segredo interno) | Chave de assinatura do JWT |
| `APP_UPLOAD_DIR` | `./uploads` | Pasta de imagens de produtos |
| `APP_PDF_DIRECTORY` | `./comprovantes` | Pasta dos comprovantes em PDF |

## Como rodar

### 1. Perfil `prod` — MySQL (padrão)

Certifique-se de que o MySQL está no ar e que o banco existe:

```sql
CREATE DATABASE IF NOT EXISTS coretech;
```

No Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

Ou a partir da IDE (classe principal: `coretech.sistemaCoreTech.SistemaCoreTechApplication`).

### 2. Perfil `test` — H2 em memória (sem MySQL)

Edite `src/main/resources/application.properties` e troque:

```properties
spring.profiles.active=prod   # →   spring.profiles.active=test
```

Assim não é necessário MySQL nem `.env` de banco; o console H2 fica em `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:testdb`, usuário `sa`, sem senha). Os dados são apagados a cada reinício.

### 3. Acessar a aplicação

Abra **http://localhost:8080** (a porta padrão do Spring, nenhuma `server.port` configurada).

| Página | URL |
|---|---|
| Loja (catálogo) | http://localhost:8080/ |
| Login | http://localhost:8080/login.html |
| Cadastro | http://localhost:8080/cadastro.html |
| Carrinho | http://localhost:8080/carrinho.html |
| Perfil | http://localhost:8080/perfil.html |
| Painel admin | http://localhost:8080/admin.html |

## Credenciais de demonstração (seed)

O seed (`TesteConfig`) roda nos perfis `test` e `prod` de forma **idempotente**
(só cria o que não existe, nunca duplica):

| Perfil | E-mail | Senha |
|---|---|---|
| Admin master | `admin@gmail.com` | `admin123` |
| Cliente demo | `tom@gmail.com` | `123456` |

Catálogo inicial: 8 produtos. Para alterar o admin, use `APP_ADMIN_EMAIL` / `APP_ADMIN_SENHA`.

## API REST — endpoints representativos

Base URL: `http://localhost:8080`. Todas as rotas abaixo **existem no código**
(`src/main/java/coretech/sistemaCoreTech/controller/`) e foram verificadas em execução real.
O nível de acesso é definido em `SegurityConfig.securityFilterChain`: rotas protegidas exigem o
header `Authorization: Bearer <token>` (token obtido no `POST /auth/login`) — sem ele a resposta é
`403 Forbidden`.

### Autenticação e cadastro — público (`/auth/**`)

| Método | Rota | Finalidade |
|---|---|---|
| POST | `/auth/cadastro` | Cria a conta inativa e envia por e-mail o código de 6 dígitos |
| POST | `/auth/confirmar-codigo` | Ativa a conta com o código recebido por e-mail |
| POST | `/auth/reenviar-codigo` | Gera e reenvia um novo código de confirmação |
| POST | `/auth/login` | Autentica e devolve o JWT (+ perfil e e-mail); `428` se a conta não foi confirmada |
| POST | `/auth/logout` | Registra o logout do token e limpa o contexto de segurança |

### Catálogo de produtos — leitura pública (`GET /produtos/**`)

| Método | Rota | Finalidade |
|---|---|---|
| GET | `/produtos` | Lista o catálogo completo |
| GET | `/produtos/{id}` | Detalhes de um produto pelo id |
| GET | `/produtos/busca?nome={texto}` | Busca produtos pelo nome |

### Carrinho e pedidos — exige JWT (`USER` ou `ADMIN`)

| Método | Rota | Finalidade |
|---|---|---|
| GET | `/carrinho` | Retorna o carrinho do usuário logado |
| POST | `/carrinho/itens` | Adiciona item ao carrinho (body: `produtoId`, `quantidade`) |
| PUT | `/carrinho/itens/{itemId}` | Altera a quantidade de um item do carrinho (body: `quantidade`) |
| DELETE | `/carrinho/itens/{itemId}` | Remove um item do carrinho |
| DELETE | `/carrinho` | Esvazia o carrinho ("Limpar Carrinho") |
| POST | `/pedidos/checkout` | Finaliza a compra: valida estoque, cria o pedido, baixa estoque, gera comprovante PDF e limpa o carrinho |
| GET | `/usuarios/user` | Lista os usuários cadastrados (rota autenticada) |

### Administração — exige JWT + `ROLE_ADMIN` (`/admin/**`, `POST/PUT/DELETE /produtos/**`)

| Método | Rota | Finalidade |
|---|---|---|
| POST | `/produtos` | Cria um produto no catálogo |
| PUT | `/produtos/{id}` | Atualiza um produto existente |
| DELETE | `/produtos/{id}` | Remove um produto (HTTP `204`) |
| POST | `/admin/imagens` | Upload de imagem do produto (multipart `arquivo`, até 5MB) → devolve `{"url": "/uploads/..."}` |
| GET | `/admin/usuarios` | Lista todos os usuários |
| GET | `/admin/usuarios-logados` | Lista os usuários com sessão ativa |

Rotas estáticas (sem token): `/`, `*.html`, `/css/**`, `/js/**`, `/images/**` e `/uploads/**`
(imagens enviadas via `/admin/imagens`).

## Testes

```powershell
.\mvnw.cmd test
```

```bash
./mvnw test
```

## Estrutura do projeto

```
Coretech/
├── src/
│   ├── main/
│   │   ├── java/coretech/sistemaCoreTech/   # Código Java (controllers, services, config, security)
│   │   └── resources/
│   │       ├── application.properties        # Configuração principal (perfil ativo: prod)
│   │       ├── application-prod.properties   # Datasource MySQL (lê o .env)
│   │       ├── application-test.properties   # H2 em memória + console /h2-console
│   │       └── static/                       # Frontend (HTML, CSS, JS)
│   └── test/                                 # Testes (JUnit)
├── uploads/                                  # Imagens de produtos (servidas em /uploads/**)
├── comprovantes/                             # Comprovantes PDF gerados
├── .env                                      # Credenciais locais (não versionado)
└── pom.xml
```

## Funcionalidades

- Catálogo de produtos público (com busca, categorias e favoritos)
- Cadastro/login de clientes com confirmação por código de 6 dígitos enviado por e-mail
- Carrinho de compras, pedidos e checkout com comprovante PDF
- Painel administrativo (`/admin.html`) com gestão de produtos (CRUD com upload de imagens)
- Autenticação JWT e autorização por perfil (`ADMIN` / `USER`)

## Solução de problemas

- **`Port already in use` (8080):** outra instância da aplicação já está no ar — encerre-a
  (ou o processo da IDE/terminal anterior) antes de subir de novo.
- **Cadastro falha informando problema de e-mail:** o SMTP está vazio ou incorreto no `.env`.
- **`Access Denied` no painel admin:** a rota `/admin/**` exige o perfil `ADMIN`
  (faça login com `admin@gmail.com`).
- **Quer ver alterações no frontend sem reiniciar:** alterações em `static/` são servidas direto,
  mas o navegador cacheia — use **Ctrl+F5** (hard refresh). Depois de um `mvnw compile`, a cópia
  em `target/classes/static/` também é atualizada.
