import styles from './ResourceDetailModal.module.css';

const FORMAT_ICON = {
  PDF:  { icon: '📄', bg: '#fee2e2', color: '#b91c1c' },
  DOCX: { icon: '📝', bg: '#dbeafe', color: '#1d4ed8' },
  PPT:  { icon: '📊', bg: '#fef3c7', color: '#b45309' },
  Khác: { icon: '📎', bg: '#f3e8ff', color: '#7c3aed' },
};

const STATUS_STYLE = {
  pending:  { label: 'Chờ duyệt', bg: '#fef3c7', color: '#92400e' },
  approved: { label: 'Đã duyệt',  bg: '#dcfce7', color: '#15803d' },
  rejected: { label: 'Từ chối',   bg: '#fee2e2', color: '#b91c1c' },
};

/**
 * Props:
 *   resource   – object | null
 *   onClose    – () => void
 *   onApprove  – (id) => void   (chỉ admin)
 *   onReject   – (id) => void   (chỉ admin)
 *   onEdit     – (resource) => void  (chỉ admin)
 *   isAdmin    – boolean
 */
export default function ResourceDetailModal({
  resource,
  onClose,
  onApprove,
  onReject,
  onEdit,
  isAdmin = false,
}) {
  if (!resource) return null;

  const {
    title, subject, type, format, source, description, link,
    uploadedBy, createdAt, status, approvedBy, approvedAt, note,
  } = resource;

  const fmt = FORMAT_ICON[format] || FORMAT_ICON['Khác'];
  const ss  = STATUS_STYLE[status] || STATUS_STYLE.pending;

  const dateStr = createdAt
    ? new Date(createdAt).toLocaleDateString('vi-VN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })
    : '—';

  const approvedDateStr = approvedAt
    ? new Date(approvedAt).toLocaleDateString('vi-VN')
    : null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.box} onClick={(e) => e.stopPropagation()}>

        {/* Close */}
        <button className={styles.closeBtn} onClick={onClose}>✕</button>

        {/* Header */}
        <div className={styles.header}>
          <div className={styles.fmtIcon} style={{ background: fmt.bg }}>
            <span>{fmt.icon}</span>
          </div>
          <div className={styles.headerText}>
            <h2 className={styles.title}>{title}</h2>
            <p className={styles.subject}>{subject}</p>
          </div>
        </div>

        {/* Badges */}
        <div className={styles.badges}>
          <span className={styles.fmtBadge} style={{ color: fmt.color, background: fmt.bg }}>
            {format}
          </span>
          <span className={styles.typeBadge}>{type}</span>
          <span className={styles.statusBadge} style={{ background: ss.bg, color: ss.color }}>
            {ss.label}
          </span>
        </div>

        {/* Description */}
        {description && (
          <p className={styles.description}>{description}</p>
        )}

        {/* Info grid */}
        <div className={styles.infoGrid}>
          <InfoRow label="Nguồn"          value={source || '—'} />
          <InfoRow label="Người đề xuất"  value={uploadedBy || '—'} />
          <InfoRow label="Ngày lập phiếu" value={dateStr} />
          {approvedBy && (
            <InfoRow label="Người xét duyệt" value={approvedBy} />
          )}
          {approvedDateStr && (
            <InfoRow label="Ngày duyệt" value={approvedDateStr} />
          )}
          {note && (
            <InfoRow label="Ghi chú" value={note} />
          )}
        </div>

        {/* Uploaded file */}
        {link ? (
          <a
            href={link}
            target="_blank"
            rel="noopener noreferrer"
            className={styles.downloadBtn}
          >
            <svg width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3"/>
            </svg>
            Xem tài liệu
          </a>
        ) : (
          <span className={styles.downloadBtn}>Chưa có tệp</span>
        )}

        {/* Admin actions — chỉ hiện khi pending */}
        {isAdmin && status === 'pending' && (
          <div className={styles.adminActions}>
            <button
              className={styles.rejectBtn}
              onClick={() => onReject?.(resource.id)}
            >
              ✕ Từ chối
            </button>
            <button
              className={styles.editBtn}
              onClick={() => onEdit?.(resource)}
            >
              ✎ Sửa
            </button>
            <button
              className={styles.approveBtn}
              onClick={() => onApprove?.(resource.id)}
            >
              ✓ Duyệt tài liệu
            </button>
          </div>
        )}

        {/* Admin — đã approved thì chỉ có nút sửa */}
        {isAdmin && status === 'approved' && (
          <div className={styles.adminActions}>
            <button
              className={styles.editBtn}
              onClick={() => onEdit?.(resource)}
            >
              ✎ Chỉnh sửa
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

function InfoRow({ label, value }) {
  return (
    <div className={styles.infoRow}>
      <span className={styles.infoLabel}>{label}</span>
      <span className={styles.infoValue}>{value}</span>
    </div>
  );
}
