<template>
  <div class="login-page">
    <div class="login-box">
      <h2>在线文件管理系统</h2>
      <p class="subtitle">用户登录</p>
      <p class="hint">用户：2016001 / 123456 &nbsp;|&nbsp; 管理员：admin / 123456 &nbsp;|&nbsp; 超管：super / 123456</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
        <div class="footer-link">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '2016001',
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    userStore.setAuth({
      token: res.data.token,
      role: res.data.role,
      realName: res.data.realName
    })
    ElMessage.success('登录成功')
    if (res.data.role === 'admin' || res.data.role === 'super') {
      router.push('/admin/dashboard')
    } else {
      await userStore.fetchProfile()
      router.push('/profile')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #e6f4f1 0%, #f5f7fa 50%, #e8f5f3 100%);
}

.login-box {
  width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 36px 32px;
  border: 1px solid #ebeef5;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
}

.login-box h2 {
  margin: 0 0 8px;
  text-align: center;
  color: #303133;
  font-size: 22px;
  font-weight: 600;
}

.subtitle {
  text-align: center;
  color: #909399;
  margin: 0 0 8px;
  font-size: 14px;
}

.hint {
  text-align: center;
  color: var(--primary);
  margin: 0 0 24px;
  font-size: 12px;
  line-height: 1.6;
  padding: 8px 12px;
  background: var(--primary-light);
  border-radius: 8px;
}

.login-btn {
  width: 100%;
}

.footer-link {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.footer-link a {
  color: var(--primary);
  text-decoration: none;
}

.footer-link a:hover {
  text-decoration: underline;
}
</style>
