import styles from './ResourceCard.module.css';

const FORMAT_CONFIG = {
  PDF: { icon: '📄', label: 'PDF', bg: '#fee2e2', color: '#b91c1c', accent: '#ef4444' },
  DOCX: { icon: '📝', label: 'DOCX', bg: '#dbeafe', color: '#1d4ed8', accent: '#3b82f6' },
  PPT: { icon: '📊', label: 'PPT', bg: '#fef3c7', color: '#b45309', accent: '#f59e0b' },
  'Khác': { icon: '📎', label: '—', bg: '#f3e8ff', color: '#7c3aed', accent: '#8b5cf6' },
};

const TYPE_CONFIG = {
  'Giáo trình': { bg: '#e0f2fe', color: '#0369a1' },
  'Slide bài giảng': { bg: '#dcfce7', color: '#15803d' },
  'Tài liệu tham khảo': { bg: '#fef9c3', color: '#a16207' },
  'Khác': { bg: '#f3e8ff', color: '#7c3aed' },
};

export default function ResourceCard({ resource, viewMode = 'list', onRequestFix }) {
  const {
    title,
    subject,
    type,
    format,
    description,
    createdAt,
    source,
    link,
    workflowStatus,
  } = resource;

  const fmt = FORMAT_CONFIG[format] || FORMAT_CONFIG['Khác'];
  const typeStyle = TYPE_CONFIG[type] || { bg: '#f3f4f6', color: '#374151' };
  const isFixing = workflowStatus === 'fixing';
  const statusDot = isFixing
    ? { label: 'Fixing', color: '#f59e0b' }
    : { label: 'Working', color: '#22c55e' };

  const dateStr = createdAt
    ? new Date(createdAt).toLocaleDateString('vi-VN', { day: '2-digit', month: 'short', year: 'numeric' })
    : '—';

  if (viewMode === 'grid') {
    return (
      <div className={styles.gridCard}>
        <div className={styles.gridAccent} style={{ background: fmt.accent }} />
        <span className={styles.statusDot} style={{ background: statusDot.color }} title={statusDot.label} />

        <div className={styles.gridFmtWrap} style={{ background: fmt.bg }}>
          <span className={styles.gridFmtIcon}>{fmt.icon}</span>
          <span className={styles.gridFmtLabel} style={{ color: fmt.color }}>{fmt.label}</span>
        </div>

        <h3 className={styles.gridTitle}>{title}</h3>
        <p className={styles.gridSubject}>{subject}</p>
        {description && <p className={styles.gridDesc}>{description}</p>}

        <span className={styles.typeBadge} style={{ background: typeStyle.bg, color: typeStyle.color }}>
          {type}
        </span>

        <div className={styles.gridFooter}>
          <span className={styles.gridDate}>{dateStr}</span>
        </div>

        <div className={styles.cardActions}>
          {onRequestFix && (
            <button
              type="button"
              className={`${styles.fixButton} ${isFixing ? styles.fixButtonDisabled : ''}`}
              onClick={() => onRequestFix(resource)}
              disabled={isFixing}
              title={isFixing ? 'Tài liệu đang trong quá trình sửa/đợi duyệt' : 'Đề xuất sửa tài liệu'}
            >
              Đề xuất sửa
            </button>
          )}
          <ResourceLink link={link} />
        </div>
      </div>
    );
  }

  return (
    <div className={styles.listCard}>
      <span className={styles.statusDot} style={{ background: statusDot.color }} title={statusDot.label} />
      <div className={styles.listFmt} style={{ background: fmt.bg }}>
        <span className={styles.listFmtIcon}>{fmt.icon}</span>
        <span className={styles.listFmtLabel} style={{ color: fmt.color }}>{fmt.label}</span>
      </div>

      <div className={styles.listBody}>
        <div className={styles.listTop}>
          <h3 className={styles.listTitle}>{title}</h3>
          <span className={styles.typeBadge} style={{ background: typeStyle.bg, color: typeStyle.color }}>
            {type}
          </span>
        </div>

        <p className={styles.listSubject}>
          <svg width="11" height="11" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
            <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
          </svg>
          {subject}
        </p>

        {description && <p className={styles.listDesc}>{description}</p>}

        <div className={styles.listMeta}>
          {source && <MetaTag icon="link">{source}</MetaTag>}
          <span className={styles.listDate}>{dateStr}</span>
        </div>
      </div>

      <div className={styles.cardActions}>
        {onRequestFix && (
          <button
            type="button"
            className={`${styles.fixButton} ${isFixing ? styles.fixButtonDisabled : ''}`}
            onClick={() => onRequestFix(resource)}
            disabled={isFixing}
            title={isFixing ? 'Tài liệu đang trong quá trình sửa/đợi duyệt' : 'Đề xuất sửa tài liệu'}
          >
            Đề xuất sửa
          </button>
        )}
        <ResourceLink link={link} compact />
      </div>
    </div>
  );
}

function ResourceLink({ link, compact = false }) {
  if (!link) {
    return (
      <span className={`${styles.resourceLink} ${compact ? styles.resourceLinkCompact : ''} ${styles.resourceLinkDisabled}`}>
        Chưa có link
      </span>
    );
  }

  return (
    <a
      className={`${styles.resourceLink} ${compact ? styles.resourceLinkCompact : ''}`}
      href={link}
      target="_blank"
      rel="noopener noreferrer"
    >
      Xem
    </a>
  );
}

function MetaTag({ icon, children }) {
  return (
    <span className={styles.metaTag}>
      {icon === 'user' ? (
        <svg width="10" height="10" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
          <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
      ) : (
        <svg width="10" height="10" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
          <path d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
        </svg>
      )}
      {children}
    </span>
  );
}
