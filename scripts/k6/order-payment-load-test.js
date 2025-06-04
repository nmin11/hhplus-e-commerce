import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';

export const createdCount = new Counter('payment_201_count');
export const conflictCount = new Counter('payment_409_count');
export const otherCount = new Counter('payment_other_count');

export const createdRate = new Rate('payment_201_rate');
export const conflictRate = new Rate('payment_409_rate');
export const otherRate = new Rate('payment_other_rate');

export let options = {
  stages: [
    { duration: '1m', target: 10 },
    { duration: '1m', target: 50 },
    { duration: '1m', target: 100 },
    { duration: '1m', target: 50 },
    { duration: '1m', target: 10 },
  ],
  thresholds: {
    http_req_duration: [
      {
        threshold: 'p(90)<1000',
        abortOnFail: false
      }
    ],
    http_req_failed: [
      {
        threshold: 'rate<0.001',
        abortOnFail: false
      }
    ]
  }
};

export default function () {
  const customerId = 1001 + ((__VU - 1) * 100 + __ITER) % 2000;

  const productId1 = 5001 + Math.floor(Math.random() * 20);
  const productId2 = 5001 + Math.floor(Math.random() * 20);

  const optionS1 = 3 * (productId1 - 5001) + 6001;
  const optionM2 = 3 * (productId2 - 5001) + 6002;

  // Step 1. 주문 생성
  const orderPayload = JSON.stringify({
    customerId,
    items: [
      {
        productId: productId1,
        productOptionId: optionS1,
        quantity: 1
      },
      {
        productId: productId2,
        productOptionId: optionM2,
        quantity: 1
      }
    ]
  });

  const orderRes = http.post(`${BASE_URL}/orders`, orderPayload, {
    headers: { 'Content-Type': 'application/json' }
  });

  check(orderRes, {
    'order created': (r) => r.status === 201,
  });

  if (orderRes.status !== 201) {
    console.warn(`🚨 Order creation failed: status=${orderRes.status}, body=${orderRes.body}`);
    return;
  }

  const orderId = orderRes.json().orderId;
  if (!orderId) {
    console.warn('orderId not found in response');
    return;
  }

  // Step 2. 결제 요청
  const paymentPayload = JSON.stringify({
    orderId
  });

  const paymentRes = http.post(`${BASE_URL}/payments`, paymentPayload, {
    headers: { 'Content-Type': 'application/json' }
  });

  const status = paymentRes.status;

  createdRate.add(status === 201);
  conflictRate.add(status === 409);
  otherRate.add(status !== 201 && status !== 409);

  if (status === 201) createdCount.add(1);
  else if (status === 409) {
    console.warn(`🚨 Payment failed: body=${paymentRes.body}`);
    conflictCount.add(1);
  }
  else otherCount.add(1);

  check(paymentRes, {
    'payment success': (r) => r.status === 201
  });
}
