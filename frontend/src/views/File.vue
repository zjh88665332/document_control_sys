<template>
  <div class="file-page">
    <aside class="folder-panel page-card">
      <div class="folder-header">
        <span>文件夹</span>
        <el-button link type="primary" @click="openCreateFolder">新建</el-button>
      </div>
      <el-tree
        :data="folderTree"
        node-key="id"
        :props="{ label: 'name', children: 'children' }"
        highlight-current
        default-expand-all
        @node-click="handleFolderClick"
      />
    </aside>

    <div class="file-main page-card">
      <div class="page-toolbar">
        <h3>我的文件</h3>
        <div class="actions">
          <el-input
            v-model="keyword"
            placeholder="智能搜索：文件名/标签/备注/内容"
            clearable
            style="width: 260px"
            @keyup.enter="loadData"
          />
          <el-button @click="loadData">搜索</el-button>
          <el-button @click="$router.push('/recycle')">回收站</el-button>
          <el-button type="primary" @click="openUploadDialog">上传文件</el-button>
        </div>
      </div>

      <el-table :data="list" stripe border v-loading="loading">
        <el-table-column prop="name" label="文件名" min-width="160">
          <template #default="{ row }">
            <span class="file-name-link" :class="{ disabled: row.status !== 1 }" @click="openPreview(row)">
              {{ row.name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="format" label="格式" width="70" />
        <el-table-column label="智能标签" min-width="140">
          <template #default="{ row }">
            <el-tag v-for="tag in parseTags(row.tags)" :key="tag" size="small" style="margin: 2px">{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="驳回原因" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.status === 2" class="reject-reason">{{ row.auditRejectReason || '-' }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="uploadTime" label="上传时间" width="165" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.status !== 1" @click="handleDownload(row)">下载</el-button>
            <el-button link type="primary" :disabled="row.status !== 1" @click="openShareDialog(row)">分享</el-button>
            <el-button link type="primary" @click="openMoveDialog(row)">移动</el-button>
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
    </div>

    <el-dialog v-model="uploadVisible" title="上传文件" width="480px" @closed="resetUploadForm">
      <el-form label-width="80px">
        <el-form-item label="文件夹">
          <el-select v-model="uploadForm.folderId" placeholder="默认根目录" clearable style="width: 100%">
            <el-option v-for="f in flatFolders" :key="f.id" :label="f.label" :value="f.id" />
          </el-select>
        </el-form-item>
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
            <template #tip>
              <div class="upload-tip">支持文档、图片、视频等，单文件最大 200MB</div>
            </template>
          </el-upload>
          <el-progress v-if="uploading && uploadProgress > 0" :percentage="uploadProgress" style="margin-top: 8px" />
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

    <el-dialog v-model="folderVisible" title="新建文件夹" width="400px">
      <el-form label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="folderForm.name" maxlength="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="folderVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreateFolder">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="moveVisible" title="移动文件" width="400px">
      <el-select v-model="moveForm.folderId" placeholder="选择目标文件夹" clearable style="width: 100%">
        <el-option label="根目录" :value="0" />
        <el-option v-for="f in flatFolders" :key="f.id" :label="f.label" :value="f.id" />
      </el-select>
      <template #footer>
        <el-button @click="moveVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMove">确认移动</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="shareVisible" title="分享文件" width="420px" @closed="resetShareForm">
      <el-form label-width="80px">
        <el-form-item label="文件名"><span>{{ shareForm.fileName }}</span></el-form-item>
        <el-form-item label="分享给" required>
          <el-select v-model="shareForm.receiverId" placeholder="请选择好友" style="width: 100%">
            <el-option v-for="f in friends" :key="f.id" :label="`${f.realName} (${f.phone})`" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="shareForm.remark" type="textarea" :rows="3" maxlength="255" show-word-limit />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FilePreviewDialog from '@/components/FilePreviewDialog.vue'
import {
  createFolder,
  createShare,
  deleteFile,
  downloadFile,
  getFileList,
  getFolderTree,
  getFriendList,
  MAX_UPLOAD_SIZE,
  moveFile,
  saveBlob,
  uploadFile
} from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const currentFolderId = ref(null)
const folderTree = ref([])

const uploadVisible = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadRef = ref(null)
const selectedFile = ref(null)
const uploadForm = reactive({ name: '', remark: '', folderId: null })

const folderVisible = ref(false)
const folderForm = reactive({ name: '' })

const moveVisible = ref(false)
const moveForm = reactive({ fileId: null, folderId: 0 })

const shareVisible = ref(false)
const sharing = ref(false)
const friends = ref([])
const shareForm = reactive({ fileId: null, fileName: '', receiverId: null, remark: '' })

const previewVisible = ref(false)
const previewFile = reactive({ id: null, name: '', format: '', status: 0 })

const statusMap = { 0: '待审核', 1: '通过', 2: '拒绝' }
const statusTypeMap = { 0: 'warning', 1: 'success', 2: 'danger' }

const flatFolders = computed(() => {
  const result = []
  function walk(nodes, prefix = '') {
    for (const node of nodes) {
      if (node.id > 0) {
        result.push({ id: node.id, label: prefix + node.name })
        if (node.children?.length) walk(node.children, prefix + node.name + ' / ')
      } else if (node.children?.length) {
        walk(node.children, prefix)
      }
    }
  }
  walk(folderTree.value)
  return result
})

function statusText(s) { return statusMap[s] || '未知' }
function statusType(s) { return statusTypeMap[s] || 'info' }
function parseTags(tags) {
  if (!tags) return []
  return tags.split(',').filter(Boolean).slice(0, 5)
}

function stripExtension(filename) {
  const dotIndex = filename.lastIndexOf('.')
  return dotIndex > 0 ? filename.slice(0, dotIndex) : filename
}

async function loadFolders() {
  const res = await getFolderTree()
  folderTree.value = res.data
}

async function loadData() {
  loading.value = true
  try {
    const res = await getFileList({
      keyword: keyword.value || undefined,
      folderId: currentFolderId.value ?? undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleFolderClick(node) {
  currentFolderId.value = node.id === 0 ? null : node.id
  pageNum.value = 1
  loadData()
}

function openCreateFolder() {
  folderForm.name = ''
  folderVisible.value = true
}

async function submitCreateFolder() {
  if (!folderForm.name.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }
  await createFolder({ name: folderForm.name.trim(), parentId: currentFolderId.value || 0 })
  ElMessage.success('创建成功')
  folderVisible.value = false
  loadFolders()
}

function openUploadDialog() {
  uploadForm.folderId = currentFolderId.value
  uploadVisible.value = true
}

function resetUploadForm() {
  selectedFile.value = null
  uploadForm.name = ''
  uploadForm.remark = ''
  uploadForm.folderId = currentFolderId.value
  uploadRef.value?.clearFiles()
}

function handleFileChange(uploadFile) {
  const file = uploadFile.raw
  if (file && file.size > MAX_UPLOAD_SIZE) {
    ElMessage.warning('文件不能超过 200MB，请选择较小的视频或压缩后再上传')
    uploadRef.value?.clearFiles()
    selectedFile.value = null
    uploadForm.name = ''
    return
  }
  selectedFile.value = file
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

  if (selectedFile.value.size > MAX_UPLOAD_SIZE) {
    ElMessage.warning('文件不能超过 200MB')
    return
  }

  uploading.value = true
  uploadProgress.value = 0
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('name', uploadForm.name.trim())
    if (uploadForm.remark.trim()) formData.append('remark', uploadForm.remark.trim())
    if (uploadForm.folderId) formData.append('folderId', uploadForm.folderId)
    await uploadFile(formData, (event) => {
      if (event.total) {
        uploadProgress.value = Math.round((event.loaded / event.total) * 100)
      }
    })
    ElMessage.success('上传成功，等待审核')
    uploadVisible.value = false
    loadData()
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

function openMoveDialog(row) {
  moveForm.fileId = row.id
  moveForm.folderId = row.folderId || 0
  moveVisible.value = true
}

async function submitMove() {
  await moveFile(moveForm.fileId, { folderId: moveForm.folderId || null })
  ElMessage.success('移动成功')
  moveVisible.value = false
  loadData()
}

async function handleDownload(row) {
  const res = await downloadFile(row.id)
  saveBlob(res.data, row.name)
}

function openPreview(row) {
  if (row.status !== 1) {
    ElMessage.warning(row.status === 2 ? `文件被驳回：${row.auditRejectReason || '无原因'}` : '文件未通过审核，无法预览')
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
  if (friends.value.length === 0) ElMessage.warning('暂无好友，请先在好友管理中添加好友')
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
  await ElMessageBox.confirm(`确定删除文件「${row.name}」吗？文件将移入回收站。`, '提示', { type: 'warning' })
  await deleteFile(row.id)
  ElMessage.success('已移入回收站')
  loadData()
}

onMounted(async () => {
  await loadFolders()
  loadData()
})
</script>

<style scoped>
.file-page {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.folder-panel {
  width: 220px;
  flex-shrink: 0;
  padding: 14px;
}

.folder-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-weight: 600;
}

.file-main {
  flex: 1;
  min-width: 0;
}

.actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.file-name-link {
  color: var(--primary);
  cursor: pointer;
}

.file-name-link.disabled {
  color: #909399;
  cursor: not-allowed;
}

.reject-reason {
  color: #f56c6c;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
</style>
