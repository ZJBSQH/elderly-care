import request from '../utils/request'

// 与 utils/request.js 保持一致，统一走网关
const BASE_URL = 'http://localhost:8090'

/**
 * 调用 SSE 流式接口，解析所有事件后一次性返回内容数组
 * UniApp 不支持原生流式读取，此处获取完整响应后解析 SSE 格式
 */
function sseRequest(url, options = {}) {
  const token = uni.getStorageSync('token')
  // 根据数据类型自动设置 Content-Type：对象用 JSON，字符串用 text/plain
  const defaultType = typeof options.data === 'string' ? 'text/plain' : 'application/json'
  const header = { 'Content-Type': defaultType, ...options.header }
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
 * 后端从 Token 中解析用户身份，无需传递 elderId。
 * 返回解析后的事件数组 [{type, data}, ...]
 *
 * @param question 用户问题（纯文本，后端 @RequestBody String 直接接收）
 */
export function askAIQuestion(question) {
  return sseRequest('/ai/medicine/question', {
    method: 'POST',
    data: question  // 发送原始字符串，非 JSON 对象
  })
}

/**
 * 获取今日用药建议（SSE 流式）
 * 后端从 Token 中解析用户身份，无需传递 elderId。
 */
export function getAITodayAdvice() {
  return sseRequest('/ai/medicine/today-advice', { method: 'GET' })
}

/**
 * 发起用药提醒（非流式）
 * 注意：当前后端 AssistantController 中暂未提供此接口，调用将 404
 */
export function initiateReminder(elderId) {
  return request(`/ai/medicine/remind/${elderId}`, { method: 'POST' })
}

/**
 * RAG 增强健康知识问答（SSE 流式，无需登录）
 * 基于医学知识库检索，不关联用户个人数据
 */
export function askHealthKnowledge(question) {
  return sseRequest('/ai/rag/health-knowledge', {
    method: 'POST',
    data: { question }
  })
}

/**
 * RAG 增强个性化问答（SSE 流式，需要 Token）
 * 结合知识库 + 个人医疗数据生成回答
 */
export function askRagQuestion(question) {
  return sseRequest('/ai/rag/ask', {
    method: 'POST',
    data: { question }
  })
}
