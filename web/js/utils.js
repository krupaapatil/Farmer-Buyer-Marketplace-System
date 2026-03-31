export function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}

export function formatNumber(value) {
  return new Intl.NumberFormat("en-IN", {
    maximumFractionDigits: 2
  }).format(Number(value || 0));
}

export function formatCurrency(value) {
  return `Rs. ${formatNumber(value)}`;
}

export function formatDate(value) {
  if (!value) {
    return "Not available";
  }
  return new Date(value).toLocaleString("en-IN", {
    dateStyle: "medium",
    timeStyle: "short"
  });
}

export function formatRelativeTime(value) {
  if (!value) {
    return "Just now";
  }

  const target = new Date(value).getTime();
  const diffMs = target - Date.now();
  const absSeconds = Math.round(Math.abs(diffMs) / 1000);
  const units = [
    ["year", 60 * 60 * 24 * 365],
    ["month", 60 * 60 * 24 * 30],
    ["day", 60 * 60 * 24],
    ["hour", 60 * 60],
    ["minute", 60],
    ["second", 1]
  ];
  const formatter = new Intl.RelativeTimeFormat("en", { numeric: "auto" });

  for (const [unit, seconds] of units) {
    if (absSeconds >= seconds || unit === "second") {
      return formatter.format(Math.round(diffMs / 1000 / seconds), unit);
    }
  }

  return "Just now";
}

export function sentenceCase(value) {
  if (!value) {
    return "";
  }
  return String(value).charAt(0).toUpperCase() + String(value).slice(1);
}

export function slugify(value) {
  return String(value ?? "")
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

export function initialsFromName(value) {
  const parts = String(value ?? "").trim().split(/\s+/).filter(Boolean);
  if (!parts.length) {
    return "U";
  }
  return parts.slice(0, 2).map((part) => part[0].toUpperCase()).join("");
}

export function isDesktopViewport() {
  return window.innerWidth >= 1024;
}

export function clamp(value, min, max) {
  return Math.min(max, Math.max(min, value));
}
