import { useMemo, useState } from 'react';
import { FORMAT_CONFIG, RESOURCE_LOOKUP_MAX_DISPLAY } from '../../../data/Resource/resourceAdminData';
import styles from './ResourceLookupTable.module.css';

const FOLDER_TREE = [
  {
    id: 'general',
    label: 'ĐẠI CƯƠNG',
    children: [
      {
        id: 'politics-law',
        label: 'Lý luận chính trị và pháp luật',
        children: [
          { id: 'tu-tuong-ho-chi-minh', label: 'Tư tưởng Hồ Chí Minh' },
          { id: 'triet-hoc-mac-lenin', label: 'Triết học Mác - Lênin' },
          { id: 'kinh-te-chinh-tri', label: 'Kinh tế Chính trị Mác - Lênin' },
          { id: 'chu-nghia-xa-hoi-khoa-hoc', label: 'Chủ nghĩa xã hội khoa học' },
          { id: 'lich-su-dang', label: 'Lịch sử Đảng Cộng sản Việt Nam' },
          { id: 'phap-luat-dai-cuong', label: 'Pháp luật đại cương' },
        ],
      },
      {
        id: 'math-it-science',
        label: 'Toán - Tin học - Khoa học tự nhiên',
        children: [
          { id: 'giai-tich', label: 'Giải tích' },
          { id: 'dai-so-tuyen-tinh', label: 'Đại số tuyến tính' },
          { id: 'cau-truc-roi-rac', label: 'Cấu trúc rời rạc' },
          { id: 'xac-suat-thong-ke', label: 'Xác suất thống kê' },
          { id: 'nhap-mon-lap-trinh', label: 'Nhập môn lập trình' },
        ],
      },
      {
        id: 'foreign-language',
        label: 'Ngoại ngữ',
        children: [
          { id: 'anh-van-1', label: 'Anh văn 1' },
          { id: 'anh-van-2', label: 'Anh văn 2' },
          { id: 'anh-van-3', label: 'Anh văn 3' },
        ],
      },
    ],
  },
  {
    id: 'major',
    label: 'CHUYÊN NGÀNH',
    children: [
      { id: 'cong-nghe-phan-mem', label: 'Công nghệ phần mềm' },
      { id: 'he-thong-thong-tin', label: 'Hệ thống thông tin' },
      { id: 'khoa-hoc-may-tinh', label: 'Khoa học máy tính' },
      { id: 'ky-thuat-may-tinh', label: 'Kỹ thuật máy tính' },
      { id: 'mang-may-tinh', label: 'Mạng máy tính và truyền thông dữ liệu' },
      { id: 'an-toan-thong-tin', label: 'An toàn thông tin' },
      { id: 'thuong-mai-dien-tu', label: 'Thương mại điện tử' },
    ],
  },
];

const LEAF_FOLDERS = flattenLeaves(FOLDER_TREE);
const DEFAULT_FOLDER_ID = LEAF_FOLDERS[0].id;

const SAMPLE_LOOKUP_RESOURCES = LEAF_FOLDERS.map((folder, index) => ({
  id: `sample-${folder.id}`,
  formCode: `TK-${String(index + 1).padStart(3, '0')}`,
  title: `Tài liệu tham khảo ${folder.label}`,
  subject: folder.label,
  type: index % 3 === 0 ? 'Giáo trình' : index % 3 === 1 ? 'Slide bài giảng' : 'Tài liệu tham khảo',
  format: index % 4 === 0 ? 'PDF' : index % 4 === 1 ? 'PPT' : index % 4 === 2 ? 'DOCX' : 'ZIP',
  source: index % 2 === 0 ? 'Giảng viên cung cấp' : 'Internet',
  description: `Tài liệu mẫu thuộc thư mục ${folder.pathLabel}.`,
  link: `https://drive.google.com/example-${folder.id}`,
  uploadedBy: 'Ban học tập',
  createdAt: `2024-12-${String((index % 20) + 1).padStart(2, '0')}`,
  status: 'approved',
  lookupFolderId: folder.id,
}));

const WORKFLOW_STATUS = {
  working: { label: 'working', bg: '#dcfce7', color: '#15803d' },
  fixing: { label: 'fixing', bg: '#e0f2fe', color: '#0369a1' },
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
        lookupFolderId: resource.lookupFolderId || resolveFolderId(resource),
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
                  <th>Link</th>
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
                  const workflow = WORKFLOW_STATUS[resource.workflowStatus] || WORKFLOW_STATUS.working;
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
                          <span className={styles.noLink}>-</span>
                        )}
                      </td>
                      <td onClick={(event) => event.stopPropagation()}>
                        <div className={styles.rowActions}>
                          <button type="button" className={styles.actionBtn} onClick={() => onEdit?.(resource)}>
                            Sửa
                          </button>
                          <button type="button" className={styles.actionBtnDanger} onClick={() => onDelete?.(resource)}>
                            Xóa
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

function flattenLeaves(nodes, parentLabels = []) {
  return nodes.flatMap((node) => {
    const path = [...parentLabels, node.label];
    if (!node.children?.length) {
      return [{ ...node, pathLabel: path.join(' / ') }];
    }
    return flattenLeaves(node.children, path);
  });
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
    ['lập trình', 'cong-nghe-phan-mem'],
    ['cơ sở dữ liệu', 'he-thong-thong-tin'],
    ['hệ thống', 'he-thong-thong-tin'],
    ['trí tuệ nhân tạo', 'khoa-hoc-may-tinh'],
    ['hệ điều hành', 'khoa-hoc-may-tinh'],
    ['kiến trúc máy tính', 'ky-thuat-may-tinh'],
    ['mạng máy tính', 'mang-may-tinh'],
    ['an toàn thông tin', 'an-toan-thong-tin'],
    ['web', 'cong-nghe-phan-mem'],
    ['ui/ux', 'thuong-mai-dien-tu'],
  ];

  return rules.find(([keyword]) => text.includes(keyword))?.[1] || DEFAULT_FOLDER_ID;
}
