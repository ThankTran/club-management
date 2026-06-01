import { Check, Pencil, Trash2, X } from 'lucide-react';
import styles from './ResourceTable.module.css';

const FORMAT_ICON = {
  PDF:  { icon: '📄', color: '#b91c1c', bg: '#fee2e2' },
  DOCX: { icon: '📝', color: '#1d4ed8', bg: '#dbeafe' },
  PPT:  { icon: '📊', color: '#b45309', bg: '#fef3c7' },
  Khác: { icon: '📎', color: '#7c3aed', bg: '#f3e8ff' },
};

const STATUS_STYLE = {
  pending:  { label: 'Chờ duyệt thêm', bg: '#fef3c7', color: '#92400e', dot: '#f59e0b' },
  fixing:   { label: 'Chờ duyệt sửa', bg: '#e0f2fe', color: '#0369a1', dot: '#0ea5e9' },
  approved: { label: 'Đã duyệt',  bg: '#dcfce7', color: '#15803d', dot: '#16a34a' },
  rejected: { label: 'Từ chối',   bg: '#fee2e2', color: '#b91c1c', dot: '#ef4444' },
};

/**
 * Props:
 *   resources    – array
 *   total        – number
 *   page         – number
 *   totalPages   – number
 *   pageSize     – number
 *   onPageChange – (page) => void
 *   onView       – (resource) => void
 *   onEdit       – (resource) => void
 *   onDelete     – (resource) => void
 *   onApprove    – (id) => void
 *   onReject     – (id) => void
 *   loading      – boolean
 */
export default function ResourceTable({
  resources = [],
  total = 0,
  page = 1,
  totalPages = 1,
  pageSize = 10,
  onPageChange,
  onView,
  onEdit,
  onDelete,
  onApprove,
  onReject,
  showActions = true,
  showPagination = true,
  verticalScroll = false,
  dateLabel = 'NGÀY LẬP',
  dateKey = 'createdAt',
  loading = false,
}) {
  if (loading) {
    return (
      <div className={styles.loadingWrap}>
        <div className={styles.spinner} />
        <p>Đang tải danh sách tài liệu...</p>
      </div>
    );
  }

  if (!resources.length) {
    return (
      <div className={styles.empty}>
        <svg width="48" height="48" fill="none" viewBox="0 0 24 24" stroke="#cbd5e0" strokeWidth="1.2">
          <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
        </svg>
        <p>Không tìm thấy tài liệu nào</p>
      </div>
    );
  }

  const start = (page - 1) * pageSize + 1;
  const end   = Math.min(page * pageSize, total);

  // Pagination pages
  const getPages = () => {
    if (totalPages <= 5) return Array.from({ length: totalPages }, (_, i) => i + 1);
    const pages = new Set([1, 2, page - 1, page, page + 1, totalPages - 1, totalPages]);
    return [...pages].filter((p) => p >= 1 && p <= totalPages).sort((a, b) => a - b);
  };

  const pageList = getPages();

  return (
    <div className={styles.wrapper}>
      <div className={`${styles.tableScroll} ${verticalScroll ? styles.tableScrollVertical : ''}`}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>TÀI LIỆU</th>
              <th>CHỦ ĐỀ</th>
              <th>LOẠI</th>
              <th>TRẠNG THÁI</th>
              <th>{dateLabel}</th>
              <th>LINK</th>
              {showActions && <th>THAO TÁC</th>}
            </tr>
          </thead>
          <tbody>
            {resources.map((r) => {
              const fmt = FORMAT_ICON[r.format] || FORMAT_ICON['Khác'];
              const ss  = STATUS_STYLE[r.status] || STATUS_STYLE.pending;
              const displayDate = r[dateKey] || r.createdAt;
              const date = displayDate
                ? new Date(displayDate).toLocaleDateString('vi-VN', { day: '2-digit', month: 'short', year: 'numeric' })
                : '—';

              return (
                <tr key={r.id} className={styles.row} onClick={() => onView?.(r)}>
                  <td>
                    <div className={styles.docCell}>
                      <div className={styles.docIcon} style={{ background: fmt.bg }}>
                        <span>{fmt.icon}</span>
                        <span className={styles.docFmt} style={{ color: fmt.color }}>{r.format}</span>
                      </div>
                      <div>
                        <p className={styles.docTitle}>{r.title}</p>
                      </div>
                    </div>
                  </td>

                  <td className={styles.subjectCell}>{r.subject}</td>

                  <td>
                    <span className={styles.typeBadge}>{r.type}</span>
                  </td>

                  <td>
                    <span className={styles.statusBadge} style={{ background: ss.bg, color: ss.color }}>
                      <span className={styles.statusDot} style={{ background: ss.dot }} />
                      {ss.label}
                    </span>
                  </td>

                  <td className={styles.dateCell}>{date}</td>

                  <td onClick={(e) => e.stopPropagation()}>
                    {r.link ? (
                      <a
                        className={styles.linkBtn}
                        href={r.link}
                        target="_blank"
                        rel="noopener noreferrer"
                        title="Mở tài liệu"
                      >
                        Mở tài liệu
                      </a>
                    ) : (
                      <span className={styles.noLink}>—</span>
                    )}
                  </td>

                  {showActions && (
                    <td onClick={(e) => e.stopPropagation()}>
                      <div className={styles.actionBtns}>
                        <button
                          className={styles.approveBtn}
                          onClick={() => onApprove?.(r.id)}
                          title="Duyệt"
                          aria-label="Duyệt"
                        >
                          <Check size={15} strokeWidth={2.4} />
                        </button>
                        <button
                          className={styles.rejectBtn}
                          onClick={() => onReject?.(r.id)}
                          title="Từ chối"
                          aria-label="Từ chối"
                        >
                          <X size={15} strokeWidth={2.4} />
                        </button>
                        <button
                          className={styles.editBtn}
                          onClick={() => onEdit?.(r)}
                          title="Chỉnh sửa"
                          aria-label="Chỉnh sửa"
                        >
                          <Pencil size={15} strokeWidth={2.2} />
                        </button>
                        <button
                          className={styles.deleteBtn}
                          onClick={() => onDelete?.(r)}
                          title="Xoá"
                          aria-label="Xóa"
                        >
                          <Trash2 size={15} strokeWidth={2.2} />
                        </button>
                      </div>
                    </td>
                  )}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {showPagination && (
        <div className={styles.pagination}>
        <span className={styles.paginationInfo}>
          Hiển thị {start}–{end} trong tổng số {total.toLocaleString()} tài liệu
        </span>
        <div className={styles.paginationControls}>
          <button
            className={styles.pageBtn}
            onClick={() => onPageChange(page - 1)}
            disabled={page === 1}
          >‹</button>

          {pageList.map((p, i) => {
            const prev = pageList[i - 1];
            const showEllipsis = prev && p - prev > 1;
            return (
              <span key={p} className={styles.pageGroup}>
                {showEllipsis && <span className={styles.ellipsis}>…</span>}
                <button
                  className={`${styles.pageBtn} ${p === page ? styles.pageBtnActive : ''}`}
                  onClick={() => onPageChange(p)}
                >{p}</button>
              </span>
            );
          })}

          <button
            className={styles.pageBtn}
            onClick={() => onPageChange(page + 1)}
            disabled={page === totalPages}
          >›</button>
        </div>
        </div>
      )}
    </div>
  );
}
