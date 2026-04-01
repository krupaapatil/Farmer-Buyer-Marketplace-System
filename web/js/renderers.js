import {
  AUTH_ROUTES,
  DEFAULT_CROP_TYPES,
  DEFAULT_ROLES,
  DEFAULT_STATUSES,
  DEFAULT_UNITS,
  ROUTE_META
} from "./config.js";
import {
  escapeHtml,
  formatCurrency,
  formatDate,
  formatNumber,
  formatRelativeTime,
  sentenceCase
} from "./utils.js";

function renderNavLink(route, label, isActive) {
  return `
    <button
      type="button"
      class="nav-link ${isActive ? "nav-link--active" : ""}"
      data-route="${route}">
      <span>${escapeHtml(label)}</span>
    </button>
  `;
}

function renderDrawer(state) {
  const user = state.user || {};
  const isDocked = state.ui.navDocked && AUTH_ROUTES.has(state.route);
  const isOpen = state.ui.drawerOpen;
  return `
    <aside
      class="drawer ${isDocked ? "drawer--docked" : ""} ${isOpen ? "drawer--open" : ""}"
      aria-label="Primary navigation">
      <div class="drawer__header">
        <div class="brand-lockup">
          <span class="brand-mark">HH</span>
          <div>
            <strong>Harvest Hub</strong>
            <p>Farmer and buyer marketplace</p>
          </div>
        </div>
        <div class="drawer__header-actions">
          <button
            type="button"
            class="icon-button drawer__dock-toggle"
            data-nav-mode="${isDocked ? "drawer" : "dock"}"
            aria-label="${isDocked ? "Switch to drawer mode" : "Dock sidebar"}">
            ${isDocked ? "Docked" : "Dock"}
          </button>
          <button type="button" class="icon-button" data-close-drawer="true" aria-label="Close navigation">
            Close
          </button>
        </div>
      </div>

      <nav class="drawer__nav">
        ${renderNavLink("/dashboard", "Home Dashboard", state.route === "/dashboard")}
        ${renderNavLink("/add-crops", "Add Crops", state.route === "/add-crops")}
        ${renderNavLink("/buy-crops", "Buy Crops", state.route === "/buy-crops")}
        ${renderNavLink("/profile", "Profile", state.route === "/profile")}
      </nav>

      <div class="drawer__footer">
        <button type="button" class="drawer-profile" data-route="/profile">
          <span class="avatar-chip">${escapeHtml(user.initials || "U")}</span>
          <span class="drawer-profile__copy">
            <strong>${escapeHtml(user.fullName || "Your profile")}</strong>
            <small>${escapeHtml(sentenceCase(user.role || "member"))}</small>
          </span>
        </button>
      </div>
    </aside>
  `;
}

function renderProfileMenu(state) {
  const user = state.user || {};
  return `
    <div class="profile-menu ${state.ui.profileMenuOpen ? "profile-menu--open" : ""}">
      <button
        type="button"
        class="profile-menu__trigger"
        data-profile-toggle="true"
        aria-haspopup="menu"
        aria-expanded="${state.ui.profileMenuOpen ? "true" : "false"}">
        <span class="avatar-chip avatar-chip--soft">${escapeHtml(user.initials || "U")}</span>
        <span class="profile-menu__copy">
          <strong>${escapeHtml(user.fullName || "Member")}</strong>
          <small>${escapeHtml(user.location || "Marketplace")}</small>
        </span>
      </button>
      <div class="profile-menu__panel" role="menu">
        <button type="button" class="profile-menu__item" role="menuitem" data-route="/profile">Profile</button>
        <button type="button" class="profile-menu__item" role="menuitem" data-logout="true">Log Out</button>
      </div>
    </div>
  `;
}

function renderTopHeader(state) {
  const meta = ROUTE_META[state.route] || ROUTE_META["/dashboard"];
  return `
    <header class="top-header">
      <div class="top-header__left">
        <button type="button" class="icon-button hamburger-button" data-drawer-toggle="true" aria-label="Toggle navigation">
          <span></span><span></span><span></span>
        </button>
        <div class="top-header__titles">
          <span class="eyebrow">Marketplace workspace</span>
          <strong>${escapeHtml(meta.title)}</strong>
        </div>
      </div>
      <div class="top-header__right">
        <span class="header-pill">${escapeHtml(sentenceCase(state.user?.role || "member"))}</span>
        ${renderProfileMenu(state)}
      </div>
    </header>
  `;
}

function renderHeroCard(state) {
  const meta = ROUTE_META[state.route] || ROUTE_META["/dashboard"];
  const user = state.user || {};
  return `
    <section class="hero-card">
      <div class="hero-card__content">
        <span class="eyebrow eyebrow--light">Harvest Hub</span>
        <h1>${escapeHtml(meta.title)}</h1>
        ${meta.subtitle ? `<p>${escapeHtml(meta.subtitle)}</p>` : ""}
        <div class="hero-card__actions">
          <button type="button" class="button button--primary" data-route="/add-crops">Create Listing</button>
          <button type="button" class="button button--secondary" data-route="/buy-crops">Browse Supply</button>
        </div>
      </div>
      <div class="hero-card__profile">
        <div class="hero-card__profile-row">
          <span class="avatar-chip avatar-chip--large">${escapeHtml(user.initials || "U")}</span>
          <div>
            <strong>${escapeHtml(user.fullName || "Marketplace member")}</strong>
            <p>${escapeHtml(user.location || "Location pending")}</p>
          </div>
        </div>
        <dl class="hero-card__meta">
          <div>
            <dt>Role</dt>
            <dd>${escapeHtml(sentenceCase(user.role || "member"))}</dd>
          </div>
          <div>
            <dt>Member since</dt>
            <dd>${escapeHtml(formatDate(user.createdAt))}</dd>
          </div>
        </dl>
      </div>
    </section>
  `;
}

function renderSectionHeader(title, actionLabel = "", actionAttrs = "") {
  return `
    <div class="section-header">
      <div>
        <h2>${escapeHtml(title)}</h2>
      </div>
      ${actionLabel ? `<button type="button" class="button button--ghost" ${actionAttrs}>${escapeHtml(actionLabel)}</button>` : ""}
    </div>
  `;
}

function renderStatCard(label, value, detail) {
  return `
    <article class="stat-card">
      <span>${escapeHtml(formatNumber(value))}</span>
      <strong>${escapeHtml(label)}</strong>
      <p>${escapeHtml(detail)}</p>
    </article>
  `;
}

function renderEmptyState({ icon, title, body, actionLabel, actionRoute }) {
  return `
    <div class="empty-state-card">
      <div class="empty-state-card__icon" aria-hidden="true">${icon}</div>
      <h3>${escapeHtml(title)}</h3>
      ${body ? `<p>${escapeHtml(body)}</p>` : ""}
      ${actionLabel ? `<button type="button" class="button button--primary" data-route="${actionRoute}">${escapeHtml(actionLabel)}</button>` : ""}
    </div>
  `;
}

function renderMatchCard(match, state) {
  const actionRoute = state.user?.role === "farmer" ? "/add-crops" : "/buy-crops";
  return `
    <article class="match-card">
      <div class="match-card__top">
        <div>
          <span class="chip chip--success">${escapeHtml(sentenceCase(match.status || "Match ready"))}</span>
          <h3>${escapeHtml(match.cropType)}</h3>
        </div>
        <span class="score-pill">${escapeHtml(`${formatNumber(match.score)}%`)}</span>
      </div>
      <dl class="match-card__details">
        <div><dt>Location</dt><dd>${escapeHtml(match.location)}</dd></div>
        <div><dt>Quantity</dt><dd>${escapeHtml(`${formatNumber(match.matchedQuantity)} / ${formatNumber(match.requestedQuantity)} ${match.unit || "kg"}`)}</dd></div>
        <div><dt>Expected price</dt><dd>${escapeHtml(formatCurrency(match.pricePerUnit))}</dd></div>
        <div><dt>Budget ceiling</dt><dd>${escapeHtml(formatCurrency(match.buyerBudget))}</dd></div>
      </dl>
      <p class="match-card__copy">
        ${escapeHtml(`${match.farmerName} can supply ${match.availableQuantity} units for ${match.buyerName}.`)}
      </p>
      <button type="button" class="button button--secondary button--full" data-route="${actionRoute}">
        ${escapeHtml(match.ctaLabel || "View Match")}
      </button>
    </article>
  `;
}

function renderActivityCard(activity) {
  return `
    <button type="button" class="activity-card" data-route="${escapeHtml(activity.route || "/dashboard")}">
      <div class="activity-card__top">
        <span class="chip">${escapeHtml(sentenceCase(activity.status || "Active"))}</span>
        <span class="activity-card__time">${escapeHtml(formatRelativeTime(activity.createdAt))}</span>
      </div>
      <strong>${escapeHtml(activity.title)}</strong>
      <p>${escapeHtml(activity.subtitle)}</p>
      <div class="activity-card__footer">
        <span>${escapeHtml(activity.actionLabel || "Open item")}</span>
        <small>${escapeHtml(formatDate(activity.createdAt))}</small>
      </div>
    </button>
  `;
}

function renderListingCard(item, kind) {
  const priceLabel = kind === "crop"
    ? `Price ${formatCurrency(item.pricePerKg)} / ${item.unit}`
    : `Budget ${formatCurrency(item.maxBudget)} / ${item.unit}`;
  const ownerLabel = kind === "crop" ? item.sellerName : item.buyerName;
  return `
    <article class="listing-card">
      <div class="listing-card__top">
        <div>
          <span class="chip">${escapeHtml(sentenceCase(item.status || "Active"))}</span>
          <h3>${escapeHtml(item.cropType)}</h3>
        </div>
        <span class="listing-card__quantity">${escapeHtml(`${formatNumber(item.quantityKg)} ${item.unit}`)}</span>
      </div>
      <p class="listing-card__owner">${escapeHtml(ownerLabel)} • ${escapeHtml(item.location || item.city)}</p>
      <p class="listing-card__price">${escapeHtml(priceLabel)}</p>
      <p class="listing-card__notes">${escapeHtml(item.notes || "No extra notes added yet.")}</p>
      <small>${escapeHtml(formatDate(item.createdAt))}</small>
    </article>
  `;
}

function renderSelectField({ label, name, value = "", options, required = false, compact = false }) {
  const list = options.map((option) => {
    const optionValue = typeof option === "string" ? option : option.value;
    const optionLabel = typeof option === "string" ? option : option.label;
    return `<option value="${escapeHtml(optionValue)}" ${value === optionValue ? "selected" : ""}>${escapeHtml(optionLabel)}</option>`;
  }).join("");
  return `
    <label class="form-field ${compact ? "form-field--compact" : ""}">
      <span>${escapeHtml(label)}${required ? ' <em>*</em>' : ""}</span>
      <select name="${escapeHtml(name)}" ${required ? "required" : ""}>
        ${list}
      </select>
      <small class="field-error" data-error-for="${escapeHtml(name)}"></small>
    </label>
  `;
}

function renderInputField({
  label,
  name,
  type = "text",
  value = "",
  placeholder = "",
  required = false,
  rows = 0,
  min = "",
  step = "",
  disabled = false,
  compact = false
}) {
  const requiredMark = required ? ' <em>*</em>' : "";
  const input = rows > 0
    ? `<textarea name="${escapeHtml(name)}" rows="${rows}" placeholder="${escapeHtml(placeholder)}" ${required ? "required" : ""}>${escapeHtml(value)}</textarea>`
    : `<input
        name="${escapeHtml(name)}"
        type="${escapeHtml(type)}"
        value="${escapeHtml(value)}"
        placeholder="${escapeHtml(placeholder)}"
        ${required ? "required" : ""}
        ${min !== "" ? `min="${escapeHtml(min)}"` : ""}
        ${step !== "" ? `step="${escapeHtml(step)}"` : ""}
        ${disabled ? "disabled" : ""}>`;

  return `
    <label class="form-field ${compact ? "form-field--compact" : ""}">
      <span>${escapeHtml(label)}${requiredMark}</span>
      ${input}
      <small class="field-error" data-error-for="${escapeHtml(name)}"></small>
    </label>
  `;
}

function renderFilterBar(kind, filters, catalog) {
  const budgetLabel = kind === "crops" ? "Max price" : "Max budget";
  const budgetName = kind === "crops" ? "maxPrice" : "maxBudget";
  return `
    <form id="${kind === "crops" ? "cropFilterForm" : "demandFilterForm"}" class="filter-bar">
      ${renderSelectField({
        label: "Crop type",
        name: "crop",
        value: filters.crop || "",
        compact: true,
        options: [{ value: "", label: "All crop types" }].concat((catalog.cropTypes || DEFAULT_CROP_TYPES).map((crop) => ({ value: crop, label: crop })))
      })}
      ${renderInputField({ label: "Location", name: "city", value: filters.city || "", placeholder: "Filter by city or district", compact: true })}
      ${renderInputField({ label: "Min quantity", name: "minQuantity", type: "number", value: filters.minQuantity || "", placeholder: "Any", compact: true, min: "1" })}
      ${renderInputField({ label: budgetLabel, name: budgetName, type: "number", value: filters[budgetName] || "", placeholder: "Any", compact: true, min: "1", step: "0.01" })}
      ${renderSelectField({
        label: "Status",
        name: "status",
        value: filters.status || "active",
        compact: true,
        options: [{ value: "", label: "Any status" }].concat((catalog.statuses || DEFAULT_STATUSES).map((status) => ({ value: status, label: sentenceCase(status) })))
      })}
      <button type="submit" class="button button--secondary">Apply Filters</button>
    </form>
  `;
}

function renderSkeletonGrid(count = 4) {
  return `<div class="skeleton-grid">${Array.from({ length: count }, () => `<div class="skeleton-card"></div>`).join("")}</div>`;
}

function renderDashboard(state) {
  const dashboard = state.dashboard || {};
  const stats = dashboard.stats || {};
  const topMatches = dashboard.topMatches || [];
  const activities = dashboard.recentActivity || [];
  const role = state.user?.role || "both";
  const emptyAction = role === "buyer" ? { label: "Create Request", route: "/buy-crops" } : { label: "Browse Buyers", route: "/add-crops" };

  if (state.loading.route) {
    return `
      ${renderHeroCard(state)}
      ${renderSkeletonGrid(5)}
      <section class="dashboard-grid">
        <div class="surface-card">${renderSkeletonGrid(2)}</div>
        <div class="surface-card">${renderSkeletonGrid(3)}</div>
      </section>
    `;
  }

  return `
    ${renderHeroCard(state)}
    <section class="stats-grid">
      ${renderStatCard("Your Listings", stats.cropPosts || 0, "Active crop posts created by you")}
      ${renderStatCard("Your Requests", stats.purchaseRequests || 0, "Purchase requests currently open")}
      ${renderStatCard("Market Listings", stats.marketplaceCrops || 0, "Visible crop supply across the market")}
      ${renderStatCard("Buyer Demand", stats.marketplaceDemand || 0, "Open requests from active buyers")}
      ${renderStatCard("Top Matches", stats.availableMatches || 0, "High-fit matches involving your account")}
    </section>

    <section class="dashboard-grid">
      <article class="surface-card">
        ${renderSectionHeader("Top Matches")}
        ${topMatches.length
          ? `<div class="match-grid">${topMatches.map((match) => renderMatchCard(match, state)).join("")}</div>`
          : renderEmptyState({
              icon: "🌾",
              title: "No polished matches yet",
              body: "",
              actionLabel: emptyAction.label,
              actionRoute: emptyAction.route
            })}
      </article>

      <article class="surface-card">
        ${renderSectionHeader("Recent Activities", "View All", 'data-open-modal="activities"')}
        ${activities.length
          ? `<div class="activity-list">${activities.map((activity) => renderActivityCard(activity)).join("")}</div>`
          : renderEmptyState({
              icon: "📌",
              title: "Your timeline is still empty",
              body: "",
              actionLabel: "Create Listing",
              actionRoute: "/add-crops"
            })}
      </article>
    </section>
  `;
}

function renderFormIntro(title, body) {
  return `
    <div class="card-intro">
      <h2>${escapeHtml(title)}</h2>
      ${body ? `<p>${escapeHtml(body)}</p>` : ""}
    </div>
  `;
}

function renderAddCropsPage(state) {
  const catalog = state.catalog;
  const profile = state.user || {};
  const myCrops = state.myCrops || [];
  const marketDemand = state.marketPurchases || [];

  if (state.loading.route) {
    return `
      ${renderHeroCard(state)}
      ${renderSkeletonGrid(4)}
      <section class="surface-card">${renderSkeletonGrid(3)}</section>
    `;
  }

  return `
    ${renderHeroCard(state)}
    <section class="page-grid">
      <article class="surface-card">
        ${renderFormIntro("Create crop listing", "")}
        <form id="cropForm" class="stack-form">
          ${renderSelectField({ label: "Crop type", name: "cropType", required: true, value: "", options: [{ value: "", label: "Select crop type" }].concat((catalog.cropTypes || DEFAULT_CROP_TYPES).map((crop) => ({ value: crop, label: crop }))) })}
          <div class="form-row">
            ${renderInputField({ label: "Quantity", name: "quantityKg", type: "number", required: true, placeholder: "Ex: 250", min: "1" })}
            ${renderSelectField({ label: "Unit", name: "unit", required: true, value: catalog.units?.[0] || DEFAULT_UNITS[0], options: (catalog.units || DEFAULT_UNITS).map((unit) => ({ value: unit, label: sentenceCase(unit) })) })}
          </div>
          <div class="form-row">
            ${renderInputField({ label: "Expected price", name: "pricePerKg", type: "number", required: true, placeholder: "Ex: 24.50", min: "1", step: "0.01" })}
            ${renderInputField({ label: "Location", name: "location", required: true, value: profile.location || "", placeholder: "City or district" })}
          </div>
          ${renderInputField({ label: "Notes", name: "notes", rows: 4, value: "", placeholder: "Mention quality, harvest freshness, packaging, delivery window, or certifications." })}
          <button type="submit" class="button button--primary button--full" data-submit-label="Add Listing">Add Listing</button>
        </form>
      </article>

      <article class="surface-card">
        ${renderSectionHeader("Your listings")}
        ${myCrops.length
          ? `<div class="listing-grid">${myCrops.map((item) => renderListingCard(item, "crop")).join("")}</div>`
          : renderEmptyState({
              icon: "🧺",
              title: "No crop listings yet",
              body: "Your new inventory posts will appear here with status, price, notes, and timestamps once you publish them."
            })}
      </article>
    </section>

    <article class="surface-card">
      ${renderSectionHeader("Buyer Demand")}
      ${renderFilterBar("demand", state.filters.addCrops, catalog)}
      ${marketDemand.length
        ? `<div class="listing-grid">${marketDemand.map((item) => renderListingCard(item, "purchase")).join("")}</div>`
        : renderEmptyState({
            icon: "🤝",
            title: "No buyer demand matches these filters",
            body: "Try widening the location or quantity filters to surface more buyer requests."
          })}
    </article>
  `;
}

function renderBuyCropsPage(state) {
  const catalog = state.catalog;
  const profile = state.user || {};
  const marketCrops = state.marketCrops || [];
  const myPurchases = state.myPurchases || [];

  if (state.loading.route) {
    return `
      ${renderHeroCard(state)}
      ${renderSkeletonGrid(4)}
      <section class="surface-card">${renderSkeletonGrid(3)}</section>
    `;
  }

  return `
    ${renderHeroCard(state)}
    <section class="page-grid">
      <article class="surface-card">
        ${renderFormIntro("Create purchase request", "")}
        <form id="purchaseForm" class="stack-form">
          ${renderSelectField({ label: "Crop type", name: "cropType", required: true, value: "", options: [{ value: "", label: "Select crop type" }].concat((catalog.cropTypes || DEFAULT_CROP_TYPES).map((crop) => ({ value: crop, label: crop }))) })}
          <div class="form-row">
            ${renderInputField({ label: "Required quantity", name: "quantityKg", type: "number", required: true, placeholder: "Ex: 400", min: "1" })}
            ${renderSelectField({ label: "Unit", name: "unit", required: true, value: catalog.units?.[0] || DEFAULT_UNITS[0], options: (catalog.units || DEFAULT_UNITS).map((unit) => ({ value: unit, label: sentenceCase(unit) })) })}
          </div>
          <div class="form-row">
            ${renderInputField({ label: "Budget per unit", name: "maxBudget", type: "number", required: true, placeholder: "Ex: 28.00", min: "1", step: "0.01" })}
            ${renderInputField({ label: "Location", name: "location", required: true, value: profile.location || "", placeholder: "City or district" })}
          </div>
          ${renderInputField({ label: "Notes", name: "notes", rows: 4, value: "", placeholder: "Add delivery expectations, quality requirements, timing, or preferred packaging." })}
          <button type="submit" class="button button--primary button--full" data-submit-label="Add Request">Add Request</button>
        </form>
      </article>

      <article class="surface-card">
        ${renderSectionHeader("Your requests")}
        ${myPurchases.length
          ? `<div class="listing-grid">${myPurchases.map((item) => renderListingCard(item, "purchase")).join("")}</div>`
          : renderEmptyState({
              icon: "🛒",
              title: "No purchase requests yet",
              body: "Published requests will land here so you can revisit your demand, pricing, and status at a glance."
            })}
      </article>
    </section>

    <article class="surface-card">
      ${renderSectionHeader("Available Crop Listings")}
      ${renderFilterBar("crops", state.filters.buyCrops, catalog)}
      ${marketCrops.length
        ? `<div class="listing-grid">${marketCrops.map((item) => renderListingCard(item, "crop")).join("")}</div>`
        : renderEmptyState({
            icon: "🚜",
            title: "No listings match these filters",
            body: "Adjust price, quantity, or location filters to reveal more crop supply."
          })}
    </article>
  `;
}

function renderProfilePage(state) {
  const profile = state.profile || state.user || {};
  if (state.loading.route) {
    return `
      ${renderHeroCard(state)}
      ${renderSkeletonGrid(4)}
    `;
  }

  return `
    ${renderHeroCard(state)}
    <section class="page-grid">
      <article class="surface-card">
        ${renderFormIntro("Account details", "Your saved role and location feed the navigation, form defaults, and future match logic across the app.")}
        <form id="profileForm" class="stack-form">
          ${renderInputField({ label: "Full name", name: "fullName", required: true, value: profile.fullName || "", placeholder: "Your full name" })}
          ${renderSelectField({ label: "Marketplace role", name: "role", required: true, value: profile.role || "both", options: DEFAULT_ROLES.map((role) => ({ value: role, label: sentenceCase(role) })) })}
          <div class="form-row">
            ${renderInputField({ label: "Location", name: "city", required: true, value: profile.location || "", placeholder: "City or district" })}
            ${renderInputField({ label: "Phone", name: "phone", required: true, value: profile.phone || "", placeholder: "Contact number" })}
          </div>
          ${renderInputField({ label: "Email", name: "email", value: profile.email || "", disabled: true })}
          ${renderInputField({ label: "User ID", name: "userId", value: profile.userId || "", disabled: true })}
          <div class="button-row">
            <button type="submit" class="button button--primary" data-submit-label="Save Changes">Save Changes</button>
            <button type="button" class="button button--ghost" data-logout="true">Log Out</button>
          </div>
        </form>
      </article>

      <article class="surface-card">
        ${renderSectionHeader("Membership snapshot")}
        <div class="summary-grid">
          <article class="summary-tile"><span>Role</span><strong>${escapeHtml(sentenceCase(profile.role || "member"))}</strong></article>
          <article class="summary-tile"><span>Location</span><strong>${escapeHtml(profile.location || "Not set")}</strong></article>
          <article class="summary-tile"><span>Joined</span><strong>${escapeHtml(formatDate(profile.createdAt))}</strong></article>
          <article class="summary-tile"><span>Last updated</span><strong>${escapeHtml(formatDate(profile.updatedAt))}</strong></article>
        </div>
      </article>
    </section>
  `;
}

function renderAuthPage(mode) {
  const isSignup = mode === "signup";
  return `
    <div class="auth-shell">
      <section class="auth-visual">
        <div class="auth-visual__inner">
          <span class="eyebrow eyebrow--light">Responsive marketplace</span>
          <h1>Harvest Hub</h1>
        </div>
      </section>

      <section class="auth-panel">
        <div class="card-intro">
          <h2>${isSignup ? "Create your account" : "Log in to continue"}</h2>
          <p>${isSignup ? "Choose your role, keep your location current, and land directly inside the responsive dashboard." : "Your dashboard, matches, activity history, and saved profile will be ready when you sign in."}</p>
        </div>

        <form id="${isSignup ? "signupForm" : "loginForm"}" class="stack-form">
          ${isSignup ? `
            ${renderInputField({ label: "Full name", name: "fullName", required: true, placeholder: "Your full name" })}
            ${renderSelectField({ label: "Marketplace role", name: "role", required: true, value: "both", options: DEFAULT_ROLES.map((role) => ({ value: role, label: sentenceCase(role) })) })}
            ${renderInputField({ label: "Location", name: "city", required: true, placeholder: "City or district" })}
            ${renderInputField({ label: "Phone", name: "phone", required: true, placeholder: "Contact number" })}
          ` : ""}
          ${renderInputField({ label: "Email", name: "email", type: "email", required: true, placeholder: "name@example.com" })}
          ${renderInputField({ label: "Password", name: "password", type: "password", required: true, placeholder: "Minimum 8 characters" })}
          <button type="submit" class="button button--primary button--full" data-submit-label="${isSignup ? "Create Account" : "Log In"}">${isSignup ? "Create Account" : "Log In"}</button>
        </form>

        <p class="auth-switch">
          ${isSignup ? "Already have an account?" : "Need a new account?"}
          <button type="button" class="text-link" data-route="${isSignup ? "/login" : "/signup"}">${isSignup ? "Log in" : "Sign up"}</button>
        </p>
      </section>
    </div>
  `;
}

function renderModal(state) {
  if (!state.ui.modal) {
    return "";
  }

  const modal = state.ui.modal;
  const body = modal.type === "activities"
    ? `<div class="activity-list">${(state.activities || []).map((activity) => renderActivityCard(activity)).join("")}</div>`
    : modal.content || "";

  return `
    <div class="modal-backdrop" data-close-modal="true"></div>
    <section class="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title">
      <div class="modal__header">
        <h2 id="modal-title">${escapeHtml(modal.title)}</h2>
        <button type="button" class="icon-button" data-close-modal="true" aria-label="Close dialog">Close</button>
      </div>
      <div class="modal__body">${body}</div>
    </section>
  `;
}

function renderToastStack(state) {
  return `
    <div class="toast-stack" aria-live="polite" aria-atomic="true">
      ${(state.ui.toasts || []).map((toast) => `
        <div class="toast toast--${escapeHtml(toast.tone || "success")}">
          <strong>${escapeHtml(sentenceCase(toast.tone || "success"))}</strong>
          <span>${escapeHtml(toast.message)}</span>
        </div>
      `).join("")}
    </div>
  `;
}

function renderPage(state) {
  if (state.route === "/dashboard") {
    return renderDashboard(state);
  }
  if (state.route === "/add-crops") {
    return renderAddCropsPage(state);
  }
  if (state.route === "/buy-crops") {
    return renderBuyCropsPage(state);
  }
  return renderProfilePage(state);
}

export function renderAppView(state) {
  if (!state.user) {
    return `
      ${renderAuthPage(state.route === "/signup" ? "signup" : "login")}
      ${renderToastStack(state)}
    `;
  }

  return `
    <div class="workspace ${state.ui.navDocked ? "workspace--nav-docked" : ""}">
      ${renderDrawer(state)}
      <div class="drawer-backdrop ${state.ui.drawerOpen ? "drawer-backdrop--visible" : ""}" data-close-drawer="true"></div>
      <div class="workspace__main">
        ${renderTopHeader(state)}
        <main class="page-shell">
          ${renderPage(state)}
        </main>
      </div>
      ${renderModal(state)}
      ${renderToastStack(state)}
    </div>
  `;
}
