package com.example.demo.application.service.finance.impl;

import com.example.demo.application.dto.request.finance.TransactionRequest;
import com.example.demo.application.dto.response.finance.MemberDueResponse;
import com.example.demo.application.dto.response.finance.TransactionResponse;
import com.example.demo.application.mapper.finance.TransactionMapper;
import com.example.demo.application.service.notification.interfaces.NotificationDispatchService;
import com.example.demo.domain.enums.ApprovalStatusEnum;
import com.example.demo.domain.enums.TransactionStatus;
import com.example.demo.domain.enums.TransactionType;
import com.example.demo.domain.model.finance.Transaction;
import com.example.demo.domain.model.member.Member;
import com.example.demo.domain.repository.event.EventRepository;
import com.example.demo.domain.repository.finance.TransactionRepository;
import com.example.demo.domain.repository.member.MemberRepository;
import com.example.demo.domain.service.finance.TransactionDomainService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@CacheConfig(cacheNames = "transactions")
public class TransactionServiceImpl implements com.example.demo.application.service.finance.interfaces.TransactionService {
    private static final BigDecimal MONTHLY_FUND_AMOUNT = BigDecimal.valueOf(75_000L);
    private static final DateTimeFormatter MONTH_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
    private static final String TARGET_FINANCE = "FINANCE";

    private final TransactionRepository transactionRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionDomainService transactionDomainService;
    private final NotificationDispatchService notificationDispatchService;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            EventRepository eventRepository,
            MemberRepository memberRepository,
            TransactionMapper transactionMapper,
            TransactionDomainService transactionDomainService,
            NotificationDispatchService notificationDispatchService) {
        this.transactionRepository = transactionRepository;
        this.eventRepository = eventRepository;
        this.memberRepository = memberRepository;
        this.transactionMapper = transactionMapper;
        this.transactionDomainService = transactionDomainService;
        this.notificationDispatchService = notificationDispatchService;
    }

    @CacheEvict(cacheNames = {"transactions", "finance"}, allEntries = true)
    public TransactionResponse create(TransactionRequest request) {
        TransactionType type = parseTransactionType(request == null ? null : request.getType());
        transactionDomainService.validateCreateRequest(request, type);
        if (transactionRepository.existsById(request.getTransactionId())) {
            throw new IllegalArgumentException("Transaction ID already exists: " + request.getTransactionId());
        }

        var event = request.getEventId() == null || request.getEventId().isBlank() ? null
                : eventRepository.findById(request.getEventId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện: " + request.getEventId()));
        var member = request.getMemberId() == null ? null
                : memberRepository.findById(request.getMemberId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên: " + request.getMemberId()));
        var createdBy = request.getCreatedById() == null ? null
                : memberRepository.findById(request.getCreatedById())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Không tìm thấy người tạo: " + request.getCreatedById()));
        var approvedBy = request.getApprovedById() == null ? null
                : memberRepository.findById(request.getApprovedById())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Không tìm thấy người duyệt: " + request.getApprovedById()));
        var entity = transactionMapper.toEntity(request, event, member, createdBy, approvedBy);
        Transaction savedTransaction = transactionRepository.save(entity);
        notifyFinance(
                savedTransaction,
                "Khoản tài chính mới",
                "Có khoản tài chính mới: " + describeTransaction(savedTransaction) + ".",
                createdBy);
        return transactionMapper.toResponse(savedTransaction);
    }

    @CacheEvict(cacheNames = {"transactions", "finance"}, allEntries = true)
    public TransactionResponse update(String id, TransactionRequest request) {
        TransactionType type = parseTransactionType(request == null ? null : request.getType());
        transactionDomainService.validateCreateRequest(request, type);
        if (request.getTransactionId() != null && !request.getTransactionId().isBlank()
                && !id.equals(request.getTransactionId())) {
            throw new IllegalArgumentException("Transaction ID in path and body must match");
        }

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khoản tài chính: " + id));
        var event = request.getEventId() == null || request.getEventId().isBlank() ? null
                : eventRepository.findById(request.getEventId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện: " + request.getEventId()));
        var member = request.getMemberId() == null ? null
                : memberRepository.findById(request.getMemberId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên: " + request.getMemberId()));
        var createdBy = request.getCreatedById() == null ? null
                : memberRepository.findById(request.getCreatedById())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Không tìm thấy người tạo: " + request.getCreatedById()));
        var approvedBy = request.getApprovedById() == null ? null
                : memberRepository.findById(request.getApprovedById())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Không tìm thấy người duyệt: " + request.getApprovedById()));

        transaction.setEvent(event);
        transaction.setMember(member);
        transaction.setCounterpartyName(request.getCounterpartyName());
        transaction.setType(type);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate() == null ? LocalDateTime.now() : request.getTransactionDate());
        transaction.setStatus(request.getStatus() == null ? transaction.getStatus() : request.getStatus());
        transaction.setCreatedBy(createdBy);
        transaction.setApprovedBy(approvedBy);
        transaction.setApprovedAt(approvedBy == null ? null : LocalDateTime.now());

        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Cacheable(key = "'all'")
    public List<TransactionResponse> getAll() {
        return transactionRepository.findActive().stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Cacheable(key = "'type:' + #type")
    public List<TransactionResponse> getByType(String type) {
        return transactionRepository.findActiveByType(parseTransactionType(type)).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Cacheable(key = "'event:' + #eventId")
    public List<TransactionResponse> getByEvent(String eventId) {
        return transactionRepository.findActiveByEventId(eventId).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @CacheEvict(cacheNames = {"transactions", "finance"}, allEntries = true)
    public List<TransactionResponse> getByMemberDues(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID must not be empty");
        }
        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên: " + memberId));

        YearMonth currentMonth = YearMonth.now();
        ensureMonthlyDue(member, currentMonth);

        return transactionRepository.findActiveByMemberIdAndType(memberId, TransactionType.INCOME).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @CacheEvict(cacheNames = {"transactions", "finance"}, allEntries = true)
    public List<MemberDueResponse> getPendingMonthlyDues() {
        YearMonth currentMonth = YearMonth.now();
        String description = monthlyDueDescription(currentMonth);
        Map<Long, Transaction> latestMonthlyTransactionByMember = new HashMap<>();
        transactionRepository.findActiveByType(TransactionType.INCOME).stream()
                .filter(transaction -> description.equals(transaction.getDescription()))
                .forEach(transaction -> {
                    if (transaction.getMember() != null) {
                        latestMonthlyTransactionByMember.putIfAbsent(
                                transaction.getMember().getMemberId(), transaction);
                    }
                });

        return memberRepository.findByReqStatus(ApprovalStatusEnum.APPROVED).stream()
                .filter(this::isMonthlyDueMember)
                .filter(member -> !isMonthlyDuePaid(latestMonthlyTransactionByMember.get(member.getMemberId())))
                .map(member -> toMemberDueResponse(member, latestMonthlyTransactionByMember.get(member.getMemberId()), currentMonth))
                .toList();
    }

    @Cacheable(key = "'id:' + #id")
    public TransactionResponse getById(String id) {
        return transactionRepository.findById(id).map(transactionMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khoản tài chính: " + id));
    }

    @CacheEvict(cacheNames = {"transactions", "finance"}, allEntries = true)
    public TransactionResponse complete(String id, Long currentMemberId, boolean currentUserIsManager) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khoản tài chính: " + id));
        if (transaction.getType() == TransactionType.Expense && !currentUserIsManager) {
            throw new IllegalArgumentException("Chỉ có thành viên có chức vụ mới được duyệt phiếu chi");
        }
        if (transaction.getType() == TransactionType.INCOME
                && !currentUserIsManager
                && (transaction.getMember() == null
                || currentMemberId == null
                || !currentMemberId.equals(transaction.getMember().getMemberId()))) {
            throw new IllegalArgumentException("Bạn chỉ có thể xác nhận thanh toán cho khoản thu của chính mình");
        }
        Member actor = currentMemberId == null ? null : memberRepository.findById(currentMemberId).orElse(null);
        if (transaction.getType() == TransactionType.INCOME && !currentUserIsManager) {
            markAwaitingConfirmation(transaction);
        } else {
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setApprovedBy(actor);
            transaction.setApprovedAt(LocalDateTime.now());
        }
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDateTime.now());
        }
        Transaction savedTransaction = transactionRepository.save(transaction);
        notifyFinance(
                savedTransaction,
                isAwaitingConfirmation(savedTransaction)
                        ? "Thanh toán đang chờ xác nhận"
                        : "Đóng tiền thành công",
                isAwaitingConfirmation(savedTransaction)
                        ? "Khoản " + describeTransaction(savedTransaction) + " đang chờ ban quản lý xác nhận."
                        : "Khoản " + describeTransaction(savedTransaction) + " đã được hoàn tất.",
                savedTransaction.getApprovedBy());
        return transactionMapper.toResponse(savedTransaction);
    }

    @CacheEvict(cacheNames = {"transactions", "finance"}, allEntries = true)
    public TransactionResponse submitPayment(String id, Long currentMemberId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch: " + id));
        if (transaction.getType() != TransactionType.INCOME) {
            throw new IllegalArgumentException("Chỉ có thể thanh toán khoản thu");
        }
        if (transaction.getMember() == null
                || currentMemberId == null
                || !currentMemberId.equals(transaction.getMember().getMemberId())) {
            throw new IllegalArgumentException("Bạn chỉ có thể xác nhận thanh toán cho khoản thu của chính mình");
        }

        markAwaitingConfirmation(transaction);
        Transaction savedTransaction = transactionRepository.save(transaction);
        notifyFinance(
                savedTransaction,
                "Thanh toán đang chờ xác nhận",
                "Khoản " + describeTransaction(savedTransaction) + " đang chờ ban quản lý xác nhận.",
                null);
        return transactionMapper.toResponse(savedTransaction);
    }

    @CacheEvict(cacheNames = {"transactions", "finance"}, allEntries = true)
    public TransactionResponse rejectPayment(String id, Long currentMemberId, boolean currentUserIsManager) {
        if (!currentUserIsManager) {
            throw new IllegalArgumentException("Chỉ có thành viên có chức vụ mới được phép từ chối xác nhận thanh toán");
        }
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khoản tài chính: " + id));
        if (transaction.getType() != TransactionType.INCOME) {
            throw new IllegalArgumentException("Chỉ có thể từ chối xác nhận khoản thu");
        }

        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setApprovedBy(null);
        transaction.setApprovedAt(null);
        Transaction savedTransaction = transactionRepository.save(transaction);
        notifyFinance(
                savedTransaction,
                "Thanh toán không thành công",
                "Khoản " + describeTransaction(savedTransaction) + " chưa được xác nhận. Vui lòng kiểm tra lại thông tin và thử thanh toán lại.",
                currentMemberId == null ? null : memberRepository.findById(currentMemberId).orElse(null));
        return transactionMapper.toResponse(savedTransaction);
    }

    @CacheEvict(cacheNames = {"transactions", "finance"}, allEntries = true)
    public void delete(String id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khoản tài chính: " + id));
        transaction.setDeletedAt(LocalDateTime.now());
        Transaction savedTransaction = transactionRepository.save(transaction);
        notifyFinance(
                savedTransaction,
                "Giao dịch đã được xóa",
                "Giao dịch " + describeTransaction(savedTransaction) + " đã được xóa khỏi hệ thống.",
                savedTransaction.getApprovedBy());
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<List<TransactionResponse>> getAllAsync() {
        return CompletableFuture.completedFuture(getAll());
    }

    private TransactionType parseTransactionType(String type) {
        return transactionMapper.parseTransactionType(type);
    }

    private void ensureMonthlyDue(Member member, YearMonth month) {
        String description = monthlyDueDescription(month);
        boolean hasMonthlyDue = transactionRepository
                .findActiveByMemberIdAndType(member.getMemberId(), TransactionType.INCOME)
                .stream()
                .anyMatch(transaction -> description.equals(transaction.getDescription()));
        if (hasMonthlyDue) {
            return;
        }

        String transactionId = buildMonthlyDueId(month, member.getMemberId());
        Transaction monthlyDue = Transaction.builder()
                .transactionId(transactionId)
                .member(member)
                .counterpartyName(member.getFullName())
                .type(TransactionType.INCOME)
                .amount(MONTHLY_FUND_AMOUNT)
                .description(description)
                .transactionDate(month.atDay(1).atTime(12, 0))
                .status(TransactionStatus.PENDING)
                .build();
        Transaction savedDue = transactionRepository.save(monthlyDue);
        notifyFinance(
                savedDue,
                "Khoản quỹ tháng mới",
                "Bạn có khoản " + describeTransaction(savedDue) + " cần thanh toán.",
                null);
    }

    private void markAwaitingConfirmation(Transaction transaction) {
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setApprovedBy(null);
        transaction.setApprovedAt(LocalDateTime.now());
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDateTime.now());
        }
    }

    private boolean isAwaitingConfirmation(Transaction transaction) {
        return transaction != null
                && transaction.getType() == TransactionType.INCOME
                && transaction.getStatus() == TransactionStatus.PENDING
                && transaction.getApprovedBy() == null
                && transaction.getApprovedAt() != null;
    }

    private String buildMonthlyDueId(YearMonth month, Long memberId) {
        String base = String.format("DUE-FUND-%s-%d", month.format(MONTH_ID_FORMAT), memberId);
        if (!transactionRepository.existsById(base)) {
            return base;
        }
        return String.format("%s-%d", base, System.currentTimeMillis() % 10_000L);
    }

    private String monthlyDueDescription(YearMonth month) {
        return String.format("Đóng quỹ tháng %02d/%d", month.getMonthValue(), month.getYear());
    }

    private boolean isMonthlyDueMember(Member member) {
        return member.getDeletedAt() == null
                && member.getGraduatedStatus() != com.example.demo.domain.enums.GraduatedStatusEnum.GRADUATED
                && member.getGraduatedStatus() != com.example.demo.domain.enums.GraduatedStatusEnum.INACTIVE;
    }

    private boolean isMonthlyDuePaid(Transaction transaction) {
        return transaction != null
                && (transaction.getStatus() == TransactionStatus.COMPLETED
                    || transaction.getStatus() == TransactionStatus.APPROVED);
    }

    private MemberDueResponse toMemberDueResponse(Member member, Transaction transaction, YearMonth month) {
        return MemberDueResponse.builder()
                .transactionId(transaction == null ? null : transaction.getTransactionId())
                .memberId(member.getMemberId())
                .studentId(member.getStudentId())
                .memberName(member.getFullName())
                .roleName(member.getRole() == null ? "Thành viên" : member.getRole().getRoleName())
                .month(String.format("Tháng %d", month.getMonthValue()))
                .amount(transaction == null || transaction.getAmount() == null
                        ? MONTHLY_FUND_AMOUNT
                        : transaction.getAmount())
                .status(TransactionStatus.PENDING)
                .build();
    }

    private void notifyFinance(Transaction transaction, String title, String content, Member sender) {
        Member member = transaction.getMember();
        if (member == null) {
            notificationDispatchService.toManagers(title, content, TARGET_FINANCE, sender);
            return;
        }
        notificationDispatchService.toManagersAndMembers(List.of(member), title, content, TARGET_FINANCE, sender);
    }

    private String describeTransaction(Transaction transaction) {
        if (transaction.getDescription() != null && !transaction.getDescription().isBlank()) {
            return transaction.getDescription();
        }
        if (transaction.getTransactionId() != null && !transaction.getTransactionId().isBlank()) {
            return transaction.getTransactionId();
        }
        return "giao dịch tài chính";
    }
}
