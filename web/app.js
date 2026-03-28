const CROP_TYPES = ["All", "Wheat", "Rice", "Cotton", "Sugarcane", "Maize", "Tomato", "Potato", "Onion"];

const farmerForm = document.getElementById("farmerForm");
const buyerForm = document.getElementById("buyerForm");
const farmerSearchForm = document.getElementById("farmerSearchForm");
const buyerSearchForm = document.getElementById("buyerSearchForm");
const buyerMatchSelect = document.getElementById("buyerMatchSelect");
const reportArea = document.getElementById("reportArea");
const toast = document.getElementById("toast");

document.addEventListener("DOMContentLoaded", async () => {
  fillCropSelects();
  bindEvents();
  await refreshAll();
});

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
    await loadFarmers();
  });

  document.getElementById("resetBuyerSearch").addEventListener("click", async () => {
    buyerSearchForm.reset();
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
  fillSelect(farmerForm.querySelector("select[name='cropType']"), CROP_TYPES.filter((crop) => crop !== "All"));
  fillSelect(buyerForm.querySelector("select[name='requiredCrop']"), CROP_TYPES.filter((crop) => crop !== "All"));
  fillSelect(farmerSearchForm.querySelector("select[name='crop']"), CROP_TYPES);
  fillSelect(buyerSearchForm.querySelector("select[name='crop']"), CROP_TYPES);
}

function fillSelect(select, options) {
  select.innerHTML = "";
  options.forEach((option) => {
    const element = document.createElement("option");
    element.value = option === "All" ? "" : option;
    element.textContent = option;
    select.appendChild(element);
  });
}

async function refreshAll() {
  await Promise.all([loadFarmers(), loadBuyers(), loadSummary()]);
  clearMatchTable();
}

async function submitForm(url, form) {
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
    body: new URLSearchParams(new FormData(form))
  });
  const data = await response.json();
  if (!response.ok) {
    showToast(data.error || "Request failed.");
    return;
  }
  form.reset();
  showToast(data.message || "Saved.");
  await refreshAll();
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
