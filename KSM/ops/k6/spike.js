import http from 'k6/http';
import { check, sleep } from 'k6';

const baselineDuration = __ENV.IWM_K6_SPIKE_BASELINE_DURATION || '20s';
const baselineTarget = Number(__ENV.IWM_K6_SPIKE_BASELINE_TARGET || 10);
const spikeDuration = __ENV.IWM_K6_SPIKE_DURATION || '20s';
const spikeTarget = Number(__ENV.IWM_K6_SPIKE_TARGET || 120);
const recoveryDuration = __ENV.IWM_K6_SPIKE_RECOVERY_DURATION || '40s';
const recoveryTarget = Number(__ENV.IWM_K6_SPIKE_RECOVERY_TARGET || 10);
const cooldownDuration = __ENV.IWM_K6_SPIKE_COOLDOWN_DURATION || '20s';

export const options = {
  stages: [
    { duration: baselineDuration, target: baselineTarget },
    { duration: spikeDuration, target: spikeTarget },
    { duration: recoveryDuration, target: recoveryTarget },
    { duration: cooldownDuration, target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<2500', 'p(99)<4000'],
  },
};

const baseUrl = __ENV.IWM_BASE_URL || 'http://localhost:8080';
const clientId = __ENV.IWM_BOOTSTRAP_CLIENT_ID || 'dev-platform-backend';
const clientSecret = __ENV.IWM_BOOTSTRAP_CLIENT_SECRET || 'dev-api-key';
const tenantId = __ENV.IWM_K6_TENANT_ID || '00000000-0000-0000-0000-000000000001';

export default function () {
  const actorPayload = JSON.stringify({
    email: `k6-spike-${__VU}-${__ITER}-${Date.now()}@example.com`,
    firstName: 'K6',
    lastName: 'Spike',
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

  sleep(0.3);
}
