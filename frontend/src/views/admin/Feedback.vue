<template>
  <div class="page-card">
    <div class="page-toolbar">
      <h3>反馈管理</h3>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="loadData">
        <el-option label="待回复" :value="0" />
        <el-option label="已回复" :value="1" />
      </el-select>
    </div>

    <el-table :data="list" stripe border v-loading="loading">
      <el-table-column prop="subject" label="主题" min-width="140" />
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="submitterName" label="提交人" width="90" />
      <el-table-column prop="submitterPhone" label="手机号" width="120" />
      <el-table-column prop="submitTime" label="提交时间" width="170" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
            {{ row.status === 1 ? '已回复' : '待回复' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openReply(row)">回复</el-button>
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

    <el-dialog v-model="dialogVisible" title="回复反馈" width="520px">
      <p><strong>主题：</strong>{{ current?.subject }}</p>
      <p><strong>内容：</strong>{{ current?.content }}</p>
      <el-input v-model="replyText" type="textarea" :rows="4" placeholder="请输入回复内容" style="margin-top: 12px" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminFeedbackList, replyFeedback } from '@/api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const query = reactive({ status: 0 })
const dialogVisible = ref(false)
const current = ref(null)
const replyText = ref('')

async function loadData() {
  loading.value = true
  try {
    const res = await getAdminFeedbackList({
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

function openReply(row) {
  current.value = row
  replyText.value = row.reply || ''
  dialogVisible.value = true
}

async function submitReply() {
  if (!replyText.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  await replyFeedback(current.value.id, { reply: replyText.value })
  ElMessage.success('回复成功')
  dialogVisible.value = false
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
