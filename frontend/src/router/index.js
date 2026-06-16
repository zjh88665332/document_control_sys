import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/profile',
    meta: { requiresUser: true },
    children: [
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人信息' } },
      { path: 'notice', name: 'Notice', component: () => import('@/views/Notice.vue'), meta: { title: '公告管理' } },
      { path: 'feedback', name: 'Feedback', component: () => import('@/views/Feedback.vue'), meta: { title: '系统反馈' } },
      { path: 'file', name: 'File', component: () => import('@/views/File.vue'), meta: { title: '文件管理' } },
      { path: 'recycle', name: 'RecycleBin', component: () => import('@/views/RecycleBin.vue'), meta: { title: '回收站' } },
      { path: 'friend', name: 'Friend', component: () => import('@/views/Friend.vue'), meta: { title: '好友管理' } },
      { path: 'share', name: 'Share', component: () => import('@/views/Share.vue'), meta: { title: '分享管理' } },
      { path: 'statistics', name: 'Statistics', component: () => import('@/views/Statistics.vue'), meta: { title: '文件统计' } }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAdmin: true },
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '控制台' } },
      { path: 'user', name: 'AdminUser', component: () => import('@/views/admin/User.vue'), meta: { title: '用户管理' } },
      { path: 'admin-account', name: 'AdminAccount', component: () => import('@/views/admin/AdminAccount.vue'), meta: { title: '管理员管理', requiresSuper: true } },
      { path: 'notice', name: 'AdminNotice', component: () => import('@/views/admin/Notice.vue'), meta: { title: '公告管理' } },
      { path: 'file', name: 'AdminFile', component: () => import('@/views/admin/File.vue'), meta: { title: '文件审核' } },
      { path: 'feedback', name: 'AdminFeedback', component: () => import('@/views/admin/Feedback.vue'), meta: { title: '反馈管理' } },
      { path: 'operation-log', name: 'AdminOperationLog', component: () => import('@/views/admin/OperationLog.vue'), meta: { title: '操作日志' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

function isAdminRole(role) {
  return role === 'admin' || role === 'super'
}

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.meta.public) {
    return true
  }

  if (!token) {
    return '/login'
  }

  if (to.meta.requiresAdmin && !isAdminRole(role)) {
    return '/profile'
  }

  if (to.meta.requiresUser && isAdminRole(role)) {
    return '/admin/dashboard'
  }

  if (to.meta.requiresSuper && role !== 'super') {
    return '/admin/dashboard'
  }
})

export default router
