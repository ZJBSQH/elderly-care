import request from '../utils/request'

/**
 * AI用药助手API（已废弃，请使用 api/ai.js 中的 sseRequest 版本）
 * 此文件使用普通 request()，无法正确解析 SSE 流式响应。
 * 保留仅作为非流式接口的备用参考。
 */

// 1. 用药问题咨询（注意：后端 @RequestBody String，发送原始字符串）
export function askMedicineQuestion(question) {
  return request('/ai/medicine/question', { method: 'POST', data: question })
}

// 2. 发起用药提醒（当前后端暂未提供此接口）
export function initiateMedicineReminder(elderId) {
  return request(`/ai/medicine/remind/${elderId}`, { method: 'POST' })
}

// 3. 获取今日用药建议（后端从 Token 中解析用户身份）
export function getTodayAdvice() {
  return request('/ai/medicine/today-advice', { method: 'GET' })
}

// 4. 漏服干预（当前后端暂未提供此接口）
export function handleMissedMedicine(elderId, taskId) {
  return request('/ai/medicine/missed', { method: 'POST', params: { elderId, taskId } })
}

// 5. 获取当前用户信息
export function getCurrentUserInfo() {
  return request('/auth/profile', { method: 'GET' })
}

// 6. 家属获取绑定的老人列表
export function getBoundElders() {
  return request('/user/family/elders', { method: 'GET' })
}
