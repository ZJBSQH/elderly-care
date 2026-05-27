import request from '../utils/request'

/**
 * AI用药助手API
 */

// 1. 用药问题咨询
export function askMedicineQuestion(elderId, question) {
  return request('/ai-assistant/medicine/question', { method: 'POST', params: { elderId }, data: { question } })
}

// 2. 发起用药提醒
export function initiateMedicineReminder(elderId) {
  return request(`/ai-assistant/medicine/remind/${elderId}`, { method: 'POST' })
}

// 3. 获取今日用药建议
export function getTodayAdvice(elderId) {
  return request(`/ai-assistant/medicine/today-advice/${elderId}`, { method: 'GET' })
}

// 4. 漏服干预
export function handleMissedMedicine(elderId, taskId) {
  return request('/ai-assistant/medicine/missed', { method: 'POST', params: { elderId, taskId } })
}

// 5. 获取当前用户信息
export function getCurrentUserInfo() {
  return request('/auth/profile', { method: 'GET' })
}

// 6. 家属获取绑定的老人列表
export function getBoundElders() {
  return request('/user/family/elders', { method: 'GET' })
}
