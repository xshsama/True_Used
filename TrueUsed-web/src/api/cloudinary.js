import request from '@/utils/request'

/**
 * 获取 Cloudinary 上传签名
 * @returns {Promise<object>}
 */
export function getCloudinarySignature() {
  // request 已设置 baseURL='/api'，此处不要再加 '/api' 前缀，避免变成 /api/api/...
  return request.get('/cloudinary/signature')
}
