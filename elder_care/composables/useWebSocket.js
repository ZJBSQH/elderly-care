import { ref, onUnmounted } from 'vue'

const BASE_WS = 'ws://localhost:8080'

/**
 * WebSocket 连接管理 composable
 * 对接后端 /ws/notification/{userId} 端点
 */
export function useWebSocket(userId) {
  const unreadCount = ref(0)
  const lastNotification = ref(null)
  const connected = ref(false)
  let socketTask = null
  let reconnectTimer = null

  function connect() {
    if (!userId) return

    try {
      socketTask = uni.connectSocket({
        url: `${BASE_WS}/ws/notification/${userId}`,
        success() {
          connected.value = true
        }
      })

      socketTask.onOpen(() => {
        connected.value = true
        console.log('WebSocket 已连接, userId:', userId)
      })

      socketTask.onMessage((res) => {
        try {
          const notification = JSON.parse(res.data)
          lastNotification.value = notification
          unreadCount.value++
        } catch (e) {
          // 忽略非 JSON 消息
        }
      })

      socketTask.onError((err) => {
        console.error('WebSocket 连接错误:', err)
        connected.value = false
      })

      socketTask.onClose(() => {
        connected.value = false
        scheduleReconnect()
      })
    } catch (e) {
      console.error('WebSocket 初始化失败:', e)
      scheduleReconnect()
    }
  }

  function scheduleReconnect() {
    if (reconnectTimer) clearTimeout(reconnectTimer)
    reconnectTimer = setTimeout(() => {
      console.log('WebSocket 尝试重连...')
      connect()
    }, 5000)
  }

  function disconnect() {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    socketTask?.close({ code: 1000, reason: '用户主动断开' })
    socketTask = null
    connected.value = false
  }

  /** 重置未读计数 */
  function resetCount() {
    unreadCount.value = 0
  }

  onUnmounted(disconnect)

  return { unreadCount, lastNotification, connected, connect, disconnect, resetCount }
}
