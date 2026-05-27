import request from '../utils/request'

// ==================== 提醒设置 ====================

/** 获取当前用户提醒设置 */
export function getRemindSettings() {
  return request('/remind/settings', { method: 'GET' })
}

/** 更新提醒设置 */
export function updateRemindSettings(data) {
  return request('/remind/settings', { method: 'PUT', data })
}

// ==================== 提醒任务 ====================

/** 创建提醒任务 */
export function createRemindTask(data) {
  return request('/remind/task/create', { method: 'POST', data })
}

/** 更新提醒任务 */
export function updateRemindTask(data) {
  return request('/remind/task/update', { method: 'PUT', data })
}

/** 删除提醒任务 */
export function deleteRemindTask(id) {
  return request(`/remind/task/delete/${id}`, { method: 'DELETE' })
}

/** 查询今日提醒任务 */
export function getTodayTasks(params) {
  return request('/remind/task/today', { method: 'GET', params })
}

/** 按老人ID查询提醒任务 */
export function getTasksByElderId(elderId) {
  return request(`/remind/task/elder/${elderId}`, { method: 'GET' })
}

/** 查询提醒任务列表（支持筛选） */
export function listRemindTasks(params) {
  return request('/remind/task/list', { method: 'GET', params })
}

// ==================== 通知 ====================

/** 获取用户所有通知 */
export function getMyNotifications() {
  return request('/remind/notification/list', { method: 'GET' })
}

/** 获取未读通知数量 */
export function countUnread() {
  return request('/remind/notification/unread', { method: 'GET' })
}

/** 标记单条通知已读 */
export function markNotificationRead(id) {
  return request(`/remind/notification/read/${id}`, { method: 'POST' })
}

/** 标记全部通知已读 */
export function markAllNotificationsRead() {
  return request('/remind/notification/read-all', { method: 'POST' })
}
