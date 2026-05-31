import { useCallback, useEffect, useState, useMemo } from 'react';
import styles from './FinancePage.module.css';

import { getThang } from '../../utils/Finance/financeUtils';

import FinanceHeader from '../../components/sections/Finance/FinanceHeader';
import FinanceStats from '../../components/sections/Finance/FinanceStats';
import FinanceTabs from '../../components/sections/Finance/FinanceTabs';
import FinanceOverview from '../../components/sections/Finance/FinanceOverview';
import IncomeTable from '../../components/sections/Finance/IncomeTable';
import ExpenseTable from '../../components/sections/Finance/ExpenseTable';
import FinanceReport from '../../components/sections/Finance/FinanceReport';
import IncomeFormModal from '../../components/sections/Finance/IncomeFormModal';
import ExpenseFormModal from '../../components/sections/Finance/ExpenseFormModal';
import ConfirmModal from '../../components/sections/Finance/ConfirmModal';
import TransferDueTable from '../../components/sections/Finance/TransferDueTable';
import {
  completeTransactionAPI,
  createTransactionAPI,
  deleteTransactionAPI,
  getPendingMonthlyDuesAPI,
  getTransactionsAPI,
  normalizeMemberDueFromApi,
  normalizeTransactionFromApi,
  rejectTransactionPaymentAPI,
  toExpensePayload,
  toIncomePayload,
  updateTransactionAPI,
} from '../../services/finance-service';
import { getMembersAPI, normalizeMemberFromApi } from '../../services/member-service';
import useAuthStore from '../../store/auth-store';
import { isManager } from '../../utils/access-control';

export default function FinancePage() {
  const currentUser = useAuthStore((state) => state.user);
  const [thuList, setThuList] = useState([]);
  const [chiList, setChiList] = useState([]);
  const [transferDues, setTransferDues] = useState([]);
  const [memberOptions, setMemberOptions] = useState([]);
  const [pendingDues, setPendingDues] = useState([]);
  const [pendingDuesLoading, setPendingDuesLoading] = useState(false);
  const [apiError, setApiError] = useState('');

  const [thuFilters, setThuFilters] = useState({
    lyDo: '',
    hinhThuc: '',
    dateType: '',
    month: '',
    quarter: '',
    year: '',
  });

  const [chiFilters, setChiFilters] = useState({
    noiDung: '',
    nguoiNhan: '',
    status: '',
    dateType: '',
    month: '',
    quarter: '',
    year: '',
  });

  const [tab, setTab] = useState('overview');

  const [thuOpen, setThuOpen]       = useState(false);
  const [chiOpen, setChiOpen]       = useState(false);
  const [editThu, setEditThu]       = useState(null);
  const [editChi, setEditChi]       = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const [baocaoThang, setBaocaoThang] = useState(new Date().getMonth() + 1);

  const [searchThu, setSearchThu] = useState('');
  const [searchChi, setSearchChi] = useState('');
  const [sortThu, setSortThu] = useState('desc');
  const [sortChi, setSortChi] = useState('desc');

  const loadFinanceData = useCallback((ignoreRef = { current: false }) => {
    setPendingDuesLoading(true);

    return Promise.allSettled([getTransactionsAPI(), getPendingMonthlyDuesAPI(), getMembersAPI()])
      .then(([transactionsResult, dueResult, membersResult]) => {
        if (ignoreRef.current) return;
        const data = transactionsResult.status === 'fulfilled' ? transactionsResult.value : [];
        const dueData = dueResult.status === 'fulfilled' ? dueResult.value : [];
        const memberData = membersResult.status === 'fulfilled' ? membersResult.value : [];
        const transactions = Array.isArray(data) ? data.map(normalizeTransactionFromApi) : [];
        const income = transactions.filter((item) => item.raw?.type === 'INCOME');
        const expense = transactions.filter((item) => item.raw?.type !== 'INCOME');

        setThuList(income);
        setChiList(expense);
        setTransferDues(income.filter((item) => String(item.status || '').toUpperCase() === 'PROCESSING').map(toTransferDueRow));
        setPendingDues(Array.isArray(dueData) ? dueData.map(normalizeMemberDueFromApi) : []);
        setMemberOptions(Array.isArray(memberData) ? memberData.map(normalizeMemberFromApi) : []);
        setApiError('');
      })
      .catch((error) => {
        if (ignoreRef.current) return;
        setThuList([]);
        setChiList([]);
        setPendingDues([]);
        setMemberOptions([]);
        setApiError(error?.message || 'Không tải được dữ liệu thu chi từ API.');
      })
      .finally(() => {
        if (!ignoreRef.current) setPendingDuesLoading(false);
      });
  }, []);

  useEffect(() => {
    const ignoreRef = { current: false };
    loadFinanceData(ignoreRef);

    return () => {
      ignoreRef.current = true;
    };
  }, [loadFinanceData]);

  useEffect(() => {
    const refreshFinanceData = () => {
      loadFinanceData();
    };

    window.addEventListener('finance:transactions-updated', refreshFinanceData);
    window.addEventListener('focus', refreshFinanceData);

    return () => {
      window.removeEventListener('finance:transactions-updated', refreshFinanceData);
      window.removeEventListener('focus', refreshFinanceData);
    };
  }, [loadFinanceData]);

  const completedThuList = useMemo(() => thuList.filter(isSettledTransaction), [thuList]);
  const completedChiList = useMemo(() => chiList.filter(isSettledTransaction), [chiList]);
  const canApproveExpense = isManager(currentUser);

  const tongThu = completedThuList.reduce((s, r) => s + r.soTien, 0);
  const tongChi = completedChiList.reduce((s, r) => s + r.soTien, 0);
  const soDu    = tongThu - tongChi;

  const bcThu = useMemo(() => completedThuList.filter(r => getThang(r.ngayThu) === baocaoThang), [completedThuList, baocaoThang]);
  const bcChi = useMemo(() => completedChiList.filter(r => getThang(r.ngayLap) === baocaoThang), [completedChiList, baocaoThang]);
  const bcTongThu = bcThu.reduce((s, r) => s + r.soTien, 0);
  const bcTongChi = bcChi.reduce((s, r) => s + r.soTien, 0);
  const bcSoDu    = bcTongThu - bcTongChi;

  // Lọc ngày
  const matchDateFilter = (dateStr, filters) => {
    if (!filters.dateType || filters.dateType === '') return true;  // ← thêm check rỗng
    
    const date = new Date(dateStr);
    const month = date.getMonth() + 1;
    const year = date.getFullYear();
    const quarter = Math.ceil(month / 3);

    if (filters.dateType === 'month') {
      return !filters.month || month === Number(filters.month);
    }
    if (filters.dateType === 'quarter') {
      return !filters.quarter || quarter === Number(filters.quarter);
    }
    if (filters.dateType === 'year') {
      return !filters.year || year === Number(filters.year);
    }
    return true;
  };

  const filteredThu = useMemo(() => {
    const q = searchThu.toLowerCase();

    const filtered = thuList.filter(r => {
      const matchSearch =
        !q ||
        r.nguoiNop.toLowerCase().includes(q) ||
        r.lyDo.toLowerCase().includes(q) ||
        r.id.toLowerCase().includes(q);

      const matchLyDo =
        !thuFilters.lyDo ||
        r.lyDo.toLowerCase().includes(thuFilters.lyDo.toLowerCase());

      const matchHinhThuc =
        !thuFilters.hinhThuc ||
        r.hinhThuc === thuFilters.hinhThuc;

      const matchDate = matchDateFilter(r.ngayThu, thuFilters);

      return matchSearch && matchLyDo && matchHinhThuc && matchDate;
    });

    return filtered.sort((a, b) => {
      const da = new Date(a.ngayThu);
      const db = new Date(b.ngayThu);
      return sortThu === 'asc' ? da - db : db - da;
    });
  }, [thuList, searchThu, thuFilters, sortThu]);

  const filteredChi = useMemo(() => {
    const q = searchChi.toLowerCase();

    const filtered = chiList.filter(r => {
      const matchSearch =
        !q ||
        r.nguoiNhan.toLowerCase().includes(q) ||
        r.noiDung.toLowerCase().includes(q) ||
        r.id.toLowerCase().includes(q);

      const matchNoiDung =
        !chiFilters.noiDung ||
        r.noiDung.toLowerCase().includes(chiFilters.noiDung.toLowerCase());

      const matchNguoiNhan =
        !chiFilters.nguoiNhan ||
        r.nguoiNhan.toLowerCase().includes(chiFilters.nguoiNhan.toLowerCase());

      const matchStatus =
        !chiFilters.status ||
        (chiFilters.status === 'COMPLETED'
          ? ['COMPLETED', 'APPROVED'].includes(String(r.status || '').toUpperCase())
          : String(r.status || '').toUpperCase() === chiFilters.status);

      const matchDate = matchDateFilter(r.ngayLap, chiFilters);

      return matchSearch && matchNoiDung && matchNguoiNhan && matchStatus && matchDate;
    });

    return filtered.sort((a, b) => {
      const da = new Date(a.ngayLap);
      const db = new Date(b.ngayLap);
      return sortChi === 'asc' ? da - db : db - da;
    });
  }, [chiList, searchChi, chiFilters, sortChi]);

  const openThuModal = () => { setEditThu(null); setThuOpen(true); };
  const openChiModal = () => { setEditChi(null); setChiOpen(true); };
  const openEditThuModal = (r) => { setEditThu(r); setThuOpen(true); };
  const openEditChiModal = (r) => { setEditChi(r); setChiOpen(true); };

  const refreshTransferDuesAndReceipts = () => {
    loadFinanceData();
  };

  const handleConfirmTransferPaid = async (id) => {
    try {
      await completeTransactionAPI(id);
      await loadFinanceData();
      setApiError('');
    } catch (error) {
      setApiError(error?.message || 'Không xác nhận được khoản chuyển khoản.');
    }
  };

  const handleRejectTransferPaid = async (id) => {
    try {
      await rejectTransactionPaymentAPI(id);
      await loadFinanceData();
      setApiError('');
    } catch (error) {
      setApiError(error?.message || 'Không từ chối được khoản chuyển khoản.');
    }
  };

  const handleThuSubmit = async (data) => {
    setFormLoading(true);
    try {
      if (editThu) {
        const updated = await updateTransactionAPI(editThu.id, toIncomePayload({ ...editThu, ...data }));
        setThuList(p => p.map(r => r.id === editThu.id ? normalizeTransactionFromApi(updated) : r));
      } else {
        const records = Array.isArray(data) ? data : [data];
        const created = await Promise.all(records.map((record, index) => {
          const localRecord = {
            ...record,
            id: record.id || `THU${String(thuList.length + index + 1).padStart(3, '0')}`,
          };
          return createTransactionAPI(toIncomePayload(localRecord));
        }));
        setThuList(p => [...p, ...created.map(normalizeTransactionFromApi)]);
      }
      setThuOpen(false);
      setEditThu(null);
      setApiError('');
    } catch (error) {
      setApiError(error?.message || 'Không lưu được phiếu thu.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleChiSubmit = async (data) => {
    setFormLoading(true);
    try {
      if (editChi) {
        const updated = await updateTransactionAPI(editChi.id, toExpensePayload({ ...editChi, ...data }));
        setChiList(p => p.map(r => r.id === editChi.id ? normalizeTransactionFromApi(updated) : r));
      } else {
        const localRecord = { ...data, id: data.id || `CHI${String(chiList.length + 1).padStart(3, '0')}` };
        const created = await createTransactionAPI(toExpensePayload(localRecord));
        setChiList(p => [...p, normalizeTransactionFromApi(created)]);
      }
      setChiOpen(false);
      setEditChi(null);
      setApiError('');
    } catch (error) {
      setApiError(error?.message || 'Không lưu được phiếu chi.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleApproveExpense = async (item) => {
    try {
      const updated = await completeTransactionAPI(item.id);
      setChiList((prev) => prev.map((row) => row.id === item.id ? normalizeTransactionFromApi(updated) : row));
      setApiError('');
    } catch (error) {
      setApiError(error?.message || 'KhÃ´ng duyá»‡t Ä‘Æ°á»£c phiáº¿u chi.');
    }
  };

  const handleRejectExpense = async (item) => {
    try {
      const updated = await updateTransactionAPI(
        item.id,
        toExpensePayload({ ...item, status: 'REJECTED' }),
      );
      setChiList((prev) => prev.map((row) => row.id === item.id ? normalizeTransactionFromApi(updated) : row));
      setApiError('');
    } catch (error) {
      setApiError(error?.message || 'Không từ chối được phiếu chi.');
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      await deleteTransactionAPI(deleteTarget.id);
      setThuList(p => p.filter(r => r.id !== deleteTarget.id));
      setChiList(p => p.filter(r => r.id !== deleteTarget.id));
      setDeleteTarget(null);
      setApiError('');
    } catch (error) {
      setApiError(error?.message || 'Không xoá được giao dịch.');
    }
  };

  return (
    <div className={styles.page}>
      {apiError && <div className={styles.apiError}>{apiError}</div>}

      <FinanceHeader
        onOpenThu={openThuModal}
        onOpenChi={openChiModal}
        thuList={thuList}
        chiList={chiList}
        bcThu={bcThu}
        bcChi={bcChi}
      />
      <FinanceStats
        tongThu={tongThu}
        tongChi={tongChi}
        soDu={soDu}
        thuCount={thuList.length}
        chiCount={chiList.length}
        bcThu={bcThu}
        baocaoThang={baocaoThang}
      />

      <FinanceTabs tab={tab} setTab={setTab} />

      {tab === 'overview' && (
        <FinanceOverview
          thuList={thuList}
          chiList={chiList}
          pendingDues={pendingDues}
          pendingDuesLoading={pendingDuesLoading}
          setTab={setTab}
        />
      )}

      {tab === 'thu' && (
        <IncomeTable
          thuList={thuList}
          filteredThu={filteredThu}
          searchThu={searchThu}
          setSearchThu={setSearchThu}
          onOpenThu={openThuModal}
          onEditThu={openEditThuModal}
          setDeleteTarget={setDeleteTarget}
          sortThu={sortThu}
          setSortThu={setSortThu}
          filters={thuFilters}
          setFilters={setThuFilters}
        />
      )}

      {tab === 'chi' && (
        <ExpenseTable
          chiList={chiList}
          filteredChi={filteredChi}
          searchChi={searchChi}
          setSearchChi={setSearchChi}
          onOpenChi={openChiModal}
          onEditChi={openEditChiModal}
          setDeleteTarget={setDeleteTarget}
          onApproveChi={handleApproveExpense}
          onRejectChi={handleRejectExpense}
          canApproveExpense={canApproveExpense}
          sortChi={sortChi}
          setSortChi={setSortChi}
          filters={chiFilters}
          setFilters={setChiFilters}
        />
      )}

      {tab === 'chuyenKhoan' && (
        <TransferDueTable
          dues={transferDues}
          onRefresh={refreshTransferDuesAndReceipts}
          onConfirmPaid={handleConfirmTransferPaid}
          onRejectPaid={handleRejectTransferPaid}
        />
      )}

      {tab === 'baocao' && (
        <FinanceReport
          baocaoThang={baocaoThang}
          setBaocaoThang={setBaocaoThang}
          bcThu={bcThu}
          bcChi={bcChi}
          bcTongThu={bcTongThu}
          bcTongChi={bcTongChi}
          bcSoDu={bcSoDu}
        />
      )}

      <IncomeFormModal
        open={thuOpen}
        onClose={() => { setThuOpen(false); setEditThu(null); }}
        onSubmit={handleThuSubmit}
        initial={editThu}
        loading={formLoading}
        memberOptions={memberOptions}
      />

      <ExpenseFormModal
        open={chiOpen}
        onClose={() => { setChiOpen(false); setEditChi(null); }}
        onSubmit={handleChiSubmit}
        initial={editChi}
        loading={formLoading}
      />
      <ConfirmModal item={deleteTarget} onConfirm={handleDelete} onCancel={() => setDeleteTarget(null)} />
    </div>

    
  );
}

function isSettledTransaction(item) {
  return ['COMPLETED', 'APPROVED'].includes(String(item?.status || item?.raw?.status || '').toUpperCase());
}

function toTransferDueRow(item) {
  return {
    id: item.id,
    transferCode: `${item.maSuKien || 'QUY'}-${item.id}`,
    lyDo: item.lyDo,
    maSuKien: item.maSuKien,
    targetName: item.nguoiNop,
    soTien: item.soTien,
    status: 'processing',
    paidBy: item.nguoiNop,
    paidMethod: 'Chuyển khoản',
    paidAt: item.raw?.updatedAt || item.raw?.transactionDate || item.ngayThu,
    raw: item.raw,
  };
}
