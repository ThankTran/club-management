import { useCallback, useEffect, useState, useMemo } from 'react';
import styles from './FinancePage.module.css';

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
import ActionToast from '../../components/common/ActionToast/ActionToast';
import {
  completeTransactionAPI,
  createTransactionAPI,
  deleteTransactionAPI,
  getPendingMonthlyDuesAPI,
  getTotalExpenseAPI,
  getTotalIncomeAPI,
  getTransactionsPageAPI,
  normalizeMemberDueFromApi,
  normalizeTransactionFromApi,
  rejectTransactionPaymentAPI,
  toExpensePayload,
  toIncomePayload,
  updateTransactionAPI,
} from '../../services/finance-service';
import { getEventsAPI, normalizeEventFromApi } from '../../services/event-service';
import { getMembersAPI, normalizeMemberFromApi } from '../../services/member-service';
import useAuthStore from '../../store/auth-store';
import { isManager } from '../../utils/access-control';
import useActionToast from '../../hooks/useActionToast';

export default function FinancePage() {
  const TABLE_PAGE_SIZE = 10;
  const ANALYTICS_PAGE_SIZE = 100;
  const currentUser = useAuthStore((state) => state.user);
  const [thuList, setThuList] = useState([]);
  const [chiList, setChiList] = useState([]);
  const [analyticsThuList, setAnalyticsThuList] = useState([]);
  const [analyticsChiList, setAnalyticsChiList] = useState([]);
  const [transferDues, setTransferDues] = useState([]);
  const [memberOptions, setMemberOptions] = useState([]);
  const [eventOptions, setEventOptions] = useState([]);
  const [pendingDues, setPendingDues] = useState([]);
  const [pendingDuesLoading, setPendingDuesLoading] = useState(false);
  const [apiError, setApiError] = useState('');
  const [optionsLoading, setOptionsLoading] = useState(false);
  const [tableLoading, setTableLoading] = useState(false);
  const [thuPage, setThuPage] = useState(1);
  const [chiPage, setChiPage] = useState(1);
  const [thuTotal, setThuTotal] = useState(0);
  const [chiTotal, setChiTotal] = useState(0);
  const [thuTotalPages, setThuTotalPages] = useState(1);
  const [chiTotalPages, setChiTotalPages] = useState(1);
  const [tongThu, setTongThu] = useState(0);
  const [tongChi, setTongChi] = useState(0);
  const [bcThu, setBcThu] = useState([]);
  const [bcChi, setBcChi] = useState([]);

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
  const [deleteLoading, setDeleteLoading] = useState(false);
  const { toast, showPending, showSuccess, showError } = useActionToast();

  const [baocaoThang, setBaocaoThang] = useState(new Date().getMonth() + 1);

  const [searchThu, setSearchThu] = useState('');
  const [searchChi, setSearchChi] = useState('');
  const [sortThu, setSortThu] = useState('desc');
  const [sortChi, setSortChi] = useState('desc');

  const setBaocaoData = useCallback((income = [], expense = []) => {
    setBcThu(income);
    setBcChi(expense);
  }, []);

  const loadOverviewData = useCallback((ignoreRef = { current: false }) => {
    setPendingDuesLoading(true);
    const currentYear = new Date().getFullYear();
    const selectedMonthRange = buildMonthRange(currentYear, baocaoThang);

    return Promise.allSettled([
      getPendingMonthlyDuesAPI(),
      getTotalIncomeAPI('2000-01-01', '2100-12-31'),
      getTotalExpenseAPI('2000-01-01', '2100-12-31'),
      getTransactionsPageAPI({ type: 'INCOME', page: 0, size: ANALYTICS_PAGE_SIZE }),
      getTransactionsPageAPI({ type: 'EXPENSE', page: 0, size: ANALYTICS_PAGE_SIZE }),
      getTransactionsPageAPI({ type: 'INCOME', page: 0, size: ANALYTICS_PAGE_SIZE, ...selectedMonthRange }),
      getTransactionsPageAPI({ type: 'EXPENSE', page: 0, size: ANALYTICS_PAGE_SIZE, ...selectedMonthRange }),
    ])
      .then(([
        dueResult,
        totalIncomeResult,
        totalExpenseResult,
        analyticsIncomeResult,
        analyticsExpenseResult,
        monthIncomeResult,
        monthExpenseResult,
      ]) => {
        if (ignoreRef.current) return;

        const dueData = dueResult.status === 'fulfilled' ? dueResult.value : [];
        const analyticsIncome = analyticsIncomeResult.status === 'fulfilled'
          ? normalizePageContent(analyticsIncomeResult.value)
          : [];
        const analyticsExpense = analyticsExpenseResult.status === 'fulfilled'
          ? normalizePageContent(analyticsExpenseResult.value)
          : [];
        const monthIncome = monthIncomeResult.status === 'fulfilled'
          ? normalizePageContent(monthIncomeResult.value)
          : [];
        const monthExpense = monthExpenseResult.status === 'fulfilled'
          ? normalizePageContent(monthExpenseResult.value)
          : [];

        setPendingDues(Array.isArray(dueData) ? dueData.map(normalizeMemberDueFromApi) : []);
        setAnalyticsThuList(analyticsIncome);
        setAnalyticsChiList(analyticsExpense);
        setThuTotal(Number(analyticsIncomeResult.status === 'fulfilled' ? analyticsIncomeResult.value?.totalElements || 0 : 0));
        setChiTotal(Number(analyticsExpenseResult.status === 'fulfilled' ? analyticsExpenseResult.value?.totalElements || 0 : 0));
        setTransferDues(analyticsIncome.filter(isAwaitingConfirmationTransaction).map(toTransferDueRow));
        setTongThu(totalIncomeResult.status === 'fulfilled' ? Number(totalIncomeResult.value || 0) : 0);
        setTongChi(totalExpenseResult.status === 'fulfilled' ? Number(totalExpenseResult.value || 0) : 0);
        setBaocaoData(monthIncome, monthExpense);
        setApiError('');
      })
      .catch((error) => {
        if (ignoreRef.current) return;
        setPendingDues([]);
        setAnalyticsThuList([]);
        setAnalyticsChiList([]);
        setTransferDues([]);
        setTongThu(0);
        setTongChi(0);
        setBaocaoData([], []);
        setApiError(error?.message || 'Không tải được dữ liệu thu chi từ API.');
      })
      .finally(() => {
        if (!ignoreRef.current) {
          setPendingDuesLoading(false);
        }
      });
  }, [ANALYTICS_PAGE_SIZE, baocaoThang, setBaocaoData]);

  const loadTablePage = useCallback(async (type, page, ignoreRef = { current: false }) => {
    setTableLoading(true);
    try {
      const data = await getTransactionsPageAPI({ type, page: page - 1, size: TABLE_PAGE_SIZE });
      if (ignoreRef.current) return;

      const normalized = normalizePageContent(data);
      if (type === 'INCOME') {
        setThuList(normalized);
        setThuTotal(Number(data?.totalElements || 0));
        setThuTotalPages(Math.max(Number(data?.totalPages || 1), 1));
      } else {
        setChiList(normalized);
        setChiTotal(Number(data?.totalElements || 0));
        setChiTotalPages(Math.max(Number(data?.totalPages || 1), 1));
      }
      setApiError('');
    } catch (error) {
      if (ignoreRef.current) return;
      if (type === 'INCOME') {
        setThuList([]);
        setThuTotal(0);
        setThuTotalPages(1);
      } else {
        setChiList([]);
        setChiTotal(0);
        setChiTotalPages(1);
      }
      setApiError(error?.message || 'Không tải được danh sách giao dịch.');
    } finally {
      if (!ignoreRef.current) setTableLoading(false);
    }
  }, [TABLE_PAGE_SIZE]);

  const loadModalOptions = useCallback(async (ignoreRef = { current: false }) => {
    if (memberOptions.length > 0 && eventOptions.length > 0) return;
    setOptionsLoading(true);
    try {
      const [membersResult, eventsResult] = await Promise.allSettled([getMembersAPI(), getEventsAPI()]);
      if (ignoreRef.current) return;

      const memberData = membersResult.status === 'fulfilled' ? membersResult.value : [];
      const eventData = eventsResult.status === 'fulfilled' ? eventsResult.value : [];
      setMemberOptions(Array.isArray(memberData) ? memberData.map(normalizeMemberFromApi) : []);
      setEventOptions(Array.isArray(eventData) ? eventData.map(normalizeEventFromApi) : []);
    } finally {
      if (!ignoreRef.current) setOptionsLoading(false);
    }
  }, [eventOptions.length, memberOptions.length]);

  useEffect(() => {
    const ignoreRef = { current: false };
    loadOverviewData(ignoreRef);

    return () => {
      ignoreRef.current = true;
    };
  }, [loadOverviewData]);

  useEffect(() => {
    const refreshFinanceData = () => {
      loadOverviewData();
      if (tab === 'thu') loadTablePage('INCOME', thuPage);
      if (tab === 'chi') loadTablePage('EXPENSE', chiPage);
    };

    window.addEventListener('finance:transactions-updated', refreshFinanceData);

    return () => {
      window.removeEventListener('finance:transactions-updated', refreshFinanceData);
    };
  }, [chiPage, loadOverviewData, loadTablePage, tab, thuPage]);

  useEffect(() => {
    const ignoreRef = { current: false };
    if (tab === 'thu') {
      loadTablePage('INCOME', thuPage, ignoreRef);
    }
    if (tab === 'chi') {
      loadTablePage('EXPENSE', chiPage, ignoreRef);
    }
    return () => {
      ignoreRef.current = true;
    };
  }, [chiPage, loadTablePage, tab, thuPage]);

  useEffect(() => {
    if (!thuOpen && !chiOpen) return;

    const ignoreRef = { current: false };
    loadModalOptions(ignoreRef);

    return () => {
      ignoreRef.current = true;
    };
  }, [chiOpen, loadModalOptions, thuOpen]);

  const canApproveExpense = isManager(currentUser);

  const soDu    = tongThu - tongChi;
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
        (thuFilters.hinhThuc === 'PROCESSING'
          ? isAwaitingConfirmationTransaction(r)
          : String(r.status || '').toUpperCase() === thuFilters.hinhThuc);

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

  const refreshFinanceView = useCallback(async () => {
    await loadOverviewData();
    if (tab === 'thu') await loadTablePage('INCOME', thuPage);
    if (tab === 'chi') await loadTablePage('EXPENSE', chiPage);
  }, [chiPage, loadOverviewData, loadTablePage, tab, thuPage]);

  const refreshTransferDuesAndReceipts = () => {
    refreshFinanceView();
  };

  const handleConfirmTransferPaid = async (id) => {
    try {
      showPending('Đang xác nhận chuyển khoản...');
      await completeTransactionAPI(id);
      await refreshFinanceView();
      setApiError('');
      showSuccess('Đã xác nhận chuyển khoản.');
    } catch (error) {
      setApiError(error?.message || 'Không xác nhận được khoản thu.');
      showError(error?.message || 'Không xác nhận được khoản thu.');
    }
  };

  const handleRejectTransferPaid = async (id) => {
    try {
      showPending('Đang từ chối chuyển khoản...');
      await rejectTransactionPaymentAPI(id);
      await refreshFinanceView();
      setApiError('');
      showSuccess('Đã từ chối chuyển khoản.');
    } catch (error) {
      setApiError(error?.message || 'Không từ chối được khoản thu.');
      showError(error?.message || 'Không từ chối được khoản thu.');
    }
  };

  const handleThuSubmit = async (data) => {
    setFormLoading(true);
    try {
      if (editThu) {
        await updateTransactionAPI(editThu.id, toIncomePayload({ ...editThu, ...data }));
      } else {
        const records = Array.isArray(data) ? data : [data];
        await Promise.all(records.map((record, index) => {
          const localRecord = {
            ...record,
            id: record.id || `THU${String(thuList.length + index + 1).padStart(3, '0')}`,
          };
          return createTransactionAPI(toIncomePayload(localRecord));
        }));
      }
      await refreshFinanceView();
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
        await updateTransactionAPI(editChi.id, toExpensePayload({ ...editChi, ...data }));
      } else {
        const localRecord = { ...data, id: data.id || `CHI${String(chiList.length + 1).padStart(3, '0')}` };
        await createTransactionAPI(toExpensePayload(localRecord));
      }
      await refreshFinanceView();
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
      showPending('Đang duyệt phiếu chi...');
      await completeTransactionAPI(item.id);
      await refreshFinanceView();
      setApiError('');
      showSuccess('Đã duyệt phiếu chi.');
    } catch (error) {
      const message = error?.message || 'Không duyệt được phiếu chi.';
      setApiError(message);
      showError(message);
    }
  };

  const handleRejectExpense = async (item) => {
    try {
      showPending('Đang từ chối phiếu chi...');
      await updateTransactionAPI(
        item.id,
        toExpensePayload({ ...item, status: 'REJECTED' }),
      );
      await refreshFinanceView();
      setApiError('');
      showSuccess('Đã từ chối phiếu chi.');
    } catch (error) {
      const message = error?.message || 'Không từ chối được phiếu chi.';
      setApiError(message);
      showError(message);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      setDeleteLoading(true);
      showPending('Đang xóa phiếu...');
      await deleteTransactionAPI(deleteTarget.id);
      await refreshFinanceView();
      setDeleteTarget(null);
      setApiError('');
      setDeleteLoading(false);
      showSuccess('Đã xóa phiếu.');
    } catch (error) {
      const message = error?.message || 'Không xoá được giao dịch.';
      setApiError(message);
      setDeleteLoading(false);
      showError(message);
    }
  };

  return (
    <div className={styles.page}>
      <ActionToast toast={toast} />
      {apiError && <div className={styles.apiError}>{apiError}</div>}

      <FinanceHeader
        onOpenThu={openThuModal}
        onOpenChi={openChiModal}
        thuList={analyticsThuList}
        chiList={analyticsChiList}
        bcThu={bcThu}
        bcChi={bcChi}
      />
      <FinanceStats
        tongThu={tongThu}
        tongChi={tongChi}
        soDu={soDu}
        thuCount={thuTotal}
        chiCount={chiTotal}
        bcThu={bcThu}
        baocaoThang={baocaoThang}
      />

      <FinanceTabs tab={tab} setTab={setTab} />

      {tab === 'overview' && (
        <FinanceOverview
          thuList={analyticsThuList}
          chiList={analyticsChiList}
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
          page={thuPage}
          totalPages={thuTotalPages}
          total={thuTotal}
          pageSize={TABLE_PAGE_SIZE}
          onPageChange={setThuPage}
          loading={tableLoading}
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
          page={chiPage}
          totalPages={chiTotalPages}
          total={chiTotal}
          pageSize={TABLE_PAGE_SIZE}
          onPageChange={setChiPage}
          loading={tableLoading}
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
        loading={formLoading || optionsLoading}
        memberOptions={memberOptions}
      />

      <ExpenseFormModal
        open={chiOpen}
        onClose={() => { setChiOpen(false); setEditChi(null); }}
        onSubmit={handleChiSubmit}
        initial={editChi}
        loading={formLoading || optionsLoading}
        eventOptions={eventOptions}
      />
      <ConfirmModal
        item={deleteTarget}
        onConfirm={handleDelete}
        onCancel={() => { if (!deleteLoading) setDeleteTarget(null); }}
        loading={deleteLoading}
      />
    </div>

    
  );
}

function isAwaitingConfirmationTransaction(item) {
  const status = String(item?.status || item?.raw?.status || '').toUpperCase();
  const approvedAt = item?.raw?.approvedAt || '';
  const approvedById = item?.raw?.approvedById;
  return status === 'PROCESSING'
    || (status === 'PENDING' && Boolean(approvedAt) && !approvedById);
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
    paidAt: item.raw?.approvedAt || item.raw?.updatedAt || item.raw?.transactionDate || item.ngayThu,
    raw: item.raw,
  };
}

function normalizePageContent(pageData) {
  const content = Array.isArray(pageData?.content) ? pageData.content : [];
  return content.map(normalizeTransactionFromApi);
}

function buildMonthRange(year, month) {
  const start = `${year}-${String(month).padStart(2, '0')}-01`;
  const endDate = new Date(year, month, 0);
  const end = `${year}-${String(month).padStart(2, '0')}-${String(endDate.getDate()).padStart(2, '0')}`;
  return { from: start, to: end };
}
