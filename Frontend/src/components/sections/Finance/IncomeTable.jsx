import { useState } from 'react';
import styles from './IncomeTable.module.css';
import { fmtDate, fmtMoney } from '../../../utils/Finance/financeUtils';
import FinanceFilter from './FinanceFilter';

export default function IncomeTable({
  thuList,
  filteredThu,
  searchThu,
  setSearchThu,
  onOpenThu,
  onEditThu,
  setDeleteTarget,
  sortThu,
  setSortThu,
  filters,
  setFilters,
  page = 1,
  totalPages = 1,
  total = 0,
  pageSize = 10,
  onPageChange,
  loading = false,
}) {
  const [filterOpen, setFilterOpen] = useState(false);
  const start = total === 0 ? 0 : (page - 1) * pageSize + 1;
  const end = Math.min(page * pageSize, total);

  return (
    <div className={styles.tableSection}>
      <div className={styles.tableHeader}>
        <h3 className={styles.tableTitle}>
          Danh sach phieu thu <span className={styles.tableBadgeThu}>{thuList.length} phieu</span>
        </h3>
        <div className={styles.tableActions}>
          <div className={styles.searchWrap}>
            <svg width="13" height="13" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
            </svg>
            <input
              className={styles.searchInput}
              placeholder="Tim phieu thu..."
              value={searchThu}
              onChange={(e) => setSearchThu(e.target.value)}
            />
          </div>
          <FinanceFilter
            open={filterOpen}
            setOpen={setFilterOpen}
            type="income"
            filters={filters}
            setFilters={setFilters}
          />
          <button className={styles.btnThu} onClick={onOpenThu}>+ Lap phieu thu</button>
        </div>
      </div>

      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>MA PHIEU</th>
              <th>NGUOI NOP</th>
              <th>LY DO</th>
              <th>TRANG THAI</th>
              <th>
                <button
                  className={styles.sortBtn}
                  onClick={() => setSortThu(sortThu === 'asc' ? 'desc' : 'asc')}
                >
                  NGAY THU {sortThu === 'asc' ? '↑' : '↓'}
                </button>
              </th>
              <th>SO TIEN</th>
              <th>MA SU KIEN</th>
              <th>THAO TAC</th>
            </tr>
          </thead>
          <tbody>
            {filteredThu.map((r) => (
              <tr key={r.id} className={styles.row}>
                <td><span className={styles.idBadge}>{r.id}</span></td>
                <td className={styles.nameCell}>{r.nguoiNop}</td>
                <td>{r.lyDo}</td>
                <td><span className={styles.hinhThucBadge}>{formatIncomeStatus(r)}</span></td>
                <td className={styles.dateCell}>{fmtDate(r.ngayThu)}</td>
                <td><span className={styles.amtThu}>{fmtMoney(r.soTien)}</span></td>
                <td>{r.maSuKien ? <span className={styles.skBadge}>{r.maSuKien}</span> : <span className={styles.naBadge}>-</span>}</td>
                <td>
                  <div className={styles.rowActions}>
                    <button
                      className={styles.editBtn}
                      onClick={() => onEditThu(r)}
                      title="Sua phieu thu"
                      aria-label="Sua phieu thu"
                    >
                      <svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                        <path d="M12 20h9" />
                        <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5Z" />
                      </svg>
                    </button>
                    <button
                      className={styles.deleteBtn}
                      onClick={() => setDeleteTarget(r)}
                      title="Xoa phieu thu"
                      aria-label="Xoa phieu thu"
                    >
                      <svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                        <path d="M4 7h16" />
                        <path d="M10 11v6" />
                        <path d="M14 11v6" />
                        <path d="M6 7l1 13a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2l1-13" />
                        <path d="M9 7V4h6v3" />
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {!loading && filteredThu.length === 0 && <div className={styles.empty}>Khong tim thay phieu thu nao</div>}
        {loading && <div className={styles.empty}>Dang tai phieu thu...</div>}
      </div>

      <div className={styles.tableFoot}>
        <span>Hien thi {start}-{end} trong tong {total} phieu</span>
      </div>
      <div className={styles.tableFoot}>
        <button className={styles.sortBtn} onClick={() => onPageChange?.(page - 1)} disabled={page <= 1}>Trang truoc</button>
        <span>Trang {page}/{Math.max(totalPages, 1)}</span>
        <button className={styles.sortBtn} onClick={() => onPageChange?.(page + 1)} disabled={page >= totalPages}>Trang sau</button>
      </div>
    </div>
  );
}

function formatIncomeStatus(row) {
  const value = String(row?.status || '').toUpperCase();
  const awaitingConfirmation = value === 'PROCESSING'
    || (value === 'PENDING' && Boolean(row?.raw?.approvedAt) && !row?.raw?.approvedById);
  if (value === 'COMPLETED' || value === 'APPROVED') return 'Da thu';
  if (awaitingConfirmation) return 'Cho xac nhan';
  if (value === 'FAILED') return 'Khong thanh cong';
  return 'Cho dong';
}
