/* ============================================
   GameStore - Lógica de Autenticação
   ============================================ */

document.addEventListener('DOMContentLoaded', () => {
  const loginForm = document.getElementById('loginForm');
  const cadastroForm = document.getElementById('cadastroForm');

  // ============ LOGIN ============
  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();

      const email = document.getElementById('email').value.trim();
      const senha = document.getElementById('senha').value;
      const alert = document.getElementById('authAlert');

      alert.className = 'auth-alert';
      alert.textContent = '';

      if (!email || !senha) {
        alert.className = 'auth-alert error';
        alert.textContent = 'Preencha todos os campos.';
        return;
      }

      const btn = loginForm.querySelector('button[type="submit"]');
      btn.disabled = true;
      btn.textContent = 'Entrando...';

      try {
        const data = await API.login(email, senha);
        setAuth(data.token, {
          email: data.email,
          nome: data.nome,
          role: data.role
        });

        const role = (data.role || 'USER').toUpperCase();
        // somente o admin master vai para o painel de administração
        const destino = role === 'ADMIN' ? '/admin.html' : '/';

        alert.className = 'auth-alert success';
        alert.textContent = 'Login realizado com sucesso! Redirecionando...';

        setTimeout(() => {
          window.location.href = destino;
        }, 1200);
      } catch (err) {
        alert.className = 'auth-alert error';
        alert.textContent = err.message || 'Erro ao fazer login.';
        btn.disabled = false;
        btn.textContent = 'Entrar';
      }
    });
  }

  // ============ CADASTRO (1: dados / 2: código de 6 números por e-mail) ============
  if (cadastroForm) {
    const etapaCodigo = document.getElementById('etapaCodigo');
    const codigoForm = document.getElementById('codigoForm');
    const codigoDica = document.getElementById('codigoDica');
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
        if (codigoDica) {
          if (data.codigoDev) {
            codigoDica.style.display = 'block';
            codigoDica.textContent = 'Modo desenvolvimento (e-mail desabilitado): seu código é ' + data.codigoDev;
          } else {
            codigoDica.style.display = 'none';
          }
        }
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

          if (codigoDica && data.codigoDev) {
            codigoDica.style.display = 'block';
            codigoDica.textContent = 'Modo desenvolvimento (e-mail desabilitado): seu novo código é ' + data.codigoDev;
          }
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