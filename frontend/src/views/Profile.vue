<template>
  <div class="page-card">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="profile-form">
      <el-row :gutter="40">
        <el-col :span="12">
          <el-form-item label="用户名">
            <el-input v-model="form.username" disabled />
          </el-form-item>
          <el-form-item label="姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="原密码">
            <el-input v-model="form.oldPassword" type="password" placeholder="修改密码时填写" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="form.newPassword" type="password" placeholder="不修改请留空" show-password />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="form.confirmPassword" type="password" placeholder="不修改请留空" show-password />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="身份证" prop="idCard">
            <el-input v-model="form.idCard" placeholder="请输入身份证号" />
          </el-form-item>
          <el-form-item label="生日" prop="birthday">
            <el-date-picker
              v-model="form.birthday"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="form.gender">
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="学历" prop="education">
            <el-select v-model="form.education" placeholder="请选择" style="width: 100%">
              <el-option label="专科" value="专科" />
              <el-option label="本科" value="本科" />
              <el-option label="硕士" value="硕士" />
              <el-option label="博士" value="博士" />
            </el-select>
          </el-form-item>
          <el-form-item label="身份" prop="identity">
            <el-select v-model="form.identity" placeholder="请选择" style="width: 100%">
              <el-option label="学生" value="学生" />
              <el-option label="教师" value="教师" />
              <el-option label="职工" value="职工" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword, getProfile, updateProfile } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref()
const saving = ref(false)

const form = reactive({
  username: '',
  realName: '',
  idCard: '',
  gender: 1,
  birthday: '',
  education: '',
  phone: '',
  identity: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

async function loadProfile() {
  const res = await getProfile()
  Object.assign(form, {
    username: res.data.username,
    realName: res.data.realName,
    idCard: res.data.idCard,
    gender: res.data.gender || 1,
    birthday: res.data.birthday,
    education: res.data.education,
    phone: res.data.phone,
    identity: res.data.identity,
    newPassword: '',
    confirmPassword: ''
  })
}

async function handleSave() {
  await formRef.value.validate()

  if (form.newPassword || form.confirmPassword) {
    if (form.newPassword !== form.confirmPassword) {
      ElMessage.warning('两次输入的密码不一致')
      return
    }
    if (!form.oldPassword) {
      ElMessage.warning('修改密码请输入原密码')
      return
    }
  }

  saving.value = true
  try {
    await updateProfile({
      realName: form.realName,
      idCard: form.idCard,
      gender: form.gender,
      birthday: form.birthday,
      education: form.education,
      phone: form.phone,
      identity: form.identity
    })

    if (form.newPassword) {
      await changePassword({
        oldPassword: form.oldPassword || '',
        newPassword: form.newPassword,
        confirmPassword: form.confirmPassword
      })
      ElMessage.success('保存成功，密码已修改，请重新登录')
      localStorage.removeItem('token')
      window.location.href = '/login'
      return
    }

    await userStore.fetchProfile()
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-form {
  max-width: 900px;
}
</style>
