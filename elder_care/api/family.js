import request from '../utils/request'

/** 家属获取绑定的老人列表 */
export function getBoundElders() {
  return request('/user/family/elders', { method: 'GET' })
}

/** 查看老人的用药计划 */
export function getElderMedicinePlan(elderId) {
  return request(`/medicine/plan/${elderId}`, { method: 'GET' })
}

/** 远程修改用药计划 */
export function remoteUpdateMedicine(data) {
  return request('/medicine/remote/update', { method: 'PUT', data })
}

/** 查看老人今日服药记录 */
export function getElderTodayRecords(elderId) {
  return request('/record/today', { method: 'GET', params: { elderId } })
}

/** 扫描二维码获取老人信息 */
export function parseQRCode(token) {
  return request('/auth/code/parse', { method: 'GET', params: { qrCodeToken: token } })
}

/** 确认绑定老人 */
export function confirmBindElder(data) {
  return request('/auth/bind/confirm', { method: 'POST', data })
}
