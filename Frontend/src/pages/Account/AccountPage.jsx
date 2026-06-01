import { useEffect, useMemo, useState } from "react";
import { UserCog } from "lucide-react";
import AccountCreateModal from "../../components/sections/Account/AccountCreateModal";
import AccountDeleteConfirmModal from "../../components/sections/Account/AccountDeleteConfirmModal";
import AccountDetailPanel from "../../components/sections/Account/AccountDetailPanel";
import AccountTable from "../../components/sections/Account/AccountTable";
import AccountToolbar from "../../components/sections/Account/AccountToolbar";
import ActionToast from "../../components/common/ActionToast/ActionToast";
import {
  createUserAPI,
  deleteUserAPI,
  getUserSessionsAPI,
  loadAccountUsersAPI,
  normalizeAccountFromApi,
  normalizeLoginSessionFromApi,
  updateUserForAdminAPI,
} from "../../services/account-service";
import { getMembersAPI, normalizeMemberFromApi } from "../../services/member-service";
import useAuthStore from "../../store/auth-store";
import useActionToast from "../../hooks/useActionToast";
import styles from "./AccountPage.module.css";

const PAGE_SIZE = 10;
const ADMIN_VISIBLE_PASSWORDS_KEY = "account-visible-passwords";

export default function AccountPage() {
  const currentUser = useAuthStore((state) => state.user);
  const [accounts, setAccounts] = useState([]);
  const [members, setMembers] = useState([]);
  const [selectedId, setSelectedId] = useState("");
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);
  const [createOpen, setCreateOpen] = useState(false);
  const [createdAtSort, setCreatedAtSort] = useState("desc");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState(null);
  const [apiError, setApiError] = useState("");
  const { toast, showPending, showSuccess, showError } = useActionToast();

  const reloadAccounts = async () => {
    const nextAccounts = applyVisiblePasswords(await loadAccountUsersAPI());
    setAccounts(nextAccounts);
    setSelectedId((current) => {
      if (current && nextAccounts.some((account) => account.id === current)) return current;

      const currentUserId = currentUser?.userId ? String(currentUser.userId) : "";
      if (currentUserId && nextAccounts.some((account) => account.id === currentUserId)) {
        return currentUserId;
      }

      return nextAccounts[0]?.id || "";
    });
    return nextAccounts;
  };

  useEffect(() => {
    let ignore = false;

    Promise.allSettled([loadAccountUsersAPI(), getMembersAPI()])
      .then(([accountsResult, membersResult]) => {
        if (ignore) return;

        if (accountsResult.status === "fulfilled") {
          const nextAccounts = applyVisiblePasswords(accountsResult.value);
          setAccounts(nextAccounts);

          const currentUserId = currentUser?.userId ? String(currentUser.userId) : "";
          const initialSelectedId =
            nextAccounts.find((account) => account.id === currentUserId)?.id ||
            nextAccounts[0]?.id ||
            "";
          setSelectedId(initialSelectedId);
          setApiError("");
        } else {
          setAccounts([]);
          setSelectedId("");
          setApiError(accountsResult.reason?.message || "Không tải được danh sách tài khoản từ API.");
        }

        if (membersResult.status === "fulfilled" && Array.isArray(membersResult.value)) {
          setMembers(membersResult.value.map(normalizeMemberFromApi));
        } else {
          setMembers([]);
        }
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [currentUser?.userId]);

  const filteredAccounts = useMemo(() => {
    const keyword = search.trim().toLowerCase();
    return accounts
      .filter((account) => {
        const matchesSearch =
          !keyword ||
          account.name.toLowerCase().includes(keyword) ||
          account.username.toLowerCase().includes(keyword) ||
          account.email.toLowerCase().includes(keyword) ||
          account.memberId.toLowerCase().includes(keyword) ||
          account.department.toLowerCase().includes(keyword);
        return matchesSearch;
      })
      .sort((a, b) => {
        const aTime = parseDate(a.createdAt);
        const bTime = parseDate(b.createdAt);
        return createdAtSort === "desc" ? bTime - aTime : aTime - bTime;
      });
  }, [accounts, createdAtSort, search]);

  useEffect(() => {
    setPage(1);
  }, [search]);

  useEffect(() => {
    if (!selectedId) return;
    let ignore = false;

    getUserSessionsAPI(selectedId)
      .then((sessions) => {
        if (ignore) return;
        const normalizedSessions = Array.isArray(sessions)
          ? sessions.map(normalizeLoginSessionFromApi)
          : [];
        setAccounts((current) => current.map((account) => (
          account.id === selectedId
            ? {
              ...account,
              sessions: normalizedSessions,
              lastLogin: normalizedSessions[0]?.time || account.lastLogin,
            }
            : account
        )));
      })
      .catch(() => {
        if (ignore) return;
        setAccounts((current) => current.map((account) => (
          account.id === selectedId ? { ...account, sessions: [] } : account
        )));
      });

    return () => {
      ignore = true;
    };
  }, [selectedId]);

  const totalPages = Math.max(1, Math.ceil(filteredAccounts.length / PAGE_SIZE));
  const currentPage = Math.min(page, totalPages);
  const paginatedAccounts = filteredAccounts.slice(
    (currentPage - 1) * PAGE_SIZE,
    currentPage * PAGE_SIZE,
  );

  const selectedAccount =
    accounts.find((account) => account.id === selectedId) || filteredAccounts[0] || accounts[0];

  const updateAccount = async (accountId, fields) => {
    if (!fields.password?.trim()) {
      setApiError("Vui lòng nhập mật khẩu mới.");
      return false;
    }

    setSaving(true);
    try {
      await updateUserForAdminAPI(accountId, { newPassword: fields.password.trim() });
      saveVisiblePassword(accountId, fields.password.trim());
      await reloadAccounts();
      setApiError("");
      return true;
    } catch (error) {
      setApiError(error?.message || "Không cập nhật được mật khẩu tài khoản.");
      return false;
    } finally {
      setSaving(false);
    }
  };

  const requestDelete = (account) => {
    if (!account) return;
    setDeleteConfirm(account);
  };

  const confirmDelete = async () => {
    if (!deleteConfirm?.id) {
      setDeleteConfirm(null);
      return;
    }

    const currentUserId = currentUser?.userId ? String(currentUser.userId) : "";
    if (currentUserId && String(deleteConfirm.id) === currentUserId) {
      showError("Không thể xóa tài khoản đang đăng nhập.");
      return;
    }

    setDeleting(true);
    try {
      showPending("Đang xóa tài khoản...");
      await deleteUserAPI(deleteConfirm.id);
      setDeleteConfirm(null);
      await reloadAccounts();
      setApiError("");
      showSuccess("Xóa tài khoản thành công.");
    } catch (error) {
      const message = error?.message || "Không xóa được tài khoản.";
      setApiError(message);
      showError(message);
    } finally {
      setDeleting(false);
    }
  };

  const createAccount = async (formData) => {
    const memberId = Number(formData.memberId);

    if (!Number.isFinite(memberId)) {
      setApiError("MemberId không hợp lệ.");
      return false;
    }

    setSaving(true);
    try {
      const created = await createUserAPI({
        memberId,
        password: formData.password.trim(),
      });
      const nextAccount = normalizeAccountFromApi(created, "");
      saveVisiblePassword(nextAccount.id, formData.password.trim());
      await reloadAccounts();
      setSelectedId(String(nextAccount.userId || ""));
      setSearch("");
      setPage(1);
      setApiError("");
      return true;
    } catch (error) {
      setApiError(error?.message || "Không tạo được tài khoản.");
      return false;
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className={styles.page}>
      <ActionToast toast={toast} />
      {apiError && <div className={styles.apiError}>{apiError}</div>}

      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>Quản lý tài khoản đăng nhập</h1>
          <p className={styles.pageSubtitle}>
            Theo dõi thông tin người dùng hiện tại, mật khẩu đã mã hóa và quyền truy cập của thành viên câu lạc bộ.
          </p>
        </div>
        <button className={styles.primaryBtn} type="button" onClick={() => setCreateOpen(true)}>
          <UserCog size={16} />
          Tạo tài khoản
        </button>
      </div>

      <AccountToolbar
        search={search}
        onSearchChange={setSearch}
      />

      <div className={styles.contentGrid}>
        <div className={styles.tableSection}>
          <div className={styles.tableHeader}>
            <div>
              <div className={styles.tableTitleRow}>
                <h2 className={styles.tableTitle}>Danh sách tài khoản</h2>
                <span className={styles.tableCount}>{filteredAccounts.length}</span>
              </div>
            </div>
          </div>

          {loading ? (
            <div className={styles.loadingText}>Đang tải danh sách tài khoản...</div>
          ) : (
            <AccountTable
              accounts={paginatedAccounts}
              selectedId={selectedAccount?.id}
              onSelect={setSelectedId}
              sortDirection={createdAtSort}
              onSortCreatedAt={() => setCreatedAtSort((current) => (current === "desc" ? "asc" : "desc"))}
            />
          )}

          {!loading && filteredAccounts.length > 0 && (
            <div className={styles.pagination}>
              <span className={styles.paginationInfo}>
                Hiển thị {(currentPage - 1) * PAGE_SIZE + 1}-
                {Math.min(currentPage * PAGE_SIZE, filteredAccounts.length)} trong tổng số{" "}
                {filteredAccounts.length} tài khoản
              </span>
              <div className={styles.paginationControls}>
                <button
                  className={styles.pageBtn}
                  type="button"
                  disabled={currentPage === 1}
                  onClick={() => setPage((value) => Math.max(1, value - 1))}
                >
                  ‹
                </button>
                {Array.from({ length: totalPages }, (_, index) => index + 1).map((pageNumber) => (
                  <button
                    key={pageNumber}
                    className={`${styles.pageBtn} ${currentPage === pageNumber ? styles.pageBtnActive : ""}`}
                    type="button"
                    onClick={() => setPage(pageNumber)}
                  >
                    {pageNumber}
                  </button>
                ))}
                <button
                  className={styles.pageBtn}
                  type="button"
                  disabled={currentPage === totalPages}
                  onClick={() => setPage((value) => Math.min(totalPages, value + 1))}
                >
                  ›
                </button>
              </div>
            </div>
          )}
        </div>

        <AccountDetailPanel
          account={selectedAccount}
          onAccountUpdate={(fields) => updateAccount(selectedAccount.id, fields)}
          onDelete={requestDelete}
          saving={saving}
          deleting={deleting}
        />
      </div>

      <AccountCreateModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onSubmit={createAccount}
        existingAccounts={accounts}
        members={members}
      />

      <AccountDeleteConfirmModal
        account={deleteConfirm}
        onCancel={() => { if (!deleting) setDeleteConfirm(null); }}
        onConfirm={confirmDelete}
        loading={deleting}
      />
    </div>
  );
}

function parseDate(value) {
  if (!value || value === "Chua cap nhat") return 0;

  const [day, month, year] = value.split("/");
  if (!day || !month || !year) return 0;

  return new Date(Number(year), Number(month) - 1, Number(day)).getTime();
}

function applyVisiblePasswords(accounts) {
  const visiblePasswords = getVisiblePasswords();
  return accounts.map((account) => ({
    ...account,
    password: visiblePasswords[account.id] || "",
  }));
}

function getVisiblePasswords() {
  try {
    return JSON.parse(sessionStorage.getItem(ADMIN_VISIBLE_PASSWORDS_KEY) || "{}");
  } catch {
    return {};
  }
}

function saveVisiblePassword(accountId, password) {
  if (!accountId) return;
  const visiblePasswords = getVisiblePasswords();
  visiblePasswords[String(accountId)] = password;
  sessionStorage.setItem(ADMIN_VISIBLE_PASSWORDS_KEY, JSON.stringify(visiblePasswords));
}
