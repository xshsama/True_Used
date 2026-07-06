import request from '../utils/request'

export function createPayment({ outTradeNo, totalAmount, subject, body }) {
  return request
    .post('/alipay/pay', {
      outTradeNo,
      totalAmount,
      subject,
      body,
    })
    .then((res) => {
      // res is HTML form string returned by backend
      // Render the form in the current window and let it auto-submit
      const div = document.createElement('div')
      div.innerHTML = res
      document.body.appendChild(div)
      const form = div.querySelector('form')
      if (form) {
        form.submit()
      } else {
        // Fallback if no form found
        document.open()
        document.write(res)
        document.close()
      }
      return true
    })
}
