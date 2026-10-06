/* ============================================
   Coretech - Lógica do Carrinho (soma por item + checkout)
   ============================================ */

// ============ ÍCONES EXTRAS (os demais vêm de ICONES em main.js) ============

const ICONES_CARRINHO = {
  mais: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>',
  menos: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/></svg>',
  lixeira: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>',
  check: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>',
  email: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>'
};

const IMAGEM_PADRAO = 'https://images.unsplash.com/photo-1606813907291-d86efa9b94db?q=80&w=800&auto=format&fit=crop';
const MAXIMO_PARCELAS = 10;

// ============ ESTADO ============

// carrinhoAtual: { itens: [{ id, quantidade, preco, subtotal, produto: {...} }], total }
let carrinhoAtual = { itens: [], total: 0 };
let finalizandoCompra = false;

// ============ INIT ============

document.addEventListener('DOMContentLoaded', () => {
  if (!isLogged() || !getUser()) {
    renderEstadoNaoLogado();
    return;
  }

  const btnCheckout = document.getElementById('btnCheckout');
  const btnClearCart = document.getElementById('btnClearCart');
  const listaItens = document.getElementById('cartItemsContainer');

  if (btnCheckout) btnCheckout.addEventListener('click', finalizarCompra);
  if (btnClearCart) btnClearCart.addEventListener('click', limparCarrinho);

  // Delegação de eventos: sobrevive a cada nova renderização dos itens
  if (listaItens) {
    listaItens.addEventListener('click', tratarCliqueItem);
    listaItens.addEventListener('change', tratarMudancaQuantidade);
  }

  // Fecha o recibo ao clicar fora do modal (registrado uma única vez)
  const modalRecibo = document.getElementById('checkoutSuccessModal');
  if (modalRecibo) {
    modalRecibo.addEventListener('click', (evento) => {
      if (evento.target === modalRecibo) fecharRecibo();
    });
  }

  carregarCarrinho();
});

// ============ ESTADOS DE TELA ============

function renderEstadoNaoLogado() {
  const lista = document.getElementById('cartItemsContainer');
  const resumo = document.getElementById('cartSummaryContainer');
  const layout = document.getElementById('cartLayout');
  const contador = document.getElementById('cartHeaderCount');

  if (resumo) resumo.style.display = 'none';
  if (layout) layout.style.gridTemplateColumns = '1fr';
  if (contador) contador.textContent = 'Entre na sua conta para continuar';

  if (lista) {
    lista.innerHTML = `
      <div class="cart-auth-required">
        <div class="cart-empty-icon">${ICONES.usuario}</div>
        <h2>Entre para ver o seu carrinho</h2>
        <p>Faça login na sua conta Coretech para somar os valores e finalizar a compra.</p>
        <a href="/login.html" class="btn btn-primary">Entrar na minha conta</a>
      </div>
    `;
  }
}

function renderErroCarrinho(mensagem) {
  const lista = document.getElementById('cartItemsContainer');
  if (!lista) return;

  lista.innerHTML = `
    <div class="cart-auth-required">
      <div class="cart-empty-icon">${ICONES.carrinho}</div>
      <h2>Não foi possível carregar o carrinho</h2>
      <p>${escapeHtml(mensagem)}</p>
      <button type="button" class="btn btn-primary" id="btnRecarregarCarrinho">Tentar novamente</button>
    </div>
  `;

  const btn = document.getElementById('btnRecarregarCarrinho');
  if (btn) btn.addEventListener('click', carregarCarrinho);
}

// ============ CARREGAR / SINCRONIZAR ============

async function carregarCarrinho() {
  const lista = document.getElementById('cartItemsContainer');
  if (lista) lista.innerHTML = '<div class="loading"><div class="spinner"></div></div>';

  try {
    const carrinho = await API.verCarrinho();
    aplicarCarrinho(carrinho);
  } catch (e) {
    renderErroCarrinho(e.message);
  }
}

/**
 * Guarda o carrinho devolvido pelo backend e calcula o valor de cada item
 * (preço x quantidade) e o total geral.
 */
function aplicarCarrinho(carrinho) {
  const itensRecebidos = carrinho && Array.isArray(carrinho.itens) ? carrinho.itens : [];

  const itens = itensRecebidos.map(item => {
    const produto = item.produto || {};
    const preco = Number(item.preco) || 0;
    const quantidade = Number(item.quantidade) || 0;

    return {
      id: Number(item.id),
      preco,
      quantidade,
      subtotal: preco * quantidade,
      produto: {
        id: Number(produto.id) || 0,
        nome: produto.nome || 'Produto',
        categoria: produto.categoria || 'Produto',
        estoque: produto.estoque === null || produto.estoque === undefined ? null : Number(produto.estoque),
        imagem: Array.isArray(produto.imagemProduto) && produto.imagemProduto.length > 0
          ? produto.imagemProduto[0]
          : IMAGEM_PADRAO
      }
    };
  });

  carrinhoAtual = {
    itens,
    total: itens.reduce((soma, item) => soma + item.subtotal, 0)
  };

  renderItens();
  renderResumo();
}


// ============ RENDER: ITENS ============

function renderItens() {
  const lista = document.getElementById('cartItemsContainer');
  const layout = document.getElementById('cartLayout');
  const vazio = document.getElementById('cartEmptyState');
  const resumo = document.getElementById('cartSummaryContainer');

  if (!lista) return;

  // Carrinho vazio: esconde o layout e mostra o estado vazio
  if (carrinhoAtual.itens.length === 0) {
    lista.innerHTML = '';
    if (layout) layout.style.display = 'none';
    if (resumo) resumo.style.display = 'none';
    if (vazio) vazio.style.display = 'block';
    return;
  }

  if (vazio) vazio.style.display = 'none';
  if (layout) {
    layout.style.display = 'grid';
    layout.style.gridTemplateColumns = '';
  }
  if (resumo) resumo.style.display = '';

  lista.innerHTML = `
    <div class="cart-items-list">
      ${carrinhoAtual.itens.map(cardItem).join('')}
    </div>
  `;
}

function cardItem(item) {
  const estoqueMaximo = item.produto.estoque;
  const noLimite = estoqueMaximo !== null && item.quantidade >= estoqueMaximo;

  return `
    <article class="cart-item-card" data-item-id="${item.id}">
      <div class="cart-item-image">
        <img src="${item.produto.imagem}" alt="${escapeHtml(item.produto.nome)}" loading="lazy">
      </div>

      <div class="cart-item-details">
        <span class="cart-item-category">${escapeHtml(item.produto.categoria)}</span>
        <h3 class="cart-item-name">${escapeHtml(item.produto.nome)}</h3>
        <span class="cart-item-unit-price">${formatarPreco(item.preco)} por unidade</span>
      </div>

      <div class="cart-item-quantity-wrapper">
        <div class="qty-control">
          <button type="button" class="qty-btn" data-acao="diminuir" title="Diminuir quantidade">${ICONES_CARRINHO.menos}</button>
          <input class="qty-input" type="number" inputmode="numeric" min="1"
                 data-acao="quantidade" value="${item.quantidade}"
                 aria-label="Quantidade de ${escapeHtml(item.produto.nome)}">
          <button type="button" class="qty-btn" data-acao="aumentar" title="Aumentar quantidade"
                  ${noLimite ? 'disabled' : ''}>${ICONES_CARRINHO.mais}</button>
        </div>
      </div>

      <div class="cart-item-subtotal">
        <span class="subtotal-label">Subtotal</span>
        <span class="subtotal-value">${formatarPreco(item.subtotal)}</span>
      </div>

      <button type="button" class="cart-item-remove-btn" data-acao="remover" title="Remover do carrinho">${ICONES_CARRINHO.lixeira}</button>
    </article>
  `;
}

// ============ RENDER: RESUMO ============

function renderResumo() {
  const contador = document.getElementById('cartHeaderCount');
  const subtotal = document.getElementById('summarySubtotal');
  const total = document.getElementById('summaryTotal');
  const parcelas = document.getElementById('summaryInstallment');
  const badge = document.getElementById('cartBadge');
  const btnCheckout = document.getElementById('btnCheckout');
  const btnClearCart = document.getElementById('btnClearCart');

  const quantidadeItens = carrinhoAtual.itens.reduce((soma, item) => soma + item.quantidade, 0);
  const totalCarrinho = carrinhoAtual.total;
  const vazio = carrinhoAtual.itens.length === 0;

  if (contador) {
    contador.textContent = vazio
      ? 'Nenhum item no carrinho'
      : `${quantidadeItens} ${quantidadeItens === 1 ? 'item' : 'itens'} no carrinho`;
  }

  // Cada item já soma o seu valor (preço x quantidade); aqui vai a soma de todos
  if (subtotal) subtotal.textContent = formatarPreco(totalCarrinho);
  if (total) total.textContent = formatarPreco(totalCarrinho);

  if (parcelas) {
    parcelas.textContent = vazio
      ? 'Adicione itens para calcular os valores'
      : `ou até ${MAXIMO_PARCELAS}x de ${formatarPreco(totalCarrinho / MAXIMO_PARCELAS)} sem juros`;
  }

  // Badge do header
  if (badge) {
    badge.textContent = carrinhoAtual.itens.length;
    badge.style.display = carrinhoAtual.itens.length > 0 ? 'flex' : 'none';
  }

  if (btnCheckout) btnCheckout.disabled = vazio || finalizandoCompra;
  if (btnClearCart) btnClearCart.disabled = vazio;
}

// ============ AÇÕES NOS ITENS ============

function tratarCliqueItem(evento) {
  const botao = evento.target.closest('button[data-acao]');
  if (!botao || botao.disabled) return;

  const card = botao.closest('.cart-item-card');
  if (!card) return;

  const item = buscarItem(card.dataset.itemId);
  if (!item) return;

  const acao = botao.dataset.acao;

  if (acao === 'remover') {
    removerItem(item, botao);
  } else if (acao === 'aumentar') {
    alterarQuantidade(item, item.quantidade + 1, botao);
  } else if (acao === 'diminuir') {
    // quantidade 1 -> o botão "-" remove o item
    if (item.quantidade <= 1) {
      removerItem(item, botao);
      return;
    }
    alterarQuantidade(item, item.quantidade - 1, botao);
  }
}

function tratarMudancaQuantidade(evento) {
  const input = evento.target.closest('input[data-acao="quantidade"]');
  if (!input) return;

  const card = input.closest('.cart-item-card');
  if (!card) return;

  const item = buscarItem(card.dataset.itemId);
  if (!item) return;

  const quantidade = parseInt(input.value, 10);

  if (!Number.isFinite(quantidade) || quantidade < 1) {
    input.value = item.quantidade;
    showToast('Quantidade mínima é 1. Use a lixeira para remover o item.', 'error');
    return;
  }

  if (quantidade === item.quantidade) return;

  alterarQuantidade(item, quantidade, null);
}

async function removerItem(item, botao) {
  const textoOriginal = botao ? botao.innerHTML : null;
  if (botao) {
    botao.disabled = true;
    botao.innerHTML = '...';
  }

  try {
    const carrinho = await API.removerItemCarrinho(item.id);
    aplicarCarrinho(carrinho);
    showToast(`${item.produto.nome} removido do carrinho`, 'success');
  } catch (e) {
    if (botao) {
      botao.disabled = false;
      botao.innerHTML = textoOriginal;
    }
    showToast(mensagemAmigavel(e), 'error');
  }
}

async function alterarQuantidade(item, novaQuantidade, botao) {
  const estoque = item.produto.estoque;

  if (estoque !== null && novaQuantidade > estoque) {
    showToast(`Estoque máximo de ${item.produto.nome}: ${estoque} unidade(s)`, 'error');
    return;
  }

  if (botao) botao.disabled = true;

  try {
    const carrinho = await API.atualizarQuantidadeItem(item.id, novaQuantidade);
    aplicarCarrinho(carrinho);
  } catch (e) {
    // volta a tela para os valores que já estavam carregados
    renderItens();
    renderResumo();
    showToast(mensagemAmigavel(e), 'error');
  }
}

async function limparCarrinho() {
  if (carrinhoAtual.itens.length === 0) {
    showToast('Seu carrinho já está vazio', 'error');
    return;
  }

  if (!window.confirm('Deseja remover todos os itens do carrinho?')) return;

  const btn = document.getElementById('btnClearCart');
  if (btn) btn.disabled = true;

  try {
    const carrinho = await API.limparCarrinho();
    aplicarCarrinho(carrinho);
    showToast('Carrinho esvaziado com sucesso!', 'success');
  } catch (e) {
    showToast(mensagemAmigavel(e), 'error');
  } finally {
    renderResumo();
  }
}

// ============ FINALIZAR COMPRA ============

async function finalizarCompra() {
  if (finalizandoCompra) return;

  if (carrinhoAtual.itens.length === 0) {
    showToast('Seu carrinho está vazio. Adicione itens antes de comprar.', 'error');
    return;
  }

  // valida o estoque no frontend antes de chamar a API, para avisar de forma clara
  const semEstoque = carrinhoAtual.itens.find(item =>
    item.produto.estoque !== null && item.quantidade > item.produto.estoque);

  if (semEstoque) {
    showToast(`Estoque insuficiente para ${semEstoque.produto.nome}. Ajuste a quantidade.`, 'error');
    return;
  }

  const btn = document.getElementById('btnCheckout');
  const conteudoOriginal = btn ? btn.innerHTML : null;

  finalizandoCompra = true;
  if (btn) {
    btn.disabled = true;
    btn.textContent = 'Finalizando compra...';
  }

  try {
    const pedido = await API.realizarCheckout();

    // o backend limpa o carrinho ao concluir o pedido: refletimos isso na tela
    aplicarCarrinho({ itens: [], total: 0 });
    abrirRecibo(pedido);
    showToast('Compra realizada com sucesso!', 'success');
  } catch (e) {
    showToast(mensagemAmigavel(e), 'error');
  } finally {
    finalizandoCompra = false;
    if (btn) btn.innerHTML = conteudoOriginal;
    renderResumo();
  }
}

// ============ RECIBO DO PEDIDO ============

function abrirRecibo(pedido) {
  const modal = document.getElementById('checkoutSuccessModal');
  const detalhes = document.getElementById('orderReceiptDetails');
  if (!modal || !detalhes) return;

  const itens = Array.isArray(pedido.itensDescricao) ? pedido.itensDescricao : [];
  const statusEmail = pedido.statusEmail || 'Comprovante da compra gerado com sucesso.';

  detalhes.innerHTML = `
    <div class="receipt-header-info">
      <div>
        <span class="receipt-label">Pedido</span>
        <strong class="receipt-order-id">#${pedido.id}</strong>
      </div>
      <div>
        <span class="receipt-label">Cliente</span>
        <strong>${escapeHtml(pedido.cliente)}</strong>
      </div>
      <div>
        <span class="receipt-label">Data</span>
        <strong>${formatarData(pedido.data)}</strong>
      </div>
    </div>

    <div class="receipt-items-box">
      <h4>Itens comprados</h4>
      <ul>
        ${itens.length > 0
          ? itens.map(descricao => `<li>${ICONES_CARRINHO.check}<span>${escapeHtml(descricao)}</span></li>`).join('')
          : '<li>Itens do pedido registrados no comprovante enviado por e-mail.</li>'}
      </ul>
    </div>

    <div class="receipt-total-row">
      <span>Total pago</span>
      <strong class="receipt-total-value">${formatarPreco(Number(pedido.total) || 0)}</strong>
    </div>

    <div class="receipt-status-info">
      ${ICONES_CARRINHO.email}
      <span>${escapeHtml(statusEmail)}</span>
    </div>
  `;

  modal.classList.add('active');
  document.body.style.overflow = 'hidden';
  document.addEventListener('keydown', tratarEscapeRecibo);
}

function tratarEscapeRecibo(evento) {
  if (evento.key === 'Escape') fecharRecibo();
}

function fecharRecibo() {
  const modal = document.getElementById('checkoutSuccessModal');
  if (modal) modal.classList.remove('active');
  document.body.style.overflow = '';
  document.removeEventListener('keydown', tratarEscapeRecibo);
}

// ============ UTILITÁRIOS ============

function buscarItem(itemId) {
  const id = Number(itemId);
  return carrinhoAtual.itens.find(item => item.id === id) || null;
}

function mensagemAmigavel(erro) {
  const mensagem = erro && erro.message ? erro.message : '';

  if (/faça login|token|expir/i.test(mensagem)) {
    return 'Sua sessão expirou. Faça login novamente para continuar.';
  }

  return mensagem || 'Não foi possível concluir a operação. Tente novamente.';
}

// O Jackson pode devolver a data como texto ISO ("2026-09-29T10:30:00") ou como array
function formatarData(valor) {
  if (!valor) return '-';

  const data = Array.isArray(valor)
    ? new Date(valor[0], (valor[1] || 1) - 1, valor[2] || 1, valor[3] || 0, valor[4] || 0, valor[5] || 0)
    : new Date(valor);

  if (isNaN(data.getTime())) return '-';

  return data.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

function escapeHtml(texto) {
  return String(texto === null || texto === undefined ? '' : texto)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

