import api from '../utils/api'

const REQUEST_STATUS_UI = {
  PENDING: 'Đang xét duyệt',
  APPROVED: 'Đã duyệt',
  REJECTED: 'Từ chối',
}

const REQUEST_STATUS_API = {
  'Đang xét duyệt': 'PENDING',
  'Đã duyệt': 'APPROVED',
  'Từ chối': 'REJECTED',
}

const GENDER_API = {
  Nam: 'MALE',
  Nữ: 'FEMALE',
}

const GENDER_UI = {
  MALE: 'Nam',
  FEMALE: 'Nữ',
  OTHER: 'Khác',
}

const GRADUATION_API = {
  'Chưa tốt nghiệp': 'ACTIVE',
  'Đã tốt nghiệp': 'GRADUATED',
}

const GRADUATION_UI = {
  ACTIVE: 'Chưa tốt nghiệp',
  GRADUATED: 'Đã tốt nghiệp',
  INACTIVE: 'Đã rời CLB',
}

export const normalizeMemberFromApi = (member = {}) => ({
  id: member.studentId || String(member.memberId || ''),
  memberId: member.memberId,
  name: member.fullName || '',
  email: member.email || '',
  phone: member.phone || '',
  departmentId: member.departmentId || null,
  department: member.departmentName || '',
  dateOfBirth: member.dateOfBirth || '',
  gender: GENDER_UI[member.gender] || member.gender || '',
  graduationStatus: GRADUATION_UI[member.graduatedStatus] || member.graduatedStatus || 'Chưa tốt nghiệp',
  requestStatus: REQUEST_STATUS_UI[member.reqStatus] || member.reqStatus || 'Đang xét duyệt',
  role: member.roleName || 'Thành viên',
  registeredAt: member.createdAt ? String(member.createdAt).slice(0, 10) : '',
  reviewedBy: member.approverName || '',
  reviewedAt: member.approvalDate ? String(member.approvalDate).slice(0, 10) : '',
  reviewNote: member.approvalNote || '',
  avatar: (member.fullName || member.studentId || 'NA').slice(0, 2).toUpperCase(),
  raw: member,
})

export const normalizePublicMemberFromApi = (member = {}) => ({
  id: String(member.memberId || member.fullName || ''),
  memberId: member.memberId,
  name: member.fullName || '',
  role: member.roleName || 'Thành viên',
  raw: member,
})

export const toMemberPayload = (member = {}) => ({
  studentId: member.id,
  fullName: member.name,
  departmentId: member.departmentId || null,
  email: member.email,
  phoneNumber: member.phone,
  dateOfBirth: member.dateOfBirth,
  gender: GENDER_API[member.gender] || member.gender,
  graduatedStatus: GRADUATION_API[member.graduationStatus] || member.graduationStatus || 'ACTIVE',
  roleName: member.role,
})

export const toApprovalPayload = (member, reviewData = {}) => ({
  memberId: member.memberId,
  approvedBy: reviewData.approvedBy || null,
  status: REQUEST_STATUS_API[reviewData.requestStatus] || 'PENDING',
  note: reviewData.reviewNote || '',
})

export const getMembersAPI = () =>
  api.get('members')

export const getPublicLeadersAPI = () =>
  api.get('members/public-leaders')

export const getMemberByIdAPI = (id) =>
  api.get(`members/${id}`)

export const searchMembersAPI = (params = {}) =>
  api.get('members/search', { params })

export const registerMemberAPI = (payload) =>
  api.post('members/register', payload)

export const approveMemberAPI = (payload) =>
  api.post('members/approve', payload)

export const updateMemberAPI = (id, payload) =>
  api.put(`members/${id}`, payload)

export const deleteMemberAPI = (id) =>
  api.delete(`members/${id}`)

export const getMemberDepartmentsAPI = () =>
  api.get('members/departments')

// Add DELETE /api/members/{id} if frontend needs member removal.
