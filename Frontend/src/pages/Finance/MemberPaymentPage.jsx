import { useCallback, useEffect, useMemo, useState } from 'react';
import { CreditCard, RefreshCw } from 'lucide-react';
import styles from './MemberPaymentPage.module.css';
import { fmtMoney } from '../../utils/Finance/financeUtils';
import {
  getMemberDuesAPI,
  submitTransactionPaymentAPI,
} from '../../services/finance-service';
import useAuthStore from '../../store/auth-store';

export default function MemberPaymentPage() {
  const currentUser = useAuthStore((state) => state.user);
  const memberId = currentUser?.memberId;
  const memberName = currentUser?.fullName || 'Thành viên';
  const memberCode = currentUser?.studentId || (memberId ? `TV${String(memberId).padStart(3, '0')}` : '');

  const [dues, setDues] = useState([]);
  const [paymentRequest, setPaymentRequest] = useState(null);
  const [paymentStep, setPaymentStep] = useState('method');
  const [selectedDueIds, setSelectedDueIds] = useState([]);
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);
  const [paymentProcessing, setPaymentProcessing] = useState(false);

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
  const processing = useMemo(() => dues.filter((due) => due.status === 'processing'), [dues]);
  const paid = useMemo(() => dues.filter((due) => due.status === 'paid'), [dues]);
  const failedDues = useMemo(() => pending.filter((due) => due.paymentFailed), [pending]);
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

  const openPaymentRequest = (duesToPay) => {
    if (!duesToPay.length) return;
    setPaymentRequest({ dues: duesToPay });
    setPaymentStep('method');
    setApiError('');
  };

  const handleOpenBulkPayment = () => {
    if (selectedDues.length === 0) return;
    openPaymentRequest(selectedDues);
  };

  const closePaymentRequest = () => {
    if (paymentProcessing) return;
    setPaymentRequest(null);
    setPaymentStep('method');
  };

  const handleConfirmPayment = async () => {
    const duesToPay = paymentRequest?.dues || [];
    if (duesToPay.length === 0) return;

    setPaymentProcessing(true);
    const results = await Promise.allSettled(
      duesToPay.map((due) => submitTransactionPaymentAPI(due.id)),
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

    setPaymentProcessing(false);

    if (failedCount > 0) {
      setApiError(`Đã ghi nhận ${completed.length}/${duesToPay.length} khoản. ${failedCount} khoản chưa thành công.`);
      setPaymentRequest({ dues: duesToPay.filter((due) => !completedIds.includes(due.id)) });
      return;
    }

    setPaymentRequest(null);
    setPaymentStep('method');
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

        {failedDues.length > 0 && (
          <div className={styles.paymentFailedNotice}>
            Có {failedDues.length} khoản thanh toán không thành công. Vui lòng kiểm tra và thanh toán lại.
          </div>
        )}

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
                  onPay={(item) => openPaymentRequest([item])}
                />
              ))}
            </div>
          </>
        )}
      </div>

      <div className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2>Chờ xác nhận</h2>
          <span className={styles.processingCountBadge}>{processing.length} khoản</span>
        </div>
        {processing.length === 0 ? (
          <div className={styles.empty}>Không có khoản nào đang chờ xác nhận.</div>
        ) : (
          <div className={styles.paidList}>
            {processing.map((due) => (
              <div key={due.id} className={styles.paidRow}>
                <div>
                  <strong>{due.lyDo}</strong>
                  <span>{due.transferCode}</span>
                </div>
                <div className={styles.paidMeta}>
                  <span>{fmtMoney(due.soTien)}</span>
                  <small>Đang chờ admin xác nhận</small>
                </div>
              </div>
            ))}
          </div>
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

      {paymentRequest?.dues?.length > 0 && paymentStep === 'method' && (
        <PaymentMethodModal
          dues={paymentRequest.dues}
          onClose={closePaymentRequest}
          onSelect={setPaymentStep}
        />
      )}

      {paymentRequest?.dues?.length > 0 && paymentStep === 'cash' && (
        <CashPaymentModal
          dues={paymentRequest.dues}
          memberName={memberName}
          memberCode={memberCode}
          processing={paymentProcessing}
          onBack={() => setPaymentStep('method')}
          onClose={closePaymentRequest}
          onConfirm={handleConfirmPayment}
        />
      )}

      {paymentRequest?.dues?.length > 0 && paymentStep === 'transfer' && (
        <QrPaymentModal
          dues={paymentRequest.dues}
          memberName={memberName}
          memberCode={memberCode}
          processing={paymentProcessing}
          onBack={() => setPaymentStep('method')}
          onClose={closePaymentRequest}
          onConfirm={handleConfirmPayment}
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
          <span className={due.paymentFailed ? styles.failedBadge : styles.pendingBadge}>
            {due.paymentFailed ? 'Không thành công' : 'Chờ đóng'}
          </span>
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

function PaymentMethodModal({ dues, onClose, onSelect }) {
  const total = dues.reduce((sum, due) => sum + Number(due.soTien || 0), 0);

  return (
    <div className={styles.modalOverlay} role="presentation" onClick={onClose}>
      <div
        className={styles.qrModal}
        role="dialog"
        aria-modal="true"
        aria-labelledby="payment-method-title"
        onClick={(event) => event.stopPropagation()}
      >
        <div className={styles.momoHeader}>
          <div>
            <span>Chọn hình thức thanh toán</span>
            <h2 id="payment-method-title">Bạn muốn thanh toán bằng cách nào?</h2>
          </div>
          <button type="button" className={styles.closeBtn} onClick={onClose} aria-label="Đóng">
            x
          </button>
        </div>

        <div className={styles.modalBody}>
          <div className={styles.methodSummary}>
            <span>{dues.length} khoản đã chọn</span>
            <strong>{fmtMoney(total)}</strong>
          </div>
          <div className={styles.methodGrid}>
            <button type="button" className={styles.methodBtn} onClick={() => onSelect('cash')}>
              <span className={styles.methodIcon}>₫</span>
              <strong>Tiền mặt</strong>
              <small>Nộp trực tiếp cho ban quản lý, sau đó chờ admin xác nhận.</small>
            </button>
            <button type="button" className={styles.methodBtn} onClick={() => onSelect('transfer')}>
              <span className={styles.methodIcon}>QR</span>
              <strong>Chuyển khoản</strong>
              <small>Hiển thị mã QR và báo đã chuyển khoản để admin kiểm tra.</small>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

function CashPaymentModal({ dues, memberName, memberCode, processing, onBack, onClose, onConfirm }) {
  return (
    <PaymentConfirmationModal
      dues={dues}
      memberName={memberName}
      memberCode={memberCode}
      processing={processing}
      title="Xác nhận nộp tiền mặt"
      subtitle="Tiền mặt"
      hint="Sau khi xác nhận, khoản này sẽ chuyển xuống Chờ xác nhận để admin kiểm tra tiền đã thu."
      confirmLabel="Tôi sẽ nộp tiền mặt"
      onBack={onBack}
      onClose={onClose}
      onConfirm={onConfirm}
    />
  );
}

function QrPaymentModal({ dues, memberName, memberCode, processing, onBack, onClose, onConfirm }) {
  return (
    <PaymentConfirmationModal
      dues={dues}
      memberName={memberName}
      memberCode={memberCode}
      processing={processing}
      title="Quét QR để nộp tiền"
      subtitle="Chuyển khoản"
      hint="Dùng ứng dụng MoMo hoặc ngân hàng để quét mã, sau đó báo đã chuyển khoản để admin xác nhận."
      confirmLabel={dues.length > 1 ? 'Đã chuyển khoản tất cả' : 'Đã chuyển khoản'}
      showQr
      onBack={onBack}
      onClose={onClose}
      onConfirm={onConfirm}
    />
  );
}

function PaymentConfirmationModal({
  dues,
  memberName,
  memberCode,
  processing,
  title,
  subtitle,
  hint,
  confirmLabel,
  showQr = false,
  onBack,
  onClose,
  onConfirm,
}) {
  const total = dues.reduce((sum, due) => sum + Number(due.soTien || 0), 0);
  const transferCodes = dues.map((due) => due.transferCode).join(', ');
  const firstDue = dues[0];

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
            <span>{subtitle}</span>
            <h2 id="qr-payment-title">{title}</h2>
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
          {showQr ? (
            <div className={styles.fakeQr} aria-label={`QR thanh toán ${transferCodes}`}>
              <span />
              <span />
              <span />
              <small>{dues.length > 1 ? `${dues.length} khoản` : firstDue.id}</small>
            </div>
          ) : (
            <div className={styles.cashPanel}>
              <strong>{fmtMoney(total)}</strong>
              <span>{dues.length} khoản chờ nộp tiền mặt</span>
            </div>
          )}
          <p className={styles.scanHint}>{hint}</p>
        </div>

        <div className={styles.modalBody}>
          <div className={styles.paymentInfo}>
            <div>
              <span>Số tiền</span>
              <strong className={styles.modalAmount}>{fmtMoney(total)}</strong>
            </div>
            <div>
              <span>Nội dung</span>
              <strong>CLB THMN | {transferCodes}</strong>
            </div>
            <div>
              <span>Mã khoản thu</span>
              <strong>{transferCodes}</strong>
            </div>
            {dues.length === 1 && firstDue.maSuKien && (
              <div>
                <span>Mã sự kiện</span>
                <strong>{firstDue.maSuKien}</strong>
              </div>
            )}
            <div>
              <span>Người nộp</span>
              <strong>{memberName} - {memberCode}</strong>
            </div>
          </div>
          {dues.length > 1 && (
            <div className={styles.bulkList}>
              {dues.map((due) => (
                <div key={due.id} className={styles.bulkItem}>
                  <span>{due.lyDo}</span>
                  <strong>{fmtMoney(due.soTien)}</strong>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className={styles.modalActions}>
          <button type="button" className={styles.secondaryBtn} onClick={onBack} disabled={processing}>
            Quay lại
          </button>
          <button type="button" className={styles.confirmBtn} onClick={onConfirm} disabled={processing}>
            {processing ? 'Đang xử lý...' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

function normalizeDueFromTransaction(transaction = {}) {
  const status = String(transaction.status || '').toUpperCase();
  if (!['PENDING', 'PROCESSING', 'COMPLETED', 'APPROVED', 'FAILED'].includes(status)) {
    return null;
  }

  const awaitingConfirmation = isAwaitingConfirmationTransaction(transaction);
  const normalizedStatus = awaitingConfirmation
    ? 'processing'
    : ['COMPLETED', 'APPROVED'].includes(status)
      ? 'paid'
      : 'pending';

  return {
    id: transaction.transactionId,
    transferCode: `${transaction.eventId || 'QUY'}-${transaction.transactionId}`,
    lyDo: normalizeDueDescription(transaction.description || 'Khoản cần đóng'),
    soTien: Number(transaction.amount || 0),
    maSuKien: transaction.eventId || '',
    status: normalizedStatus,
    paymentFailed: status === 'FAILED',
    paidAt: transaction.approvedAt || transaction.updatedAt || '',
    targetName: transaction.memberName || transaction.counterpartyName || '',
    raw: transaction,
  };
}

function isAwaitingConfirmationTransaction(transaction = {}) {
  const status = String(transaction.status || '').toUpperCase();
  return status === 'PROCESSING'
    || (status === 'PENDING' && Boolean(transaction.approvedAt) && !transaction.approvedById);
}

function normalizeDueDescription(description = '') {
  return String(description)
    .replace(/Phi tham gia su kien:/gi, 'Phí tham gia sự kiện:')
    .replace(/Phi tham gia sự kiện:/gi, 'Phí tham gia sự kiện:')
    .replace(/Phí tham gia su kien:/gi, 'Phí tham gia sự kiện:');
}
