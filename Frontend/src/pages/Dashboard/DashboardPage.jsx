import React, { useEffect, useState } from "react";
import styles from "./DashboardPage.module.css";
import { getDashboardOverviewAPI } from "../../services/dashboard-service";
import {
  getNotificationsByMemberAPI,
  markNotificationRecipientAsReadAPI,
} from "../../services/notification-service";
import useAuthStore from "../../store/auth-store";
import ActivityTimeline from "../../components/sections/Dashboard/ActivityTimeline";
import ComboChart from "../../components/sections/Dashboard/ComboChart";

const DEFAULT_OVERVIEW = {
  stats: [
    { label: "Thành viên",        value: 124, accent: "#3b82f6", bg: "#eff6ff", border: "#bfdbfe", sub: "Đang hoạt động", trend: 8 },
    { label: "Sự kiện tháng này", value: 8,   accent: "#f59e0b", bg: "#fffbeb", border: "#fde68a", sub: "Đã lên lịch" },
    { label: "Hoạt động",         value: 42,  accent: "#10b981", bg: "#f0fdf4", border: "#a7f3d0", sub: "Tổng trong năm" },
    { label: "Chờ phê duyệt",     value: 14,  accent: "#ef4444", bg: "#fff5f5", border: "#fecaca", sub: "Cần xử lý" },
  ],
  activities: [
    { text: "Đã thêm 5 thành viên mới hôm nay.",        time: "10 phút trước", to: "/memberadmin",   read: false },
    { text: "Cập nhật lịch họp ban chủ nhiệm.",          time: "1 giờ trước",   to: "/eventadmin",    read: false },
    { text: "Phê duyệt 3 đơn đăng ký tham gia.",         time: "2 giờ trước",   to: "/memberadmin",   read: false },
    { text: "Gửi thông báo sự kiện mới đến thành viên.", time: "5 giờ trước",   to: "/eventadmin",    read: true  },
    { text: "Workshop Git & GitHub đã kết thúc.",        time: "Hôm qua",       to: "/eventadmin",    read: true  },
    { text: "Tài liệu OOP được phê duyệt.",              time: "Hôm qua",       to: "/resourcesadmin",read: true  },
    { text: "Thêm 2 tài liệu mới vào kho học thuật.",   time: "2 ngày trước",  to: "/resourcesadmin",read: true  },
    { text: "Hackathon 24h đã được lên lịch.",           time: "2 ngày trước",  to: "/eventadmin",    read: true  },
    { text: "Báo cáo tài chính Q4 đã cập nhật.",         time: "3 ngày trước",  to: "/finance",       read: true  },
    { text: "3 thành viên mới đăng ký tham gia CLB.",    time: "3 ngày trước",  to: "/memberadmin",   read: true  },
    { text: "Seminar Trí tuệ Nhân tạo sắp diễn ra.",    time: "4 ngày trước",  to: "/eventadmin",    read: true  },
  ],
  chartData: [
    { month: "T7",  events: 3,  docs: 8,  members: 12 },
    { month: "T8",  events: 5,  docs: 12, members: 18 },
    { month: "T9",  events: 4,  docs: 7,  members: 10 },
    { month: "T10", events: 8,  docs: 15, members: 25 },
    { month: "T11", events: 6,  docs: 11, members: 20 },
    { month: "T12", events: 9,  docs: 18, members: 30 },
  ],
};

const getActivityNotificationId = (activity) =>
  activity?.notificationId ?? activity?.id ?? activity?.notification?.notificationId;

const mergeActivityReadState = (activities = [], recipients = []) => {
  const readByNotificationId = new Map(
    (recipients || []).map((recipient) => [
      String(recipient.notificationId ?? recipient.id),
      Boolean(recipient.isRead),
    ])
  );

  return activities.map((activity) => {
    const notificationId = getActivityNotificationId(activity);
    if (notificationId == null) {
      return activity;
    }

    const read = readByNotificationId.get(String(notificationId));
    return read === undefined ? activity : { ...activity, read };
  });
};

const toDateInputValue = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const getDefaultDateRange = () => {
  const now = new Date();
  const start = new Date(now.getFullYear(), now.getMonth() - 5, 1);
  return {
    fromDate: toDateInputValue(start),
    toDate: toDateInputValue(now),
  };
};

// ── Main ──────────────────────────────────────────────────────
const DashboardPage = () => {
  const currentUser = useAuthStore((state) => state.user);
  const currentMemberId = currentUser?.memberId;
  const [overview, setOverview] = useState(DEFAULT_OVERVIEW);
  const [error, setError] = useState("");
  const [showWelcomeBadge, setShowWelcomeBadge] = useState(true);
  const [dateRange, setDateRange] = useState(getDefaultDateRange);

  useEffect(() => {
    const timer = window.setTimeout(() => setShowWelcomeBadge(false), 4000);
    return () => window.clearTimeout(timer);
  }, []);

  useEffect(() => {
    let ignore = false;

    const loadOverview = async () => {
      try {
        const [overviewData, recipientData] = await Promise.all([
          getDashboardOverviewAPI(dateRange),
          currentMemberId ? getNotificationsByMemberAPI(currentMemberId) : Promise.resolve([]),
        ]);

        if (ignore || !overviewData?.stats) return;

        setOverview((prev) => ({
          ...prev,
          stats: overviewData.stats,
          activities: mergeActivityReadState(
            overviewData.activities ?? prev.activities,
            recipientData
          ),
          chartData: overviewData.chartData ?? prev.chartData,
        }));
      } catch (err) {
        if (!ignore) {
          setError(err?.message || "Không thể tải dữ liệu");
        }
      }
    };

    loadOverview();
    return () => {
      ignore = true;
    };
  }, [currentMemberId, dateRange.fromDate, dateRange.toDate]);

  const handleDateChange = (field, value) => {
    setDateRange((prev) => {
      const next = { ...prev, [field]: value };
      if (!next.fromDate || !next.toDate) return next;

      if (new Date(next.fromDate) > new Date(next.toDate)) {
        if (field === "fromDate") {
          next.toDate = value;
        } else {
          next.fromDate = value;
        }
      }

      return next;
    });
  };

  const handleResetDateRange = () => {
    setDateRange(getDefaultDateRange());
  };

  const handleActivityRead = async (activity, index) => {
    setOverview((prev) => ({
      ...prev,
      activities: prev.activities.map((a, i) =>
        i === index ? { ...a, read: true } : a
      ),
    }));

    const notificationId = getActivityNotificationId(activity);
    if (!currentMemberId || notificationId == null || activity?.read) {
      return;
    }

    try {
      await markNotificationRecipientAsReadAPI(notificationId, currentMemberId);
    } catch {
      // Keep the optimistic UI update; the next refresh will reflect backend state.
    }
  };

  return (
    <div className={styles.page}>

      {/* Hero */}
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>Bảng thống kê</h1>
          <p className={styles.pageSubtitle}>
            Tổng quan nhanh về hoạt động, thành viên và sự kiện của câu lạc bộ.
          </p>
        </div>
        {showWelcomeBadge && (
          <div className={styles.pageBadge}>👋 Chào mừng trở lại</div>
        )}
      </div>

      {/* Stat Cards */}
      <div className={styles.statsGrid}>
        {overview.stats.map((stat) => (
          <div
            key={stat.label}
            className={styles.statCard}
            style={{
              backgroundColor: stat.bg,
              borderLeftColor: stat.accent,
              borderTopColor: stat.border,
              borderRightColor: stat.border,
              borderBottomColor: stat.border,
            }}
          >
            <p className={styles.statLabel}>{stat.label}</p>
            <p className={styles.statValue}>{stat.value}</p>
            <p className={styles.statSub}>{stat.sub}</p>
            {typeof stat.trend === "number" && (
              <span className={`${styles.statTrend} ${stat.trend >= 0 ? styles.trendUp : styles.trendDown}`}>
                {stat.trend >= 0 ? "↑" : "↓"} {Math.abs(stat.trend)}%
              </span>
            )}
          </div>
        ))}
      </div>

      {/* Body */}
      <div className={styles.bodyGrid}>
        <section className={styles.panel}>
          <div className={styles.panelHeader}>
            <h2>Thống kê theo mốc thời gian</h2>
            <div className={styles.chartFilterWrap}>
              <label className={styles.dateLabel}>
                Từ
                <input
                  type="date"
                  className={styles.dateInput}
                  value={dateRange.fromDate}
                  onChange={(event) => handleDateChange("fromDate", event.target.value)}
                  max={dateRange.toDate}
                />
              </label>
              <label className={styles.dateLabel}>
                Đến
                <input
                  type="date"
                  className={styles.dateInput}
                  value={dateRange.toDate}
                  onChange={(event) => handleDateChange("toDate", event.target.value)}
                  min={dateRange.fromDate}
                />
              </label>
              <button
                type="button"
                className={styles.resetRangeBtn}
                onClick={handleResetDateRange}
              >
                6 tháng gần nhất
              </button>
            </div>
          </div>
          <span className={styles.panelLabel}>Sự kiện · Tài liệu · Thành viên</span>
          <ComboChart data={overview.chartData} />
        </section>

        <section className={styles.panel}>
          <div className={styles.panelHeader}>
            <h2>Hoạt động gần đây</h2>
          </div>
          <ActivityTimeline
            activities={overview.activities}
            onRead={handleActivityRead}
          />
        </section>
      </div>

      {error && <p className={styles.errorText}>{error}</p>}
    </div>
  );
};

export default DashboardPage;
