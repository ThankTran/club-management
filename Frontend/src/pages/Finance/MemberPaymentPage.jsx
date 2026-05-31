import { useCallback, useEffect, useMemo, useState } from 'react';
import { CreditCard, RefreshCw } from 'lucide-react';
import styles from './MemberPaymentPage.module.css';
import { fmtMoney } from '../../utils/Finance/financeUtils';
import {
  completeTransactionAPI,
  getMemberDuesAPI,
} from '../../services/finance-service';
import useAuthStore from '../../store/auth-store';

export default function MemberPaymentPage() {
  const currentUser = useAuthStore((state) => state.user);
  const memberId = currentUser?.memberId;
  const memberName = currentUser?.fullName || 'Thành viên';
  const memberCode = currentUser?.studentId || (memberId ? `TV${String(memberId).padStart(3, '0')}` : '');

  const [dues, setDues] = useState([]);
  const [activeDue, setActiveDue] = useState(null);
  const [bulkDues, setBulkDues] = useState([]);
  const [selectedDueIds, setSelectedDueIds] = useState([]);
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);
  const [bulkProcessing, setBulkProcessing] = useState(false);

  const loadDues = useCallback(() => {
    if (!memberId) {
      setDues([]);
      setApiError('Không xác định được thành viên đang đăng nhập.');
      return;
    }

    setLoading(true);
    getMemberDuesAPI(memberId)
      .then((data) => {
        const next = Array.isArray(data)
          ? data.map(normalizeDueFromTransaction).filter(Boolean)
          : [];
        setDues(next);
        setApiError('');
      })
      .catch((error) => {
        setDues([]);
        setApiError(error?.message || 'Không tải được danh sách khoản cần đóng.');
      })
      .finally(() => setLoading(false));
  }, [memberId]);

  useEffect(() => {
    loadDues();
  }, [loadDues]);

  const pending = useMemo(() => dues.filter((due) => due.status === 'pending'), [dues]);
  const paid = useMemo(() => dues.filter((due) => due.status === 'paid'), [dues]);
  const selectedDues = useMemo(
    () => pending.filter((due) => selectedDueIds.includes(due.id)),
    [pending, selectedDueIds],
  );
  const selectedTotal = useMemo(
    () => selectedDues.reduce((total, due) => total + Number(due.soTien || 0), 0),
    [selectedDues],
  );
  const allPendingSelected = pending.length > 0 && selectedDueIds.length === pending.length;

  useEffect(() => {
    setSelectedDueIds((prev) => prev.filter((id) => pending.some((due) => due.id === id)));
  }, [pending]);

  const handleToggleDue = (id) => {
    setSelectedDueIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id],
    );
  };

  const handleToggleAll = () => {
    setSelectedDueIds(allPendingSelected ? [] : pending.map((due) => due.id));
  };

  const handlePay = async (id) => {
    try {
      const completed = await completeTransactionAPI(id);
      const normalized = normalizeDueFromTransaction(completed);
      setDues((prev) => prev.map((due) => (due.id === id ? normalized : due)).filter(Boolean));
      setSelectedDueIds((prev) => prev.filter((selectedId) => selectedId !== id));
      window.dispatchEvent(new CustomEvent('finance:transactions-updated'));
      setActiveDue(null);
      setApiError('');
    } catch (error) {
      setApiError(error?.message || 'Không ghi nhận được thanh toán.');
    }
  };

  const handleOpenBulkPayment = () => {
    if (selectedDues.length === 0) return;
    setBulkDues(selectedDues);
  };

  const handleBulkPay = async () => {
    if (bulkDues.length === 0) return;

    setBulkProcessing(true);
    const results = await Promise.allSettled(
      bulkDues.map((due) => completeTransactionAPI(due.id)),
    );

    const completed = results
      .filter((result) => result.status === 'fulfilled')
      .map((result) => normalizeDueFromTransaction(result.value))
      .filter(Boolean);
    const completedIds = completed.map((due) => due.id);
    const failedCount = results.length - completed.length;

    if (completed.length > 0) {
      setDues((prev) =>
        prev
          .map((due) => completed.find((item) => item.id === due.id) || due)
          .filter(Boolean),
      );
      setSelectedDueIds((prev) => prev.filter((id) => !completedIds.includes(id)));
      window.dispatchEvent(new CustomEvent('finance:transactions-updated'));
    }

    setBulkProcessing(false);

    if (failedCount > 0) {
      setApiError(`Đã ghi nhận ${completed.length}/${bulkDues.length} khoản. ${failedCount} khoản chưa thành công.`);
      setBulkDues((prev) => prev.filter((due) => !completedIds.includes(due.id)));
      return;
    }

    setBulkDues([]);
    setApiError('');
  };

  return (
    <div className={styles.page}>
      {apiError && <div className={styles.apiError}>{apiError}</div>}

      <div className={styles.hero}>
        <div>
          <h1 className={styles.title}>Danh sách các khoản cần thanh toán</h1>
          <p className={styles.subtitle}>
            Thành viên xem phí sự kiện đã đăng ký và quỹ tháng hiện tại từ dữ liệu tài chính của hệ thống.
          </p>
        </div>
        <div className={styles.memberCard}>
          <span>Đang đăng nhập</span>
          <strong>{memberName}</strong>
          <small>{memberCode}</small>
        </div>
      </div>

      <div className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2>Khoản cần đóng</h2>
          <button
            type="button"
            className={styles.refreshBtn}
            onClick={loadDues}
            disabled={loading}
            aria-label={loading ? 'Đang tải' : 'Làm mới'}
            title={loading ? 'Đang tải...' : 'Làm mới'}
          >
            <RefreshCw size={17} strokeWidth={2.4} aria-hidden="true" />
          </button>
        </div>

        {pending.length === 0 ? (
          <div className={styles.empty}>Không có khoản nào đang chờ đóng.</div>
        ) : (
          <>
            <div className={styles.bulkBar}>
              <label className={styles.selectAllControl}>
                <input
                  type="checkbox"
                  checked={allPendingSelected}
                  onChange={handleToggleAll}
                />
                <span>Chọn tất cả</span>
              </label>
              <div className={styles.bulkSummary}>
                <strong>{selectedDues.length}</strong>
                <span>khoản đã chọn</span>
                <b>{fmtMoney(selectedTotal)}</b>
              </div>
              <button
                type="button"
                className={styles.bulkPayBtn}
                onClick={handleOpenBulkPayment}
                disabled={selectedDues.length === 0}
              >
                Thanh toán đã chọn
              </button>
            </div>
            <div className={styles.grid}>
              {pending.map((due) => (
                <PaymentCard
                  key={due.id}
                  due={due}
                  selected={selectedDueIds.includes(due.id)}
                  onToggle={handleToggleDue}
                  onPay={setActiveDue}
                />
              ))}
            </div>
          </>
        )}
      </div>

      <div className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2>Lịch sử đã đóng</h2>
          <span className={styles.countBadge}>{paid.length} khoản</span>
        </div>
        {paid.length === 0 ? (
          <div className={styles.empty}>Chưa có giao dịch nào được ghi nhận.</div>
        ) : (
          <div className={styles.paidList}>
            {paid.map((due) => (
              <div key={due.id} className={styles.paidRow}>
                <div>
                  <strong>{due.lyDo}</strong>
                  <span>{due.transferCode}</span>
                </div>
                <div className={styles.paidMeta}>
                  <span>{fmtMoney(due.soTien)}</span>
                  <small>{due.paidAt ? new Date(due.paidAt).toLocaleString('vi-VN') : 'Đã ghi nhận'}</small>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {activeDue && (
        <QrPaymentModal
          due={activeDue}
          memberName={memberName}
          memberCode={memberCode}
          onClose={() => setActiveDue(null)}
          onConfirm={() => handlePay(activeDue.id)}
        />
      )}

      {bulkDues.length > 0 && (
        <BulkPaymentModal
          dues={bulkDues}
          memberName={memberName}
          memberCode={memberCode}
          processing={bulkProcessing}
          onClose={() => {
            if (!bulkProcessing) setBulkDues([]);
          }}
          onConfirm={handleBulkPay}
        />
      )}
    </div>
  );
}

function PaymentCard({ due, selected, onToggle, onPay }) {
  return (
    <article className={`${styles.card} ${selected ? styles.selectedCard : ''}`}>
      <div className={styles.cardBody}>
        <div className={styles.cardTop}>
          <label className={styles.cardSelect}>
            <input
              type="checkbox"
              checked={selected}
              onChange={() => onToggle(due.id)}
              aria-label={`Chọn khoản ${due.id}`}
            />
            <span className={styles.qrCode}>Mã: {due.id}</span>
          </label>
          <span className={styles.pendingBadge}>Chờ đóng</span>
        </div>
        <div className={styles.cardContent}>
          <p className={styles.contentLabel}>Nội dung</p>
          <h3>{due.lyDo}</h3>
        </div>
        <div className={styles.cardFooter}>
          <p className={styles.cardAmount}>{fmtMoney(due.soTien)}</p>
          <button
            type="button"
            className={styles.cardPayBtn}
            onClick={() => onPay(due)}
            aria-label={`Thanh toán khoản ${due.id}`}
            title="Thanh toán"
          >
            <CreditCard size={17} strokeWidth={2.4} aria-hidden="true" />
          </button>
        </div>
      </div>
    </article>
  );
}

function BulkPaymentModal({ dues, memberName, memberCode, processing, onClose, onConfirm }) {
  const total = dues.reduce((sum, due) => sum + Number(due.soTien || 0), 0);
  const transferCodes = dues.map((due) => due.transferCode).join(', ');

  return (
    <div className={styles.modalOverlay} role="presentation" onClick={onClose}>
      <div
        className={styles.qrModal}
        role="dialog"
        aria-modal="true"
        aria-labelledby="bulk-payment-title"
        onClick={(event) => event.stopPropagation()}
      >
        <div className={styles.momoHeader}>
          <div>
            <span>Thanh toán hàng loạt</span>
            <h2 id="bulk-payment-title">Xác nhận nộp {dues.length} khoản</h2>
          </div>
          <button type="button" className={styles.closeBtn} onClick={onClose} aria-label="Đóng" disabled={processing}>
            x
          </button>
        </div>

        <div className={styles.qrPanel}>
          <div className={styles.momoBrand}>
            <span className={styles.momoLogo}>MoMo</span>
            <strong>CLB Học thuật THMN</strong>
          </div>
          <div className={styles.bulkTotal}>
            <span>Tổng cộng</span>
            <strong>{fmtMoney(total)}</strong>
          </div>
          <p className={styles.scanHint}>Ghi nhận một lần cho tất cả khoản đã chọn</p>
        </div>

        <div className={styles.modalBody}>
          <div className={styles.paymentInfo}>
            <div>
              <span>Người nộp</span>
              <strong>{memberName} - {memberCode}</strong>
            </div>
            <div>
              <span>Số khoản</span>
              <strong>{dues.length} khoản</strong>
            </div>
            <div>
              <span>Mã chuyển khoản</span>
              <strong>{transferCodes}</strong>
            </div>
          </div>
          <div className={styles.bulkList}>
            {dues.map((due) => (
              <div key={due.id} className={styles.bulkItem}>
                <span>{due.lyDo}</span>
                <strong>{fmtMoney(due.soTien)}</strong>
              </div>
            ))}
          </div>
        </div>

        <div className={styles.modalActions}>
          <button type="button" className={styles.secondaryBtn} onClick={onClose} disabled={processing}>
            Để sau
          </button>
          <button type="button" className={styles.confirmBtn} onClick={onConfirm} disabled={processing}>
            {processing ? 'Đang xử lý...' : 'Thanh toán tất cả'}
          </button>
        </div>
      </div>
    </div>
  );
}

function QrPaymentModal({ due, memberName, memberCode, onClose, onConfirm }) {
  return (
    <div className={styles.modalOverlay} role="presentation" onClick={onClose}>
      <div
        className={styles.qrModal}
        role="dialog"
        aria-modal="true"
        aria-labelledby="qr-payment-title"
        onClick={(event) => event.stopPropagation()}
      >
        <div className={styles.momoHeader}>
          <div>
            <span>Thanh toán MoMo</span>
            <h2 id="qr-payment-title">Quét QR để nộp tiền</h2>
          </div>
          <button type="button" className={styles.closeBtn} onClick={onClose} aria-label="Đóng">
            x
          </button>
        </div>

        <div className={styles.qrPanel}>
          <div className={styles.momoBrand}>
            <span className={styles.momoLogo}>MoMo</span>
            <strong>CLB Học thuật THMN</strong>
          </div>
          <div className={styles.fakeQr} aria-label={`QR thanh toán ${due.transferCode}`}>
            <span />
            <span />
            <span />
            <small>{due.id}</small>
          </div>
          <p className={styles.scanHint}>Dùng ứng dụng MoMo hoặc ngân hàng để quét mã</p>
        </div>

        <div className={styles.modalBody}>
          <div className={styles.paymentInfo}>
            <div>
              <span>Số tiền</span>
              <strong className={styles.modalAmount}>{fmtMoney(due.soTien)}</strong>
            </div>
            <div>
              <span>Nội dung</span>
              <strong>CLB THMN | {due.transferCode}</strong>
            </div>
            <div>
              <span>Mã chuyển khoản</span>
              <strong>{due.transferCode}</strong>
            </div>
            {due.maSuKien && (
              <div>
                <span>Mã sự kiện</span>
                <strong>{due.maSuKien}</strong>
              </div>
            )}
            <div>
              <span>Người nộp</span>
              <strong>{memberName} - {memberCode}</strong>
            </div>
          </div>
        </div>

        <div className={styles.modalActions}>
          <button type="button" className={styles.secondaryBtn} onClick={onClose}>
            Để sau
          </button>
          <button type="button" className={styles.confirmBtn} onClick={onConfirm}>
            Thanh toán
          </button>
        </div>
      </div>
    </div>
  );
}

function normalizeDueFromTransaction(transaction = {}) {
  const status = String(transaction.status || '').toUpperCase();
  if (!['PENDING', 'COMPLETED', 'APPROVED'].includes(status)) {
    return null;
  }

  return {
    id: transaction.transactionId,
    transferCode: `${transaction.eventId || 'QUY'}-${transaction.transactionId}`,
    lyDo: normalizeDueDescription(transaction.description || 'Khoản cần đóng'),
    soTien: Number(transaction.amount || 0),
    maSuKien: transaction.eventId || '',
    status: status === 'PENDING' ? 'pending' : 'paid',
    paidAt: transaction.approvedAt || transaction.updatedAt || '',
    targetName: transaction.memberName || transaction.counterpartyName || '',
    raw: transaction,
  };
}

function normalizeDueDescription(description = '') {
  return String(description)
    .replace(/Phi tham gia su kien:/gi, 'Phí tham gia sự kiện:')
    .replace(/Phi tham gia sự kiện:/gi, 'Phí tham gia sự kiện:')
    .replace(/Phí tham gia su kien:/gi, 'Phí tham gia sự kiện:');
}
