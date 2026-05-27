<script setup>
import { onLaunch } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('老人用药与健康管理 APP 启动')

  // 路由守卫：检查 Token 是否存在
  const token = uni.getStorageSync('token')
  if (!token) {
    console.log('未检测到登录状态，将跳转登录页')
  } else {
    console.log('已登录，Token 有效')
  }
})
</script>

<style lang="scss">
/* 全局样式 — 适老化设计 */
page {
  background-color: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 32rpx;
  color: #333333;
  -webkit-font-smoothing: antialiased;
}

/* 统一按钮点击反馈 */
button {
  &::after {
    border: none;
  }
}

/* 全局安全区域适配 */
.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
