# Relatório — Persistência em banco de dados (MySQL)

**Data:** 05/10/2026
**Solicitante:** usuário do projeto CoreTech
**Objetivo:** fazer com que a aplicação **salve os dados no banco de dados** (cadastros, produtos, carrinhos e pedidos), em vez de perdê-los a cada reinício.

---

## 1. Diagnóstico

| Item | Situação antes | Situação depois |
|---|---|---|
| Perfil ativo | `test` (H2 **em memória**: `jdbc:h2:mem:testdb`) | `prod` (**MySQL**: `jdbc:mysql://localhost:3306/coretech`) |
| Persistência | ❌ Banco apagado a cada reinício da aplicação | ✅ Dados permanentes no MySQL |
| Servidor MySQL | Já rodando (`MySQL80`, porta 3306) — **não era usado** | Em uso pela aplicação |
| Banco `coretech` | Já existia com o schema, porém **vazio** (0 registros) | Populado (seed + admin master) e gravando normalmente |
| Catálogo de produtos | Seed só existia no perfil `test` → ficaria **vazio** no MySQL | Seed liberado também no perfil `prod` (idempotente) |

Causa raiz do problema: `spring.profiles.active=test` no `application.properties` apontava
o Hibernate para um H2 temporário em memória. O `application-prod.properties` (com as
credenciais MySQL do `.env`) já existia e estava completo, mas nunca era ativado.

## 2. Alterações feitas (apenas 3 arquivos)

| Arquivo | Mudança |
|---|---|
| `src/main/resources/application.properties` | `spring.profiles.active=test` → `spring.profiles.active=prod` |
| `src/main/java/.../config/TesteConfig.java` | `@Profile("test")` → `@Profile({"test", "prod"})` — o seed de 8 produtos + cliente `tom@gmail.com` passa a rodar também no MySQL. É **idempotente**: só cria o que não existe, nunca duplica. |
| `.gitignore` | Ignora `app_*.log` (logs de execução local) |

Nenhuma alteração em Java/JS de regra de negócio, no `.env` ou no `pom.xml`
(driver `mysql-connector-j` já estava presente).

## 3. Validação executada ( ponta a ponta )

| # | Teste | Resultado |
|---|---|---|
| 1 | Subida da aplicação no perfil `prod` | ✅ `Started SistemaCoreTechApplication in 9.852s` conectando ao MySQL |
| 2 | Seed idempotente no MySQL | ✅ `produtos = 8`, `usuarios = 2` (`tom` + `admin`) |
| 3 | Cadastro `POST /auth/cadastro` | ✅ HTTP **201**, `"emailEnviado": true` (SMTP OK no perfil prod), registro gravado no MySQL (`id=3`, `ativo=false`) |
| 4 | **Reinício completo da aplicação** | ✅ Aplicação encerrada e subida de novo |
| 5 | **Conta sobreviveu ao reinício** | ✅ Login da conta criada no passo 3 → HTTP **428** (conta existe, aguardando código). Antes, com o H2 em memória, o mesmo login daria **401 "Credenciais inválidas"** |
| 6 | Seed não duplicou após reinício | ✅ `produtos` continuou com **8** linhas |
| 7 | Limpeza do dado de teste | ✅ `teste.persistencia@gmail.com` removido; restam `tom@gmail.com` e `admin@gmail.com` |

## 4. Estado final

- Aplicação **rodando** em `http://localhost:8080` no perfil `prod` (MySQL).
- Banco `coretech` no MySQL com: 8 produtos, cliente demo `tom@gmail.com` / `123456`
  e admin master `admin@gmail.com` / `admin123`.
- Login/seed/admin funcionando; e-mails de confirmação continuam sendo enviados (SMTP já corrigido no relatório anterior).

## 5. Observações e cuidados

1. **Cliente demo em produção:** com o seed liberado no perfil `prod`, a conta
   `tom@gmail.com` / `123456` é criada no MySQL. Se não quiser esse usuário em produção,
   basta apagá-lo pelo painel/administração ou por SQL (`DELETE FROM usuarios WHERE email='tom@gmail.com';`).
2. **Tabela `produtos_imagem_produto` com 0 linhas:** é um resquício do schema antigo;
   `ddl-auto=update` não apaga tabelas. É inofensiva (as imagens atuais ficam em
   `produto_imagem_produto`, com 8 registros).
3. **Arquivo `data/coretechdb.mv.db`:** resquício de uma configuração antiga de H2 em
   arquivo; nada referencia esse arquivo hoje. Pode ser apagado com segurança se desejar.
4. **Para voltar ao modo de teste (H2 em memória):** altere em `application.properties`
   `spring.profiles.active=prod` para `spring.profiles.active=test`. Não é necessário
   MySQL nesse modo (o console H2 fica em `/h2-console`).
5. **Credenciais:** o MySQL usa `root` / senha do `.env` (`MYSQL_PASSWORD`), que não é
   versionado pelo Git.
6. **Porta 8080 ocupada:** se você subir a aplicação pela IDE enquanto a instância de
   teste estiver no ar, encerre-a antes (ou a IDE mostrará "Port already in use").

---
*Validação realizada executando a aplicação real contra o MySQL real — nenhum mock usado.*
