const CROP_TYPES = ["Wheat", "Rice", "Cotton", "Sugarcane", "Maize", "Tomato", "Potato", "Onion"];
const WEIGHT_UNITS = [
  { value: "kg", label: "Kilograms (kg)", factor: 1 },
  { value: "quintal", label: "Quintals (100 kg)", factor: 100 },
  { value: "ton", label: "Metric Tons (1000 kg)", factor: 1000 }
];

const farmerForm = document.getElementById("farmerForm");
const buyerForm = document.getElementById("buyerForm");
const farmerSearchForm = document.getElementById("farmerSearchForm");
const buyerSearchForm = document.getElementById("buyerSearchForm");
const buyerMatchSelect = document.getElementById("buyerMatchSelect");
const reportArea = document.getElementById("reportArea");
const toast = document.getElementById("toast");

async function initializeApp() {
  fillCropSelects();
  fillWeightSelects();
  bindEvents();
  setEntryFormDefaults();
  await refreshAll();
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initializeApp);
} else {
  initializeApp();
}

function bindEvents() {
  farmerForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await submitForm("/api/farmers", farmerForm);
  });

  buyerForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await submitForm("/api/buyers", buyerForm);
  });

  farmerSearchForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await loadFarmers(new URLSearchParams(new FormData(farmerSearchForm)));
  });

  buyerSearchForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await loadBuyers(new URLSearchParams(new FormData(buyerSearchForm)));
  });

  document.getElementById("resetFarmerSearch").addEventListener("click", async () => {
    farmerSearchForm.reset();
    farmerSearchForm.querySelector("select[name='crop']").value = "";
    await loadFarmers();
  });

  document.getElementById("resetBuyerSearch").addEventListener("click", async () => {
    buyerSearchForm.reset();
    buyerSearchForm.querySelector("select[name='crop']").value = "";
    await loadBuyers();
  });

  document.getElementById("matchSelectedButton").addEventListener("click", async () => {
    if (!buyerMatchSelect.value) {
      showToast("Select a buyer first.");
      return;
    }
    await loadMatches(`/api/matches?buyerId=${encodeURIComponent(buyerMatchSelect.value)}`);
  });

  document.getElementById("matchAllButton").addEventListener("click", async () => {
    await loadMatches("/api/matches");
  });

  document.getElementById("saveButton").addEventListener("click", async () => {
    await postAction("/api/files/save");
  });

  document.getElementById("loadButton").addEventListener("click", async () => {
    await postAction("/api/files/load");
    await refreshAll();
  });
}

function fillCropSelects() {
  fillSelect(farmerForm.querySelector("select[name='cropType']"), CROP_TYPES, {
    placeholder: "Select crop type"
  });
  fillSelect(buyerForm.querySelector("select[name='requiredCrop']"), CROP_TYPES, {
    placeholder: "Select crop type"
  });
  fillSelect(farmerSearchForm.querySelector("select[name='crop']"), [
    { value: "", label: "All crop types" },
    ...CROP_TYPES.map((crop) => ({ value: crop, label: crop }))
  ]);
  fillSelect(buyerSearchForm.querySelector("select[name='crop']"), [
    { value: "", label: "All crop types" },
    ...CROP_TYPES.map((crop) => ({ value: crop, label: crop }))
  ]);
}

function fillWeightSelects() {
  const weightOptions = WEIGHT_UNITS.map((unit) => ({
    value: unit.value,
    label: unit.label
  }));
  fillSelect(farmerForm.querySelector("select[name='quantityAvailableUnit']"), weightOptions);
  fillSelect(buyerForm.querySelector("select[name='requiredQuantityUnit']"), weightOptions);
}

function fillSelect(select, options, config = {}) {
  const { placeholder = "", selectedValue = "" } = config;
  select.innerHTML = "";
  if (placeholder) {
    const placeholderOption = document.createElement("option");
    placeholderOption.value = "";
    placeholderOption.textContent = placeholder;
    placeholderOption.disabled = true;
    placeholderOption.selected = true;
    placeholderOption.defaultSelected = true;
    select.appendChild(placeholderOption);
  }

  options.forEach((option) => {
    const element = document.createElement("option");
    if (typeof option === "string") {
      element.value = option;
      element.textContent = option;
    } else {
      element.value = option.value;
      element.textContent = option.label;
    }
    select.appendChild(element);
  });

  if (selectedValue) {
    select.value = selectedValue;
  }
}

function setEntryFormDefaults() {
  farmerForm.querySelector("select[name='cropType']").value = "";
  farmerForm.querySelector("select[name='quantityAvailableUnit']").value = "kg";
  buyerForm.querySelector("select[name='requiredCrop']").value = "";
  buyerForm.querySelector("select[name='requiredQuantityUnit']").value = "kg";
}

async function refreshAll() {
  await Promise.all([loadFarmers(), loadBuyers(), loadSummary()]);
  clearMatchTable();
}

async function submitForm(url, form) {
  const payload = buildFormPayload(form);
  if (!payload) {
    return;
  }

  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
    body: payload
  });
  const data = await response.json();
  if (!response.ok) {
    showToast(data.error || "Request failed.");
    return;
  }
  form.reset();
  setEntryFormDefaults();
  showToast(data.message || "Saved.");
  await refreshAll();
}

function buildFormPayload(form) {
  const formData = new FormData(form);
  const payload = new URLSearchParams();

  formData.forEach((value, key) => {
    if (!key.endsWith("Display") && !key.endsWith("Unit")) {
      payload.append(key, value);
    }
  });

  if (form === farmerForm) {
    const quantityInKg = normalizeQuantity(
      formData.get("quantityAvailableDisplay"),
      formData.get("quantityAvailableUnit")
    );
    if (quantityInKg === null) {
      return null;
    }
    payload.set("quantityAvailable", String(quantityInKg));
  }

  if (form === buyerForm) {
    const quantityInKg = normalizeQuantity(
      formData.get("requiredQuantityDisplay"),
      formData.get("requiredQuantityUnit")
    );
    if (quantityInKg === null) {
      return null;
    }
    payload.set("requiredQuantity", String(quantityInKg));
  }

  return payload;
}

function normalizeQuantity(rawQuantity, unitValue) {
  const quantity = Number(rawQuantity);
  if (!Number.isFinite(quantity) || quantity < 0) {
    showToast("Enter a valid quantity.");
    return null;
  }

  const unit = WEIGHT_UNITS.find((entry) => entry.value === unitValue);
  if (!unit) {
    showToast("Select a weight unit.");
    return null;
  }

  if (quantity === 0) {
    return 0;
  }

  return Math.ceil(quantity * unit.factor);
}

async function postAction(url) {
  const response = await fetch(url, { method: "POST" });
  const data = await response.json();
  if (!response.ok) {
    showToast(data.error || "Request failed.");
    return;
  }
  showToast(data.message || "Done.");
  await loadSummary();
}

async function loadFarmers(params = new URLSearchParams()) {
  const response = await fetch(`/api/farmers${params.toString() ? `?${params.toString()}` : ""}`);
  const data = await response.json();
  renderRows(document.getElementById("farmersTableBody"), data.farmers, (farmer) => `
    <tr>
      <td>${escapeHtml(farmer.id)}</td>
      <td>${escapeHtml(farmer.name)}</td>
      <td>${escapeHtml(farmer.city)}</td>
      <td>${escapeHtml(farmer.phone)}</td>
      <td>${escapeHtml(farmer.cropType)}</td>
      <td>${farmer.quantityAvailable}</td>
      <td>${Number(farmer.pricePerUnit).toFixed(2)}</td>
    </tr>
  `);
}

async function loadBuyers(params = new URLSearchParams()) {
  const response = await fetch(`/api/buyers${params.toString() ? `?${params.toString()}` : ""}`);
  const data = await response.json();
  renderRows(document.getElementById("buyersTableBody"), data.buyers, (buyer) => `
    <tr>
      <td>${escapeHtml(buyer.id)}</td>
      <td>${escapeHtml(buyer.name)}</td>
      <td>${escapeHtml(buyer.city)}</td>
      <td>${escapeHtml(buyer.phone)}</td>
      <td>${escapeHtml(buyer.requiredCrop)}</td>
      <td>${buyer.requiredQuantity}</td>
      <td>${Number(buyer.maxBudget).toFixed(2)}</td>
    </tr>
  `);

  buyerMatchSelect.innerHTML = '<option value="">Select Buyer</option>';
  data.buyers.forEach((buyer) => {
    const option = document.createElement("option");
    option.value = buyer.id;
    option.textContent = `${buyer.name} (${buyer.id}) - ${buyer.requiredCrop} in ${buyer.city}`;
    buyerMatchSelect.appendChild(option);
  });
}

async function loadMatches(url) {
  const response = await fetch(url);
  const data = await response.json();
  if (!response.ok) {
    showToast(data.error || "No matches found.");
    clearMatchTable();
    return;
  }

  renderRows(document.getElementById("matchesTableBody"), data.matches, (match) => `
    <tr>
      <td>${escapeHtml(match.buyerName)} (${escapeHtml(match.buyerId)})</td>
      <td>${escapeHtml(match.farmerName)} (${escapeHtml(match.farmerId)})</td>
      <td>${escapeHtml(match.cropType)}</td>
      <td>${escapeHtml(match.city)}</td>
      <td>${match.availableQuantity}</td>
      <td>${Number(match.pricePerUnit).toFixed(2)}</td>
      <td>${Number(match.score).toFixed(2)}</td>
      <td>${escapeHtml(match.status)}</td>
    </tr>
  `);

  reportArea.value = data.report || "";
  document.getElementById("matchCount").textContent = data.matches.length;
  showToast(`Generated ${data.matches.length} match${data.matches.length === 1 ? "" : "es"}.`);
}

async function loadSummary() {
  const response = await fetch("/api/reports/summary");
  const data = await response.json();
  document.getElementById("farmerCount").textContent = data.farmers;
  document.getElementById("buyerCount").textContent = data.buyers;
  document.getElementById("matchCount").textContent = data.lastMatches;
  reportArea.value = data.summary || "";
}

function clearMatchTable() {
  document.getElementById("matchesTableBody").innerHTML =
    '<tr><td colspan="8">Generate a match report to see recommendations here.</td></tr>';
}

function renderRows(target, items, renderer) {
  if (!items || items.length === 0) {
    target.innerHTML = '<tr><td colspan="8">No records found.</td></tr>';
    return;
  }
  target.innerHTML = items.map(renderer).join("");
}

function showToast(message) {
  toast.textContent = message;
  toast.hidden = false;
  clearTimeout(showToast.timeoutId);
  showToast.timeoutId = setTimeout(() => {
    toast.hidden = true;
  }, 2800);
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}
