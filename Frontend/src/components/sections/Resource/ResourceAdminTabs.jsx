import styles from './ResourceAdminTabs.module.css';

export default function ResourceAdminTabs({ activeTab, pendingCount, fixingCount, approvedCount, onChange }) {
  return (
    <div className={styles.tabs}>
      <button
        type="button"
        className={`${styles.tabBtn} ${activeTab === 'pending' ? styles.active : ''}`}
        onClick={() => onChange('pending')}
      >
        Chờ duyệt thêm
        <span className={styles.badge}>{pendingCount}</span>
      </button>
      <button
        type="button"
        className={`${styles.tabBtn} ${activeTab === 'fixing' ? styles.active : ''}`}
        onClick={() => onChange('fixing')}
      >
        Chờ duyệt sửa
        <span className={styles.badge}>{fixingCount}</span>
      </button>
      <button
        type="button"
        className={`${styles.tabBtn} ${activeTab === 'lookup' ? styles.active : ''}`}
        onClick={() => onChange('lookup')}
      >
        Tra cứu kho tài liệu
        <span className={styles.badge}>{approvedCount}</span>
      </button>
    </div>
  );
}
