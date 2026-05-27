import request from '../utils/request'

const BASE_URL = 'http://localhost:8080'

/**
 * 调用 SSE 流式接口，解析所有事件后一次性返回内容数组
 * UniApp 不支持原生流式读取，此处获取完整响应后解析 SSE 格式
 */
function sseRequest(url, options = {}) {
  const token = uni.getStorageSync('token')
  const header = { 'Content-Type': 'application/json', ...options.header }
  if (token) header['Authorization'] = `Bearer ${token}`

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method: options.method || 'POST',
      data: options.data,
      params: options.params,
      header,
      responseType: 'text',
      success(res) {
        const text = res.data
        if (typeof text !== 'string') {
          // 后端可能返回了 JSON 错误
          if (text && text.code && text.code !== 200) {
            reject(new Error(text.message || '请求失败'))
          } else {
            reject(new Error('响应格式异常'))
          }
          return
        }
        const events = parseSSE(text)
        resolve(events)
      },
      fail(err) {
        reject(err)
      }
    })
  })
}

/** 解析 SSE text/event-stream 格式 */
function parseSSE(text) {
  const events = []
  const parts = text.split(/\n\n|\r\n\r\n/)
  for (const part of parts) {
    if (!part.trim()) continue
    const lines = part.split(/\n|\r\n/)
    let eventType = 'message'
    let data = ''
    for (const line of lines) {
      if (line.startsWith('event:')) eventType = line.substring(6).trim()
      else if (line.startsWith('data:')) data = line.substring(5).trim()
    }
    if (data) events.push({ type: eventType, data })
  }
  return events
}

/**
 * AI 用药问题咨询（SSE 流式）
 * 返回解析后的事件数组 [{type, data}, ...]
 */
export function askAIQuestion(elderId, question) {
  return sseRequest('/ai-assistant/medicine/question', {
    method: 'POST',
    params: { elderId },
    data: { question }
  })
}

/**
 * 获取今日用药建议（SSE 流式）
 */
export function getAITodayAdvice(elderId) {
  return sseRequest(`/ai-assistant/medicine/today-advice/${elderId}`, {
    method: 'GET'
  })
}

/**
 * 发起用药提醒（非流式）
 */
export function initiateReminder(elderId) {
  return request(`/ai-assistant/medicine/remind/${elderId}`, { method: 'POST' })
}
