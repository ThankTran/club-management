import styles from './EventEvaluationModal.module.css';

export default function EventEvaluationModal({
  event,
  form,
  errors,
  evaluationCode,
  onChange,
  onClose,
  onSubmit,
}) {
  if (!event) return null;
  const isEditingEvaluation = Boolean(event.evaluation && event.evaluation.trim());

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.evaluationBox} onClick={(e) => e.stopPropagation()}>
        <div className={styles.evaluationHeader}>
          <div>
            <h3 className={styles.title}>{isEditingEvaluation ? 'Chỉnh sửa đánh giá sự kiện' : 'Phiếu đánh giá sự kiện'}</h3>
            <p className={styles.evaluationMeta}>{event.eventCode} - {event.title}</p>
          </div>
          <button className={styles.closeIconBtn} onClick={onClose} aria-label="Đóng">
            <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <path d="M18 6L6 18M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <div className={styles.evaluationFields}>
          <label>
            <span>Mã phiếu</span>
            <input className={styles.input} value={evaluationCode} readOnly />
          </label>
          <label>
            <span>Ngày đánh giá</span>
            <input
              className={`${styles.input} ${errors.evaluationDate ? styles.inputError : ''}`}
              type="date"
              value={form.evaluationDate}
              readOnly
            />
            {errors.evaluationDate && <p className={styles.formError}>{errors.evaluationDate}</p>}
          </label>
          <label>
            <span>Đánh giá</span>
            <textarea
              className={`${styles.evaluationTextarea} ${errors.evaluation ? styles.inputError : ''}`}
              rows={4}
              value={form.evaluation}
              onChange={(e) => onChange('evaluation', e.target.value)}
              placeholder="Nhập kết quả, nhận xét hoặc bài học sau sự kiện..."
            />
            {errors.evaluation && <p className={styles.formError}>{errors.evaluation}</p>}
          </label>
        </div>

        <div className={styles.actions}>
          <button className={styles.cancelBtn} onClick={onClose}>Hủy</button>
          <button className={styles.saveBtn} onClick={onSubmit}>Lưu đánh giá</button>
        </div>
      </div>
    </div>
  );
}
