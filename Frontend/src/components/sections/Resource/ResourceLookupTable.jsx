import { useMemo, useState } from 'react';
import { Pencil, Trash2 } from 'lucide-react';
import { FORMAT_CONFIG, RESOURCE_LOOKUP_MAX_DISPLAY } from '../../../data/Resource/resourceAdminData';
import {
  DEFAULT_RESOURCE_FOLDER_ID,
  normalizeResourceFolderId,
  RESOURCE_FOLDER_TREE,
  RESOURCE_LEAF_FOLDERS,
} from '../../../data/Resource/resourceFolderData';
import styles from './ResourceLookupTable.module.css';

const FOLDER_TREE = RESOURCE_FOLDER_TREE;
const LEAF_FOLDERS = RESOURCE_LEAF_FOLDERS;
const DEFAULT_FOLDER_ID = DEFAULT_RESOURCE_FOLDER_ID;

const WORKFLOW_STATUS = {
  working: { label: 'Đang sử dụng', bg: '#dcfce7', color: '#15803d' },
  fixing: { label: 'Đang chỉnh sửa', bg: '#e0f2fe', color: '#0369a1' },
  cancelled: { label: 'Ngừng sử dụng', bg: '#fee2e2', color: '#b91c1c' },
};

export default function ResourceLookupTable({
  resources,
  search,
  onView,
  onEdit,
  onDelete,
  typeFilter = 'all',
  formatFilter = 'all',
  subjectFilter = 'all',
  statusFilter = 'all',
}) {
  const [selectedFolderId, setSelectedFolderId] = useState(DEFAULT_FOLDER_ID);
  const [openFolderIds, setOpenFolderIds] = useState(() => new Set(FOLDER_TREE.map((node) => node.id)));

  const approvedResources = useMemo(() => {
    const published = resources
      .filter((resource) => resource.approvedAt || resource.status === 'approved' || resource.status === 'fixing')
      .map((resource) => ({
        ...resource,
        lookupFolderId: normalizeResourceFolderId(resource.lookupFolderId) || resolveFolderId(resource),
      }));

    return published
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  }, [resources]);

  const selectedFolder = LEAF_FOLDERS.find((folder) => folder.id === selectedFolderId) || LEAF_FOLDERS[0];
  const normalizedSearch = search.trim().toLowerCase();

  const rows = approvedResources
    .filter((resource) => resource.lookupFolderId === selectedFolder.id)
    .filter((resource) => {
      if (!normalizedSearch) return true;
      return (
        resource.title.toLowerCase().includes(normalizedSearch) ||
        resource.subject.toLowerCase().includes(normalizedSearch) ||
        resource.type.toLowerCase().includes(normalizedSearch) ||
        selectedFolder.pathLabel.toLowerCase().includes(normalizedSearch)
      );
    })
    .filter((resource) => typeFilter === 'all' || resource.type === typeFilter)
    .filter((resource) => formatFilter === 'all' || resource.format === formatFilter)
    .filter((resource) => subjectFilter === 'all' || resource.subject === subjectFilter)
    .filter((resource) => statusFilter === 'all' || resource.status === statusFilter)
    .slice(0, RESOURCE_LOOKUP_MAX_DISPLAY);

  const countsByFolder = approvedResources.reduce((counts, resource) => {
    counts[resource.lookupFolderId] = (counts[resource.lookupFolderId] || 0) + 1;
    return counts;
  }, {});

  const toggleFolder = (folderId) => {
    setOpenFolderIds((prev) => {
      const next = new Set(prev);
      if (next.has(folderId)) {
        next.delete(folderId);
      } else {
        next.add(folderId);
      }
      return next;
    });
  };

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <div>
          <h3 className={styles.title}>Tra cứu kho tài liệu học thuật</h3>
          <p className={styles.subtitle}>
            Chọn thư mục đến cấp môn học/ngành, sau đó xem hoặc tải tài liệu đã được duyệt.
          </p>
        </div>
      </div>

      <div className={styles.lookupLayout}>
        <aside className={styles.folderPane}>
          <div className={styles.folderTitle}>Thư mục tài liệu</div>
          <div className={styles.folderTree}>
            {FOLDER_TREE.map((node) => (
              <FolderNode
                key={node.id}
                node={node}
                depth={0}
                selectedFolderId={selectedFolderId}
                countsByFolder={countsByFolder}
                openFolderIds={openFolderIds}
                onSelect={setSelectedFolderId}
                onToggle={toggleFolder}
              />
            ))}
          </div>
        </aside>

        <div className={styles.resultPane}>
          <div className={styles.resultHeader}>
            <div>
              <p className={styles.breadcrumb}>{selectedFolder.pathLabel}</p>
              <h4 className={styles.resultTitle}>{selectedFolder.label}</h4>
            </div>
            <span className={styles.resultCount}>{rows.length} tài liệu</span>
          </div>

          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>STT</th>
                  <th>Tên tài liệu</th>
                  <th>Chủ đề / Môn học</th>
                  <th>Loại</th>
                  <th>Định dạng</th>
                  <th>Trạng thái sử dụng</th>
                  <th>Tệp</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {rows.length === 0 ? (
                  <tr>
                    <td colSpan={8} className={styles.emptyCell}>Không có tài liệu đã duyệt phù hợp</td>
                  </tr>
                ) : rows.map((resource, index) => {
                  const format = FORMAT_CONFIG[resource.format] || FORMAT_CONFIG.Khác;
                  const workflowKey = String(resource.workflowStatus || resource.status || 'working').toLowerCase();
                  const workflow = WORKFLOW_STATUS[workflowKey] || WORKFLOW_STATUS.working;
                  return (
                    <tr key={resource.id} className={styles.dataRow} onClick={() => onView?.(resource)}>
                      <td className={styles.indexCell}>{index + 1}</td>
                      <td className={styles.titleCell}>{resource.title}</td>
                      <td>{resource.subject}</td>
                      <td>{resource.type}</td>
                      <td onClick={(event) => event.stopPropagation()}>
                        <span className={styles.formatBadge} style={{ background: format.bg, color: format.color }}>
                          {resource.format}
                        </span>
                      </td>
                      <td>
                        <span className={styles.formatBadge} style={{ background: workflow.bg, color: workflow.color }}>
                          {workflow.label}
                        </span>
                      </td>
                      <td>
                        {resource.link ? (
                          <a className={styles.linkBtn} href={resource.link} target="_blank" rel="noopener noreferrer">
                            Xem tài liệu
                          </a>
                        ) : (
                          <span className={styles.noLink}>Chưa có tệp</span>
                        )}
                      </td>
                      <td onClick={(event) => event.stopPropagation()}>
                        <div className={styles.rowActions}>
                          <button type="button" className={styles.actionBtn} onClick={() => onEdit?.(resource)} title="Sửa tài liệu" aria-label="Sửa tài liệu">
                            <Pencil size={15} strokeWidth={2.2} />
                          </button>
                          <button type="button" className={styles.actionBtnDanger} onClick={() => onDelete?.(resource)} title="Xóa tài liệu" aria-label="Xóa tài liệu">
                            <Trash2 size={15} strokeWidth={2.2} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          <div className={styles.footer}>
            Hiển thị tối đa {RESOURCE_LOOKUP_MAX_DISPLAY} tài liệu trong thư mục đang chọn.
          </div>
        </div>
      </div>
    </div>
  );
}

function FolderNode({ node, depth, selectedFolderId, countsByFolder, openFolderIds, onSelect, onToggle }) {
  const isLeaf = !node.children?.length;
  const selected = selectedFolderId === node.id;
  const open = openFolderIds.has(node.id);

  if (isLeaf) {
    return (
      <button
        type="button"
        className={`${styles.folderLeaf} ${selected ? styles.folderLeafActive : ''}`}
        style={{ paddingLeft: `${12 + depth * 16}px` }}
        onClick={() => onSelect(node.id)}
      >
        <span>{node.label}</span>
        <small>{countsByFolder[node.id] || 0}</small>
      </button>
    );
  }

  return (
    <div className={styles.folderGroup}>
      <button
        type="button"
        className={styles.folderGroupLabel}
        style={{ paddingLeft: `${12 + depth * 16}px` }}
        onClick={() => onToggle(node.id)}
      >
        <span className={`${styles.folderIcon} ${open ? styles.folderIconOpen : ''}`}>›</span>
        {node.label}
      </button>
      {open && node.children.map((child) => (
        <FolderNode
          key={child.id}
          node={child}
          depth={depth + 1}
          selectedFolderId={selectedFolderId}
          countsByFolder={countsByFolder}
          openFolderIds={openFolderIds}
          onSelect={onSelect}
          onToggle={onToggle}
        />
      ))}
    </div>
  );
}

function resolveFolderId(resource) {
  const text = `${resource.subject || ''} ${resource.title || ''}`.toLowerCase();
  const directMatch = LEAF_FOLDERS.find((folder) => text.includes(folder.label.toLowerCase()));
  if (directMatch) return directMatch.id;

  const rules = [
    ['pháp luật', 'phap-luat-dai-cuong'],
    ['triết học', 'triet-hoc-mac-lenin'],
    ['giải tích', 'giai-tich'],
    ['đại số', 'dai-so-tuyen-tinh'],
    ['xác suất', 'xac-suat-thong-ke'],
    ['kỹ thuật phần mềm', 'ky-thuat-phan-mem'],
    ['lập trình', 'ky-thuat-phan-mem'],
    ['truyền thông đa phương tiện', 'truyen-thong-da-phuong-tien'],
    ['cơ sở dữ liệu', 'he-thong-thong-tin-chuyen-nganh'],
    ['hệ thống', 'he-thong-thong-tin-chuyen-nganh'],
    ['trí tuệ nhân tạo', 'tri-tue-nhan-tao'],
    ['hệ điều hành', 'khoa-hoc-may-tinh-chuyen-nganh'],
    ['khoa học dữ liệu', 'khoa-hoc-du-lieu'],
    ['công nghệ thông tin', 'cong-nghe-thong-tin'],
    ['kiến trúc máy tính', 'ky-thuat-may-tinh-chuyen-nganh'],
    ['kỹ thuật máy tính', 'ky-thuat-may-tinh-chuyen-nganh'],
    ['thiết kế vi mạch', 'thiet-ke-vi-mach'],
    ['mạng máy tính', 'mang-may-tinh-truyen-thong-du-lieu'],
    ['an toàn thông tin', 'an-toan-thong-tin'],
    ['web', 'ky-thuat-phan-mem'],
    ['ui/ux', 'thuong-mai-dien-tu'],
  ];

  return rules.find(([keyword]) => text.includes(keyword))?.[1] || DEFAULT_FOLDER_ID;
}
