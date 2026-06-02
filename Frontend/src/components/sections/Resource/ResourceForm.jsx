import { useState, useEffect } from 'react';
import styles from './ResourceForm.module.css';

const SOURCE_OPTIONS = ['Tự biên soạn', 'Internet', 'Giảng viên cung cấp', 'Sinh viên khóa trước', 'Khác'];

const EMPTY_FORM = {
  title: '',
  typeId: '',
  type: '',
  subjectId: '',
  subject: '',
  source: '',
  description: '',
  attachmentMode: 'file',
  fileUrl: '',
  file: null,
};

export default function ResourceForm({
  open,
  onClose,
  onSubmit,
  initial = null,
  loading = false,
  isAdmin = false,
  resourceTypes = [],
  subjectOptions = [],
}) {
  const isEdit = !!initial;

  const [form, setForm] = useState(EMPTY_FORM);
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (open) {
      const initialFileUrl = initial?.raw?.primaryFileUrl
        || initial?.raw?.fileUrl
        || initial?.raw?.files?.[0]?.fileUrl
        || initial?.link
        || '';
      const isLinkAttachment = /^https?:\/\//i.test(initialFileUrl) && !initialFileUrl.includes('/uploads/');
      setForm(initial
        ? {
          ...EMPTY_FORM,
          ...initial,
          attachmentMode: isLinkAttachment ? 'link' : 'file',
          fileUrl: isLinkAttachment ? initialFileUrl : '',
          file: null,
        }
        : EMPTY_FORM);
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
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
    setErrors((prev) => ({ ...prev, [field]: '' }));
  };

  const handleTypeChange = (e) => {
    const typeId = e.target.value;
    const selectedType = resourceTypes.find((type) => String(type.typeId) === typeId);
    setForm((prev) => ({
      ...prev,
      typeId,
      type: selectedType?.typeName || '',
    }));
    setErrors((prev) => ({ ...prev, typeId: '' }));
  };

  const handleSubjectChange = (e) => {
    const subjectId = e.target.value;
    const selectedSubject = subjectOptions.find((subject) => String(subject.subjectId) === subjectId);
    setForm((prev) => ({
      ...prev,
      subjectId,
      subject: selectedSubject?.subjectName || '',
    }));
    setErrors((prev) => ({ ...prev, subjectId: '' }));
  };

  const handleFileChange = (e) => {
    setForm((prev) => ({
      ...prev,
      file: e.target.files?.[0] || null,
      fileUrl: '',
    }));
    setErrors((prev) => ({ ...prev, file: '' }));
  };

  const handleAttachmentModeChange = (mode) => {
    setForm((prev) => ({
      ...prev,
      attachmentMode: mode,
      file: mode === 'file' ? prev.file : null,
      fileUrl: mode === 'link' ? prev.fileUrl : '',
    }));
    setErrors((prev) => ({ ...prev, file: '', fileUrl: '' }));
  };

  const validate = () => {
    const errs = {};
    if (!form.title.trim()) errs.title = 'Vui lòng nhập tên tài liệu';
    if (!form.typeId) errs.typeId = 'Vui lòng chọn loại tài liệu';
    if (!form.subjectId) errs.subjectId = 'Vui lòng chọn môn học / chủ đề';
    const hasFile = !!form.file;
    const hasLink = !!form.fileUrl.trim();
    const hasExistingAttachment = Boolean(
      initial?.raw?.primaryFileUrl
      || initial?.raw?.fileUrl
      || initial?.raw?.files?.[0]?.fileUrl
      || initial?.link
    );

    if (!isEdit && !hasFile && !hasLink) {
      if (form.attachmentMode === 'link') errs.fileUrl = 'Vui lòng nhập link tài liệu';
      else errs.file = 'Vui lòng chọn tệp tài liệu';
    }

    if (hasFile && hasLink) {
      errs.file = 'Chỉ được chọn 1 trong 2: upload file hoặc dán link';
      errs.fileUrl = 'Chỉ được chọn 1 trong 2: upload file hoặc dán link';
    }

    if (hasLink && !/^https?:\/\//i.test(form.fileUrl.trim())) {
      errs.fileUrl = 'Link tài liệu phải bắt đầu bằng http:// hoặc https://';
    }

    if (isEdit && !hasFile && !hasLink && !hasExistingAttachment) {
      errs.file = 'Vui lòng chọn tệp tài liệu hoặc dán link tài liệu';
    }
    return errs;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    onSubmit(form);
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <div>
            <h2 className={styles.title}>
              {isEdit ? 'Chỉnh sửa tài liệu' : isAdmin ? 'Thêm tài liệu mới' : 'Đề xuất thêm tài liệu'}
            </h2>
            <p className={styles.subtitle}>
              {isEdit
                ? 'Cập nhật thông tin tài liệu trong hệ thống'
                : isAdmin
                  ? 'Tạo phiếu tài liệu và tải tệp lên hệ thống'
                  : 'Tài liệu sẽ được admin xét duyệt trước khi hiển thị'}
            </p>
          </div>
          <button className={styles.closeBtn} onClick={onClose} type="button" aria-label="Đóng">
            <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <path d="M18 6L6 18M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <div className={styles.formMeta}>
          <span className={styles.formMetaTag}>Phiếu thêm tài liệu học thuật</span>
          <span className={styles.formMetaDate}>
            Ngày lập: {new Date().toLocaleDateString('vi-VN')}
          </span>
        </div>

        <form className={styles.form} onSubmit={handleSubmit} noValidate>
          <p className={styles.sectionLabel}>I. Thông tin tài liệu</p>

          <Field label="Tên tài liệu *" error={errors.title}>
            <input
              className={`${styles.input} ${errors.title ? styles.inputError : ''}`}
              type="text"
              placeholder="VD: Giáo trình Lập trình hướng đối tượng"
              value={form.title}
              onChange={handleChange('title')}
            />
          </Field>

          <div className={styles.row2}>
            <Field label="Loại tài liệu *" error={errors.typeId}>
              <select
                className={`${styles.input} ${errors.typeId ? styles.inputError : ''}`}
                value={form.typeId || ''}
                onChange={handleTypeChange}
              >
                <option value="">Chọn loại tài liệu</option>
                {resourceTypes.map((type) => (
                  <option key={type.typeId} value={type.typeId}>
                    {type.typeName}
                  </option>
                ))}
              </select>
            </Field>

            <Field label="Môn học / Chủ đề *" error={errors.subjectId}>
              <select
                className={`${styles.input} ${errors.subjectId ? styles.inputError : ''}`}
                value={form.subjectId || ''}
                onChange={handleSubjectChange}
              >
                <option value="">Chọn môn học</option>
                {subjectOptions.map((subject) => (
                  <option key={subject.subjectId} value={subject.subjectId}>
                    {subject.subjectName}
                  </option>
                ))}
              </select>
            </Field>
          </div>

          <Field label="Nguồn tài liệu">
            <div className={styles.radioGroup}>
              {SOURCE_OPTIONS.map((source) => (
                <label
                  key={source}
                  className={`${styles.radioBtn} ${form.source === source ? styles.radioBtnActive : ''}`}
                >
                  <input
                    type="radio"
                    name="source"
                    value={source}
                    checked={form.source === source}
                    onChange={handleChange('source')}
                    className={styles.radioHidden}
                  />
                  {source}
                </label>
              ))}
            </div>
          </Field>

          <Field label="Mô tả ngắn nội dung tài liệu">
            <textarea
              className={styles.textarea}
              rows={3}
              placeholder="Mô tả ngắn về nội dung và phạm vi của tài liệu..."
              value={form.description}
              onChange={handleChange('description')}
            />
          </Field>

          <p className={styles.sectionLabel}>II. Tệp đính kèm</p>

          <Field label="Chọn kiểu đính kèm">
            <div className={styles.segmented}>
              <button
                type="button"
                className={`${styles.segmentBtn} ${form.attachmentMode === 'file' ? styles.segmentBtnActive : ''}`}
                onClick={() => handleAttachmentModeChange('file')}
              >
                Upload file
              </button>
              <button
                type="button"
                className={`${styles.segmentBtn} ${form.attachmentMode === 'link' ? styles.segmentBtnActive : ''}`}
                onClick={() => handleAttachmentModeChange('link')}
              >
                Dán link
              </button>
            </div>
          </Field>

          {form.attachmentMode === 'file' ? (
            <Field label={isEdit ? 'Tệp tài liệu' : 'Tệp tài liệu *'} error={errors.file}>
              <input
                className={`${styles.input} ${errors.file ? styles.inputError : ''}`}
                type="file"
                onChange={handleFileChange}
              />
              {form.file && <p className={styles.helperText}>{form.file.name}</p>}
              {isEdit && !form.file && initial?.fileName && (
                <p className={styles.helperText}>Tệp hiện tại: {initial.fileName}</p>
              )}
              <p className={styles.helperText}>Tải tệp trực tiếp lên hệ thống. Tài liệu sẽ được mở từ file đính kèm.</p>
            </Field>
          ) : (
            <Field label={isEdit ? 'Link tài liệu' : 'Link tài liệu *'} error={errors.fileUrl}>
              <input
                className={`${styles.input} ${errors.fileUrl ? styles.inputError : ''}`}
                type="url"
                placeholder="https://example.com/document.pdf"
                value={form.fileUrl}
                onChange={handleChange('fileUrl')}
              />
              <p className={styles.helperText}>Dán link công khai. Hệ thống sẽ mở tài liệu theo đường dẫn này.</p>
            </Field>
          )}

          <div className={styles.actions}>
            <button type="button" className={styles.cancelBtn} onClick={onClose}>
              Hủy
            </button>
            <button type="submit" className={styles.submitBtn} disabled={loading}>
              {loading
                ? 'Đang lưu...'
                : isEdit
                  ? 'Lưu thay đổi'
                  : isAdmin ? 'Thêm tài liệu' : 'Gửi đề xuất'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

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
