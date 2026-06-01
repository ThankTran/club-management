import styles from './AccountDeleteConfirmModal.module.css';

export default function AccountDeleteConfirmModal({ account, onCancel, onConfirm, loading = false }) {
  if (!account) return null;

  return (
    <div className={styles.confirmOverlay} onClick={onCancel}>
      <div className={styles.confirmBox} onClick={(e) => e.stopPropagation()}>
        <div className={styles.confirmIcon}>!</div>
        <h3 className={styles.confirmTitle}>Xóa tài khoản?</h3>
        <p className={styles.confirmMsg}>
          Bạn có chắc muốn xóa tài khoản <strong>{account.username || account.id}</strong>? Hành động này không thể hoàn tác.
        </p>
        <div className={styles.confirmActions}>
          <button className={styles.confirmCancel} onClick={onCancel} disabled={loading}>Hủy</button>
          <button className={styles.confirmDelete} onClick={onConfirm} disabled={loading}>
            {loading ? 'Đang xóa...' : 'Xóa'}
          </button>
        </div>
      </div>
    </div>
  );
}

