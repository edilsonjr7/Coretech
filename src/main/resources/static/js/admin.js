/* ============================================
   GameStore - Painel Admin (CRUD de Produtos)
   ============================================ */

document.addEventListener('DOMContentLoaded', () => {
  // ============ VERIFICAÇÃO DE ACESSO ============
  const user = getUser();
  if (!user || user.role !== 'ADMIN') {
    window.location.href = '/login.html';
    return;
  }

  // ============ ELEMENTOS ============
  const adminNome = document.getElementById('adminNome');
  const btnLogout = document.getElementById('btnLogout');
  const btnNovoProduto = document.getElementById('btnNovoProduto');
  const btnCancelar = document.getElementById('btnCancelar');
  const modalClose = document.getElementById('modalClose');
  const modalProduto = document.getElementById('modalProduto');
  const produtoForm = document.getElementById('produtoForm');
  const buscaProduto = document.getElementById('buscaProduto');
  const adminAlert = document.getElementById('adminAlert');

  // aba de usuários
  const abas = document.querySelectorAll('.admin-tab');
  const abaProdutos = document.getElementById('abaProdutos');
  const abaUsuarios = document.getElementById('abaUsuarios');
  const buscaUsuario = document.getElementById('buscaUsuario');
  const btnAtualizarUsuarios = document.getElementById('btnAtualizarUsuarios');

  // ============ ESTADO ============
  let produtos = [];
  let usuarios = [];
  let editandoId = null;

  // ============ INICIALIZAÇÃO ============
  adminNome.textContent = user.nome || 'Admin';
  carregarProdutos();
  carregarUsuarios();

  // ============ FUNÇÕES ============

  async function carregarProdutos() {
    const tbody = document.getElementById('produtosTableBody');
    tbody.innerHTML = '<tr><td colspan="7"><div class="loading"><div class="spinner"></div></div></td></tr>';

    try {
      produtos = await API.listarProdutos();
      atualizarEstatisticas(produtos);
      renderTabela(produtos);
    } catch (err) {
      tbody.innerHTML = `
        <tr>
          <td colspan="7">
            <div class="empty-state">
              <p>Erro ao carregar produtos do banco de dados: ${err.message}</p>
            </div>
          </td>
        </tr>
      `;
    }
  }

  function atualizarEstatisticas(lista) {
    const totalProdutos = lista ? lista.length : 0;
    const emEstoque = lista ? lista.reduce((acc, p) => acc + (Number(p.estoque) || 0), 0) : 0;
    const estoqueBaixo = lista ? lista.filter(p => Number(p.estoque) <= 5).length : 0;
    const valorTotal = lista ? lista.reduce((acc, p) => acc + ((Number(p.preco) || 0) * (Number(p.estoque) || 0)), 0) : 0;

    const elTotal = document.getElementById('statTotalProdutos');
    const elEstoque = document.getElementById('statEmEstoque');
    const elBaixo = document.getElementById('statEstoqueBaixo');
    const elValor = document.getElementById('statValorEstoque');

    if (elTotal) elTotal.textContent = totalProdutos;
    if (elEstoque) elEstoque.textContent = `${emEstoque} un`;
    if (elBaixo) elBaixo.textContent = estoqueBaixo;
    if (elValor) elValor.textContent = formatarPreco(valorTotal);
  }

  function renderTabela(lista) {
    const tbody = document.getElementById('produtosTableBody');

    if (!lista || lista.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="7">
            <div class="empty-state">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
                <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
                <line x1="12" y1="22.08" x2="12" y2="12"/>
              </svg>
              <p>Nenhum produto cadastrado no banco de dados.</p>
            </div>
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = lista.map(p => {
      const imagem = p.imagemProduto && p.imagemProduto.length > 0
        ? p.imagemProduto[0]
        : 'https://images.unsplash.com/photo-1606813907291-d86efa9b94db?q=80&w=800&auto=format&fit=crop';

      const categoria = p.categoria || 'Geral';

      let stockClass = 'stock-ok';
      let stockText = `${p.estoque} un`;
      if (p.estoque === 0) {
        stockClass = 'stock-out';
        stockText = 'Esgotado';
      } else if (p.estoque <= 5) {
        stockClass = 'stock-low';
        stockText = `${p.estoque} un (baixo)`;
      }

      return `
        <tr data-id="${p.id}">
          <td><img src="${imagem}" alt="${p.nome}" class="product-thumb" onerror="this.src='https://images.unsplash.com/photo-1606813907291-d86efa9b94db?q=80&w=800&auto=format&fit=crop'"></td>
          <td class="product-name-cell">${p.nome}</td>
          <td><span class="category-badge">${categoria}</span></td>
          <td style="max-width:250px;">${p.descricao || '-'}</td>
          <td class="product-price-cell">${formatarPreco(p.preco)}</td>
          <td><span class="stock-badge ${stockClass}">${stockText}</span></td>
          <td>
            <div class="table-actions">
              <button class="btn-edit" data-action="edit" data-id="${p.id}">Editar</button>
              <button class="btn-delete" data-action="delete" data-id="${p.id}">Excluir</button>
            </div>
          </td>
        </tr>
      `;
    }).join('');

    // Eventos de editar/excluir
    tbody.querySelectorAll('button[data-action]').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const id = Number(btn.dataset.id);
        if (btn.dataset.action === 'edit') {
          abrirModalEdicao(id);
        } else if (btn.dataset.action === 'delete') {
          confirmarExclusao(id);
        }
      });
    });
  }

  // ============ USUÁRIOS (painel com todos os usuários) ============

  async function carregarUsuarios() {
    const tbody = document.getElementById('usuariosTableBody');
    tbody.innerHTML = '<tr><td colspan="5"><div class="loading"><div class="spinner"></div></div></td></tr>';

    try {
      usuarios = await API.listarUsuarios();
      atualizarEstatisticasUsuarios(usuarios);
      renderTabelaUsuarios(usuarios);
    } catch (err) {
      tbody.innerHTML = `
        <tr>
          <td colspan="5">
            <div class="empty-state">
              <p>Erro ao carregar os usuários do banco de dados: ${err.message}</p>
            </div>
          </td>
        </tr>
      `;
    }
  }

  function atualizarEstatisticasUsuarios(lista) {
    const todos = lista || [];
    const clientes = todos.filter(u => (u.role || '').toUpperCase() !== 'ADMIN').length;
    const admins = todos.filter(u => (u.role || '').toUpperCase() === 'ADMIN').length;
    const ativos = todos.filter(u => u.ativo).length;

    const elTotal = document.getElementById('statTotalUsuarios');
    const elClientes = document.getElementById('statTotalClientes');
    const elAdmins = document.getElementById('statTotalAdmins');
    const elAtivos = document.getElementById('statTotalAtivos');

    if (elTotal) elTotal.textContent = todos.length;
    if (elClientes) elClientes.textContent = clientes;
    if (elAdmins) elAdmins.textContent = admins;
    if (elAtivos) elAtivos.textContent = ativos;
  }

  function renderTabelaUsuarios(lista) {
    const tbody = document.getElementById('usuariosTableBody');

    if (!lista || lista.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="5">
            <div class="empty-state">
              <p>Nenhum usuário cadastrado no banco de dados.</p>
            </div>
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = lista.map(u => {
      const role = (u.role || 'USER').toUpperCase();
      const roleClass = role === 'ADMIN' ? 'role-admin' : 'role-user';
      const roleTexto = role === 'ADMIN' ? 'Admin (master)' : 'Cliente';
      const statusClass = u.ativo ? 'status-ativo' : 'status-pendente';
      const statusTexto = u.ativo ? 'Ativa' : 'Aguardando código';

      return `
        <tr>
          <td>${u.id}</td>
          <td class="product-name-cell">${u.nome || '-'}</td>
          <td class="user-email-cell">${u.email || '-'}</td>
          <td><span class="role-badge ${roleClass}">${roleTexto}</span></td>
          <td><span class="status-badge ${statusClass}">${statusTexto}</span></td>
        </tr>
      `;
    }).join('');
  }

  function trocarAba(nomeAba) {
    abas.forEach(aba => aba.classList.toggle('active', aba.dataset.aba === nomeAba));

    if (abaProdutos) abaProdutos.style.display = nomeAba === 'produtos' ? 'block' : 'none';
    if (abaUsuarios) abaUsuarios.style.display = nomeAba === 'usuarios' ? 'block' : 'none';

    if (nomeAba === 'usuarios') {
      carregarUsuarios();
    }
  }

  function abrirModalNovo() {
    editandoId = null;
    document.getElementById('modalTitle').textContent = 'Novo Produto';
    document.getElementById('produtoId').value = '';
    document.getElementById('nome').value = '';
    document.getElementById('categoria').value = 'Consoles';
    document.getElementById('descricao').value = '';
    document.getElementById('preco').value = '';
    document.getElementById('estoque').value = '';
    document.getElementById('imagem').value = '';
    document.getElementById('btnSalvar').textContent = 'Salvar no Banco de Dados';
    modalProduto.classList.add('active');
  }

  function abrirModalEdicao(id) {
    const produto = produtos.find(p => p.id === id);
    if (!produto) return;

    editandoId = id;
    document.getElementById('modalTitle').textContent = 'Editar Produto';
    document.getElementById('produtoId').value = produto.id;
    document.getElementById('nome').value = produto.nome;
    document.getElementById('categoria').value = produto.categoria || 'Consoles';
    document.getElementById('descricao').value = produto.descricao || '';
    document.getElementById('preco').value = produto.preco;
    document.getElementById('estoque').value = produto.estoque;
    document.getElementById('imagem').value = produto.imagemProduto && produto.imagemProduto.length > 0
      ? produto.imagemProduto[0]
      : '';
    document.getElementById('btnSalvar').textContent = 'Atualizar no Banco de Dados';
    modalProduto.classList.add('active');
  }

  function fecharModal() {
    modalProduto.classList.remove('active');
    produtoForm.reset();
    editandoId = null;
  }

  function mostrarAlerta(mensagem, tipo = 'success') {
    adminAlert.className = `admin-alert ${tipo}`;
    adminAlert.textContent = mensagem;
    setTimeout(() => {
      adminAlert.className = 'admin-alert';
      adminAlert.textContent = '';
    }, 3000);
  }

  async function confirmarExclusao(id) {
    const produto = produtos.find(p => p.id === id);
    if (!produto) return;

    if (confirm(`Tem certeza que deseja excluir o produto "${produto.nome}" do banco de dados?`)) {
      try {
        await API.deletarProduto(id);
        mostrarAlerta('Produto excluído com sucesso do banco de dados!', 'success');
        carregarProdutos();
      } catch (err) {
        mostrarAlerta(err.message || 'Erro ao excluir produto', 'error');
      }
    }
  }

  // ============ EVENTOS ============

  // Logout
  btnLogout.addEventListener('click', async () => {
    await API.logout();
    window.location.href = '/login.html';
  });

  // Novo produto
  btnNovoProduto.addEventListener('click', abrirModalNovo);

  // Cancelar / Fechar modal
  btnCancelar.addEventListener('click', fecharModal);
  modalClose.addEventListener('click', fecharModal);
  modalProduto.addEventListener('click', (e) => {
    if (e.target === modalProduto) fecharModal();
  });

  // Fechar com ESC
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') fecharModal();
  });

  // Salvar produto
  produtoForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const nome = document.getElementById('nome').value.trim();
    const categoria = document.getElementById('categoria').value;
    const descricao = document.getElementById('descricao').value.trim();
    const preco = parseFloat(document.getElementById('preco').value);
    const estoque = parseInt(document.getElementById('estoque').value);
    const imagem = document.getElementById('imagem').value.trim();

    if (!nome || isNaN(preco) || isNaN(estoque)) {
      mostrarAlerta('Preencha todos os campos obrigatórios.', 'error');
      return;
    }

    const produtoData = {
      nome,
      categoria,
      descricao,
      preco,
      estoque,
      imagemProduto: imagem ? [imagem] : []
    };

    const btnSalvar = document.getElementById('btnSalvar');
    btnSalvar.disabled = true;
    btnSalvar.textContent = 'Salvando no banco de dados...';

    try {
      if (editandoId) {
        await API.atualizarProduto(editandoId, produtoData);
        mostrarAlerta('Produto atualizado com sucesso no banco de dados!', 'success');
      } else {
        await API.criarProduto(produtoData);
        mostrarAlerta('Produto cadastrado com sucesso no banco de dados!', 'success');
      }

      fecharModal();
      carregarProdutos();
    } catch (err) {
      mostrarAlerta(err.message || 'Erro ao salvar produto', 'error');
    } finally {
      btnSalvar.disabled = false;
      btnSalvar.textContent = editandoId ? 'Atualizar no Banco de Dados' : 'Salvar no Banco de Dados';
    }
  });

  // Busca
  let debounceTimer;
  buscaProduto.addEventListener('input', () => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      const termo = buscaProduto.value.trim().toLowerCase();
      if (!termo) {
        renderTabela(produtos);
        return;
      }
      const filtrados = produtos.filter(p =>
        p.nome.toLowerCase().includes(termo) ||
        (p.categoria && p.categoria.toLowerCase().includes(termo)) ||
        (p.descricao && p.descricao.toLowerCase().includes(termo))
      );
      renderTabela(filtrados);
    }, 300);
  });

  // ============ ABAS / USUÁRIOS ============

  // Troca entre as abas Produtos e Usuários
  abas.forEach(aba => {
    aba.addEventListener('click', () => trocarAba(aba.dataset.aba));
  });

  // Recarrega a lista de usuários do banco
  if (btnAtualizarUsuarios) {
    btnAtualizarUsuarios.addEventListener('click', async () => {
      await carregarUsuarios();
      mostrarAlerta('Lista de usuários atualizada!', 'success');
    });
  }

  // Busca de usuários por nome, e-mail ou perfil
  if (buscaUsuario) {
    let debounceUsuario;
    buscaUsuario.addEventListener('input', () => {
      clearTimeout(debounceUsuario);
      debounceUsuario = setTimeout(() => {
        const termo = buscaUsuario.value.trim().toLowerCase();
        if (!termo) {
          renderTabelaUsuarios(usuarios);
          return;
        }
        const filtrados = usuarios.filter(u =>
          (u.nome && u.nome.toLowerCase().includes(termo)) ||
          (u.email && u.email.toLowerCase().includes(termo)) ||
          (u.role && u.role.toLowerCase().includes(termo))
        );
        renderTabelaUsuarios(filtrados);
      }, 300);
    });
  }
});