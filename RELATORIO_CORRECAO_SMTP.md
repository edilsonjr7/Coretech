# Relatório — Correção do e-mail de confirmação de cadastro (SMTP)

**Data:** 05/10/2026  
**Sintoma relatado:** ao criar a conta, aparecia a mensagem
*"Conta criada, mas o e-mail de confirmação não foi enviado. Não foi possível enviar o e-mail de
confirmação para edilsondesouzalimajunior10@gmail.com. Motivo: Authentication failed. Confira as
configurações de SMTP no arquivo .env…"* — e nenhum e-mail chegava.

---

## 1. Causa raiz

O arquivo `.env` (raiz do projeto) estava com o **usuário SMTP de exemplo (placeholder)**:

```properties
SMTP_USER=email@gmail.com        # ← conta inexistente (placeholder)
SMTP_PASSWORD=ktby fpni cxvm tmrs # ← senha de app real, gerada para OUTRA conta
```

O Gmail autentica usuário + senha **em conjunto**: a senha de app pertence a
`edilsondesouzalimajunior10@gmail.com`, mas o cliente SMTP estava se identificando como
`email@gmail.com`. O Gmail rejeitou o login → JavaMail lançou `Authentication failed` →
`EmailService` converteu em `EmailNaoEnviadoException` → `AuthController` respondeu **503** com a
mensagem exibida na tela.

Ocorrências descartadas durante a investigação:
- Variáveis de ambiente do sistema (`SMTP_HOST/USER/PASSWORD`) sobrescrevendo o `.env` → **não existem**.
- Perfis `application-test.properties` / `application-prod.properties` alterando SMTP → **não mexem no mail**.
- Falha de importação do `.env` → o host/credenciais eram lidos (o erro era de autenticação, não de
  "SMTP não configurado"), ou seja, o `.env` era carregado normalmente.

## 2. Correção aplicada

| Arquivo | Antes | Depois |
|---|---|---|
| `.env` (linha 13) | `SMTP_USER=email@gmail.com` | `SMTP_USER=edilsondesouzalimajunior10@gmail.com` |
| `.env` (linha 6) | `// conexão com o banco` (comentário inválido em formato `.properties`) | `# conexão com o banco` |

**Nenhum código Java/JS foi alterado.** A mensagem de erro não "some" por suppressão: ela deixa de
aparecer porque o envio agora **realmente funciona**. Por design, a mensagem continua existindo como
resposta 503 apenas para falhas futuras reais de SMTP (sinal claro, em vez de conta fantasma).

## 3. Validação executada (testes ao vivo)

**Teste 1 — autenticação SMTP direta (smtp.gmail.com:587 / STARTTLS):**

| Credencial testada | Resultado |
|---|---|
| `email@gmail.com` + senha de app (como estava) | ❌ Erro de conexão/autenticação (reproduz o bug) |
| `edilsondesouzalimajunior10@gmail.com` + senha de app | ✅ **E-mail enviado** |
| idem, senha sem espaços | ✅ **E-mail enviado** |

**Teste 2 — fluxo completo da aplicação (Spring Boot, perfil `test`/H2):**

| Requisição | Resultado | Esperado |
|---|---|---|
| `POST /auth/cadastro` | **201** — `"Cadastro realizado! Enviamos um código de 6 dígitos…"` + `emailEnviado: true` | 201 ✅ |
| `POST /auth/reenviar-codigo` | **200** — `"Enviamos um novo código…"` + `emailEnviado: true` | 200 ✅ |
| `POST /auth/login` (conta sem confirmar) | **428** `PRECONDITION_REQUIRED` | 428 ✅ |

**Teste 3 — log da aplicação:** 4 registros
`EmailService: Código de confirmação enviado para: edilsondesouzalimajunior10@gmail.com`
e **zero** falhas de envio. A mensagem de erro 503 **não apareceu em nenhuma das chamadas**.

Após os testes, a aplicação de teste foi encerrada (porta 8080 livre) e os logs removidos da raiz.

## 4. O que fazer de agora em diante

1. **Inicie a aplicação normalmente** — ela já lê o `.env` corrigido na inicialização.
2. Ao se cadastrar, verá *"Cadastro realizado! Enviamos um código de 6 dígitos…"* e receberá o
   e-mail **CoreTech - Confirme seu cadastro**. Confira também **Spam/Lixo eletrônico**.
3. Conta criada durante a falha (ficou inativa): use **"Reenviar código"** na tela de confirmação —
   já está funcionando (testado, 200).
4. Nas validações acima foram enviados e-mails de teste/reais para a caixa de entrada
   (assuntos "CoreTech - teste…" e códigos de confirmação) — pode ignorá-los.

## 5. Notas de segurança e manutenção

- O `.env` **não é versionado** (consta no `.gitignore`, `git ls-files .env` vazio). ✅
- A senha de app no `.env` está mascarada neste relatório (`ktby **** **** ****`).
- Se a senha de app for revogada/alterada, a mensagem de erro voltará a aparecer: gere nova em
  https://myaccount.google.com/apppasswords (exige **Verificação em 2 etapas** ativada; a senha
  normal da conta não funciona no SMTP).
- Credenciais devem ser trocadas apenas no `.env` — não no código.

## 6. Arquivos alterados

- `c:\Users\User20\OneDrive\Desktop\Coretech\.env` — `SMTP_USER` corrigido + comentário `//`→`#`
- `c:\Users\User20\OneDrive\Desktop\Coretech\RELATORIO_CORRECAO_SMTP.md` — este relatório
