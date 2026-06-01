import api, { API_BASE_URL } from '../utils/api'

const API_ORIGIN = new URL(API_BASE_URL).origin

const resolveResourceLink = (link = '') => {
  if (!link) return ''
  if (/^https?:\/\//i.test(link)) return link
  if (link.startsWith('/uploads/')) return `${API_ORIGIN}${link}`
  return link
}

const detectFormat = (resource = {}) => {
  const fileName = resource.primaryFileName || resource.fileName || ''
  const mimeType = resource.mimeType || ''
  const extension = fileName.includes('.') ? fileName.split('.').pop().toUpperCase() : ''

  if (extension) return extension
  if (mimeType.includes('pdf')) return 'PDF'
  if (mimeType.includes('word')) return 'DOCX'
  if (mimeType.includes('powerpoint') || mimeType.includes('presentation')) return 'PPT'
  if (mimeType.includes('zip')) return 'ZIP'
  if (mimeType.includes('png')) return 'PNG'
  return 'Khác'
}

export const normalizeResourceFromApi = (resource = {}, member = null) => ({
    id: resource.documentId,
    title: resource.documentName || '',
    formCode: `TL-${String(resource.documentId || '').padStart(3, '0')}`,
    typeId: resource.typeId,
    type: resource.typeName || '',
    subjectId: resource.subjectId,
    subject: resource.subjectName || '',
    status: resource.reqStatus === 'APPROVED'
        ? 'approved'
        : resource.reqStatus === 'REQUESTED_CHANGES'
            ? 'fixing'
            : resource.reqStatus === 'REJECTED'
                ? 'rejected'
                : 'pending',
    reqStatus: resource.reqStatus,
    workflowStatus: resource.reqStatus === 'REQUESTED_CHANGES' ? 'fixing' : 'working',
    format: detectFormat(resource),
    source: resource.source || '',
    description: resource.note || '',
    link: resolveResourceLink(resource.primaryFileUrl || resource.fileUrl || resource.url || resource.files?.[0]?.fileUrl || ''),
    fileName: resource.primaryFileName || resource.fileName || resource.files?.[0]?.fileName || '',
    fileSize: resource.fileSize || resource.files?.[0]?.fileSize || 0,
    mimeType: resource.mimeType || resource.files?.[0]?.mimeType || '',
    lookupFolderId: resource.lookupFolderId || '',
    approvedAt: resource.approvedAt ? String(resource.approvedAt).slice(0, 10) : '',

    uploadedBy: member?.name || member?.fullName || resource.proposedByName || '—',
    memberId: resource.proposedById || member?.memberId || '—',
    memberCode: member?.id || member?.studentId || resource.proposedById || '—',
    position: member?.role || member?.roleName || 'Thành viên',

    reviewedBy: resource.approvedById ? String(resource.approvedById) : '',
    reviewedAt: resource.approvedAt ? String(resource.approvedAt).slice(0, 10) : '',
    createdAt: resource.createdAt ? String(resource.createdAt).slice(0, 10) : '',
    note: resource.note || '',
    raw: resource,
});

export const toResourcePayload = (resource = {}) => ({
  documentName: resource.title,
  typeId: Number(resource.typeId || resource.type),
  subjectId: Number(resource.subjectId || resource.subject),
  source: resource.source || '',
  note: resource.description || resource.note || '',
  proposedById: resource.proposedById || null,
})

export const getResourcesAPI = (params = {}) =>
  api.get('documents', { params })

export const getResourceByIdAPI = (id) =>
  api.get(`documents/${id}`)

export const searchResourcesAPI = (name) =>
  api.get('documents/search', { params: { name } })

export const getResourcesBySubjectAPI = (subjectId) =>
  api.get(`documents/by-subject/${subjectId}`)

export const getResourcesByTypeAPI = (typeId) =>
  api.get(`documents/by-type/${typeId}`)

export const createResourceAPI = (payload) =>
  api.post('documents', payload)

export const approveResourceAPI = (payload) =>
  api.post('documents/approve', payload)

export const softDeleteResourceAPI = (id) =>
  api.delete(`documents/${id}`)

export const hardDeleteResourceAPI = (id) =>
  api.delete(`documents/${id}/hard`)

export const createResourceFileAPI = (payload) =>
  api.post('document-files', payload, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })

export const getResourceFilesAPI = (documentId) =>
  api.get(`document-files/by-document/${documentId}`)

export const deleteResourceFileAPI = (fileId) =>
  api.delete(`document-files/${fileId}`)

export const createResourceTypeAPI = (payload) =>
  api.post('document-types', payload)

export const getResourceTypesAPI = () =>
  api.get('document-types')

export const getResourceTypeByNameAPI = (typeName) =>
  api.get('document-types/by-name', { params: { name: typeName } })

export const createResourceStatusAPI = (payload) =>
  api.post('document-statuses', payload)

export const getResourceStatusesAPI = () =>
  api.get('document-statuses')

export const getResourceStatusByNameAPI = (statusName) =>
  api.get('document-statuses/by-name', { params: { name: statusName } })
