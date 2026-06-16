<template>
  <div class="page-card">
    <div class="page-toolbar">
      <h3>回收站</h3>
      <el-button @click="$router.push('/file')">返回文件管理</el-button>
    </div>

    <el-table :data="list" stripe border v-loading="loading">
      <el-table-column prop="name" label="文件名" min-width="180" />
      <el-table-column prop="format" label="格式" width="80" />
      <el-table-column prop="deleteTime" label="删除时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleRestore(row)">恢复</el-button>
          <el-button link type="danger" @click="handlePermanentDelete(row)">彻底删除</el-button>
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
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRecycleBinList, permanentDeleteFile, restoreFile } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

async function loadData() {
  loading.value = true
  try {
    const res = await getRecycleBinList({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handleRestore(row) {
  await ElMessageBox.confirm(`确定恢复文件「${row.name}」吗？`, '提示')
  await restoreFile(row.id)
  ElMessage.success('恢复成功')
  loadData()
}

async function handlePermanentDelete(row) {
  await ElMessageBox.confirm(`彻底删除后无法恢复，确定删除「${row.name}」吗？`, '警告', { type: 'warning' })
  await permanentDeleteFile(row.id)
  ElMessage.success('已彻底删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
