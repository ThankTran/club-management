import { useEffect, useRef } from 'react';

import styles from './ResourceFilter.module.css';

const TYPES    = ['Giáo trình', 'Slide bài giảng', 'Tài liệu tham khảo', 'Khác'];
const FORMATS  = ['PDF', 'DOCX', 'PPT', 'Khác'];

/**
 * Props:
 *   open            – boolean
 *   setOpen         – fn
 *   typeFilter      – string
 *   setTypeFilter   – fn
 *   formatFilter    – string
 *   setFormatFilter – fn
 *   subjectFilter   – string
 *   setSubjectFilter– fn
 *   subjects        – string[]   (danh sách môn học từ data)
 *   statusFilter    – string     (chỉ dùng ở AdminPage, optional)
 *   setStatusFilter – fn         (optional)
 *   showStatus      – boolean    (hiện filter trạng thái hay không)
 */
export default function ResourceFilter({
  open,
  setOpen,
  typeFilter,
  setTypeFilter,
  formatFilter,
  setFormatFilter,
  subjectFilter,
  setSubjectFilter,
  subjects = [],
  statusFilter,
  setStatusFilter,
  showStatus = false,
}) {
  const wrapRef = useRef(null);
  const hasFilter =
    typeFilter !== 'all' ||
    formatFilter !== 'all' ||
    subjectFilter !== 'all' ||
    (showStatus && statusFilter !== 'all');

  const handleReset = () => {
    setTypeFilter('all');
    setFormatFilter('all');
    setSubjectFilter('all');
    if (showStatus && setStatusFilter) setStatusFilter('all');
  };

  useEffect(() => {
    if (!open) return undefined;

    const handlePointerDown = (event) => {
      if (wrapRef.current && !wrapRef.current.contains(event.target)) {
        setOpen(false);
      }
    };

    document.addEventListener('mousedown', handlePointerDown);
    document.addEventListener('touchstart', handlePointerDown);

    return () => {
      document.removeEventListener('mousedown', handlePointerDown);
      document.removeEventListener('touchstart', handlePointerDown);
    };
  }, [open, setOpen]);

  return (
    <div className={styles.wrap} ref={wrapRef}>
      <button
        className={`${styles.filterBtn} ${hasFilter ? styles.filterBtnActive : ''}`}
        onClick={() => setOpen(!open)}
      >
        <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
          <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z"/>
        </svg>
        Bộ lọc
        {hasFilter && <span className={styles.dot} />}
      </button>

      {open && (
        <div className={styles.dropdown}>
          <p className={styles.dropdownTitle}>Lọc tài liệu</p>

          {/* Loại tài liệu */}
          <div className={styles.group}>
            <label className={styles.groupLabel}>Loại tài liệu</label>
            <select
              className={styles.select}
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value)}
            >
              <option value="all">Tất cả</option>
              {TYPES.map((t) => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
          </div>

          {/* Định dạng */}
          <div className={styles.group}>
            <label className={styles.groupLabel}>Định dạng</label>
            <select
              className={styles.select}
              value={formatFilter}
              onChange={(e) => setFormatFilter(e.target.value)}
            >
              <option value="all">Tất cả</option>
              {FORMATS.map((f) => (
                <option key={f} value={f}>{f}</option>
              ))}
            </select>
          </div>

          {/* Môn học / Chủ đề */}
          <div className={styles.group}>
            <label className={styles.groupLabel}>Môn học / Chủ đề</label>
            <select
              className={styles.select}
              value={subjectFilter}
              onChange={(e) => setSubjectFilter(e.target.value)}
            >
              <option value="all">Tất cả</option>
              {subjects.map((s) => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
          </div>

          {/* Trạng thái duyệt (chỉ Admin) */}
          {showStatus && (
            <div className={styles.group}>
              <label className={styles.groupLabel}>Trạng thái</label>
              <select
                className={styles.select}
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="all">Tất cả</option>
                <option value="pending">Chờ duyệt thêm</option>
                <option value="fixing">Chờ duyệt sửa</option>
                <option value="approved">Đã duyệt</option>
                <option value="rejected">Từ chối</option>
              </select>
            </div>
          )}

          <div className={styles.actions}>
            <button className={styles.resetBtn} onClick={handleReset}>
              Đặt lại
            </button>
            <button className={styles.applyBtn} onClick={() => setOpen(false)}>
              Áp dụng
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
