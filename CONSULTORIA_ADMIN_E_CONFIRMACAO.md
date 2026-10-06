# CONSULTORIA - Acesso ADMIN, Confirmação de Cadastro por Código e Painel de Usuários

Documento da consultoria aplicada ao sistema CoreTech. Cada item abaixo corresponde a
um arquivo real criado ou alterado no projeto.

---

## 1. Requisitos solicitados x situação atual

| Requisito do cliente | Situação anterior | Situação agora |
|---|---|---|
| (a) Apagar todos os usuários ADMIN | Existiam 2 admins: `admin@coretech.com` (AdminConfig) e `ana@gmail.com` (seed TesteConfig) | Existe **apenas 1** admin: o admin master. Os demais são apagados na inicialização |
| (b) Criar login `admin@gamil.com` / `admin123` com acesso exclusivo à administração | O login admin era `admin@coretech.com` / `admin123` | Login master configurável em `application.properties` (`app.admin.email`, `app.admin.senha`) |
| (c) Cadastro de cliente com token de 6 números enviado por e-mail e confirmação em tela | Cadastro ativava a conta direto, sem confirmação | Fluxo em 2 etapas: dados → código de 6 números → conta ativada |
| (d) Painel do admin com todos os usuários e CRUD de produtos | Painel tinha apenas o CRUD de produtos; `/admin/usuarios` existia na API mas não na tela | Painel com abas **Produtos** (CRUD) e **Usuários** (todos os usuários + estatísticas + busca) |

**Ponto de atenção (consultoria):** o e-mail informado foi `admin@gamil.com`, que parece ser
um erro de digitação de `admin@gmail.com` (o domínio `gamil.com` não é um provedor de e-mail).
Como pedido, o login foi criado **exatamente** com `admin@gamil.com`. Se preferir o domínio
correto, **não é necessário mexer em código**: basta trocar o valor padrão em
`src/main/resources/application.properties`:

```properties
app.admin.email=${APP_ADMIN_EMAIL:admin@gmail.com}
```

---

## 2. Arquivos CRIADOS

### DTOs (`src/main/java/coretech/sistemaCoreTech/dto/`)
- `ConfirmarCodigoRequest.java` — corpo de `POST /auth/confirmar-codigo` (email + código validado com `\d{6}`)
- `ReenviarCodigoRequest.java` — corpo de `POST /auth/reenviar-codigo` (email)
- `CodigoResponse.java` — resposta de cadastro/reenvio (`mensagem`, `email`, `emailEnviado`)

### Services
- `service/VerificacaoService.java` — gera o código de 6 números (`SecureRandom`), aplica validade
  (`app.verificacao.codigo-expiracao-minutos`, padrão 15 min), envia por e-mail, valida o código
  digitado e ativa a conta.
- `service/EmailNaoEnviadoException.java` — lançada quando o SMTP não está configurado ou o envio falha
  (a API responde 503 com o motivo e **nenhum código é "inventado"** no log ou na resposta).
- `service/AdminMasterService.java` — apaga todos os usuários `ADMIN` exceto o admin master
  (removendo antes carrinho e pedidos para não violar chave estrangeira) e cria/atualiza o admin master.

  (removendo antes carrinho e pedidos para não violar chave estrangeira) e cria/atualiza o admin master.

---

## 3. Arquivos ALTERADOS

| Arquivo | O que mudou |
|---|---|
| `config/AdminConfig.java` | Passou a: (1) apagar todos os ADMIN diferentes do master; (2) criar/garantir o master com `app.admin.*`. Ordem de execução `@Order(2)`. Não existe mais o admin `admin@coretech.com` |
| `config/TesteConfig.java` | Removido o seed de ADMIN (`ana@gmail.com`); o seed cria apenas o cliente `tom@gmail.com` (`USER`). `@Order(1)` |
| `model/Usuario.java` | Novos campos `codigo_confirmacao` e `codigo_expira_em`. `@JsonIgnore` em `getSenha()`, `getCodigoConfirmacao()` e `getCodigoExpiraEm()` para nunca devolver hash de senha nem o código na API |
| `repository/UsuarioRepository.java` | Novo `findByRole(Role)` (usado para apagar admins antigos) |
| `repository/PedidoRepository.java` | Novo `findByUsuarioId(Long)` (limpeza de dados do admin removido) |
| `service/EmailService.java` | `enviarCodigoConfirmacao(para, nome, codigo)` com envio **sempre real** por SMTP: se `SMTP_HOST`/`SMTP_USER` não estiverem preenchidos ou o envio falhar, lança `EmailNaoEnviadoException` (o código não vai para o log nem para a resposta) |
| `controller/AuthController.java` | `POST /auth/cadastro` agora cria `USER` **inativo** + código; novos `POST /auth/confirmar-codigo` e `POST /auth/reenviar-codigo`; **login responde 428** (`PRECONDITION_REQUIRED`) enquanto o código não for confirmado; falha de e-mail responde **503** com o motivo |
| `resources/application.properties` | Novas chaves `app.admin.email/senha/nome` e `app.verificacao.codigo-expiracao-minutos` |
| `static/js/api.js` | `cadastro()` retorna JSON; novos `confirmarCodigo()`, `reenviarCodigo()`, `listarUsuarios()`, `listarUsuariosLogados()`; `login()` detecta HTTP 428 e marca `precisaConfirmar` |
| `static/js/auth.js` | Login redireciona por perfil (`ADMIN` → `/admin.html`, cliente → `/`); ao receber 428 abre a etapa de confirmação direto na tela de login (com reenvio) e, depois de confirmar, entra sozinho; cadastro em 2 etapas |
| `static/login.html` | Nova etapa "Confirme sua conta" (campo de 6 dígitos + reenvio + voltar para o login) |
| `static/cadastro.html` | Nova etapa de confirmação (campo de 6 dígitos + reenvio). REMOVIDO o aviso de "código de desenvolvimento" |
| `static/admin.html` | Abas **Produtos** / **Usuários**, cards de estatísticas de usuários e tabela com todos os usuários. Novos estilos (abas, badges de perfil/status) |
| `static/js/admin.js` | Carrega e renderiza todos os usuários (`GET /admin/usuarios`), estatísticas (total, clientes, admins, ativas), busca com filtro e troca de abas |

---

## 4. Como funciona o fluxo de confirmação do cliente

1. Cliente preenche nome, e-mail e senha em `/cadastro.html` → `POST /auth/cadastro`.
2. O backend cria o usuário com perfil `USER` e `ativo=false`, gera um código de 6 números
   (`SecureRandom`, ex.: `042317`) válido por 15 minutos e envia por e-mail.
3. A tela troca para a etapa do código: o cliente digita os 6 números → `POST /auth/confirmar-codigo`.
4. Só então a conta é ativada (`ativo=true`) e o login é liberado. Antes disso o login responde
   **428 (`PRECONDITION_REQUIRED`)** com a mensagem "Conta não confirmada. Digite o código de 6 dígitos...".
   A própria tela de login entende esse 428, abre o campo do código, confirma e entra sozinha.
5. Se o código expirar ou não chegar, o cliente usa "Reenviar código" → `POST /auth/reenviar-codigo`
   (gera um novo código e invalida o anterior).

### Sobre o envio real de e-mail (transparência)
**Não existe mais código fictício.** O `codigoDev`, o aviso "Modo desenvolvimento" e o log
`E-mail desabilitado. Código de confirmação de ...: 123456` foram removidos: o código de 6 dígitos
só existe no e-mail recebido pelo cliente. Consequências:

- sem SMTP configurado (ou com falha de envio) o cadastro responde **503** explicando o motivo — a conta
  fica criada e inativa, e o cliente pode usar "Reenviar código" quando o SMTP estiver ajustado;
- o login continua bloqueado (428) enquanto o código não for confirmado, ou seja, sem e-mail funcionando
  não é possível entrar como cliente novo;
- `app.email.habilitado` agora controla **apenas o comprovante de compra** (que é opcional). O código de
  confirmação é sempre enviado de verdade.

Preencha o `.env` com um SMTP real (no Gmail, use uma **senha de app**):

```
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=seuemail@gmail.com
SMTP_PASSWORD=senha-de-app-do-gmail
SMTP_AUTH=true
SMTP_STARTTLS=true
APP_EMAIL_HABILITADO=true
```

> Validação feita no projeto: com um SMTP local de teste, o cadastro entregou o e-mail
> (assunto "CoreTech - Confirme seu cadastro"), o login respondeu 428 antes da confirmação, a
> confirmação com o código do e-mail ativou a conta e o login seguinte liberou o token JWT.
> Sem SMTP, o cadastro respondeu 503 e nenhuma resposta da API trouxe código de 6 dígitos.

---

## 5. Credenciais e roteiro de teste

| Perfil | E-mail | Senha | Para onde vai ao logar |
|---|---|---|---|
| ADMIN (único) | `admin@gamil.com` | `admin123` | `/admin.html` (painel) |
| USER (cliente, seed) | `tom@gmail.com` | `123456` | `/` (loja) |

Teste rápido da API (PowerShell):

```powershell
# 1) login do admin master
Invoke-RestMethod -Uri http://localhost:8080/auth/login -Method Post -ContentType 'application/json' `
  -Body '{"email":"admin@gamil.com","senha":"admin123"}'

# 2) painel: todos os usuários (use o token retornado acima)
Invoke-RestMethod -Uri http://localhost:8080/admin/usuarios -Headers @{ Authorization = "Bearer SEU_TOKEN" }
```

---

## 6. Observações honestas / limites

1. O login master é criado/atualizado **a cada inicialização** — se a senha do master for alterada
   direto no banco, ela volta para `admin123` no próximo start (é o comportamento pedido: acesso fixo
   apenas para esse login). Para mudar, use `app.admin.senha` ou a variável `APP_ADMIN_SENHA`.
2. A limpeza dos admins antigos roda na inicialização do backend. Com o H2 do profile `test` (padrão)
   o banco é recriado a cada start; em MySQL (`application-prod.properties`) a limpeza acontece na
   primeira subida e depois só o master existe.
3. O painel de usuários é **somente leitura** (listar/buscar), conforme pedido ("painel com todos os
   usuários"); o CRUD implementado é o de produtos. Não foi criada exclusão de usuários pelo painel
   para não haver risco de apagar o próprio admin master.
4. Recomendação (não aplicada, fora do escopo): `GET /usuarios/user` está acessível a qualquer usuário
   autenticado e lista todos os usuários; o ideal é restringi-lo a `ADMIN` ou removê-lo, já que o painel
   usa `GET /admin/usuarios`.
5. Recomendação (não aplicada): o código de confirmação não tem limite de tentativas; um próximo passo
   seria bloquear após N tentativas erradas.
