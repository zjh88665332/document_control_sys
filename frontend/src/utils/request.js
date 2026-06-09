import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

let handling401 = false

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response
    }
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    const url = error.config?.url || ''
    const msg = error.response?.data?.msg

    if (status === 401) {
      if (url.includes('/auth/login')) {
        ElMessage.error(msg || '用户名或密码错误')
      } else if (!handling401) {
        handling401 = true
        localStorage.removeItem('token')
        localStorage.removeItem('role')
        localStorage.removeItem('realName')
        if (router.currentRoute.value.path !== '/login') {
          ElMessage.error(msg || '登录已过期，请重新登录')
          router.push('/login').finally(() => {
            handling401 = false
          })
        } else {
          handling401 = false
        }
      }
    } else if (status === 404) {
      console.warn('接口不存在:', url)
    } else {
      ElMessage.error(msg || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
