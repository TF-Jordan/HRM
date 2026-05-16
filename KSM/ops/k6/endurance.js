import http from 'k6/http';
import { check, sleep } from 'k6';

const targetVus = Number(__ENV.IWM_K6_ENDURANCE_VUS || 20);
const duration = __ENV.IWM_K6_ENDURANCE_DURATION || '15m';

export const options = {
  scenarios: {
    endurance: {
      executor: 'constant-vus',
      vus: targetVus,
      duration,
      gracefulStop: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1200', 'p(99)<2000'],
  },
};

const baseUrl = __ENV.IWM_BASE_URL || 'http://localhost:8080';
const managementUrl = __ENV.IWM_MANAGEMENT_URL || 'http://localhost:8081';
const clientId = __ENV.IWM_BOOTSTRAP_CLIENT_ID || 'dev-platform-backend';
const clientSecret = __ENV.IWM_BOOTSTRAP_CLIENT_SECRET || 'dev-api-key';
const managementApiKey = __ENV.IWM_MANAGEMENT_API_KEY || 'dev-management-key';
const tenantId = __ENV.IWM_K6_TENANT_ID || '00000000-0000-0000-0000-000000000001';

export default function () {
  if (__ITER % 25 === 0) {
    const metrics = http.get(`${managementUrl}/actuator/prometheus`, {
      headers: { 'X-Management-Api-Key': managementApiKey },
    });
    check(metrics, {
      'prometheus endpoint is available': (r) => r.status === 200,
    });
  }

  const actorPayload = JSON.stringify({
    email: `k6-endurance-${__VU}-${__ITER}-${Date.now()}@example.com`,
    firstName: 'K6',
    lastName: 'Endurance',
  });

  const actor = http.post(`${baseUrl}/api/actors`, actorPayload, {
    headers: {
      'Content-Type': 'application/json',
      'X-Client-Id': clientId,
      'X-Api-Key': clientSecret,
      'X-Tenant-Id': tenantId,
    },
  });
  check(actor, {
    'actor request accepted': (r) => r.status === 201 || r.status === 409,
  });

  sleep(1);
}
