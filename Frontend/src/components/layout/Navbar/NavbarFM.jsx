import React, { useState, useEffect, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";
import styles from "./NavbarFM.module.css";
import logo from "../../../assets/logo/logo_cnpm.png";
import noti from "../../../assets/icons/noti.svg";
import setting from "../../../assets/icons/setting.svg";
import NotificationPopover from "./NotificationPopover";
import { getNotificationsByMemberAPI } from "../../../services/notification-service";
import useAuthStore from "../../../store/auth-store";
import { canAccessPath, isManager } from "../../../utils/access-control";
import {
  PROFILE_CUSTOM_UPDATED_EVENT,
  getCustomProfileKey,
  getInitials,
  readCustomProfile,
} from "../../../utils/profile-custom";

const targetTypeToNotificationType = (targetType = "") => {
  const value = targetType.toLowerCase();
  if (value.includes("member")) return "new_member";
  if (value.includes("event")) return "upcoming_event";
  if (value.includes("document") || value.includes("resource")) return "document";
  return "document";
};

const formatRelativeTime = (value) => {
  const date = value ? new Date(value) : null;
  if (!date || Number.isNaN(date.getTime())) return "";

  const diffMs = Date.now() - date.getTime();
  const diffMinutes = Math.max(0, Math.floor(diffMs / 60000));
  if (diffMinutes < 1) return "Vừa xong";
  if (diffMinutes < 60) return `${diffMinutes} phút trước`;

  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `${diffHours} giờ trước`;

  const diffDays = Math.floor(diffHours / 24);
  if (diffDays === 1) return "Hôm qua";
  return `${diffDays} ngày trước`;
};

const isToday = (value) => {
  const date = value ? new Date(value) : null;
  if (!date || Number.isNaN(date.getTime())) return false;

  const today = new Date();
  return date.toDateString() === today.toDateString();
};

const normalizeNotification = (item) => {
  const id = item.notificationId ?? item.id;

  return {
    id,
    type: targetTypeToNotificationType(item.targetType),
    actorName: item.title || "Thông báo",
    content: item.content || "",
    highlight: "",
    time: formatRelativeTime(item.sentAt),
    isUnread: !item.isRead,
    category: isToday(item.sentAt) ? "today" : "earlier",
    targetType: item.targetType,
    raw: item,
  };
};

const NavbarFM = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const queryClient = useQueryClient();
  const [openMenu, setOpenMenu] = useState(false);
  const [showNoti, setShowNoti] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [notificationLoading, setNotificationLoading] = useState(false);
  const [notificationError, setNotificationError] = useState("");
  const [profileAvatar, setProfileAvatar] = useState("");
  const profileRef = useRef(null);
  const currentUser = useAuthStore((state) => state.user);
  const token = useAuthStore((state) => state.token);
  const logout = useAuthStore((state) => state.logout);
  const currentMemberId = currentUser?.memberId;
  const canOpenSettings = isManager(currentUser);
  const canOpenProfile = canAccessPath("/profile", currentUser, token);
  const canOpenNotifications = true;

  useEffect(() => {
    let ignore = false;
    setNotifications([]);

    const loadNotifications = async () => {
      if (!currentMemberId) {
        setNotificationLoading(false);
        setNotificationError("");
        return;
      }

      setNotificationLoading(true);
      setNotificationError("");
      try {
        const items = await getNotificationsByMemberAPI(currentMemberId);
        if (ignore) return;

        setNotifications(
          (items || [])
            .slice()
            .sort((a, b) => new Date(b.sentAt || 0) - new Date(a.sentAt || 0))
            .map(normalizeNotification)
        );
      } catch (error) {
        if (!ignore) {
          setNotificationError(error?.message || "Không thể tải thông báo");
          setNotifications([]);
        }
      } finally {
        if (!ignore) {
          setNotificationLoading(false);
        }
      }
    };

    loadNotifications();
    return () => {
      ignore = true;
    };
  }, [currentMemberId]);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (profileRef.current && !profileRef.current.contains(e.target)) {
        setOpenMenu(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  useEffect(() => {
    const syncAvatar = () => {
      setProfileAvatar(readCustomProfile(currentMemberId).avatar);
    };

    syncAvatar();
    if (!currentMemberId) return undefined;

    const handleCustomProfileUpdate = (event) => {
      if (String(event.detail?.memberId) === String(currentMemberId)) {
        syncAvatar();
      }
    };

    const handleStorage = (event) => {
      if (event.key === getCustomProfileKey(currentMemberId)) {
        syncAvatar();
      }
    };

    window.addEventListener(PROFILE_CUSTOM_UPDATED_EVENT, handleCustomProfileUpdate);
    window.addEventListener("storage", handleStorage);

    return () => {
      window.removeEventListener(
        PROFILE_CUSTOM_UPDATED_EVENT,
        handleCustomProfileUpdate
      );
      window.removeEventListener("storage", handleStorage);
    };
  }, [currentMemberId]);

  const unreadCount = notifications.filter((n) => n.isUnread).length;

  const isSettingsActive = location.pathname === "/settings";
  const isProfileActive = location.pathname === "/profile";
  const isProfileOpenOrActive = openMenu || isProfileActive;
  const avatarFallback = currentUser?.studentId
    ? String(currentUser.studentId).slice(0, 2)
    : "TV";
  const avatarInitials = getInitials(
    currentUser?.fullName,
    avatarFallback
  );
  const handleLogout = () => {
    logout();
    queryClient.clear();
    setOpenMenu(false);
    navigate("/");
  };

  return (
    <nav className={styles.navbar}>
      <div className={styles.logoSection}>
        <img
          src={logo}
          alt="THMN club logo"
          className={styles.logoImg}
        />
        <div className={styles.logoText}>
          <h1 className={styles.logoTitle}>THMN</h1>
          <h2 className={styles.logoSubtitle}>Academic Club</h2>
          <h3 className={styles.logoSlogan}>KNOWLEDGE - LEADERSHIP - IMPACT</h3>
        </div>
      </div>

      <div className={styles.actionsSection}>
        {canOpenSettings && (
          <button
            className={`${styles.iconBtn} ${isSettingsActive ? styles.activeIconBtn : ""}`}
            title="Settings"
            onClick={() => navigate("/settings")}
          >
            <img src={setting} alt="Settings" className={styles.iconImg} />
          </button>
        )}

        {canOpenNotifications && (
          <div className={styles.notificationContainer}>
            <button
              className={`${styles.iconBtn} ${showNoti ? styles.activeIconBtn : ""}`}
              title="Notifications"
              onClick={() => setShowNoti(!showNoti)}
              data-bell-button="true"
            >
              <img src={noti} alt="Notifications" className={styles.iconImg} />
              {unreadCount > 0 && (
                <span className={styles.bellBadge}>{unreadCount}</span>
              )}
            </button>

            {showNoti && (
              <NotificationPopover
                notifications={notifications}
                setNotifications={setNotifications}
                isLoading={notificationLoading}
                error={notificationError}
                onClose={() => setShowNoti(false)}
              />
            )}
          </div>
        )}

        <div className={styles.profileContainer} ref={profileRef}>
          <button
            className={`${styles.profileBtn} ${isProfileOpenOrActive ? styles.activeProfileBtn : ""}`}
            title="Profile"
            onClick={() => setOpenMenu(!openMenu)}
          >
            <div className={`${styles.avatar} ${isProfileOpenOrActive ? styles.activeAvatar : ""}`}>
              {profileAvatar ? (
                <img
                  src={profileAvatar}
                  alt={currentUser?.fullName || "Avatar"}
                  className={styles.avatarImg}
                />
              ) : (
                <span className={styles.avatarInitials}>{avatarInitials}</span>
              )}
            </div>
          </button>

          {openMenu && (
            <div className={styles.dropdownMenu}>
              {canOpenProfile && (
                <button
                  className={styles.dropdownItem}
                  onClick={() => {
                    navigate("/profile");
                    setOpenMenu(false);
                  }}
                >
                  Hồ sơ
                </button>
              )}

              <button className={styles.dropdownItem} onClick={handleLogout}>
                Đăng xuất
              </button>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default NavbarFM;
