import request from '../utils/request'

/**
 * 获取今日用药提醒任务
 */
export function getTodayMedicine(params) {
  return request('/remind/task/today', { method: 'GET', params })
}

/**
 * 添加用药计划
 */
export function addMedicine(data) {
  return request('/medicine/add', { method: 'POST', data })
}

/**
 * 查询用药计划列表
 */
export function getMedicineList(elderId) {
  return request(`/medicine/look/${elderId}`, { method: 'GET' })
}

/**
 * 更新用药计划
 */
export function updateMedicine(data) {
  return request('/medicine/update', { method: 'PUT', data })
}

/**
 * 删除用药计划
 */
export function deleteMedicine(id) {
  return request(`/medicine/delete/${id}`, { method: 'DELETE' })
}

/**
 * 获取用药历史记录
 */
export function getMedicineRecord(params) {
  return request('/record/history', { method: 'GET', params })
}

/**
 * 标记已服用
 */
export function takeMedicine(taskId) {
  return request('/record/take', { method: 'POST', data: { taskId } })
}

/**
 * 检查是否已服药
 */
export function checkRecord(taskId) {
  return request('/record/check', { method: 'GET', params: { taskId } })
}

/**
 * 标记漏服
 */
export function markMissed(taskId) {
  return request('/record/missed', { method: 'POST', data: { taskId } })
}

/**
 * 查询今日服药记录
 */
export function getTodayRecords(elderId) {
  return request('/record/today', { method: 'GET', params: { elderId } })
}

/**
 * 查询用药历史（含药品名）
 */
export function getMedicineExport(params) {
  return request('/record/export', { method: 'GET', params })
}