import request from '../utils/request'

/** 获取最新健康数据 */
export function getLatestHealth(params) {
  return request('/health/latest', { method: 'GET', params })
}

/** 获取今日健康数据 */
export function getTodayHealth(params) {
  return request('/health/today', { method: 'GET', params })
}

/** 录入健康数据（基础版） */
export function saveHealth(data) {
  return request('/health/record', { method: 'POST', data })
}

/** 录入健康数据并预警 */
export function saveHealthWithAlert(data) {
  return request('/health/alert/record', { method: 'POST', data })
}

/** 获取健康历史记录 */
export function getHealthHistory(params) {
  return request('/health/history', { method: 'GET', params })
}

/** 获取健康趋势数据 */
export function getHealthTrend(params) {
  return request('/health/trend', { method: 'GET', params })
}

/** 获取健康统计 */
export function getHealthStatistics(params) {
  return request('/health/statistics', { method: 'GET', params })
}

/** 获取预警列表（基于 elderId 查健康记录 warning_flag=1） */
export function getAlertList(params) {
  return request('/health/alert/list', { method: 'GET', params })
}

/** 获取未读预警数 */
export function getUnreadWarnings(params) {
  return request('/health/alert/unread', { method: 'GET', params })
}

/** 标记预警已读 */
export function markAlertRead(id) {
  return request(`/health/alert/read/${id}`, { method: 'POST' })
}

/** 标记全部预警已读 */
export function markAllAlertsRead(elderId) {
  return request('/health/alert/read-all', { method: 'POST', params: { elderId } })
}

/** 删除健康记录 */
export function deleteHealthRecord(id) {
  return request(`/health/record/${id}`, { method: 'DELETE' })
}

/** 更新健康记录 */
export function updateHealthRecord(id, data) {
  return request(`/health/record/${id}`, { method: 'PUT', data })
}
