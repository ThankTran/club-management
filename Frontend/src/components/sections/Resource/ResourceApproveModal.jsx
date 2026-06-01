import { useEffect, useState } from 'react';
import {
  DEFAULT_RESOURCE_FOLDER_ID,
  RESOURCE_FOLDER_TREE,
  RESOURCE_LEAF_FOLDERS,
} from '../../../data/Resource/resourceFolderData';
import styles from './ResourceApproveModal.module.css';

export default function ResourceApproveModal({ resource, onCancel, onConfirm, loading = false }) {
  const [folderId, setFolderId] = useState(DEFAULT_RESOURCE_FOLDER_ID);

  useEffect(() => {
    if (resource) {
      setFolderId(resource.lookupFolderId || DEFAULT_RESOURCE_FOLDER_ID);
    }
  }, [resource]);

  if (!resource) return null;

  const selectedFolder = RESOURCE_LEAF_FOLDERS.find((folder) => folder.id === folderId);

  return (
    <div className={styles.overlay} onClick={onCancel}>
      <div className={styles.box} onClick={(event) => event.stopPropagation()}>
        <div className={styles.icon}>✓</div>
        <h3 className={styles.title}>Duyệt và phân loại tài liệu</h3>
        <p className={styles.message}>
          Chọn thư mục lưu cho phiếu <strong>{resource.formCode}</strong> - <em>{resource.title}</em>.
        </p>

        <div className={styles.folderPicker}>
          {RESOURCE_FOLDER_TREE.map((node) => (
            <FolderOptions
              key={node.id}
              node={node}
              depth={0}
              selectedId={folderId}
              onSelect={setFolderId}
            />
          ))}
        </div>

        <div className={styles.selection}>
          <span>Thư mục đã chọn</span>
          <strong>{selectedFolder?.pathLabel || 'Chưa chọn'}</strong>
        </div>

        <div className={styles.actions}>
          <button type="button" className={styles.cancelBtn} onClick={onCancel} disabled={loading}>Hủy</button>
          <button type="button" className={styles.approveBtn} onClick={() => onConfirm(resource.id, folderId)} disabled={loading}>
            {loading ? 'Đang duyệt...' : 'Xác nhận duyệt'}
          </button>
        </div>
      </div>
    </div>
  );
}

function FolderOptions({ node, depth, selectedId, onSelect }) {
  const isLeaf = !node.children?.length;

  if (isLeaf) {
    return (
      <button
        type="button"
        className={`${styles.folderItem} ${selectedId === node.id ? styles.folderItemActive : ''}`}
        style={{ paddingLeft: `${14 + depth * 16}px` }}
        onClick={() => onSelect(node.id)}
      >
        {node.label}
      </button>
    );
  }

  return (
    <div className={styles.folderGroup}>
      <div className={styles.folderGroupLabel} style={{ paddingLeft: `${14 + depth * 16}px` }}>
        {node.label}
      </div>
      {node.children.map((child) => (
        <FolderOptions
          key={child.id}
          node={child}
          depth={depth + 1}
          selectedId={selectedId}
          onSelect={onSelect}
        />
      ))}
    </div>
  );
}
