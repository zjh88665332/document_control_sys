import request from '@/utils/request'

export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)

export const getNotificationBadges = () => request.get('/notification/badges')
export const markNotificationRead = (type) => request.post(`/notification/read/${type}`)

export const getProfile = () => request.get('/user/profile')
export const updateProfile = (data) => request.put('/user/profile', data)
export const changePassword = (data) => request.put('/user/password', data)

export const getNoticeList = (params) => request.get('/notice/list', { params })

export const submitFeedback = (data) => request.post('/feedback', data)
export const getFeedbackList = (params) => request.get('/feedback/list', { params })

export const MAX_UPLOAD_SIZE = 200 * 1024 * 1024

export const uploadFile = (formData, onUploadProgress) =>
  request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 300000,
    onUploadProgress
  })
export const batchUploadFiles = (formData) =>
  request.post('/file/upload/batch', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 300000
  })
export const getFileList = (params) => request.get('/file/list', { params })
export const getRecycleBinList = (params) => request.get('/file/recycle/list', { params })
export const restoreFile = (id) => request.put(`/file/recycle/${id}/restore`)
export const permanentDeleteFile = (id) => request.delete(`/file/recycle/${id}`)
export const moveFile = (id, data) => request.put(`/file/${id}/move`, data)
export const getFolderTree = () => request.get('/folder/tree')
export const createFolder = (data) => request.post('/folder', data)
export const deleteFolder = (id) => request.delete(`/folder/${id}`)
export const deleteFile = (id) => request.delete(`/file/${id}`)
export const downloadFile = (id) =>
  request.get(`/file/${id}/download`, { responseType: 'blob' })
export const previewFile = (id) =>
  request.get(`/file/${id}/preview`, { responseType: 'blob' })

export const searchFriend = (keyword) => request.get('/friend/search', { params: { keyword } })
export const applyFriend = (data) => request.post('/friend/apply', data)
export const getReceivedApplies = (params) => request.get('/friend/apply/received', { params })
export const getSentApplies = (params) => request.get('/friend/apply/sent', { params })
export const handleApply = (id, data) => request.put(`/friend/apply/${id}/handle`, data)
export const getFriendList = (params) => request.get('/friend/list', { params })
export const deleteFriend = (friendId) => request.delete(`/friend/${friendId}`)

export const createShare = (data) => request.post('/share', data)
export const getReceivedShares = (params) => request.get('/share/received', { params })
export const getSentShares = (params) => request.get('/share/sent', { params })
export const cancelShare = (id) => request.delete(`/share/${id}`)
export const downloadShare = (id) =>
  request.get(`/share/${id}/download`, { responseType: 'blob' })
export const previewShare = (id) =>
  request.get(`/share/${id}/preview`, { responseType: 'blob' })

export const getFileTypeStatistics = () => request.get('/statistics/fileType')

export function saveBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  window.URL.revokeObjectURL(url)
}
