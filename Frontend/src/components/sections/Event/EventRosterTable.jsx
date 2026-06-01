import EventFilter from './EventFilter';
import styles from './EventRosterTable.module.css';

const getRateClass = (rate) => {
  if (rate >= 80) return styles.rateHigh;
  if (rate >= 50) return styles.rateMedium;
  return styles.rateLow;
};

const getStatusActionClass = (status) => {
  if (status === 'draft') return styles.statusUpcomingBtn;
  if (status === 'upcoming') return styles.statusRunningBtn;
  if (status === 'published') return styles.statusFinishBtn;
  return '';
};

const STATUS_ACTIONS = {
    draft: { label: 'Chưa bắt đầu', nextStatus: 'upcoming' },
    upcoming: { label: 'Sắp diễn ra', nextStatus: 'published' },
    published: { label: 'Đang diễn ra', nextStatus: 'completed' },
};

export default function EventRosterTable({
  events,
  filteredCount,
  summary,
  page,
  totalPages,
  search,
  setSearch,
  setPage,
  filterOpen,
  setFilterOpen,
  statusFilter,
  tagFilter,
  dateFromFilter,
  dateToFilter,
  onApplyFilters,
  dateSort,
  setDateSort,
  publishedCount,
  statusLabels,
  tagLabels,
  tagFilters,
  evaluations,
  memberCount = 0,
  onEdit,
  onEvaluate,
  onViewRegistrations,
  onUpdateStatus,
  onDelete,
}) {
  return (
    <div className={styles.rosterCard}>
      <div className={styles.rosterHeader}>
        <h2 className={styles.rosterTitle}>Danh sách Sự kiện</h2>
        <div className={styles.rosterActions}>
          <div className={styles.searchWrap}>
            <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
            </svg>
            <input
              className={styles.searchInput}
              placeholder="Tìm sự kiện..."
              value={search}
              onChange={(event) => { setSearch(event.target.value); setPage(1); }}
            />
          </div>
          <EventFilter
            open={filterOpen}
            setOpen={setFilterOpen}
            statusFilter={statusFilter}
            tagFilter={tagFilter}
            dateFromFilter={dateFromFilter}
            dateToFilter={dateToFilter}
            statuses={statusLabels}
            tags={tagFilters}
            onApplyFilters={onApplyFilters}
          />
          <span className={styles.publishedBadge}>{publishedCount} đang diễn ra</span>
        </div>
      </div>

      <div className={styles.summaryGrid}>
        <div className={styles.summaryItem}>
          <p className={styles.summaryLabel}>Số lượng hoạt động</p>
          <strong>{summary.activityCount}</strong>
        </div>
        <div className={styles.summaryItem}>
          <p className={styles.summaryLabel}>Người tham gia</p>
          <strong>{summary.totalAttendance.toLocaleString('vi-VN')}</strong>
        </div>
        <div className={styles.summaryItem}>
          <p className={styles.summaryLabel}>Tỷ lệ tham gia</p>
          <strong className={getRateClass(summary.attendanceRate)}>
            {summary.attendanceRate}%
          </strong>
        </div>
      </div>

      <div className={styles.tableScroll}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>MÃ SỰ KIỆN</th>
              <th>TÊN SỰ KIỆN</th>
              <th>LOẠI SỰ KIỆN</th>
              <th>TRẠNG THÁI</th>
              <th>
                <div className={styles.dateHead}>
                  NGÀY
                  <button
                    className={styles.sortBtn}
                    onClick={() => setDateSort(dateSort === 'nearest' ? 'farthest' : 'nearest')}
                    title="Sắp xếp ngày"
                  >
                    {dateSort === 'nearest' ? '↑' : '↓'}
                  </button>
                </div>
              </th>
              <th>THAM DỰ</th>
              <th>TỶ LỆ</th>
              <th>CẬP NHẬT</th>
              <th>THAO TÁC</th>
              <th>DANH SÁCH ĐĂNG KÝ</th>
            </tr>
          </thead>
          <tbody>
            {events.map((event) => {
              const tag = tagLabels[event.tag] || tagLabels.OTHER;
              const date = new Date(event.date);
              const registeredCount = Number(event.attendance || 0);
              const percent = memberCount > 0
                ? Math.round((registeredCount / memberCount) * 100)
                : 0;
              const isFinished = event.status === 'completed' || event.status === 'evaluated';
                const hasEvaluation =
                    evaluations.some(
                        (item) =>
                            item.eventCode === event.eventCode &&
                            item.evaluationDate &&
                            item.evaluation &&
                            item.evaluation.trim() !== ''
                    ) ||
                    Boolean(event.evaluationDate && event.evaluation && event.evaluation.trim() !== '');
              const displayStatus = isFinished && hasEvaluation ? 'evaluated' : event.status;
              const status = statusLabels[displayStatus] || statusLabels.upcoming;
              const statusAction = STATUS_ACTIONS[event.status];

              return (
                <tr key={event.id} className={styles.row}>
                  <td className={styles.codeCell}>{event.eventCode}</td>
                  <td>
                    <p className={styles.eventName}>{event.title}</p>
                    <p className={styles.eventLocation}>{event.location} · {event.time} - {event.endTime}</p>
                  </td>
                  <td>
                    <span className={styles.tagBadge} style={{ background: tag.bg, color: tag.color }}>
                      {tag.label}
                    </span>
                  </td>
                  <td>
                    <span className={styles.statusBadge} style={{ background: status.bg, color: status.color }}>
                      {status.label}
                    </span>
                  </td>
                  <td className={styles.dateCell}>
                    {date.toLocaleDateString('vi-VN', { day: '2-digit', month: 'short', year: 'numeric' })}
                  </td>
                  <td>
                    <span className={styles.attendance}>{registeredCount} / {memberCount}</span>
                  <div className={styles.attBar}>
                    <div className={styles.attBarFill} style={{ width: `${percent}%` }} />
                  </div>
                </td>
                <td>
                    <span className={`${styles.rateBadge} ${getRateClass(percent)}`}>
                      {percent}%
                    </span>
                  </td>
                <td className={styles.centerCell}>
                  {statusAction ? (
                    <button
                      className={`${styles.statusActionBtn} ${getStatusActionClass(event.status)}`}
                      onClick={() => onUpdateStatus(event.id, statusAction.nextStatus)}
                    >
                      {statusAction.label}
                    </button>
                  ) : (
                    <button className={styles.statusDoneBtn} disabled>
                      {status.label}
                    </button>
                  )}
                </td>
                <td className={styles.centerCell}>
                  <div className={styles.actionBtns}>
                    {event.status === 'cancelled' ? (
                      <button className={styles.detailBtn} onClick={() => onEdit(event)} title="Xem chi tiết">
                        <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                          <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
                          <path d="M14 2v6h6M8 13h8M8 17h5"/>
                        </svg>
                      </button>
                    ) : (
                      <>
                        <button className={styles.editBtn} onClick={() => onEdit(event)} title="Chỉnh sửa">
                          <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                            <path d="M8 2v4M16 2v4M3 10h18"/>
                            <path d="M5 4h14a2 2 0 012 2v13a2 2 0 01-2 2H5a2 2 0 01-2-2V6a2 2 0 012-2z"/>
                            <path d="M14.5 13.5l2 2L12 20H10v-2z"/>
                          </svg>
                        </button>
                        {isFinished && (
                          <button
                            className={hasEvaluation ? styles.viewEvaluationBtn : styles.evaluateBtn}
                            onClick={() => onEvaluate(event)}
                            title={hasEvaluation ? 'Xem đánh giá' : 'Viết đánh giá'}
                          >
                            {hasEvaluation ? (
                              <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                                <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12z"/>
                                <circle cx="12" cy="12" r="3"/>
                              </svg>
                            ) : (
                              <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                                <path d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5"/>
                                <path d="M18.5 2.5a2.12 2.12 0 013 3L12 15l-3 1 1-3z"/>
                              </svg>
                            )}
                          </button>
                        )}
                        <button className={styles.cancelEventBtn} onClick={() => onUpdateStatus(event.id, 'cancelled')} title="Hủy sự kiện">
                          <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                            <circle cx="12" cy="12" r="10"/><path d="M15 9l-6 6M9 9l6 6"/>
                          </svg>
                        </button>
                      </>
                    )}
                  </div>
                </td>
                <td className={styles.centerCell}>
                  <button
                    className={styles.registrationsBtn}
                    onClick={() => onViewRegistrations(event)}
                  >
                    Xem
                  </button>
                </td>
              </tr>
            );
          })}
        </tbody>
        </table>
      </div>

      <div className={styles.pagination}>
        <span className={styles.paginationInfo}>{filteredCount} sự kiện</span>
        <div className={styles.paginationBtns}>
          <button className={styles.pageBtn} disabled={page === 1} onClick={() => setPage(page - 1)}>‹</button>
          {Array.from({ length: totalPages }, (_, i) => i + 1).map((pageNumber) => (
            <button
              key={pageNumber}
              className={`${styles.pageBtn} ${pageNumber === page ? styles.pageBtnActive : ''}`}
              onClick={() => setPage(pageNumber)}
            >
              {pageNumber}
            </button>
          ))}
          <button className={styles.pageBtn} disabled={page === totalPages} onClick={() => setPage(page + 1)}>›</button>
        </div>
      </div>
    </div>
  );
}
