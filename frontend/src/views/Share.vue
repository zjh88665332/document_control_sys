<template>
  <div class="page-card">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="分享文件" name="create">
        <el-form label-width="90px" style="max-width: 520px">
          <el-form-item label="选择文件">
            <el-select v-model="shareForm.fileId" placeholder="请选择已通过审核的文件" style="width: 100%">
              <el-option v-for="f in myFiles" :key="f.id" :label="f.name" :value="f.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="分享给">
            <el-select v-model="shareForm.receiverId" placeholder="请选择好友" style="width: 100%">
              <el-option v-for="f in friends" :key="f.id" :label="`${f.realName} (${f.phone})`" :value="f.id" />
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
          <el-form-item>
            <el-button type="primary" :loading="sharing" @click="handleShare">分享</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="分享给我" name="received">
        <el-table :data="receivedList" stripe border v-loading="loadingReceived">
          <el-table-column prop="fileName" label="文件名" min-width="160">
            <template #default="{ row }">
              <span
                class="file-name-link"
                :class="{ disabled: row.status !== 1 }"
                @click="openPreview(row)"
              >
                {{ row.fileName }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="fileType" label="类型" width="80" />
          <el-table-column prop="sharerName" label="分享人" width="100" />
          <el-table-column prop="sharerPhone" label="手机号" width="130" />
          <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
          <el-table-column prop="shareTime" label="分享时间" width="170" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.status !== 1" @click="handleDownload(row)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="我的分享" name="sent">
        <el-table :data="sentList" stripe border v-loading="loadingSent">
          <el-table-column prop="fileName" label="文件名" min-width="160">
            <template #default="{ row }">
              <span
                class="file-name-link"
                :class="{ disabled: row.status !== 1 }"
                @click="openPreview(row)"
              >
                {{ row.fileName }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="fileType" label="类型" width="80" />
          <el-table-column prop="receiverName" label="接收人" width="100" />
          <el-table-column prop="receiverPhone" label="手机号" width="130" />
          <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
          <el-table-column prop="shareTime" label="分享时间" width="170" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button link type="danger" :disabled="row.status !== 1" @click="handleCancel(row)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <FilePreviewDialog
      v-model="previewVisible"
      :share-id="previewShare.id"
      :file-name="previewShare.fileName"
      :format="previewShare.fileFormat"
      :status="previewShare.status"
      invalid-message="分享已失效，无法预览"
      @download="handleDownload(previewShare)"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FilePreviewDialog from '@/components/FilePreviewDialog.vue'
import {
  cancelShare,
  createShare,
  downloadShare,
  getFileList,
  getFriendList,
  getReceivedShares,
  getSentShares,
  saveBlob
} from '@/api'

const activeTab = ref('received')
const sharing = ref(false)
const loadingReceived = ref(false)
const loadingSent = ref(false)

const myFiles = ref([])
const friends = ref([])
const receivedList = ref([])
const sentList = ref([])

const shareForm = reactive({ fileId: null, receiverId: null, remark: '' })

const previewVisible = ref(false)
const previewShare = reactive({ id: null, fileName: '', fileFormat: '', status: 0 })

async function loadOptions() {
  const [fileRes, friendRes] = await Promise.all([
    getFileList({ pageNum: 1, pageSize: 100 }),
    getFriendList({ pageNum: 1, pageSize: 100 })
  ])
  myFiles.value = fileRes.data.list.filter((f) => f.status === 1)
  friends.value = friendRes.data.list
}

async function handleShare() {
  if (!shareForm.fileId || !shareForm.receiverId) {
    ElMessage.warning('请选择文件和好友')
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
    shareForm.fileId = null
    shareForm.receiverId = null
    shareForm.remark = ''
    activeTab.value = 'sent'
    loadSent()
  } finally {
    sharing.value = false
  }
}

async function loadReceived() {
  loadingReceived.value = true
  try {
    const res = await getReceivedShares({ pageNum: 1, pageSize: 50 })
    receivedList.value = res.data.list
  } finally {
    loadingReceived.value = false
  }
}

async function loadSent() {
  loadingSent.value = true
  try {
    const res = await getSentShares({ pageNum: 1, pageSize: 50 })
    sentList.value = res.data.list
  } finally {
    loadingSent.value = false
  }
}

function openPreview(row) {
  if (row.status !== 1) {
    ElMessage.warning('分享已失效，无法预览')
    return
  }
  previewShare.id = row.id
  previewShare.fileName = row.fileName
  previewShare.fileFormat = row.fileFormat || row.fileName
  previewShare.status = row.status
  previewVisible.value = true
}

async function handleDownload(row) {
  if (!row?.id) return
  const res = await downloadShare(row.id)
  saveBlob(res.data, row.fileName)
}

async function handleCancel(row) {
  await ElMessageBox.confirm(`确定取消分享「${row.fileName}」吗？`, '提示', { type: 'warning' })
  await cancelShare(row.id)
  ElMessage.success('取消分享成功')
  loadSent()
}

watch(activeTab, (tab) => {
  if (tab === 'create') loadOptions()
  if (tab === 'received') loadReceived()
  if (tab === 'sent') loadSent()
})

onMounted(() => {
  loadReceived()
})
</script>
