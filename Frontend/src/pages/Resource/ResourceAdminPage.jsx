import { useEffect, useMemo, useState } from 'react';
import ResourceForm from '../../components/sections/Resource/ResourceForm';
import ResourceTable from '../../components/sections/Resource/ResourceTable';
import ResourceFilter from '../../components/sections/Resource/ResourceFilter';
import ResourceAdminHeader from '../../components/sections/Resource/ResourceAdminHeader';
import ResourceStats from '../../components/sections/Resource/ResourceStats';
import ResourceAdminTabs from '../../components/sections/Resource/ResourceAdminTabs';
import ResourceReviewModal from '../../components/sections/Resource/ResourceReviewModal';
import ResourceLookupTable from '../../components/sections/Resource/ResourceLookupTable';
import ResourceDeleteConfirmModal from '../../components/sections/Resource/ResourceDeleteConfirmModal';
import ResourceHistoryModal from '../../components/sections/Resource/ResourceHistoryModal';
import ResourceApproveModal from '../../components/sections/Resource/ResourceApproveModal';
import ResourceRejectModal from '../../components/sections/Resource/ResourceRejectModal';
import ActionToast from '../../components/common/ActionToast/ActionToast';
import {
  RESOURCE_RULES,
} from '../../data/Resource/resourceAdminData';
import {
  approveResourceAPI,
  buildResourceFilePayload,
  createResourceAPI,
  createResourceFileAPI,
  getResourceTypesAPI,
  getResourcesAPI,
  hydrateResourcesWithFiles,
  normalizeResourceFromApi,
  normalizeUploadedResourceFile,
  softDeleteResourceAPI,
  toResourcePayload,
} from '../../services/resource-service';
import { getSubjectsAPI } from '../../services/subject-service';
import useAuthStore from '../../store/auth-store';
import styles from './ResourceAdminPage.module.css';
import useActionToast from '../../hooks/useActionToast';
import {
    getMembersAPI,
    normalizeMemberFromApi,
} from '../../services/member-service';
export default function ResourceAdminPage() {
  const currentUser = useAuthStore((state) => state.user);
  const [resources, setResources] = useState([]);
  const [apiError, setApiError] = useState('');
  const [resourceTypes, setResourceTypes] = useState([]);
  const [subjectOptions, setSubjectOptions] = useState([]);
  const [formLoading, setFormLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('pending');
  const [search, setSearch] = useState('');

  const [filterOpen, setFilterOpen] = useState(false);
  const [typeFilter, setTypeFilter] = useState('all');
  const [formatFilter, setFormatFilter] = useState('all');
  const [statusFilter, setStatusFilter] = useState('all');
  const [subjectFilter, setSubjectFilter] = useState('all');

  const [selected, setSelected] = useState(null);
  const [editing, setEditing] = useState(null);
  const [formOpen, setFormOpen] = useState(false);
  const [historyOpen, setHistoryOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [approveTarget, setApproveTarget] = useState(null);
  const [rejectTarget, setRejectTarget] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [approveLoading, setApproveLoading] = useState(false);
  const [rejectLoading, setRejectLoading] = useState(false);
  const { toast, showPending, showSuccess, showError } = useActionToast();

    const loadResources = () =>
        Promise.all([getResourcesAPI(), getMembersAPI()])
            .then(([resourceData, memberData]) => {
                const normalizedMembers = Array.isArray(memberData)
                    ? memberData.map(normalizeMemberFromApi)
                    : [];

                const memberMap = new Map(
                    normalizedMembers.map((member) => [Number(member.memberId), member])
                );

                const nextResources = Array.isArray(resourceData)
                    ? resourceData.map((resource) =>
                        normalizeResourceFromApi(
                            resource,
                            memberMap.get(Number(resource.proposedById))
                        )
                    )
                    : [];

                return hydrateResourcesWithFiles(nextResources).then((hydratedResources) => {
                    setResources(hydratedResources);
                    setApiError('');
                });
            })
            .catch((error) => {
                setResources([]);
                setApiError(error?.message || 'Không tải được danh sách tài liệu từ API.');
            });

    useEffect(() => {
        let ignore = false;

        Promise.allSettled([
            getResourcesAPI(),
            getResourceTypesAPI(),
            getSubjectsAPI(),
            getMembersAPI(),
        ]).then(([resourcesResult, typesResult, subjectsResult, membersResult]) => {
            if (ignore) return;

            let memberMap = new Map();

            if (
                membersResult.status === 'fulfilled' &&
                Array.isArray(membersResult.value)
            ) {
                const normalizedMembers = membersResult.value.map(normalizeMemberFromApi);

                memberMap = new Map(
                    normalizedMembers.map((member) => [Number(member.memberId), member])
                );
            }

            if (resourcesResult.status === 'fulfilled') {
                const nextResources = Array.isArray(resourcesResult.value)
                    ? resourcesResult.value.map((resource) =>
                        normalizeResourceFromApi(
                            resource,
                            memberMap.get(Number(resource.proposedById))
                        )
                    )
                    : [];

                hydrateResourcesWithFiles(nextResources).then((hydratedResources) => {
                    if (ignore) return;
                    setResources(hydratedResources);
                });
                setApiError('');
            } else {
                setResources([]);
                setApiError(
                    resourcesResult.reason?.message ||
                    'Không tải được danh sách tài liệu từ API.'
                );
            }

            if (
                typesResult.status === 'fulfilled' &&
                Array.isArray(typesResult.value)
            ) {
                setResourceTypes(typesResult.value);
            }

            if (
                subjectsResult.status === 'fulfilled' &&
                Array.isArray(subjectsResult.value)
            ) {
                setSubjectOptions(subjectsResult.value);
            }
        });

        return () => {
            ignore = true;
        };
    }, []);

  const stats = useMemo(() => ({
    total: resources.length,
    pending: resources.filter((resource) => resource.status === 'pending').length,
    fixing: resources.filter((resource) => resource.status === 'fixing').length,
    approved: resources.filter((resource) => resource.status === 'approved').length,
    rejected: resources.filter((resource) => resource.status === 'rejected').length,
  }), [resources]);

  const subjects = useMemo(
    () => [...new Set(resources.map((resource) => resource.subject).filter(Boolean))].sort(),
    [resources],
  );

  const filteredResources = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase();
    return resources
      .filter((resource) => {
        const matchesSearch =
          !normalizedSearch ||
          resource.title.toLowerCase().includes(normalizedSearch) ||
          resource.formCode.toLowerCase().includes(normalizedSearch) ||
          resource.subject.toLowerCase().includes(normalizedSearch) ||
          String(resource.uploadedBy || '').toLowerCase().includes(normalizedSearch);

        const matchesType = typeFilter === 'all' || resource.type === typeFilter;
        const matchesFormat = formatFilter === 'all' || resource.format === formatFilter;
        const matchesStatus =
          activeTab === 'pending'
            ? resource.status === 'pending'
            : activeTab === 'fixing'
              ? resource.status === 'fixing'
              : statusFilter === 'all' || resource.status === statusFilter;
        const matchesSubject = subjectFilter === 'all' || resource.subject === subjectFilter;

        return matchesSearch && matchesType && matchesFormat && matchesStatus && matchesSubject;
      })
      .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
  }, [resources, search, typeFilter, formatFilter, statusFilter, subjectFilter, activeTab]);

  const historyResources = useMemo(
    () => resources
      .filter((resource) => resource.status === 'approved' || resource.status === 'rejected')
      .sort((a, b) => new Date(b.reviewedAt || b.createdAt) - new Date(a.reviewedAt || a.createdAt)),
    [resources],
  );

  const openCreateForm = () => {
    setEditing(null);
    setFormOpen(true);
  };

  const openApproveForm = (id) => {
    const resource = resources.find((item) => item.id === id);
    if (resource) setApproveTarget(resource);
  };

  const handleApprove = async (id, lookupFolderId) => {
    if (!currentUser?.memberId) {
      setApiError('Không xác định được tài khoản admin đang đăng nhập.');
      return;
    }

    try {
      setApproveLoading(true);
      showPending('Đang duyệt tài liệu...');
      const updated = await approveResourceAPI({
        documentId: id,
        approvedBy: currentUser.memberId,
        status: 'APPROVED',
        lookupFolderId,
        note: 'Đã duyệt',
      });
      const normalized = normalizeResourceFromApi(updated);
      setResources((prev) => prev.map((resource) => (resource.id === id ? normalized : resource)));
      setApiError('');
      setSelected(null);
      setApproveTarget(null);
      setRejectTarget(null);
      setApproveLoading(false);
      showSuccess('Đã duyệt tài liệu.');
    } catch (error) {
      setApproveLoading(false);
      showError(error?.message || error || 'Không duyệt được tài liệu.');
    }
  };

  const openRejectForm = (id) => {
    const resource = resources.find((item) => item.id === id);
    if (resource) setRejectTarget(resource);
  };

  const handleReject = async (id, reason) => {
    if (!currentUser?.memberId) {
      setApiError('Không xác định được tài khoản admin đang đăng nhập.');
      return;
    }

    try {
      setRejectLoading(true);
      showPending('Đang từ chối tài liệu...');
      const updated = await approveResourceAPI({
        documentId: id,
        approvedBy: currentUser.memberId,
        status: 'REJECTED',
        note: reason,
      });
      const normalized = normalizeResourceFromApi(updated);
      setResources((prev) => prev.map((resource) => (resource.id === id ? normalized : resource)));
      setApiError('');
      setSelected(null);
      setRejectTarget(null);
      setRejectLoading(false);
      showSuccess('Đã từ chối tài liệu.');
    } catch (error) {
      setRejectLoading(false);
      showError(error?.message || error || 'Không từ chối được tài liệu.');
    }
  };

  const handleSubmit = async (data) => {
    const normalizedTitle = data.title.trim().toLowerCase();
    const normalizedSource = (data.source || '').trim().toLowerCase();

    const duplicate = resources.some((resource) =>
      resource.id !== editing?.id &&
      resource.title.trim().toLowerCase() === normalizedTitle &&
      (resource.source || '').trim().toLowerCase() === normalizedSource
    );

    if (duplicate) {
      alert(RESOURCE_RULES.uniqueTitleAndLink);
      return;
    }

    if (editing) {
      setResources((prev) =>
        prev.map((resource) =>
          resource.id === editing.id
            ? { ...resource, ...data, status: resource.status }
            : resource,
        ),
      );
      setFormOpen(false);
      setEditing(null);
      return;
    }

    if (!currentUser?.memberId) {
      setApiError('Không xác định được tài khoản đang đăng nhập.');
      return;
    }

    setFormLoading(true);
    try {
      const created = await createResourceAPI(toResourcePayload({
        ...data,
        proposedById: currentUser.memberId,
      }));

      let uploadedFile = null;
      if (data.file || data.fileUrl?.trim()) {
        try {
          const filePayload = buildResourceFilePayload(created.documentId, data);
          uploadedFile = await createResourceFileAPI(filePayload);
        } catch (uploadError) {
          await loadResources();
          setApiError(uploadError?.message || 'Đã tạo phiếu nhưng tải tệp thất bại.');
          return;
        }
      }

      await loadResources();
      if (uploadedFile) {
        const uploadedFields = normalizeUploadedResourceFile(uploadedFile);
        const createdId = created.documentId || created.id;
        setResources((prev) =>
          prev.map((resource) =>
            Number(resource.id) === Number(createdId) ? { ...resource, ...uploadedFields } : resource
          )
        );
      }
      setFormOpen(false);
      setEditing(null);
    } catch (error) {
      setApiError(error?.message || error || 'Không lưu được tài liệu.');
    } finally {
      setFormLoading(false);
    }
  };

  const confirmDelete = async () => {
    if (!deleteTarget) return;
    try {
      setDeleteLoading(true);
      showPending('Đang xóa tài liệu...');
      await softDeleteResourceAPI(deleteTarget.id);
      setResources((prev) => prev.filter((resource) => resource.id !== deleteTarget.id));
      setDeleteTarget(null);
      setApiError('');
      setDeleteLoading(false);
      showSuccess('Xóa tài liệu thành công.');
    } catch (error) {
      setDeleteLoading(false);
      showError(error?.message || error || 'Không xoá được tài liệu.');
    }
  };

  return (
    <div className={styles.page}>
      <ActionToast toast={toast} />
      {apiError && <div className={styles.apiError}>{apiError}</div>}

      <ResourceAdminHeader
        onAddResource={openCreateForm}
        onOpenHistory={() => setHistoryOpen(true)}
        historyActive={historyOpen}
      />

      <ResourceStats stats={stats} />

      <div className={styles.workflowPanel}>
        <ResourceAdminTabs
          activeTab={activeTab}
          pendingCount={stats.pending}
          fixingCount={stats.fixing}
          approvedCount={stats.approved}
          onChange={setActiveTab}
        />

        <div className={styles.controlRow}>
          <div className={styles.searchBox}>
            <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
            </svg>
            <input
              type="text"
              placeholder="Tìm theo tên tài liệu, môn học, người đề xuất..."
              value={search}
              onChange={(event) => setSearch(event.target.value)}
            />
            {search && (
              <button type="button" className={styles.clearSearch} onClick={() => setSearch('')}>×</button>
            )}
          </div>

          {(activeTab === 'pending' || activeTab === 'fixing' || activeTab === 'lookup') && (
            <ResourceFilter
              open={filterOpen}
              setOpen={setFilterOpen}
              typeFilter={typeFilter}
              setTypeFilter={setTypeFilter}
              formatFilter={formatFilter}
              setFormatFilter={setFormatFilter}
              subjectFilter={subjectFilter}
              setSubjectFilter={setSubjectFilter}
              subjects={subjects}
              statusFilter={statusFilter}
              setStatusFilter={setStatusFilter}
              showStatus={activeTab === 'lookup'}
            />
          )}
        </div>
      </div>

      {(activeTab === 'pending' || activeTab === 'fixing') && (
        <div className={styles.tableSection}>
          <div className={styles.tableHeader}>
            <div>
              <h2 className={styles.tableTitle}>
                {activeTab === 'pending' ? 'Phiếu tài liệu chờ duyệt thêm' : 'Phiếu tài liệu chờ duyệt sửa'}
              </h2>
              <p className={styles.tableSubtitle}>
                Danh sách phiếu đề xuất đang chờ admin xét duyệt.
              </p>
            </div>
            <span className={styles.tableCount}>{filteredResources.length} phiếu</span>
          </div>

          <ResourceTable
            resources={filteredResources}
            total={filteredResources.length}
            page={1}
            totalPages={1}
            pageSize={Math.max(filteredResources.length, 1)}
            onPageChange={() => {}}
            onView={setSelected}
            onEdit={(resource) => {
              setEditing(resource);
              setFormOpen(true);
            }}
            onApprove={openApproveForm}
            onReject={openRejectForm}
            onDelete={setDeleteTarget}
            loading={false}
          />
        </div>
      )}

      {activeTab === 'lookup' && (
        <ResourceLookupTable
          resources={resources}
          search={search}
          onView={setSelected}
          onEdit={(resource) => {
            setEditing(resource);
            setFormOpen(true);
          }}
          onDelete={setDeleteTarget}
          typeFilter={typeFilter}
          formatFilter={formatFilter}
          subjectFilter={subjectFilter}
          statusFilter={statusFilter}
        />
      )}

      <ResourceReviewModal
        resource={selected}
        onClose={() => setSelected(null)}
        onApprove={openApproveForm}
        onReject={openRejectForm}
      />

      <ResourceForm
        open={formOpen}
        initial={editing}
        isAdmin
        onClose={() => {
          setFormOpen(false);
          setEditing(null);
        }}
        onSubmit={handleSubmit}
        loading={formLoading}
        resourceTypes={resourceTypes}
        subjectOptions={subjectOptions}
      />

      <ResourceDeleteConfirmModal
        resource={deleteTarget}
        onCancel={() => { if (!deleteLoading) setDeleteTarget(null); }}
        onConfirm={confirmDelete}
        loading={deleteLoading}
      />

      <ResourceApproveModal
        resource={approveTarget}
        onCancel={() => { if (!approveLoading) setApproveTarget(null); }}
        onConfirm={handleApprove}
        loading={approveLoading}
      />

      <ResourceRejectModal
        resource={rejectTarget}
        onCancel={() => { if (!rejectLoading) setRejectTarget(null); }}
        onConfirm={handleReject}
        loading={rejectLoading}
      />

      <ResourceHistoryModal
        open={historyOpen}
        resources={historyResources}
        onClose={() => setHistoryOpen(false)}
        onView={setSelected}
        onEdit={(resource) => {
          setEditing(resource);
          setFormOpen(true);
        }}
        onApprove={openApproveForm}
        onReject={openRejectForm}
        onDelete={setDeleteTarget}
      />
    </div>
  );
}
