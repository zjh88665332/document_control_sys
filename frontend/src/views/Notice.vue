<template>
  <div class="page-card">
    <div class="page-toolbar">
      <h3>公告列表</h3>
    </div>
    <el-table :data="list" stripe border v-loading="loading">
      <el-table-column prop="title" label="主题" min-width="160" />
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column prop="publisherName" label="发布人" width="120" />
      <el-table-column prop="publishTime" label="发布时间" width="170" />
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
import { getNoticeList } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

async function loadData() {
  loading.value = true
  try {
    const res = await getNoticeList({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
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
