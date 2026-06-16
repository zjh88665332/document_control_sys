<template>
  <div class="app-layout">
    <header class="app-header">
      <div class="app-header-left">
        <el-icon class="app-menu-toggle" @click="collapsed = !collapsed"><Fold /></el-icon>
        <span class="app-logo">在线文件管理系统</span>
      </div>
      <div class="app-header-right">
        <el-dropdown @command="handleCommand">
          <span class="app-user-info">
            {{ userStore.displayName }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="app-body">
      <aside class="app-sidebar" :class="{ collapsed }">
        <el-menu :default-active="activeMenu" :collapse="collapsed" router>
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="/notice">
            <el-icon><Bell /></el-icon>
            <template #title>
              <span class="menu-label">
                公告管理
                <span v-if="notificationStore.badges.notice" class="menu-dot" />
              </span>
            </template>
          </el-menu-item>
          <el-menu-item index="/feedback">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>
              <span class="menu-label">
                系统反馈
                <span v-if="notificationStore.badges.feedback" class="menu-dot" />
              </span>
            </template>
          </el-menu-item>
          <el-menu-item index="/file">
            <el-icon><Folder /></el-icon>
            <template #title>
              <span class="menu-label">
                文件管理
                <span v-if="notificationStore.badges.file" class="menu-dot" />
              </span>
            </template>
          </el-menu-item>
          <el-menu-item index="/friend">
            <el-icon><UserFilled /></el-icon>
            <template #title>
              <span class="menu-label">
                好友管理
                <span v-if="notificationStore.badges.friend" class="menu-dot" />
              </span>
            </template>
          </el-menu-item>
          <el-menu-item index="/share">
            <el-icon><Share /></el-icon>
            <template #title>
              <span class="menu-label">
                分享管理
                <span v-if="notificationStore.badges.share" class="menu-dot" />
              </span>
            </template>
          </el-menu-item>
          <el-menu-item index="/statistics">
            <el-icon><PieChart /></el-icon>
            <span>文件统计</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <main class="app-main">
        <div class="app-breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
          <el-button circle size="small" plain @click="refresh">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
        <router-view v-slot="{ Component }">
          <component :is="Component" :key="route.fullPath + refreshKey" />
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'
import { connectNotificationWs, disconnectNotificationWs } from '@/utils/websocket'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const notificationStore = useNotificationStore()
const collapsed = ref(false)
const refreshKey = ref(0)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')

const badgeTypeMap = {
  '/notice': 'notice',
  '/feedback': 'feedback',
  '/file': 'file',
  '/friend': 'friend',
  '/share': 'share'
}

function handleWsMessage(msg) {
  notificationStore.fetchBadges()
  if (msg?.title) {
    ElNotification({ title: msg.title, message: msg.content || '', type: 'info', duration: 4500 })
  }
}

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (token) {
    userStore.fetchProfile().catch(() => {
      localStorage.removeItem('token')
      userStore.clear()
      router.push('/login')
    })
    await notificationStore.fetchBadges()
    connectNotificationWs(handleWsMessage)
    const type = badgeTypeMap[route.path]
    if (type) {
      await notificationStore.markRead(type)
    }
  }
})

onBeforeUnmount(() => {
  disconnectNotificationWs()
})

watch(
  () => route.path,
  (path) => {
    notificationStore.fetchBadges()
    const type = badgeTypeMap[path]
    if (type) {
      notificationStore.markRead(type)
    }
  }
)

function refresh() {
  refreshKey.value++
  notificationStore.fetchBadges()
}

function handleCommand(command) {
  if (command === 'logout') {
    userStore.clear()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>
