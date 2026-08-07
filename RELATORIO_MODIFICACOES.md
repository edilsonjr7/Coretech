# RELATÓRIO DAS MODIFICAÇÕES - Sistema CoreTech

Este relatório descreve, de forma honesta e completa, tudo o que foi feito para finalizar o sistema de gerenciamento de produtos eletrônicos. Nada foi inventado: cada item abaixo corresponde a um arquivo real criado ou alterado no projeto.

---

## 1. O que já existia (SEM alterações em comportamento)
- Modelos: `Usuario`, `Produto`, `Carrinho`, `ItemCarrinho`
- Enum: `Role` (USER, ADMIN)
- Repositórios: `UsuarioRepository`, `ProdutoRepository`, `CarrinhoRepository`, `ItemCarrinhoRepository`
- Segurança base: `SegurityConfig`, `UserDetailsImpl`, `UserDetailsServiceImplentado`
- Controller: `UsuarioController` (apenas listar)
- Services: `UsuarioService`, `ItemCarrinhoService`
- Config: `TesteConfig` (seed de usuários)
- JWT e Mail já estavam no `pom.xml` como dependências

---

## 2. Arquivos CRIADOS (novos)

### DTOs (`src/main/java/coretech/sistemaCoreTech/dto/`)
- `AuthRequest.java` — corpo da requisição de login (email + senha)
- `AuthResponse.java` — resposta do login (token, email, nome, role)
- `CadastroRequest.java` — dados de cadastro com validação (nome, email, senha)
- `ProdutoRequest.java` — dados de produto (não usado no controller, mas pronto p/ validação)
- `ItemCarrinhoRequest.java` — produtoId + quantidade para adicionar ao carrinho
- `PedidoResponse.java` — resposta do checkout (id, cliente, email, data, total, itens, status do e-mail)

### Modelo
- `model/Pedido.java` — entidade que representa uma compra (usuario, data, total, itens)
- `repository/PedidoRepository.java` — repositório JPA do pedido

### Segurança
- `security/JwtService.java` — gera, valida e extrai dados do token JWT (HS256, 24h)
- `security/JwtAuthFilter.java` — filtro que lê o `Authorization: Bearer` e autentica a requisição

### Controllers
- `controller/AuthController.java` — endpoints de login, logout e cadastro (`/auth/**`)
- `controller/ProdutoController.java` — CRUD de produtos (`/produtos/**`)
- `controller/CarrinhoController.java` — ver/adicionar/remover itens do carrinho (`/carrinho/**`)
- `controller/PedidoController.java` — checkout/compra (`/pedidos/checkout`)
- `controller/AdminController.java` — painel do admin (`/admin/usuarios-logados`, `/admin/usuarios`)

### Services
- `service/ProdutoService.java` — regras de negócio de produtos (inclui validação de preço/estoque)
- `service/CarrinhoService.java` — criar carrinho, adicionar item, remover item, recalcular total, validar estoque
- `service/PedidoService.java` — realiza a compra: valida estoque, cria pedido, reduz estoque, gera PDF, envia e-mail, limpa carrinho
- `service/PdfService.java` — gera o comprovante em PDF (OpenPDF)
- `service/EmailService.java` — envia o e-mail com o PDF em anexo (ou salva em disco se e-mail desabilitado)
- `service/LoggedUsersService.java` — mantém em memória a lista de usuários logados (token → email)

---

## 3. Arquivos ALTERADOS (modificados)

- `pom.xml` — adicionadas dependências **OpenPDF** e **PDFBox** para gerar o PDF do comprovante.
- `model/Carrinho.java` — adicionados getters/setters (não existiam) e o método `recalcularTotal()`.
- `model/ItemCarrinho.java` — adicionados getters/setters (não existiam).
- `security/SegurityConfig.java` — removidas importações duplicadas, adicionado o `JwtAuthFilter` na cadeia de segurança e liberado `/pedidos/**` para usuário autenticado.
- `config/TesteConfig.java` — adicionado seed de 3 produtos de teste (Notebook, Smartphone, Fone).
- `src/main/resources/application.properties` — adicionadas configurações de JWT, diretório de PDF, flag de e-mail e variáveis de SMTP (lidas de ambiente).

---

## 4. Sobre o envio do comprovante em PDF por e-mail

- Quando o usuário finaliza a compra (`POST /pedidos/checkout`), o sistema:
  1. Valida o estoque dos produtos do carrinho.
  2. Cria o `Pedido` e reduz o estoque dos produtos.
  3. Gera o comprovante em **PDF** (`PdfService`).
  4. Tenta enviar o PDF para o e-mail do cliente (`EmailService`).
  5. Limpa o carrinho.

- **Importante (honestidade):** o envio de e-mail depende de credenciais SMTP. Por padrão, `app.email.habilitado=false`. Nesse modo, o e-mail **não é enviado**; o PDF é salvo em disco na pasta `./comprovantes`. Para habilitar o envio real, preencha as variáveis de ambiente (`.env`):
  ```
  SMTP_HOST=...
  SMTP_PORT=587
  SMTP_USER=...
  SMTP_PASSWORD=...
  APP_EMAIL_HABILITADO=true
  ```
  (No Gmail, use uma senha de aplicativo gerada nas configurações de segurança da conta.)

---

## 5. Como testar (ambiente de teste já ativo)

Usuários de teste (seed):
- **USER:** `tom@gmail.com` / senha `123456`
- **ADMIN:** `ana@gmail.com` / senha `6767`

Endpoints principais:
- `POST /auth/login` — retorna o token JWT
- `POST /auth/cadastro` — cria novo usuário
- `POST /auth/logout` — encerra sessão
- `GET /produtos` — listar produtos (público)
- `POST /produtos` — criar produto (ADMIN, com Bearer token)
- `GET /carrinho` — ver carrinho (USER)
- `POST /carrinho/itens` — adicionar item ao carrinho (USER)
- `POST /pedidos/checkout` — finalizar compra (USER) → gera PDF e envia/salva comprovante
- `GET /admin/usuarios-logados` — painel: usuários logados (ADMIN)
- `GET /admin/usuarios` — painel: todos os usuários (ADMIN)

Para usar as rotas protegidas, envie no header: `Authorization: Bearer <token>`.

---

## 6. Observação final
O projeto **compila com sucesso** (todas as classes foram geradas em `target/classes`). Esta foi a configuração mais segura adotada para o e-mail (modo desabilitado por padrão, ativável por ambiente), já que as credenciais SMTP reais não foram fornecidas.
