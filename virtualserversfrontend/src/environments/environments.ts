export const environment = {
  production: false,
  apiBaseUrl: getDevApiBaseUrl(),
};

function getDevApiBaseUrl(): string {
  if (typeof window === 'undefined') return 'http://localhost:8080';
  const host = window.location.hostname;
  return host === 'localhost' ? 'http://localhost:8080' : `http://${host}:8080`;
}