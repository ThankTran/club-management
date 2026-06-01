import { useState, useEffect } from 'react';
import styles from './EventForm.module.css';

const EMPTY_FORM = {
  eventCode: '',
  // Step 1 — Fundamentals
  title: '',
  date: '',
  time: '',
  endTime: '',
  location: '',
  estimatedCost: '',
  description: '',
  // Step 2 — Logistics
  capacity: '',
  organizer: '',
  tag: '',
  // Step 3 — Review (read-only)
};

const STEPS = ['Thông tin cơ bản', 'Chi tiết tổ chức', 'Xác nhận'];

/**
 * Props:
 *   open     – boolean
 *   onClose  – () => void
 *   onSubmit – (formData) => void
 *   initial  – event object (edit) | null (add)
 *   loading  – boolean
 */
export default function EventForm({ open, onClose, onSubmit, initial = null, loading = false, existingEvents = [], readOnly = false }) {
  const isEdit = !!initial;
  const [step, setStep]     = useState(0);
  const [form, setForm]     = useState(EMPTY_FORM);
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (open) {
      setStep(0);
      setForm(initial ? { ...EMPTY_FORM, ...initial } : EMPTY_FORM);
      setErrors({});
    }
  }, [open, initial]);

  useEffect(() => {
    const handler = (e) => { if (e.key === 'Escape') onClose(); };
    if (open) window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [open, onClose]);

  if (!open) return null;

  const handleChange = (field) => (e) => {
    if (readOnly) return;
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
    setErrors((prev) => ({ ...prev, [field]: '' }));
  };

  // ── Validation theo step ─────────────────────────────────
  const validateStep = (s) => {
    const errs = {};
    if (s === 0) {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const selectedDate = form.date ? new Date(form.date) : null;
      const duplicateCode = existingEvents.some((event) =>
        event.id !== initial?.id &&
        event.eventCode &&
        form.eventCode.trim() &&
        event.eventCode.toLowerCase() === form.eventCode.trim().toLowerCase()
      );
      const duplicateTitle = existingEvents.some((event) =>
        event.id !== initial?.id &&
        event.title.toLowerCase() === form.title.trim().toLowerCase()
      );

      if (!form.eventCode.trim()) errs.eventCode = 'Vui lòng nhập mã sự kiện';
      if (duplicateCode) errs.eventCode = 'Mã sự kiện đã tồn tại';
      if (duplicateTitle) errs.title = 'Tên sự kiện không được trùng';
      if (selectedDate && selectedDate < today) errs.date = 'Ngày tổ chức phải lớn hơn hoặc bằng ngày hiện tại';
      if (!form.time) errs.time = 'Vui lòng chọn thời gian bắt đầu';
      if (!form.endTime) errs.endTime = 'Vui lòng chọn thời gian kết thúc';
      if (form.time && form.endTime && form.endTime <= form.time) {
        errs.endTime = 'Thời gian kết thúc phải lớn hơn thời gian bắt đầu';
      }
      if (!form.title.trim())    errs.title    = 'Vui lòng nhập tên sự kiện';
      if (!form.date)            errs.date     = 'Vui lòng chọn ngày';
      if (!form.location.trim()) errs.location = 'Vui lòng nhập địa điểm';
    }
    if (s === 1) {
      if (!form.capacity) errs.capacity = 'Vui lòng nhập sức chứa';
      if (Number(form.capacity) <= 0) errs.capacity = 'Sức chứa phải lớn hơn 0';
      if (!form.organizer.trim()) errs.organizer = 'Vui lòng nhập ban tổ chức';
    }
    return errs;
  };

  const handleNext = () => {
    if (readOnly) {
      setStep((s) => s + 1);
      return;
    }
    const errs = validateStep(step);
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setStep((s) => s + 1);
  };

  const handleBack = () => setStep((s) => s - 1);

  const handleSubmit = () => {
    if (readOnly) return;
    onSubmit(form);
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>

        {/* ── Header ── */}
        <div className={styles.header}>
          <div>
            <h2 className={styles.title}>
              {readOnly ? 'Chi tiết sự kiện' : isEdit ? 'Chỉnh sửa sự kiện' : 'Tạo sự kiện mới'}
            </h2>
            <p className={styles.subtitle}>
              {readOnly ? 'Xem thông tin chi tiết sự kiện' : isEdit ? 'Cập nhật thông tin sự kiện' : 'Điền thông tin để tạo sự kiện'}
            </p>
          </div>
          <button className={styles.closeBtn} onClick={onClose}>
            <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <path d="M18 6L6 18M6 6l12 12"/>
            </svg>
          </button>
        </div>

        {/* ── Stepper ── */}
        <div className={styles.stepper}>
          {STEPS.map((label, i) => (
            <div key={i} className={styles.stepItem}>
              <div className={`${styles.stepCircle} ${i <= step ? styles.stepActive : ''} ${i < step ? styles.stepDone : ''}`}>
                {i < step
                  ? <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="3"><path d="M5 13l4 4L19 7"/></svg>
                  : i + 1
                }
              </div>
              <span className={`${styles.stepLabel} ${i <= step ? styles.stepLabelActive : ''}`}>
                {label}
              </span>
              {i < STEPS.length - 1 && (
                <div className={`${styles.stepLine} ${i < step ? styles.stepLineDone : ''}`} />
              )}
            </div>
          ))}
        </div>

        {/* ── Form content ── */}
        <div className={styles.body}>
          <fieldset className={styles.fieldset} disabled={readOnly}>

          {/* Step 1 — Thông tin cơ bản */}
          {step === 0 && (
            <div className={styles.fields}>
              <Field label="Mã sự kiện *" error={errors.eventCode}>
                <input
                  className={`${styles.input} ${errors.eventCode ? styles.inputError : ''}`}
                  type="text"
                  placeholder="VD: SK001"
                  value={form.eventCode}
                  onChange={handleChange('eventCode')}
                />
              </Field>

              <Field label="Tên sự kiện *" error={errors.title}>
                <input
                  className={`${styles.input} ${errors.title ? styles.inputError : ''}`}
                  type="text"
                  placeholder="VD: Hội thảo Khoa học 2024"
                  value={form.title}
                  onChange={handleChange('title')}
                />
              </Field>

              <div className={styles.row2}>
                <Field label="Ngày *" error={errors.date}>
                  <input
                    className={`${styles.input} ${errors.date ? styles.inputError : ''}`}
                    type="date"
                    value={form.date}
                    onChange={handleChange('date')}
                  />
                </Field>
                <Field label="Giờ bắt đầu *" error={errors.time}>
                  <input
                    className={`${styles.input} ${errors.time ? styles.inputError : ''}`}
                    type="time"
                    value={form.time}
                    onChange={handleChange('time')}
                  />
                </Field>
              </div>

              <Field label="Giờ kết thúc *" error={errors.endTime}>
                <input
                  className={`${styles.input} ${errors.endTime ? styles.inputError : ''}`}
                  type="time"
                  value={form.endTime}
                  onChange={handleChange('endTime')}
                />
              </Field>

              <Field label="Địa điểm *" error={errors.location}>
                <input
                  className={`${styles.input} ${errors.location ? styles.inputError : ''}`}
                  type="text"
                  placeholder="VD: Hội trường A, Tầng 2"
                  value={form.location}
                  onChange={handleChange('location')}
                />
              </Field>

              <Field label="Ngân sách dự kiến (₫)">
                <div className={styles.inputPrefix}>
                  <span className={styles.prefix}>₫</span>
                  <input
                    className={styles.inputWithPrefix}
                    type="number"
                    placeholder="5,000,000"
                    value={form.estimatedCost}
                    onChange={handleChange('estimatedCost')}
                  />
                </div>
              </Field>

              <Field label="Mô tả sự kiện">
                <textarea
                  className={styles.textarea}
                  rows={3}
                  placeholder="Mô tả ngắn về mục tiêu và nội dung sự kiện..."
                  value={form.description}
                  onChange={handleChange('description')}
                />
              </Field>
            </div>
          )}

          {/* Step 2 — Chi tiết tổ chức */}
          {step === 1 && (
            <div className={styles.fields}>
              <Field label="Sức chứa tối đa *" error={errors.capacity}>
                <input
                  className={`${styles.input} ${errors.capacity ? styles.inputError : ''}`}
                  type="number"
                  placeholder="VD: 200"
                  value={form.capacity}
                  onChange={handleChange('capacity')}
                />
              </Field>

              <Field label="Ban tổ chức / Người phụ trách *" error={errors.organizer}>
                <input
                  className={`${styles.input} ${errors.organizer ? styles.inputError : ''}`}
                  type="text"
                  placeholder="VD: Ban học thuật"
                  value={form.organizer}
                  onChange={handleChange('organizer')}
                />
              </Field>

              <Field label="Thẻ phân loại">
                <select
                  className={styles.select}
                  value={form.tag}
                  onChange={handleChange('tag')}
                >
                  <option value="">-- Chọn phân loại --</option>
                  <option value="TECH">Công nghệ</option>
                  <option value="ACAD">Học thuật</option>
                  <option value="CERT">Chứng chỉ</option>
                  <option value="SOCIAL">Giao lưu</option>
                  <option value="OTHER">Khác</option>
                </select>
              </Field>

            </div>
          )}

          {/* Step 3 — Xác nhận */}
          {step === 2 && (
            <div className={styles.review}>
              <p className={styles.reviewTitle}>Xác nhận thông tin sự kiện</p>

              <div className={styles.reviewGrid}>
                <ReviewRow label="Mã sự kiện"    value={form.eventCode} />
                <ReviewRow label="Tên sự kiện"   value={form.title}    />
                <ReviewRow label="Ngày"           value={form.date}     />
                <ReviewRow label="Giờ bắt đầu"    value={form.time || '—'} />
                <ReviewRow label="Giờ kết thúc"   value={form.endTime || '—'} />
                <ReviewRow label="Địa điểm"       value={form.location} />
                <ReviewRow
                  label="Ngân sách"
                  value={form.estimatedCost ? `${Number(form.estimatedCost).toLocaleString('vi-VN')}₫` : '—'}
                />
                <ReviewRow label="Sức chứa"       value={form.capacity ? `${form.capacity} người` : '—'} />
                <ReviewRow label="Ban tổ chức"    value={form.organizer || '—'} />
                <ReviewRow label="Phân loại"      value={form.tag || '—'} />
              </div>

              {form.description && (
                <div className={styles.reviewDesc}>
                  <p className={styles.reviewDescLabel}>Mô tả</p>
                  <p className={styles.reviewDescText}>{form.description}</p>
                </div>
              )}
            </div>
          )}
          </fieldset>
        </div>

        {/* ── Footer actions ── */}
        <div className={styles.footer}>
          {step > 0 && (
            <button className={styles.backBtn} onClick={handleBack}>
              ← Quay lại
            </button>
          )}
          <div style={{ flex: 1 }} />
          <button className={styles.cancelBtn} onClick={onClose}>{readOnly ? 'Đóng' : 'Hủy'}</button>

          {step < STEPS.length - 1 ? (
            <button className={styles.nextBtn} onClick={handleNext}>
              Tiếp theo →
            </button>
          ) : !readOnly && (
            <button className={styles.submitBtn} onClick={handleSubmit} disabled={loading}>
              {loading ? 'Đang lưu...' : isEdit ? 'Lưu thay đổi' : 'Tạo sự kiện'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

// ── Field wrapper ────────────────────────────────────────────
function Field({ label, error, children }) {
  const labelContent = typeof label === 'string' && label.trimEnd().endsWith('*')
    ? (
      <>
        {label.replace(/\s*\*$/, '')} <span className={styles.required}>*</span>
      </>
    )
    : label;

  return (
    <div className={styles.field}>
      <label className={styles.label}>{labelContent}</label>
      {children}
      {error && <p className={styles.errorMsg}>{error}</p>}
    </div>
  );
}

function ReviewRow({ label, value }) {
  return (
    <div className={styles.reviewRow}>
      <span className={styles.reviewLabel}>{label}</span>
      <span className={styles.reviewValue}>{value}</span>
    </div>
  );
}
