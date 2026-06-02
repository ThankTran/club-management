package com.example.demo.application.service.document.impl;

import com.example.demo.application.dto.request.document.DocumentApprovalRequest;
import com.example.demo.application.dto.request.document.DocumentRequest;
import com.example.demo.application.dto.response.document.DocumentResponse;
import com.example.demo.application.exception.BusinessException;
import com.example.demo.application.mapper.document.DocumentMapper;
import com.example.demo.application.service.document.interfaces.DocumentService;
import com.example.demo.application.service.notification.interfaces.NotificationDispatchService;
import com.example.demo.domain.enums.ApprovalStatusEnum;
import com.example.demo.domain.model.document.Document;
import com.example.demo.domain.model.document.DocumentType;
import com.example.demo.domain.model.member.Member;
import com.example.demo.domain.model.subject.Subject;
import com.example.demo.domain.repository.document.DocumentFileRepository;
import com.example.demo.domain.repository.document.DocumentRepository;
import com.example.demo.domain.repository.document.DocumentTypeRepository;
import com.example.demo.domain.repository.member.MemberRepository;
import com.example.demo.domain.repository.subject.SubjectRepository;
import com.example.demo.domain.service.document.DocumentDomainService;
import com.example.demo.domain.service.member.MemberDomainService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@CacheConfig(cacheNames = "documents")
public class DocumentServiceImpl implements DocumentService {
    private static final String TARGET_DOCUMENT = "DOCUMENT";
    private static final Set<String> LOOKUP_FOLDER_IDS = Set.of(
            "tu-tuong-ho-chi-minh",
            "triet-hoc-mac-lenin",
            "kinh-te-chinh-tri",
            "chu-nghia-xa-hoi-khoa-hoc",
            "lich-su-dang",
            "phap-luat-dai-cuong",
            "giai-tich",
            "dai-so-tuyen-tinh",
            "cau-truc-roi-rac",
            "xac-suat-thong-ke",
            "nhap-mon-lap-trinh",
            "anh-van-1",
            "anh-van-2",
            "anh-van-3",
            "ky-thuat-phan-mem",
            "truyen-thong-da-phuong-tien",
            "he-thong-thong-tin-chuyen-nganh",
            "thuong-mai-dien-tu",
            "khoa-hoc-may-tinh-chuyen-nganh",
            "tri-tue-nhan-tao",
            "cong-nghe-thong-tin",
            "khoa-hoc-du-lieu",
            "an-toan-thong-tin",
            "mang-may-tinh-truyen-thong-du-lieu",
            "ky-thuat-may-tinh-chuyen-nganh",
            "thiet-ke-vi-mach");

    private final DocumentRepository documentRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final SubjectRepository subjectRepository;
    private final MemberRepository memberRepository;
    private final DocumentFileRepository documentFileRepository;
    private final DocumentMapper documentMapper;
    private final DocumentDomainService documentDomainService;
    private final MemberDomainService memberDomainService;
    private final NotificationDispatchService notificationDispatchService;

    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            DocumentTypeRepository documentTypeRepository,
            SubjectRepository subjectRepository,
            MemberRepository memberRepository,
            DocumentFileRepository documentFileRepository,
            DocumentMapper documentMapper,
            DocumentDomainService documentDomainService,
            MemberDomainService memberDomainService,
            NotificationDispatchService notificationDispatchService) {
        this.documentRepository = documentRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.subjectRepository = subjectRepository;
        this.memberRepository = memberRepository;
        this.documentFileRepository = documentFileRepository;
        this.documentMapper = documentMapper;
        this.documentDomainService = documentDomainService;
        this.memberDomainService = memberDomainService;
        this.notificationDispatchService = notificationDispatchService;
    }

    @Override
    @CacheEvict(allEntries = true)
    public DocumentResponse create(DocumentRequest request) {
        documentDomainService.validateCreateRequest(request);
        documentDomainService.validateDocumentUniqueness(
                request.getDocumentName(),
                request.getTypeId(),
                request.getSubjectId(),
                documentRepository.existsByDocumentNameIgnoreCaseAndTypeTypeIdAndSubjectSubjectId(
                        request.getDocumentName(),
                        request.getTypeId(),
                        request.getSubjectId()));

        DocumentType type = documentTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy loại tài liệu: " + request.getTypeId()));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy chủ đề: " + request.getSubjectId()));
        Member proposedBy = memberRepository.findById(request.getProposedById())
                .orElseThrow(() -> new BusinessException("Không tìm thấy thành viên đề xuất: " + request.getProposedById()));

        documentDomainService.validateProposer(proposedBy);

        Document document = documentMapper.toEntity(request, type, subject, proposedBy);
        Document savedDocument = documentRepository.save(document);
        notificationDispatchService.toManagers(
                "Tài liệu mới cần duyệt",
                proposedBy.getFullName() + " vừa đề xuất tài liệu " + savedDocument.getDocumentName() + ".",
                TARGET_DOCUMENT,
                proposedBy);
        return toResponseWithPrimaryFile(savedDocument);
    }

    @Override
    @Cacheable(key = "'all'")
    public List<DocumentResponse> getAll() {
        return documentRepository.findAll().stream().map(this::toResponseWithPrimaryFile).toList();
    }

    @Override
    @Cacheable(key = "'filters:' + (#reqStatus ?: '') + '|' + (#lookupFolderId ?: '') + '|' + (#typeId ?: '') + '|' + (#subjectId ?: '') + '|' + (#name ?: '')")
    public List<DocumentResponse> getAll(String reqStatus, String lookupFolderId, Integer typeId, Integer subjectId, String name) {
        ApprovalStatusEnum status = parseApprovalStatus(reqStatus);
        String normalizedFolderId = normalizeBlank(lookupFolderId);
        String normalizedName = normalizeBlank(name);
        return documentRepository.findWithFilters(status, normalizedFolderId, typeId, subjectId, normalizedName)
                .stream()
                .map(this::toResponseWithPrimaryFile)
                .toList();
    }

    @Override
    @CacheEvict(allEntries = true)
    public DocumentResponse approve(DocumentApprovalRequest request) {
        validateApprovalRequest(request);
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài liệu: " + request.getDocumentId()));

        Member approver = memberRepository.findById(request.getApprovedBy())
                .orElseThrow(() -> new BusinessException("Không tìm thấy người duyệt: " + request.getApprovedBy()));
        if (request.getStatus() != ApprovalStatusEnum.REQUESTED_CHANGES) {
            memberDomainService.validateApproverPermission(approver);
        }

        document.setReqStatus(request.getStatus());
        document.setApprovedBy(approver);
        document.setNote(request.getNote());
        if (request.getStatus() == ApprovalStatusEnum.APPROVED) {
            document.setApprovedAt(LocalDateTime.now());
            document.setLookupFolderId(request.getLookupFolderId());
        } else {
            document.setLookupFolderId(null);
            if (request.getStatus() == ApprovalStatusEnum.REQUESTED_CHANGES && document.getApprovedAt() == null) {
                document.setApprovedAt(null);
            }
        }

        Document savedDocument = documentRepository.save(document);
        if (request.getStatus() == ApprovalStatusEnum.APPROVED) {
            notificationDispatchService.toApprovedActiveMembers(
                    "Tài liệu đã được duyệt",
                    "Tài liệu " + savedDocument.getDocumentName() + " đã được duyệt và có thể tra cứu.",
                    TARGET_DOCUMENT,
                    approver);
        } else if (request.getStatus() == ApprovalStatusEnum.REQUESTED_CHANGES) {
            notificationDispatchService.toManagers(
                    "Tài liệu cần chỉnh sửa",
                    "Tài liệu " + savedDocument.getDocumentName() + " đang chờ duyệt chỉnh sửa.",
                    TARGET_DOCUMENT,
                    approver);
        } else {
            notificationDispatchService.toMembers(
                    List.of(savedDocument.getProposedBy()),
                    "Tài liệu đã bị từ chối",
                    "Tài liệu " + savedDocument.getDocumentName() + " đã bị từ chối.",
                    TARGET_DOCUMENT,
                    approver);
        }

        return toResponseWithPrimaryFile(savedDocument);
    }

    @Override
    @CacheEvict(allEntries = true)
    public DocumentResponse moveLookupFolder(Long documentId, String lookupFolderId) {
        if (documentId == null) {
            throw new BusinessException("Document id is required");
        }

        String normalizedFolderId = normalizeBlank(lookupFolderId);
        if (normalizedFolderId == null || !LOOKUP_FOLDER_IDS.contains(normalizedFolderId)) {
            throw new BusinessException("Thư mục tra cứu không hợp lệ. Vui lòng chọn một trong các thư mục hợp lệ.");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài liệu: " + documentId));

        document.setLookupFolderId(normalizedFolderId);
        return toResponseWithPrimaryFile(documentRepository.save(document));
    }

    @Override
    @Cacheable(key = "'name:' + #documentName")
    public List<DocumentResponse> searchByName(String documentName) {
        return documentRepository.searchByName(documentName)
                .stream().map(this::toResponseWithPrimaryFile).toList();
    }

    @Override
    @Cacheable(key = "'subject:' + #subjectId")
    public List<DocumentResponse> getBySubject(Integer subjectId) {
        return documentRepository.findBySubjectId(subjectId)
                .stream().map(this::toResponseWithPrimaryFile).toList();
    }

    @Override
    @Cacheable(key = "'type:' + #typeId")
    public List<DocumentResponse> getByType(Integer typeId) {
        return documentRepository.findByTypeId(typeId)
                .stream().map(this::toResponseWithPrimaryFile).toList();
    }

    @Override
    @Cacheable(key = "'id:' + #id")
    public DocumentResponse getById(Long id) {
        return documentRepository.findById(id)
                .map(this::toResponseWithPrimaryFile)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài liệu: " + id));
    }

    @Override
    @CacheEvict(allEntries = true)
    public void softDeleteById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài liệu: " + id));
        documentRepository.softDeleteById(id);
        if (document.getReqStatus() == ApprovalStatusEnum.APPROVED) {
            notificationDispatchService.toApprovedActiveMembers(
                    "Tài liệu đã được xóa",
                    "Tài liệu " + document.getDocumentName() + " đã được xóa khỏi kho tài liệu.",
                    TARGET_DOCUMENT,
                    null);
        } else {
            notificationDispatchService.toManagersAndMembers(
                    List.of(document.getProposedBy()),
                    "Tài liệu đã được xóa",
                    "Tài liệu " + document.getDocumentName() + " đã được xóa khỏi hệ thống.",
                    TARGET_DOCUMENT,
                    null);
        }
    }

    @Override
    @CacheEvict(allEntries = true)
    public void hardDeleteById(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new BusinessException("Không tìm thấy tài liệu: " + id);
        }
        if (documentFileRepository.existsByDocumentDocumentId(id)) {
            throw new BusinessException(
                    "Cannot delete document because it already has files. Please remove related document files first.");
        }
        documentRepository.deleteById(id);
    }

    @Override
    @Async("applicationTaskExecutor")
    public CompletableFuture<List<DocumentResponse>> getAllAsync() {
        return CompletableFuture.completedFuture(getAll());
    }

    @Override
    @Async("applicationTaskExecutor")
    public CompletableFuture<DocumentResponse> getByIdAsync(Long id) {
        return CompletableFuture.completedFuture(getById(id));
    }

    private DocumentResponse toResponseWithPrimaryFile(Document document) {
        return documentMapper.toResponse(
                document,
                documentFileRepository.findFirstByDocumentDocumentIdOrderByUploadedAtDesc(document.getDocumentId())
                        .orElse(null));
    }

    private void validateApprovalRequest(DocumentApprovalRequest request) {
        if (request == null) {
            throw new BusinessException("Approval request must not be empty");
        }
        if (request.getDocumentId() == null) {
            throw new BusinessException("Document id is required");
        }
        if (request.getApprovedBy() == null) {
            throw new BusinessException("Approver is required");
        }
        if (request.getStatus() == null
                || (request.getStatus() != ApprovalStatusEnum.APPROVED
                && request.getStatus() != ApprovalStatusEnum.REJECTED
                && request.getStatus() != ApprovalStatusEnum.REQUESTED_CHANGES)) {
            throw new BusinessException("Approval status must be APPROVED, REJECTED or REQUESTED_CHANGES");
        }
        if (request.getStatus() == ApprovalStatusEnum.APPROVED) {
            String folderId = normalizeBlank(request.getLookupFolderId());
            if (folderId == null || !LOOKUP_FOLDER_IDS.contains(folderId)) {
                throw new BusinessException("Thư mục tra cứu không hợp lệ. Vui lòng chọn một trong các thư mục hợp lệ.");
            }
            request.setLookupFolderId(folderId);
        } else if (request.getStatus() == ApprovalStatusEnum.REQUESTED_CHANGES) {
            String note = normalizeBlank(request.getNote());
            if (note == null) {
                throw new BusinessException("Lý do yêu cầu chỉnh sửa không được để trống");
            }
            request.setNote(note);
        }
    }

    private ApprovalStatusEnum parseApprovalStatus(String reqStatus) {
        String normalized = normalizeBlank(reqStatus);
        if (normalized == null) {
            return null;
        }
        try {
            return ApprovalStatusEnum.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Trạng thái duyệt không hợp lệ: " + reqStatus);
        }
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
