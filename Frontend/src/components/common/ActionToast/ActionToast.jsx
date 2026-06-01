import styles from './ActionToast.module.css';

export default function ActionToast({ toast }) {
  if (!toast?.message) return null;

  const typeClass = styles[toast.type] || '';
  const icon = toast.type === 'success' ? '✓' : toast.type === 'error' ? '⚠' : '…';

  return (
    <div className={`${styles.toast} ${typeClass}`}>
      <div className={styles.toastIcon}>{icon}</div>
      <div className={styles.toastMessage}>{toast.message}</div>
    </div>
  );
}

