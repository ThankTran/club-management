import React, { useState, useRef, useEffect } from "react";
import {
  FileText,
  UserPlus,
  CalendarPlus,
  Clock,
  MoreHorizontal,
  Trash2,
  Settings,
  CheckCheck,
  BellOff,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import useAuthStore from "../../../store/auth-store";
import { isManager } from "../../../utils/access-control";
import {
  deleteNotificationRecipientAPI,
  markNotificationRecipientAsReadAPI,
} from "../../../services/notification-service";
import styles from "./NotificationPopover.module.css";

const NotificationPopover = ({
  notifications,
  setNotifications,
  isLoading = false,
  error = "",
  onClose,
}) => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const isAdmin = isManager(user);
  const memberId = user?.memberId;
  const [activeTab, setActiveTab] = useState("all");
  const [showMoreMenu, setShowMoreMenu] = useState(false);
  const menuRef = useRef(null);
  const popoverRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setShowMoreMenu(false);
      }
      if (popoverRef.current && !popoverRef.current.contains(event.target)) {
        const bellBtn = document.querySelector('[data-bell-button="true"]');
        if (bellBtn && !bellBtn.contains(event.target)) {
          onClose();
        }
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [onClose]);

  const persistRead = (id) => {
    if (!memberId || !id) return;
    markNotificationRecipientAsReadAPI(id, memberId).catch(() => {});
  };

  const normalizeText = (value = "") =>
    String(value)
      .toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .trim();

  const isDueNotification = (noti) => {
    const targetType = normalizeText(noti?.targetType);
    const title = normalizeText(noti?.actorName);
    const content = normalizeText(noti?.content);
    const text = `${title} ${content}`.trim();

    if (targetType === "finance") {
      return (
        text.includes("dong quy") ||
        text.includes("can dong quy") ||
        text.includes("khoan quy") ||
        text.includes("quy thang") ||
        text.includes("thanh toan dang cho xac nhan")
      );
    }

    return (
      text.includes("dong quy") ||
      text.includes("can dong quy") ||
      text.includes("khoan quy") ||
      text.includes("quy thang") ||
      text.includes("da dong quy") ||
      text.includes("dong tien thanh cong")
    );
  };

  const resolveNotificationPath = (noti) => {
    const targetType = normalizeText(noti?.targetType);
    const isManagerRoute = isAdmin;

    switch (targetType) {
      case "member":
      case "members":
        return isManagerRoute ? "/memberadmin" : "/memberuser";
      case "event":
      case "events":
        return isManagerRoute ? "/eventadmin" : "/eventuser";
      case "document":
      case "documents":
      case "resource":
      case "resources":
        return isManagerRoute ? "/resourcesadmin" : "/resourcesuser";
      case "finance":
      case "transaction":
      case "transactions":
        return isDueNotification(noti)
          ? "/memberdues"
          : (isManagerRoute ? "/finance" : "/memberdues");
      default:
        return isManagerRoute ? "/dashboard" : "/home";
    }
  };

  const handleMarkAsRead = (id) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, isUnread: false } : n))
    );
    persistRead(id);
  };

  // Handle single-click: mark as read AND navigate
  const handleClick = (noti) => {
    if (noti.isUnread) {
      handleMarkAsRead(noti.id);
    }

    navigate(resolveNotificationPath(noti));
    onClose();
  };

  const handleMarkAllAsRead = () => {
    const unreadIds = notifications.filter((n) => n.isUnread).map((n) => n.id);
    setNotifications((prev) => prev.map((n) => ({ ...n, isUnread: false })));
    unreadIds.forEach(persistRead);
    setShowMoreMenu(false);
  };

  const handleClearAll = () => {
    if (memberId) {
      notifications.forEach((item) => {
        deleteNotificationRecipientAPI(item.id, memberId).catch(() => {});
      });
    }
    setNotifications([]);
    setShowMoreMenu(false);
  };

  const handleDeleteNotification = (id, e) => {
    e.stopPropagation();
    if (memberId) {
      deleteNotificationRecipientAPI(id, memberId).catch(() => {});
    }
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  };

  const filteredNotifications = notifications.filter((n) => {
    if (activeTab === "unread") return n.isUnread;
    return true;
  });

  const todayNotis = filteredNotifications.filter(
    (n) => n.category === "today" || n.category === "Hôm nay"
  );
  const earlierNotis = filteredNotifications.filter(
    (n) => n.category === "earlier" || n.category === "Trước đó"
  );
  const UNGROUPED_NOTIS = filteredNotifications.filter(
    (n) => !todayNotis.includes(n) && !earlierNotis.includes(n)
  );

  return (
    <div className={styles.popoverContainer} ref={popoverRef}>
      <div className={styles.header}>
        <h2 className={styles.title}>Thông báo</h2>
        <div className={styles.moreMenuContainer} ref={menuRef}>
          <button
            className={styles.moreBtn}
            onClick={() => setShowMoreMenu(!showMoreMenu)}
            title="Tùy chọn thông báo"
          >
            <MoreHorizontal size={20} />
          </button>

          {showMoreMenu && (
            <div className={styles.moreDropdown}>
              <button className={styles.menuItem} onClick={handleMarkAllAsRead}>
                <CheckCheck size={16} />
                <span>Đánh dấu tất cả đã đọc</span>
              </button>
              <button className={styles.menuItem} onClick={handleClearAll}>
                <Trash2 size={16} />
                <span>Xóa tất cả thông báo</span>
              </button>
            </div>
          )}
        </div>
      </div>

      <div className={styles.tabs}>
        <button
          className={`${styles.tab} ${activeTab === "all" ? styles.activeTab : ""}`}
          onClick={() => setActiveTab("all")}
        >
          Tất cả
        </button>
        <button
          className={`${styles.tab} ${activeTab === "unread" ? styles.activeTab : ""}`}
          onClick={() => setActiveTab("unread")}
        >
          Chưa đọc
          {notifications.filter((n) => n.isUnread).length > 0 && (
            <span className={styles.unreadCountBadge}>
              {notifications.filter((n) => n.isUnread).length}
            </span>
          )}
        </button>
      </div>

      <div className={styles.listScroll}>
        {isLoading ? (
          <div className={styles.emptyState}>
            <p className={styles.emptyText}>Đang tải thông báo...</p>
          </div>
        ) : error ? (
          <div className={styles.emptyState}>
            <BellOff size={48} className={styles.emptyIcon} />
            <p className={styles.emptyText}>{error}</p>
          </div>
        ) : filteredNotifications.length === 0 ? (
          <div className={styles.emptyState}>
            <BellOff size={48} className={styles.emptyIcon} />
            <p className={styles.emptyText}>Không có thông báo nào</p>
          </div>
        ) : (
          <>
            {todayNotis.length > 0 && (
              <div className={styles.section}>
                <h3 className={styles.sectionHeader}>Hôm nay</h3>
                <div className={styles.sectionList}>
                  {todayNotis.map((noti) => (
                    <NotificationItem 
                      key={noti.id} 
                      noti={noti} 
                      onDelete={handleDeleteNotification}
                      onClick={handleClick}
                    />
                  ))}
                </div>
              </div>
            )}

            {earlierNotis.length > 0 && (
              <div className={styles.section}>
                <h3 className={styles.sectionHeader}>Trước đó</h3>
                <div className={styles.sectionList}>
                  {earlierNotis.map((noti) => (
                    <NotificationItem 
                      key={noti.id} 
                      noti={noti} 
                      onDelete={handleDeleteNotification}
                      onClick={handleClick}
                    />
                  ))}
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

// Sub-component for individual notification item
const NotificationItem = ({ noti, onDelete, onClick }) => {
  // Generate a beautiful monochromatic illustration icon based on the type
  const getIllustrationIcon = (type) => {
    switch (type) {
      case "document":
        return <FileText size={22} className={styles.illustrationIcon} />;
      case "new_member":
        return <UserPlus size={22} className={styles.illustrationIcon} />;
      case "new_event":
        return <CalendarPlus size={22} className={styles.illustrationIcon} />;
      case "upcoming_event":
        return <Clock size={22} className={styles.illustrationIcon} />;
      default:
        return <FileText size={22} className={styles.illustrationIcon} />;
    }
  };

  return (
    <div
      className={`${styles.notiItem} ${noti.isUnread ? styles.unreadItem : ""}`}
      onClick={() => onClick(noti)}
      style={{ cursor: "pointer" }}
    >
      <div className={`${styles.illustrationWrapper} ${styles[`illustration_${noti.type}`]}`}>
        {getIllustrationIcon(noti.type)}
      </div>

      <div className={styles.contentWrapper}>
        <p className={styles.notiText}>
          <span className={styles.actorName}>{noti.actorName}</span>
          {noti.content && <> {noti.content} </>}
          {noti.highlight && (
            <span className={styles.highlightText}>{noti.highlight}</span>
          )}
        </p>
        <span className={`${styles.time} ${noti.isUnread ? styles.unreadTime : ""}`}>
          {noti.time}
        </span>
      </div>

      <div className={styles.rightActions}>
        {noti.isUnread && <div className={styles.unreadDot}></div>}
        <button
          className={styles.deleteItemBtn}
          onClick={(e) => onDelete(noti.id, e)}
          title="Xóa thông báo"
        >
          <Trash2 size={14} />
        </button>
      </div>
    </div>
  );
};

export default NotificationPopover;
