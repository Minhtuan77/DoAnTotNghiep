(() => {
  'use strict';

  const API_URL = 'http://localhost:8080/api';
  const doc = document;
  const topWin = window.parent && window.parent !== window ? window.parent : window;
  const topLoc = topWin.location;
  const storage = topWin.localStorage;
  const session = topWin.sessionStorage;
  const screen = location.pathname.split('/').filter(Boolean).slice(-2, -1)[0] || '';

  const state = {
    products: [],
    product: null,
    cart: null,
    addresses: [],
    selectedAddress: null,
    orders: [],
    wishlist: [],
    reviews: [],
    rating: 5,
    busy: new Set(),
  };

  const q = (sel, root = doc) => root.querySelector(sel);
  const qa = (sel, root = doc) => Array.from(root.querySelectorAll(sel));
  const txt = (el) => (el?.textContent || '').replace(/\s+/g, ' ').trim();
  const norm = (s) => (s || '').toLowerCase().normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd')
    .replace(/[^a-z0-9]+/g, ' ').trim();
  const money = (v) => new Intl.NumberFormat('vi-VN').format(Number(v || 0)) + ' ₫';
  const idFromTopPath = () => {
    const seg = topLoc.pathname.split('/').filter(Boolean);
    const last = seg[seg.length - 1];
    return /^\d+$/.test(last || '') ? Number(last) : null;
  };
  const go = (path) => { topLoc.href = path; };

  function toast(message, type = 'info') {
    let box = q('#__datn_toast');
    if (!box) {
      box = doc.createElement('div');
      box.id = '__datn_toast';
      box.style.cssText = [
        'position:fixed','right:24px','top:190px','z-index:999999','max-width:420px',
        'padding:12px 16px','border-radius:10px','font:600 14px Inter,Arial,sans-serif',
        'box-shadow:0 10px 30px rgba(15,23,42,.18)','transition:.2s','opacity:0','transform:translateY(-8px)'
      ].join(';');
      doc.body.appendChild(box);
    }
    box.textContent = String(message || 'Có lỗi xảy ra');
    box.style.background = type === 'error' ? '#ffdad6' : type === 'success' ? '#e8f5e9' : '#eef2ff';
    box.style.color = type === 'error' ? '#93000a' : type === 'success' ? '#166534' : '#1e3a8a';
    box.style.opacity = '1';
    box.style.transform = 'translateY(0)';
    clearTimeout(box.__timer);
    box.__timer = setTimeout(() => {
      box.style.opacity = '0';
      box.style.transform = 'translateY(-8px)';
    }, 3500);
  }

  function setBusy(key, busy, button) {
    if (busy) state.busy.add(key); else state.busy.delete(key);
    if (button) {
      button.disabled = busy;
      button.style.opacity = busy ? '0.7' : '';
      button.style.cursor = busy ? 'wait' : '';
    }
  }

  function clearAuth() {
    storage.removeItem('accessToken');
    storage.removeItem('refreshToken');
    storage.removeItem('currentUser');
  }

  function isLoggedIn() {
    const token = storage.getItem('accessToken');
    return !!token && token !== 'null' && token !== 'undefined';
  }

  function requireAuth(returnTo = topLoc.pathname + topLoc.search, message = 'Vui lòng đăng nhập để tiếp tục.') {
    if (isLoggedIn()) return true;
    session.setItem('returnAfterLogin', returnTo);
    session.setItem('flashMessage', message);
    go('/login');
    return false;
  }

  async function fetchJson(path, init = {}, retry = true) {
    const headers = new Headers(init.headers || {});
    if (init.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
    const token = storage.getItem('accessToken');
    if (token) headers.set('Authorization', 'Bearer ' + token);

    let res;
    try {
      res = await fetch(API_URL + path, { ...init, headers, cache: 'no-store' });
    } catch (_) {
      throw new Error('Không kết nối được backend. Hãy kiểm tra Spring Boot đang chạy ở cổng 8080.');
    }

    if (res.status === 401 && retry) {
      const refreshToken = storage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const rr = await fetch(API_URL + '/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken })
          });
          if (rr.ok) {
            const auth = await rr.json();
            if (auth.accessToken) storage.setItem('accessToken', auth.accessToken);
            if (auth.refreshToken) storage.setItem('refreshToken', auth.refreshToken);
            if (auth.user) storage.setItem('currentUser', JSON.stringify(auth.user));
            return fetchJson(path, init, false);
          }
        } catch (_) {}
      }
      clearAuth();
      const error = new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
      error.status = 401;
      throw error;
    }

    if (!res.ok) {
      let message = `HTTP ${res.status}`;
      try {
        const body = await res.json();
        message = body.message || body.error || body.detail || message;
        if (body.details && typeof body.details === 'object') {
          const first = Object.values(body.details)[0];
          if (first) message = String(first);
        }
      } catch (_) {
        try {
          const body = await res.text();
          if (body) message = body;
        } catch (_) {}
      }
      const error = new Error(message);
      error.status = res.status;
      throw error;
    }

    if (res.status === 204) return null;
    const type = res.headers.get('content-type') || '';
    if (type.includes('application/json')) return res.json();
    return res.text();
  }

  async function apiData(path, init = {}, retry = true) {
    const value = await fetchJson(path, init, retry);
    return value && typeof value === 'object' && Object.prototype.hasOwnProperty.call(value, 'data')
      ? value.data
      : value;
  }

  async function protectedCall(fn, returnTo = topLoc.pathname + topLoc.search) {
    if (!requireAuth(returnTo)) return null;
    try {
      return await fn();
    } catch (e) {
      if (e?.status === 401) {
        session.setItem('returnAfterLogin', returnTo);
        session.setItem('flashMessage', e.message);
        setTimeout(() => go('/login'), 250);
      }
      throw e;
    }
  }

  function currentUser() {
    try { return JSON.parse(storage.getItem('currentUser') || 'null'); } catch (_) { return null; }
  }

  function productImage(p) {
    const images = Array.isArray(p?.images) ? p.images : [];
    const primary = images.find(x => x?.isPrimary) || images[0];
    return primary?.imageUrl || primary?.url || null;
  }

  function findByText(selector, needle, root = doc) {
    const n = norm(needle);
    return qa(selector, root).find(el => norm(txt(el)).includes(n));
  }

  function productQuery() {
    const p = new URL(topLoc.href).searchParams;
    const sp = new URLSearchParams();
    sp.set('page', p.get('page') || '0');
    sp.set('size', '100');
    const keyword = p.get('q') || p.get('keyword');
    if (keyword) sp.set('keyword', keyword);
    ['categoryId', 'brandId', 'minPrice', 'maxPrice'].forEach(k => {
      if (p.get(k)) sp.set(k, p.get(k));
    });
    const sort = p.get('sort');
    if (sort === 'price-asc') { sp.set('sortBy', 'salePrice'); sp.set('sortDir', 'asc'); }
    else if (sort === 'price-desc') { sp.set('sortBy', 'salePrice'); sp.set('sortDir', 'desc'); }
    else if (sort === 'newest') { sp.set('sortBy', 'createdAt'); sp.set('sortDir', 'desc'); }
    else { sp.set('sortBy', 'productId'); sp.set('sortDir', 'desc'); }
    return sp.toString();
  }

  function catalogCards() {
    const result = [];
    const seen = new Set();
    qa('main article, main .product-item').forEach(card => {
      if (!card.querySelector('img')) return;
      const heading = card.querySelector('h2,h3,h4');
      if (!heading) return;
      if (seen.has(card)) return;
      seen.add(card);
      result.push(card);
    });
    if (result.length) return result;

    qa('button').forEach(btn => {
      const t = norm(txt(btn));
      if (!(t.includes('add shopping cart') || t.includes('them vao gio') || t.includes('mua ngay'))) return;
      let el = btn;
      for (let i = 0; i < 8 && el; i++, el = el.parentElement) {
        if (el.querySelector?.('img') && el.querySelector?.('h2,h3,h4')) {
          if (!seen.has(el)) { seen.add(el); result.push(el); }
          break;
        }
      }
    });
    return result;
  }

  function assignProductCard(card, p, index) {
    if (!card || !p) return;
    card.dataset.datnProductId = String(p.id);
    card.dataset.datnProductIndex = String(index);

    const heading = qa('h2,h3,h4,a', card).find(el => {
      const t = txt(el);
      return t.length > 8 && !t.includes('₫') && !norm(t).includes('xem chi tiet');
    });
    if (heading) heading.textContent = p.name;

    const img = qa('img', card).find(i => !norm(i.alt).includes('logo'));
    const src = productImage(p);
    if (img && src) { img.src = src; img.alt = p.name; }

    const prices = qa('*', card).filter(el => el.children.length === 0 && txt(el).includes('₫'));
    const current = p.salePrice ?? p.price;
    if (prices[0]) prices[0].textContent = money(current);
    if (prices[1] && p.salePrice != null && Number(p.price) > Number(p.salePrice)) prices[1].textContent = money(p.price);

    card.style.cursor = 'pointer';
  }

  async function resolveProductForCard(card) {
    const id = Number(card?.dataset?.datnProductId || 0);
    if (id) return state.products.find(p => Number(p.id) === id) || { id };

    const index = Number(card?.dataset?.datnProductIndex ?? -1);
    if (index >= 0 && state.products[index]) return state.products[index];

    const name = txt(card?.querySelector('h2,h3,h4,a'));
    if (name) {
      try {
        const page = await fetchJson('/v1/products?page=0&size=20&keyword=' + encodeURIComponent(name));
        const p = page?.content?.[0];
        if (p) {
          card.dataset.datnProductId = String(p.id);
          return p;
        }
      } catch (_) {}
    }
    return null;
  }

  async function addToCart(productId, quantity = 1, button = null, goCheckout = false) {
    if (!requireAuth('/products/' + productId, 'Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng.')) return;
    const key = `cart-add-${productId}`;
    if (state.busy.has(key)) return;
    setBusy(key, true, button);
    try {
      const cart = await apiData('/cart/items', {
        method: 'POST',
        body: JSON.stringify({ productId: Number(productId), quantity: Math.max(1, Number(quantity || 1)) })
      });
      state.cart = cart;
      updateCartBadge(cart);
      toast(goCheckout ? 'Đã thêm sản phẩm. Đang chuyển đến thanh toán...' : 'Đã thêm vào giỏ hàng', 'success');
      if (goCheckout) setTimeout(() => go('/checkout'), 300);
    } catch (e) {
      if (e?.status === 401) {
        session.setItem('returnAfterLogin', '/products/' + productId);
        setTimeout(() => go('/login'), 250);
      }
      toast(e.message, 'error');
    } finally {
      setBusy(key, false, button);
    }
  }

  async function toggleWishlist(productId, remove = false, button = null) {
    if (!requireAuth(topLoc.pathname + topLoc.search, 'Vui lòng đăng nhập để sử dụng danh sách yêu thích.')) return;
    const key = `wish-${productId}`;
    if (state.busy.has(key)) return;
    setBusy(key, true, button);
    try {
      await apiData('/wishlist/' + productId, { method: remove ? 'DELETE' : 'POST' });
      toast(remove ? 'Đã bỏ khỏi danh sách yêu thích' : 'Đã thêm vào danh sách yêu thích', 'success');
      if (screen === 'wishlist' && remove) setTimeout(() => location.reload(), 250);
    } catch (e) {
      // duplicate wishlist should behave like success from the UI perspective
      if (!remove && (e?.status === 409 || norm(e?.message).includes('ton tai') || norm(e?.message).includes('yeu thich'))) {
        toast('Sản phẩm đã có trong danh sách yêu thích', 'info');
      } else toast(e.message, 'error');
    } finally {
      setBusy(key, false, button);
    }
  }

  function updateCartBadge(cart) {
    if (!cart) return;
    qa('a[data-path="gio-hang"], a[href="/cart"]').forEach(a => {
      const badge = qa('span', a).find(s => /^\d+$/.test(txt(s)));
      if (badge) badge.textContent = String(cart.totalItems || 0);
      const price = qa('span', a).find(s => txt(s).includes('₫'));
      if (price) price.textContent = money(cart.totalAmount ?? cart.subtotal);
    });
    const headerBadge = q('#headerCounterBadge');
    if (headerBadge) headerBadge.textContent = String(cart.totalItems || 0);
  }

  async function syncHeader() {
    const user = currentUser();
    if (user?.fullName) {
      qa('span,div,p').filter(el => ['Nguyễn Văn A', 'Mai Linh', 'Nguyễn Mai Linh'].includes(txt(el)))
        .forEach(el => { el.textContent = user.fullName; });
    }
    if (!isLoggedIn()) return;
    try {
      const cart = await apiData('/cart');
      state.cart = cart;
      updateCartBadge(cart);
    } catch (_) {}
  }

  function bindGlobalNavigation() {
    const routeByPath = {
      'trang-chu': '/', 'san-pham': '/products', 'yeu-thich': '/wishlist', 'gio-hang': '/cart', 'tai-khoan': '/account',
      'ho-so-ca-nhan': '/account', 'quan-ly-don-hang': '/orders', 'don-hang-cua-toi': '/orders',
      'danh-gia-cua-toi': '/reviews', 'danh-gia-san-pham': '/reviews'
    };
    qa('a[data-path]').forEach(a => {
      const path = routeByPath[a.dataset.path];
      if (path) { a.href = path; a.target = '_top'; }
    });

    qa('a[href="#"]').forEach(a => {
      const t = norm(txt(a));
      const routes = [
        ['trang chu', '/'], ['san pham', '/products'], ['gio hang', '/cart'], ['yeu thich', '/wishlist'],
        ['don hang', '/orders'], ['ho so ca nhan', '/account'], ['danh gia cua toi', '/reviews']
      ];
      for (const [needle, path] of routes) {
        if (t === needle || t.endsWith(' ' + needle)) { a.href = path; a.target = '_top'; break; }
      }
    });

    qa('input[placeholder*="Tìm kiếm sản phẩm"],input[placeholder*="Tìm kiếm"],input[placeholder*="Tìm sản phẩm"]').forEach(input => {
      input.addEventListener('keydown', e => {
        if (e.key === 'Enter') {
          const value = input.value.trim();
          if (value) { e.preventDefault(); go('/search?q=' + encodeURIComponent(value)); }
        }
      });
    });
  }

  function installDelegatedActions() {
    doc.addEventListener('click', async (e) => {
      const el = e.target.closest('button,a');
      const buttonText = norm(txt(el));

      // Always-functional account/sidebar navigation buttons.
      if (el) {
        const fixedRoutes = [
          ['don hang cua toi', '/orders'], ['quan ly don hang', '/orders'],
          ['san pham yeu thich', '/wishlist'], ['yeu thich', '/wishlist'],
          ['danh gia cua toi', '/reviews'], ['danh gia phan hoi', '/reviews'],
          ['ho so ca nhan', '/account'], ['tong quan tai khoan', '/account']
        ];
        for (const [needle, path] of fixedRoutes) {
          if (buttonText.includes(needle) && el.tagName === 'BUTTON') {
            if (needle === 'ho so ca nhan' && screen === 'account' && typeof window.switchTab === 'function') break;
            e.preventDefault();
            go(path);
            return;
          }
        }
        if (buttonText.includes('dang xuat')) {
          e.preventDefault();
          await doLogout();
          return;
        }
      }

      if (['home', 'products', 'search'].includes(screen)) {
        const card = e.target.closest('[data-datn-product-id], main article, main .product-item');
        if (card && card.querySelector('img') && card.querySelector('h2,h3,h4')) {
          const t = norm(txt(el));
          const p = await resolveProductForCard(card);
          if (!p?.id) return;
          if (el && (t.includes('add shopping cart') || t.includes('them vao gio') || t.includes('them gio'))) {
            e.preventDefault(); e.stopPropagation();
            await addToCart(p.id, 1, el, false);
            return;
          }
          if (el && (t === 'favorite' || t.includes('yeu thich'))) {
            e.preventDefault(); e.stopPropagation();
            await toggleWishlist(p.id, false, el);
            return;
          }
          if (!e.target.closest('input,select,textarea,label') && !(el && buttonText.includes('xoa'))) {
            e.preventDefault();
            go('/products/' + p.id);
            return;
          }
        }
      }

      if (screen === 'product-detail') {
        const productId = idFromTopPath();
        if (!productId || !el) return;
        if (el.id === 'btn-add-to-cart' || buttonText.includes('them vao gio hang') || buttonText === 'add shopping cart them vao gio hang' || buttonText === 'add shopping cart them gio') {
          e.preventDefault(); e.stopPropagation();
          const qty = Number(q('#qty-input')?.value || 1);
          await addToCart(productId, qty, el, false);
          return;
        }
        if (el.id === 'btn-buy-now' || buttonText.includes('mua ngay')) {
          e.preventDefault(); e.stopPropagation();
          const qty = Number(q('#qty-input')?.value || 1);
          await addToCart(productId, qty, el, true);
          return;
        }
        if (norm(el.getAttribute?.('aria-label') || '').includes('yeu thich') || buttonText === 'favorite' || buttonText.startsWith('favorite da luu')) {
          e.preventDefault();
          await toggleWishlist(productId, false, el);
          return;
        }
        if (buttonText.includes('viet nhan xet')) {
          e.preventDefault();
          if (requireAuth('/products/' + productId)) go('/orders');
          return;
        }
      }

      if (screen === 'cart' && el) {
        if (buttonText.includes('tien hanh thanh toan')) {
          e.preventDefault();
          if (requireAuth('/checkout')) go('/checkout');
          return;
        }
        if (buttonText.includes('tiep tuc mua') || buttonText.includes('tiep tuc chon')) {
          e.preventDefault(); go('/products'); return;
        }
      }

      if (screen === 'checkout' && el && buttonText.includes('dat hang ngay')) {
        e.preventDefault();
        await placeOrder(el);
        return;
      }

      if (screen === 'orders' && el) {
        const card = e.target.closest('.order-card,[data-datn-order-id]');
        const orderId = Number(card?.dataset?.datnOrderId || 0);
        if (buttonText.includes('xem chi tiet') && orderId) {
          e.preventDefault(); go('/orders/' + orderId); return;
        }
        if (buttonText.includes('huy don') && orderId) {
          e.preventDefault();
          await cancelOrder(orderId, el);
          return;
        }
        if (buttonText.includes('danh gia') && orderId) {
          e.preventDefault();
          const order = state.orders.find(o => Number(o.orderId) === orderId) || await fetchJson('/orders/' + orderId).catch(() => null);
          const item = order?.items?.[0];
          if (!item) { toast('Không tìm thấy sản phẩm trong đơn hàng để đánh giá.', 'error'); return; }
          go('/reviews/write?orderItemId=' + item.orderItemId + '&productId=' + item.productId);
          return;
        }
      }

      if (screen === 'wishlist') {
        const card = e.target.closest('[data-datn-product-id], .product-item');
        const productId = Number(card?.dataset?.datnProductId || 0);
        if (productId && el) {
          if (buttonText.includes('them vao gio') || buttonText.startsWith('shopping cart')) {
            e.preventDefault(); await addToCart(productId, 1, el, false); return;
          }
          if (buttonText.includes('bo yeu thich') || buttonText === 'favorite') {
            e.preventDefault(); await toggleWishlist(productId, true, el); return;
          }
          if (buttonText.includes('xem chi tiet')) {
            e.preventDefault(); go('/products/' + productId); return;
          }
        }
        if (card && productId && !e.target.closest('input,select,textarea,label,button')) {
          e.preventDefault(); go('/products/' + productId); return;
        }
      }

      if (screen === 'reviews' && el) {
        const card = e.target.closest('[data-datn-review-id], article');
        const reviewId = Number(card?.dataset?.datnReviewId || 0);
        const productId = Number(card?.dataset?.datnProductId || 0);
        if (buttonText.includes('chinh sua') && reviewId) {
          e.preventDefault(); go('/reviews/write?reviewId=' + reviewId + '&productId=' + productId); return;
        }
        if (buttonText === 'xoa' || buttonText.endsWith(' xoa')) {
          if (!reviewId) return;
          e.preventDefault();
          if (!confirm('Xóa đánh giá này?')) return;
          try {
            await apiData('/reviews/' + reviewId, { method: 'DELETE' });
            toast('Đã xóa đánh giá', 'success');
            setTimeout(() => location.reload(), 250);
          } catch (err) { toast(err.message, 'error'); }
          return;
        }
        if (buttonText.includes('viet danh gia ngay')) {
          e.preventDefault(); go('/orders'); return;
        }
      }
    }, true);
  }

  async function doLogout() {
    try {
      const refreshToken = storage.getItem('refreshToken');
      if (refreshToken) await fetchJson('/auth/logout', { method: 'POST', body: JSON.stringify({ refreshToken }) });
    } catch (_) {}
    clearAuth();
    toast('Đã đăng xuất', 'success');
    setTimeout(() => go('/'), 250);
  }

  function findInputByPlaceholder(...needles) {
    return qa('input,textarea').find(el => needles.some(n => norm(el.placeholder).includes(norm(n))));
  }

  async function initLogin() {
    const flash = session.getItem('flashMessage');
    if (flash) { session.removeItem('flashMessage'); setTimeout(() => toast(flash, 'info'), 120); }
    const form = q('#loginForm') || q('form');
    const email = q('#emailInput') || q('input[type="email"]') || findInputByPlaceholder('email');
    const pwd = q('#passwordInput') || q('input[type="password"]');
    if (!form || !email || !pwd) return;
    form.addEventListener('submit', async e => {
      e.preventDefault();
      const submit = form.querySelector('button[type="submit"]');
      setBusy('login', true, submit);
      try {
        const auth = await fetchJson('/auth/login', {
          method: 'POST', body: JSON.stringify({ email: email.value.trim(), password: pwd.value })
        });
        storage.setItem('accessToken', auth.accessToken);
        storage.setItem('refreshToken', auth.refreshToken);
        storage.setItem('currentUser', JSON.stringify(auth.user));
        toast('Đăng nhập thành công', 'success');
        const target = session.getItem('returnAfterLogin') || '/';
        session.removeItem('returnAfterLogin');
        setTimeout(() => go(target), 300);
      } catch (err) { toast(err.message, 'error'); }
      finally { setBusy('login', false, submit); }
    });
  }

  async function initRegister() {
    const form = q('form');
    const submit = q('#btn-submit') || findByText('button', 'Đăng ký');
    if (!submit) return;
    const handler = async e => {
      e?.preventDefault();
      const fullName = (q('#fullname') || findInputByPlaceholder('họ và tên', 'ho va ten'))?.value?.trim();
      const email = (q('#email') || q('input[type="email"]'))?.value?.trim();
      const phone = (q('#phone') || q('input[type="tel"]'))?.value?.trim().replace(/\s/g, '');
      const passwords = qa('input[type="password"]');
      const password = (q('#password') || passwords[0])?.value;
      const confirmPassword = (q('#confirm-password') || passwords[1])?.value;
      if (confirmPassword != null && password !== confirmPassword) { toast('Mật khẩu xác nhận không khớp', 'error'); return; }
      const terms = q('#terms-agree');
      if (terms && !terms.checked) { toast('Bạn cần đồng ý điều khoản sử dụng', 'error'); return; }
      setBusy('register', true, submit);
      try {
        await fetchJson('/auth/register', { method: 'POST', body: JSON.stringify({ fullName, email, phone, password }) });
        toast('Đăng ký thành công. Hãy đăng nhập.', 'success');
        setTimeout(() => go('/login'), 400);
      } catch (err) { toast(err.message, 'error'); }
      finally { setBusy('register', false, submit); }
    };
    if (form) form.addEventListener('submit', handler); else submit.addEventListener('click', handler);
  }

  async function initPassword() {
    const isForgot = topLoc.pathname.includes('forgot-password');
    const isReset = topLoc.pathname.includes('reset-password');
    const token = new URL(topLoc.href).searchParams.get('token');
    if (isForgot) {
      const email = q('input[type="email"]') || findInputByPlaceholder('email');
      const submit = findByText('button', 'Gửi') || findByText('button', 'Tiếp tục') || findByText('button', 'Xác nhận');
      if (submit && email) submit.addEventListener('click', async e => {
        e.preventDefault();
        try {
          const msg = await fetchJson('/auth/forgot-password', { method: 'POST', body: JSON.stringify({ email: email.value.trim() }) });
          toast(typeof msg === 'string' ? msg : 'Yêu cầu khôi phục mật khẩu đã được gửi', 'success');
        } catch (err) { toast(err.message, 'error'); }
      });
      return;
    }
    if (isReset) {
      const pwds = qa('input[type="password"]');
      const submit = findByText('button', 'Đặt lại') || findByText('button', 'Xác nhận');
      if (submit && pwds.length) submit.addEventListener('click', async e => {
        e.preventDefault();
        if (!token) { toast('Thiếu token đặt lại mật khẩu trong URL', 'error'); return; }
        if (pwds[1] && pwds[0].value !== pwds[1].value) { toast('Mật khẩu xác nhận không khớp', 'error'); return; }
        try {
          await fetchJson('/auth/reset-password', { method: 'POST', body: JSON.stringify({ token, newPassword: pwds[0].value }) });
          toast('Đặt lại mật khẩu thành công', 'success');
          setTimeout(() => go('/login'), 350);
        } catch (err) { toast(err.message, 'error'); }
      });
    }
  }

  async function initCatalog() {
    try {
      const page = await fetchJson('/v1/products?' + productQuery());
      state.products = Array.isArray(page?.content) ? page.content : [];
      const cards = catalogCards();
      cards.forEach((card, i) => {
        const p = state.products[i];
        if (p) assignProductCard(card, p, i);
        else card.style.display = 'none';
      });
      if (!state.products.length) toast('Backend chưa có sản phẩm ACTIVE để hiển thị.', 'info');

      if (screen === 'search') {
        const kw = new URL(topLoc.href).searchParams.get('q') || '';
        const input = q('#mainSearchInput') || qa('input').find(i => norm(i.placeholder).includes('tim kiem'));
        if (input) input.value = kw;
      }
    } catch (e) {
      console.error('[DATN catalog]', e);
      toast(e.message, 'error');
    }
  }

  function renderProductDetail(p) {
    state.product = p;
    const headings = qa('main h1,main h2,main h3,main h4');
    const title = headings.find(h => norm(txt(h)).includes('philips') || txt(h).length > 30);
    if (title) title.textContent = p.name;

    const prices = qa('main *').filter(el => el.children.length === 0 && /\d[\d\.]*\s*₫/.test(txt(el)));
    if (prices[0]) prices[0].textContent = money(p.salePrice ?? p.price);
    if (prices[1] && p.salePrice != null && Number(p.price) > Number(p.salePrice)) prices[1].textContent = money(p.price);

    const src = productImage(p);
    const img = qa('main img')[0];
    if (img && src) { img.src = src; img.alt = p.name; }

    const reviewLink = q('a[href="#danh-gia-khach-hang"]');
    if (reviewLink) reviewLink.textContent = `${p.reviewCount || 0} Đánh giá`;

    const qty = q('#qty-input');
    if (qty) {
      qty.value = '1';
      qty.min = '1';
      qty.max = String(Math.max(1, Number(p.stockQuantity || 1)));
    }
    const minus = q('#qty-minus');
    const plus = q('#qty-plus');
    if (minus && qty) minus.onclick = () => { qty.value = String(Math.max(1, Number(qty.value || 1) - 1)); };
    if (plus && qty) plus.onclick = () => { qty.value = String(Math.min(Number(qty.max || 999), Number(qty.value || 1) + 1)); };
  }

  async function initProductDetail() {
    const id = idFromTopPath();
    if (!id) return;

    // Make primary actions usable immediately, even before product API finishes.
    const add = q('#btn-add-to-cart') || findByText('button', 'Thêm vào giỏ hàng');
    const buy = q('#btn-buy-now') || findByText('button', 'Mua ngay');
    if (add) add.id = 'btn-add-to-cart';
    if (buy) buy.id = 'btn-buy-now';

    try {
      const p = await fetchJson('/v1/products/' + id);
      renderProductDetail(p);
      try {
        const page = await apiData(`/reviews/product/${id}?page=0&size=10`);
        const reviews = page?.content || [];
        const commentTargets = qa('main p').filter(el => txt(el).length > 50);
        reviews.slice(0, commentTargets.length).forEach((r, i) => {
          if (r.comment) commentTargets[i].textContent = r.comment;
        });
      } catch (_) {}
    } catch (e) {
      toast(e.message, 'error');
    }
  }

  function cartRows() {
    const rows = [];
    const seen = new Set();
    qa('main button').filter(b => norm(txt(b)) === 'remove').forEach(btn => {
      let el = btn;
      for (let i = 0; i < 6 && el; i++, el = el.parentElement) {
        if (el.querySelector?.('input[type="number"]') && el.querySelectorAll?.('button').length >= 2) {
          if (!seen.has(el)) { seen.add(el); rows.push(el); }
          break;
        }
      }
    });
    return rows;
  }

  function renderCart(cart) {
    state.cart = cart;
    updateCartBadge(cart);
    const items = cart?.items || [];
    if (!items.length) {
      if (typeof window.switchCartView === 'function') window.switchCartView('empty');
      else q('#viewEmptyBtn')?.click();
      return;
    }
    if (typeof window.switchCartView === 'function') window.switchCartView('active');

    const rows = cartRows();
    rows.forEach((row, i) => {
      const item = items[i];
      if (!item) { row.style.display = 'none'; return; }
      row.style.display = '';
      row.dataset.datnCartItemId = String(item.cartItemId);
      row.dataset.datnProductId = String(item.productId);

      const title = qa('a,h2,h3,h4', row).find(el => txt(el).length > 15);
      if (title) {
        title.textContent = item.productName;
        if (title.tagName === 'A') { title.href = '/products/' + item.productId; title.target = '_top'; }
      }
      const img = q('img', row);
      if (img && item.imageUrl) { img.src = item.imageUrl; img.alt = item.productName; }
      const quantity = q('input[type="number"]', row);
      if (quantity) { quantity.value = String(item.quantity); quantity.min = '1'; quantity.max = String(item.availableQuantity || 999); }
      const prices = qa('*', row).filter(el => el.children.length === 0 && txt(el).includes('₫'));
      if (prices[0]) prices[0].textContent = money(item.unitPrice);
      if (prices.length) prices[prices.length - 1].textContent = money(item.lineTotal);

      qa('button', row).forEach(btn => {
        const t = norm(txt(btn));
        if (t === 'add') btn.onclick = () => updateCartItem(item.cartItemId, item.quantity + 1, btn);
        if (t === 'remove') btn.onclick = () => updateCartItem(item.cartItemId, Math.max(1, item.quantity - 1), btn);
        if (t.includes('xoa')) btn.onclick = () => removeCartItem(item.cartItemId, btn);
        if (t.includes('luu vao danh sach yeu thich')) btn.onclick = async () => {
          await toggleWishlist(item.productId, false, btn);
        };
      });
    });

    // best-effort summary replacement
    const allPrices = qa('main *').filter(el => el.children.length === 0 && txt(el).includes('₫'));
    const summaryCandidates = allPrices.slice(-8);
    if (summaryCandidates.length >= 1) summaryCandidates[summaryCandidates.length - 1].textContent = money(cart.totalAmount ?? cart.subtotal);
  }

  async function updateCartItem(cartItemId, quantity, button) {
    try {
      setBusy('cart-update-' + cartItemId, true, button);
      const cart = await apiData('/cart/items/' + cartItemId, { method: 'PUT', body: JSON.stringify({ quantity }) });
      renderCart(cart);
    } catch (e) { toast(e.message, 'error'); }
    finally { setBusy('cart-update-' + cartItemId, false, button); }
  }

  async function removeCartItem(cartItemId, button) {
    try {
      setBusy('cart-remove-' + cartItemId, true, button);
      const cart = await apiData('/cart/items/' + cartItemId, { method: 'DELETE' });
      toast('Đã xóa sản phẩm khỏi giỏ hàng', 'success');
      renderCart(cart);
      setTimeout(() => location.reload(), 200);
    } catch (e) { toast(e.message, 'error'); }
    finally { setBusy('cart-remove-' + cartItemId, false, button); }
  }

  async function initCart() {
    if (!requireAuth('/cart', 'Vui lòng đăng nhập để xem giỏ hàng.')) return;
    try {
      const cart = await apiData('/cart');
      renderCart(cart);

      const coupon = q('#couponInput') || q('#voucher-input');
      const apply = findByText('button', 'Áp dụng');
      if (coupon && apply) apply.onclick = async e => {
        e.preventDefault();
        try {
          const v = await apiData('/vouchers/validate', {
            method: 'POST', body: JSON.stringify({ code: coupon.value.trim(), orderAmount: cart.subtotal })
          });
          session.setItem('voucherCode', v.code || coupon.value.trim());
          toast(v.message || 'Voucher hợp lệ', 'success');
        } catch (err) { toast(err.message, 'error'); }
      };

      const clear = findByText('button', 'Xóa tất cả');
      if (clear) clear.onclick = async e => {
        e.preventDefault();
        try { await apiData('/cart', { method: 'DELETE' }); location.reload(); }
        catch (err) { toast(err.message, 'error'); }
      };
    } catch (e) {
      if (e?.status === 401) setTimeout(() => go('/login'), 250);
      toast(e.message, 'error');
    }
  }

  function addressText(a) {
    return [a.detailAddress, a.ward, a.district, a.province].filter(Boolean).join(', ');
  }

  async function loadCheckoutState() {
    const [cart, addresses] = await Promise.all([apiData('/cart'), fetchJson('/users/me/addresses')]);
    state.cart = cart;
    state.addresses = Array.isArray(addresses) ? addresses : [];
    state.selectedAddress = state.addresses.find(a => a.isDefault) || state.addresses[0] || null;
    return { cart, addresses: state.addresses };
  }

  function mapCheckoutAddresses() {
    const radios = qa('input[name="delivery_address"]');
    radios.forEach((radio, i) => {
      const a = state.addresses[i];
      if (!a) {
        const card = radio.closest('label,div');
        if (card && i >= state.addresses.length) card.style.display = 'none';
        return;
      }
      radio.value = String(a.addressId);
      radio.checked = Number(a.addressId) === Number(state.selectedAddress?.addressId);
      radio.onchange = () => { state.selectedAddress = a; };
      let box = radio.parentElement;
      for (let n = 0; n < 3 && box?.parentElement; n++) box = box.parentElement;
      if (box) {
        const strings = qa('p,span,strong', box).filter(el => txt(el).length > 2);
        const nameEl = strings.find(el => norm(txt(el)).includes('nguyen') || norm(txt(el)).includes('linh')) || strings[0];
        if (nameEl) nameEl.textContent = `${a.recipientName} - ${a.phone}`;
        const addrEl = strings.find(el => norm(txt(el)).includes('ha noi') || norm(txt(el)).includes('ho chi minh'));
        if (addrEl) addrEl.textContent = addressText(a);
      }
    });
  }

  async function initCheckout() {
    if (!requireAuth('/checkout', 'Vui lòng đăng nhập để thanh toán.')) return;
    try {
      const { cart } = await loadCheckoutState();
      if (!cart?.items?.length) {
        toast('Giỏ hàng đang trống', 'error');
        setTimeout(() => go('/cart'), 500);
        return;
      }
      mapCheckoutAddresses();

      const voucher = q('#voucher-input');
      if (voucher && session.getItem('voucherCode')) voucher.value = session.getItem('voucherCode');
      const apply = q('#apply-voucher-btn');
      if (apply && voucher) apply.onclick = async e => {
        e.preventDefault();
        try {
          const v = await apiData('/vouchers/validate', {
            method: 'POST', body: JSON.stringify({ code: voucher.value.trim(), orderAmount: cart.subtotal })
          });
          session.setItem('voucherCode', v.code || voucher.value.trim());
          toast(v.message || 'Voucher hợp lệ', 'success');
        } catch (err) { toast(err.message, 'error'); }
      };
    } catch (e) {
      if (e?.status === 401) setTimeout(() => go('/login'), 250);
      toast(e.message, 'error');
    }
  }

  function manualCheckoutAddress() {
    const name = findInputByPlaceholder('nguyễn văn an', 'nguyen van an')?.value?.trim();
    const phone = q('input[type="tel"]')?.value?.trim().replace(/\s/g, '');
    const detail = findInputByPlaceholder('vincom')?.value?.trim();
    const selects = qa('select');
    const province = selects[0]?.selectedOptions?.[0]?.text?.trim() || '';
    const district = selects[1]?.selectedOptions?.[0]?.text?.trim() || '';
    const ward = selects[2]?.selectedOptions?.[0]?.text?.trim() || '';
    if (!name || !phone || !detail) return null;
    return { recipientName: name, phone, province, district, ward, detailAddress: detail };
  }

  async function placeOrder(button) {
    if (!requireAuth('/checkout')) return;
    const key = 'place-order';
    if (state.busy.has(key)) return;
    setBusy(key, true, button);
    try {
      if (!state.cart?.items?.length) await loadCheckoutState();
      if (!state.cart?.items?.length) throw new Error('Giỏ hàng đang trống.');

      const radio = q('input[name="delivery_address"]:checked');
      if (radio) {
        const found = state.addresses.find(a => String(a.addressId) === String(radio.value));
        if (found) state.selectedAddress = found;
      }
      const address = state.selectedAddress || manualCheckoutAddress();
      if (!address) throw new Error('Vui lòng chọn hoặc nhập đầy đủ địa chỉ nhận hàng.');

      const payment = q('input[name="payment_method"]:checked');
      const paymentValue = norm(payment?.value || 'cod');
      const online = paymentValue !== 'cod';
      const voucherCode = q('#voucher-input')?.value?.trim() || session.getItem('voucherCode') || null;
      const body = {
        recipientName: address.recipientName,
        recipientPhone: address.phone,
        shippingAddress: addressText(address),
        note: q('#order-notes')?.value?.trim() || '',
        paymentMethod: online ? 'ONLINE' : 'COD',
        voucherCode: voucherCode || null,
        paymentProvider: online ? 'VNPAY' : null
      };

      const order = await fetchJson('/orders', { method: 'POST', body: JSON.stringify(body) });
      session.removeItem('voucherCode');
      if (online) {
        const pay = await fetchJson('/payments/vnpay/create/' + order.orderId, { method: 'POST' });
        if (!pay?.paymentUrl) throw new Error('Backend không trả về URL thanh toán VNPAY.');
        topLoc.href = pay.paymentUrl;
      } else {
        toast('Đặt hàng COD thành công', 'success');
        setTimeout(() => go('/orders/' + order.orderId), 450);
      }
    } catch (e) {
      toast(e.message, 'error');
    } finally {
      setBusy(key, false, button);
    }
  }

  function orderCards() {
    const cards = qa('article.order-card');
    if (cards.length) return cards;
    const seen = new Set(), result = [];
    qa('button').filter(b => norm(txt(b)).includes('xem chi tiet')).forEach(btn => {
      let el = btn;
      for (let i = 0; i < 7 && el; i++, el = el.parentElement) {
        if (el.querySelector?.('button') && /ma don/i.test(norm(txt(el)))) {
          if (!seen.has(el)) { seen.add(el); result.push(el); }
          break;
        }
      }
    });
    return result;
  }

  function statusLabel(status) {
    return {
      PENDING_CONFIRMATION: 'Chờ xác nhận', CONFIRMED: 'Đã xác nhận', SHIPPING: 'Đang giao hàng',
      DELIVERED: 'Đã giao hàng', COMPLETED: 'Hoàn thành', CANCELLED: 'Đã hủy', RETURNED: 'Hoàn trả'
    }[status] || status || '';
  }

  function renderOrderCard(card, order) {
    if (!card || !order) return;
    card.dataset.datnOrderId = String(order.orderId);
    const code = qa('*', card).find(el => el.children.length === 0 && norm(txt(el)).includes('ma don'));
    if (code) code.textContent = `Mã đơn: #${order.orderCode}`;

    const statusEl = qa('*', card).find(el => el.children.length === 0 && [
      'cho xac nhan','da xac nhan','dang giao hang','da giao hang','hoan thanh','da huy','hoan tra'
    ].some(s => norm(txt(el)).includes(s)));
    if (statusEl) statusEl.textContent = statusLabel(order.status);

    const productTextEls = qa('a,h3,h4,p', card).filter(el => txt(el).length > 15);
    if (order.items?.[0] && productTextEls[0]) productTextEls[0].textContent = order.items[0].productName;
    const prices = qa('*', card).filter(el => el.children.length === 0 && txt(el).includes('₫'));
    if (prices.length) prices[prices.length - 1].textContent = money(order.totalAmount);

    // Hide cancellation if backend status cannot be cancelled.
    qa('button', card).forEach(btn => {
      const t = norm(txt(btn));
      if (t.includes('huy don') && !['PENDING_CONFIRMATION','CONFIRMED'].includes(order.status)) btn.style.display = 'none';
      if (t.includes('danh gia') && !['DELIVERED','COMPLETED'].includes(order.status)) btn.style.display = 'none';
    });
  }

  async function cancelOrder(orderId, button) {
    if (!confirm('Bạn có chắc muốn hủy đơn hàng này?')) return;
    setBusy('cancel-' + orderId, true, button);
    try {
      const order = await fetchJson('/orders/' + orderId + '/cancel', { method: 'PATCH' });
      toast('Đã hủy đơn hàng', 'success');
      const card = q(`[data-datn-order-id="${orderId}"]`);
      if (card) renderOrderCard(card, order);
    } catch (e) { toast(e.message, 'error'); }
    finally { setBusy('cancel-' + orderId, false, button); }
  }

  async function initOrders() {
    if (!requireAuth(topLoc.pathname + topLoc.search, 'Vui lòng đăng nhập để xem đơn hàng.')) return;
    try {
      const detailId = idFromTopPath();
      const cards = orderCards();
      if (detailId) {
        const order = await fetchJson('/orders/' + detailId);
        state.orders = [order];
        if (cards[0]) renderOrderCard(cards[0], order);
        cards.slice(1).forEach(c => { c.style.display = 'none'; });
        return;
      }
      const page = await fetchJson('/orders?page=0&size=50&sort=createdAt,desc');
      state.orders = Array.isArray(page?.content) ? page.content : [];
      cards.forEach((card, i) => {
        if (state.orders[i]) renderOrderCard(card, state.orders[i]);
        else card.style.display = 'none';
      });
      if (!state.orders.length) toast('Bạn chưa có đơn hàng nào.', 'info');
    } catch (e) {
      if (e?.status === 401) setTimeout(() => go('/login'), 250);
      toast(e.message, 'error');
    }
  }

  function wishlistCards() {
    const cards = qa('.product-item');
    if (cards.length) return cards;
    return catalogCards();
  }

  async function initWishlist() {
    if (!requireAuth('/wishlist', 'Vui lòng đăng nhập để xem sản phẩm yêu thích.')) return;
    try {
      const page = await apiData('/wishlist?page=0&size=50');
      state.wishlist = Array.isArray(page?.content) ? page.content : [];
      const cards = wishlistCards();
      cards.forEach((card, i) => {
        const item = state.wishlist[i];
        if (!item) { card.style.display = 'none'; return; }
        card.style.display = '';
        card.dataset.datnProductId = String(item.productId);
        const title = qa('h2,h3,h4,a', card).find(el => txt(el).length > 12);
        if (title) title.textContent = item.productName;
        const img = q('img', card);
        if (img && item.primaryImageUrl) { img.src = item.primaryImageUrl; img.alt = item.productName; }
        const prices = qa('*', card).filter(el => el.children.length === 0 && txt(el).includes('₫'));
        if (prices[0]) prices[0].textContent = money(item.salePrice ?? item.price);
      });
      if (!state.wishlist.length) {
        if (typeof window.toggleWishlistView === 'function') window.toggleWishlistView('empty');
        toast('Danh sách yêu thích đang trống.', 'info');
      }
    } catch (e) {
      if (e?.status === 401) setTimeout(() => go('/login'), 250);
      toast(e.message, 'error');
    }
  }

  async function initAccount() {
    if (!requireAuth('/account', 'Vui lòng đăng nhập để xem tài khoản.')) return;
    try {
      const [me, addresses] = await Promise.all([fetchJson('/users/me'), fetchJson('/users/me/addresses')]);
      storage.setItem('currentUser', JSON.stringify(me));
      state.addresses = Array.isArray(addresses) ? addresses : [];

      const mainInputs = qa('main input');
      const full = mainInputs.find(i => i.type === 'text' && !i.placeholder);
      const email = mainInputs.find(i => i.type === 'email');
      const phone = mainInputs.find(i => i.type === 'tel');
      if (full) full.value = me.fullName || '';
      if (email) email.value = me.email || '';
      if (phone) phone.value = me.phone || '';
      qa('span,div,p').filter(el => ['Nguyễn Văn A', 'Mai Linh', 'Nguyễn Mai Linh'].includes(txt(el)))
        .forEach(el => { el.textContent = me.fullName || txt(el); });

      const save = qa('button[type="submit"]').find(b => norm(txt(b)).includes('luu thay doi'));
      if (save) save.onclick = async e => {
        e.preventDefault();
        try {
          const updated = await fetchJson('/users/me', {
            method: 'PUT',
            body: JSON.stringify({ fullName: full?.value?.trim() || me.fullName, phone: phone?.value?.trim().replace(/\s/g, '') || me.phone, avatarUrl: me.avatarUrl })
          });
          storage.setItem('currentUser', JSON.stringify(updated));
          toast('Cập nhật hồ sơ thành công', 'success');
        } catch (err) { toast(err.message, 'error'); }
      };

      const passwordSubmit = qa('button[type="submit"]').find(b => norm(txt(b)).includes('doi mat khau'));
      if (passwordSubmit) passwordSubmit.onclick = async e => {
        e.preventDefault();
        const currentPassword = q('#pwd-current')?.value || '';
        const newPassword = q('#pwd-new')?.value || '';
        const confirmPassword = q('#pwd-confirm')?.value || '';
        if (newPassword !== confirmPassword) { toast('Mật khẩu xác nhận không khớp', 'error'); return; }
        try {
          await fetchJson('/users/me/password', { method: 'PUT', body: JSON.stringify({ currentPassword, newPassword }) });
          toast('Đổi mật khẩu thành công', 'success');
        } catch (err) { toast(err.message, 'error'); }
      };

      const deleteButtons = qa('button').filter(b => norm(txt(b)) === 'xoa');
      deleteButtons.forEach((btn, i) => {
        const a = state.addresses[i];
        if (!a) return;
        btn.onclick = async e => {
          e.preventDefault();
          try { await fetchJson('/users/me/addresses/' + a.addressId, { method: 'DELETE' }); toast('Đã xóa địa chỉ', 'success'); location.reload(); }
          catch (err) { toast(err.message, 'error'); }
        };
      });
      const defaultButtons = qa('button').filter(b => norm(txt(b)).includes('dat lam mac dinh'));
      defaultButtons.forEach((btn, i) => {
        const a = state.addresses.find(x => !x.isDefault) || state.addresses[i];
        if (!a) return;
        btn.onclick = async e => {
          e.preventDefault();
          try { await fetchJson('/users/me/addresses/' + a.addressId + '/default', { method: 'PUT' }); toast('Đã đặt địa chỉ mặc định', 'success'); location.reload(); }
          catch (err) { toast(err.message, 'error'); }
        };
      });

      const addAddress = qa('button').find(b => norm(txt(b)).includes('them dia chi moi'));
      if (addAddress) addAddress.onclick = async e => {
        e.preventDefault();
        const recipientName = prompt('Tên người nhận:'); if (!recipientName) return;
        const phoneValue = prompt('Số điện thoại (10 số):'); if (!phoneValue) return;
        const province = prompt('Tỉnh/Thành phố:') || '';
        const district = prompt('Quận/Huyện:') || '';
        const ward = prompt('Phường/Xã:') || '';
        const detailAddress = prompt('Địa chỉ chi tiết:'); if (!detailAddress) return;
        try {
          await fetchJson('/users/me/addresses', { method: 'POST', body: JSON.stringify({ recipientName, phone: phoneValue.replace(/\s/g, ''), province, district, ward, detailAddress, isDefault: state.addresses.length === 0 }) });
          toast('Thêm địa chỉ thành công', 'success');
          location.reload();
        } catch (err) { toast(err.message, 'error'); }
      };
    } catch (e) {
      if (e?.status === 401) setTimeout(() => go('/login'), 250);
      toast(e.message, 'error');
    }
  }

  function reviewCards() {
    return qa('section article').filter(card => qa('button', card).some(b => {
      const t = norm(txt(b)); return t.includes('chinh sua') || t === 'xoa' || t.endsWith(' xoa');
    }));
  }

  async function initReviews() {
    if (!requireAuth('/reviews', 'Vui lòng đăng nhập để xem đánh giá của bạn.')) return;
    try {
      const page = await apiData('/reviews/me?page=0&size=50');
      state.reviews = Array.isArray(page?.content) ? page.content : [];
      const cards = reviewCards();
      cards.forEach((card, i) => {
        const review = state.reviews[i];
        if (!review) { card.style.display = 'none'; return; }
        card.style.display = '';
        card.dataset.datnReviewId = String(review.reviewId);
        card.dataset.datnProductId = String(review.productId);
        const title = qa('h2,h3,h4,a', card).find(el => txt(el).length > 12);
        if (title) title.textContent = review.productName;
        const comment = qa('p', card).find(p => txt(p).length > 40);
        if (comment && review.comment) comment.textContent = review.comment;
      });
      if (!state.reviews.length) toast('Bạn chưa có đánh giá nào.', 'info');
    } catch (e) {
      if (e?.status === 401) setTimeout(() => go('/login'), 250);
      toast(e.message, 'error');
    }
  }

  async function initReviewWrite() {
    if (!requireAuth(topLoc.pathname + topLoc.search, 'Vui lòng đăng nhập để đánh giá sản phẩm.')) return;
    const params = new URL(topLoc.href).searchParams;
    const orderItemId = params.get('orderItemId');
    const reviewId = params.get('reviewId');
    const productId = params.get('productId');

    const stars = qa('.star-btn');
    state.rating = 5;
    stars.forEach((button, i) => {
      button.addEventListener('click', e => {
        e.preventDefault();
        state.rating = i + 1;
      });
    });

    if (productId) {
      try {
        const p = await fetchJson('/v1/products/' + productId);
        const title = qa('main h1,main h2,main h3,h4').find(el => norm(txt(el)).includes('noi chien') || txt(el).length > 25);
        if (title) title.textContent = p.name;
      } catch (_) {}
    }

    const form = q('#productReviewForm') || q('form');
    if (!form) return;
    form.addEventListener('submit', async e => {
      e.preventDefault();
      const submit = form.querySelector('button[type="submit"]');
      const comment = q('#reviewComment')?.value?.trim() || '';
      setBusy('review-submit', true, submit);
      try {
        if (reviewId) {
          await apiData('/reviews/' + reviewId, { method: 'PUT', body: JSON.stringify({ rating: state.rating, comment }) });
        } else {
          if (!orderItemId) throw new Error('Đánh giá phải được mở từ sản phẩm trong đơn hàng đã mua.');
          await apiData('/reviews', { method: 'POST', body: JSON.stringify({ orderItemId: Number(orderItemId), rating: state.rating, comment }) });
        }
        toast('Lưu đánh giá thành công', 'success');
        setTimeout(() => go('/reviews'), 350);
      } catch (err) { toast(err.message, 'error'); }
      finally { setBusy('review-submit', false, submit); }
    });
  }

  async function initPaymentResult() {
    const params = topLoc.search;
    if (params && params.includes('vnp_')) {
      try {
        const result = await fetchJson('/payments/vnpay/return' + params);
        const target = result.paymentSuccessful ? 'success' : (result.signatureValid ? 'failed' : 'cancelled');
        if (typeof window.switchState === 'function') window.switchState(target);
        toast(result.message || (result.paymentSuccessful ? 'Thanh toán thành công' : 'Thanh toán chưa thành công'), result.paymentSuccessful ? 'success' : 'error');
      } catch (e) { toast(e.message, 'error'); }
    }
  }

  async function boot() {
    bindGlobalNavigation();
    installDelegatedActions();
    syncHeader();

    try {
      if (screen === 'login') await initLogin();
      else if (screen === 'register') await initRegister();
      else if (screen === 'reset-password') await initPassword();
      else if (['home', 'products', 'search'].includes(screen)) await initCatalog();
      else if (screen === 'product-detail') await initProductDetail();
      else if (screen === 'cart') await initCart();
      else if (screen === 'checkout') await initCheckout();
      else if (screen === 'payment-result') await initPaymentResult();
      else if (screen === 'orders') await initOrders();
      else if (screen === 'wishlist') await initWishlist();
      else if (screen === 'account') await initAccount();
      else if (screen === 'reviews') await initReviews();
      else if (screen === 'product-review') await initReviewWrite();
    } catch (e) {
      console.error('[DATN boot]', e);
      toast(e.message || 'Có lỗi xảy ra', 'error');
    }
  }

  if (doc.readyState === 'loading') doc.addEventListener('DOMContentLoaded', () => setTimeout(boot, 30));
  else setTimeout(boot, 30);
})();
