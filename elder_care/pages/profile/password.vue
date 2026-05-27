<template>
  <view class="page-container">
    <view class="form-card">
      <!-- 手机号（只读） -->
      <view class="form-item">
        <text class="form-label">手机号</text>
        <text class="readonly-text">{{ phone }}</text>
      </view>

      <!-- 原密码 -->
      <view class="form-item">
        <text class="form-label">原密码</text>
        <input
          class="form-input"
          type="password"
          v-model="oldPassword"
          placeholder="请输入原密码"
          maxlength="20"
        />
      </view>

      <!-- 新密码 -->
      <view class="form-item">
        <text class="form-label">新密码</text>
        <input
          class="form-input"
          type="password"
          v-model="newPassword"
          placeholder="6-20位新密码"
          maxlength="20"
        />
      </view>

      <!-- 确认新密码 -->
      <view class="form-item">
        <text class="form-label">确认新密码</text>
        <input
          class="form-input"
          type="password"
          v-model="confirmPassword"
          placeholder="再次输入新密码"
          maxlength="20"
        />
      </view>

      <button class="submit-btn" :loading="loading" @click="handleSubmit">修改密码</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '../../store/user.js'
import { changePassword } from '../../api/auth.js'

const userStore = useUserStore()

const phone = computed(() => userStore.userInfo?.phone || '')

const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const loading = ref(false)

async function handleSubmit() {
  if (!oldPassword.value) {
    return uni.showToast({ title: '请输入原密码', icon: 'none', duration: 2000 })
  }
  if (!newPassword.value || newPassword.value.length < 6) {
    return uni.showToast({ title: '新密码至少6位', icon: 'none', duration: 2000 })
  }
  if (newPassword.value !== confirmPassword.value) {
    return uni.showToast({ title: '两次输入的新密码不一致', icon: 'none', duration: 2000 })
  }
  if (newPassword.value === oldPassword.value) {
    return uni.showToast({ title: '新密码不能与旧密码相同', icon: 'none', duration: 2000 })
  }
  if (!phone.value) {
    return uni.showToast({ title: '无法获取手机号', icon: 'none', duration: 2000 })
  }

  loading.value = true
  try {
    await changePassword({
      phone: phone.value,
      oldPassword: oldPassword.value,
      newPassword: newPassword.value
    })
    uni.showToast({ title: '密码修改成功，请重新登录', icon: 'success', duration: 2000 })
    setTimeout(() => userStore.logout(), 2000)
  } catch (e) {
    uni.showToast({ title: e.message || '修改失败', icon: 'none', duration: 2000 })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.page-container {
  min-height: 100vh;
  background: $color-bg;
  padding: $elder-spacing-xl;
}

.form-card {
  background: $color-white;
  border-radius: $elder-radius-lg;
  padding: $elder-spacing-lg;
  box-shadow: $elder-shadow;
}

.form-item {
  margin-bottom: 36rpx;
}

.form-label {
  display: block;
  font-size: $elder-font-base;
  font-weight: 600;
  color: $color-text;
  margin-bottom: 12rpx;
}

.form-input {
  height: 88rpx;
  background: #f5f7fa;
  border-radius: $elder-radius;
  padding: 0 24rpx;
  font-size: $elder-font-base;
  color: $color-text;
}

.readonly-text {
  display: block;
  height: 88rpx;
  line-height: 88rpx;
  background: #f5f7fa;
  border-radius: $elder-radius;
  padding: 0 24rpx;
  font-size: $elder-font-base;
  color: $color-text-placeholder;
}

.submit-btn {
  width: 100%;
  height: $elder-btn-height;
  line-height: $elder-btn-height;
  background: linear-gradient(135deg, $color-primary 0%, #36cfc9 100%);
  color: #fff;
  border-radius: $elder-radius-lg;
  font-size: $elder-font-medium;
  font-weight: bold;
  border: none;
  margin-top: 20rpx;

  &[disabled] { opacity: 0.7; }
}
</style>
