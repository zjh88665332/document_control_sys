<template>
  <div class="page-card">
    <div class="page-toolbar">
      <h3>我的文件</h3>
      <div class="actions">
        <el-input v-model="keyword" placeholder="搜索文件名" clearable style="width: 200px" @keyup.enter="loadData" />
        <el-button @click="loadData">搜索</el-button>
        <el-button type="primary" @click="openUploadDialog">上传文件</el-button>
      </div>
    </div>

    <el-table :data="list" stripe border v-loading="loading">
      <el-table-column prop="name" label="文件名" min-width="180">
        <template #default="{ row }">
          <span
            class="file-name-link"
            :class="{ disabled: row.status !== 1 }"
            @click="openPreview(row)"
          >
            {{ row.name }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="format" label="格式" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
      <el-table-column prop="uploadTime" label="上传时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :disabled="row.status !== 1" @click="handleDownload(row)">下载</el-button>
          <el-button link type="primary" :disabled="row.status !== 1" @click="openShareDialog(row)">分享</el-button>
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

    <el-dialog v-model="uploadVisible" title="上传文件" width="480px" @closed="resetUploadForm">
      <el-form label-width="80px">
        <el-form-item label="选择文件" required>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-exceed="handleFileExceed"
            :on-remove="handleFileRemove"
          >
            <el-button type="primary" plain>选择文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="文件名" required>
          <el-input v-model="uploadForm.name" placeholder="请输入文件名" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="uploadForm.remark" type="textarea" :rows="2" placeholder="可选" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="submitUpload">确认上传</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="shareVisible" title="分享文件" width="420px" @closed="resetShareForm">
      <el-form label-width="80px">
        <el-form-item label="文件名">
          <span>{{ shareForm.fileName }}</span>
        </el-form-item>
        <el-form-item label="分享给" required>
          <el-select v-model="shareForm.receiverId" placeholder="请选择好友" style="width: 100%">
            <el-option
              v-for="f in friends"
              :key="f.id"
              :label="`${f.realName} (${f.phone})`"
              :value="f.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="shareForm.remark"
            type="textarea"
            :rows="3"
            placeholder="选填，可说明分享原因"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shareVisible = false">取消</el-button>
        <el-button type="primary" :loading="sharing" @click="submitShare">确认分享</el-button>
      </template>
    </el-dialog>

    <FilePreviewDialog
      v-model="previewVisible"
      :file-id="previewFile.id"
      :file-name="previewFile.name"
      :format="previewFile.format"
      :status="previewFile.status"
      @download="handleDownload(previewFile)"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FilePreviewDialog from '@/components/FilePreviewDialog.vue'
import {
  createShare,
  deleteFile,
  downloadFile,
  getFileList,
  getFriendList,
  saveBlob,
  uploadFile
} from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const uploadVisible = ref(false)
const uploading = ref(false)
const uploadRef = ref(null)
const selectedFile = ref(null)
const uploadForm = reactive({ name: '', remark: '' })

const shareVisible = ref(false)
const sharing = ref(false)
const friends = ref([])
const shareForm = reactive({ fileId: null, fileName: '', receiverId: null, remark: '' })

const previewVisible = ref(false)
const previewFile = reactive({ id: null, name: '', format: '', status: 0 })

const statusMap = { 0: '待审核', 1: '通过', 2: '拒绝' }
const statusTypeMap = { 0: 'warning', 1: 'success', 2: 'danger' }

function statusText(s) { return statusMap[s] || '未知' }
function statusType(s) { return statusTypeMap[s] || 'info' }

function stripExtension(filename) {
  const dotIndex = filename.lastIndexOf('.')
  return dotIndex > 0 ? filename.slice(0, dotIndex) : filename
}

async function loadData() {
  loading.value = true
  try {
    const res = await getFileList({
      name: keyword.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openUploadDialog() {
  uploadVisible.value = true
}

function resetUploadForm() {
  selectedFile.value = null
  uploadForm.name = ''
  uploadForm.remark = ''
  uploadRef.value?.clearFiles()
}

function handleFileChange(uploadFile) {
  selectedFile.value = uploadFile.raw
  uploadForm.name = stripExtension(uploadFile.name)
}

function handleFileRemove() {
  selectedFile.value = null
  uploadForm.name = ''
}

function handleFileExceed() {
  ElMessage.warning('每次只能上传一个文件')
}

async function submitUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要上传的文件')
    return
  }
  if (!uploadForm.name.trim()) {
    ElMessage.warning('请输入文件名')
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('name', uploadForm.name.trim())
    if (uploadForm.remark.trim()) {
      formData.append('remark', uploadForm.remark.trim())
    }
    await uploadFile(formData)
    ElMessage.success('上传成功，等待审核')
    uploadVisible.value = false
    loadData()
  } finally {
    uploading.value = false
  }
}

async function handleDownload(row) {
  const res = await downloadFile(row.id)
  saveBlob(res.data, row.name)
}

function openPreview(row) {
  if (row.status !== 1) {
    ElMessage.warning('文件未通过审核，无法预览')
    return
  }
  previewFile.id = row.id
  previewFile.name = row.name
  previewFile.format = row.format
  previewFile.status = row.status
  previewVisible.value = true
}

async function openShareDialog(row) {
  shareForm.fileId = row.id
  shareForm.fileName = row.name
  shareForm.receiverId = null
  shareForm.remark = ''
  shareVisible.value = true

  const res = await getFriendList({ pageNum: 1, pageSize: 100 })
  friends.value = res.data.list
  if (friends.value.length === 0) {
    ElMessage.warning('暂无好友，请先在好友管理中添加好友')
  }
}

function resetShareForm() {
  shareForm.fileId = null
  shareForm.fileName = ''
  shareForm.receiverId = null
  shareForm.remark = ''
}

async function submitShare() {
  if (!shareForm.receiverId) {
    ElMessage.warning('请选择好友')
    return
  }

  sharing.value = true
  try {
    await createShare({
      fileId: shareForm.fileId,
      receiverId: shareForm.receiverId,
      remark: shareForm.remark.trim() || undefined
    })
    ElMessage.success('分享成功')
    shareVisible.value = false
  } finally {
    sharing.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除文件「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteFile(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.file-name-link {
  color: var(--primary);
  cursor: pointer;
  transition: color 0.2s ease;
}

.file-name-link:hover {
  color: #00796b;
  text-decoration: underline;
}

.file-name-link.disabled {
  color: #909399;
  cursor: not-allowed;
}

.file-name-link.disabled:hover {
  text-decoration: none;
}
</style>
