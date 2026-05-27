<template>
  <view class="page-container">
    <view class="page-header">
      <text class="page-icon">👨‍👩‍👦</text>
      <text class="page-title">家属绑定</text>
      <text class="page-desc">让家人扫描二维码绑定，随时关注你的健康</text>
    </view>

    <!-- 我的二维码 -->
    <view class="card qr-card">
      <text class="card-title">📱 我的二维码</text>
      <text class="card-desc">让家人打开TA的APP → 添加家人 → 扫码绑定</text>
      <view class="qr-code-area">
        <image v-if="qrCodeUrl" class="qr-image" :src="qrCodeUrl" mode="aspectFit" />
        <view class="qr-placeholder" v-else>
          <text class="qr-placeholder-icon">📷</text>
          <text class="qr-placeholder-text">二维码加载中...</text>
        </view>
      </view>
      <button class="refresh-btn" :loading="loadingQR" @click="loadQRCode">刷新二维码</button>
    </view>

    <!-- 已绑定家属列表 -->
    <view class="card" v-if="boundList.length > 0">
      <text class="card-title">已绑定的家人（{{ boundList.length }}人）</text>
      <view class="family-item" v-for="f in boundList" :key="f.id || f.userId">
        <view class="family-avatar">
          <text class="avatar-text">{{ (f.name || '家').charAt(0) }}</text>
        </view>
        <view class="family-info">
          <text class="family-name">{{ f.name || '家属' }}</text>
          <text class="family-relation" v-if="f.relation">{{ f.relation }}</text>
        </view>
        <text class="family-phone" v-if="f.phone">{{ f.phone }}</text>
      </view>
    </view>

    <!-- 暂无绑定 -->
    <view class="card empty-card" v-if="boundList.length === 0 && !loadingList">
      <text class="empty-text">暂未绑定家属</text>
      <text class="empty-desc">让家人扫描上方二维码即可完成绑定</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { generateQRCode } from '../../api/auth.js'
import { getBoundElders } from '../../api/family.js'
import { useUserStore } from '../../store/user.js'

const userStore = useUserStore()

const qrCodeUrl = ref('')
const loadingQR = ref(false)
const boundList = ref([])
const loadingList = ref(true)

/** 加载二维码 */
async function loadQRCode() {
  loadingQR.value = true
  try {
    const result = await generateQRCode()
    qrCodeUrl.value = result?.qrCodeUrl || result?.url || result || ''
  } catch (e) {
    uni.showToast({ title: '二维码生成失败', icon: 'none', duration: 2000 })
  } finally {
    loadingQR.value = false
  }
}

/** 加载已绑定家属列表 */
async function loadBoundList() {
  loadingList.value = true
  try {
    const list = await getBoundElders()
    boundList.value = Array.isArray(list) ? list : []
  } catch (e) {
    boundList.value = []
  } finally {
    loadingList.value = false
  }
}

onMounted(() => {
  loadQRCode()
  loadBoundList()
})
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

.card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing-lg;
  margin-bottom: 24rpx;
  box-shadow: $elder-shadow;
}

.card-title { display: block; font-size: $elder-font-large; font-weight: bold; color: $color-text; margin-bottom: 12rpx; }
.card-desc { display: block; font-size: $elder-font-small; color: $color-text-placeholder; margin-bottom: 24rpx; line-height: 1.6; }

// 二维码
.qr-card { text-align: center; }

.qr-code-area {
  width: 360rpx;
  height: 360rpx;
  margin: 0 auto 24rpx;
  background: #f5f7fa;
  border-radius: $elder-radius;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.qr-image { width: 100%; height: 100%; }

.qr-placeholder { text-align: center; }
.qr-placeholder-icon { font-size: 72rpx; display: block; margin-bottom: 12rpx; }
.qr-placeholder-text { font-size: $elder-font-small; color: $color-text-placeholder; }

.refresh-btn {
  display: inline-block;
  height: 72rpx;
  line-height: 72rpx;
  background: #f5f7fa;
  color: $color-primary;
  border-radius: $elder-radius;
  font-size: $elder-font-small;
  border: 2rpx solid $color-primary;
  padding: 0 36rpx;
}

// 家属列表
.family-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx 0;
  border-bottom: 2rpx solid #f5f5f5;
  &:last-child { border-bottom: none; }
}

.family-avatar {
  width: 80rpx; height: 80rpx; border-radius: 50%;
  background: linear-gradient(135deg, #5B8FF9, #36cfc9);
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.avatar-text { font-size: 36rpx; font-weight: bold; color: #fff; }

.family-info { flex: 1; }
.family-name { display: block; font-size: $elder-font-medium; font-weight: 600; color: $color-text; }
.family-relation { font-size: $elder-font-small; color: $color-text-placeholder; }
.family-phone { font-size: $elder-font-small; color: $color-text-light; }

// 空状态
.empty-card { text-align: center; padding: 64rpx $elder-spacing-lg; }
.empty-text { display: block; font-size: $elder-font-medium; color: $color-text-placeholder; margin-bottom: 8rpx; }
.empty-desc { display: block; font-size: $elder-font-small; color: $color-text-placeholder; }
</style>
