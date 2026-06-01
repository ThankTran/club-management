import { useEffect, useMemo, useState } from 'react';
import ResourceCard from '../../components/sections/Resource/ResourceCard';
import ResourceForm from '../../components/sections/Resource/ResourceForm';
import ResourceFolderView from '../../components/sections/Resource/ResourceFolderView';
import { normalizeResourceFolderId, RESOURCE_LEAF_FOLDERS } from '../../data/Resource/resourceFolderData';
import { INITIAL_MEMBER_SUBMISSIONS, MOCK_RESOURCES, PAGE_SIZE, FORMAT_OPTIONS, SOURCE_OPTIONS, TYPE_TABS } from '../../data/Resource/resourceUserData';
import {
  createResourceAPI,
  createResourceFileAPI,
  approveResourceAPI,
  getResourceTypesAPI,
  getResourcesAPI,
  normalizeResourceFromApi,
  toResourcePayload,
} from '../../services/resource-service';
import { getSubjectsAPI } from '../../services/subject-service';
import useAuthStore from '../../store/auth-store';
import styles from './ResourceUserPage.module.css';

export default function ResourceUserPage() {
  const currentUser = useAuthStore((state) => state.user);
  const [resources, setResources] = useState(MOCK_RESOURCES);
  const [resourceTypes, setResourceTypes] = useState([]);
  const [subjectOptions, setSubjectOptions] = useState([]);
  const [apiError, setApiError] = useState('');
  // Filters
  const [search, setSearch]       = useState('');
  const [activeType, setActiveType]     = useState('Tất cả');
  const [activeFormat, setActiveFormat] = useState('Tất cả');
  const [activeSource, setActiveSource] = useState('Tất cả');
  const [selectedCategory] = useState(null);
  const [selectedMajor, setSelectedMajor] = useState(null);
  const [selectedSubjectFolder, setSelectedSubjectFolder] = useState(null);
  const [selectedFolderId, setSelectedFolderId] = useState(null);

  // UI state
  const [viewMode, setViewMode]   = useState('list'); // 'list' | 'grid'
  const [page, setPage]           = useState(1);
  const [formOpen, setFormOpen]   = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [fixEditOpen, setFixEditOpen] = useState(false);
  const [fixEditTarget, setFixEditTarget] = useState(null);
  const [fixLoading, setFixLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [submissionsOpen, setSubmissionsOpen] = useState(false);
  const [memberSubmissions, setMemberSubmissions] = useState(INITIAL_MEMBER_SUBMISSIONS);

  const loadResources = () =>
    getResourcesAPI()
      .then((data) => {
        const nextResources = Array.isArray(data)
          ? data.map(normalizeResourceFromApi)
          : [];
        setResources(nextResources.length ? nextResources : MOCK_RESOURCES);
        if (currentUser?.memberId) {
          setMemberSubmissions(
            nextResources
              .filter((resource) => resource.memberId === currentUser.memberId)
              .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)),
          );
        }
        setApiError('');
      })
      .catch((error) => {
        setResources(MOCK_RESOURCES);
        setApiError(error?.message || 'Không tải được kho tài liệu từ API.');
      });

  useEffect(() => {
    let ignore = false;

    Promise.allSettled([getResourcesAPI(), getResourceTypesAPI(), getSubjectsAPI()])
      .then(([resourcesResult, typesResult, subjectsResult]) => {
        if (ignore) return;

        if (resourcesResult.status === 'fulfilled') {
          const nextResources = Array.isArray(resourcesResult.value)
            ? resourcesResult.value.map(normalizeResourceFromApi)
            : [];
          setResources(nextResources.length ? nextResources : MOCK_RESOURCES);
          if (currentUser?.memberId) {
            setMemberSubmissions(
              nextResources
                .filter((resource) => resource.memberId === currentUser.memberId)
                .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)),
            );
          }
          setApiError('');
        } else {
          setResources(MOCK_RESOURCES);
          setApiError(resourcesResult.reason?.message || 'Không tải được kho tài liệu từ API.');
        }

        if (typesResult.status === 'fulfilled' && Array.isArray(typesResult.value)) {
          setResourceTypes(typesResult.value);
        }
        if (subjectsResult.status === 'fulfilled' && Array.isArray(subjectsResult.value)) {
          setSubjectOptions(subjectsResult.value);
        }
      });

    return () => {
      ignore = true;
    };
  }, [currentUser?.memberId]);

  const selectedFolder = RESOURCE_LEAF_FOLDERS.find((folder) => folder.id === selectedFolderId);

  const approvedResources = useMemo(
    () => resources.filter(isApprovedResource),
    [resources],
  );

  const scopedResources = useMemo(() => {
    if (!selectedFolderId) {
      return approvedResources;
    }

    return approvedResources.filter((resource) => resolveUserFolderId(resource) === selectedFolderId);
  }, [approvedResources, selectedFolderId]);

  const folderCounts = useMemo(() => {
    const counts = {};

    approvedResources.forEach((resource) => {
      const folderId = resolveUserFolderId(resource);
      counts[folderId] = (counts[folderId] || 0) + 1;
    });

    return counts;
  }, [approvedResources]);

  const filtered = useMemo(() => {
    const q = search.toLowerCase().trim();

    return scopedResources
      .filter((r) => {
        const matchSearch =
          !q ||
          String(r.title || '').toLowerCase().includes(q) ||
          String(r.subject || '').toLowerCase().includes(q) ||
          String(r.type || '').toLowerCase().includes(q) ||
          String(r.format || '').toLowerCase().includes(q);

        const matchType = activeType === "Tất cả" || r.type === activeType;
        const matchFormat = activeFormat === "Tất cả" || r.format === activeFormat;
        const matchSource = activeSource === "Tất cả" || r.source === activeSource;
        return matchSearch && matchType && matchFormat && matchSource;
      })
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  }, [
    scopedResources,
    search,
    activeType,
    activeFormat,
    activeSource,
  ]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const paginated  = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  // Stats
  const totalApproved = approvedResources.length;
  const totalSubjects = new Set(approvedResources.map((r) => r.subject).filter(Boolean)).size;

  const hasFilter = activeType !== 'Tất cả' || activeFormat !== 'Tất cả' || activeSource !== 'Tất cả';

  const resetFilters = () => {
    setActiveType('Tất cả');
    setActiveFormat('Tất cả');
    setActiveSource('Tất cả');
    setSearch('');
    setPage(1);
  };

  const handleSubmitForm = async (formData) => {
    if (!currentUser?.memberId) {
      setApiError('Không xác định được tài khoản đang đăng nhập.');
      return;
    }

    setFormLoading(true);
    try {
      const created = await createResourceAPI(toResourcePayload({
        ...formData,
        proposedById: currentUser.memberId,
      }));

      try {
        const filePayload = new FormData();
        filePayload.append('documentId', created.documentId);
        filePayload.append('file', formData.file);
        await createResourceFileAPI(filePayload);
      } catch (uploadError) {
        await loadResources();
        setApiError(uploadError?.message || 'Đã tạo phiếu nhưng tải tệp thất bại.');
        return;
      }

      await loadResources();
      setFormOpen(false);
      setSubmitted(true);
      setTimeout(() => setSubmitted(false), 5000);
    } catch (error) {
      setApiError(error?.message || error || 'Không gửi được đề xuất tài liệu.');
    } finally {
      setFormLoading(false);
    }
  };

  const openFixRequest = (resource) => {
    if (!resource || resource.workflowStatus === 'fixing') {
      return;
    }

    setFixEditTarget(resource);
    setFixEditOpen(true);
  };

  const submitFixEdit = async (formData) => {
    if (!fixEditTarget) return;

    if (!currentUser?.memberId) {
      setApiError('Không xác định được tài khoản đang đăng nhập.');
      return;
    }

    if (!String(formData?.description || '').trim()) {
      setApiError('Vui lòng nhập nội dung cần chỉnh sửa (mô tả).');
      return;
    }

    setFixLoading(true);
    try {
      await approveResourceAPI({
        documentId: fixEditTarget.id,
        approvedBy: currentUser.memberId,
        status: 'REQUESTED_CHANGES',
        note: String(formData.description || '').trim(),
      });
      await loadResources();
      setFixEditOpen(false);
      setFixEditTarget(null);
      setApiError('');
    } catch (error) {
      setApiError(error?.message || 'Không gửi được đề xuất sửa tài liệu.');
    } finally {
      setFixLoading(false);
    }
  };

  const handleFilter = (setter) => (val) => {
    setter(val);
    setPage(1);
  };

  return (
    <div className={styles.page}>
      {apiError && <div className={styles.toast}>{apiError}</div>}

      {/* ══ HERO BANNER ══ */}
      <div className={styles.hero}>
        <div className={styles.heroContent}>
          <div className={styles.heroLeft}>
            <div className={styles.heroBadge}>📚 Kho học liệu</div>
            <h1 className={styles.heroTitle}>
              Tài liệu học thuật<br/>
              <span className={styles.heroAccent}>được kiểm duyệt</span>
            </h1>
            <p className={styles.heroSub}>
              Giáo trình, slide và tài liệu tham khảo từ giảng viên &amp; cộng đồng — miễn phí, đã xét duyệt.
            </p>
            <div className={styles.heroActions}>
              <button className={styles.proposeBtn} onClick={() => setFormOpen(true)}>
                <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
                  <path d="M12 5v14M5 12h14"/>
                </svg>
                Đề xuất tài liệu
              </button>
              <button
                className={styles.proposeBtn}
                onClick={() => setSubmissionsOpen(true)}
              >
                <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
                  <path d="M9 11l3 3L22 4"/>
                  <path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11"/>
                </svg>
                Tài liệu đã đề xuất
              </button>
            </div>
          </div>

          {/* Stats pills */}
          <div className={styles.heroStats}>
            <StatPill icon="📄" value={totalApproved} label="Tài liệu" />
            <StatPill icon="📖" value={totalSubjects} label="Môn học" />
            <StatPill icon="✅" value="100%" label="Đã duyệt" />
          </div>
        </div>
      </div>

      {/* ══ TOAST ══ */}
      {submitted && (
        <div className={styles.toast}>
          <svg width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
            <path d="M5 13l4 4L19 7"/>
          </svg>
          Đề xuất đã gửi thành công! Admin sẽ xét duyệt trong 1–3 ngày làm việc.
        </div>
      )}

      {/* ══ MAIN LAYOUT: sidebar + content ══ */}
      <div className={`${styles.layout} ${!selectedFolderId ? styles.folderOnlyLayout : ''}`}>

        {/* ── SIDEBAR ── */}
        {selectedFolderId && (
        <aside className={styles.sidebar}>
          <div className={styles.sidebarInner}>

            {/* Search */}
            <div className={styles.searchBox}>
              <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
              </svg>
              <input
                className={styles.searchInput}
                placeholder="Tìm tài liệu..."
                value={search}
                onChange={(e) => { setSearch(e.target.value); setPage(1); }}
              />
              {search && (
                <button className={styles.clearBtn} onClick={() => { setSearch(''); setPage(1); }}>×</button>
              )}
            </div>

            {/* Loại tài liệu */}
            <FilterSection title="Loại tài liệu">
              {TYPE_TABS.map((t) => (
                <FilterOption
                  key={t}
                  label={t}
                  count={t === 'Tất cả' ? scopedResources.length : scopedResources.filter((r) => r.type === t).length}
                  active={activeType === t}
                  onClick={() => handleFilter(setActiveType)(t)}
                />
              ))}
            </FilterSection>

            {/* Định dạng */}
            <FilterSection title="Định dạng">
              {FORMAT_OPTIONS.map((f) => (
                <FilterOption
                  key={f}
                  label={f}
                  count={f === 'Tất cả' ? scopedResources.length : scopedResources.filter((r) => r.format === f).length}
                  active={activeFormat === f}
                  onClick={() => handleFilter(setActiveFormat)(f)}
                />
              ))}
            </FilterSection>

            {/* Nguồn */}
            <FilterSection title="Nguồn tài liệu">
              {SOURCE_OPTIONS.map((s) => (
                <FilterOption
                  key={s}
                  label={s}
                  count={s === 'Tất cả' ? scopedResources.length : scopedResources.filter((r) => r.source === s).length}
                  active={activeSource === s}
                  onClick={() => handleFilter(setActiveSource)(s)}
                />
              ))}
            </FilterSection>

            {/* Reset */}
            {hasFilter && (
              <button className={styles.resetBtn} onClick={resetFilters}>
                × Xóa tất cả bộ lọc
              </button>
            )}
          </div>
        </aside>
        )}

        {/* ── CONTENT ── */}
        <main className={styles.content}>
          {selectedFolder && (
                        <div className={styles.breadcrumbBar}>
              <button
                className={styles.backBtn}
                onClick={() => {
                  setSelectedFolderId(null);
                  resetFilters();
                }}
              >
                ← Quay lại
              </button>

              <div className={styles.breadcrumbs}>
                <button
                  className={styles.breadcrumbLink}
                  onClick={() => {
                    setSelectedFolderId(null);
                    resetFilters();
                  }}
                >
                  Kho tài liệu
                </button>

                {selectedFolder && (
                  <>
                    <span className={styles.breadcrumbSep}>/</span>
                    <button className={`${styles.breadcrumbLink} ${styles.breadcrumbCurrent}`}>
                      {selectedFolder.pathLabel}
                    </button>
                  </>
                )}

                {selectedCategory && (
                  <>
                    <span className={styles.breadcrumbSep}>/</span>

                    <button
                      className={styles.breadcrumbLink}
                      onClick={() => {
                        setSelectedMajor(null);
                        setSelectedSubjectFolder(null);
                      }}
                    >
                      {selectedCategory === "general"
                        ? "Môn đại cương"
                        : "Môn theo ngành"}
                    </button>
                  </>
                )}

                {selectedMajor && (
                  <>
                    <span className={styles.breadcrumbSep}>/</span>

                    <button
                      className={`${styles.breadcrumbLink} ${styles.breadcrumbCurrent}`}
                    >
                      {selectedMajor}
                    </button>
                  </>
                )}

                {selectedSubjectFolder && (
                  <>
                    <span className={styles.breadcrumbSep}>/</span>

                    <button
                      className={`${styles.breadcrumbLink} ${styles.breadcrumbCurrent}`}
                    >
                      {selectedSubjectFolder}
                    </button>
                  </>
                )}
              </div>
            </div>
          )}
          {/* Folder view (chỉ hiện khi chưa chọn category hoặc major/subject) */}
          {!selectedFolderId && (
            <ResourceFolderView
              selectedFolderId={selectedFolderId}
              folderCounts={folderCounts}
              onSelectFolder={(folderId) => {
                setSelectedFolderId(folderId);
                setPage(1);
              }}
            />
          )}

          {selectedFolderId && (
          <>
            {/* Toolbar: kết quả + view toggle */}
            <div className={styles.toolbar}>
              <div className={styles.toolbarLeft}>
                <span className={styles.resultCount}>
                  <strong>{filtered.length}</strong> tài liệu
                  {hasFilter && <span className={styles.resultFiltered}> (đang lọc)</span>}
                </span>
              </div>
              <div className={styles.viewToggle}>
                <button
                  className={`${styles.viewBtn} ${viewMode === 'list' ? styles.viewBtnActive : ''}`}
                  onClick={() => setViewMode('list')}
                  title="Xem dạng danh sách"
                >
                  <svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <path d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01"/>
                  </svg>
                </button>
                <button
                  className={`${styles.viewBtn} ${viewMode === 'grid' ? styles.viewBtnActive : ''}`}
                  onClick={() => setViewMode('grid')}
                  title="Xem dạng lưới"
                >
                  <svg width="15" height="15" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
                    <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
                  </svg>
                </button>
              </div>
            </div>

            {/* Active filter chips */}
            {hasFilter && (
              <div className={styles.chips}>
                {activeType !== 'Tất cả' && (
                  <Chip label={activeType} onRemove={() => handleFilter(setActiveType)('Tất cả')} />
                )}
                {activeFormat !== 'Tất cả' && (
                  <Chip label={activeFormat} onRemove={() => handleFilter(setActiveFormat)('Tất cả')} />
                )}
                {activeSource !== 'Tất cả' && (
                  <Chip label={activeSource} onRemove={() => handleFilter(setActiveSource)('Tất cả')} />
                )}
              </div>
            )}

            {/* Card list / grid */}
            {paginated.length === 0 ? (
              <div className={styles.empty}>
                <div className={styles.emptyIcon}>🔍</div>
                <p className={styles.emptyTitle}>Không tìm thấy tài liệu phù hợp</p>
                <p className={styles.emptySub}>Thử thay đổi từ khóa hoặc bỏ bớt bộ lọc</p>
                <button className={styles.emptyReset} onClick={resetFilters}>Xóa bộ lọc</button>
              </div>
            ) : (
              <div className={viewMode === 'grid' ? styles.gridList : styles.cardList}>
                {paginated.map((r) => (
                  <ResourceCard
                    key={r.id}
                    resource={r}
                    viewMode={viewMode}
                    onRequestFix={openFixRequest}
                  />
                ))}
              </div>
            )}

            {/* Pagination */}
            {totalPages > 1 && (
              <div className={styles.pagination}>
                <button
                  className={styles.pageBtn}
                  disabled={page === 1}
                  onClick={() => setPage((p) => p - 1)}
                >
                  ‹ Trước
                </button>

                <div className={styles.pageDots}>
                  {Array.from({ length: totalPages }, (_, i) => i + 1).map((p) => (
                    <button
                      key={p}
                      className={`${styles.pageDot} ${p === page ? styles.pageDotActive : ''}`}
                      onClick={() => setPage(p)}
                    >
                      {p}
                    </button>
                  ))}
                </div>

                <button
                  className={styles.pageBtn}
                  disabled={page === totalPages}
                  onClick={() => setPage((p) => p + 1)}
                >
                  Sau ›
                </button>
              </div>)}
            </>
          )}
          
          
        </main>
      </div>

      {/* ══ MODALS ══ */}
      <ResourceForm
        open={formOpen}
        onClose={() => setFormOpen(false)}
        onSubmit={handleSubmitForm}
        loading={formLoading}
        isAdmin={false}
        resourceTypes={resourceTypes}
        subjectOptions={subjectOptions}
      />

      <ResourceForm
        open={fixEditOpen}
        initial={fixEditTarget
          ? {
            title: fixEditTarget.title,
            typeId: fixEditTarget.typeId,
            type: fixEditTarget.type,
            subjectId: fixEditTarget.subjectId,
            subject: fixEditTarget.subject,
            source: fixEditTarget.source,
            description: fixEditTarget.description || fixEditTarget.note || '',
          }
          : null}
        onClose={() => {
          if (fixLoading) return;
          setFixEditOpen(false);
          setFixEditTarget(null);
        }}
        onSubmit={submitFixEdit}
        loading={fixLoading}
        isAdmin={false}
        resourceTypes={resourceTypes}
        subjectOptions={subjectOptions}
      />

      <SubmissionHistoryModal
        open={submissionsOpen}
        submissions={memberSubmissions}
        onClose={() => setSubmissionsOpen(false)}
      />
    </div>
  );
}

/* ── Sub-components ── */
function resolveUserFolderId(resource) {
  if (resource.lookupFolderId) return normalizeResourceFolderId(resource.lookupFolderId);

  const text = `${resource.major || ''} ${resource.subject || ''} ${resource.title || ''}`.toLowerCase();
  const directMatch = RESOURCE_LEAF_FOLDERS.find((folder) => text.includes(folder.label.toLowerCase()));
  if (directMatch) return directMatch.id;

  const rules = [
    ['pháp luật', 'phap-luat-dai-cuong'],
    ['triết học', 'triet-hoc-mac-lenin'],
    ['giải tích', 'giai-tich'],
    ['đại số', 'dai-so-tuyen-tinh'],
    ['xác suất', 'xac-suat-thong-ke'],
    ['kỹ thuật phần mềm', 'ky-thuat-phan-mem'],
    ['lập trình hướng đối tượng', 'ky-thuat-phan-mem'],
    ['công nghệ phần mềm', 'ky-thuat-phan-mem'],
    ['truyền thông đa phương tiện', 'truyen-thong-da-phuong-tien'],
    ['cơ sở dữ liệu', 'he-thong-thong-tin-chuyen-nganh'],
    ['hệ thống thông tin', 'he-thong-thong-tin-chuyen-nganh'],
    ['trí tuệ nhân tạo', 'tri-tue-nhan-tao'],
    ['hệ điều hành', 'khoa-hoc-may-tinh-chuyen-nganh'],
    ['khoa học máy tính', 'khoa-hoc-may-tinh-chuyen-nganh'],
    ['khoa học dữ liệu', 'khoa-hoc-du-lieu'],
    ['công nghệ thông tin', 'cong-nghe-thong-tin'],
    ['kiến trúc máy tính', 'ky-thuat-may-tinh-chuyen-nganh'],
    ['kỹ thuật máy tính', 'ky-thuat-may-tinh-chuyen-nganh'],
    ['thiết kế vi mạch', 'thiet-ke-vi-mach'],
    ['mạng máy tính', 'mang-may-tinh-truyen-thong-du-lieu'],
    ['an toàn thông tin', 'an-toan-thong-tin'],
    ['thương mại điện tử', 'thuong-mai-dien-tu'],
    ['web', 'ky-thuat-phan-mem'],
    ['ui/ux', 'thuong-mai-dien-tu'],
  ];

  return rules.find(([keyword]) => text.includes(keyword))?.[1] || 'nhap-mon-lap-trinh';
}

function isApprovedResource(resource) {
  return Boolean(resource.approvedAt || resource.raw?.approvedAt) || String(resource.status || '').toLowerCase() === 'approved';
}

function StatPill({ icon, value, label }) {
  return (
    <div className={styles.statPill}>
      <span className={styles.statIcon}>{icon}</span>
      <span className={styles.statValue}>{value}</span>
      <span className={styles.statLabel}>{label}</span>
    </div>
  );
}

function SubmissionHistoryModal({ open, submissions, onClose }) {
  if (!open) return null;

  const pendingCount = submissions.filter((item) => item.status === 'pending' || item.status === 'fixing').length;

  return (
    <div className={styles.historyOverlay} onClick={onClose}>
      <section className={styles.historyModal} onClick={(event) => event.stopPropagation()}>
        <div className={styles.historyHeader}>
          <div>
            <p className={styles.historyEyebrow}>Theo dõi phiếu của tôi</p>
            <h2 className={styles.historyTitle}>Lịch sử đề xuất tài liệu</h2>
          </div>
          <div className={styles.historyHeaderActions}>
            <span className={styles.historySummary}>
              {submissions.length} phiếu · {pendingCount} chờ duyệt/chờ sửa
            </span>
            <button type="button" className={styles.historyCloseBtn} onClick={onClose}>
              ×
            </button>
          </div>
        </div>

        {submissions.length === 0 ? (
          <div className={styles.historyEmpty}>
            Bạn chưa gửi đề xuất tài liệu nào.
          </div>
        ) : (
          <div className={styles.historyList}>
            {submissions.map((item) => {
              const status = SUBMISSION_STATUS[item.status] || SUBMISSION_STATUS.pending;
              return (
                <article key={item.id} className={styles.historyItem}>
                  <div className={styles.historyMain}>
                    <div className={styles.historyTopline}>
                      <span className={styles.historyCode}>{item.id}</span>
                      <span className={styles.statusBadge} style={{ background: status.bg, color: status.color }}>
                        {status.label}
                      </span>
                    </div>
                    <h3 className={styles.historyItemTitle}>{item.title}</h3>
                    <p className={styles.historyMeta}>
                      {item.subject} · {item.type} · {item.format}
                    </p>
                    <p className={styles.historyNote}>{item.note}</p>
                  </div>
                  <div className={styles.historyDates}>
                    <span>Gửi: {formatHistoryDate(item.createdAt)}</span>
                    <span>Duyệt: {item.reviewedAt ? formatHistoryDate(item.reviewedAt) : 'Chưa có'}</span>
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </section>
    </div>
  );
}

function ResourceFixRequestModal({ open, resource, reason, loading, onClose, onReasonChange, onSubmit }) {
  if (!open || !resource) return null;

  return (
    <div className={styles.historyOverlay} onClick={onClose}>
      <section className={styles.historyModal} onClick={(event) => event.stopPropagation()}>
        <div className={styles.historyHeader}>
          <div>
            <p className={styles.historyEyebrow}>Đề xuất sửa tài liệu</p>
            <h2 className={styles.historyTitle}>{resource.title}</h2>
          </div>
          <button type="button" className={styles.historyCloseBtn} onClick={onClose}>
            ×
          </button>
        </div>

        <div className={styles.historyList}>
          <article className={styles.historyItem}>
            <div className={styles.historyMain}>
              <p className={styles.historyMeta}>
                {resource.subject} · {resource.type} · {resource.format}
              </p>
              <p className={styles.historyNote}>
                Tài liệu sẽ chuyển sang trạng thái chờ duyệt sửa cho admin kiểm tra.
              </p>
              <textarea
                className={styles.searchInput}
                style={{ width: '100%', minHeight: '120px', paddingTop: '12px', paddingBottom: '12px' }}
                rows={4}
                value={reason}
                onChange={(event) => onReasonChange(event.target.value)}
                placeholder="Nhập lý do hoặc mô tả phần cần chỉnh sửa..."
              />
            </div>
          </article>
        </div>

        <div className={styles.historyHeaderActions}>
          <button type="button" className={styles.historyCloseBtn} onClick={onClose}>
            Hủy
          </button>
          <button type="button" className={styles.proposeBtn} onClick={onSubmit} disabled={loading}>
            {loading ? 'Đang gửi...' : 'Gửi đề xuất sửa'}
          </button>
        </div>
      </section>
    </div>
  );
}

const SUBMISSION_STATUS = {
  pending: { label: 'Chờ duyệt', bg: '#fef3c7', color: '#92400e' },
  fixing: { label: 'Chờ duyệt sửa', bg: '#e0f2fe', color: '#0369a1' },
  approved: { label: 'Đã duyệt', bg: '#dcfce7', color: '#15803d' },
  rejected: { label: 'Từ chối', bg: '#fee2e2', color: '#b91c1c' },
};

function formatHistoryDate(date) {
  if (!date) return 'Chưa có';
  return new Date(date).toLocaleDateString('vi-VN');
}

function FilterSection({ title, children }) {
  return (
    <div className={styles.filterSection}>
      <p className={styles.filterTitle}>{title}</p>
      <div className={styles.filterOptions}>{children}</div>
    </div>
  );
}

function FilterOption({ label, count, active, onClick, small = false }) {
  return (
    <button
      className={`${styles.filterOpt} ${active ? styles.filterOptActive : ''} ${small ? styles.filterOptSmall : ''}`}
      onClick={onClick}
    >
      <span className={styles.filterOptLabel}>{label}</span>
      <span className={styles.filterOptCount}>{count}</span>
    </button>
  );
}

function Chip({ label, onRemove }) {
  return (
    <span className={styles.chip}>
      {label}
      <button className={styles.chipRemove} onClick={onRemove}>✕</button>
    </span>
  );
}
