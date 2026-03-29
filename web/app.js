const CROP_TYPES = ["Wheat", "Rice", "Cotton", "Sugarcane", "Maize", "Tomato", "Potato", "Onion"];
const ROUTES = new Set(["/", "/login", "/signup", "/dashboard", "/add-crops", "/buy-crops", "/profile"]);
const AUTH_ROUTES = new Set(["/dashboard", "/add-crops", "/buy-crops", "/profile"]);

const appRoot = document.getElementById("app");
const toast = document.getElementById("toast");

const state = {
  user: null,
  route: "/",
  profileMenuOpen: false,
  dashboard: null,
  profile: null,
  myCrops: [],
  marketCrops: [],
  myPurchases: [],
  marketPurchases: [],
  filters: {
    addCrops: { crop: "", city: "" },
    buyCrops: { crop: "", city: "" }
  }
};

window.addEventListener("popstate", () => {
  navigate(window.location.pathname, { history: false, replace: true });
});

document.addEventListener("click", (event) => {
  const routeTarget = event.target.closest("[data-route]");
  if (routeTarget) {
    event.preventDefault();
    state.profileMenuOpen = false;
    navigate(routeTarget.dataset.route);
    return;
  }

  const profileToggle = event.target.closest("[data-profile-toggle]");
  if (profileToggle) {
    event.preventDefault();
    state.profileMenuOpen = !state.profileMenuOpen;
    renderApp();
    bindForms();
    return;
  }

  const logoutTarget = event.target.closest("[data-logout]");
  if (logoutTarget) {
    event.preventDefault();
    handleLogout();
    return;
  }

  if (state.profileMenuOpen && !event.target.closest(".profile-menu-container")) {
    state.profileMenuOpen = false;
    renderApp();
    bindForms();
  }
});

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initializeApp);
} else {
  initializeApp();
}

async function initializeApp() {
  renderLoading("Preparing your marketplace workspace...");
  await refreshSession();
  await navigate(window.location.pathname, { history: false, replace: true });
}

async function refreshSession() {
  try {
    const payload = await apiRequest("/api/auth/session", {
      suppressAuthRedirect: true
    });
    state.user = payload.user;
  } catch {
    state.user = null;
  }
}

async function navigate(nextRoute, options = {}) {
  const route = resolveRoute(nextRoute);
  syncHistory(route, options);
  state.route = route;

  renderLoading(`Loading ${routeLabel(route)}...`);

  try {
    await loadRouteData(route);
    renderApp();
    bindForms();
  } catch (error) {
    showToast(error.message || "Something went wrong.");
    renderApp();
    bindForms();
  }
}

function resolveRoute(rawRoute) {
  const normalized = ROUTES.has(rawRoute) ? rawRoute : "/";
  const fallback = state.user ? "/dashboard" : "/login";
  const candidate = normalized === "/" ? fallback : normalized;

  if (state.user && (candidate === "/login" || candidate === "/signup")) {
    return "/dashboard";
  }
  if (!state.user && AUTH_ROUTES.has(candidate)) {
    return "/login";
  }
  return candidate;
}

function syncHistory(route, options) {
  const { history = true, replace = false } = options;
  if (!history) {
    if (replace && window.location.pathname !== route) {
      historyReplace(route);
    }
    return;
  }

  if (replace || window.location.pathname === route) {
    historyReplace(route);
    return;
  }

  window.history.pushState({}, "", route);
}

function historyReplace(route) {
  window.history.replaceState({}, "", route);
}

async function loadRouteData(route) {
  if (!AUTH_ROUTES.has(route)) {
    return;
  }

  if (!state.user) {
    return;
  }

  if (route === "/dashboard") {
    state.dashboard = await apiRequest("/api/dashboard");
    state.user = state.dashboard.user;
    return;
  }

  if (route === "/add-crops") {
    const filters = state.filters.addCrops;
    const [myCropResponse, demandResponse] = await Promise.all([
      apiRequest("/api/crops?scope=mine"),
      apiRequest(`/api/purchases?scope=all&crop=${encodeURIComponent(filters.crop)}&city=${encodeURIComponent(filters.city)}`)
    ]);
    state.myCrops = myCropResponse.crops;
    state.marketPurchases = demandResponse.purchases;
    return;
  }

  if (route === "/buy-crops") {
    const filters = state.filters.buyCrops;
    const [marketCropResponse, myPurchaseResponse] = await Promise.all([
      apiRequest(`/api/crops?scope=all&crop=${encodeURIComponent(filters.crop)}&city=${encodeURIComponent(filters.city)}`),
      apiRequest("/api/purchases?scope=mine")
    ]);
    state.marketCrops = marketCropResponse.crops;
    state.myPurchases = myPurchaseResponse.purchases;
    return;
  }

  if (route === "/profile") {
    const profileResponse = await apiRequest("/api/profile");
    state.profile = profileResponse.user;
    state.user = profileResponse.user;
  }
}

function renderApp() {
  if (!state.user) {
    appRoot.innerHTML = state.route === "/signup" ? renderAuthPage("signup") : renderAuthPage("login");
    return;
  }

  if (state.route === "/dashboard") {
    appRoot.innerHTML = renderShell({
      title: "Home Dashboard",
      subtitle: "Track your listings, demand, and best-fit marketplace opportunities from one place.",
      content: renderDashboard()
    });
    return;
  }

  if (state.route === "/add-crops") {
    appRoot.innerHTML = renderShell({
      title: "Add Crops",
      subtitle: "Publish fresh inventory and keep an eye on what buyers currently need.",
      content: renderAddCropsPage()
    });
    return;
  }

  if (state.route === "/buy-crops") {
    appRoot.innerHTML = renderShell({
      title: "Buy Crops",
      subtitle: "Post your demand, review live listings, and identify crop offers that suit your budget.",
      content: renderBuyCropsPage()
    });
    return;
  }

  appRoot.innerHTML = renderShell({
    title: "Profile",
    subtitle: "Manage your account details. New listings automatically use this profile information.",
    content: renderProfilePage()
  });
}

function renderAuthPage(mode) {
  const isSignup = mode === "signup";
  return `
    <div class="auth-shell">
      <section class="auth-visual">
        <div class="auth-visual__title">Harvest Hub</div>
      </section>
      <section class="auth-panel">
        <div class="auth-panel__header">
          <span class="eyebrow">${isSignup ? "Create account" : "Welcome back"}</span>
          <h2>${isSignup ? "Start your marketplace account" : "Log in to your dashboard"}</h2>
          <p>${isSignup ? "Your account receives a unique user ID and secure saved profile details." : "Your previous crop and purchase activity stays connected to your account."}</p>
        </div>
        <form id="${isSignup ? "signupForm" : "loginForm"}" class="stack-form">
          ${isSignup ? `
            <label class="field"><span>Full name</span><input name="fullName" required placeholder="Enter your full name"></label>
            <label class="field"><span>City</span><input name="city" required placeholder="Enter your city"></label>
            <label class="field"><span>Phone</span><input name="phone" required placeholder="Enter your phone number"></label>
          ` : ""}
          <label class="field"><span>Email</span><input name="email" type="email" required placeholder="name@example.com"></label>
          <label class="field"><span>Password</span><input name="password" type="password" minlength="8" required placeholder="Minimum 8 characters"></label>
          <button type="submit" class="button button--primary button--full">${isSignup ? "Create Account" : "Log In"}</button>
        </form>
        <p class="auth-switch">
          ${isSignup ? "Already have an account?" : "Need a new account?"}
          <a href="${isSignup ? "/login" : "/signup"}" data-route="${isSignup ? "/login" : "/signup"}">${isSignup ? "Log in" : "Sign up"}</a>
        </p>
      </section>
    </div>
  `;
}

function renderShell({ title, subtitle, content }) {
  const initial = escapeHtml((state.user.fullName || "U").trim().charAt(0).toUpperCase());
  return `
    <div class="app-shell">
      <aside class="sidebar">
        <div class="brand-block">
          <span class="brand-mark">HH</span>
          <div>
            <strong>Harvest Hub</strong>
            <p>Central crop marketplace control panel</p>
          </div>
        </div>
        <nav class="nav-links">
          ${renderNavLink("/dashboard", "Home Dashboard")}
          ${renderNavLink("/add-crops", "Add Crops")}
          ${renderNavLink("/buy-crops", "Buy Crops")}
          ${renderNavLink("/profile", "Profile")}
        </nav>
      </aside>
      <main class="content-area">
        <section class="page-hero">
          <div>
            <span class="eyebrow">Marketplace Workspace</span>
            <h1>${escapeHtml(title)}</h1>
            <p>${escapeHtml(subtitle)}</p>
          </div>
          <div class="page-hero__meta profile-menu-container">
            <button class="profile-avatar" data-profile-toggle="true" aria-label="Open profile summary">${initial}</button>
            ${state.profileMenuOpen ? renderProfileMenu() : ""}
          </div>
        </section>
        ${content}
      </main>
    </div>
  `;
}

function renderDashboard() {
  const dashboard = state.dashboard || { stats: {}, recentActivity: [], topMatches: [], user: state.user };
  const stats = dashboard.stats || {};
  return `
    <article class="panel">
      <div class="panel__header"><h2>Top Matches</h2></div>
      ${renderMatches(dashboard.topMatches || [])}
    </article>
    <article class="panel dashboard-section">
      <div class="panel__header"><h2>Recent Activities</h2></div>
      ${renderActivityList(dashboard.recentActivity || [])}
    </article>
    <section class="stats-grid dashboard-section">
      ${renderStatCard("Crops Posted", stats.cropPosts || 0, "Listings created by you")}
      ${renderStatCard("Purchase Requests", stats.purchaseRequests || 0, "Demand records in your account")}
      ${renderStatCard("Market Listings", stats.marketplaceCrops || 0, "All available crop offers")}
      ${renderStatCard("Open Buyer Demand", stats.marketplaceDemand || 0, "Active requests across the market")}
      ${renderStatCard("Ready Matches", stats.availableMatches || 0, "High-fit opportunities involving you")}
    </section>
    <section class="quick-grid dashboard-section">
      ${renderQuickCard("/add-crops", "Add crop inventory", "Create a new crop post and review buyer demand.")}
      ${renderQuickCard("/buy-crops", "Buy crops", "Post buying needs and browse current marketplace supply.")}
      ${renderQuickCard("/profile", "Profile settings", "Update your city, phone, and account details.")}
    </section>
  `;
}

function renderAddCropsPage() {
  return `
    <section class="panel-grid">
      <article class="panel">
        <div class="panel__header"><h2>Create Crop Listing</h2></div>
        <form id="cropForm" class="stack-form">
          ${renderCropSelect("cropType")}
          <label class="field"><span>Quantity (kg)</span><input name="quantityKg" type="number" min="1" required></label>
          <label class="field"><span>Price per kg</span><input name="pricePerKg" type="number" min="0" step="0.01" required></label>
          <label class="field"><span>Notes</span><textarea name="notes" rows="4" placeholder="Mention quality, harvest date, or packaging details"></textarea></label>
          <button type="submit" class="button button--primary">Add Listing</button>
        </form>
      </article>
      <article class="panel">
        <div class="panel__header"><h2>Your Crop History</h2></div>
        ${renderCropTable(state.myCrops, true)}
      </article>
    </section>
    <article class="panel">
      <div class="panel__header">
        <h2>Buyer Demand</h2>
        <form id="demandFilterForm" class="inline-form">
          ${renderCropSelect("crop", true, state.filters.addCrops.crop)}
          <label class="field field--compact"><span>City</span><input name="city" value="${escapeHtml(state.filters.addCrops.city)}" placeholder="Filter by city"></label>
          <button type="submit" class="button">Filter</button>
        </form>
      </div>
      ${renderPurchaseTable(state.marketPurchases, false)}
    </article>
  `;
}

function renderBuyCropsPage() {
  return `
    <section class="panel-grid">
      <article class="panel">
        <div class="panel__header"><h2>Create Purchase Request</h2></div>
        <form id="purchaseForm" class="stack-form">
          ${renderCropSelect("cropType")}
          <label class="field"><span>Required quantity (kg)</span><input name="quantityKg" type="number" min="1" required></label>
          <label class="field"><span>Budget per kg</span><input name="maxBudget" type="number" min="0" step="0.01" required></label>
          <label class="field"><span>Notes</span><textarea name="notes" rows="4" placeholder="Add timing, quality, or delivery preferences"></textarea></label>
          <button type="submit" class="button button--primary">Add Request</button>
        </form>
      </article>
      <article class="panel">
        <div class="panel__header"><h2>Your Purchase History</h2></div>
        ${renderPurchaseTable(state.myPurchases, true)}
      </article>
    </section>
    <article class="panel">
      <div class="panel__header">
        <h2>Available Crop Listings</h2>
        <form id="cropFilterForm" class="inline-form">
          ${renderCropSelect("crop", true, state.filters.buyCrops.crop)}
          <label class="field field--compact"><span>City</span><input name="city" value="${escapeHtml(state.filters.buyCrops.city)}" placeholder="Filter by city"></label>
          <button type="submit" class="button">Filter</button>
        </form>
      </div>
      ${renderCropTable(state.marketCrops, false)}
    </article>
  `;
}

function renderProfilePage() {
  const profile = state.profile || state.user;
  return `
    <section class="panel-grid">
      <article class="panel">
        <div class="panel__header"><h2>Account Details</h2></div>
        <form id="profileForm" class="stack-form">
          <label class="field"><span>Full name</span><input name="fullName" required value="${escapeHtml(profile.fullName)}"></label>
          <label class="field"><span>City</span><input name="city" required value="${escapeHtml(profile.city)}"></label>
          <label class="field"><span>Phone</span><input name="phone" required value="${escapeHtml(profile.phone)}"></label>
          <label class="field"><span>Email</span><input value="${escapeHtml(profile.email)}" disabled></label>
          <label class="field"><span>User ID</span><input value="${escapeHtml(profile.userId)}" disabled></label>
          <button type="submit" class="button button--primary">Save Changes</button>
        </form>
      </article>
      <article class="panel">
        <div class="panel__header"><h2>Membership Snapshot</h2></div>
        <div class="profile-summary">
          <div><span>Account created</span><strong>${formatDate(profile.createdAt)}</strong></div>
          <div><span>Last updated</span><strong>${formatDate(profile.updatedAt)}</strong></div>
          <div><span>Current city</span><strong>${escapeHtml(profile.city)}</strong></div>
          <div><span>Contact</span><strong>${escapeHtml(profile.phone)}</strong></div>
        </div>
      </article>
    </section>
  `;
}

function renderCropTable(items, ownedView) {
  if (!items?.length) {
    return `<div class="empty-state">No crop records to show yet.</div>`;
  }
  return `<div class="table-wrap"><table><thead><tr><th>ID</th><th>Seller</th><th>Crop</th><th>City</th><th>Qty (kg)</th><th>Price/kg</th><th>Created</th></tr></thead><tbody>${items.map((item) => `<tr><td>${escapeHtml(item.cropPostId)}</td><td>${escapeHtml(ownedView ? "You" : item.sellerName)}</td><td>${escapeHtml(item.cropType)}</td><td>${escapeHtml(item.city)}</td><td>${formatNumber(item.quantityKg)}</td><td>${formatCurrency(item.pricePerKg)}</td><td>${formatDate(item.createdAt)}</td></tr>`).join("")}</tbody></table></div>`;
}

function renderPurchaseTable(items, ownedView) {
  if (!items?.length) {
    return `<div class="empty-state">No purchase requests available right now.</div>`;
  }
  return `<div class="table-wrap"><table><thead><tr><th>ID</th><th>Buyer</th><th>Crop</th><th>City</th><th>Qty (kg)</th><th>Budget/kg</th><th>Created</th></tr></thead><tbody>${items.map((item) => `<tr><td>${escapeHtml(item.purchaseRequestId)}</td><td>${escapeHtml(ownedView ? "You" : item.buyerName)}</td><td>${escapeHtml(item.cropType)}</td><td>${escapeHtml(item.city)}</td><td>${formatNumber(item.quantityKg)}</td><td>${formatCurrency(item.maxBudget)}</td><td>${formatDate(item.createdAt)}</td></tr>`).join("")}</tbody></table></div>`;
}

function renderMatches(items) {
  if (!items?.length) {
    return `<div class="empty-state">Your dashboard will surface top matches as soon as compatible listings or requests are available.</div>`;
  }
  return `<div class="match-grid">${items.map((item) => `<article class="match-card"><span class="match-card__score">${formatNumber(item.score)}</span><h3>${escapeHtml(item.cropType)} in ${escapeHtml(item.city)}</h3><p>${escapeHtml(item.farmerName)} can supply ${formatNumber(item.availableQuantity)} kg at ${formatCurrency(item.pricePerUnit)} per kg.</p><span class="badge">Buyer: ${escapeHtml(item.buyerName)}</span></article>`).join("")}</div>`;
}

function renderActivityList(items) {
  if (!items?.length) {
    return `<div class="empty-state">Post a crop or add a buying request to build your activity timeline.</div>`;
  }
  return `<div class="activity-list">${items.map((item) => `<button class="activity-item" data-route="${item.route}"><strong>${escapeHtml(item.title)}</strong><span>${escapeHtml(item.subtitle)}</span><small>${formatDate(item.createdAt)}</small></button>`).join("")}</div>`;
}

function renderNavLink(route, label) {
  const active = state.route === route ? "nav-link nav-link--active" : "nav-link";
  return `<button class="${active}" data-route="${route}">${label}</button>`;
}

function renderProfileMenu() {
  const profile = state.profile || state.dashboard?.user || state.user;
  return `
    <div class="profile-menu">
      <div class="profile-menu__header">
        <strong>${escapeHtml(profile.fullName)}</strong>
        <span>${escapeHtml(profile.userId)}</span>
      </div>
      <div class="profile-summary profile-summary--compact">
        <div><span>Email</span><strong>${escapeHtml(profile.email)}</strong></div>
        <div><span>City</span><strong>${escapeHtml(profile.city)}</strong></div>
      </div>
      <div class="profile-menu__actions">
        <button class="button" data-route="/profile">Open Profile</button>
        <button class="button button--ghost" data-logout="true">Log Out</button>
      </div>
    </div>
  `;
}

function renderQuickCard(route, title, description) {
  return `<button class="quick-card" data-route="${route}"><strong>${title}</strong><span>${description}</span></button>`;
}

function renderStatCard(label, value, detail) {
  return `<article class="stat-card"><span>${formatNumber(value)}</span><strong>${label}</strong><p>${detail}</p></article>`;
}

function renderCropSelect(name, allowAll = false, selected = "") {
  const options = [allowAll ? `<option value="">All crop types</option>` : `<option value="" disabled ${selected ? "" : "selected"}>Select crop type</option>`]
    .concat(CROP_TYPES.map((crop) => `<option value="${crop}" ${selected === crop ? "selected" : ""}>${crop}</option>`))
    .join("");
  return `<label class="field"><span>${name === "cropType" ? "Crop type" : "Crop filter"}</span><select name="${name}" ${allowAll ? "" : "required"}>${options}</select></label>`;
}

function renderLoading(message) {
  appRoot.innerHTML = `<div class="loading-screen"><div class="loading-card"><div class="spinner"></div><p>${escapeHtml(message)}</p></div></div>`;
}

function bindForms() {
  bindForm("loginForm", async (form) => {
    const payload = await apiRequest("/api/auth/login", { method: "POST", data: formDataToObject(form) });
    state.user = payload.user;
    showToast(payload.message);
    await navigate("/dashboard", { replace: true });
  });

  bindForm("signupForm", async (form) => {
    const payload = await apiRequest("/api/auth/signup", { method: "POST", data: formDataToObject(form) });
    state.user = payload.user;
    showToast(payload.message);
    await navigate("/dashboard", { replace: true });
  });

  bindForm("cropForm", async (form) => {
    const payload = await apiRequest("/api/crops", { method: "POST", data: formDataToObject(form) });
    showToast(payload.message);
    form.reset();
    await loadRouteData("/add-crops");
    renderApp();
    bindForms();
  });

  bindForm("purchaseForm", async (form) => {
    const payload = await apiRequest("/api/purchases", { method: "POST", data: formDataToObject(form) });
    showToast(payload.message);
    form.reset();
    await loadRouteData("/buy-crops");
    renderApp();
    bindForms();
  });

  bindForm("profileForm", async (form) => {
    const payload = await apiRequest("/api/profile", { method: "POST", data: formDataToObject(form) });
    state.user = payload.user;
    state.profile = payload.user;
    showToast(payload.message);
    renderApp();
    bindForms();
  });

  bindForm("demandFilterForm", async (form) => {
    const data = formDataToObject(form);
    state.filters.addCrops = data;
    await loadRouteData("/add-crops");
    renderApp();
    bindForms();
  });

  bindForm("cropFilterForm", async (form) => {
    const data = formDataToObject(form);
    state.filters.buyCrops = data;
    await loadRouteData("/buy-crops");
    renderApp();
    bindForms();
  });
}

function bindForm(id, handler) {
  const form = document.getElementById(id);
  if (!form) {
    return;
  }
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
      await handler(form);
    } catch (error) {
      showToast(error.message || "Unable to complete the request.");
    }
  });
}

async function handleLogout() {
  await apiRequest("/api/auth/logout", { method: "POST", suppressAuthRedirect: true });
  state.user = null;
  state.dashboard = null;
  state.profile = null;
  state.myCrops = [];
  state.marketCrops = [];
  state.myPurchases = [];
  state.marketPurchases = [];
  showToast("Logged out successfully.");
  await navigate("/login", { replace: true });
}

async function apiRequest(path, options = {}) {
  const { method = "GET", data = null, suppressAuthRedirect = false } = options;
  const requestOptions = { method, headers: {} };

  if (data) {
    requestOptions.headers["Content-Type"] = "application/x-www-form-urlencoded;charset=UTF-8";
    requestOptions.body = new URLSearchParams(data).toString();
  }

  const response = await fetch(path, requestOptions);
  const payload = await response.json().catch(() => ({}));

  if (response.status === 401 && !suppressAuthRedirect) {
    state.user = null;
    await navigate("/login", { replace: true });
    throw new Error(payload.error || "Please log in to continue.");
  }

  if (!response.ok) {
    throw new Error(payload.error || "Request failed.");
  }

  return payload;
}

function formDataToObject(form) {
  return Object.fromEntries(new FormData(form).entries());
}

function routeLabel(route) {
  return {
    "/login": "login",
    "/signup": "signup",
    "/dashboard": "dashboard",
    "/add-crops": "crop tools",
    "/buy-crops": "buying tools",
    "/profile": "profile"
  }[route] || "page";
}

function formatDate(value) {
  if (!value) {
    return "Not available";
  }
  return new Date(value).toLocaleString("en-IN", {
    dateStyle: "medium",
    timeStyle: "short"
  });
}

function formatNumber(value) {
  return new Intl.NumberFormat("en-IN", { maximumFractionDigits: 2 }).format(Number(value || 0));
}

function formatCurrency(value) {
  return `Rs. ${formatNumber(value)}`;
}

function showToast(message) {
  toast.textContent = message;
  toast.hidden = false;
  clearTimeout(showToast.timeoutId);
  showToast.timeoutId = setTimeout(() => {
    toast.hidden = true;
  }, 3200);
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}
