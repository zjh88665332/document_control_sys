<template>
  <div class="page-card">
    <div class="page-toolbar">
      <h3>公告管理</h3>
      <el-button type="primary" @click="openDialog()">发布公告</el-button>
    </div>

    <div class="filters">
      <el-input v-model="query.title" placeholder="标题" clearable style="width: 180px" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="已发布" :value="1" />
        <el-option label="草稿" :value="0" />
      </el-select>
      <el-button @click="loadData">搜索</el-button>
    </div>

    <el-table :data="list" stripe border v-loading="loading" style="margin-top: 12px">
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column prop="publisherName" label="发布人" width="100" />
      <el-table-column label="置顶" width="70">
        <template #default="{ row }">{{ row.isTop === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑公告' : '发布公告'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">发布</el-radio>
            <el-radio :value="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createNotice, deleteNotice, getAdminNoticeList, updateNotice } from '@/api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const query = reactive({ title: '', status: null })
const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({ title: '', content: '', isTop: 0, status: 1 })

async function loadData() {
  loading.value = true
  try {
    const res = await getAdminNoticeList({
      title: query.title || undefined,
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

function openDialog(row) {
  editingId.value = row?.id || null
  Object.assign(form, {
    title: row?.title || '',
    content: row?.content || '',
    isTop: row?.isTop ?? 0,
    status: row?.status ?? 1
  })
  dialogVisible.value = true
}

async function handleSave() {
  if (editingId.value) {
    await updateNotice(editingId.value, form)
    ElMessage.success('修改成功')
  } else {
    await createNotice(form)
    ElMessage.success('发布成功')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除公告「${row.title}」吗？`, '提示', { type: 'warning' })
  await deleteNotice(row.id)
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
