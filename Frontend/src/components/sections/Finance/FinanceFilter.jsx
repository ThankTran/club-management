import { useEffect, useRef } from 'react';

import styles from './FinanceFilter.module.css';

const THU_STATUS_OPTIONS = [
  { value: 'PENDING', label: 'Chờ đóng' },
  { value: 'PROCESSING', label: 'Chờ xác nhận' },
  { value: 'COMPLETED', label: 'Đã thu' },
  { value: 'FAILED', label: 'Không thành công' },
];

const CHI_STATUS_OPTIONS = [
  { value: 'PENDING', label: 'Chờ duyệt' },
  { value: 'COMPLETED', label: 'Đã duyệt' },
  { value: 'REJECTED', label: 'Từ chối' },
];

export default function FinanceFilter({
  open,
  setOpen,
  type,
  filters,
  setFilters,
}) {
  const wrapRef = useRef(null);
  const isIncome = type === 'income';

    const hasFilter = Object.values(filters).some(value => value !== '');

  const updateFilter = (field, value) => {
    setFilters(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleReset = () => {
    if (isIncome) {
        setFilters({
        lyDo: '',
        hinhThuc: '',  
        dateType: '',   
        month: '',     
        quarter: '',   
        year: '',
        });
    } else {
        setFilters({
        noiDung: '',
        nguoiNhan: '',
        status: '',
        dateType: '',
        month: '',
        quarter: '',
        year: '',
        });
    }
    };

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

  return (
    <div className={styles.wrap} ref={wrapRef}>
      <button
        type="button"
        className={`${styles.filterBtn} ${hasFilter ? styles.filterBtnActive : ''}`}
        onClick={() => setOpen(!open)}
      >
        <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
          <path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" />
        </svg>
        Bộ lọc
        {hasFilter && <span className={styles.dot} />}
      </button>

      {open && (
        <div className={styles.dropdown}>
          <p className={styles.dropdownTitle}>
            {isIncome ? 'Lọc phiếu thu' : 'Lọc phiếu chi'}
          </p>

          {isIncome ? (
            <>
              <div className={styles.group}>
                <label className={styles.groupLabel}>Lý do</label>
                <input
                  className={styles.input}
                  placeholder="Nhập lý do..."
                  value={filters.lyDo}
                  onChange={(e) => updateFilter('lyDo', e.target.value)}
                />
              </div>

              <div className={styles.group}>
                <label className={styles.groupLabel}>Trạng thái</label>
                <select
                  className={styles.select}
                  value={filters.hinhThuc}
                  onChange={(e) => updateFilter('hinhThuc', e.target.value)}
                >
                  <option value="">Tất cả</option>
                  {THU_STATUS_OPTIONS.map(item => (
                    <option key={item.value} value={item.value}>{item.label}</option>
                  ))}
                </select>
              </div>
            </>
          ) : (
            <>
              <div className={styles.group}>
                <label className={styles.groupLabel}>Nội dung chi</label>
                <input
                  className={styles.input}
                  placeholder="Nhập nội dung chi..."
                  value={filters.noiDung}
                  onChange={(e) => updateFilter('noiDung', e.target.value)}
                />
              </div>

              <div className={styles.group}>
                <label className={styles.groupLabel}>Người nhận</label>
                <input
                  className={styles.input}
                  placeholder="Nhập người nhận..."
                  value={filters.nguoiNhan}
                  onChange={(e) => updateFilter('nguoiNhan', e.target.value)}
                />
              </div>

              <div className={styles.group}>
                <label className={styles.groupLabel}>Trạng thái</label>
                <select
                  className={styles.select}
                  value={filters.status}
                  onChange={(e) => updateFilter('status', e.target.value)}
                >
                  <option value="">Tất cả</option>
                  {CHI_STATUS_OPTIONS.map(item => (
                    <option key={item.value} value={item.value}>{item.label}</option>
                  ))}
                </select>
              </div>
            </>
          )}

          <div className={styles.group}>
            <label className={styles.groupLabel}>Thời gian</label>
            <select
              className={styles.select}
              value={filters.dateType}
              onChange={(e) => updateFilter('dateType', e.target.value)}
            >
              <option value="">Tất cả</option>
              <option value="month">Theo tháng</option>
              <option value="quarter">Theo quý</option>
              <option value="year">Theo năm</option>
            </select>
          </div>

          {filters.dateType === 'month' && (
            <div className={styles.group}>
              <label className={styles.groupLabel}>Tháng</label>
              <select
                className={styles.select}
                value={filters.month}
                onChange={(e) => updateFilter('month', e.target.value)}
              >
                <option value="">Tất cả</option>
                {Array.from({ length: 12 }, (_, i) => (
                  <option key={i + 1} value={String(i + 1)}>
                    Tháng {i + 1}
                  </option>
                ))}
              </select>
            </div>
          )}

          {filters.dateType === 'quarter' && (
            <div className={styles.group}>
              <label className={styles.groupLabel}>Quý</label>
              <select
                className={styles.select}
                value={filters.quarter}
                onChange={(e) => updateFilter('quarter', e.target.value)}
              >
                <option value="">Tất cả</option>
                <option value="1">Quý 1</option>
                <option value="2">Quý 2</option>
                <option value="3">Quý 3</option>
                <option value="4">Quý 4</option>
              </select>
            </div>
          )}

          {filters.dateType === 'year' && (
            <div className={styles.group}>
              <label className={styles.groupLabel}>Năm</label>
              <input
                className={styles.input}
                placeholder="VD: 2024"
                value={filters.year}
                onChange={(e) => updateFilter('year', e.target.value)}
              />
            </div>
          )}

          <div className={styles.actions}>
            <button type="button" className={styles.resetBtn} onClick={handleReset}>
              Đặt lại
            </button>
            <button type="button" className={styles.applyBtn} onClick={() => setOpen(false)}>
              Áp dụng
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
