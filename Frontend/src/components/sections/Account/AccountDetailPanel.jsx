import { useEffect, useState } from "react";
import { KeyRound, Monitor, Save, Trash2, X } from "lucide-react";
import styles from "./AccountDetailPanel.module.css";

export default function AccountDetailPanel({ account, onAccountUpdate, onDelete, saving = false, deleting = false }) {
  const [editingAccount, setEditingAccount] = useState(false);
  const [passwordDraft, setPasswordDraft] = useState("");
  const [accountSaved, setAccountSaved] = useState(false);

  useEffect(() => {
    setPasswordDraft("");
    setEditingAccount(false);
    setAccountSaved(false);
  }, [account?.id]);

  if (!account) {
    return null;
  }

  const handleAccountAction = async () => {
    if (!editingAccount) {
      setEditingAccount(true);
      setAccountSaved(false);
      return;
    }

    const saved = await onAccountUpdate({ password: passwordDraft });
    if (!saved) return;

    setEditingAccount(false);
    setPasswordDraft("");
    setAccountSaved(true);
  };

  const cancelEdit = () => {
    setEditingAccount(false);
    setPasswordDraft("");
  };

  return (
    <aside className={styles.panel}>
      <div className={styles.profile}>
        <div className={styles.avatar}>{getInitials(account.name)}</div>
        <div>
          <h2 className={styles.name}>{account.name}</h2>
          <p className={styles.meta}>{account.memberId} · {account.role}</p>
        </div>
      </div>

      <div className={styles.infoGrid}>
        <div>
          <span>Email</span>
          <strong>{account.email || "Chưa cập nhật"}</strong>
        </div>
        <div>
          <span>Khoa</span>
          <strong>{formatDepartment(account.department) || "Chưa cập nhật"}</strong>
        </div>
        <div>
          <span>Tên đăng nhập</span>
          <strong className={styles.monoValue}>{account.username || "Chưa cập nhật"}</strong>
        </div>
        <div>
          <span>Mật khẩu</span>
          <strong className={styles.monoValue}>
            {account.password || "Đã mã hóa trong hệ thống"}
          </strong>
        </div>
        {editingAccount && (
          <div>
            <span>Mật khẩu mới</span>
            <input
              className={styles.accountInput}
              value={passwordDraft}
              type="password"
              onChange={(event) => setPasswordDraft(event.target.value)}
              autoFocus
            />
          </div>
        )}
        <div>
          <span>Ngày tạo</span>
          <strong>{account.createdAt}</strong>
        </div>
      </div>

      <div className={styles.actions}>
        <button className={styles.actionBtn} type="button" onClick={handleAccountAction} disabled={saving}>
          {editingAccount ? <Save size={15} /> : <KeyRound size={15} />}
          {editingAccount ? "Cập nhật mật khẩu" : accountSaved ? "Sửa tiếp mật khẩu" : "Sửa mật khẩu"}
        </button>
        {editingAccount && (
          <button className={styles.actionBtn} type="button" onClick={cancelEdit} disabled={saving}>
            <X size={15} />
            Hủy
          </button>
        )}
        <button
          className={`${styles.actionBtn} ${styles.deleteBtn}`}
          type="button"
          onClick={() => onDelete?.(account)}
          disabled={saving || deleting}
        >
          <Trash2 size={15} />
          Xóa tài khoản
        </button>
      </div>

      <div className={styles.section}>
        <div className={styles.sectionHeader}>
          <h3>Phiên đăng nhập</h3>
          <span>{account.sessions.length}</span>
        </div>
        <div className={styles.sessionList}>
          {account.sessions.length ? (
            account.sessions.map((session) => (
              <div key={session.id} className={styles.sessionItem}>
                <div className={styles.sessionIcon}>
                  <Monitor size={16} />
                </div>
                <div>
                  <p>{session.device}</p>
                  <span>{session.location} · IP: {session.ip}</span>
                  <small>{session.time}</small>
                </div>
              </div>
            ))
          ) : (
            <p className={styles.emptyText}>Chưa có dữ liệu phiên đăng nhập từ hệ thống.</p>
          )}
        </div>
      </div>
    </aside>
  );
}

function formatDepartment(department) {
  return department?.replace(/^Khoa\s+/i, "") || "";
}

function getInitials(name = "") {
  const initials = name
    .split(" ")
    .filter(Boolean)
    .slice(-2)
    .map((part) => part.charAt(0))
    .join("")
    .toUpperCase();

  return initials || "NA";
}
