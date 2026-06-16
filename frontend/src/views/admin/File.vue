<template>
  <div class="page-card">
    <div class="page-toolbar">
      <h3>文件审核</h3>
      <div class="filters">
        <el-input v-model="query.name" placeholder="文件名" clearable style="width: 160px" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>
    </div>

    <el-table :data="list" stripe border v-loading="loading">
      <el-table-column prop="name" label="文件名" min-width="150" />
      <el-table-column prop="format" label="格式" width="70" />
      <el-table-column label="标签" min-width="120">
        <template #default="{ row }">
          <el-tag v-for="tag in parseTags(row.tags)" :key="tag" size="small" style="margin: 2px">{{ tag }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="uploaderName" label="上传人" width="90" />
      <el-table-column prop="uploaderPhone" label="手机号" width="120" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditRejectReason" label="驳回原因" min-width="120" show-overflow-tooltip />
      <el-table-column prop="uploadTime" label="上传时间" width="165" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDownload(row)">下载</el-button>
          <template v-if="row.status === 0">
            <el-button link type="success" @click="handleAudit(row, 1)">通过</el-button>
            <el-button link type="warning" @click="openRejectDialog(row)">拒绝</el-button>
          </template>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="rejectVisible" title="驳回文件" width="440px">
      <p style="margin: 0 0 12px">文件：{{ rejectForm.fileName }}</p>
      <el-input
        v-model="rejectForm.reason"
        type="textarea"
        :rows="4"
        placeholder="请填写驳回原因（必填）"
        maxlength="500"
        show-word-limit
      />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="warning" @click="submitReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminDeleteFile, auditFile, downloadAdminFile, getAdminFileList } from '@/api/admin'

const route = useRoute()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const query = reactive({ name: '', status: route.query.status !== undefined ? Number(route.query.status) : 0 })

const rejectVisible = ref(false)
const rejectForm = reactive({ id: null, fileName: '', reason: '' })

const statusMap = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
const statusTypeMap = { 0: 'warning', 1: 'success', 2: 'danger' }
function statusText(s) { return statusMap[s] || '未知' }
function statusType(s) { return statusTypeMap[s] || 'info' }
function parseTags(tags) {
  if (!tags) return []
  return tags.split(',').filter(Boolean).slice(0, 4)
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAdminFileList({
      name: query.name || undefined,
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

async function handleAudit(row, status) {
  await ElMessageBox.confirm(`确定通过文件「${row.name}」吗？`, '审核')
  await auditFile(row.id, { status })
  ElMessage.success('审核成功')
  loadData()
}

function openRejectDialog(row) {
  rejectForm.id = row.id
  rejectForm.fileName = row.name
  rejectForm.reason = ''
  rejectVisible.value = true
}

async function submitReject() {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  await auditFile(rejectForm.id, { status: 2, rejectReason: rejectForm.reason.trim() })
  ElMessage.success('已驳回')
  rejectVisible.value = false
  loadData()
}

async function handleDownload(row) {
  await downloadAdminFile(row.id, row.name)
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除文件「${row.name}」吗？`, '提示', { type: 'warning' })
  await adminDeleteFile(row.id)
  ElMessage.success('删除成功')
  loadData()
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
