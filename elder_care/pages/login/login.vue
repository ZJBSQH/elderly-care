<template>
  <view class="login-container">
    <!-- Logo 区域 -->
    <view class="logo-section">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <text class="app-name">老人用药管理</text>
      <text class="app-desc">关爱健康，从用药开始</text>
    </view>

    <!-- 登录表单 -->
    <view class="form-section">
      <view class="input-item">
        <text class="input-label">手机号</text>
        <input
          class="input-field"
          type="number"
          v-model="form.phone"
          placeholder="请输入11位手机号"
          maxlength="11"
        />
      </view>

      <view class="input-item">
        <text class="input-label">密码</text>
        <input
          class="input-field"
          type="password"
          v-model="form.password"
          placeholder="请输入密码"
          maxlength="20"
        />
      </view>

      <button
        class="login-btn"
        :loading="loading"
        :disabled="loading"
        @click="handleLogin"
      >
        {{ loading ? '登录中...' : '立即登录' }}
      </button>

      <view class="action-links">
        <text class="link" @click="goToRegister">注册账号</text>
        <text class="link" @click="goToForgot">忘记密码？</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { login } from '../../api/auth.js'
import { useUserStore } from '../../store/user.js'

const userStore = useUserStore()

const form = reactive({
  phone: '',
  password: ''
})
const loading = ref(false)

// 手机号正则
const PHONE_REGEX = /^1[3-9]\d{9}$/

/** 登录处理 */
async function handleLogin() {
  // 表单校验
  if (!form.phone) {
    return uni.showToast({ title: '请输入手机号', icon: 'none', duration: 2000 })
  }
  if (!PHONE_REGEX.test(form.phone)) {
    return uni.showToast({ title: '手机号格式不正确', icon: 'none', duration: 2000 })
  }
  if (!form.password) {
    return uni.showToast({ title: '请输入密码', icon: 'none', duration: 2000 })
  }

  loading.value = true
  try {
    // 调用后端登录接口，request.js 已解包，res 即 data: { token, user, elderId }
    const res = await login({ phone: form.phone, password: form.password })

    if (!res || !res.token) {
      throw new Error('登录响应缺少 Token')
    }

    // 存入 Pinia store（自动同步到 Storage）
    userStore.loginSuccess(res.token, res.user, res.elderId)

    uni.showToast({ title: '登录成功', icon: 'success', duration: 1500 })

    // 按 userType 分流跳转
    setTimeout(() => {
      const userType = res.user?.userType
      if (userType === 2) {
        // 管理员 → 管理后台仪表盘
        uni.reLaunch({ url: '/pages/admin/dashboard' })
      } else if (userType === 1) {
        // 家属 → 家属首页
        uni.reLaunch({ url: '/pages/family/home' })
      } else {
        // 老人（0 或 undefined）→ TabBar 首页
        uni.reLaunch({ url: '/pages/home/index' })
      }
    }, 1500)
  } catch (e) {
    console.error('登录失败：', e)
    uni.showToast({ title: e.message || '手机号或密码错误', icon: 'none', duration: 2000 })
  } finally {
    loading.value = false
  }
}

/** 跳转注册页 */
function goToRegister() {
  uni.navigateTo({ url: '/pages/login/register' })
}

/** 跳转忘记密码页 */
function goToForgot() {
  uni.navigateTo({ url: '/pages/login/forgot' })
}
</script>

<style lang="scss" scoped>
// 适老化配色：蓝绿渐变 + 大字体 + 高对比度
$primary-blue: #409eff;
$primary-green: #36cfc9;

.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, $primary-blue 0%, $primary-green 100%);
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.logo-section {
  text-align: center;
  margin-bottom: 60rpx;
}

.logo {
  width: 180rpx;
  height: 180rpx;
  border-radius: 20rpx;
}

.app-name {
  display: block;
  margin-top: 20rpx;
  font-size: 52rpx;
  font-weight: bold;
  color: #ffffff;
}

.app-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 32rpx;
  color: rgba(255, 255, 255, 0.9);
}

.form-section {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 50rpx 40rpx;
}

.input-item {
  margin-bottom: 40rpx;
}

.input-label {
  display: block;
  margin-bottom: 16rpx;
  font-size: 34rpx;
  font-weight: 600;
  color: #333333;
}

.input-field {
  height: 96rpx;
  background: #f5f7fa;
  border-radius: 16rpx;
  padding: 0 24rpx;
  font-size: 34rpx;
  color: #333333;
}

.login-btn {
  height: 96rpx;
  line-height: 96rpx;
  background: linear-gradient(135deg, $primary-blue 0%, $primary-green 100%);
  color: #ffffff;
  border-radius: 16rpx;
  font-size: 36rpx;
  font-weight: bold;
  border: none;
  margin-top: 10rpx;

  &[disabled] {
    opacity: 0.7;
  }
}

.action-links {
  display: flex;
  justify-content: space-between;
  margin-top: 30rpx;
}

.link {
  font-size: 30rpx;
  color: $primary-blue;
  padding: 10rpx 0;
}
</style>
