<template>
  <div class="page-card">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="我的好友" name="friends">
        <div class="toolbar">
          <el-input v-model="nameFilter" placeholder="姓名" clearable style="width: 140px" />
          <el-input v-model="phoneFilter" placeholder="手机号" clearable style="width: 160px" />
          <el-button type="primary" @click="loadFriends">搜索</el-button>
        </div>
        <el-table :data="friends" stripe border v-loading="loadingFriends">
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="phone" label="手机号" width="140" />
          <el-table-column label="添加时间" width="170">
            <template #default="{ row }">{{ formatTime(row.addTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button link type="danger" @click="handleDeleteFriend(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="搜索添加" name="search">
        <div class="toolbar">
          <el-input v-model="keyword" placeholder="姓名或手机号" style="width: 240px" @keyup.enter="handleSearch" />
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
        <el-table :data="searchResults" stripe border>
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="phone" label="手机号" width="140" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="openApply(row)">加好友</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="收到的申请" name="received">
        <el-table :data="receivedList" stripe border v-loading="loadingReceived">
          <el-table-column prop="applicantName" label="申请人" width="100" />
          <el-table-column prop="applicantPhone" label="手机号" width="130" />
          <el-table-column prop="applyMessage" label="验证信息" min-width="160" show-overflow-tooltip />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="applyStatusType(row.status)">{{ applyStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 0">
                <el-button link type="primary" @click="handleApply(row, 1)">同意</el-button>
                <el-button link type="danger" @click="handleApply(row, 2)">拒绝</el-button>
              </template>
              <span v-else>{{ row.replyMessage || '-' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="发出的申请" name="sent">
        <el-table :data="sentList" stripe border v-loading="loadingSent">
          <el-table-column prop="targetName" label="目标用户" width="100" />
          <el-table-column prop="targetPhone" label="手机号" width="130" />
          <el-table-column prop="applyMessage" label="验证信息" min-width="160" show-overflow-tooltip />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="applyStatusType(row.status)">{{ applyStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="replyMessage" label="回复" min-width="120" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="applyVisible" title="发送好友申请" width="420px">
      <el-form label-width="90px">
        <el-form-item label="验证信息">
          <el-input v-model="applyMessage" type="textarea" :rows="3" placeholder="你好，可以加个好友吗？" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  applyFriend,
  deleteFriend,
  getFriendList,
  getReceivedApplies,
  getSentApplies,
  handleApply as handleApplyApi,
  searchFriend
} from '@/api'

const activeTab = ref('friends')
const loadingFriends = ref(false)
const loadingReceived = ref(false)
const loadingSent = ref(false)

const friends = ref([])
const searchResults = ref([])
const receivedList = ref([])
const sentList = ref([])

const nameFilter = ref('')
const phoneFilter = ref('')
const keyword = ref('')

const applyVisible = ref(false)
const applyMessage = ref('')
const applyTarget = ref(null)

function formatTime(ts) {
  if (!ts) return '-'
  return new Date(ts).toLocaleString('zh-CN')
}

function applyStatusText(s) {
  return { 0: '待处理', 1: '已通过', 2: '已拒绝' }[s] || '未知'
}
function applyStatusType(s) {
  return { 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info'
}

async function loadFriends() {
  loadingFriends.value = true
  try {
    const res = await getFriendList({
      name: nameFilter.value || undefined,
      phone: phoneFilter.value || undefined,
      pageNum: 1,
      pageSize: 100
    })
    friends.value = res.data.list
  } finally {
    loadingFriends.value = false
  }
}

async function handleSearch() {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  const res = await searchFriend(keyword.value.trim())
  searchResults.value = res.data
}

function openApply(row) {
  applyTarget.value = row
  applyMessage.value = ''
  applyVisible.value = true
}

async function submitApply() {
  try {
    await applyFriend({ friendId: applyTarget.value.id, applyMessage: applyMessage.value })
    ElMessage.success('申请已发送')
    applyVisible.value = false
  } catch {
    // 错误信息由请求拦截器提示
  }
}

async function loadReceived() {
  loadingReceived.value = true
  try {
    const res = await getReceivedApplies({ pageNum: 1, pageSize: 50 })
    receivedList.value = res.data.list
  } finally {
    loadingReceived.value = false
  }
}

async function loadSent() {
  loadingSent.value = true
  try {
    const res = await getSentApplies({ pageNum: 1, pageSize: 50 })
    sentList.value = res.data.list
  } catch {
    sentList.value = []
  } finally {
    loadingSent.value = false
  }
}

async function handleApply(row, status) {
  const action = status === 1 ? '同意' : '拒绝'
  await ElMessageBox.confirm(`确定${action}该好友申请吗？`, '提示')
  await handleApplyApi(row.id, { status, replyMessage: status === 1 ? '已同意' : '已拒绝' })
  ElMessage.success('处理成功')
  loadReceived()
  loadFriends()
}

async function handleDeleteFriend(row) {
  await ElMessageBox.confirm(`确定删除好友「${row.realName}」吗？`, '提示', { type: 'warning' })
  await deleteFriend(row.id)
  ElMessage.success('删除成功')
  loadFriends()
}

watch(activeTab, (tab) => {
  if (tab === 'friends') loadFriends()
  if (tab === 'received') loadReceived()
  if (tab === 'sent') loadSent()
})

onMounted(loadFriends)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
</style>
