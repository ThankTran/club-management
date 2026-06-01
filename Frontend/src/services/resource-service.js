import api, { API_BASE_URL } from '../utils/api'

const API_ORIGIN = new URL(API_BASE_URL).origin

const resolveResourceLink = (link = '') => {
  if (!link) return ''
  if (link.startsWith('/uploads/')) return `${API_ORIGIN}${link}`
  if (/^https?:\/\//i.test(link)) return link
  return ''
}

export const normalizeUploadedResourceFile = (file = {}) => ({
  link: resolveResourceLink(file.fileUrl || ''),
  fileName: file.fileName || '',
  fileSize: file.fileSize || 0,
  mimeType: file.mimeType || '',
})

const fileNameFromUrl = (url = '') => {
  try {
    const { pathname } = new URL(url)
    const fileName = decodeURIComponent(pathname.split('/').filter(Boolean).pop() || '')
    return fileName || 'Tai lieu lien ket'
  } catch {
    return 'Tai lieu lien ket'
  }
}

const mimeTypeFromFileName = (fileName = '') => {
  const extension = fileName.split('.').pop()?.toLowerCase()
  const types = {
    pdf: 'application/pdf',
    doc: 'application/msword',
    docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    ppt: 'application/vnd.ms-powerpoint',
    pptx: 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    xls: 'application/vnd.ms-excel',
    xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    zip: 'application/zip',
    rar: 'application/vnd.rar',
    png: 'image/png',
    jpg: 'image/jpeg',
    jpeg: 'image/jpeg',
    txt: 'text/plain',
  }

  return types[extension] || 'application/octet-stream'
}

export const buildResourceFilePayload = (documentId, attachment = {}) => {
  const payload = new FormData()
  payload.append('documentId', documentId)

  if (attachment.file) {
    payload.append('file', attachment.file)
    return payload
  }

  const fileUrl = String(attachment.fileUrl || '').trim()
  if (fileUrl) {
    const fileName = fileNameFromUrl(fileUrl)
    payload.append('fileUrl', fileUrl)
    payload.append('fileName', fileName)
    payload.append('fileSize', '0')
    payload.append('mimeType', mimeTypeFromFileName(fileName))
  }

  return payload
}

export const hydrateResourcesWithFiles = async (resources = []) =>
  Promise.all(resources.map(async (resource) => {
    if (resource.link || !resource.id) return resource

    try {
      const files = await getResourceFilesAPI(resource.id)
      if (!Array.isArray(files) || files.length === 0) return resource

      const latestFile = [...files].sort((a, b) =>
        new Date(b.uploadedAt || 0) - new Date(a.uploadedAt || 0)
      )[0]

      return {
        ...resource,
        ...normalizeUploadedResourceFile(latestFile),
      }
    } catch {
      return resource
    }
  }))

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
    link: resolveResourceLink(resource.primaryFileUrl || resource.fileUrl || resource.files?.[0]?.fileUrl || ''),
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
  api.post('document-files', payload)

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
