<template>
  <div class="page-card">
    <div class="page-toolbar">
      <h3>普通用户管理</h3>
      <div class="filters">
        <el-input v-model="query.username" placeholder="用户名" clearable style="width: 130px" />
        <el-input v-model="query.realName" placeholder="姓名" clearable style="width: 130px" />
        <el-input v-model="query.phone" placeholder="手机号" clearable style="width: 140px" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 110px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>
    </div>

    <el-table :data="list" stripe border v-loading="loading">
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button link type="warning" @click="handleReset(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUserList, resetUserPassword, updateUserStatus } from '@/api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const query = reactive({ username: '', realName: '', phone: '', status: null })

async function loadData() {
  loading.value = true
  try {
    const res = await getAdminUserList({
      ...query,
      status: query.status ?? undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${action}用户「${row.realName || row.username}」吗？`, '提示', { type: 'warning' })
  await updateUserStatus(row.id, { status: newStatus })
  ElMessage.success('操作成功')
  loadData()
}

async function handleReset(row) {
  await ElMessageBox.confirm(`确定将「${row.realName || row.username}」的密码重置为 123456 吗？`, '提示')
  await resetUserPassword(row.id)
  ElMessage.success('密码已重置')
}

onMounted(loadData)
</script>

<style scoped>
.filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
