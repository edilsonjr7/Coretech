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
- `CodigoResponse.java` — resposta de cadastro/reenvio (`mensagem`, `email`, `emailEnviado`, `codigoDev`)

### Services
- `service/VerificacaoService.java` — gera o código de 6 números (`SecureRandom`), aplica validade
  (`app.verificacao.codigo-expiracao-minutos`, padrão 15 min), envia por e-mail, valida o código
  digitado e ativa a conta.
- `service/AdminMasterService.java` — apaga todos os usuários `ADMIN` exceto o admin master
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
| `service/EmailService.java` | Novo `enviarCodigoConfirmacao(para, nome, codigo)` e `isEmailHabilitado()`. Com `app.email.habilitado=false` o código é registrado no log da aplicação |
| `controller/AuthController.java` | `POST /auth/cadastro` agora cria `USER` **inativo** + código; novos `POST /auth/confirmar-codigo` e `POST /auth/reenviar-codigo`; login bloqueia conta não confirmada com mensagem explicando o código |
| `resources/application.properties` | Novas chaves `app.admin.email/senha/nome` e `app.verificacao.codigo-expiracao-minutos` |
| `static/js/api.js` | `cadastro()` retorna JSON; novos `confirmarCodigo()`, `reenviarCodigo()`, `listarUsuarios()`, `listarUsuariosLogados()` |
| `static/js/auth.js` | Login redireciona por perfil (`ADMIN` → `/admin.html`, cliente → `/`); cadastro em 2 etapas com código e botão "Reenviar código" |
| `static/cadastro.html` | Nova etapa de confirmação (campo de 6 dígitos + reenvio) |
| `static/admin.html` | Abas **Produtos** / **Usuários**, cards de estatísticas de usuários e tabela com todos os usuários. Novos estilos (abas, badges de perfil/status) |
| `static/js/admin.js` | Carrega e renderiza todos os usuários (`GET /admin/usuarios`), estatísticas (total, clientes, admins, ativas), busca com filtro e troca de abas |

---

## 4. Como funciona o fluxo de confirmação do cliente

1. Cliente preenche nome, e-mail e senha em `/cadastro.html` → `POST /auth/cadastro`.
2. O backend cria o usuário com perfil `USER` e `ativo=false`, gera um código de 6 números
   (`SecureRandom`, ex.: `042317`) válido por 15 minutos e envia por e-mail.
3. A tela troca para a etapa do código: o cliente digita os 6 números → `POST /auth/confirmar-codigo`.
4. Só então a conta é ativada (`ativo=true`) e o login é liberado. Antes disso o login responde
   403 com "Usuário não ativado. Digite o código de 6 números enviado para o seu e-mail...".
5. Se o código expirar ou não chegar, o cliente usa "Reenviar código" → `POST /auth/reenviar-codigo`
   (gera um novo código e invalida o anterior).

### Sobre o envio real de e-mail (transparência)
Igual ao comprovante de compra, o envio depende de SMTP. Por padrão o projeto está com
`app.email.habilitado=false`. Nesse modo:
- o código **não é enviado por e-mail**;
- o código aparece **no log do backend** (`E-mail desabilitado. Código de confirmação de ...: 123456`);
- a resposta da API inclui `codigoDev` e a tela exibe o aviso "Modo desenvolvimento ... seu código é ...".

Para enviar de verdade, preencha o `.env`:

```
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=seuemail@gmail.com
SMTP_PASSWORD=senha-de-app-do-gmail
APP_EMAIL_HABILITADO=true
```

Com o SMTP habilitado, `codigoDev` volta sempre `null` e o código só existe no e-mail do cliente.

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
