import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate } from 'k6/metrics';

const COUPON_ID = 4001;
const BASE_URL = 'http://localhost:8080';

export const createdCount = new Counter('coupon_201_count');
export const conflictCount = new Counter('coupon_409_count');
export const otherCount = new Counter('coupon_other_count');

export const createdRate = new Rate('coupon_201_rate');
export const conflictRate = new Rate('coupon_409_rate');
export const otherRate = new Rate('coupon_other_rate');

export const options = {
  stages: [
    { duration: '1s', target: 10 },
    { duration: '2s', target: 500 },
    { duration: '3s', target: 1500 },
    { duration: '3s', target: 1000 },
    { duration: '1s', target: 10 }
  ],
  thresholds: {
    http_req_duration: [
      {
        threshold: 'p(95)<500',
        abortOnFail: false
      }
    ]
  }
};

export default function () {
  const customerId = 1001 + Math.floor(Math.random() * 2000);

  const res = http.post(`${BASE_URL}/coupons/issue`, JSON.stringify({
    couponId: COUPON_ID,
    customerId
  }), {
    headers: { 'Content-Type': 'application/json' }
  });

  const status = res.status;

  createdRate.add(status === 201);
  conflictRate.add(status === 409);
  otherRate.add(status !== 201 && status !== 409);

  if (status === 201) {
    createdCount.add(1);
  } else if (status === 409) {
    conflictCount.add(1);
  } else {
    otherCount.add(1);
    console.warn(`🚨 Unexpected response: status=${status}, body=${res.body}`);
  }

  check(res, {
    'is 201 or 409': (r) => r.status === 201 || r.status === 409
  });
}
