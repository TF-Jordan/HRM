import http from 'k6/http';
import { check, sleep } from 'k6';

const stage1Duration = __ENV.IWM_K6_STAGE1_DURATION || '30s';
const stage1Target = Number(__ENV.IWM_K6_STAGE1_TARGET || 20);
const stage2Duration = __ENV.IWM_K6_STAGE2_DURATION || '60s';
const stage2Target = Number(__ENV.IWM_K6_STAGE2_TARGET || 50);
const stage3Duration = __ENV.IWM_K6_STAGE3_DURATION || '30s';
const stage3Target = Number(__ENV.IWM_K6_STAGE3_TARGET || 0);

export const options = {
  stages: [
    { duration: stage1Duration, target: stage1Target },
    { duration: stage2Duration, target: stage2Target },
    { duration: stage3Duration, target: stage3Target },
  ],
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<1500', 'p(99)<2500'],
  },
};

const baseUrl = __ENV.IWM_BASE_URL || 'http://localhost:8080';
const managementUrl = __ENV.IWM_MANAGEMENT_URL || 'http://localhost:8081';
const clientId = __ENV.IWM_BOOTSTRAP_CLIENT_ID || 'dev-platform-backend';
const clientSecret = __ENV.IWM_BOOTSTRAP_CLIENT_SECRET || 'dev-api-key';
const managementApiKey = __ENV.IWM_MANAGEMENT_API_KEY || 'dev-management-key';

export default function () {
  if (__ITER % 10 === 0) {
    const metrics = http.get(`${managementUrl}/actuator/prometheus`, {
      headers: { 'X-Management-Api-Key': managementApiKey },
    });
    check(metrics, {
      'prometheus endpoint is available': (r) => r.status === 200,
    });
  }

  const actorPayload = JSON.stringify({
    email: `k6-load-${__VU}-${__ITER}-${Date.now()}@example.com`,
    firstName: 'K6',
    lastName: 'Load',
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
    'actor request accepted': (r) => r.status === 201 || r.status === 409,
  });

  sleep(1);
}
