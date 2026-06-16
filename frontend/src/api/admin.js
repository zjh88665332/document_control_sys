import request from '@/utils/request'
import { saveBlob } from '@/api'

export const getDashboardOverview = () => request.get('/admin/dashboard/overview')
export const getDashboardCharts = () => request.get('/admin/dashboard/charts')
export const getPendingFileCount = () => request.get('/admin/file/pending-count')
export const getOperationLogList = (params) => request.get('/admin/operation-log/list', { params })

export const getAdminUserList = (params) => request.get('/admin/user/list', { params })
export const getAdminUserDetail = (id) => request.get(`/admin/user/${id}`)
export const updateUserStatus = (id, data) => request.put(`/admin/user/${id}/status`, data)
export const resetUserPassword = (id) => request.put(`/admin/user/${id}/reset-password`)

export const getAdminAccountList = (params) => request.get('/admin/admin/list', { params })
export const createAdminAccount = (data) => request.post('/admin/admin', data)
export const deleteAdminAccount = (id) => request.delete(`/admin/admin/${id}`)
export const resetAdminPassword = (id) => request.put(`/admin/admin/${id}/reset-password`)

export const getAdminNoticeList = (params) => request.get('/admin/notice/list', { params })
export const createNotice = (data) => request.post('/admin/notice', data)
export const updateNotice = (id, data) => request.put(`/admin/notice/${id}`, data)
export const deleteNotice = (id) => request.delete(`/admin/notice/${id}`)
export const batchDeleteNotice = (ids) => request.delete('/admin/notice/batch', { data: { ids } })

export const getAdminFileList = (params) => request.get('/admin/file/list', { params })
export const auditFile = (id, data) => request.put(`/admin/file/${id}/audit`, data)
export const adminDeleteFile = (id) => request.delete(`/admin/file/${id}`)
export const adminDownloadFile = (id) =>
  request.get(`/admin/file/${id}/download`, { responseType: 'blob' }).then((res) => res)

export const getAdminFeedbackList = (params) => request.get('/admin/feedback/list', { params })
export const replyFeedback = (id, data) => request.put(`/admin/feedback/${id}/reply`, data)

export async function downloadAdminFile(id, filename) {
  const res = await adminDownloadFile(id)
  saveBlob(res.data, filename)
}
