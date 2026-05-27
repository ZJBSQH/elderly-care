<template>
  <view class="page-container">
    <view class="form-card">
      <!-- 姓名 -->
      <view class="form-item">
        <text class="form-label">姓名</text>
        <input class="form-input" v-model="form.name" placeholder="请输入姓名" maxlength="20" />
      </view>

      <!-- 年龄 -->
      <view class="form-item">
        <text class="form-label">年龄</text>
        <input class="form-input" type="number" v-model="ageStr" placeholder="请输入年龄" maxlength="3" />
      </view>

      <!-- 性别 -->
      <view class="form-item">
        <text class="form-label">性别</text>
        <view class="radio-row">
          <view class="radio-item" :class="{ active: form.sex === '1' }" @click="form.sex = '1'">
            <text>男</text>
          </view>
          <view class="radio-item" :class="{ active: form.sex === '0' }" @click="form.sex = '0'">
            <text>女</text>
          </view>
        </view>
      </view>

      <button class="submit-btn" :loading="loading" @click="handleSubmit">保存修改</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../store/user.js'
import { updateProfile, getUserInfo } from '../../api/auth.js'

const userStore = useUserStore()

const loading = ref(false)
const ageStr = ref('')

const form = reactive({
  name: '',
  sex: '1',
  age: null
})

/** 加载当前信息 */
async function loadProfile() {
  try {
    const data = await getUserInfo()
    const u = data?.user || data || userStore.userInfo || {}
    form.name = u.name || ''
    form.sex = u.sex !== undefined ? String(u.sex) : '1'
    form.age = u.age || null
    ageStr.value = u.age ? String(u.age) : ''
  } catch (e) {
    // 降级使用 store 数据
    const u = userStore.userInfo || {}
    form.name = u.name || ''
    form.sex = u.sex !== undefined ? String(u.sex) : '1'
    form.age = u.age || null
    ageStr.value = u.age ? String(u.age) : ''
  }
}

async function handleSubmit() {
  const name = form.name.trim()
  if (!name) return uni.showToast({ title: '请输入姓名', icon: 'none', duration: 2000 })

  const age = ageStr.value ? parseInt(ageStr.value, 10) : null
  if (age !== null && (age < 1 || age > 150)) {
    return uni.showToast({ title: '请输入有效年龄', icon: 'none', duration: 2000 })
  }

  const userId = userStore.userId
  if (!userId) return uni.showToast({ title: '无法获取用户信息', icon: 'none', duration: 2000 })

  loading.value = true
  try {
    await updateProfile({
      id: userId,
      name,
      age,
      sex: form.sex
    })
    // 更新 Pinia store
    userStore.setUserInfo({ name, age, sex: form.sex })
    uni.showToast({ title: '保存成功', icon: 'success', duration: 1500 })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none', duration: 2000 })
  } finally {
    loading.value = false
  }
}

onMounted(loadProfile)
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

.radio-row {
  display: flex;
  gap: 20rpx;
}

.radio-item {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background: #f5f7fa;
  border-radius: $elder-radius;
  font-size: $elder-font-base;
  color: $color-text-light;
  border: 2rpx solid transparent;

  &.active {
    background: rgba(64, 158, 255, 0.1);
    border-color: $color-primary;
    color: $color-primary;
    font-weight: bold;
  }
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
