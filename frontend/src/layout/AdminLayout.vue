<template>
  <div class="app-layout">
    <header class="app-header">
      <div class="app-header-left">
        <el-icon class="app-menu-toggle" @click="collapsed = !collapsed"><Fold /></el-icon>
        <span class="app-logo">在线文件管理系统 · 管理端</span>
      </div>
      <div class="app-header-right">
        <el-tag size="small" type="success" effect="plain" style="margin-right: 12px">{{ roleLabel }}</el-tag>
        <el-dropdown @command="handleCommand">
          <span class="app-user-info">
            {{ userStore.displayName }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="app-body">
      <aside class="app-sidebar" :class="{ collapsed }">
        <el-menu :default-active="activeMenu" :collapse="collapsed" router>
          <el-menu-item index="/admin/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>控制台</span>
          </el-menu-item>
          <el-menu-item index="/admin/user">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.isSuper" index="/admin/admin-account">
            <el-icon><UserFilled /></el-icon>
            <span>管理员管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/notice">
            <el-icon><Bell /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/file">
            <el-icon><Folder /></el-icon>
            <template #title>
              <span class="menu-label">
                文件审核
                <span v-if="pendingFileCount > 0" class="menu-dot" />
              </span>
            </template>
          </el-menu-item>
          <el-menu-item index="/admin/feedback">
            <el-icon><ChatDotRound /></el-icon>
            <span>反馈管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/operation-log">
            <el-icon><Document /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <main class="app-main">
        <div class="app-breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>管理端</el-breadcrumb-item>
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
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getPendingFileCount } from '@/api/admin'
import { connectNotificationWs, disconnectNotificationWs } from '@/utils/websocket'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)
const refreshKey = ref(0)
const pendingFileCount = ref(0)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')
const roleLabel = computed(() => (userStore.isSuper ? '超级管理员' : '管理员'))

async function loadPendingCount() {
  try {
    const res = await getPendingFileCount()
    pendingFileCount.value = res.data || 0
  } catch {
    pendingFileCount.value = 0
  }
}

function handleWsMessage(msg) {
  loadPendingCount()
  if (msg?.type === 'admin_file' && msg.title) {
    ElNotification({ title: msg.title, message: msg.content || '', type: 'warning', duration: 4500 })
  }
}

function refresh() {
  refreshKey.value++
  loadPendingCount()
}

function handleCommand(command) {
  if (command === 'logout') {
    disconnectNotificationWs()
    userStore.clear()
    router.push('/login')
  }
}

onMounted(() => {
  loadPendingCount()
  connectNotificationWs(handleWsMessage)
})

onBeforeUnmount(() => {
  disconnectNotificationWs()
})
</script>
