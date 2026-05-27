import request from '../utils/request'

/**
 * 密码登录
 * 返回 { token, user, elderId }
 */
export function login(data) {
  return request('/auth/login', {
    method: 'POST',
    data
  })
}

/**
 * 用户注册
 * data: { phone, smsCode, password, name, age, sex, userType }
 */
export function register(data) {
  return request('/auth/register', {
    method: 'POST',
    data
  })
}

/**
 * 发送短信验证码
 * data: { phone, type }  type: 'register' | 'login' | 'reset'
 */
export function sendSmsCode(data) {
  return request('/auth/sms', {
    method: 'POST',
    data
  })
}

/**
 * 重置密码（忘记密码）
 * data: { phone, smsCode, newPassword }
 */
export function resetPassword(data) {
  return request('/auth/password/reset', {
    method: 'POST',
    data
  })
}

/**
 * 修改密码（已登录，需原密码）
 * data: { phone, oldPassword, newPassword }
 */
export function changePassword(data) {
  return request('/auth/password/change', {
    method: 'PUT',
    data
  })
}

/**
 * 获取当前登录用户信息
 */
export function getUserInfo() {
  return request('/auth/profile', { method: 'GET' })
}

/**
 * 更新用户资料
 * data: { id, name, avatar, age, sex }
 */
export function updateProfile(data) {
  return request('/auth/profile', {
    method: 'PUT',
    data
  })
}

/**
 * 绑定家人（手机号方式）
 * data: { familyPhone, elderPhone }
 */
export function bindFamily(data) {
  return request('/user/family/bind', {
    method: 'POST',
    data
  })
}

/**
 * 老人生成专属二维码
 */
export function generateQRCode() {
  return request('/user/elder/qrcode/generate', { method: 'GET' })
}

/**
 * 家属扫描二维码获取老人信息
 */
export function parseQRCode(qrCodeToken) {
  return request('/user/elder/qrcode/parse', {
    method: 'GET',
    params: { qrCodeToken }
  })
}

/**
 * 家属确认绑定老人
 * data: { elderId, relation }
 */
export function bindElderConfirm(data) {
  return request('/user/family/bind/confirm', {
    method: 'POST',
    data
  })
}
