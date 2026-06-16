let socket = null
let reconnectTimer = null
const listeners = new Set()

function getWsUrl() {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role') || ''
  const params = `token=${encodeURIComponent(token || '')}&role=${encodeURIComponent(role)}`

  // 开发环境直连后端，避免 Vite 代理 WebSocket 兼容问题
  if (import.meta.env.DEV) {
    return `ws://localhost:8080/api/ws/notification?${params}`
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/api/ws/notification?${params}`
}

export function connectNotificationWs(onMessage) {
  if (onMessage) {
    listeners.add(onMessage)
  }
  if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
    return
  }

  const token = localStorage.getItem('token')
  if (!token) return

  try {
    socket = new WebSocket(getWsUrl())
  } catch {
    scheduleReconnect()
    return
  }

  socket.onopen = () => {
    clearTimeout(reconnectTimer)
  }

  socket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      listeners.forEach((fn) => fn(data))
    } catch {
      // ignore invalid message
    }
  }

  socket.onerror = () => {
    socket?.close()
  }

  socket.onclose = () => {
    socket = null
    scheduleReconnect()
  }
}

function scheduleReconnect() {
  clearTimeout(reconnectTimer)
  reconnectTimer = setTimeout(() => connectNotificationWs(), 5000)
}

export function disconnectNotificationWs() {
  clearTimeout(reconnectTimer)
  listeners.clear()
  if (socket) {
    socket.onclose = null
    socket.close()
    socket = null
  }
}
