import api from '../utils/api'

export const getUsersAPI = () =>
  api.get('users')

export const getUserPasswordHashAPI = (userId) =>
  api.get(`users/${userId}/password-hash`)

export const createUserAPI = (payload) =>
  api.post('users', payload)

export const updateUserForAdminAPI = (userId, payload) =>
  api.patch(`users/${userId}/admin`, payload)

export const deleteUserAPI = (userId) =>
  api.delete(`users/${userId}`)

export const getUserSessionsAPI = (userId) =>
  api.get(`users/${userId}/sessions`)

export const normalizeAccountFromApi = (user = {}, passwordHash = '', sessions = []) => {
  const studentId = user.studentId || ''
  const memberId = user.memberId ? String(user.memberId) : ''

  return {
    id: String(user.userId || ''),
    userId: user.userId,
    memberDbId: user.memberId,
    memberId: studentId || memberId,
    username: memberId,
    password: '',
    passwordHash,
    name: user.fullName || '',
    email: user.email || '',
    role: user.roleName || 'Thành viên',
    department: user.departmentName || '',
    status: user.reqStatus || '',
    lastLogin: sessions[0]?.time || 'Chưa có dữ liệu',
    createdAt: formatDate(user.createdAt),
    updatedAt: formatDate(user.updatedAt),
    sessions,
    raw: user,
  }
}

export const loadAccountUsersAPI = async () => {
  const users = await getUsersAPI()
  return users.map((user) => normalizeAccountFromApi(user))
}

function formatDate(value) {
  if (!value) return 'Chưa cập nhật'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)

  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(date)
}

export const normalizeLoginSessionFromApi = (session = {}) => ({
  id: String(session.sessionId || session.id || `${session.loginAt || ''}-${session.ipAddress || ''}`),
  device: session.deviceLabel || 'Thiết bị không xác định',
  location: 'Không có dữ liệu vị trí',
  ip: session.ipAddress || '',
  userAgent: session.userAgent || '',
  time: formatDateTime(session.loginAt),
  raw: session,
})

function formatDateTime(value) {
  if (!value) return 'Chưa cập nhật'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)

  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}
