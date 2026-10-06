/* ============================================
   Coretech - Lógica Principal (Home)
   ============================================ */

// ============ DADOS FICTÍCIOS (fallback) ============

const CATEGORIAS = [
  { nome: 'Consoles', icone: 'console', count: '12 produtos' },
  { nome: 'Controles', icone: 'controle', count: '18 produtos' },
  { nome: 'Headsets', icone: 'headset', count: '9 produtos' },
  { nome: 'Acessórios', icone: 'acessorio', count: '24 produtos' }
];

const PRODUTOS_FALLBACK = [
  {
    id: 1,
    nome: 'PlayStation 5',
    descricao: 'Console de nova geração com SSD ultrarrápido, ray tracing e gráficos 4K.',
    preco: 4499.00,
    precoAntigo: 4999.00,
    categoria: 'Consoles',
    imagem: 'https://images.unsplash.com/photo-1606813907291-d86efa9b94db?q=80&w=800&auto=format&fit=crop',
    avaliacao: 5.0,
    avaliacoes: 128,
    specs: ['SSD 825GB', '4K @ 120Hz', 'GPU 10.28 TFLOPS', 'Ray Tracing']
  },
  {
    id: 2,
    nome: 'Xbox Series X',
    descricao: 'O console mais poderoso da Microsoft com 12 TFLOPS de potência e Quick Resume.',
    preco: 4299.00,
    precoAntigo: 4599.00,
    categoria: 'Consoles',
    imagem: 'https://images.unsplash.com/photo-1621259182978-fbf93132d53d?q=80&w=800&auto=format&fit=crop',
    avaliacao: 4.8,
    avaliacoes: 96,
    specs: ['SSD 1TB', '4K @ 120Hz', 'GPU 12 TFLOPS', 'Quick Resume']
  },
  {
    id: 3,
    nome: 'Nintendo Switch OLED',
    descricao: 'Console híbrido com tela OLED de 7 polegadas e modo portátil e dock.',
    preco: 2499.00,
    precoAntigo: null,
    categoria: 'Consoles',
    imagem: 'https://images.unsplash.com/photo-1578303512597-81e6cc155b3e?q=80&w=800&auto=format&fit=crop',
    avaliacao: 4.9,
    avaliacoes: 210,
    specs: ['Tela OLED 7"', '64GB', 'Modo Híbrido', 'Joy-Con']
  },
  {
    id: 4,
    nome: 'Controle DualSense',
    descricao: 'Controle sem fio com feedback háptico e gatilhos adaptativos.',
    preco: 499.00,
    precoAntigo: 599.00,
    categoria: 'Controles',
    imagem: 'https://images.unsplash.com/photo-1606144042614-b2417e99c4e3?q=80&w=800&auto=format&fit=crop',
    avaliacao: 4.7,
    avaliacoes: 342,
    specs: ['Bluetooth 5.1', 'Feedback Háptico', 'Gatilhos Adaptativos', 'Bateria 12h']
  },
  {
    id: 5,
    nome: 'Controle Xbox Elite',
    descricao: 'Controle profissional com paddles traseiros e sticks intercambiáveis.',
    preco: 1299.00,
    precoAntigo: null,
    categoria: 'Controles',
    imagem: 'https://images.unsplash.com/photo-1607853202273-797f1c22a38e?q=80&w=800&auto=format&fit=crop',
    avaliacao: 4.6,
    avaliacoes: 87,
    specs: ['Paddles', 'Sticks Intercambiáveis', 'Bluetooth', 'App Xbox']
  },
  {
    id: 6,
    nome: 'Headset Gamer Pro',
    descricao: 'Headset com som surround 7.1, microfone com cancelamento de ruído e RGB.',
    preco: 349.00,
    precoAntigo: 449.00,
    categoria: 'Headsets',
    imagem: 'https://images.unsplash.com/photo-1599669454699-248893623440?q=80&w=800&auto=format&fit=crop',
    avaliacao: 4.5,
    avaliacoes: 156,
    specs: ['Surround 7.1', 'Microfone com Noise Gate', 'RGB', 'USB/3.5mm']
  },
  {
    id: 7,
    nome: 'Headset Sem Fio Pulse',
    descricao: 'Headset sem fio com áudio 3D e bateria de 30 horas de duração.',
    preco: 899.00,
    precoAntigo: null,
    categoria: 'Headsets',
    imagem: 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?q=80&w=800&auto=format&fit=crop',
    avaliacao: 4.8,
    avaliacoes: 64,
    specs: ['Áudio 3D', 'Bateria 30h', 'Sem Fio', 'Microfone Retrátil']
  },
  {
    id: 8,
    nome: 'Teclado Mecânico RGB',
    descricao: 'Teclado mecânico com switches blue, RGB por tecla e estrutura em alumínio.',
    preco: 599.00,
    precoAntigo: 699.00,
    categoria: 'Acessórios',
    imagem: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?q=80&w=800&auto=format&fit=crop',
    avaliacao: 4.7,
    avaliacoes: 189,
    specs: ['Switches Blue', 'RGB por Tecla', 'Alumínio', 'Anti-Ghosting']
  }
];

// ============ ESTADO ============

let produtos = [];
let filtroAtual = 'Todos';
let favoritos = JSON.parse(localStorage.getItem('gs_favoritos') || '[]');

// ============ ÍCONES SVG ============

const ICONES = {
  console: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="6" width="20" height="12" rx="2"/><path d="M6 12h4"/><path d="M15 9h.01"/><path d="M18 15h.01"/></svg>',
  controle: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="6" y1="11" x2="10" y2="11"/><line x1="8" y1="9" x2="8" y2="13"/><line x1="15" y1="12" x2="15.01" y2="12"/><line x1="18" y1="10" x2="18.01" y2="10"/><path d="M17.32 5H6.68a4 4 0 0 0-3.978 3.59c-.006.052-.01.101-.017.152C2.604 9.416 2 14.456 2 16a3 3 0 0 0 3 3c1 0 1.5-.5 2-1l1.414-1.414A2 2 0 0 1 9.828 16h4.344a2 2 0 0 1 1.414.586L17 18c.5.5 1 1 2 1a3 3 0 0 0 3-3c0-1.545-.604-6.584-.685-7.258-.007-.05-.011-.1-.017-.151A4 4 0 0 0 17.32 5z"/></svg>',
  headset: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 18v-6a9 9 0 0 1 18 0v6"/><path d="M21 19a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3zM3 19a2 2 0 0 0 2 2h1a2 2 0 0 0 2-2v-3a2 2 0 0 0-2-2H3z"/></svg>',
  acessorio: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 7h-9"/><path d="M14 17H5"/><circle cx="17" cy="17" r="3"/><circle cx="7" cy="7" r="3"/></svg>',
  carrinho: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>',
  coracao: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>',
  coracaoCheio: '<svg viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>',
  estrela: '<svg viewBox="0 0 24 24" fill="currentColor"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>',
  frete: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>',
  seguro: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>',
  entrega: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>',
  suporte: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>',
  busca: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>',
  usuario: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
  menu: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="18" x2="21" y2="18"/></svg>',
  fechar: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>',
  setaEsquerda: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>',
  setaDireita: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>',
  enviar: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>'
};

// ============ HEADER ============

function renderHeader() {
  const header = document.querySelector('.header');
  if (!header) return;

  const user = getUser();
  const logged = isLogged();

  header.innerHTML = `
    <div class="container header-inner">
      <a href="/" class="logo">
        Coretech
        <span class="logo-icon"></span>
      </a>

      <nav class="nav-menu" id="navMenu">
        <a href="/" class="nav-link">Home</a>
        <a href="/#produtos" class="nav-link">Produtos</a>
        <a href="/#categorias" class="nav-link">Categorias</a>
        <a href="/#ofertas" class="nav-link">Ofertas</a>
        <a href="/carrinho.html" class="nav-link">Carrinho</a>
      </nav>

      <div class="header-actions">
        ${logged && user && user.role === 'ADMIN' ? `
          <a href="/admin.html" class="icon-btn admin-badge-btn" title="Painel Admin" style="color:var(--primary);">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            <span class="desktop-only-text">Admin</span>
          </a>
        ` : ''}

        <a href="/carrinho.html" class="icon-btn cart-btn-header" title="Meu Carrinho">
          ${ICONES.carrinho}
          <span class="cart-badge" id="cartBadge" style="display:none">0</span>
        </a>

        ${logged && user ? `
          <div class="user-logged-box">
            <a href="/perfil.html" class="user-greeting" title="Ver Meu Perfil (${user.email})">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              <span>Olá, <strong>${user.nome ? user.nome.split(' ')[0] : 'Gamer'}</strong></span>
            </a>
            <button class="btn btn-outline btn-sm btn-logout" id="btnLogout" title="Sair da Conta">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
              <span class="desktop-only-text">Sair</span>
            </button>
          </div>
        ` : `
          <div class="auth-header-buttons">
            <a href="/login.html" class="btn btn-outline btn-sm">Entrar</a>
            <a href="/cadastro.html" class="btn btn-primary btn-sm">Cadastrar</a>
          </div>
        `}

        <button class="menu-toggle" id="menuToggle" title="Menu">
          ${ICONES.menu}
        </button>
      </div>
    </div>
  `;

  // Menu mobile
  const menuToggle = document.getElementById('menuToggle');
  const navMenu = document.getElementById('navMenu');

  if (menuToggle && navMenu) {
    menuToggle.addEventListener('click', () => {
      navMenu.classList.toggle('open');
    });

    // Fechar menu ao clicar em link
    navMenu.querySelectorAll('.nav-link').forEach(link => {
      link.addEventListener('click', () => navMenu.classList.remove('open'));
    });
  }

  // Logout
  const btnLogout = document.getElementById('btnLogout');
  if (btnLogout) {
    btnLogout.addEventListener('click', async () => {
      await API.logout();
      showToast('Logout realizado com sucesso!', 'success');
      setTimeout(() => {
        window.location.href = '/';
      }, 800);
    });
  }

  // Atualizar badge do carrinho
  if (logged) {
    atualizarBadgeCarrinho();
  }
}

async function atualizarBadgeCarrinho() {
  try {
    const carrinho = await API.verCarrinho();
    const badge = document.getElementById('cartBadge');
    if (badge) {
      const total = carrinho.itens ? carrinho.itens.length : 0;
      badge.textContent = total;
      badge.style.display = total > 0 ? 'flex' : 'none';
    }
  } catch (e) {
    // Silencioso
  }
}

// ============ CATEGORIAS ============

function renderCategorias() {
  const grid = document.getElementById('categoriasGrid');
  if (!grid) return;

  grid.innerHTML = CATEGORIAS.map(cat => `
    <div class="category-card" data-categoria="${cat.nome}">
      <div class="category-icon">${ICONES[cat.icone]}</div>
      <h3 class="category-name">${cat.nome}</h3>
      <p class="category-count">${cat.count}</p>
    </div>
  `).join('');

  // Clicar em categoria filtra produtos
  grid.querySelectorAll('.category-card').forEach(card => {
    card.addEventListener('click', () => {
      const categoria = card.dataset.categoria;
      setFiltro(categoria);
      document.getElementById('produtos').scrollIntoView({ behavior: 'smooth' });
    });
  });
}

// ============ PRODUTOS ============

async function carregarProdutos() {
  const grid = document.getElementById('produtosGrid');
  if (!grid) return;

  grid.innerHTML = '<div class="loading"><div class="spinner"></div></div>';

  try {
    const data = await API.listarProdutos();
    if (data && data.length > 0) {
      produtos = data.map(p => ({
        ...p,
        categoria: p.categoria || 'Consoles',
        preco: Number(p.preco),
        imagem: p.imagemProduto && p.imagemProduto.length > 0
          ? p.imagemProduto[0]
          : 'https://images.unsplash.com/photo-1606813907291-d86efa9b94db?q=80&w=800&auto=format&fit=crop'
      }));
    } else {
      produtos = PRODUTOS_FALLBACK;
    }
  } catch (e) {
    console.warn('Usando dados fictícios:', e.message);
    produtos = PRODUTOS_FALLBACK;
  }

  renderProdutos();
}

function getProdutosFiltrados() {
  let lista = [...produtos];

  if (filtroAtual !== 'Todos') {
    lista = lista.filter(p => p.categoria === filtroAtual);
  }

  const sort = document.getElementById('sortSelect');
  if (sort) {
    const valor = sort.value;
    if (valor === 'menor') {
      lista.sort((a, b) => a.preco - b.preco);
    } else if (valor === 'maior') {
      lista.sort((a, b) => b.preco - a.preco);
    } else if (valor === 'nome') {
      lista.sort((a, b) => a.nome.localeCompare(b.nome));
    }
  }

  return lista;
}

function renderProdutos() {
  const grid = document.getElementById('produtosGrid');
  if (!grid) return;

  const lista = getProdutosFiltrados();

  if (lista.length === 0) {
    grid.innerHTML = '<p style="text-align:center;color:var(--text-secondary);padding:40px;">Nenhum produto encontrado.</p>';
    return;
  }

  grid.innerHTML = lista.map(p => {
    const isFav = favoritos.includes(p.id);
    const desconto = p.precoAntigo ? Math.round((1 - p.preco / p.precoAntigo) * 100) : null;

    return `
      <div class="product-card" data-id="${p.id}">
        ${desconto ? `<span class="product-badge">-${desconto}%</span>` : ''}
        <button class="favorite-btn ${isFav ? 'active' : ''}" data-id="${p.id}" title="Favoritar">
          ${isFav ? ICONES.coracaoCheio : ICONES.coracao}
        </button>
        <div class="product-image">
          <img src="${p.imagem}" alt="${p.nome}" loading="lazy">
        </div>
        <div class="product-info">
          <h3 class="product-name">${p.nome}</h3>
          <div class="product-price">
            ${formatarPreco(p.preco)}
            ${p.precoAntigo ? `<span class="old-price">${formatarPreco(p.precoAntigo)}</span>` : ''}
          </div>
          <button class="add-cart-btn" data-id="${p.id}">
            ${ICONES.carrinho}
            Adicionar ao Carrinho
          </button>
        </div>
      </div>
    `;
  }).join('');

  // Eventos
  grid.querySelectorAll('.product-card').forEach(card => {
    card.addEventListener('click', (e) => {
      if (e.target.closest('.favorite-btn') || e.target.closest('.add-cart-btn')) return;
      const id = Number(card.dataset.id);
      const produto = produtos.find(p => p.id === id);
      if (produto) abrirModal(produto);
    });
  });

  grid.querySelectorAll('.favorite-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      toggleFavorito(Number(btn.dataset.id), btn);
    });
  });

  grid.querySelectorAll('.add-cart-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      adicionarAoCarrinho(Number(btn.dataset.id));
    });
  });
}

function setFiltro(categoria) {
  filtroAtual = categoria;

  document.querySelectorAll('.filter-tab').forEach(tab => {
    tab.classList.toggle('active', tab.dataset.filtro === categoria);
  });

  renderProdutos();
}

function toggleFavorito(id, btn) {
  const index = favoritos.indexOf(id);
  if (index >= 0) {
    favoritos.splice(index, 1);
    btn.classList.remove('active');
    btn.innerHTML = ICONES.coracao;
    showToast('Removido dos favoritos', 'success');
  } else {
    favoritos.push(id);
    btn.classList.add('active');
    btn.innerHTML = ICONES.coracaoCheio;
    showToast('Adicionado aos favoritos', 'success');
  }
  localStorage.setItem('gs_favoritos', JSON.stringify(favoritos));
}

async function adicionarAoCarrinho(produtoId) {
  try {
    await API.adicionarAoCarrinho(produtoId, 1);
    showToast('Produto adicionado ao carrinho!', 'success');
    atualizarBadgeCarrinho();
  } catch (e) {
    if (e.message.includes('login')) {
      showToast('Faça login para adicionar ao carrinho', 'error');
      setTimeout(() => window.location.href = '/login.html', 1500);
    } else {
      showToast(e.message, 'error');
    }
  }
}

// ============ MODAL ============

function abrirModal(produto) {
  const overlay = document.getElementById('modalOverlay');
  if (!overlay) return;

  const isFav = favoritos.includes(produto.id);

  overlay.innerHTML = `
    <div class="modal">
      <div class="modal-content">
        <div class="modal-image">
          <img src="${produto.imagem}" alt="${produto.nome}">
        </div>
        <div class="modal-info">
          <button class="modal-close" id="modalClose">${ICONES.fechar}</button>
          <p class="modal-category">${produto.categoria || 'Produto'}</p>
          <h2 class="modal-title">${produto.nome}</h2>
          <div class="modal-rating">
            <div class="stars">
              ${ICONES.estrela}${ICONES.estrela}${ICONES.estrela}${ICONES.estrela}${ICONES.estrela}
            </div>
            <span class="rating-value">${produto.avaliacao || '5.0'}</span>
            <span class="rating-count">(${produto.avaliacoes || 0} avaliações)</span>
          </div>
          <p class="modal-description">${produto.descricao}</p>
          <div class="modal-specs">
            <p class="modal-specs-title">Especificações</p>
            <div class="specs-list">
              ${(produto.specs || ['SSD 825GB', '4K @ 120Hz', 'GPU 10.28 TFLOPS', 'Ray Tracing']).map(spec => `
                <span class="spec-item">${spec}</span>
              `).join('')}
            </div>
          </div>
          <div class="modal-price">${formatarPreco(produto.preco)}</div>
          <div class="modal-actions">
            <button class="btn btn-primary" id="modalAddCart">
              ${ICONES.carrinho}
              Adicionar ao Carrinho
            </button>
            <button class="modal-fav ${isFav ? 'active' : ''}" id="modalFav" title="Favoritar">
              ${isFav ? ICONES.coracaoCheio : ICONES.coracao}
            </button>
          </div>
        </div>
      </div>
    </div>
  `;

  overlay.classList.add('active');
  document.body.style.overflow = 'hidden';

  // Fechar
  const fechar = () => {
    overlay.classList.remove('active');
    document.body.style.overflow = '';
  };

  document.getElementById('modalClose').addEventListener('click', fechar);
  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) fechar();
  });

  document.addEventListener('keydown', function handler(e) {
    if (e.key === 'Escape') {
      fechar();
      document.removeEventListener('keydown', handler);
    }
  });

  // Adicionar ao carrinho
  document.getElementById('modalAddCart').addEventListener('click', () => {
    adicionarAoCarrinho(produto.id);
  });

  // Favoritar
  document.getElementById('modalFav').addEventListener('click', (e) => {
    const btn = e.currentTarget;
    toggleFavorito(produto.id, btn);
  });
}

// ============ BENEFÍCIOS ============

function renderBeneficios() {
  const grid = document.getElementById('beneficiosGrid');
  if (!grid) return;

  const beneficios = [
    { icone: 'frete', titulo: 'Frete Grátis', desc: 'Em compras acima de R$ 299' },
    { icone: 'seguro', titulo: 'Pagamento Seguro', desc: 'Criptografia SSL de ponta a ponta' },
    { icone: 'entrega', titulo: 'Entrega em 48h', desc: 'Para todo o Brasil' },
    { icone: 'suporte', titulo: 'Suporte 24/7', desc: 'Atendimento especializado' }
  ];

  grid.innerHTML = beneficios.map(b => `
    <div class="benefit-card">
      <div class="benefit-icon">${ICONES[b.icone]}</div>
      <h3 class="benefit-title">${b.titulo}</h3>
      <p class="benefit-desc">${b.desc}</p>
    </div>
  `).join('');
}

// ============ FILTROS ============

function initFiltros() {
  document.querySelectorAll('.filter-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      setFiltro(tab.dataset.filtro);
    });
  });

  const sortSelect = document.getElementById('sortSelect');
  if (sortSelect) {
    sortSelect.addEventListener('change', renderProdutos);
  }
}

// ============ NEWSLETTER ============

function initNewsletter() {
  const form = document.getElementById('newsletterForm');
  if (!form) return;

  form.addEventListener('submit', (e) => {
    e.preventDefault();
    const input = form.querySelector('input');
    if (input.value.trim()) {
      showToast('Inscrição realizada com sucesso!', 'success');
      input.value = '';
    }
  });
}

// ============ INIT ============

document.addEventListener('DOMContentLoaded', () => {
  renderHeader();
  renderCategorias();
  renderBeneficios();
  initFiltros();
  initNewsletter();
  carregarProdutos();
});