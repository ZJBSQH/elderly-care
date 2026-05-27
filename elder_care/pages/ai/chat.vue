<template>
  <view class="chat-container">
    <!-- 顶部 -->
    <view class="chat-header">
      <text class="header-avatar">🤖</text>
      <view class="header-text">
        <text class="header-title">AI 用药助手</text>
        <text class="header-desc">智能用药咨询 · 仅供参考</text>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view
      class="msg-list"
      scroll-y
      :scroll-top="scrollTop"
      :scroll-with-animation="true"
    >
      <!-- 欢迎提示 -->
      <view class="welcome-area" v-if="messages.length === 0">
        <text class="welcome-icon">💊</text>
        <text class="welcome-title">你好！我是你的用药助手</text>
        <text class="welcome-desc">可以问我用药时间、副作用、注意事项等问题</text>
      </view>

      <!-- 消息气泡 -->
      <view
        v-for="(msg, idx) in messages"
        :key="idx"
        class="msg-row"
        :class="msg.role"
      >
        <view class="msg-avatar">
          <text>{{ msg.role === 'user' ? '👤' : '🤖' }}</text>
        </view>
        <view class="msg-bubble" :class="msg.role">
          <text class="msg-text" v-if="msg.role === 'ai' && msg.isStreaming">
            {{ msg.displayText }}<text class="cursor">|</text>
          </text>
          <text class="msg-text" v-else>{{ msg.displayText || msg.text }}</text>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="msg-row ai" v-if="loading">
        <view class="msg-avatar"><text>🤖</text></view>
        <view class="msg-bubble ai loading-bubble">
          <view class="typing-dots">
            <view class="dot"></view>
            <view class="dot"></view>
            <view class="dot"></view>
          </view>
        </view>
      </view>

      <view class="bottom-spacer"></view>
    </scroll-view>

    <!-- 快捷提问 -->
    <view class="quick-questions" v-if="messages.length === 0">
      <text class="quick-label">💡 你可以问：</text>
      <view class="quick-chips">
        <view class="quick-chip" v-for="q in quickQuestions" :key="q" @click="sendQuick(q)">
          <text>{{ q }}</text>
        </view>
      </view>
    </view>

    <!-- 底部输入区 -->
    <view class="input-area">
      <button class="advice-btn" v-if="messages.length === 0" @click="getTodayAdvice">
        📋 今日用药建议
      </button>
      <view class="input-row">
        <button
          class="voice-btn"
          :class="{ recording: isRecording }"
          :disabled="loading"
          @touchstart="startVoice"
          @touchend="stopVoice"
        >
          {{ isRecording ? '🔴' : '🎤' }}
        </button>
        <input
          class="msg-input"
          v-model="inputText"
          placeholder="输入用药问题..."
          :disabled="loading"
          @confirm="sendMsg"
          maxlength="200"
        />
        <button
          class="send-btn"
          :disabled="!inputText.trim() || loading"
          @click="sendMsg"
        >
          发送
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { askAIQuestion, getAITodayAdvice } from '../../api/ai.js'
import { useUserStore } from '../../store/user.js'

const userStore = useUserStore()

const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const scrollTop = ref(0)
const isRecording = ref(false)
let recorderManager = null

const quickQuestions = [
  '阿司匹林什么时候吃最好？',
  '降压药漏服了怎么办？',
  '多种药可以一起吃吗？',
  '服药期间能喝酒吗？'
]

// 处理 URL 参数（来自 ai-assistant 入口）
onMounted(() => {
  try {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const action = currentPage?.options?.action
    const useVoice = currentPage?.options?.voice
    if (action === 'todayAdvice') {
      getTodayAdvice()
    }
    if (useVoice === '1') {
      setTimeout(() => startVoice(), 500)
    }
  } catch (e) { /* ignore */ }
})

/** 初始化录音管理器 */
function getRecorderManager() {
  if (recorderManager) return recorderManager
  recorderManager = uni.getRecorderManager()
  recorderManager.onStop((res) => {
    isRecording.value = false
    if (res.tempFilePath) {
      uni.showToast({ title: '语音录制完成', icon: 'success', duration: 1000 })
      inputText.value = '[语音消息] 请帮我查看用药建议'
      sendMsg()
    }
  })
  recorderManager.onError((err) => {
    isRecording.value = false
    uni.showToast({ title: '录音失败: ' + (err.errMsg || '未知错误'), icon: 'none', duration: 2000 })
  })
  return recorderManager
}

/** 开始录音 */
function startVoice() {
  if (loading.value) return
  try {
    const rm = getRecorderManager()
    rm.start({ format: 'mp3', duration: 60000 })
    isRecording.value = true
  } catch (e) {
    uni.showToast({ title: '录音功能暂不可用', icon: 'none', duration: 1500 })
  }
}

/** 停止录音 */
function stopVoice() {
  if (!isRecording.value) return
  try {
    const rm = getRecorderManager()
    rm.stop()
  } catch (e) {
    isRecording.value = false
  }
}

function scrollToBottom() {
  nextTick(() => {
    scrollTop.value = scrollTop.value + 99999
  })
}

/** 发送消息 */
async function sendMsg() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  const elderId = userStore.elderId
  if (!elderId) {
    uni.showToast({ title: '无法获取用户信息', icon: 'none', duration: 2000 })
    return
  }

  // 添加用户消息
  messages.value.push({ role: 'user', text, displayText: text })
  inputText.value = ''
  scrollToBottom()

  // 添加占位 AI 消息
  const aiMsg = { role: 'ai', text: '', displayText: '', isStreaming: true }
  messages.value.push(aiMsg)
  loading.value = true
  scrollToBottom()

  try {
    const events = await askAIQuestion(elderId, text)
    const errorEvent = events.find(e => e.type === 'error')
    if (errorEvent) {
      aiMsg.text = errorEvent.data
      aiMsg.displayText = errorEvent.data
      aiMsg.isStreaming = false
      return
    }

    // 拼接所有内容
    const fullText = events
      .filter(e => e.type === 'message')
      .map(e => e.data)
      .join('')

    if (!fullText) {
      aiMsg.text = '抱歉，暂时无法回答这个问题，请稍后再试。'
      aiMsg.displayText = aiMsg.text
      aiMsg.isStreaming = false
      return
    }

    // 打字机效果
    aiMsg.text = fullText
    await typewriterEffect(aiMsg)
  } catch (e) {
    aiMsg.text = '网络请求失败，请检查网络后重试。'
    aiMsg.displayText = aiMsg.text
    aiMsg.isStreaming = false
  } finally {
    loading.value = false
  }
}

/** 快捷问题 */
function sendQuick(question) {
  inputText.value = question
  sendMsg()
}

/** 获取今日用药建议 */
async function getTodayAdvice() {
  const elderId = userStore.elderId
  if (!elderId) {
    uni.showToast({ title: '无法获取用户信息', icon: 'none', duration: 2000 })
    return
  }

  // 添加用户消息
  messages.value.push({ role: 'user', text: '今日用药建议', displayText: '请给我今天的用药建议' })

  const aiMsg = { role: 'ai', text: '', displayText: '', isStreaming: true }
  messages.value.push(aiMsg)
  loading.value = true
  scrollToBottom()

  try {
    const events = await getAITodayAdvice(elderId)
    const errorEvent = events.find(e => e.type === 'error')
    if (errorEvent) {
      aiMsg.text = errorEvent.data
      aiMsg.displayText = errorEvent.data
      aiMsg.isStreaming = false
      return
    }

    const fullText = events
      .filter(e => e.type === 'message')
      .map(e => e.data)
      .join('')

    aiMsg.text = fullText || '暂无可用的用药建议。'
    await typewriterEffect(aiMsg)
  } catch (e) {
    aiMsg.text = '获取建议失败，请稍后重试。'
    aiMsg.displayText = aiMsg.text
    aiMsg.isStreaming = false
  } finally {
    loading.value = false
  }
}

/** 打字机效果：逐字显示 */
function typewriterEffect(msg) {
  const fullText = msg.text
  msg.displayText = ''

  // 每 30ms 显示 2 个字符
  const charsPerTick = 2
  const interval = 20
  let pos = 0

  return new Promise(resolve => {
    const timer = setInterval(() => {
      pos += charsPerTick
      if (pos >= fullText.length) {
        msg.displayText = fullText
        msg.isStreaming = false
        clearInterval(timer)
        scrollToBottom()
        resolve()
      } else {
        msg.displayText = fullText.substring(0, pos)
        scrollToBottom()
      }
    }, interval)
  })
}
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: $color-bg;
}

// ========== 头部 ==========
.chat-header {
  background: linear-gradient(135deg, #5B8FF9 0%, #36cfc9 100%);
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-shrink: 0;
}

.header-avatar {
  font-size: 56rpx;
}

.header-text {
  display: flex;
  flex-direction: column;
}

.header-title {
  font-size: $elder-font-large;
  font-weight: bold;
  color: #fff;
}

.header-desc {
  font-size: 22rpx;
  color: rgba(255,255,255,0.75);
}

// ========== 消息列表 ==========
.msg-list {
  flex: 1;
  padding: 24rpx 28rpx;
  overflow-y: auto;
}

.bottom-spacer {
  height: 24rpx;
}

// ========== 欢迎区域 ==========
.welcome-area {
  text-align: center;
  padding: 80rpx 0;
}

.welcome-icon { font-size: 80rpx; display: block; margin-bottom: 20rpx; }
.welcome-title { display: block; font-size: $elder-font-large; font-weight: bold; color: $color-text; margin-bottom: 12rpx; }
.welcome-desc { display: block; font-size: $elder-font-small; color: $color-text-placeholder; }

// ========== 消息行 ==========
.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  margin-bottom: 28rpx;

  &.user {
    flex-direction: row-reverse;
  }
}

.msg-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  flex-shrink: 0;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.06);
}

.msg-bubble {
  max-width: 75%;
  padding: 18rpx 24rpx;
  border-radius: 20rpx;
  word-break: break-all;

  &.user {
    background: linear-gradient(135deg, $color-primary, #36cfc9);
    color: #fff;
    border-top-right-radius: 6rpx;

    .msg-text { color: #fff; }
  }

  &.ai {
    background: $color-white;
    color: $color-text;
    border-top-left-radius: 6rpx;
    box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.06);
    line-height: 1.7;
  }
}

.msg-text {
  font-size: $elder-font-base;
  color: $color-text;
  line-height: 1.7;
}

.cursor {
  color: $color-primary;
  font-weight: bold;
  animation: blink 0.8s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

// ========== 加载动画 ==========
.loading-bubble {
  padding: 24rpx 36rpx;
}

.typing-dots {
  display: flex;
  gap: 10rpx;
}

.dot {
  width: 14rpx;
  height: 14rpx;
  background: #ccc;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;

  &:nth-child(1) { animation-delay: -0.32s; }
  &:nth-child(2) { animation-delay: -0.16s; }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

// ========== 快捷问题 ==========
.quick-questions {
  padding: 0 28rpx 20rpx;
  flex-shrink: 0;

  .quick-label {
    font-size: $elder-font-small;
    color: $color-text-placeholder;
    margin-bottom: 16rpx;
    display: block;
  }
}

.quick-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.quick-chip {
  padding: 12rpx 24rpx;
  background: $color-white;
  border-radius: 36rpx;
  font-size: $elder-font-small;
  color: $color-primary;
  border: 2rpx solid rgba(64, 158, 255, 0.25);

  &:active {
    background: rgba(64, 158, 255, 0.1);
  }
}

// ========== 底部输入区 ==========
.input-area {
  background: $color-white;
  padding: 16rpx 28rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  border-top: 2rpx solid #f0f0f0;
  flex-shrink: 0;
}

.advice-btn {
  width: 100%;
  height: 68rpx;
  line-height: 68rpx;
  background: #f7fcff;
  color: $color-primary;
  font-size: $elder-font-small;
  border: 2rpx dashed $color-primary;
  border-radius: $elder-radius;
  margin-bottom: 14rpx;
}

.input-row {
  display: flex;
  gap: 16rpx;
  align-items: center;
}

.voice-btn {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #f5f7fa;
  border: 2rpx solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  flex-shrink: 0;
  padding: 0;

  &.recording {
    background: #fff1f0;
    border-color: #ff4d4f;
    animation: pulse 1s ease-in-out infinite;
  }
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 77, 79, 0.4); }
  50% { box-shadow: 0 0 0 20rpx rgba(255, 77, 79, 0); }
}

.msg-input {
  flex: 1;
  height: 80rpx;
  background: #f5f7fa;
  border-radius: 44rpx;
  padding: 0 28rpx;
  font-size: $elder-font-base;
  color: $color-text;
}

.send-btn {
  width: 120rpx;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  background: linear-gradient(135deg, $color-primary, #36cfc9);
  color: #fff;
  border-radius: 44rpx;
  font-size: $elder-font-base;
  font-weight: bold;
  border: none;
  flex-shrink: 0;

  &[disabled] { opacity: 0.5; }
}
</style>
