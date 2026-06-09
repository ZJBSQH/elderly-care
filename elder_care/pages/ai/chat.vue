<template>
  <view class="chat-container">
    <!-- 顶部 -->
    <view class="chat-header">
      <text class="header-avatar">🤖</text>
      <view class="header-text">
        <text class="header-title">健康问答助手</text>
        <text class="header-desc">智能用药咨询 · 健康知识 · 仅供参考</text>
      </view>
    </view>

    <!-- 模式切换 -->
    <view class="mode-bar">
      <view
        class="mode-chip"
        :class="{ active: chatMode === 'rag' }"
        @click="switchMode('rag')"
      >
        <text>🧠 智能增强</text>
      </view>
      <view
        class="mode-chip"
        :class="{ active: chatMode === 'normal' }"
        @click="switchMode('normal')"
      >
        <text>💬 普通问答</text>
      </view>
      <text class="mode-hint">{{ chatMode === 'rag' ? '结合知识库回答，更准确' : '仅凭AI自身知识回答' }}</text>
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
        <text class="welcome-title">你好！关于用药和健康的问题都可以问我</text>
        <text class="welcome-desc">
          {{ chatMode === 'rag' ? '我会结合医学知识库为你提供更准确的回答' : '我会尽力为你解答健康相关的问题' }}
        </text>
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
        <input
          class="msg-input"
          v-model="inputText"
          placeholder="输入问题..."
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
import { askAIQuestion, getAITodayAdvice, askHealthKnowledge, askRagQuestion } from '../../api/ai.js'
import { useUserStore } from '../../store/user.js'

const userStore = useUserStore()

const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const scrollTop = ref(0)
const chatMode = ref('rag')  // 默认 RAG 模式

const quickQuestions = [
  '降压药漏服了怎么办？',
  '布洛芬和阿司匹林能一起吃吗？',
  '吃头孢类抗生素能喝酒吗？',
  '二甲双胍什么时候吃比较好？'
]

// 处理 URL 参数
onMounted(() => {
  try {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const action = currentPage?.options?.action
    if (action === 'todayAdvice') {
      getTodayAdvice()
    }
  } catch (e) { /* ignore */ }
})

function scrollToBottom() {
  nextTick(() => { scrollTop.value = scrollTop.value + 99999 })
}

function switchMode(mode) {
  chatMode.value = mode
}

/** 发送消息 */
async function sendMsg() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

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
    // 根据模式选择接口
    const events = chatMode.value === 'rag'
      ? await askHealthKnowledge(text)
      : await askAIQuestion(text)

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

    aiMsg.text = fullText || (chatMode.value === 'rag'
      ? '抱歉，暂时无法回答这个问题。请确认知识库中已有相关文档。'
      : '抱歉，暂时无法回答这个问题，请稍后再试。')
    await typewriterEffect(aiMsg)
  } catch (e) {
    aiMsg.text = '网络请求失败，请检查网络后重试。'
    aiMsg.displayText = aiMsg.text
    aiMsg.isStreaming = false
  } finally {
    loading.value = false
  }
}

function sendQuick(question) {
  inputText.value = question
  sendMsg()
}

/** 获取今日用药建议（RAG 模式用 RAG 接口，普通模式用旧接口） */
async function getTodayAdvice() {
  const displayText = '请给我今天的用药建议'
  messages.value.push({ role: 'user', text: '今日用药建议', displayText })

  const aiMsg = { role: 'ai', text: '', displayText: '', isStreaming: true }
  messages.value.push(aiMsg)
  loading.value = true
  scrollToBottom()

  try {
    const events = chatMode.value === 'rag'
      ? await askRagQuestion(displayText)
      : await getAITodayAdvice()

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

/** 打字机效果 */
function typewriterEffect(msg) {
  const fullText = msg.text
  msg.displayText = ''

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

.header-avatar { font-size: 56rpx; }

.header-text { display: flex; flex-direction: column; }

.header-title {
  font-size: $elder-font-large;
  font-weight: bold;
  color: #fff;
}

.header-desc {
  font-size: 22rpx;
  color: rgba(255,255,255,0.75);
}

// ========== 模式切换 ==========
.mode-bar {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 28rpx;
  background: $color-white;
  flex-shrink: 0;
  border-bottom: 2rpx solid #f0f0f0;
}

.mode-chip {
  padding: 8rpx 24rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  background: #f5f7fa;
  color: $color-text-light;
  border: 2rpx solid transparent;
  transition: all 0.2s;

  &.active {
    background: rgba(64,158,255,0.08);
    color: $color-primary;
    border-color: $color-primary;
    font-weight: 600;
  }
}

.mode-hint {
  font-size: 22rpx;
  color: $color-text-placeholder;
  margin-left: auto;
}

// ========== 消息列表 ==========
.msg-list {
  flex: 1;
  padding: 24rpx 28rpx;
  overflow-y: auto;
}

.bottom-spacer { height: 24rpx; }

// ========== 欢迎区域 ==========
.welcome-area {
  text-align: center;
  padding: 80rpx 0;
}

.welcome-icon { font-size: 80rpx; display: block; margin-bottom: 20rpx; }
.welcome-title {
  display: block;
  font-size: $elder-font-large;
  font-weight: bold;
  color: $color-text;
  margin-bottom: 12rpx;
}
.welcome-desc {
  display: block;
  font-size: $elder-font-small;
  color: $color-text-placeholder;
}

// ========== 消息行 ==========
.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  margin-bottom: 28rpx;

  &.user { flex-direction: row-reverse; }
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
.loading-bubble { padding: 24rpx 36rpx; }

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

  &:active { background: rgba(64, 158, 255, 0.1); }
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
  height: 72rpx;
  line-height: 72rpx;
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
