export const RESOURCE_FOLDER_TREE = [
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
      {
        id: 'cong-nghe-phan-mem',
        label: 'Công nghệ phần mềm',
        children: [
          { id: 'ky-thuat-phan-mem', label: 'Kỹ thuật phần mềm' },
          { id: 'truyen-thong-da-phuong-tien', label: 'Truyền thông đa phương tiện' },
        ],
      },
      {
        id: 'he-thong-thong-tin',
        label: 'Hệ thống thông tin',
        children: [
          { id: 'he-thong-thong-tin-chuyen-nganh', label: 'Hệ thống thông tin' },
          { id: 'thuong-mai-dien-tu', label: 'Thương mại điện tử' },
        ],
      },
      {
        id: 'khoa-hoc-may-tinh',
        label: 'Khoa học máy tính',
        children: [
          { id: 'khoa-hoc-may-tinh-chuyen-nganh', label: 'Khoa học máy tính' },
          { id: 'tri-tue-nhan-tao', label: 'Trí tuệ nhân tạo' },
        ],
      },
      {
        id: 'khoa-hoc-ky-thuat-thong-tin',
        label: 'Khoa học & Kỹ thuật thông tin',
        children: [
          { id: 'cong-nghe-thong-tin', label: 'Công nghệ thông tin' },
          { id: 'khoa-hoc-du-lieu', label: 'Khoa học dữ liệu' },
        ],
      },
      {
        id: 'mang-may-tinh-truyen-thong',
        label: 'Mạng máy tính & Truyền thông',
        children: [
          { id: 'an-toan-thong-tin', label: 'An toàn thông tin' },
          { id: 'mang-may-tinh-truyen-thong-du-lieu', label: 'Mạng máy tính & Truyền thông dữ liệu' },
        ],
      },
      {
        id: 'ky-thuat-may-tinh',
        label: 'Kỹ thuật máy tính',
        children: [
          { id: 'ky-thuat-may-tinh-chuyen-nganh', label: 'Kỹ thuật máy tính' },
          { id: 'thiet-ke-vi-mach', label: 'Thiết kế vi mạch' },
        ],
      },
    ],
  },
];

export const RESOURCE_LEAF_FOLDERS = flattenLeaves(RESOURCE_FOLDER_TREE);
export const DEFAULT_RESOURCE_FOLDER_ID = RESOURCE_LEAF_FOLDERS[0].id;
export const LEGACY_RESOURCE_FOLDER_ID_MAP = {
  'cong-nghe-phan-mem': 'ky-thuat-phan-mem',
  'he-thong-thong-tin': 'he-thong-thong-tin-chuyen-nganh',
  'khoa-hoc-may-tinh': 'khoa-hoc-may-tinh-chuyen-nganh',
  'ky-thuat-may-tinh': 'ky-thuat-may-tinh-chuyen-nganh',
  'mang-may-tinh': 'mang-may-tinh-truyen-thong-du-lieu',
};

export function normalizeResourceFolderId(folderId) {
  if (!folderId) return '';
  return LEGACY_RESOURCE_FOLDER_ID_MAP[folderId] || folderId;
}

export function flattenLeaves(nodes, parentLabels = []) {
  return nodes.flatMap((node) => {
    const path = [...parentLabels, node.label];
    if (!node.children?.length) {
      return { ...node, pathLabel: path.join(' / ') };
    }
    return flattenLeaves(node.children, path);
  });
}
