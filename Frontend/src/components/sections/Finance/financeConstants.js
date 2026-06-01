export const THANG_OPTIONS = Array.from({ length: 12 }, (_, i) => i + 1);
export const HINH_THUC_OPTIONS = ['Tiền mặt', 'Chuyển khoản'];

export const EMPTY_THU = { nguoiNop: '', lyDo: '', hinhThuc: '', ngayThu: '', soTien: '', maSuKien: '' };
export const EMPTY_CHI = { nguoiNhan: '', maSuKien: '', noiDung: '', ngayLap: '', soTien: '' };

export const TABS = [
  { id: 'overview', label: 'Tổng quan', icon: '📊' },
  { id: 'thu',      label: 'Phiếu Thu', icon: '💰' },
  { id: 'chi',      label: 'Phiếu Chi', icon: '💸' },
  { id: 'chuyenKhoan', label: 'Chờ xác nhận thu', icon: '🏦' },
  { id: 'baocao',   label: 'Báo cáo Quỹ', icon: '📋' },
];
