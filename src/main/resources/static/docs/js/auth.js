/* ============================================
   Coretech - Lógica de Autenticação
   ============================================ */

document.addEventListener('DOMContentLoaded', () => {
  const loginForm = document.getElementById('loginForm');
  const cadastroForm = document.getElementById('cadastroForm');

  // ============ LOGIN ============
  if (loginForm) {
    const alert = document.getElementById('authAlert');
    const btnEntrar = loginForm.querySelector('button[type="submit"]');
    const etapaConfirmacao = document.getElementById('etapaConfirmacao');
    const confirmacaoForm = document.getElementById('confirmacaoForm');
    const confirmacaoEmail = document.getElementById('confirmacaoEmail');
    const codigoLogin = document.getElementById('codigoLogin');
    const btnReenviarCodigoLogin = document.getElementById('btnReenviarCodigoLogin');
    const btnVoltarLogin = document.getElementById('btnVoltarLogin');

    // guarda email/senha da conta que falta confirmar: depois do código o login é liberado
    let contaPendente = null;

    const mostrarAlerta = (tipo, texto) => {
      alert.className = tipo ? `auth-alert ${tipo}` : 'auth-alert';
      alert.textContent = texto;
    };

    const entrar = (data) => {
      setAuth(data.token, {
        email: data.email,
        nome: data.nome,
        role: data.role
      });

      const role = (data.role || 'USER').toUpperCase();
      // somente o admin master vai para o painel de administração
      const destino = role === 'ADMIN' ? '/admin.html' : '/';

      mostrarAlerta('success', 'Login realizado com sucesso! Redirecionando...');

      setTimeout(() => {
        window.location.href = destino;
      }, 1200);
    };

    const abrirEtapaConfirmacao = (mensagem) => {
      loginForm.style.display = 'none';
      if (etapaConfirmacao) etapaConfirmacao.style.display = 'block';
      if (confirmacaoEmail) confirmacaoEmail.textContent = contaPendente.email;
      mostrarAlerta('error', mensagem || 'Confirme o código de 6 dígitos enviado para o seu e-mail.');

      if (codigoLogin) {
        codigoLogin.value = '';
        codigoLogin.focus();
      }
    };

    const voltarParaLogin = () => {
      contaPendente = null;
      if (etapaConfirmacao) etapaConfirmacao.style.display = 'none';
      loginForm.style.display = '';
      btnEntrar.disabled = false;
      btnEntrar.textContent = 'Entrar';

      const campoSenha = document.getElementById('senha');
      if (campoSenha) campoSenha.value = '';

      mostrarAlerta('', '');
    };

    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();

      const email = document.getElementById('email').value.trim();
      const senha = document.getElementById('senha').value;

      mostrarAlerta('', '');

      if (!email || !senha) {
        mostrarAlerta('error', 'Preencha todos os campos.');
        return;
      }

      btnEntrar.disabled = true;
      btnEntrar.textContent = 'Entrando...';

      try {
        const data = await API.login(email, senha);
        entrar(data);
      } catch (err) {
        // 428 = conta criada, mas o código de 6 dígitos ainda não foi confirmado
        if (err.precisaConfirmar) {
          contaPendente = { email, senha };
          abrirEtapaConfirmacao(err.message);
          return;
        }

        mostrarAlerta('error', err.message || 'Erro ao fazer login.');
        btnEntrar.disabled = false;
        btnEntrar.textContent = 'Entrar';
      }
    });

    // ============ CONFIRMA O CÓDIGO DE 6 DÍGITOS E ENTRA NA CONTA ============
    if (confirmacaoForm) {
      confirmacaoForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        if (!contaPendente) {
          voltarParaLogin();
          mostrarAlerta('error', 'Faça login novamente para gerar um novo código.');
          return;
        }

        const codigo = codigoLogin ? codigoLogin.value.trim() : '';

        if (!/^\d{6}$/.test(codigo)) {
          mostrarAlerta('error', 'Digite os 6 números do código enviado para o seu e-mail.');
          return;
        }

        const btn = confirmacaoForm.querySelector('button[type="submit"]');
        btn.disabled = true;
        btn.textContent = 'Confirmando...';

        try {
          await API.confirmarCodigo(contaPendente.email, codigo);
          mostrarAlerta('success', 'Conta confirmada! Entrando...');

          const data = await API.login(contaPendente.email, contaPendente.senha);
          entrar(data);
        } catch (err) {
          mostrarAlerta('error', err.message || 'Erro ao confirmar o código.');
          btn.disabled = false;
          btn.textContent = 'Confirmar e entrar';
        }
      });
    }

    // ============ REENVIAR O CÓDIGO (tela de login) ============
    if (btnReenviarCodigoLogin) {
      btnReenviarCodigoLogin.addEventListener('click', async (e) => {
        e.preventDefault();

        if (!contaPendente) {
          mostrarAlerta('error', 'Faça login novamente para gerar um novo código.');
          return;
        }

        btnReenviarCodigoLogin.textContent = 'Reenviando...';

        try {
          const data = await API.reenviarCodigo(contaPendente.email);
          mostrarAlerta('success', data.mensagem || 'Enviamos um novo código para o seu e-mail.');
        } catch (err) {
          mostrarAlerta('error', err.message || 'Erro ao reenviar o código.');
        } finally {
          btnReenviarCodigoLogin.textContent = 'Reenviar código';
        }
      });
    }

    if (btnVoltarLogin) {
      btnVoltarLogin.addEventListener('click', (e) => {
        e.preventDefault();
        voltarParaLogin();
      });
    }
  }

  // ============ CADASTRO (1: dados / 2: código de 6 números por e-mail) ============
  if (cadastroForm) {
    const etapaCodigo = document.getElementById('etapaCodigo');
    const codigoForm = document.getElementById('codigoForm');
    const codigoEmail = document.getElementById('codigoEmail');
    const btnReenviarCodigo = document.getElementById('btnReenviarCodigo');
    let emailPendente = null;

    cadastroForm.addEventListener('submit', async (e) => {
      e.preventDefault();

      const nome = document.getElementById('nome').value.trim();
      const email = document.getElementById('email').value.trim();
      const senha = document.getElementById('senha').value;
      const confirmarSenha = document.getElementById('confirmarSenha').value;
      const alert = document.getElementById('authAlert');

      alert.className = 'auth-alert';
      alert.textContent = '';

      if (!nome || !email || !senha || !confirmarSenha) {
        alert.className = 'auth-alert error';
        alert.textContent = 'Preencha todos os campos.';
        return;
      }

      if (senha.length < 6) {
        alert.className = 'auth-alert error';
        alert.textContent = 'A senha deve ter no mínimo 6 caracteres.';
        return;
      }

      if (senha !== confirmarSenha) {
        alert.className = 'auth-alert error';
        alert.textContent = 'As senhas não coincidem.';
        return;
      }

      const btn = cadastroForm.querySelector('button[type="submit"]');
      btn.disabled = true;
      btn.textContent = 'Criando conta...';

      try {
        const data = await API.cadastro(nome, email, senha);
        emailPendente = data.email || email;

        // esconde os dados e mostra a etapa do código
        cadastroForm.style.display = 'none';
        if (codigoEmail) codigoEmail.textContent = emailPendente;
        if (etapaCodigo) etapaCodigo.style.display = 'block';

        alert.className = 'auth-alert success';
        alert.textContent = data.mensagem || 'Enviamos um código de 6 números para o seu e-mail.';

        const campoCodigo = document.getElementById('codigo');
        if (campoCodigo) campoCodigo.focus();
      } catch (err) {
        alert.className = 'auth-alert error';
        alert.textContent = err.message || 'Erro ao criar conta.';
      } finally {
        btn.disabled = false;
        btn.textContent = 'Criar conta';
      }
    });

    // ============ ETAPA 2: confirma o cadastro com o código recebido ============
    if (codigoForm) {
      codigoForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const codigo = document.getElementById('codigo').value.trim();
        const alert = document.getElementById('authAlert');

        alert.className = 'auth-alert';
        alert.textContent = '';

        if (!/^\d{6}$/.test(codigo)) {
          alert.className = 'auth-alert error';
          alert.textContent = 'Digite os 6 números do código enviado para o seu e-mail.';
          return;
        }

        if (!emailPendente) {
          alert.className = 'auth-alert error';
          alert.textContent = 'Refaça o cadastro para gerar um novo código.';
          return;
        }

        const btn = codigoForm.querySelector('button[type="submit"]');
        btn.disabled = true;
        btn.textContent = 'Confirmando...';

        try {
          await API.confirmarCodigo(emailPendente, codigo);

          alert.className = 'auth-alert success';
          alert.textContent = 'Conta confirmada com sucesso! Redirecionando para o login...';

          setTimeout(() => {
            window.location.href = '/login.html';
          }, 1500);
        } catch (err) {
          alert.className = 'auth-alert error';
          alert.textContent = err.message || 'Erro ao confirmar o código.';
          btn.disabled = false;
          btn.textContent = 'Confirmar código';
        }
      });
    }

    // ============ REENVIAR CÓDIGO ============
    if (btnReenviarCodigo) {
      btnReenviarCodigo.addEventListener('click', async (e) => {
        e.preventDefault();

        const alert = document.getElementById('authAlert');

        if (!emailPendente) {
          alert.className = 'auth-alert error';
          alert.textContent = 'Refaça o cadastro para gerar um novo código.';
          return;
        }

        btnReenviarCodigo.textContent = 'Reenviando...';

        try {
          const data = await API.reenviarCodigo(emailPendente);

          alert.className = 'auth-alert success';
          alert.textContent = data.mensagem || 'Novo código enviado para o seu e-mail.';
        } catch (err) {
          alert.className = 'auth-alert error';
          alert.textContent = err.message || 'Erro ao reenviar o código.';
        } finally {
          btnReenviarCodigo.textContent = 'Reenviar código';
        }
      });
    }
  }
});