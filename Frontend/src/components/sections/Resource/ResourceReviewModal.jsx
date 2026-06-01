import {
  RESOURCE_FORMATS,
  RESOURCE_RULES,
  RESOURCE_SOURCES,
  RESOURCE_TYPES,
  STATUS_CONFIG,
} from '../../../data/Resource/resourceAdminData';
import styles from './ResourceReviewModal.module.css';

export default function ResourceReviewModal({ resource, onClose, onApprove, onReject }) {
  if (!resource) return null;

  const status = STATUS_CONFIG[resource.status] || STATUS_CONFIG.pending;
  const canReview = resource.status === 'pending' || resource.status === 'fixing';

  const handleApprove = () => {
    onApprove(resource.id, new Date().toISOString().split('T')[0]);
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(event) => event.stopPropagation()}>
        <div className={styles.header}>
          <div>
            <div className={styles.metaLine}>
              <span>Ban phụ trách: Ban học tập</span>
              <strong>{resource.formCode}</strong>
            </div>
            <h2 className={styles.title}>Phiếu thêm tài liệu học thuật</h2>
            <p className={styles.subtitle}>
              Ngày lập phiếu: {formatDate(resource.createdAt)}
            </p>
          </div>
          <button type="button" className={styles.closeBtn} onClick={onClose}>×</button>
        </div>

        <div className={styles.body}>
          <div title="I. Thông tin người đề xuất">
            <div className={styles.grid2}>
              <Info label="Họ và tên" value={resource.uploadedBy} />
              <Info label="Mã thành viên" value={resource.memberId} />
                <Info label="Chức vụ" value={resource.position || resource.memberRole || 'Thành viên'} />
            </div>
          </div>

          <div title="II. Thông tin tài liệu">
            <Info label="Tên tài liệu" value={resource.title} />
            <Info label="Môn học / Chủ đề" value={resource.subject} />

            <div className={styles.choiceGrid}>
              <ChoiceGroup title="Loại tài liệu" items={RESOURCE_TYPES} value={resource.type} />
              <ChoiceGroup title="Định dạng" items={RESOURCE_FORMATS} value={resource.format} />
              <ChoiceGroup title="Nguồn tài liệu" items={RESOURCE_SOURCES} value={resource.source} />
            </div>

            <Info label="Mô tả ngắn nội dung tài liệu" value={resource.description || '—'} multiline />
          </div>

          <div title="III. Tệp đính kèm">
            <div className={styles.linkRow}>
              <span className={styles.infoLabel}>Tệp tài liệu</span>
              {resource.link ? (
                <a href={resource.link} target="_blank" rel="noopener noreferrer" className={styles.linkBtn}>
                  Xem tài liệu
                </a>
              ) : (
                <span className={styles.pendingLink}>Chưa có tệp</span>
              )}
            </div>
          </div>

          <div title="IV. Xét duyệt">
            <div className={styles.grid2}>
              <Info label="Người xét duyệt" value={resource.reviewedBy || '—'} />
              <Info label="Ngày duyệt" value={formatDate(resource.reviewedAt)} />
              <Info label="Ghi chú" value={resource.note || '—'} />
              <div className={styles.infoBlock}>
                <p className={styles.infoLabel}>Trạng thái</p>
                <span className={styles.statusBadge} style={{ background: status.bg, color: status.color }}>
                  <span className={styles.statusDot} style={{ background: status.dot }} />
                  {status.label}
                </span>
              </div>
            </div>
            {canReview && <p className={styles.ruleNote}>{RESOURCE_RULES.reviewDateAfterCreatedDate}</p>}
          </div>
        </div>

        {canReview && (
          <div className={styles.actions}>
            <button type="button" className={styles.rejectBtn} onClick={() => onReject(resource.id)}>
              Từ chối
            </button>
            <button type="button" className={styles.approveBtn} onClick={handleApprove}>
              Duyệt tài liệu
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

function Section({ title, children }) {
  return (
    <section className={styles.section}>
      <h3 className={styles.sectionTitle}>{title}</h3>
      {children}
    </section>
  );
}

function Info({ label, value, multiline = false }) {
  return (
    <div className={styles.infoBlock}>
      <p className={styles.infoLabel}>{label}</p>
      <p className={multiline ? styles.infoDesc : styles.infoValue}>{value || '—'}</p>
    </div>
  );
}

function ChoiceGroup({ title, items, value }) {
  return (
    <div>
      <p className={styles.infoLabel}>{title}</p>
      <div className={styles.choiceList}>
        {items.map((item) => {
          const checked = item === value || (!items.includes(value) && item === 'Khác');
          return (
            <span key={item} className={`${styles.choiceItem} ${checked ? styles.checked : ''}`}>
              <span>{checked ? '☑' : '☐'}</span>
              {item}
            </span>
          );
        })}
      </div>
    </div>
  );
}

function formatDate(date) {
  if (!date) return '—';
  return new Date(date).toLocaleDateString('vi-VN');
}
