import http from 'k6/http';
import { check, sleep } from 'k6';

const stage1Duration = __ENV.IWM_K6_SATURATION_STAGE1_DURATION || '30s';
const stage1Target = Number(__ENV.IWM_K6_SATURATION_STAGE1_TARGET || 25);
const stage2Duration = __ENV.IWM_K6_SATURATION_STAGE2_DURATION || '30s';
const stage2Target = Number(__ENV.IWM_K6_SATURATION_STAGE2_TARGET || 75);
const stage3Duration = __ENV.IWM_K6_SATURATION_STAGE3_DURATION || '30s';
const stage3Target = Number(__ENV.IWM_K6_SATURATION_STAGE3_TARGET || 150);
const stage4Duration = __ENV.IWM_K6_SATURATION_STAGE4_DURATION || '30s';
const stage4Target = Number(__ENV.IWM_K6_SATURATION_STAGE4_TARGET || 0);

export const options = {
  stages: [
    { duration: stage1Duration, target: stage1Target },
    { duration: stage2Duration, target: stage2Target },
    { duration: stage3Duration, target: stage3Target },
    { duration: stage4Duration, target: stage4Target },
  ],
  thresholds: {
    http_req_failed: ['rate<0.10'],
    http_req_duration: ['p(95)<3500', 'p(99)<6000'],
  },
};

const baseUrl = __ENV.IWM_BASE_URL || 'http://localhost:8080';
const clientId = __ENV.IWM_BOOTSTRAP_CLIENT_ID || 'dev-platform-backend';
const clientSecret = __ENV.IWM_BOOTSTRAP_CLIENT_SECRET || 'dev-api-key';
const tenantId = __ENV.IWM_K6_TENANT_ID || '00000000-0000-0000-0000-000000000001';

export default function () {
  const actorPayload = JSON.stringify({
    email: `k6-saturation-${__VU}-${__ITER}-${Date.now()}@example.com`,
    firstName: 'K6',
    lastName: 'Saturation',
  });

  const actor = http.post(`${baseUrl}/api/actors`, actorPayload, {
    headers: {
      'Content-Type': 'application/json',
      'X-Client-Id': clientId,
      'X-Api-Key': clientSecret,
      'X-Tenant-Id': tenantId,
    },
    responseCallback: http.expectedStatuses(201, 409, 429),
  });
  check(actor, {
    'actor request accepted': (r) => r.status === 201 || r.status === 409 || r.status === 429,
  });

  sleep(0.2);
}
