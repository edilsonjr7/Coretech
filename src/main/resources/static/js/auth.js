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

        alert.className = 'auth-alert success';
        alert.textContent = 'Login realizado com sucesso! Redirecionando...';

        setTimeout(() => {
          window.location.href = '/';
        }, 1200);
      } catch (err) {
        alert.className = 'auth-alert error';
        alert.textContent = err.message || 'Erro ao fazer login.';
        btn.disabled = false;
        btn.textContent = 'Entrar';
      }
    });
  }

  // ============ CADASTRO ============
  if (cadastroForm) {
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
        await API.cadastro(nome, email, senha);

        alert.className = 'auth-alert success';
        alert.textContent = 'Conta criada com sucesso! Redirecionando para login...';

        setTimeout(() => {
          window.location.href = '/login.html';
        }, 1500);
      } catch (err) {
        alert.className = 'auth-alert error';
        alert.textContent = err.message || 'Erro ao criar conta.';
        btn.disabled = false;
        btn.textContent = 'Criar conta';
      }
    });
  }
});