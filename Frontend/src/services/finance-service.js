import api from '../utils/api'

const isIncome = (type) => String(type).toUpperCase() === 'INCOME'
const toDateTimeParam = (value, fallbackTime) => {
  if (!value) return value
  const text = String(value)
  return text.includes('T') ? text : `${text}T${fallbackTime}`
}

export const normalizeTransactionFromApi = (transaction = {}) => {
  const displayName = transaction.memberName || transaction.counterpartyName || transaction.memberId
  const transactionDate = transaction.transactionDate || transaction.createdAt
  const base = {
    id: transaction.transactionId,
    maSuKien: transaction.eventId || null,
    soTien: Number(transaction.amount || 0),
    status: transaction.status,
    raw: transaction,
  }

  if (isIncome(transaction.type)) {
    return {
      ...base,
      nguoiNop: displayName ? String(displayName) : '',
      lyDo: transaction.description || '',
      hinhThuc: 'Tiền mặt',
      ngayThu: transactionDate ? String(transactionDate).slice(0, 10) : '',
    }
  }

  return {
    ...base,
    nguoiNhan: displayName ? String(displayName) : '',
    noiDung: transaction.description || '',
    ngayLap: transactionDate ? String(transactionDate).slice(0, 10) : '',
  }
}

export const normalizeMemberDueFromApi = (due = {}) => ({
  id: due.studentId || String(due.memberId || ''),
  transactionId: due.transactionId,
  memberId: due.memberId,
  name: due.memberName || '',
  role: due.roleName || 'Thành viên',
  month: due.month || '',
  amount: Number(due.amount || 0),
  status: due.status,
  raw: due,
})

export const toIncomePayload = (item = {}) => ({
  transactionId: item.id,
  eventId: item.maSuKien || null,
  memberId: item.memberId || null,
  counterpartyName: item.nguoiNop || '',
  type: 'INCOME',
  amount: Number(item.soTien || 0),
  description: item.lyDo,
  transactionDate: item.ngayThu ? `${item.ngayThu}T12:00:00` : null,
  status: item.status || (item.memberId ? 'PENDING' : 'COMPLETED'),
  createdById: item.createdById || null,
  approvedById: item.approvedById || null,
})

export const toExpensePayload = (item = {}) => ({
  transactionId: item.id,
  eventId: item.maSuKien || null,
  memberId: item.memberId || null,
  counterpartyName: item.nguoiNhan || '',
  type: 'EXPENSE',
  amount: Number(item.soTien || 0),
  description: item.noiDung,
  transactionDate: item.ngayLap ? `${item.ngayLap}T12:00:00` : null,
  status: item.status || 'PENDING',
  createdById: item.createdById || null,
  approvedById: item.approvedById || null,
})

export const getTotalIncomeAPI = (from, to) =>
  api.get('finance/income', {
    params: {
      from: toDateTimeParam(from, '00:00:00'),
      to: toDateTimeParam(to, '23:59:59'),
    },
  })

export const getTotalExpenseAPI = (from, to) =>
  api.get('finance/expense', {
    params: {
      from: toDateTimeParam(from, '00:00:00'),
      to: toDateTimeParam(to, '23:59:59'),
    },
  })

export const getRevenueAPI = (from, to) =>
  api.get('finance/revenue', {
    params: {
      from: toDateTimeParam(from, '00:00:00'),
      to: toDateTimeParam(to, '23:59:59'),
    },
  })

export const getIncomeByEventAPI = (eventId) =>
  api.get(`finance/income/by-event/${eventId}`)

export const getExpenseByEventAPI = (eventId) =>
  api.get(`finance/expense/by-event/${eventId}`)

export const getRevenueByEventAPI = (eventId) =>
  api.get(`finance/revenue/by-event/${eventId}`)

export const getTransactionsAPI = () =>
  api.get('transactions')

export const getTransactionsPageAPI = ({
  type,
  from,
  to,
  page = 0,
  size = 20,
} = {}) =>
  api.get('transactions', {
    params: {
      type: type || undefined,
      from: toDateTimeParam(from, '00:00:00'),
      to: toDateTimeParam(to, '23:59:59'),
      page,
      size,
    },
  })

export const getTransactionByIdAPI = (id) =>
  api.get(`transactions/${id}`)

export const getTransactionsByTypeAPI = (type) =>
  api.get('transactions/by-type', { params: { type } })

export const getTransactionsByEventAPI = (eventId) =>
  api.get(`transactions/by-event/${eventId}`)

export const getMemberDuesAPI = (memberId) =>
  api.get(`transactions/member-dues/${memberId}`)

export const getPendingMonthlyDuesAPI = () =>
  api.get('transactions/monthly-dues/pending')

export const createTransactionAPI = (payload) =>
  api.post('transactions', payload)

export const updateTransactionAPI = (id, payload) =>
  api.put(`transactions/${id}`, payload)

export const completeTransactionAPI = (id) =>
  api.patch(`transactions/${id}/complete`)

export const submitTransactionPaymentAPI = (id) =>
  api.patch(`transactions/${id}/submit-payment`)

export const rejectTransactionPaymentAPI = (id) =>
  api.patch(`transactions/${id}/reject-payment`)

export const deleteTransactionAPI = (id) =>
  api.delete(`transactions/${id}`)

// Add approve/reject transaction endpoints if finance approval is required.
// Add monthly report endpoint for BM12 with month validation and balance.
