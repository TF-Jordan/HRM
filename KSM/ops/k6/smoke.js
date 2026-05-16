import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

const baseUrl = __ENV.IWM_BASE_URL || 'http://localhost:8080';
const managementUrl = __ENV.IWM_MANAGEMENT_URL || 'http://localhost:8081';
const clientId = __ENV.IWM_BOOTSTRAP_CLIENT_ID || 'dev-platform-backend';
const clientSecret = __ENV.IWM_BOOTSTRAP_CLIENT_SECRET || 'dev-api-key';
const managementApiKey = __ENV.IWM_MANAGEMENT_API_KEY || 'dev-management-key';

export default function () {
  const health = http.get(`${managementUrl}/actuator/health`);
  check(health, {
    'management health is 200': (r) => r.status === 200,
  });

  const ops = http.get(`${managementUrl}/actuator/health/operations`, {
    headers: { 'X-Management-Api-Key': managementApiKey },
  });
  check(ops, {
    'operations health is 200': (r) => r.status === 200,
  });

  const actorPayload = JSON.stringify({
    email: `k6-smoke-${Date.now()}@example.com`,
    firstName: 'K6',
    lastName: 'Smoke',
  });

  const actor = http.post(`${baseUrl}/api/actors`, actorPayload, {
    headers: {
      'Content-Type': 'application/json',
      'X-Client-Id': clientId,
      'X-Api-Key': clientSecret,
      'X-Tenant-Id': '00000000-0000-0000-0000-000000000001',
    },
  });

  check(actor, {
    'actor created or rejected by business rule only': (r) => r.status === 201 || r.status === 409,
  });

  sleep(1);
}
