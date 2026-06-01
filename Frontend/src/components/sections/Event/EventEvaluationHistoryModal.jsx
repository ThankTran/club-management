import { useMemo, useState } from 'react';

import styles from './EventEvaluationHistoryModal.module.css';

const formatDate = (value) => {
  if (!value) return '-';
  const [year, month, day] = String(value).slice(0, 10).split('-');
  return year && month && day ? `${day}-${month}-${year}` : value;
};

export default function EventEvaluationHistoryModal({ open, events, onClose, onSelectEvent }) {
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [sortDirection, setSortDirection] = useState('desc');

  const filteredEvents = useMemo(() => {
    const result = events.filter((item) => {
      const evaluationDate = item.evaluationDate || '';
      if (dateFrom && evaluationDate < dateFrom) return false;
      if (dateTo && evaluationDate > dateTo) return false;
      return true;
    });

    return result.sort((a, b) => {
      const dateA = a.evaluationDate || '';
      const dateB = b.evaluationDate || '';
      return sortDirection === 'asc'
        ? dateA.localeCompare(dateB)
        : dateB.localeCompare(dateA);
    });
  }, [dateFrom, dateTo, events, sortDirection]);

  const clearDateFilter = () => {
    setDateFrom('');
    setDateTo('');
  };

  if (!open) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(event) => event.stopPropagation()}>
        <div className={styles.header}>
          <div>
            <h3 className={styles.title}>Lịch sử đánh giá sự kiện</h3>
            <p className={styles.meta}>
              {filteredEvents.length.toLocaleString('vi-VN')} / {events.length.toLocaleString('vi-VN')} sự kiện đã đánh giá
            </p>
          </div>
          <button className={styles.closeBtn} onClick={onClose} title="Đóng">
            <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <path d="M18 6L6 18M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <div className={styles.filters}>
          <label>
            <span>Từ ngày</span>
            <input type="date" value={dateFrom} onChange={(event) => setDateFrom(event.target.value)} />
          </label>
          <label>
            <span>Đến ngày</span>
            <input type="date" value={dateTo} onChange={(event) => setDateTo(event.target.value)} />
          </label>
          <button type="button" className={styles.clearFilterBtn} onClick={clearDateFilter}>
            Reset
          </button>
        </div>

        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Mã sự kiện</th>
                <th>Tên sự kiện</th>
                <th>
                  <button
                    type="button"
                    className={styles.sortBtn}
                    onClick={() => setSortDirection((current) => (current === 'asc' ? 'desc' : 'asc'))}
                    title="Sắp xếp ngày đánh giá"
                  >
                    Ngày đánh giá {sortDirection === 'asc' ? '↑' : '↓'}
                  </button>
                </th>
                <th>Nội dung đánh giá</th>
              </tr>
            </thead>
            <tbody>
              {filteredEvents.length > 0 ? (
                filteredEvents.map((item) => (
                  <tr
                    key={item.id}
                    className={styles.clickableRow}
                    onClick={() => onSelectEvent(item)}
                    title="Xem đánh giá sự kiện"
                  >
                    <td className={styles.codeCell}>{item.eventCode}</td>
                    <td className={styles.centerCell}>{item.title}</td>
                    <td className={styles.centerCell}>{formatDate(item.evaluationDate)}</td>
                    <td className={styles.contentCell}>
                      <span>{item.evaluation}</span>
                      <button
                        className={styles.viewBtn}
                        title="Chỉnh sửa đánh giá"
                        aria-label={`Chỉnh sửa đánh giá ${item.eventCode}`}
                        onClick={(event) => {
                          event.stopPropagation();
                          onSelectEvent(item);
                        }}
                      >
                        <svg width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                          <path d="M12 20h9"/>
                          <path d="M16.5 3.5a2.12 2.12 0 013 3L7 19l-4 1 1-4 12.5-12.5z"/>
                        </svg>
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td className={styles.emptyCell} colSpan={4}>Không có đánh giá phù hợp với bộ lọc</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
