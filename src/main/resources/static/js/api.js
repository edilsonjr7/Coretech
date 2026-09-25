/* ============================================
   GameStore - Camada de API (Backend Connection)
   ============================================ */

const API = {
  baseUrl: '',

  // ============ PRODUTOS ============

  async listarProdutos() {
    const res = await fetch(`${this.baseUrl}/produtos`);
    if (!res.ok) throw new Error('Erro ao carregar produtos');
    return res.json();
  },

  async buscarProduto(id) {
    const res = await fetch(`${this.baseUrl}/produtos/${id}`);
    if (!res.ok) throw new Error('Produto não encontrado');
    return res.json();
  },

  async buscarPorNome(nome) {
    const res = await fetch(`${this.baseUrl}/produtos/busca?nome=${encodeURIComponent(nome)}`);
    if (!res.ok) throw new Error('Erro na busca');
    return res.json();
  },

  // ============ PRODUTOS (ADMIN - CRUD) ============

  async criarProduto(produto) {
    const token = localStorage.getItem('gs_token');
    if (!token) throw new Error('Faça login como admin');

    const res = await fetch(`${this.baseUrl}/produtos`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(produto)
    });

    if (!res.ok) {
      const erro = await res.text();
      throw new Error(erro || 'Erro ao criar produto');
    }

    return res.json();
  },

  async atualizarProduto(id, produto) {
    const token = localStorage.getItem('gs_token');
    if (!token) throw new Error('Faça login como admin');

    const res = await fetch(`${this.baseUrl}/produtos/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(produto)
    });

    if (!res.ok) {
      const erro = await res.text();
      throw new Error(erro || 'Erro ao atualizar produto');
    }

    return res.json();
  },

  async deletarProduto(id) {
    const token = localStorage.getItem('gs_token');
    if (!token) throw new Error('Faça login como admin');

    const res = await fetch(`${this.baseUrl}/produtos/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (!res.ok) {
      const erro = await res.text();
      throw new Error(erro || 'Erro ao deletar produto');
    }

    return res.ok;
  },

  // ============ AUTH ============

  async login(email, senha) {
    const res = await fetch(`${this.baseUrl}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, senha })
    });

    if (!res.ok) {
      const erro = await res.text();
      throw new Error(erro || 'Credenciais inválidas');
    }

    return res.json();
  },

  async cadastro(nome, email, senha) {
    const res = await fetch(`${this.baseUrl}/auth/cadastro`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nome, email, senha })
    });

    if (!res.ok) {
      const erro = await res.text();
      throw new Error(erro || 'Erro ao cadastrar');
    }

    // resposta: { mensagem, email, emailEnviado, codigoDev }
    return res.json();
  },

  // Confirma o cadastro com o código de 6 números recebido por e-mail
  async confirmarCodigo(email, codigo) {
    const res = await fetch(`${this.baseUrl}/auth/confirmar-codigo`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, codigo })
    });

    if (!res.ok) {
      const erro = await res.text();
      throw new Error(erro || 'Erro ao confirmar o código');
    }

    return res.text();
  },

  // Gera e envia um novo código de 6 números
  async reenviarCodigo(email) {
    const res = await fetch(`${this.baseUrl}/auth/reenviar-codigo`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email })
    });

    if (!res.ok) {
      const erro = await res.text();
      throw new Error(erro || 'Erro ao reenviar o código');
    }

    return res.json();
  },

  async logout() {
    const token = localStorage.getItem('gs_token');
    if (!token) return;

    const res = await fetch(`${this.baseUrl}/auth/logout`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` }
    });

    localStorage.removeItem('gs_token');
    localStorage.removeItem('gs_user');
    return res.ok;
  },

  // ============ ADMIN ============

  // Lista todos os usuários cadastrados (apenas ADMIN master)
  async listarUsuarios() {
    const token = localStorage.getItem('gs_token');
    if (!token) throw new Error('Faça login como admin');

    const res = await fetch(`${this.baseUrl}/admin/usuarios`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (!res.ok) throw new Error('Erro ao carregar usuários');
    return res.json();
  },

  // Usuários com login ativo no momento
  async listarUsuariosLogados() {
    const token = localStorage.getItem('gs_token');
    if (!token) throw new Error('Faça login como admin');

    const res = await fetch(`${this.baseUrl}/admin/usuarios-logados`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (!res.ok) throw new Error('Erro ao carregar usuários logados');
    return res.json();
  },

  // ============ CARRINHO ============

  async verCarrinho() {
    const token = localStorage.getItem('gs_token');
    if (!token) throw new Error('Faça login para ver o carrinho');

    const res = await fetch(`${this.baseUrl}/carrinho`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (!res.ok) throw new Error('Erro ao carregar carrinho');
    return res.json();
  },

  async adicionarAoCarrinho(produtoId, quantidade = 1) {
    const token = localStorage.getItem('gs_token');
    if (!token) throw new Error('Faça login para adicionar ao carrinho');

    const res = await fetch(`${this.baseUrl}/carrinho/itens`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ produtoId, quantidade })
    });

    if (!res.ok) throw new Error('Erro ao adicionar ao carrinho');
    return res.json();
  },

  async removerItemCarrinho(itemId) {
    const token = localStorage.getItem('gs_token');
    if (!token) throw new Error('Faça login');

    const res = await fetch(`${this.baseUrl}/carrinho/itens/${itemId}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (!res.ok) throw new Error('Erro ao remover item');
    return res.json();
  }
};

/* ============================================
   Utilitários
   ============================================ */

function formatarPreco(valor) {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  }).format(valor);
}

function getToken() {
  return localStorage.getItem('gs_token');
}

function getUser() {
  const user = localStorage.getItem('gs_user');
  return user ? JSON.parse(user) : null;
}

function isLogged() {
  return !!getToken();
}

function setAuth(token, user) {
  localStorage.setItem('gs_token', token);
  localStorage.setItem('gs_user', JSON.stringify(user));
}

function clearAuth() {
  localStorage.removeItem('gs_token');
  localStorage.removeItem('gs_user');
}

/* ============================================
   Toast
   ============================================ */

function showToast(message, type = 'success') {
  let container = document.querySelector('.toast-container');
  if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  const icon = type === 'success'
    ? '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>'
    : '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>';

  toast.innerHTML = `${icon}<span>${message}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}