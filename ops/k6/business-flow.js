import http from 'k6/http';
import { check, group, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://backend:8081';
const PASSWORD = __ENV.TEST_PASSWORD || 'Test123456';
const PAY_PASSWORD = __ENV.PAY_PASSWORD || '123456';

function envDuration(name, fallback) {
  return __ENV[name] || fallback;
}

function envInt(name, fallback) {
  const value = Number.parseInt(__ENV[name] || '', 10);
  return Number.isFinite(value) ? value : fallback;
}

export const options = {
  stages: [
    { duration: envDuration('STAGE_1_DURATION', '15s'), target: envInt('STAGE_1_TARGET', 1) },
    { duration: envDuration('STAGE_2_DURATION', '30s'), target: envInt('STAGE_2_TARGET', 3) },
    { duration: envDuration('STAGE_3_DURATION', '15s'), target: envInt('STAGE_3_TARGET', 0) },
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<800', 'p(99)<1500'],
    checks: ['rate>0.95'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

function unwrap(response, step) {
  const ok = check(response, {
    [`${step}: http 2xx`]: (r) => r.status >= 200 && r.status < 300,
    [`${step}: json body`]: (r) => {
      try {
        r.json();
        return true;
      } catch {
        return false;
      }
    },
  });

  if (!ok) {
    throw new Error(`${step} failed: status=${response.status}, body=${response.body}`);
  }

  const body = response.json();
  if (body.code !== undefined && body.code !== 0 && body.code !== 200) {
    throw new Error(`${step} failed: appCode=${body.code}, message=${body.message}`);
  }

  return body.data;
}

function jsonParams(token, name) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return { headers, tags: { name } };
}

function post(path, body, token, name) {
  return http.post(`${BASE_URL}${path}`, JSON.stringify(body), jsonParams(token, name));
}

function put(path, body, token, name) {
  return http.put(`${BASE_URL}${path}`, JSON.stringify(body), jsonParams(token, name));
}

function get(path, token, name) {
  return http.get(`${BASE_URL}${path}`, jsonParams(token, name));
}

export default function () {
  const suffix = `${Date.now()}_${__VU}_${__ITER}`;
  const seller = `k6_seller_${suffix}`;
  const buyer = `k6_buyer_${suffix}`;

  let sellerToken;
  let buyerToken;
  let addressId;
  let productId;
  let orderId;

  group('auth', () => {
    unwrap(post('/api/auth/register', {
      username: seller,
      email: `${seller}@example.com`,
      password: PASSWORD,
    }, null, 'POST /api/auth/register'), 'register seller');

    unwrap(post('/api/auth/register', {
      username: buyer,
      email: `${buyer}@example.com`,
      password: PASSWORD,
    }, null, 'POST /api/auth/register'), 'register buyer');

    sellerToken = unwrap(post('/api/auth/login', {
      username: seller,
      password: PASSWORD,
    }, null, 'POST /api/auth/login'), 'login seller').token;

    buyerToken = unwrap(post('/api/auth/login', {
      username: buyer,
      password: PASSWORD,
    }, null, 'POST /api/auth/login'), 'login buyer').token;
  });

  group('catalog', () => {
    addressId = unwrap(post('/api/addresses', {
      recipientName: 'K6 Buyer',
      phone: '13800000000',
      province: 'Shanghai',
      city: 'Shanghai',
      district: 'Pudong',
      detailedAddress: 'K6 Test Road 1',
      isDefault: true,
      areaCode: '310115',
    }, buyerToken, 'POST /api/addresses'), 'create address').id;

    productId = unwrap(post('/api/products', {
      title: `K6 Test Product ${suffix}`,
      description: 'Created by k6 business-flow load test',
      price: '88.88',
      originalPrice: '199.00',
      currency: 'CNY',
      condition: 'LIKE_NEW',
      sellerClaimCondition: 'LIKE_NEW',
      locationText: 'Shanghai',
      shippingPayer: 'SELLER',
      tradeTypes: 'EXPRESS',
      tradeModel: 'FREE_TRADING',
      imageKeys: [],
    }, sellerToken, 'POST /api/products'), 'create product').id;
  });

  group('wallet_order', () => {
    unwrap(post('/api/wallet/set-password', {
      password: PAY_PASSWORD,
    }, buyerToken, 'POST /api/wallet/set-password'), 'set wallet password');

    unwrap(post('/api/wallet/top-up', {
      amount: '500.00',
      bizNo: `TOPUP-K6-${suffix}`,
    }, buyerToken, 'POST /api/wallet/top-up'), 'top up wallet');

    const walletBefore = unwrap(get('/api/wallet', buyerToken, 'GET /api/wallet'), 'wallet before');
    check(walletBefore, {
      'wallet balance before pay is enough': (w) => Number(w.balance) >= 88.88,
    });

    orderId = unwrap(post('/api/orders', {
      productId,
      addressId,
    }, buyerToken, 'POST /api/orders'), 'create order').id;

    const paidOrder = unwrap(put(`/api/orders/${orderId}/pay-wallet`, {
      password: PAY_PASSWORD,
    }, buyerToken, 'PUT /api/orders/{id}/pay-wallet'), 'pay wallet');

    check(paidOrder, {
      'order is paid': (o) => o.status === 'PAID',
    });
  });

  sleep(1);
}
