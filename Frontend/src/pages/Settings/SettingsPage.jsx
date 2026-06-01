import React, { useEffect, useState } from "react";
import styles from "./SettingsPage.module.css";
import useScrollReveal from "../../hooks/useScrollReveal";
import ActionToast from "../../components/common/ActionToast/ActionToast";
import useActionToast from "../../hooks/useActionToast";

import notiIcon from "../../assets/icons/noti.svg";
import shieldIcon from "../../assets/icons/shield.svg";
import settingIcon from "../../assets/icons/setting.svg";
import {
    getDepartmentsAPI,
    createDepartmentAPI,
    deleteDepartmentAPI,
} from "../../services/department-service";

import {
    getSubjectsAPI,
    createSubjectAPI,
    updateSubjectAPI,
    deleteSubjectAPI,
} from "../../services/subject-service";

import {
    getMonthlyDueAmountAPI,
    saveMonthlyDueAmountAPI,
} from "../../services/system-setting-service";
const DEFAULT_FUND_AMOUNT = "50000";

const getErrorMessage = (error, fallback) => {
  if (typeof error === "string") return error;
  return error?.message || error?.error || fallback;
};

const normalizeDepartment = (department = {}) => ({
  departmentId: department.departmentId,
  departmentName: department.departmentName || "",
});

const normalizeSubject = (subject = {}) => ({
  subjectId: subject.subjectId,
  subjectName: subject.subjectName || "",
});

export default function SettingsPage() {
  useScrollReveal();
  const currentUser = JSON.parse(localStorage.getItem("user") || "null");

  // Active Tab State: 'notifications' | 'system'
  const [activeTab, setActiveTab] = useState("notifications");

  const { toast, showSuccess, showError, showPending } = useActionToast();

    const [notifications, setNotifications] = useState({
        emailAlerts: true,
        activityUpdates: true,
        financeReminder: true,
        documentUploads: true,
        eventRegistration: true,
    });

    const [systemLoading, setSystemLoading] = useState(false);
    const [savingSystem, setSavingSystem] = useState(false);

    const [departments, setDepartments] = useState([]);
    const [subjects, setSubjects] = useState([]);

    const [newDept, setNewDept] = useState("");
    const [newSubject, setNewSubject] = useState("");

    const [fundAmount, setFundAmount] = useState(DEFAULT_FUND_AMOUNT);

    const [isAddingDept, setIsAddingDept] = useState(false);
    const [removingDeptId, setRemovingDeptId] = useState(null);

    const [isAddingSubject, setIsAddingSubject] = useState(false);
    const [editingSubjectId, setEditingSubjectId] = useState(null);
    const [editingSubjectName, setEditingSubjectName] = useState("");
    const [savingSubjectId, setSavingSubjectId] = useState(null);
    const [removingSubjectId, setRemovingSubjectId] = useState(null);

  const showToast = (message, type = "success") => {
    if (type === "error") {
      showError(message);
      return;
    }
    if (type === "info") {
      showPending(message);
      return;
    }
    showSuccess(message);
  };

  useEffect(() => {
    let ignore = false;

    const loadSystemSettings = async () => {
      setSystemLoading(true);

      try {
        const [departmentItems, subjectItems, monthlyDueSetting] = await Promise.all([
          getDepartmentsAPI(),
          getSubjectsAPI(),
          getMonthlyDueAmountAPI().catch(() => null),
        ]);

        if (ignore) return;

        setDepartments(
          (departmentItems || [])
            .map(normalizeDepartment)
            .filter((department) => department.departmentId)
        );
        setSubjects(
          (subjectItems || [])
            .map(normalizeSubject)
            .filter((subject) => subject.subjectId)
        );
        setFundAmount(monthlyDueSetting?.settingValue || DEFAULT_FUND_AMOUNT);
      } catch (error) {
        if (!ignore) {
          setDepartments([]);
          setSubjects([]);
          setFundAmount(DEFAULT_FUND_AMOUNT);
          showToast(
            getErrorMessage(error, "Không thể tải cài đặt hệ thống"),
            "error"
          );
        }
      } finally {
        if (!ignore) {
          setSystemLoading(false);
        }
      }
    };

    loadSystemSettings();

    return () => {
      ignore = true;
    };
  }, []);

  const handleToggleNoti = (key) => {
    setNotifications((prev) => {
      const updated = { ...prev, [key]: !prev[key] };
      showToast(
        `Đã ${updated[key] ? "bật" : "tắt"} thông báo ${
          key === "emailAlerts"
            ? "qua Email"
            : key === "activityUpdates"
              ? "hoạt động CLB"
              : key === "financeReminder"
                ? "nhắc nhở tài chính"
                : key === "documentUploads"
                  ? "khi có tài liệu mới"
                  : "đăng ký sự kiện"
        }.`
      );
      return updated;
    });
  };

  const handleAddDept = async () => {
    const departmentName = newDept.trim();

    if (!departmentName) {
      showToast("Tên khoa không được để trống", "error");
      return;
    }

    setIsAddingDept(true);
    try {
      const created = await createDepartmentAPI({ departmentName });
      setDepartments((prev) => [...prev, normalizeDepartment(created)]);
      setNewDept("");
      showToast(`Đã thêm khoa "${departmentName}" thành công`);
    } catch (error) {
      showToast(getErrorMessage(error, "Không thể thêm khoa"), "error");
    } finally {
      setIsAddingDept(false);
    }
  };

  const handleRemoveDept = async (department) => {
    if (!department?.departmentId) return;

    setRemovingDeptId(department.departmentId);
    try {
      await deleteDepartmentAPI(department.departmentId);
      setDepartments((prev) =>
        prev.filter((item) => item.departmentId !== department.departmentId)
      );
      showToast(`Đã xóa khoa "${department.departmentName}"`);
    } catch (error) {
      showToast(getErrorMessage(error, "Không thể xóa khoa này"), "error");
    } finally {
      setRemovingDeptId(null);
    }
  };

  const handleAddSubject = async () => {
    const subjectName = newSubject.trim();

    if (!subjectName) {
      showToast("Tên môn học không được để trống", "error");
      return;
    }

    setIsAddingSubject(true);
    try {
      const created = await createSubjectAPI({ subjectName });
      setSubjects((prev) => [...prev, normalizeSubject(created)]);
      setNewSubject("");
      showToast(`Đã thêm môn học "${subjectName}" thành công`);
    } catch (error) {
      showToast(getErrorMessage(error, "Không thể thêm môn học"), "error");
    } finally {
      setIsAddingSubject(false);
    }
  };

  const handleStartEditSubject = (subject) => {
    setEditingSubjectId(subject.subjectId);
    setEditingSubjectName(subject.subjectName);
  };

  const handleCancelEditSubject = () => {
    setEditingSubjectId(null);
    setEditingSubjectName("");
  };

  const handleSaveSubject = async (subject) => {
    const subjectName = editingSubjectName.trim();

    if (!subjectName) {
      showToast("Tên môn học không được để trống", "error");
      return;
    }

    if (subjectName === subject.subjectName) {
      handleCancelEditSubject();
      return;
    }

    setSavingSubjectId(subject.subjectId);
    try {
      const updated = await updateSubjectAPI(subject.subjectId, { subjectName });
      const normalized = normalizeSubject(updated);
      setSubjects((prev) =>
        prev.map((item) =>
          item.subjectId === normalized.subjectId ? normalized : item
        )
      );
      handleCancelEditSubject();
      showToast(`Đã cập nhật môn học "${subjectName}"`);
    } catch (error) {
      showToast(getErrorMessage(error, "Không thể cập nhật môn học"), "error");
    } finally {
      setSavingSubjectId(null);
    }
  };

  const handleRemoveSubject = async (subject) => {
    if (!subject?.subjectId) return;

    setRemovingSubjectId(subject.subjectId);
    try {
      await deleteSubjectAPI(subject.subjectId);
      setSubjects((prev) =>
        prev.filter((item) => item.subjectId !== subject.subjectId)
      );
      if (editingSubjectId === subject.subjectId) {
        handleCancelEditSubject();
      }
      showToast(`Đã xóa môn học "${subject.subjectName}"`);
    } catch (error) {
      showToast(getErrorMessage(error, "Không thể xóa môn học này"), "error");
    } finally {
      setRemovingSubjectId(null);
    }
  };

  const handleSystemSave = async (e) => {
    e.preventDefault();
    const parsed = Number(fundAmount);

    if (!fundAmount || !/^\d+$/.test(fundAmount) || Number.isNaN(parsed) || parsed < 0) {
      showToast("Số tiền quỹ không hợp lệ", "error");
      return;
    }

    setSavingSystem(true);
    try {
      await saveMonthlyDueAmountAPI({
        settingValue: fundAmount,
        updatedById: currentUser?.memberId || null,
      });
      showToast("Cài đặt hệ thống đã được lưu thành công!");
    } catch (error) {
      showToast(
        getErrorMessage(error, "Không thể lưu cài đặt hệ thống"),
        "error"
      );
    } finally {
      setSavingSystem(false);
    }
  };

  const departmentBusy = systemLoading || isAddingDept || removingDeptId !== null;
  const subjectBusy =
    systemLoading ||
    isAddingSubject ||
    savingSubjectId !== null ||
    removingSubjectId !== null;

  return (
    <div className={styles.container}>
      <ActionToast toast={toast} />

      <div className={`${styles.headerBlock} reveal`}>
        <div className={styles.coverBanner}>
          <div className={styles.coverOverlay}></div>
          <div className={styles.headerContent}>
            <div className={styles.headerTitleRow}>
              <div className={styles.iconCircle}>
                <img
                  src={settingIcon}
                  alt="Settings"
                  className={styles.titleIcon}
                />
              </div>
              <div>
                <h1 className={styles.title}>Cài đặt hệ thống</h1>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className={styles.layoutBody}>
        <div className={`${styles.navTabs} reveal-left`}>
          <button
            className={`${styles.tabBtn} ${
              activeTab === "notifications" ? styles.activeTab : ""
            }`}
            onClick={() => setActiveTab("notifications")}
          >
            <img src={notiIcon} alt="" className={styles.tabIcon} />
            <span>Tùy chọn thông báo</span>
          </button>

          <button
            className={`${styles.tabBtn} ${
              activeTab === "system" ? styles.activeTab : ""
            }`}
            onClick={() => setActiveTab("system")}
          >
            <img src={shieldIcon} alt="" className={styles.tabIcon} />
            <span>Cài đặt hệ thống</span>
          </button>
        </div>

        <div className={`${styles.contentCard} reveal-right`}>
          {/* TAB 2: NOTIFICATIONS */}
          {activeTab === "notifications" && (
            <div className={styles.tabContent}>
              <div className={styles.contentHeader}>
                <h2>Tùy chọn thông báo</h2>
                <p>
                  Kiểm soát cách thức và tần suất bạn nhận được thông báo từ
                  hoạt động câu lạc bộ.
                </p>
              </div>

              <div className={styles.toggleGroup}>
                <div className={styles.toggleItem}>
                  <div className={styles.toggleText}>
                    <h4>Thông báo qua Email</h4>
                    <p>
                      Nhận thư tóm tắt hàng tuần, thông tin tài chính quan
                      trọng qua email của bạn.
                    </p>
                  </div>
                  <label className={styles.switch}>
                    <input
                      type="checkbox"
                      checked={notifications.emailAlerts}
                      onChange={() => handleToggleNoti("emailAlerts")}
                    />
                    <span className={styles.slider}></span>
                  </label>
                </div>

                <div className={styles.toggleItem}>
                  <div className={styles.toggleText}>
                    <h4>Thông báo hoạt động CLB</h4>
                    <p>
                      Đăng ký các thông tin sự kiện mới phát sinh và tin tức
                      nội bộ CLB.
                    </p>
                  </div>
                  <label className={styles.switch}>
                    <input
                      type="checkbox"
                      checked={notifications.activityUpdates}
                      onChange={() => handleToggleNoti("activityUpdates")}
                    />
                    <span className={styles.slider}></span>
                  </label>
                </div>

                <div className={styles.toggleItem}>
                  <div className={styles.toggleText}>
                    <h4>Tải lên tài liệu học thuật</h4>
                    <p>
                      Gửi thông báo khi có tài liệu học tập mới thuộc chuyên
                      ngành của bạn được phê duyệt.
                    </p>
                  </div>
                  <label className={styles.switch}>
                    <input
                      type="checkbox"
                      checked={notifications.documentUploads}
                      onChange={() => handleToggleNoti("documentUploads")}
                    />
                    <span className={styles.slider}></span>
                  </label>
                </div>
              </div>
            </div>
          )}

          {activeTab === "system" && (
            <div className={styles.tabContent}>
              <div className={styles.contentHeader}>
                <h2>Cài đặt hệ thống</h2>
                <p>
                  Quản lý danh sách khoa và số tiền đóng quỹ định kỳ của câu
                  lạc bộ.
                </p>
              </div>

              <form onSubmit={handleSystemSave} className={styles.settingsForm}>
                <div className={styles.sectionCard}>
                  <h3>Quản lý khoa</h3>
                  <div className={styles.formGroup}>
                    <label>Thêm khoa mới</label>
                    <div className={styles.inlineInputRow}>
                      <input
                        type="text"
                        placeholder="Nhập tên khoa..."
                        value={newDept}
                        disabled={departmentBusy}
                        onChange={(e) => setNewDept(e.target.value)}
                        onKeyDown={(e) => {
                          if (e.key === "Enter") {
                            e.preventDefault();
                            handleAddDept();
                          }
                        }}
                      />
                      <button
                        type="button"
                        className={styles.addDeptBtn}
                        onClick={handleAddDept}
                        disabled={departmentBusy}
                      >
                        {isAddingDept ? "Đang thêm..." : "+ Thêm"}
                      </button>
                    </div>
                  </div>

                  <div className={styles.deptList}>
                    {systemLoading && (
                      <p className={styles.emptyText}>Đang tải danh sách khoa...</p>
                    )}
                    {!systemLoading && departments.length === 0 && (
                      <p className={styles.emptyText}>
                        Chưa có khoa nào được thêm.
                      </p>
                    )}
                    {!systemLoading &&
                      departments.map((dept, idx) => (
                        <div key={dept.departmentId} className={styles.deptItem}>
                          <div className={styles.deptInfo}>
                            <span className={styles.deptIndex}>{idx + 1}</span>
                            <span className={styles.deptName}>
                              {dept.departmentName}
                            </span>
                          </div>
                          <button
                            type="button"
                            className={styles.removeDeptBtn}
                            onClick={() => handleRemoveDept(dept)}
                            disabled={
                              departmentBusy || removingDeptId === dept.departmentId
                            }
                            title="Xóa khoa"
                          >
                            {removingDeptId === dept.departmentId ? "..." : "×"}
                          </button>
                        </div>
                      ))}
                  </div>
                </div>

                <div className={styles.sectionCard}>
                  <h3>Quản lý môn học</h3>
                  <div className={styles.formGroup}>
                    <label>Thêm môn học mới</label>
                    <div className={styles.inlineInputRow}>
                      <input
                        type="text"
                        placeholder="Nhập tên môn học..."
                        value={newSubject}
                        disabled={subjectBusy}
                        onChange={(e) => setNewSubject(e.target.value)}
                        onKeyDown={(e) => {
                          if (e.key === "Enter") {
                            e.preventDefault();
                            handleAddSubject();
                          }
                        }}
                      />
                      <button
                        type="button"
                        className={styles.addDeptBtn}
                        onClick={handleAddSubject}
                        disabled={subjectBusy}
                      >
                        {isAddingSubject ? "Đang thêm..." : "+ Thêm"}
                      </button>
                    </div>
                  </div>

                  <div className={styles.deptList}>
                    {systemLoading && (
                      <p className={styles.emptyText}>
                        Đang tải danh sách môn học...
                      </p>
                    )}
                    {!systemLoading && subjects.length === 0 && (
                      <p className={styles.emptyText}>
                        Chưa có môn học nào được thêm.
                      </p>
                    )}
                    {!systemLoading &&
                      subjects.map((subject, idx) => {
                        const isEditing =
                          editingSubjectId === subject.subjectId;
                        const isSaving =
                          savingSubjectId === subject.subjectId;
                        const isRemoving =
                          removingSubjectId === subject.subjectId;

                        return (
                          <div
                            key={subject.subjectId}
                            className={styles.deptItem}
                          >
                            <div className={styles.deptInfo}>
                              <span className={styles.deptIndex}>{idx + 1}</span>
                              {isEditing ? (
                                <input
                                  className={styles.editSubjectInput}
                                  type="text"
                                  value={editingSubjectName}
                                  autoFocus
                                  disabled={isSaving || isRemoving}
                                  onChange={(e) =>
                                    setEditingSubjectName(e.target.value)
                                  }
                                  onKeyDown={(e) => {
                                    if (e.key === "Enter") {
                                      e.preventDefault();
                                      handleSaveSubject(subject);
                                    }
                                    if (e.key === "Escape") {
                                      handleCancelEditSubject();
                                    }
                                  }}
                                />
                              ) : (
                                <span className={styles.deptName}>
                                  {subject.subjectName}
                                </span>
                              )}
                            </div>
                            <div className={styles.itemActions}>
                              {isEditing ? (
                                <>
                                  <button
                                    type="button"
                                    className={styles.saveItemBtn}
                                    onClick={() => handleSaveSubject(subject)}
                                    disabled={isSaving || isRemoving}
                                    title="Lưu môn học"
                                  >
                                    {isSaving ? "..." : "✓"}
                                  </button>
                                  <button
                                    type="button"
                                    className={styles.cancelItemBtn}
                                    onClick={handleCancelEditSubject}
                                    disabled={isSaving || isRemoving}
                                    title="Hủy sửa"
                                  >
                                    ×
                                  </button>
                                </>
                              ) : (
                                <button
                                  type="button"
                                  className={styles.editItemBtn}
                                  onClick={() => handleStartEditSubject(subject)}
                                  disabled={subjectBusy}
                                  title="Sửa môn học"
                                >
                                  ✎
                                </button>
                              )}
                              <button
                                type="button"
                                className={styles.removeDeptBtn}
                                onClick={() => handleRemoveSubject(subject)}
                                disabled={subjectBusy || isRemoving}
                                title="Xóa môn học"
                              >
                                {isRemoving ? "..." : "×"}
                              </button>
                            </div>
                          </div>
                        );
                      })}
                  </div>
                </div>

                <div className={styles.sectionCard}>
                  <h3>Quỹ định kỳ</h3>
                  <div className={styles.formGroup}>
                    <label htmlFor="fund-amount">
                      Số tiền đóng quỹ định kỳ (VND)
                    </label>
                    <div className={styles.fundInputWrapper}>
                      <input
                        id="fund-amount"
                        type="text"
                        inputMode="numeric"
                        pattern="[0-9]*"
                        value={fundAmount}
                        disabled={systemLoading || savingSystem}
                        onChange={(e) => {
                          const val = e.target.value.replace(/^0+(?=\d)/, "");
                          if (/^\d*$/.test(val)) setFundAmount(val);
                        }}
                      />
                      <span className={styles.fundUnit}>VND</span>
                    </div>
                    <p className={styles.fundHint}>
                      Hiện tại:{" "}
                      <strong>
                        {(Number(fundAmount) || 0).toLocaleString("vi-VN")} VND
                      </strong>{" "}
                      / kỳ
                    </p>
                  </div>
                </div>

                <div className={styles.formActions}>
                  <button
                    type="submit"
                    className={styles.saveSubmitBtn}
                    disabled={systemLoading || savingSystem}
                  >
                    {savingSystem
                      ? "Đang lưu..."
                      : "Lưu cài đặt hệ thống"}
                  </button>
                </div>
              </form>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
