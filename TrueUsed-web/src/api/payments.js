import request from '../utils/request'

const ALIPAY_ALLOWED_HOSTS = new Set([
  'openapi.alipay.com',
  'openapi-sandbox.dl.alipaydev.com',
])

function resolvePaymentFormHtml(payload) {
  if (typeof payload === 'string') {
    return payload
  }
  if (payload && typeof payload === 'object') {
    if (typeof payload.htmlForm === 'string') {
      return payload.htmlForm
    }
    if (payload.data && typeof payload.data.htmlForm === 'string') {
      return payload.data.htmlForm
    }
  }
  throw new Error('支付表单无效')
}

function submitTrustedAlipayForm(payload) {
  const html = resolvePaymentFormHtml(payload)
  const doc = new DOMParser().parseFromString(html, 'text/html')
  const form = doc.querySelector('form')
  if (!form) {
    throw new Error('支付表单无效')
  }

  const actionValue = form.getAttribute('action')
  if (!actionValue) {
    throw new Error('支付跳转地址缺失')
  }
  const action = new URL(actionValue)
  if (action.protocol !== 'https:' || !ALIPAY_ALLOWED_HOSTS.has(action.host)) {
    throw new Error('支付跳转地址未被信任')
  }
  if (form.method && form.method.toLowerCase() !== 'post') {
    throw new Error('支付表单方法无效')
  }

  const trustedForm = document.createElement('form')
  trustedForm.method = 'post'
  trustedForm.action = action.toString()

  form.querySelectorAll('input').forEach((input) => {
    const name = input.getAttribute('name')
    if (!name) return
    const field = document.createElement('input')
    field.type = 'hidden'
    field.name = name
    field.value = input.getAttribute('value') || ''
    trustedForm.appendChild(field)
  })

  document.body.appendChild(trustedForm)
  trustedForm.submit()
}

export function createPayment({ outTradeNo, totalAmount, subject, body }) {
  return request
    .post('/alipay/pay', {
      outTradeNo,
      totalAmount,
      subject,
      body,
    }, { silent: true })
    .then((res) => {
      submitTrustedAlipayForm(res)
      return true
    })
}
