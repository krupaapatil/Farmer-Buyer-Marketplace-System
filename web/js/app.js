import { apiRequest } from "./api.js";
import {
  AUTH_ROUTES,
  BREAKPOINTS,
  DEFAULT_CROP_TYPES,
  DEFAULT_STATUSES,
  DEFAULT_UNITS,
  DEFAULT_ROLES,
  ROUTES
} from "./config.js";
import { renderAppView } from "./renderers.js";
import { isDesktopViewport } from "./utils.js";

const appRoot = document.getElementById("app");
const navModeStorageKey = "harvest-hub-nav-docked";

const state = {
  user: null,
  route: "/",
  dashboard: null,
  profile: null,
  myCrops: [],
  marketCrops: [],
  myPurchases: [],
  marketPurchases: [],
  activities: [],
  catalog: {
    cropTypes: DEFAULT_CROP_TYPES,
    units: DEFAULT_UNITS,
    statuses: DEFAULT_STATUSES,
    roles: DEFAULT_ROLES
  },
  loading: {
    route: false
  },
  filters: {
    addCrops: { crop: "", city: "", minQuantity: "", maxBudget: "", status: "active" },
    buyCrops: { crop: "", city: "", minQuantity: "", maxPrice: "", status: "active" }
  },
  ui: {
    drawerOpen: false,
    navDocked: true,
    profileMenuOpen: false,
    modal: null,
    toasts: []
  }
};

const formSchemas = {
  signupForm: {
    fullName: (value) => value.trim().length < 2 ? "Enter your full name." : "",
    role: (value) => !DEFAULT_ROLES.includes(value) ? "Choose a marketplace role." : "",
    city: (value) => value.trim().length < 2 ? "Enter your location." : "",
    phone: (value) => !/^[0-9+()\-\s]{7,20}$/.test(value.trim()) ? "Enter a valid phone number." : "",
    email: (value) => !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim()) ? "Enter a valid email address." : "",
    password: (value) => value.length < 8 ? "Password must be at least 8 characters." : ""
  },
  loginForm: {
    email: (value) => !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim()) ? "Enter a valid email address." : "",
    password: (value) => value.length < 8 ? "Password must be at least 8 characters." : ""
  },
  cropForm: {
    cropType: (value) => !state.catalog.cropTypes.includes(value) ? "Select a crop type." : "",
    quantityKg: (value) => Number(value) <= 0 ? "Quantity must be greater than zero." : "",
    unit: (value) => !state.catalog.units.includes(value) ? "Select a supported unit." : "",
    pricePerKg: (value) => Number(value) <= 0 ? "Expected price must be greater than zero." : "",
    location: (value) => value.trim().length < 2 ? "Enter the listing location." : "",
    notes: (value) => value.trim().length > 500 ? "Notes must stay under 500 characters." : ""
  },
  purchaseForm: {
    cropType: (value) => !state.catalog.cropTypes.includes(value) ? "Select a crop type." : "",
    quantityKg: (value) => Number(value) <= 0 ? "Required quantity must be greater than zero." : "",
    unit: (value) => !state.catalog.units.includes(value) ? "Select a supported unit." : "",
    maxBudget: (value) => Number(value) <= 0 ? "Budget must be greater than zero." : "",
    location: (value) => value.trim().length < 2 ? "Enter the request location." : "",
    notes: (value) => value.trim().length > 500 ? "Notes must stay under 500 characters." : ""
  },
  profileForm: {
    fullName: (value) => value.trim().length < 2 ? "Enter your full name." : "",
    role: (value) => !DEFAULT_ROLES.includes(value) ? "Choose a marketplace role." : "",
    city: (value) => value.trim().length < 2 ? "Enter your location." : "",
    phone: (value) => !/^[0-9+()\-\s]{7,20}$/.test(value.trim()) ? "Enter a valid phone number." : ""
  }
};

window.addEventListener("popstate", () => {
  navigate(window.location.pathname, { history: false, replace: true }).catch(handleApiError);
});

window.addEventListener("resize", () => {
  syncResponsiveLayout();
  render();
});

document.addEventListener("keydown", (event) => {
  if (event.key !== "Escape") {
    return;
  }
  state.ui.drawerOpen = false;
  state.ui.profileMenuOpen = false;
  state.ui.modal = null;
  render();
});

document.addEventListener("click", (event) => {
  const routeTarget = event.target.closest("[data-route]");
  if (routeTarget) {
    event.preventDefault();
    state.ui.profileMenuOpen = false;
    state.ui.modal = null;
    if (!isDesktopViewport()) {
      state.ui.drawerOpen = false;
    }
    navigate(routeTarget.dataset.route).catch(handleApiError);
    return;
  }

  const drawerToggle = event.target.closest("[data-drawer-toggle]");
  if (drawerToggle) {
    state.ui.profileMenuOpen = false;
    if (isDesktopViewport() && state.ui.navDocked) {
      state.ui.navDocked = false;
      persistNavMode(false);
      state.ui.drawerOpen = true;
    } else {
      state.ui.drawerOpen = !state.ui.drawerOpen;
    }
    render();
    return;
  }

  const drawerCloser = event.target.closest("[data-close-drawer]");
  if (drawerCloser) {
    state.ui.drawerOpen = false;
    render();
    return;
  }

  const navModeToggle = event.target.closest("[data-nav-mode]");
  if (navModeToggle) {
    const mode = navModeToggle.dataset.navMode;
    state.ui.navDocked = mode === "dock";
    state.ui.drawerOpen = false;
    persistNavMode(state.ui.navDocked);
    render();
    return;
  }

  const profileToggle = event.target.closest("[data-profile-toggle]");
  if (profileToggle) {
    state.ui.profileMenuOpen = !state.ui.profileMenuOpen;
    render();
    return;
  }

  const logoutTarget = event.target.closest("[data-logout]");
  if (logoutTarget) {
    handleLogout().catch(handleApiError);
    return;
  }

  const modalTarget = event.target.closest("[data-open-modal]");
  if (modalTarget?.dataset.openModal === "activities") {
    openActivitiesModal().catch(handleApiError);
    return;
  }

  const closeModal = event.target.closest("[data-close-modal]");
  if (closeModal) {
    state.ui.modal = null;
    render();
    return;
  }

  if (state.ui.profileMenuOpen && !event.target.closest(".profile-menu")) {
    state.ui.profileMenuOpen = false;
    render();
  }
});

document.addEventListener("submit", (event) => {
  const form = event.target;
  if (!(form instanceof HTMLFormElement)) {
    return;
  }

  const formId = form.id;
  if (!formId) {
    return;
  }

  event.preventDefault();

  if (formId === "loginForm") {
    submitAuthForm(form, "/api/auth/login").catch(handleApiError);
    return;
  }
  if (formId === "signupForm") {
    submitAuthForm(form, "/api/auth/signup").catch(handleApiError);
    return;
  }
  if (formId === "cropForm") {
    submitDataForm(form, "/api/crops", "Crop listing created successfully.", async () => {
      await loadRouteData("/add-crops");
    }).catch(handleApiError);
    return;
  }
  if (formId === "purchaseForm") {
    submitDataForm(form, "/api/purchases", "Purchase request created successfully.", async () => {
      await loadRouteData("/buy-crops");
    }).catch(handleApiError);
    return;
  }
  if (formId === "profileForm") {
    submitDataForm(form, "/api/profile", "Profile updated successfully.", async (payload) => {
      state.user = payload.user;
      state.profile = payload.user;
    }).catch(handleApiError);
    return;
  }
  if (formId === "demandFilterForm") {
    state.filters.addCrops = normalizeFilterData(new FormData(form), "demand");
    loadRouteData("/add-crops").then(render).catch(handleApiError);
    return;
  }
  if (formId === "cropFilterForm") {
    state.filters.buyCrops = normalizeFilterData(new FormData(form), "crops");
    loadRouteData("/buy-crops").then(render).catch(handleApiError);
  }
});

document.addEventListener("input", (event) => {
  const target = event.target;
  if (!(target instanceof HTMLInputElement || target instanceof HTMLTextAreaElement || target instanceof HTMLSelectElement)) {
    return;
  }
  const form = target.form;
  if (!form || !form.id || !formSchemas[form.id]) {
    return;
  }
  validateField(form, target.name);
});

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initializeApp);
} else {
  initializeApp();
}

async function initializeApp() {
  syncResponsiveLayout(true);
  render();
  await refreshSession();
  await navigate(window.location.pathname, { history: false, replace: true });
}

async function refreshSession() {
  try {
    const payload = await apiRequest("/api/auth/session", { suppressAuthRedirect: true });
    state.user = payload.user;
  } catch {
    state.user = null;
  }
}

async function navigate(nextRoute, options = {}) {
  const route = resolveRoute(nextRoute);
  syncHistory(route, options);
  state.route = route;
  state.ui.profileMenuOpen = false;
  state.ui.modal = null;
  if (!isDesktopViewport()) {
    state.ui.drawerOpen = false;
  }
  state.loading.route = AUTH_ROUTES.has(route);
  render();
  await loadRouteData(route);
  state.loading.route = false;
  render();
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
      window.history.replaceState({}, "", route);
    }
    return;
  }

  if (replace || window.location.pathname === route) {
    window.history.replaceState({}, "", route);
    return;
  }

  window.history.pushState({}, "", route);
}

async function loadRouteData(route) {
  if (!state.user || !AUTH_ROUTES.has(route)) {
    return;
  }

  if (route === "/dashboard") {
    const payload = await apiRequest("/api/dashboard");
    state.dashboard = payload;
    state.user = payload.user;
    state.catalog = {
      cropTypes: payload.catalog?.cropTypes || DEFAULT_CROP_TYPES,
      units: payload.catalog?.units || DEFAULT_UNITS,
      statuses: payload.catalog?.statuses || DEFAULT_STATUSES,
      roles: DEFAULT_ROLES
    };
    return;
  }

  if (route === "/add-crops") {
    const filters = state.filters.addCrops;
    const [myCropResponse, demandResponse] = await Promise.all([
      apiRequest("/api/crops?scope=mine&status=active"),
      apiRequest(`/api/purchases?scope=all&crop=${encodeURIComponent(filters.crop)}&city=${encodeURIComponent(filters.city)}&minQuantity=${encodeURIComponent(filters.minQuantity)}&maxBudget=${encodeURIComponent(filters.maxBudget)}&status=${encodeURIComponent(filters.status)}`)
    ]);
    state.myCrops = myCropResponse.crops;
    state.marketPurchases = demandResponse.purchases;
    return;
  }

  if (route === "/buy-crops") {
    const filters = state.filters.buyCrops;
    const [marketCropResponse, myPurchaseResponse] = await Promise.all([
      apiRequest(`/api/crops?scope=all&crop=${encodeURIComponent(filters.crop)}&city=${encodeURIComponent(filters.city)}&minQuantity=${encodeURIComponent(filters.minQuantity)}&maxPrice=${encodeURIComponent(filters.maxPrice)}&status=${encodeURIComponent(filters.status)}`),
      apiRequest("/api/purchases?scope=mine&status=active")
    ]);
    state.marketCrops = marketCropResponse.crops;
    state.myPurchases = myPurchaseResponse.purchases;
    return;
  }

  if (route === "/profile") {
    const payload = await apiRequest("/api/profile");
    state.profile = payload.user;
    state.user = payload.user;
  }
}

async function submitAuthForm(form, endpoint) {
  if (!validateForm(form)) {
    return;
  }

  const submitButton = setFormSubmitting(form, true);
  try {
    const payload = await apiRequest(endpoint, {
      method: "POST",
      data: formToObject(form)
    });
    state.user = payload.user;
    showToast(payload.message || "Authentication complete.");
    await navigate("/dashboard", { replace: true });
  } finally {
    setFormSubmitting(form, false, submitButton);
  }
}

async function submitDataForm(form, endpoint, successMessage, afterSubmit) {
  if (!validateForm(form)) {
    return;
  }

  const submitButton = setFormSubmitting(form, true);
  try {
    const payload = await apiRequest(endpoint, {
      method: "POST",
      data: formToObject(form)
    });
    if (typeof afterSubmit === "function") {
      await afterSubmit(payload);
    }
    if (endpoint !== "/api/profile") {
      form.reset();
    }
    showToast(payload.message || successMessage);
    state.loading.route = false;
    render();
  } finally {
    setFormSubmitting(form, false, submitButton);
  }
}

async function handleLogout() {
  await apiRequest("/api/auth/logout", { method: "POST", suppressAuthRedirect: true });
  resetAuthenticatedState();
  showToast("Logged out successfully.", "neutral");
  await navigate("/login", { replace: true });
}

async function openActivitiesModal() {
  const payload = await apiRequest("/api/activities?limit=24");
  state.activities = payload.activities || [];
  state.ui.modal = {
    type: "activities",
    title: "All Recent Activities"
  };
  render();
}

function validateForm(form) {
  const schema = formSchemas[form.id];
  if (!schema) {
    return true;
  }
  const values = formToObject(form);
  let isValid = true;
  Object.keys(schema).forEach((name) => {
    const message = schema[name](values[name] || "", values);
    setFieldError(form, name, message);
    if (message) {
      isValid = false;
    }
  });
  return isValid;
}

function validateField(form, fieldName) {
  const schema = formSchemas[form.id];
  if (!schema || !schema[fieldName]) {
    return true;
  }
  const values = formToObject(form);
  const message = schema[fieldName](values[fieldName] || "", values);
  setFieldError(form, fieldName, message);
  return !message;
}

function setFieldError(form, fieldName, message) {
  const field = form.elements.namedItem(fieldName);
  const errorNode = form.querySelector(`[data-error-for="${fieldName}"]`);
  if (!field || !errorNode) {
    return;
  }
  errorNode.textContent = message || "";
  field.setAttribute("aria-invalid", message ? "true" : "false");
}

function setFormSubmitting(form, isSubmitting, previousState = null) {
  const button = form.querySelector("[type='submit']");
  if (!button) {
    return null;
  }
  if (isSubmitting) {
    const snapshot = {
      text: button.textContent,
      disabled: button.disabled
    };
    button.disabled = true;
    button.textContent = "Working...";
    return snapshot;
  }
  button.disabled = previousState?.disabled ?? false;
  button.textContent = previousState?.text || button.dataset.submitLabel || button.textContent;
  return null;
}

function formToObject(form) {
  return Object.fromEntries(new FormData(form).entries());
}

function normalizeFilterData(formData, kind) {
  const values = Object.fromEntries(formData.entries());
  return {
    crop: values.crop || "",
    city: values.city || "",
    minQuantity: values.minQuantity || "",
    maxBudget: kind === "demand" ? values.maxBudget || "" : undefined,
    maxPrice: kind === "crops" ? values.maxPrice || "" : undefined,
    status: values.status || "active"
  };
}

function resetAuthenticatedState() {
  state.user = null;
  state.dashboard = null;
  state.profile = null;
  state.myCrops = [];
  state.marketCrops = [];
  state.myPurchases = [];
  state.marketPurchases = [];
  state.activities = [];
  state.ui.drawerOpen = false;
  state.ui.profileMenuOpen = false;
  state.ui.modal = null;
}

function syncResponsiveLayout(initial = false) {
  if (!isDesktopViewport()) {
    state.ui.navDocked = false;
    state.ui.drawerOpen = false;
    return;
  }

  if (initial) {
    const storedValue = window.localStorage.getItem(navModeStorageKey);
    state.ui.navDocked = storedValue === null ? true : storedValue === "true";
    return;
  }

  if (window.innerWidth >= BREAKPOINTS.lg && !state.ui.drawerOpen) {
    const storedValue = window.localStorage.getItem(navModeStorageKey);
    state.ui.navDocked = storedValue === null ? true : storedValue === "true";
  }
}

function persistNavMode(value) {
  window.localStorage.setItem(navModeStorageKey, String(value));
}

function showToast(message, tone = "success") {
  const toast = {
    id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    message,
    tone
  };
  state.ui.toasts = [...state.ui.toasts.slice(-2), toast];
  render();
  window.setTimeout(() => {
    state.ui.toasts = state.ui.toasts.filter((item) => item.id !== toast.id);
    render();
  }, 3200);
}

function handleApiError(error) {
  if (error?.status === 401 && !error?.suppressAuthRedirect) {
    resetAuthenticatedState();
    navigate("/login", { replace: true }).catch(() => {});
    showToast(error.message || "Please log in to continue.", "neutral");
    return;
  }
  showToast(error?.message || "Something went wrong.", "danger");
}

function render() {
  appRoot.innerHTML = renderAppView(state);
}
