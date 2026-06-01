import styles from './TransferDueTable.module.css';
import { fmtMoney } from '../../../utils/Finance/financeUtils';

export default function TransferDueTable({ dues, onRefresh, onConfirmPaid, onRejectPaid }) {
  const waiting = dues.filter((item) => item.status === 'processing');

  return (
    <div className={styles.section}>
      <div className={styles.header}>
        <div>
          <h3 className={styles.title}>Danh sách chờ xác nhận thu</h3>
          <p className={styles.subtitle}>
            Các khoản này do thành viên báo đã thanh toán trên trang Đóng quỹ. Admin kiểm tra tiền thực tế rồi tick để xác nhận hoặc bấm X nếu chưa nhận được tiền.
          </p>
        </div>
        <button type="button" className={styles.refreshBtn} onClick={onRefresh} title="Làm mới danh sách">
          <svg width="17" height="17" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.2">
            <path d="M21 12a9 9 0 11-2.64-6.36" />
            <path d="M21 3v6h-6" />
          </svg>
        </button>
      </div>

      <div className={styles.summary}>
        <span>{waiting.length} chờ xác nhận</span>
      </div>

      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Mã khoản</th>
              <th>Lý do</th>
              <th>Mã sự kiện</th>
              <th>Người cần đóng</th>
              <th>Số tiền</th>
              <th>Trạng thái</th>
              <th>Người báo đóng</th>
              <th>Thời gian</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {dues.map((due) => (
              <tr key={due.id}>
                <td>
                  <div className={styles.codeCell}>
                    <strong>{due.id}</strong>
                    <small>{due.transferCode}</small>
                  </div>
                </td>
                <td>{due.lyDo}</td>
                <td>{due.maSuKien || '-'}</td>
                <td>{due.targetName || '-'}</td>
                <td className={styles.amount}>{fmtMoney(due.soTien)}</td>
                <td>
                  <span className={styles.processingBadge}>Chờ xác nhận</span>
                </td>
                <td>
                  <div className={styles.paidCell}>
                    <span>{due.paidBy || '-'}</span>
                  </div>
                </td>
                <td>{due.paidAt ? new Date(due.paidAt).toLocaleString('vi-VN') : '-'}</td>
                <td>
                  <div className={styles.actions}>
                    <button
                      type="button"
                      className={styles.cashBtn}
                      onClick={() => onConfirmPaid?.(due.id)}
                      title="Xác nhận đã thu"
                      aria-label="Xác nhận đã thu"
                    >
                      <svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
                        <path d="M5 13l4 4L19 7" />
                      </svg>
                    </button>
                    <button
                      type="button"
                      className={styles.deleteBtn}
                      onClick={() => onRejectPaid?.(due.id)}
                      title="Từ chối xác nhận thanh toán"
                      aria-label="Từ chối xác nhận thanh toán"
                    >
                      <svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
                        <path d="M18 6L6 18" />
                        <path d="M6 6l12 12" />
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {dues.length === 0 && (
              <tr>
                <td colSpan={9} className={styles.empty}>Chưa có khoản nào chờ xác nhận thu.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
