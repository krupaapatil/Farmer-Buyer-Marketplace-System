export async function apiRequest(path, options = {}) {
  const {
    method = "GET",
    data = null,
    headers = {},
    suppressAuthRedirect = false
  } = options;

  const requestHeaders = { ...headers };
  const requestOptions = {
    method,
    headers: requestHeaders
  };

  if (data) {
    requestHeaders["Content-Type"] = "application/x-www-form-urlencoded;charset=UTF-8";
    requestOptions.body = new URLSearchParams(data).toString();
  }

  const response = await fetch(path, requestOptions);
  const payload = await response.json().catch(() => ({}));

  if (!response.ok) {
    const message = payload?.error?.message || payload?.message || "Request failed.";
    const error = new Error(message);
    error.status = response.status;
    error.code = payload?.error?.code || "REQUEST_ERROR";
    error.payload = payload;
    error.suppressAuthRedirect = suppressAuthRedirect;
    throw error;
  }

  return payload;
}
