import styles from './EventDeleteConfirmModal.module.css';

export default function EventDeleteConfirmModal({ event, onCancel, onConfirm, loading = false, label = 'Xoá' }) {
  if (!event) return null;

  return (
    <div className={styles.overlay} onClick={onCancel}>
      <div className={styles.box} onClick={(e) => e.stopPropagation()}>
        <div className={styles.icon}>
          <svg width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="#e53e3e" strokeWidth="1.8">
            <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
        </div>
        <h3 className={styles.title}>Xoá sự kiện?</h3>
        <p className={styles.message}>
          Bạn có chắc muốn xoá <strong>{event.title}</strong>? Hành động này không thể hoàn tác.
        </p>
        <div className={styles.actions}>
          <button className={styles.cancelBtn} onClick={onCancel} disabled={loading}>Hủy</button>
          <button className={styles.deleteBtn} onClick={onConfirm} disabled={loading}>
            {loading ? 'Đang xử lý...' : label}
          </button>
        </div>
      </div>
    </div>
  );
}
