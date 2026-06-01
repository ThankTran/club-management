import { useEffect, useState } from 'react';
import styles from './ResourceRejectModal.module.css';

export default function ResourceRejectModal({ resource, onCancel, onConfirm, loading = false }) {
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    if (resource) {
      setReason('');
      setError('');
    }
  }, [resource]);

  if (!resource) return null;

  const handleSubmit = (event) => {
    event.preventDefault();
    const trimmedReason = reason.trim();

    if (!trimmedReason) {
      setError('Vui lòng nhập lý do từ chối tài liệu.');
      return;
    }

    onConfirm(resource.id, trimmedReason);
  };

  return (
    <div className={styles.overlay} onClick={onCancel}>
      <form className={styles.box} onSubmit={handleSubmit} onClick={(event) => event.stopPropagation()}>
        <div className={styles.icon}>!</div>
        <h3 className={styles.title}>Từ chối tài liệu</h3>
        <p className={styles.message}>
          Nhập lý do từ chối cho phiếu <strong>{resource.formCode}</strong> - <em>{resource.title}</em>.
        </p>

        <label className={styles.field}>
          <span>Lý do từ chối</span>
          <textarea
            value={reason}
            onChange={(event) => {
              setReason(event.target.value);
              if (error) setError('');
            }}
            rows={5}
            placeholder="Ví dụ: Tài liệu trùng nội dung, tệp không mở được, sai định dạng..."
            autoFocus
          />
        </label>

        {error && <p className={styles.error}>{error}</p>}

        <div className={styles.actions}>
          <button type="button" className={styles.cancelBtn} onClick={onCancel} disabled={loading}>Hủy</button>
          <button type="submit" className={styles.rejectBtn} disabled={loading}>
            {loading ? 'Đang xử lý...' : 'Xác nhận từ chối'}
          </button>
        </div>
      </form>
    </div>
  );
}
