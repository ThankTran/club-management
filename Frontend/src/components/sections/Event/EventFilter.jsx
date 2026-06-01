import { useEffect, useRef, useState } from 'react';

import styles from './EventFilter.module.css';

export default function EventFilter({
  open,
  setOpen,
  statusFilter,
  tagFilter,
  dateFromFilter,
  dateToFilter,
  statuses,
  tags,
  onApplyFilters,
}) {
  const wrapRef = useRef(null);
  const [draftStatusFilter, setDraftStatusFilter] = useState(statusFilter);
  const [draftTagFilter, setDraftTagFilter] = useState(tagFilter);
  const [draftDateFromFilter, setDraftDateFromFilter] = useState(dateFromFilter);
  const [draftDateToFilter, setDraftDateToFilter] = useState(dateToFilter);

  useEffect(() => {
    if (!open) return;
    setDraftStatusFilter(statusFilter);
    setDraftTagFilter(tagFilter);
    setDraftDateFromFilter(dateFromFilter);
    setDraftDateToFilter(dateToFilter);
  }, [dateFromFilter, dateToFilter, open, statusFilter, tagFilter]);

  useEffect(() => {
    if (!open) return undefined;

    const handlePointerDown = (event) => {
      if (wrapRef.current && !wrapRef.current.contains(event.target)) {
        setOpen(false);
      }
    };

    document.addEventListener('mousedown', handlePointerDown);
    document.addEventListener('touchstart', handlePointerDown);

    return () => {
      document.removeEventListener('mousedown', handlePointerDown);
      document.removeEventListener('touchstart', handlePointerDown);
    };
  }, [open, setOpen]);

  const handleReset = () => {
    setDraftStatusFilter('all');
    setDraftTagFilter('all');
    setDraftDateFromFilter('');
    setDraftDateToFilter('');
  };

  const handleApply = () => {
    const shouldSwapDates =
      draftDateFromFilter && draftDateToFilter && draftDateFromFilter > draftDateToFilter;

    onApplyFilters({
      statusFilter: draftStatusFilter,
      tagFilter: draftTagFilter,
      dateFromFilter: shouldSwapDates ? draftDateToFilter : draftDateFromFilter,
      dateToFilter: shouldSwapDates ? draftDateFromFilter : draftDateToFilter,
    });
    setOpen(false);
  };

  return (
    <div className={styles.wrap} ref={wrapRef}>
      <button
        className={styles.filterBtn}
        onClick={() => setOpen(!open)}
      >
        <svg
          width="15"
          height="15"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth="2"
        >
          <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" />
        </svg>

        Bộ lọc
      </button>

      {open && (
        <div className={styles.dropdown}>
          {/* STATUS */}
          <div className={styles.group}>
            <label>Trạng thái</label>

            <select
              value={draftStatusFilter}
              onChange={(e) =>
                setDraftStatusFilter(e.target.value)
              }
            >
              <option value="all">
                Tất cả
              </option>

              {Object.entries(statuses).map(([key, value]) => (
                <option key={key} value={key}>
                  {value.label}
                </option>
              ))}
            </select>
          </div>

          {/* TAG */}
          <div className={styles.group}>
            <label>Thẻ</label>

            <select
              value={draftTagFilter}
              onChange={(e) =>
                setDraftTagFilter(e.target.value)
              }
            >
              <option value="all">
                Tất cả
              </option>

              {tags.map((item) => (
                <option
                  key={item}
                  value={item}
                >
                  {item}
                </option>
              ))}
            </select>
          </div>

          <div className={styles.dateGrid}>
            <div className={styles.group}>
              <label>Từ ngày</label>
              <input
                type="date"
                value={draftDateFromFilter}
                onChange={(e) => setDraftDateFromFilter(e.target.value)}
              />
            </div>

            <div className={styles.group}>
              <label>Đến ngày</label>
              <input
                type="date"
                value={draftDateToFilter}
                onChange={(e) => setDraftDateToFilter(e.target.value)}
              />
            </div>
          </div>

          <div className={styles.actions}>
            <button
              className={styles.resetBtn}
              onClick={handleReset}
            >
              Reset
            </button>

            <button
              className={styles.applyBtn}
              onClick={handleApply}
            >
              Áp dụng
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
