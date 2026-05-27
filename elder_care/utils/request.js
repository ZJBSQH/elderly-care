// 后端 API 基础地址
const BASE_URL = 'http://localhost:8090'

/**
 * 统一网络请求封装
 * - 自动拼接 GET 参数到 URL
 * - 自动注入 Authorization: Bearer <token>
 * - 自动解包后端 Result<T>，调用方直接拿到 data
 * - 401/403 时清除 Token 并跳转登录页
 *
 * @param {string} url - API 路径，如 '/auth/login'
 * @param {object} options - { method, params, data, header }
 * @returns {Promise<any>} 后端 Result.data 的内容
 */
function request(url, options = {}) {
  const token = uni.getStorageSync('token')

  // 1. GET 请求自动拼接参数到 URL
  if (options.params) {
    const qs = Object.keys(options.params)
      .filter((k) => options.params[k] != null)
      .map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(options.params[k])}`)
      .join('&')
    if (qs) {
      url += (url.includes('?') ? '&' : '?') + qs
    }
  }

  // 2. 构建请求头，自动注入 Token
  const header = {
    'Content-Type': 'application/json',
    ...options.header
  }
  if (token) {
    header['Authorization'] = `Bearer ${token}`
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method: options.method || 'GET',
      data: options.data,
      header,
      success(res) {
        const body = res.data

        // 处理 HTTP 级别的 401/403
        if (res.statusCode === 401 || res.statusCode === 403) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.showToast({ title: '登录已过期，请重新登录', icon: 'none', duration: 2000 })
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login/login' })
          }, 2000)
          reject(new Error(body?.message || '认证失败'))
          return
        }

        // 3. 自动解包 Result<T>：code === 200 时返回 data
        if (body && typeof body === 'object' && 'code' in body) {
          if (body.code === 200) {
            resolve(body.data)
          } else {
            uni.showToast({ title: body.message || '操作失败', icon: 'none', duration: 2000 })
            reject(new Error(body.message))
          }
        } else {
          // 非标准格式直接返回
          resolve(body)
        }
      },
      fail(err) {
        uni.showToast({ title: '网络异常，请检查后端服务', icon: 'none', duration: 2000 })
        reject(err)
      }
    })
  })
}

export default request
