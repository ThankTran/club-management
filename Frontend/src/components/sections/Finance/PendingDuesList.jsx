import styles from './PendingDuesList.module.css';

const fmtMoney = (value) =>
  `${Number(value || 0).toLocaleString('vi-VN')} đ`;

const STATUS_META = {
  PENDING: { label: 'Chưa đóng', className: 'statusPending' },
  PROCESSING: { label: 'Chờ xác nhận', className: 'statusProcessing' },
  REJECTED: { label: 'Bị từ chối', className: 'statusRejected' },
  FAILED: { label: 'Bị từ chối', className: 'statusRejected' },
};

const getStatusMeta = (status) =>
  STATUS_META[String(status || '').toUpperCase()] || STATUS_META.PENDING;

export default function PendingDuesList({
  members = [],
  loading = false,
}) {
  const totalAmount = members.reduce(
    (sum, member) => sum + Number(member.amount || 0),
    0,
  );

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <div>
          <h3 className={styles.title}>
            Thành viên chưa hoàn thành quỹ
          </h3>
          <p className={styles.subtitle}>
            Bao gồm chưa đóng, chờ xác nhận và bị từ chối
          </p>
        </div>

        <div className={styles.summary}>
          <span>{members.length} thành viên</span>
          <strong>{fmtMoney(totalAmount)}</strong>
        </div>
      </div>

      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Mã TV</th>
              <th>Thành viên</th>
              <th>Vai trò</th>
              <th>Kỳ quỹ</th>
              <th>Trạng thái</th>
              <th>Số tiền</th>
            </tr>
          </thead>

          <tbody>
            {members.map((member) => {
              const statusMeta = getStatusMeta(member.status);

              return (
                <tr key={member.transactionId || member.id}>
                  <td>
                    <span className={styles.idBadge}>
                      {member.id}
                    </span>
                  </td>

                  <td className={styles.name}>
                    {member.name}
                  </td>

                  <td>{member.role}</td>

                  <td>
                    <span className={styles.monthBadge}>
                      {member.month}
                    </span>
                  </td>

                  <td>
                    <span className={`${styles.statusBadge} ${styles[statusMeta.className]}`}>
                      {statusMeta.label}
                    </span>
                  </td>

                  <td className={styles.amount}>
                    {fmtMoney(member.amount)}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>

        {!loading && members.length === 0 && (
          <div className={styles.empty}>
            Tất cả thành viên đã đóng quỹ tháng này
          </div>
        )}

        {loading && (
          <div className={styles.empty}>
            Đang tải danh sách chưa hoàn thành quỹ...
          </div>
        )}
      </div>
    </div>
  );
}
