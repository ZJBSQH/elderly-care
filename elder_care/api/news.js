import request from '../utils/request'

/**
 * 获取资讯列表
 */
export function getNewsList(params) {
  return request('/health-knowledge/articles', { method: 'GET', params })
}

/**
 * 获取资讯详情
 */
export function getNewsDetail(id, params = {}) {
  return request(`/health-knowledge/article/${id}`, { method: 'GET', params })
}

/**
 * 点赞
 */
export function likeArticle(newsId) {
  return request('/health-knowledge/like', { method: 'POST', data: { newsId } })
}

/**
 * 取消点赞
 */
export function cancelLikeArticle(newsId) {
  return request('/health-knowledge/like', { method: 'DELETE', data: { newsId } })
}

/**
 * 检查是否已点赞
 */
export function checkIfLiked(newsId) {
  return request('/health-knowledge/like/check', { method: 'GET', params: { newsId } })
}

/**
 * 收藏文章
 */
export function collectArticle(newsId) {
  return request('/health-knowledge/collect', { method: 'POST', data: { newsId } })
}

/**
 * 取消收藏
 */
export function cancelCollectArticle(newsId) {
  return request('/health-knowledge/collect', { method: 'DELETE', data: { newsId } })
}

/**
 * 获取我的通知列表
 */
export function getMyNotifications() {
  return request('/remind/notification/list', { method: 'GET' })
}

/**
 * 标记通知为已读
 */
export function markNotificationRead(notificationId) {
  return request(`/remind/notification/read/${notificationId}`, { method: 'POST' })
}

/**
 * 获取未读通知数
 */
export function countUnread() {
  return request('/remind/notification/unread', { method: 'GET' })
}

/**
 * 获取精选推荐
 */
export function getRecommendedArticles() {
  return request('/health-knowledge/recommended', { method: 'GET' })
}

/**
 * 获取热门文章
 */
export function getPopularArticles(limit = 10) {
  return request('/health-knowledge/popular', { method: 'GET', params: { limit } })
}

/**
 * 检查是否已收藏
 */
export function checkIfCollected(newsId) {
  return request('/health-knowledge/collect/check', { method: 'GET', params: { newsId } })
}
