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
            <span>文件审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/feedback">
            <el-icon><ChatDotRound /></el-icon>
            <span>反馈管理</span>
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
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)
const refreshKey = ref(0)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')
const roleLabel = computed(() => (userStore.isSuper ? '超级管理员' : '管理员'))

function refresh() {
  refreshKey.value++
}

function handleCommand(command) {
  if (command === 'logout') {
    userStore.clear()
    router.push('/login')
  }
}
</script>
