<template>
  <el-dialog
    v-model="visible"
    :title="`文件预览 - ${fileName}`"
    width="860px"
    destroy-on-close
    class="preview-dialog"
    @closed="handleClosed"
  >
    <div v-loading="loading" class="preview-body">
      <img v-if="previewType === 'image' && previewUrl" :src="previewUrl" class="preview-image" alt="preview" />
      <iframe v-else-if="previewType === 'pdf' && previewUrl" :src="previewUrl" class="preview-frame" title="pdf" />
      <video v-else-if="previewType === 'video' && previewUrl" :src="previewUrl" class="preview-video" controls />
      <pre v-else-if="previewType === 'text'" class="preview-text">{{ textContent }}</pre>
      <el-empty v-else-if="!loading" description="该文件类型暂不支持在线预览" />
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" :disabled="!canDownload" @click="emit('download')">下载</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { previewFile, previewShare } from '@/api'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  fileId: { type: Number, default: null },
  shareId: { type: Number, default: null },
  fileName: { type: String, default: '' },
  format: { type: String, default: '' },
  status: { type: Number, default: 0 },
  invalidMessage: { type: String, default: '文件未通过审核，无法预览' }
})

const emit = defineEmits(['update:modelValue', 'download'])

const loading = ref(false)
const previewType = ref('')
const previewUrl = ref('')
const textContent = ref('')

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const canDownload = computed(() => props.status === 1)

function normalizeExtension(formatOrName) {
  if (!formatOrName) return ''
  let ext = formatOrName.trim().toLowerCase()
  if (ext.startsWith('.')) ext = ext.slice(1)
  if (ext.includes('.')) {
    ext = ext.slice(ext.lastIndexOf('.') + 1)
  }
  return ext
}

function resolvePreviewType(formatOrName) {
  const ext = normalizeExtension(formatOrName)
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg'].includes(ext)) return 'image'
  if (ext === 'pdf') return 'pdf'
  if (['mp4', 'webm', 'avi', 'mov'].includes(ext)) return 'video'
  if (['txt', 'csv', 'md', 'json', 'xml', 'html', 'htm', 'css', 'js', 'log'].includes(ext)) return 'text'
  return 'unsupported'
}

function revokeUrl() {
  if (previewUrl.value) {
    window.URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
  textContent.value = ''
  previewType.value = ''
}

async function parseBlobError(blob) {
  try {
    const json = JSON.parse(await blob.text())
    return json.msg || '预览失败'
  } catch {
    return '预览失败'
  }
}

async function loadPreview() {
  revokeUrl()
  if (!props.fileId && !props.shareId) return

  if (props.status !== 1) {
    ElMessage.warning(props.invalidMessage)
    visible.value = false
    return
  }

  const type = resolvePreviewType(props.format || props.fileName)
  if (type === 'unsupported') {
    ElMessage.warning('该文件类型暂不支持在线预览')
    visible.value = false
    return
  }

  loading.value = true
  try {
    const res = props.shareId
      ? await previewShare(props.shareId)
      : await previewFile(props.fileId)
    const blob = res.data
    if (blob.type?.includes('application/json')) {
      throw new Error(await parseBlobError(blob))
    }

    previewType.value = type
    if (type === 'text') {
      textContent.value = await blob.text()
    } else {
      previewUrl.value = window.URL.createObjectURL(blob)
    }
  } catch (error) {
    ElMessage.error(error.message || '预览失败')
    visible.value = false
  } finally {
    loading.value = false
  }
}

function handleClosed() {
  revokeUrl()
}

watch(
  () => [props.modelValue, props.fileId, props.shareId],
  ([show, fileId, shareId]) => {
    if (show && (fileId || shareId)) {
      loadPreview()
    }
  }
)
</script>

<style scoped>
.preview-body {
  min-height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f9fc;
  border-radius: 8px;
  overflow: hidden;
}

.preview-image {
  max-width: 100%;
  max-height: 520px;
  object-fit: contain;
}

.preview-frame {
  width: 100%;
  height: 520px;
  border: none;
  background: #fff;
}

.preview-video {
  max-width: 100%;
  max-height: 520px;
  background: #000;
}

.preview-text {
  width: 100%;
  max-height: 520px;
  margin: 0;
  padding: 16px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
  line-height: 1.6;
  color: #303133;
  background: #fff;
  box-sizing: border-box;
}
</style>
