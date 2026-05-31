import { useState } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./ActivityTimeline.module.css";

export default function ActivityTimeline({ activities, onRead }) {
  const navigate = useNavigate();
  const [tab, setTab] = useState("all");

  const handleClick = async (item, index) => {
    if (!item.to) return;
    await onRead(item, index);
    navigate(item.to);
  };

  const unreadCount = activities.filter((a) => !a.read).length;
  const displayed = tab === "unread"
    ? activities.filter((a) => !a.read)
    : activities;

  return (
    <div>
      <div className={styles.tabs}>
        <button
          className={`${styles.tab} ${tab === "all" ? styles.tabActive : ""}`}
          onClick={() => setTab("all")}
        >
          Tất cả
        </button>
        <button
          className={`${styles.tab} ${tab === "unread" ? styles.tabActive : ""}`}
          onClick={() => setTab("unread")}
        >
          Chưa xem
          {unreadCount > 0 && (
            <span className={styles.tabBadge}>{unreadCount}</span>
          )}
        </button>
      </div>

      <ul className={styles.timeline}>
        {displayed.length === 0 ? (
          <li className={styles.empty}>Không có thông báo chưa xem.</li>
        ) : (
          displayed.map((item, i) => {
            const originalIndex = activities.indexOf(item);
            return (
              <li
                key={i}
                className={`${styles.timelineItem} ${item.to ? styles.timelineClickable : ""}`}
                onClick={() => handleClick(item, originalIndex)}
              >
                <div className={styles.timelineDotWrap}>
                  {!item.read && <span className={styles.timelineDot} />}
                </div>
                <div className={styles.timelineContent}>
                  <p className={`${styles.timelineText} ${!item.read ? styles.timelineTextUnread : ""}`}>
                    {item.text}
                  </p>
                  <p className={styles.timelineTime}>{item.time}</p>
                </div>
                {item.to && <span className={styles.timelineArrow}>→</span>}
              </li>
            );
          })
        )}
      </ul>
    </div>
  );
}
