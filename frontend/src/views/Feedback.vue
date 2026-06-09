<template>
  <div class="page-card">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="提交反馈" name="submit">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" style="max-width: 600px">
          <el-form-item label="反馈主题" prop="subject">
            <el-input v-model="form.subject" placeholder="请输入反馈主题" />
          </el-form-item>
          <el-form-item label="反馈内容" prop="content">
            <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入反馈内容" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="我的反馈" name="list">
        <el-table :data="list" stripe border v-loading="loading">
          <el-table-column prop="subject" label="主题" min-width="140" />
          <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
          <el-table-column prop="submitTime" label="提交时间" width="170" />
          <el-table-column prop="reply" label="回复" min-width="160" show-overflow-tooltip />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '已回复' : '待回复' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager">
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="loadList"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getFeedbackList, submitFeedback } from '@/api'

const activeTab = ref('submit')
const formRef = ref()
const submitting = ref(false)
const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const form = reactive({ subject: '', content: '' })
const rules = {
  subject: [{ required: true, message: '请输入反馈主题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入反馈内容', trigger: 'blur' }]
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await submitFeedback(form)
    ElMessage.success('提交成功')
    form.subject = ''
    form.content = ''
    activeTab.value = 'list'
    loadList()
  } finally {
    submitting.value = false
  }
}

async function loadList() {
  loading.value = true
  try {
    const res = await getFeedbackList({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

watch(activeTab, (val) => {
  if (val === 'list') loadList()
})

onMounted(() => {
  if (activeTab.value === 'list') loadList()
})
</script>

<style scoped>
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
