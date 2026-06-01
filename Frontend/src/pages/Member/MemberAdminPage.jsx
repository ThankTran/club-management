import { useEffect, useMemo, useState } from 'react';

import MemberTable from '../../components/sections/Member/MemberTable';
import MemberForm from '../../components/sections/Member/MemberForm';
import MemberFilter from '../../components/sections/Member/MemberFilter';
import MemberStatCard from '../../components/sections/Member/MemberStatCard';
import MemberDetailModal from '../../components/sections/Member/MemberDetailModal';
import MemberHistoryModal from '../../components/sections/Member/MemberHistoryModal';
import MemberReviewModal from '../../components/sections/Member/MemberReviewModal';
import MemberDeleteConfirmModal from '../../components/sections/Member/MemberDeleteConfirmModal';
import ActionToast from '../../components/common/ActionToast/ActionToast';
import useAuthStore from '../../store/auth-store';
import { getMeAPI } from '../../services/auth-services';
import { getRolesAPI } from '../../services/role-service';
import { getSystemSettingByKeyAPI } from '../../services/system-setting-service';
import {
  approveMemberAPI,
  deleteMemberAPI,
  getMemberDepartmentsAPI,
  getMembersAPI,
  normalizeMemberFromApi,
  registerMemberAPI,
  toApprovalPayload,
  toMemberPayload,
  updateMemberAPI,
} from '../../services/member-service';
import { matchesVietnameseSearch, normalizeVietnameseText } from '../../utils/vietnamese-search';
import useActionToast from '../../hooks/useActionToast';
import styles from './MemberAdminPage.module.css';

const PAGE_SIZE = 8;
const STUDENT_ID_KEY = 'id';
const STATUS = {
  pending: 'Đang xét duyệt',
  approved: 'Đã duyệt',
  rejected: 'Từ chối',
};

const matchesMemberSearch = (member, query) => {
  if (!query) return true;

  const normalizedQuery = normalizeVietnameseText(query);
  if (/^\d/.test(normalizedQuery)) {
    return matchesVietnameseSearch(member[STUDENT_ID_KEY], normalizedQuery);
  }

  return (
    matchesVietnameseSearch(member.name, normalizedQuery) ||
    matchesVietnameseSearch(member.email, normalizedQuery) ||
    matchesVietnameseSearch(String(member.phone || '').replace(/\s+/g, ''), normalizedQuery.replace(/\s+/g, ''))
  );
};

export default function MemberAdminPage() {
  const currentUser = useAuthStore((state) => state.user);
  const updateCurrentUser = useAuthStore((state) => state.updateUser);
  const currentMemberId = currentUser?.memberId;

  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [apiError, setApiError] = useState('');
  const [departmentOptions, setDepartmentOptions] = useState([]);
  const [roleOptions, setRoleOptions] = useState([]);
  const [ageBounds, setAgeBounds] = useState({ min: 18, max: 30 });
  const [activeTab, setActiveTab] = useState('review');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [formOpen, setFormOpen] = useState(false);
  const [editTarget, setEditTarget] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState(null);
  const [detailTarget, setDetailTarget] = useState(null);
  const [historyOpen, setHistoryOpen] = useState(false);
  const [reviewTarget, setReviewTarget] = useState(null);
  const [reviewType, setReviewType] = useState('approve');
  const [reviewLoading, setReviewLoading] = useState(false);
  const [reviewError, setReviewError] = useState('');
  const [filterOpen, setFilterOpen] = useState(false);
  const [departmentFilter, setDepartmentFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [deleteLoading, setDeleteLoading] = useState(false);
  const { toast, showPending, showSuccess, showError } = useActionToast();

  useEffect(() => {
    let ignore = false;

    Promise.allSettled([getMembersAPI(), getMemberDepartmentsAPI(), getRolesAPI()])
      .then(([membersResult, departmentsResult, rolesResult]) => {
        if (ignore) return;

        if (membersResult.status === 'fulfilled' && Array.isArray(membersResult.value)) {
          setMembers(membersResult.value.map(normalizeMemberFromApi));
          setApiError('');
        } else {
          setMembers([]);
          setApiError(membersResult.reason?.message || 'Không tải được danh sách thành viên từ API.');
        }

        if (departmentsResult.status === 'fulfilled' && Array.isArray(departmentsResult.value)) {
          setDepartmentOptions(
            departmentsResult.value.map((department) => ({
              id: department.departmentId,
              name: department.departmentName,
            })),
          );
        } else {
          setDepartmentOptions([]);
        }

        if (rolesResult.status === 'fulfilled' && Array.isArray(rolesResult.value)) {
          setRoleOptions(rolesResult.value.map((role) => role.roleName).filter(Boolean));
        } else {
          setRoleOptions([]);
        }
      })
      .catch((error) => {
        if (ignore) return;
        setMembers([]);
        setDepartmentOptions([]);
        setRoleOptions([]);
        setApiError(error?.message || 'Không tải được danh sách thành viên từ API.');
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {
    let ignore = false;

    Promise.allSettled([
      getSystemSettingByKeyAPI('member.age.min'),
      getSystemSettingByKeyAPI('member.age.max'),
    ]).then(([minResult, maxResult]) => {
      if (ignore) return;

      const minAge = Number(minResult.status === 'fulfilled' ? minResult.value?.settingValue : 18);
      const maxAge = Number(maxResult.status === 'fulfilled' ? maxResult.value?.settingValue : 30);
      setAgeBounds({
        min: Number.isFinite(minAge) && minAge > 0 ? minAge : 18,
        max: Number.isFinite(maxAge) && maxAge > 0 ? maxAge : 30,
      });
    });

    return () => {
      ignore = true;
    };
  }, []);

  const departmentNames = useMemo(
    () => departmentOptions.map((department) => department.name).filter(Boolean),
    [departmentOptions],
  );
  const roleNames = useMemo(
    () => (roleOptions.length ? roleOptions : [...new Set(members.map((member) => member.role).filter(Boolean))]),
    [members, roleOptions],
  );

  const getDepartmentId = (departmentName) => {
    const department = departmentOptions.find((item) => item.name === departmentName);
    return department?.id || null;
  };

  const statuses = useMemo(() => [...new Set(members.map((m) => m.requestStatus).filter(Boolean))], [members]);
  const roles = useMemo(() => [...new Set(members.map((m) => m.role).filter(Boolean))], [members]);

  const stats = useMemo(() => {
    const total = members.length;
    const pending = members.filter((m) => m.requestStatus === STATUS.pending).length;
    const approved = members.filter((m) => m.requestStatus === STATUS.approved).length;
    const rejected = members.filter((m) => m.requestStatus === STATUS.rejected).length;
    return { total, pending, approved, rejected };
  }, [members]);

  const filtered = useMemo(() => {
    const q = search.trim();
    return members
      .filter((m) => {
        const matchSearch = matchesMemberSearch(m, q);
        const matchDepartment = !departmentFilter || m.department === departmentFilter;
        const matchesTab =
          activeTab === 'review'
            ? m.requestStatus === STATUS.pending
            : m.requestStatus === STATUS.approved;
        const matchStatus = activeTab === 'review' || activeTab === 'lookup' || !statusFilter || m.requestStatus === statusFilter;
        const matchRole = !roleFilter || m.role === roleFilter;
        return matchesTab && matchSearch && matchDepartment && matchStatus && matchRole;
      })
      .sort((a, b) =>
        activeTab === 'review'
          ? new Date(a.registeredAt) - new Date(b.registeredAt)
          : a.name.localeCompare(b.name, 'vi')
      );
  }, [members, search, departmentFilter, statusFilter, roleFilter, activeTab]);

  const historyMembers = useMemo(
    () => members
      .filter((member) => member.requestStatus === STATUS.approved || member.requestStatus === STATUS.rejected)
      .sort((a, b) => new Date(b.reviewedAt || b.registeredAt) - new Date(a.reviewedAt || a.registeredAt)),
    [members],
  );

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const paginated = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  const openAdd = () => {
    setEditTarget(null);
    setFormOpen(true);
  };

  const openEdit = (member) => {
    setEditTarget(member);
    setFormOpen(true);
  };

  const handleSubmit = async (formData) => {
    setFormLoading(true);
    try {
      const payload = toMemberPayload({
        ...formData,
        departmentId: formData.departmentId || getDepartmentId(formData.department),
      });

      if (editTarget) {
        const updated = await updateMemberAPI(editTarget.memberId, payload);
        setMembers((prev) => prev.map((m) => (
          m.memberId === editTarget.memberId ? normalizeMemberFromApi(updated) : m
        )));
      } else {
        const created = await registerMemberAPI(payload);
        setMembers((prev) => [normalizeMemberFromApi(created), ...prev]);
      }
      setFormOpen(false);
      setPage(1);
      setApiError('');
    } catch (error) {
      setApiError(error?.message || 'Không lưu được hồ sơ thành viên.');
    } finally {
      setFormLoading(false);
    }
  };

  const confirmDelete = async () => {
    if (!deleteConfirm?.memberId) {
      setDeleteConfirm(null);
      return;
    }

    try {
      setDeleteLoading(true);
      showPending('Đang xóa thành viên...');
      await deleteMemberAPI(deleteConfirm.memberId);
      setMembers((prev) => prev.filter((member) => member.memberId !== deleteConfirm.memberId));
      setDeleteConfirm(null);
      setApiError('');
      setDeleteLoading(false);
      showSuccess('Xóa thành viên thành công.');
    } catch (error) {
      const message = error?.message || 'Không xóa được thành viên.';
      setDeleteLoading(false);
      showError(message);
    }
  };

  const openReview = (member, type) => {
    setReviewTarget(member);
    setReviewType(type);
    setReviewError('');
  };

  const closeReview = () => {
    if (reviewLoading) return;
    setReviewTarget(null);
    setReviewError('');
  };

  const resolveCurrentMemberId = async () => {
    if (currentMemberId) return currentMemberId;

    const me = await getMeAPI();
    if (me?.memberId) {
      updateCurrentUser(me);
      return me.memberId;
    }

    return null;
  };

  const confirmReview = async (member, reviewData) => {
    if (!member.memberId) {
      const message = 'Không xác định được memberId của hồ sơ cần xét duyệt.';
      setReviewError(message);
      setApiError(message);
      return;
    }

    setReviewLoading(true);
    setReviewError('');
    try {
      showPending(reviewData?.requestStatus === STATUS.rejected ? 'Đang từ chối hồ sơ...' : 'Đang duyệt hồ sơ...');
      const approverMemberId = await resolveCurrentMemberId();
      if (!approverMemberId) {
        throw new Error('Không xác định được memberId của tài khoản đang đăng nhập.');
      }

      const nextReviewData = {
        ...reviewData,
        approvedBy: approverMemberId,
      };
      const updated = await approveMemberAPI(toApprovalPayload(member, nextReviewData));
      const nextMember = normalizeMemberFromApi(updated);

      setMembers((prev) => prev.map((m) => (m.memberId === member.memberId ? nextMember : m)));
      setReviewTarget(null);
      setApiError('');
      showSuccess(reviewData?.requestStatus === STATUS.rejected ? 'Đã từ chối hồ sơ.' : 'Đã duyệt hồ sơ.');
    } catch (error) {
      const message = error?.message || 'Không cập nhật được trạng thái xét duyệt.';
      setReviewError(message);
      setApiError(message);
      showError(message);
    } finally {
      setReviewLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <ActionToast toast={toast} />
      {apiError && <div className={styles.apiError}>{apiError}</div>}

      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>Quản lý thành viên</h1>
          <p className={styles.pageSubtitle}>
            Tiếp nhận hồ sơ đăng ký, kiểm tra điều kiện và xét duyệt thành viên câu lạc bộ.
          </p>
        </div>

        <div className={styles.headerActions}>
          <button
            type="button"
            className={`${styles.historyBtn} ${historyOpen ? styles.historyBtnActive : ''}`}
            onClick={() => setHistoryOpen(true)}
          >
            <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" aria-hidden="true">
              <path d="M3 12a9 9 0 1 0 3-6.7" />
              <path d="M3 4v5h5" />
              <path d="M12 7v5l3 2" />
            </svg>
            Lịch sử
          </button>

          <button className={styles.addBtn} onClick={openAdd}>
            <svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
              <path d="M12 5v14M5 12h14" />
            </svg>
            Thêm hồ sơ
          </button>
        </div>
      </div>

      <div className={styles.statsRow}>
        <MemberStatCard label="Tổng hồ sơ" value={stats.total} sub="Tất cả phiếu đăng ký" />
        <MemberStatCard label="Chờ xét duyệt" value={stats.pending} sub="Cần xử lý" variant="warning" />
        <MemberStatCard label="Đã duyệt" value={stats.approved} sub="Thành viên hợp lệ" variant="success" />
        <MemberStatCard label="Từ chối" value={stats.rejected} sub="Hồ sơ không đạt" variant="danger" />
      </div>

      <div className={styles.workflowPanel}>
        <div className={styles.tabs}>
          <button
            type="button"
            className={`${styles.tabBtn} ${activeTab === 'review' ? styles.tabActive : ''}`}
            onClick={() => { setActiveTab('review'); setPage(1); }}
          >
            Chờ xét duyệt thành viên
          </button>
          <button
            type="button"
            className={`${styles.tabBtn} ${activeTab === 'lookup' ? styles.tabActive : ''}`}
            onClick={() => { setActiveTab('lookup'); setPage(1); }}
          >
            Tra cứu thành viên
            <span>{stats.approved}</span>
          </button>
        </div>

        <div className={styles.tabControls}>
          <div className={styles.searchWrap}>
            <svg className={styles.searchIcon} width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8" /><path d="M21 21l-4.35-4.35" />
            </svg>
            <input
              className={styles.searchInput}
              placeholder="Tìm theo tên, MSSV, email..."
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(1); }}
            />
          </div>

          <MemberFilter
            open={filterOpen}
            setOpen={setFilterOpen}
            departmentFilter={departmentFilter}
            setDepartmentFilter={setDepartmentFilter}
            statusFilter={statusFilter}
            setStatusFilter={setStatusFilter}
            roleFilter={roleFilter}
            setRoleFilter={setRoleFilter}
            departments={departmentNames}
            statuses={statuses}
            roles={roles}
            showStatus={false}
          />
        </div>
      </div>

      <div className={styles.tableSection}>
        <div className={styles.tableHeader}>
          <div>
            <h2 className={styles.tableTitle}>
              {activeTab === 'review' ? 'Phiếu thành viên chờ xét duyệt' : 'Danh sách thành viên'}
            </h2>
            <p className={styles.tableSubtitle}>
              {activeTab === 'review'
                ? 'Hồ sơ được xếp theo ngày đăng ký sớm nhất để xử lý trước.'
                : 'Tra cứu các thành viên hợp lệ trong câu lạc bộ.'}
            </p>
          </div>
          <span className={styles.tableCount}>{filtered.length} hồ sơ</span>
        </div>

        <MemberTable
          members={paginated}
          total={filtered.length}
          page={page}
          totalPages={totalPages}
          pageSize={PAGE_SIZE}
          onPageChange={(p) => { if (p >= 1 && p <= totalPages) setPage(p); }}
          onEdit={openEdit}
          onDelete={setDeleteConfirm}
          onView={setDetailTarget}
          onApprove={(member) => openReview(member, 'approve')}
          onReject={(member) => openReview(member, 'reject')}
          isAdmin
          showActions
          showViewAction={false}
          showReviewActions={activeTab === 'review'}
          showContact={activeTab !== 'review'}
          showRegisteredAt={activeTab === 'review'}
          loading={loading}
        />
      </div>

      <button className={styles.fab} onClick={openAdd} title="Thêm hồ sơ">
        <svg width="22" height="22" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
          <path d="M12 5v14M5 12h14" />
        </svg>
      </button>

      <MemberForm
        open={formOpen}
        onClose={() => setFormOpen(false)}
        onSubmit={handleSubmit}
        initial={editTarget}
        loading={formLoading}
        existingMembers={members}
        departments={departmentNames}
        roles={roleNames}
        ageBounds={ageBounds}
      />

      <MemberDetailModal member={detailTarget} onClose={() => setDetailTarget(null)} />
      <MemberHistoryModal
        open={historyOpen}
        members={historyMembers}
        onClose={() => setHistoryOpen(false)}
        onView={setDetailTarget}
      />
      <MemberReviewModal
        member={reviewTarget}
        type={reviewType}
        onClose={closeReview}
        onConfirm={confirmReview}
        loading={reviewLoading}
        serverError={reviewError}
      />
      <MemberDeleteConfirmModal
        member={deleteConfirm}
        onClose={() => { if (!deleteLoading) setDeleteConfirm(null); }}
        onConfirm={confirmDelete}
        loading={deleteLoading}
      />
    </div>
  );
}
