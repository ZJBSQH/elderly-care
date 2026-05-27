import { ref, onUnmounted } from 'vue'

/**
 * 用药提醒 composable
 * 定时检查当前时间是否匹配用药提醒时间，触发震动/铃声/语音播报
 *
 * @param {Ref<Array>} tasks - 用药任务列表（含 remindTime、title）
 * @returns {{ startReminder, stopReminder, isReminding }}
 */
export function useMedicineReminder(tasks) {
  const isReminding = ref(false)
  let timer = null
  let audioCtx = null
  // 记录已触发提醒的任务 ID，避免同一天重复提醒
  const triggeredToday = new Set()

  /** 每日重置已触发记录 */
  function resetDailyTriggered() {
    const today = new Date().toDateString()
    const stored = uni.getStorageSync('reminder_triggered_date')
    if (stored !== today) {
      triggeredToday.clear()
      uni.setStorageSync('reminder_triggered_date', today)
    }
  }

  /** 获取当前时间字符串 HH:MM */
  function getNowTimeStr() {
    const now = new Date()
    return `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
  }

  /** 格式化提醒时间为 HH:MM */
  function formatRemindTime(timeStr) {
    if (!timeStr) return ''
    const s = String(timeStr)
    // 支持 "08:00:00" 或 "08:00" 格式
    if (s.includes(':')) {
      const parts = s.split(':')
      return `${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}`
    }
    return s.substring(0, 5)
  }

  /** 震动 */
  function vibrate() {
    try {
      uni.vibrateLong({ success() {}, fail() {} })
    } catch (e) { /* 部分平台不支持 */ }
  }

  /** 播放提示音 */
  function playAlertSound() {
    try {
      if (audioCtx) audioCtx.destroy()
      audioCtx = uni.createInnerAudioContext()
      // 使用系统提示音（如果项目中有自定义铃声可替换路径）
      audioCtx.src = '/static/reminder.mp3'
      audioCtx.autoplay = true
      audioCtx.volume = 0.8
      audioCtx.onError(() => {
        // 如果 reminder.mp3 不存在，尝试使用振动替代
        vibrate()
      })
    } catch (e) {
      vibrate()
    }
  }

  /** 语音播报 */
  function speakMedicine(task) {
    const name = task.title || task.content || '药品'
    const text = `用药提醒：请服用${name}`
    try {
      // #ifdef APP-PLUS
      const main = plus.android.runtimeMainActivity()
      const SpeechUtility = plus.android.importClass('com.iflytek.cloud.SpeechUtility')
      // App 端使用讯飞语音（需集成 SDK），此处降级为震动+提示
      uni.showModal({
        title: '⏰ 用药提醒',
        content: `该服用「${name}」了`,
        showCancel: false,
        confirmText: '我知道了'
      })
      // #endif

      // #ifdef H5
      if ('speechSynthesis' in window) {
        const utterance = new SpeechSynthesisUtterance(text)
        utterance.lang = 'zh-CN'
        utterance.rate = 0.8
        utterance.volume = 1
        window.speechSynthesis.speak(utterance)
      }
      // #endif

      // #ifdef MP-WEIXIN
      uni.showModal({
        title: '⏰ 用药提醒',
        content: `该服用「${name}」了`,
        showCancel: false,
        confirmText: '我知道了'
      })
      // #endif
    } catch (e) {
      // 降级：弹窗提醒（老年用户友好）
      uni.showModal({
        title: '⏰ 用药提醒',
        content: `该服用「${name}」了`,
        showCancel: false,
        confirmText: '我知道了'
      })
    }
  }

  /** 触发提醒 */
  function triggerReminder(task) {
    const taskId = task.id
    if (triggeredToday.has(taskId)) return
    triggeredToday.add(taskId)

    isReminding.value = true
    vibrate()
    setTimeout(() => playAlertSound(), 300)
    setTimeout(() => speakMedicine(task), 800)

    // 3 秒后恢复状态
    setTimeout(() => {
      isReminding.value = false
    }, 3000)
  }

  /** 检查是否有需要提醒的任务 */
  function checkReminders() {
    if (!tasks.value || tasks.value.length === 0) return

    const now = getNowTimeStr()
    for (const task of tasks.value) {
      const remindTime = formatRemindTime(task.remindTime)
      if (remindTime === now) {
        triggerReminder(task)
      }
    }
  }

  /** 启动提醒检查（每 30 秒检查一次） */
  function startReminder() {
    resetDailyTriggered()
    stopReminder()
    checkReminders()
    timer = setInterval(checkReminders, 30000)
  }

  /** 停止提醒检查 */
  function stopReminder() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    if (audioCtx) {
      audioCtx.destroy()
      audioCtx = null
    }
  }

  onUnmounted(stopReminder)

  return { startReminder, stopReminder, isReminding }
}
