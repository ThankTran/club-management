import styles from './MemberDeleteConfirmModal.module.css';

export default function MemberDeleteConfirmModal({ member, onClose, onConfirm, loading = false }) {
  if (!member) return null;

  return (
    <div className={styles.confirmOverlay} onClick={onClose}>
      <div className={styles.confirmBox} onClick={(e) => e.stopPropagation()}>
        <div className={styles.confirmIcon}>
          <svg width="28" height="28" fill="none" viewBox="0 0 24 24" stroke="#e53e3e" strokeWidth="1.8">
            <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
            <line x1="12" y1="9" x2="12" y2="13" />
            <line x1="12" y1="17" x2="12.01" y2="17" />
          </svg>
        </div>
        <h3 className={styles.confirmTitle}>Xóa hồ sơ?</h3>
        <p className={styles.confirmMsg}>
          Bạn có chắc muốn xóa <strong>{member.name}</strong>? Hành động này không thể hoàn tác.
        </p>
        <div className={styles.confirmActions}>
          <button className={styles.confirmCancel} onClick={onClose} disabled={loading}>Hủy</button>
          <button className={styles.confirmDelete} onClick={onConfirm} disabled={loading}>
            {loading ? 'Đang xóa...' : 'Xóa'}
          </button>
        </div>
      </div>
    </div>
  );
}
