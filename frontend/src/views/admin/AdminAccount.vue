<template>
  <div class="page-card">
    <div class="page-toolbar">
      <h3>管理员账号（仅超级管理员）</h3>
      <el-button type="primary" @click="showCreate = true">新增管理员</el-button>
    </div>

    <el-table :data="list" stripe border v-loading="loading">
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="warning" @click="handleReset(row)">重置密码</el-button>
          <el-button link type="danger" @click="handleDelete(row)">禁用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showCreate" title="新增管理员" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createAdminAccount, deleteAdminAccount, getAdminAccountList, resetAdminPassword } from '@/api/admin'

const loading = ref(false)
const list = ref([])
const showCreate = ref(false)
const formRef = ref()
const form = reactive({ username: '', password: '123456', realName: '', phone: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAdminAccountList({ pageNum: 1, pageSize: 100 })
    list.value = res.data.list
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  await formRef.value.validate()
  await createAdminAccount(form)
  ElMessage.success('创建成功')
  showCreate.value = false
  loadData()
}

async function handleReset(row) {
  await ElMessageBox.confirm(`重置「${row.realName}」密码为 123456？`)
  await resetAdminPassword(row.id)
  ElMessage.success('密码已重置')
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定禁用管理员「${row.realName}」吗？`, '提示', { type: 'warning' })
  await deleteAdminAccount(row.id)
  ElMessage.success('已禁用')
  loadData()
}

onMounted(loadData)
</script>
