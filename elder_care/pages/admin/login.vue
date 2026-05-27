<template>
  <view class="login-container">
    <view class="login-card">
      <text class="login-icon">🔐</text>
      <text class="login-title">管理后台</text>
      <text class="login-desc">请使用管理员账号登录</text>

      <view class="form-item">
        <text class="form-label">手机号</text>
        <input class="form-input" v-model="phone" type="number" placeholder="请输入手机号" maxlength="11" />
      </view>

      <view class="form-item">
        <text class="form-label">密码</text>
        <input class="form-input" v-model="password" type="password" placeholder="请输入密码" />
      </view>

      <button class="login-btn" :loading="loading" @click="handleLogin">
        登录管理后台
      </button>

      <text class="switch-link" @click="goToUserLogin">切换到用户登录</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '../../store/user.js'
import { login } from '../../api/auth.js'

const userStore = useUserStore()
const phone = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!phone.value.trim()) return uni.showToast({ title: '请输入手机号', icon: 'none', duration: 2000 })
  if (!password.value) return uni.showToast({ title: '请输入密码', icon: 'none', duration: 2000 })

  loading.value = true
  try {
    const result = await login({ phone: phone.value.trim(), password: password.value })
    if (!result || !result.token) {
      return uni.showToast({ title: '登录失败，请检查账号密码', icon: 'none', duration: 2000 })
    }

    userStore.loginSuccess(result.token, result.user, result.elderId)
    const userType = result.user?.userType ?? result.userType

    if (userType !== 2) {
      uni.showToast({ title: '该账号不是管理员', icon: 'none', duration: 2000 })
      userStore.logout()
      return
    }

    uni.redirectTo({ url: '/pages/admin/dashboard' })
  } catch (e) {
    uni.showToast({ title: e.message || '登录失败', icon: 'none', duration: 2000 })
  } finally {
    loading.value = false
  }
}

function goToUserLogin() {
  uni.redirectTo({ url: '/pages/login/login' })
}
</script>

<style lang="scss" scoped>
@import '../../styles/common.scss';

.login-container {
  min-height: 100vh;
  background: #1a1a2e;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48rpx;
}

.login-card {
  width: 100%;
  max-width: 560rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 56rpx 44rpx;
  box-shadow: 0 16rpx 48rpx rgba(0,0,0,0.2);
}

.login-icon {
  display: block;
  text-align: center;
  font-size: 72rpx;
  margin-bottom: 16rpx;
}

.login-title {
  display: block;
  text-align: center;
  font-size: 40rpx;
  font-weight: bold;
  color: $color-text;
  margin-bottom: 8rpx;
}

.login-desc {
  display: block;
  text-align: center;
  font-size: 26rpx;
  color: $color-text-placeholder;
  margin-bottom: 48rpx;
}

.form-item {
  margin-bottom: 28rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: $color-text;
  margin-bottom: 12rpx;
}

.form-input {
  height: 88rpx;
  background: #f5f7fa;
  border-radius: 14rpx;
  padding: 0 24rpx;
  font-size: 30rpx;
}

.login-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: #fff;
  border-radius: 14rpx;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
  margin-top: 40rpx;

  &[disabled] { opacity: 0.7; }
}

.switch-link {
  display: block;
  text-align: center;
  font-size: 26rpx;
  color: $color-text-placeholder;
  margin-top: 32rpx;
}
</style>
