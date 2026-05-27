import request from '../utils/request'

// ==================== 仪表盘统计 ====================

/** 获取仪表盘统计数据 */
export function getDashboardStats() {
  return request('/admin/stats/dashboard', { method: 'GET' })
}

// ==================== 用户管理 ====================

/** 查询用户列表（分页 + 筛选） */
export function getAdminUsers(params) {
  return request('/admin/users', { method: 'GET', params })
}

/** 获取用户详情 */
export function getAdminUserDetail(id) {
  return request(`/admin/users/${id}`, { method: 'GET' })
}

/** 更新用户信息 */
export function updateAdminUser(id, data) {
  return request(`/admin/users/${id}`, { method: 'PUT', data })
}

/** 修改用户状态 */
export function updateUserStatus(id, status) {
  return request(`/admin/users/${id}/status`, { method: 'PUT', params: { status } })
}

/** 修改用户类型 */
export function updateUserType(id, userType) {
  return request(`/admin/users/${id}/type`, { method: 'PUT', params: { userType } })
}

// ==================== 资讯管理 ====================

/** 发布资讯 */
export function publishNews(data) {
  return request('/admin/news', { method: 'POST', data })
}

/** 更新资讯 */
export function updateNews(data) {
  return request('/admin/news', { method: 'PUT', data })
}

/** 删除资讯 */
export function deleteNews(id) {
  return request(`/admin/news/${id}`, { method: 'DELETE' })
}

/** 查询资讯列表 */
export function getAdminNewsList(params) {
  return request('/admin/news', { method: 'GET', params })
}

/** 获取资讯详情 */
export function getAdminNewsDetail(id) {
  return request(`/admin/news/${id}`, { method: 'GET' })
}

/** 上架/下架资讯 */
export function changeNewsStatus(id, status) {
  return request(`/admin/news/${id}/status`, { method: 'PUT', params: { status } })
}

/** 推荐/取消推荐 */
export function changeNewsRecommended(id, isRecommended) {
  return request(`/admin/news/${id}/recommended`, { method: 'PUT', params: { isRecommended } })
}

// ==================== 公告管理 ====================

/** 发布公告 */
export function publishAnnouncement(data) {
  return request('/admin/announcements', { method: 'POST', params: data })
}

/** 查询公告列表 */
export function getAnnouncements(params) {
  return request('/admin/announcements', { method: 'GET', params })
}

/** 删除公告 */
export function deleteAnnouncement(id) {
  return request(`/admin/announcements/${id}`, { method: 'DELETE' })
}

// ==================== 系统配置 ====================

/** 查询所有系统配置 */
export function getAllConfigs() {
  return request('/admin/system/configs', { method: 'GET' })
}

/** 更新单个配置 */
export function updateConfig(key, value) {
  return request(`/admin/system/configs/${key}`, { method: 'PUT', params: { value } })
}

/** 批量更新配置 */
export function batchUpdateConfigs(configs) {
  return request('/admin/system/configs', { method: 'PUT', data: configs })
}
