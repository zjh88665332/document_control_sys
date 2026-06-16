<template>
  <div class="page-card">
    <div class="page-toolbar">
      <h3>操作日志</h3>
      <div class="filters">
        <el-select v-model="query.module" placeholder="模块" clearable style="width: 140px">
          <el-option label="文件" value="文件" />
          <el-option label="文件审核" value="文件审核" />
          <el-option label="回收站" value="回收站" />
          <el-option label="文件夹" value="文件夹" />
        </el-select>
        <el-input v-model="query.username" placeholder="操作人" clearable style="width: 140px" />
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>
    </div>

    <el-table :data="list" stripe border v-loading="loading">
      <el-table-column prop="username" label="操作人" width="100" />
      <el-table-column prop="module" label="模块" width="100" />
      <el-table-column prop="action" label="操作" width="100" />
      <el-table-column prop="targetName" label="目标" min-width="140" show-overflow-tooltip />
      <el-table-column prop="detail" label="详情" min-width="180" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="130" />
      <el-table-column prop="createTime" label="时间" width="170" />
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
import { getOperationLogList } from '@/api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const query = reactive({ module: '', username: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await getOperationLogList({
      module: query.module || undefined,
      username: query.username || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.filters {
  display: flex;
  gap: 8px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
