import styles from './ResourceStats.module.css';

const CARDS = [
  { key: 'total', label: 'Tổng phiếu tài liệu', tone: 'blue', hint: 'Toàn bộ phiếu' },
  { key: 'pending', label: 'Chờ duyệt thêm', tone: 'amber', hint: 'Cần xét duyệt' },
  { key: 'fixing', label: 'Chờ duyệt sửa', tone: 'cyan', hint: 'Đang chỉnh sửa' },
  { key: 'approved', label: 'Đã duyệt', tone: 'green', hint: 'Hiển thị khi tra cứu' },
  { key: 'rejected', label: 'Từ chối', tone: 'red', hint: 'Không công khai' },
];

export default function ResourceStats({ stats }) {
  return (
    <div className={styles.grid}>
      {CARDS.map((card) => (
        <div key={card.key} className={`${styles.card} ${styles[card.tone]}`}>
          <p className={styles.label}>{card.label}</p>
          <p className={styles.value}>{stats[card.key]}</p>
          <p className={styles.hint}>{card.hint}</p>
        </div>
      ))}
    </div>
  );
}
