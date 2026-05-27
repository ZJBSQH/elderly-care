<template>
  <view class="page-container">
    <view class="page-header">
      <text class="page-icon">🤖</text>
      <text class="page-title">AI用药助手</text>
      <text class="page-desc">智能用药指导与咨询</text>
    </view>
    <view class="content-area">
      <view class="quick-card" @click="goToChat">
        <text class="quick-icon">💊</text>
        <text class="quick-title">用药咨询</text>
        <text class="quick-desc">咨询用药时间、副作用、注意事项等</text>
        <text class="quick-arrow">→</text>
      </view>
      <view class="quick-card" @click="goToTodayAdvice">
        <text class="quick-icon">📋</text>
        <text class="quick-title">今日用药建议</text>
        <text class="quick-desc">根据你的用药计划获取AI建议</text>
        <text class="quick-arrow">→</text>
      </view>
      <view class="quick-card" @click="goToVoiceChat">
        <text class="quick-icon">🎤</text>
        <text class="quick-title">语音咨询</text>
        <text class="quick-desc">使用语音向AI询问用药问题</text>
        <text class="quick-arrow">→</text>
      </view>
    </view>
  </view>
</template>

<script setup>
function goToChat() {
  uni.navigateTo({ url: '/pages/ai/chat' })
}
function goToTodayAdvice() {
  uni.navigateTo({ url: '/pages/ai/chat?action=todayAdvice' })
}
function goToVoiceChat() {
  uni.navigateTo({ url: '/pages/ai/chat?voice=1' })
}
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.page-container {
  min-height: 100vh;
  background: $color-bg;
  padding: $elder-spacing-xl;
  padding-bottom: 120rpx;
}

.page-header { text-align: center; padding: 60rpx 0 40rpx; }
.page-icon { font-size: 80rpx; display: block; margin-bottom: $elder-spacing-sm; }
.page-title { display: block; font-size: $elder-font-xl; font-weight: bold; color: $color-text; margin-bottom: 8rpx; }
.page-desc { display: block; font-size: $elder-font-base; color: $color-text-placeholder; }

.content-area { margin-top: $elder-spacing; display: flex; flex-direction: column; gap: 20rpx; }

.quick-card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: 36rpx $elder-spacing-lg;
  box-shadow: $elder-shadow;
  position: relative;
  display: flex;
  flex-direction: column;

  &:active { background: #f8f9fb; }
}

.quick-icon { font-size: 52rpx; margin-bottom: 12rpx; }
.quick-title { font-size: $elder-font-large; font-weight: bold; color: $color-text; margin-bottom: 6rpx; }
.quick-desc { font-size: $elder-font-small; color: $color-text-placeholder; }
.quick-arrow { position: absolute; right: $elder-spacing-lg; top: 50%; transform: translateY(-50%); font-size: 32rpx; color: $color-text-placeholder; }
</style>
