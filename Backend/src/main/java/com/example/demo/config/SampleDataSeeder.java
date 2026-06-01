package com.example.demo.config;

import com.example.demo.domain.enums.ApprovalStatusEnum;
import com.example.demo.domain.enums.DocumentStatus;
import com.example.demo.domain.enums.EventStatusEnum;
import com.example.demo.domain.enums.GenderEnum;
import com.example.demo.domain.enums.GraduatedStatusEnum;
import com.example.demo.domain.enums.TransactionStatus;
import com.example.demo.domain.enums.TransactionType;
import com.example.demo.domain.model.audit.AuditLog;
import com.example.demo.domain.model.department.Department;
import com.example.demo.domain.model.document.Document;
import com.example.demo.domain.model.document.DocumentFile;
import com.example.demo.domain.model.document.DocumentType;
import com.example.demo.domain.model.event.Event;
import com.example.demo.domain.model.event.EventOrganizer;
import com.example.demo.domain.model.event.EventOrganizerId;
import com.example.demo.domain.model.event.EventRole;
import com.example.demo.domain.model.finance.Transaction;
import com.example.demo.domain.model.member.Member;
import com.example.demo.domain.model.notification.Notification;
import com.example.demo.domain.model.notification.NotificationRecipient;
import com.example.demo.domain.model.notification.NotificationRecipientId;
import com.example.demo.domain.model.role.Role;
import com.example.demo.domain.model.subject.Subject;
import com.example.demo.domain.model.system.SystemSetting;
import com.example.demo.domain.model.user.User;
import com.example.demo.domain.repository.audit.AuditLogRepository;
import com.example.demo.domain.repository.department.DepartmentRepository;
import com.example.demo.domain.repository.document.DocumentFileRepository;
import com.example.demo.domain.repository.document.DocumentRepository;
import com.example.demo.domain.repository.document.DocumentTypeRepository;
import com.example.demo.domain.repository.event.EventOrganizerRepository;
import com.example.demo.domain.repository.event.EventRepository;
import com.example.demo.domain.repository.event.EventRoleRepository;
import com.example.demo.domain.repository.finance.TransactionRepository;
import com.example.demo.domain.repository.member.MemberRepository;
import com.example.demo.domain.repository.notification.NotificationRecipientRepository;
import com.example.demo.domain.repository.notification.NotificationRepository;
import com.example.demo.domain.repository.role.RoleRepository;
import com.example.demo.domain.repository.subject.SubjectRepository;
import com.example.demo.domain.repository.system.SystemSettingRepository;
import com.example.demo.domain.repository.user.UserRepository;
import com.example.demo.domain.service.user.PasswordHasher;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SampleDataSeeder implements CommandLineRunner {
    private static final BigDecimal MONTHLY_FUND_AMOUNT = BigDecimal.valueOf(75_000L);
    private static final DateTimeFormatter MONTH_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final EventRoleRepository eventRoleRepository;
    private final EventRepository eventRepository;
    private final EventOrganizerRepository eventOrganizerRepository;
    private final DocumentRepository documentRepository;
    private final DocumentFileRepository documentFileRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final TransactionRepository transactionRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordHasher passwordHasher;

    public SampleDataSeeder(RoleRepository roleRepository,
                            DepartmentRepository departmentRepository,
                            SubjectRepository subjectRepository,
                            DocumentTypeRepository documentTypeRepository,
                            MemberRepository memberRepository,
                            UserRepository userRepository,
                            EventRoleRepository eventRoleRepository,
                            EventRepository eventRepository,
                            EventOrganizerRepository eventOrganizerRepository,
                            DocumentRepository documentRepository,
                            DocumentFileRepository documentFileRepository,
                            NotificationRepository notificationRepository,
                            NotificationRecipientRepository notificationRecipientRepository,
                            TransactionRepository transactionRepository,
                            SystemSettingRepository systemSettingRepository,
                            AuditLogRepository auditLogRepository,
                            PasswordHasher passwordHasher) {
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.subjectRepository = subjectRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.eventRoleRepository = eventRoleRepository;
        this.eventRepository = eventRepository;
        this.eventOrganizerRepository = eventOrganizerRepository;
        this.documentRepository = documentRepository;
        this.documentFileRepository = documentFileRepository;
        this.notificationRepository = notificationRepository;
        this.notificationRecipientRepository = notificationRecipientRepository;
        this.transactionRepository = transactionRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0
                || departmentRepository.count() > 0
                || subjectRepository.count() > 0
                || documentTypeRepository.count() > 0
                || memberRepository.count() > 0) {
            return;
        }

        List<Role> roles = seedRoles();
        List<Department> departments = seedDepartments();
        List<Subject> subjects = seedSubjects();
        List<DocumentType> documentTypes = seedDocumentTypes();
        List<Member> members = seedMembers(roles, departments);
        seedUsers(members);
        List<EventRole> eventRoles = seedEventRoles();
        List<Event> events = seedEvents(members);
        seedEventOrganizers(events, members, eventRoles);
        List<Document> documents = seedDocuments(members, subjects, documentTypes);
        seedDocumentFiles(documents);
        List<Notification> notifications = seedNotifications(members);
        seedNotificationRecipients(notifications, members);
        seedTransactions(events, members);
        seedSystemSettings(members);
        seedAuditLogs(members);
    }

    private List<Role> seedRoles() {
        List<Role> roles = List.of(
                Role.builder().roleName("Chủ nhiệm").priority(1).build(),
                Role.builder().roleName("Phó chủ nhiệm").priority(1).build(),
                Role.builder().roleName("Trưởng ban học thuật").priority(1).build(),
                Role.builder().roleName("Trưởng ban truyền thông").priority(1).build(),
                Role.builder().roleName("Thành viên").priority(10).build());
        return roleRepository.saveAll(roles);
    }

    private List<Department> seedDepartments() {
        List<Department> departments = List.of(
                Department.builder().departmentName("Công nghệ phần mềm").build(),
                Department.builder().departmentName("Khoa học máy tính").build(),
                Department.builder().departmentName("Hệ thống thông tin").build(),
                Department.builder().departmentName("Mạng máy tính & Truyền thông dữ liệu").build(),
                Department.builder().departmentName("Khoa học và Kỹ thuật thông tin").build(),
                Department.builder().departmentName("Kỹ thuật máy tính").build());
        return departmentRepository.saveAll(departments);
    }

    private List<Subject> seedSubjects() {
        List<String> names = List.of(
                "Cấu trúc rời rạc",
                "Xác suất thống kê",
                "Nhập môn lập trình",
                "Triết học Mác - Lênin",
                "Lập trình hướng đối tượng",
                "Cơ sở dữ liệu",
                "Phân tích thiết kế hệ thống",
                "Kiến trúc phần mềm",
                "An toàn thông tin",
                "Trí tuệ nhân tạo",
                "Mạng máy tính",
                "Công nghệ phần mềm nâng cao");

        List<Subject> subjects = new ArrayList<>();
        for (String name : names) {
            subjects.add(Subject.builder().subjectName(name).build());
        }
        return subjectRepository.saveAll(subjects);
    }

    private List<DocumentType> seedDocumentTypes() {
        List<String> names = List.of(
                "Giáo trình",
                "Slide bài giảng",
                "Tài liệu tham khảo",
                "Đề thi",
                "Bài tập",
                "Báo cáo mẫu",
                "Khác");

        List<DocumentType> types = new ArrayList<>();
        for (String name : names) {
            types.add(DocumentType.builder().typeName(name).build());
        }
        return documentTypeRepository.saveAll(types);
    }

    private List<Member> seedMembers(List<Role> roles, List<Department> departments) {
        Role president = roles.get(0);
        Role vicePresident = roles.get(1);
        Role academicHead = roles.get(2);
        Role communicationHead = roles.get(3);
        Role memberRole = roles.get(4);

        Department software = departments.get(0);
        Department computerScience = departments.get(1);
        Department informationSystem = departments.get(2);
        Department networking = departments.get(3);
        Department ai = departments.get(4);

        List<MemberSeed> seeds = List.of(
                new MemberSeed("22130001", "Nguyễn Minh Anh", software, "minhanh@club.local", "0901000001", GenderEnum.FEMALE, LocalDate.of(2004, 1, 15), president),
                new MemberSeed("22130002", "Trần Quốc Bảo", software, "quocbao@club.local", "0901000002", GenderEnum.MALE, LocalDate.of(2004, 2, 18), vicePresident),
                new MemberSeed("22130003", "Lê Hoàng Nam", software, "hoangnam@club.local", "0901000003", GenderEnum.MALE, LocalDate.of(2004, 3, 12), academicHead),
                new MemberSeed("22130004", "Phạm Gia Hân", software, "giahan@club.local", "0901000004", GenderEnum.FEMALE, LocalDate.of(2004, 5, 9), communicationHead),
                new MemberSeed("22130005", "Võ Đức Tài", software, "ductai@club.local", "0901000005", GenderEnum.MALE, LocalDate.of(2004, 7, 21), memberRole),
                new MemberSeed("22130006", "Hoàng Trung Kiên", software, "trungkien@club.local", "0901000006", GenderEnum.MALE, LocalDate.of(2004, 8, 3), memberRole),
                new MemberSeed("22130007", "Đặng Minh Khôi", software, "minhkhoi@club.local", "0901000007", GenderEnum.MALE, LocalDate.of(2004, 4, 27), memberRole),
                new MemberSeed("22130008", "Bùi Thị Tuyết", software, "thituyet@club.local", "0901000008", GenderEnum.FEMALE, LocalDate.of(2004, 11, 11), memberRole),
                new MemberSeed("22130009", "Nguyễn Văn Hùng", computerScience, "vanhung@club.local", "0901000009", GenderEnum.MALE, LocalDate.of(2004, 6, 14), memberRole),
                new MemberSeed("22130010", "Lê Thu Thảo", computerScience, "thuthao@club.local", "0901000010", GenderEnum.FEMALE, LocalDate.of(2005, 2, 22), memberRole),
                new MemberSeed("22130011", "Đỗ Anh Tuấn", computerScience, "anhtuan@club.local", "0901000011", GenderEnum.MALE, LocalDate.of(2004, 9, 16), memberRole),
                new MemberSeed("22130012", "Nguyễn Hoài Nam", computerScience, "hoainam@club.local", "0901000012", GenderEnum.MALE, LocalDate.of(2005, 1, 5), memberRole),
                new MemberSeed("22130013", "Phan Vân Tài", computerScience, "phanvantai@club.local", "0901000013", GenderEnum.MALE, LocalDate.of(2004, 12, 19), memberRole),
                new MemberSeed("22130014", "Vũ Thị Hà", computerScience, "vuha@club.local", "0901000014", GenderEnum.FEMALE, LocalDate.of(2005, 3, 1), memberRole),
                new MemberSeed("22130015", "Trần Thanh Sơn", informationSystem, "thanhson@club.local", "0901000015", GenderEnum.MALE, LocalDate.of(2004, 10, 6), memberRole),
                new MemberSeed("22130016", "Lý Mỹ Linh", informationSystem, "mylinh@club.local", "0901000016", GenderEnum.FEMALE, LocalDate.of(2004, 1, 30), memberRole),
                new MemberSeed("22130017", "Phạm Đức Long", informationSystem, "duclong@club.local", "0901000017", GenderEnum.MALE, LocalDate.of(2004, 8, 28), memberRole),
                new MemberSeed("22130018", "Ngô Hải Đăng", informationSystem, "haidang@club.local", "0901000018", GenderEnum.MALE, LocalDate.of(2004, 4, 8), memberRole),
                new MemberSeed("22130019", "Mai Khánh Vy", networking, "khanhvy@club.local", "0901000019", GenderEnum.FEMALE, LocalDate.of(2005, 5, 25), memberRole),
                new MemberSeed("22130020", "Đinh Quốc Cường", networking, "quoccuong@club.local", "0901000020", GenderEnum.MALE, LocalDate.of(2004, 7, 7), memberRole),
                new MemberSeed("23130021", "Huỳnh Ngọc Mai", networking, "ngocmai@club.local", "0901000021", GenderEnum.FEMALE, LocalDate.of(2005, 6, 4), memberRole),
                new MemberSeed("23130022", "Tạ Minh Quân", networking, "minhquan@club.local", "0901000022", GenderEnum.MALE, LocalDate.of(2005, 8, 13), memberRole),
                new MemberSeed("23130023", "Cao Phương Nhi", ai, "phuongnhi@club.local", "0901000023", GenderEnum.FEMALE, LocalDate.of(2005, 9, 17), memberRole),
                new MemberSeed("23130024", "Trương Nhật Minh", ai, "nhatminh@club.local", "0901000024", GenderEnum.MALE, LocalDate.of(2005, 11, 2), memberRole),
                new MemberSeed("23130025", "Đoàn Bảo Châu", ai, "baochau@club.local", "0901000025", GenderEnum.FEMALE, LocalDate.of(2005, 12, 20), memberRole),
                new MemberSeed("23130026", "Hồ Gia Bảo", software, "giabao@club.local", "0901000026", GenderEnum.MALE, LocalDate.of(2005, 3, 18), memberRole),
                new MemberSeed("23130027", "Nguyễn Hà My", computerScience, "hamy@club.local", "0901000027", GenderEnum.FEMALE, LocalDate.of(2005, 4, 9), memberRole),
                new MemberSeed("23130028", "Lâm Tuấn Kiệt", informationSystem, "tuankiet@club.local", "0901000028", GenderEnum.MALE, LocalDate.of(2005, 7, 29), memberRole),
                new MemberSeed("24130029", "Phùng Minh Khang", networking, "minhkhang@club.local", "0901000029", GenderEnum.MALE, LocalDate.of(2006, 1, 6), memberRole),
                new MemberSeed("24130030", "Đỗ Khánh Linh", ai, "khanhlinh@club.local", "0901000030", GenderEnum.FEMALE, LocalDate.of(2006, 2, 14), memberRole),
                new MemberSeed("24130031", "Nguyễn Nhật Hạ", software, "nhatha@club.local", "0901000031", GenderEnum.FEMALE, LocalDate.of(2006, 5, 23), memberRole),
                new MemberSeed("24130032", "Trần Duy Phúc", computerScience, "duyphuc@club.local", "0901000032", GenderEnum.MALE, LocalDate.of(2006, 8, 8), memberRole),
                new MemberSeed("24130033", "Lê Bảo Ngọc", informationSystem, "baongoc@club.local", "0901000033", GenderEnum.FEMALE, LocalDate.of(2006, 9, 10), memberRole),
                new MemberSeed("24130034", "Võ Minh Triết", networking, "minhtriet@club.local", "0901000034", GenderEnum.MALE, LocalDate.of(2006, 10, 12), memberRole),
                new MemberSeed("24130035", "Phạm Hoài An", ai, "hoaian@club.local", "0901000035", GenderEnum.FEMALE, LocalDate.of(2006, 11, 15), memberRole),
                new MemberSeed("24130036", "Bùi Quang Huy", software, "quanghuy@club.local", "0901000036", GenderEnum.MALE, LocalDate.of(2006, 12, 3), memberRole),
                new MemberSeed("24130037", "Ngô Thùy Dương", computerScience, "thuyduong@club.local", "0901000037", GenderEnum.FEMALE, LocalDate.of(2006, 4, 26), memberRole),
                new MemberSeed("24130038", "Đặng Quốc Việt", informationSystem, "quocviet@club.local", "0901000038", GenderEnum.MALE, LocalDate.of(2006, 6, 18), memberRole),
                new MemberSeed("24130039", "Lương Gia Huy", networking, "giahuy@club.local", "0901000039", GenderEnum.MALE, LocalDate.of(2006, 7, 21), memberRole),
                new MemberSeed("24130040", "Tô Minh Nguyệt", ai, "minhnguyet@club.local", "0901000040", GenderEnum.FEMALE, LocalDate.of(2006, 9, 28), memberRole));

        List<Member> members = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        for (int index = 0; index < seeds.size(); index++) {
            MemberSeed seed = seeds.get(index);
            Member approver = index < 2 ? null : members.get(index % 2);
            YearMonth joinedMonth = currentMonth.minusMonths(5L - (index % 6));
            int joinedDay = Math.min(joinedMonth.lengthOfMonth(), 2 + ((index * 3) % 24));
            LocalDateTime createdAt = joinedMonth.atDay(joinedDay).atTime(8 + (index % 8), (index * 11) % 60);
            members.add(Member.builder()
                    .studentId(seed.studentId())
                    .fullName(seed.fullName())
                    .department(seed.department())
                    .email(seed.email())
                    .phone(seed.phone())
                    .gender(seed.gender())
                    .dateOfBirth(seed.dateOfBirth())
                    .role(seed.role())
                    .graduatedStatus(GraduatedStatusEnum.ACTIVE)
                    .reqStatus(ApprovalStatusEnum.APPROVED)
                    .approvalNote(index < 2 ? "Tài khoản ban chủ nhiệm" : "Đã duyệt hồ sơ thành viên")
                    .approver(approver)
                    .approvalDate(createdAt.plusHours(4))
                    .createdAt(createdAt)
                    .updatedAt(createdAt.plusHours(4))
                    .build());
        }
        List<Member> savedMembers = memberRepository.saveAll(members);
        if (savedMembers.size() > 1) {
            Member seedApprover = savedMembers.get(0);
            savedMembers.get(0).setApprover(seedApprover);
            savedMembers.get(1).setApprover(seedApprover);
            return memberRepository.saveAll(savedMembers);
        }
        return savedMembers;
    }

    private void seedUsers(List<Member> members) {
        List<User> users = List.of(
                User.create(members.get(0), passwordHasher.hash("StudyHead@123")),
                User.create(members.get(1), passwordHasher.hash("EventHead@123")),
                User.create(members.get(2), passwordHasher.hash("Member01@123")),
                User.create(members.get(3), passwordHasher.hash("Member02@123")),
                User.create(members.get(4), passwordHasher.hash("Member03@123")),
                User.create(members.get(5), passwordHasher.hash("Member04@123")));
        userRepository.saveAll(users);
    }

    private List<EventRole> seedEventRoles() {
        List<EventRole> roles = List.of(
                EventRole.builder().roleId((short) 1).roleName("Trưởng ban tổ chức").build(),
                EventRole.builder().roleId((short) 2).roleName("Phó ban tổ chức").build(),
                EventRole.builder().roleId((short) 3).roleName("Hậu cần").build(),
                EventRole.builder().roleId((short) 4).roleName("Truyền thông").build(),
                EventRole.builder().roleId((short) 5).roleName("Điều phối viên").build());
        return eventRoleRepository.saveAll(roles);
    }

    private List<Event> seedEvents(List<Member> members) {
        List<Event> events = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        for (int index = 1; index <= 36; index++) {
            Member evaluator = members.get(index % 2);
            YearMonth eventMonth = currentMonth.minusMonths(5L - ((index - 1) % 6));
            int eventDay = Math.min(eventMonth.lengthOfMonth(), 4 + (((index - 1) / 6) * 4) + (index % 3));
            LocalDate eventDate = eventMonth.atDay(eventDay);
            events.add(Event.builder()
                    .eventId(String.format("EVT%03d", index))
                    .eventName("Sự kiện học thuật số " + index)
                    .location(index % 2 == 0 ? "Hội trường A" : "Phòng sinh hoạt CLB")
                    .eventDate(eventDate)
                    .startTime(eventDate.atTime(8 + (index % 3), 0))
                    .endTime(eventDate.atTime(11 + (index % 3), 30))
                    .estimatedCost(BigDecimal.valueOf(2_000_000L + index * 150_000L))
                    .capacity(50 + index * 5)
                    .organizer(index % 2 == 0 ? "Ban học thuật" : "Ban sự kiện")
                    .tag(pickEventTag(index))
                    .status(pickEventStatus(index))
                    .reqStatus(index % 4 == 0 ? ApprovalStatusEnum.PENDING : ApprovalStatusEnum.APPROVED)
                    .description("Hoạt động chuyên môn, workshop và chia sẻ học tập đợt " + index)
                    .evaluatedBy(evaluator)
                    .evaluationDate(eventDate.atTime(17, 0))
                    .evaluationContent("Đánh giá nội bộ cho sự kiện " + index)
                    .createdAt(eventDate.minusDays(7).atTime(9, 0))
                    .updatedAt(eventDate.minusDays(1).atTime(10, 0))
                    .build());
        }
        return eventRepository.saveAll(events);
    }

    private void seedEventOrganizers(List<Event> events, List<Member> members, List<EventRole> eventRoles) {
        List<EventOrganizer> organizers = new ArrayList<>();
        for (int index = 0; index < events.size(); index++) {
            Event event = events.get(index);
            Member member = members.get(index % members.size());
            EventRole role = eventRoles.get(index % eventRoles.size());
            organizers.add(EventOrganizer.builder()
                    .id(new EventOrganizerId(event.getEventId(), member.getMemberId()))
                    .event(event)
                    .member(member)
                    .role(role)
                    .build());
        }
        eventOrganizerRepository.saveAll(organizers);
    }

    private List<Document> seedDocuments(List<Member> members, List<Subject> subjects, List<DocumentType> documentTypes) {
        List<Document> documents = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        for (int index = 1; index <= 30; index++) {
            Member proposer = members.get(index % members.size());
            Member approver = members.get(index % 2);
            YearMonth documentMonth = currentMonth.minusMonths(5L - ((index - 1) % 6));
            int documentDay = Math.min(documentMonth.lengthOfMonth(), 3 + (((index - 1) / 6) * 4));
            LocalDateTime createdAt = documentMonth.atDay(documentDay).atTime(10 + (index % 6), (index * 9) % 60);
            ApprovalStatusEnum approvalStatus = pickApprovalStatus(index);
            documents.add(Document.builder()
                    .documentName("Tài liệu học tập số " + index)
                    .type(documentTypes.get((index - 1) % documentTypes.size()))
                    .subject(subjects.get((index - 1) % subjects.size()))
                    .status(pickDocumentStatus(index))
                    .reqStatus(approvalStatus)
                    .lookupFolderId(approvalStatus == ApprovalStatusEnum.APPROVED ? pickLookupFolderId(index) : null)
                    .version("1." + ((index - 1) % 4))
                    .source("https://drive.google.com/sample-doc-" + index)
                    .note("Tài liệu mẫu phục vụ kiểm thử hệ thống số " + index)
                    .proposedBy(proposer)
                    .approvedBy(index % 3 == 0 ? null : approver)
                    .approvedAt(index % 3 == 0 ? null : createdAt.plusHours(2))
                    .createdAt(createdAt)
                    .updatedAt(createdAt.plusHours(3))
                    .build());
        }
        return documentRepository.saveAll(documents);
    }

    private void seedDocumentFiles(List<Document> documents) {
        List<String> mimeTypes = List.of(
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-powerpoint",
                "application/zip",
                "image/png");

        List<DocumentFile> files = new ArrayList<>();
        for (int index = 0; index < documents.size(); index++) {
            String extension = extensionFromMimeType(mimeTypes.get(index % mimeTypes.size()));
            files.add(DocumentFile.builder()
                    .document(documents.get(index))
                    .fileUrl("https://drive.google.com/sample-file-" + (index + 1))
                    .fileName("tai-lieu-hoc-tap-" + (index + 1) + "." + extension)
                    .fileSize(500_000L + (index * 25_000L))
                    .mimeType(mimeTypes.get(index % mimeTypes.size()))
                    .uploadedAt(LocalDateTime.now().minusDays(20 - index))
                    .build());
        }
        documentFileRepository.saveAll(files);
    }

    private List<Notification> seedNotifications(List<Member> members) {
        List<Notification> notifications = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(25);
        for (int index = 1; index <= 20; index++) {
            notifications.add(Notification.builder()
                    .title("Thông báo hoạt động số " + index)
                    .content("Nội dung thông báo mẫu cho thành viên đợt " + index)
                    .sender(members.get(index % 2))
                    .targetType(index % 2 == 0 ? "ALL_MEMBERS" : "Công nghệ phần mềm")
                    .sendMethod(index % 3 == 0 ? "EMAIL" : "SYSTEM")
                    .sentAt(baseTime.plusDays(index))
                    .build());
        }
        return notificationRepository.saveAll(notifications);
    }

    private void seedNotificationRecipients(List<Notification> notifications, List<Member> members) {
        List<NotificationRecipient> recipients = new ArrayList<>();
        for (int index = 0; index < 20; index++) {
            Notification notification = notifications.get(index);
            Member member = members.get((index + 2) % members.size());
            boolean isRead = index % 2 == 0;
            recipients.add(NotificationRecipient.builder()
                    .id(new NotificationRecipientId(notification.getNotificationId(), member.getMemberId()))
                    .notification(notification)
                    .member(member)
                    .isRead(isRead)
                    .readAt(isRead ? notification.getSentAt().plusHours(6) : null)
                    .build());
        }
        notificationRecipientRepository.saveAll(recipients);
    }

    private void seedTransactions(List<Event> events, List<Member> members) {
        List<Transaction> transactions = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        for (int monthOffset = 5; monthOffset >= 0; monthOffset--) {
            YearMonth dueMonth = currentMonth.minusMonths(monthOffset);
            String description = monthlyDueDescription(dueMonth);
            for (int index = 0; index < members.size(); index++) {
                Member member = members.get(index);
                Member creator = members.get(index % 2);
                TransactionStatus status = pickMonthlyDueStatus(monthOffset, index);
                Member approver = isPaidStatus(status) ? members.get((index + 1) % 2) : null;
                int payDay = Math.min(26, 3 + ((index * 2 + monthOffset) % 24));
                LocalDateTime transactionDate = dueMonth.atDay(payDay).atTime(8 + (index % 9), (index * 7) % 60);

                transactions.add(Transaction.builder()
                        .transactionId(String.format("DUE-FUND-%s-%03d", dueMonth.format(MONTH_ID_FORMAT), member.getMemberId()))
                        .member(member)
                        .counterpartyName(member.getFullName())
                        .type(TransactionType.INCOME)
                        .amount(MONTHLY_FUND_AMOUNT)
                        .description(description)
                        .transactionDate(transactionDate)
                        .status(status)
                        .createdBy(creator)
                        .approvedBy(approver)
                        .createdAt(transactionDate)
                        .updatedAt(transactionDate.plusHours(2))
                        .approvedAt(approver == null ? null : transactionDate.plusHours(5))
                        .build());
            }
        }

        for (int index = 1; index <= events.size(); index++) {
            Event event = events.get(index - 1);
            Member owner = members.get((index + 3) % members.size());
            Member creator = members.get(index % 2);
            Member approver = members.get((index + 1) % 2);
            LocalDateTime incomeAt = event.getEventDate().minusDays(2).atTime(9 + (index % 6), (index * 5) % 60);
            LocalDateTime expenseAt = event.getEventDate().plusDays(1).atTime(10 + (index % 6), (index * 7) % 60);

            transactions.add(Transaction.builder()
                    .transactionId(String.format("TRX-EVT-%03d-IN", index))
                    .event(event)
                    .member(owner)
                    .counterpartyName(owner.getFullName())
                    .type(TransactionType.INCOME)
                    .amount(BigDecimal.valueOf(120_000L + index * 20_000L))
                    .description("Thu phí tham gia " + event.getEventName())
                    .transactionDate(incomeAt)
                    .status(index % 12 == 0 ? TransactionStatus.PENDING : TransactionStatus.COMPLETED)
                    .createdBy(creator)
                    .approvedBy(index % 12 == 0 ? null : approver)
                    .createdAt(incomeAt)
                    .updatedAt(incomeAt.plusHours(2))
                    .approvedAt(index % 12 == 0 ? null : incomeAt.plusHours(6))
                    .build());

            transactions.add(Transaction.builder()
                    .transactionId(String.format("TRX-EVT-%03d-OUT", index))
                    .event(event)
                    .member(owner)
                    .counterpartyName(pickVendorName(index))
                    .type(TransactionType.Expense)
                    .amount(BigDecimal.valueOf(450_000L + index * 65_000L))
                    .description("Chi phí tổ chức " + event.getEventName())
                    .transactionDate(expenseAt)
                    .status(index % 10 == 0 ? TransactionStatus.PENDING : TransactionStatus.COMPLETED)
                    .createdBy(creator)
                    .approvedBy(index % 10 == 0 ? null : approver)
                    .createdAt(expenseAt)
                    .updatedAt(expenseAt.plusHours(2))
                    .approvedAt(index % 10 == 0 ? null : expenseAt.plusHours(6))
                    .build());
        }
        transactionRepository.saveAll(transactions);
    }

    private void seedSystemSettings(List<Member> members) {
        List<SystemSetting> settings = List.of(
                SystemSetting.builder().settingKey("club.name").settingValue("CLB Học thuật CNTT").description("Tên hiển thị của câu lạc bộ").updatedBy(members.get(0)).updatedAt(LocalDateTime.now().minusDays(5)).build(),
                SystemSetting.builder().settingKey("member.defaultRole").settingValue("Thành viên").description("Vai trò mặc định khi duyệt thành viên").updatedBy(members.get(0)).updatedAt(LocalDateTime.now().minusDays(5)).build(),
                SystemSetting.builder().settingKey("document.approvalRequired").settingValue("true").description("Tài liệu mới cần qua duyệt").updatedBy(members.get(0)).updatedAt(LocalDateTime.now().minusDays(4)).build(),
                SystemSetting.builder().settingKey("event.autoArchiveDays").settingValue("30").description("Số ngày tự động lưu trữ sự kiện").updatedBy(members.get(1)).updatedAt(LocalDateTime.now().minusDays(4)).build(),
                SystemSetting.builder().settingKey("notification.defaultMethod").settingValue("SYSTEM").description("Kênh gửi mặc định").updatedBy(members.get(1)).updatedAt(LocalDateTime.now().minusDays(3)).build(),
                SystemSetting.builder().settingKey("finance.maxPendingDays").settingValue("14").description("Số ngày tối đa cho giao dịch chờ duyệt").updatedBy(members.get(0)).updatedAt(LocalDateTime.now().minusDays(2)).build());
        systemSettingRepository.saveAll(settings);
    }

    private void seedAuditLogs(List<Member> members) {
        List<AuditLog> logs = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(20);
        List<String> entityTypes = List.of("MEMBER", "USER", "DOCUMENT", "EVENT", "TRANSACTION");
        List<String> actions = List.of("CREATE", "UPDATE", "APPROVE", "LOGIN");
        for (int index = 1; index <= 20; index++) {
            logs.add(AuditLog.builder()
                    .entityType(entityTypes.get((index - 1) % entityTypes.size()))
                    .entityId(String.valueOf(index))
                    .actionType(actions.get((index - 1) % actions.size()))
                    .oldValue(index % 2 == 0 ? "{\"status\":\"PENDING\"}" : null)
                    .newValue("{\"status\":\"APPROVED\",\"index\":" + index + "}")
                    .performedBy(members.get(index % 2))
                    .performedAt(baseTime.plusDays(index))
                    .build());
        }
        auditLogRepository.saveAll(logs);
    }

    private EventStatusEnum pickEventStatus(int index) {
        return switch (index % 4) {
            case 0 -> EventStatusEnum.NotStarted;
            case 1 -> EventStatusEnum.InProgress;
            case 2 -> EventStatusEnum.Finished;
            default -> EventStatusEnum.Cancelled;
        };
    }

    private String pickEventTag(int index) {
        return switch (index % 5) {
            case 0 -> "TECH";
            case 1 -> "ACAD";
            case 2 -> "SOCIAL";
            case 3 -> "CERT";
            default -> "OTHER";
        };
    }

    private DocumentStatus pickDocumentStatus(int index) {
        return switch (index % 3) {
            case 0 -> DocumentStatus.WORKING;
            case 1 -> DocumentStatus.FIXING;
            default -> DocumentStatus.CANCELLED;
        };
    }

    private ApprovalStatusEnum pickApprovalStatus(int index) {
        return switch (index % 4) {
            case 0 -> ApprovalStatusEnum.PENDING;
            case 1 -> ApprovalStatusEnum.APPROVED;
            case 2 -> ApprovalStatusEnum.REJECTED;
            default -> ApprovalStatusEnum.REQUESTED_CHANGES;
        };
    }

    private String pickLookupFolderId(int index) {
        List<String> folderIds = List.of(
                "cau-truc-roi-rac",
                "xac-suat-thong-ke",
                "nhap-mon-lap-trinh",
                "triet-hoc-mac-lenin",
                "ky-thuat-phan-mem",
                "truyen-thong-da-phuong-tien",
                "he-thong-thong-tin-chuyen-nganh",
                "thuong-mai-dien-tu",
                "tri-tue-nhan-tao",
                "khoa-hoc-du-lieu",
                "mang-may-tinh-truyen-thong-du-lieu",
                "an-toan-thong-tin",
                "ky-thuat-may-tinh-chuyen-nganh",
                "thiet-ke-vi-mach");
        return folderIds.get((index - 1) % folderIds.size());
    }

    private TransactionStatus pickTransactionStatus(int index) {
        return switch (index % 5) {
            case 0 -> TransactionStatus.PENDING;
            case 1 -> TransactionStatus.APPROVED;
            case 2 -> TransactionStatus.REJECTED;
            case 3 -> TransactionStatus.COMPLETED;
            default -> TransactionStatus.CANCELLED;
        };
    }

    private TransactionStatus pickMonthlyDueStatus(int monthOffset, int memberIndex) {
        if (monthOffset == 0 && memberIndex % 7 == 0) {
            return TransactionStatus.PENDING;
        }
        if (monthOffset == 0 && memberIndex % 11 == 0) {
            return TransactionStatus.REJECTED;
        }
        return memberIndex % 5 == 0 ? TransactionStatus.APPROVED : TransactionStatus.COMPLETED;
    }

    private boolean isPaidStatus(TransactionStatus status) {
        return status == TransactionStatus.APPROVED || status == TransactionStatus.COMPLETED;
    }

    private String monthlyDueDescription(YearMonth month) {
        return String.format("Đóng quỹ tháng %02d/%d", month.getMonthValue(), month.getYear());
    }

    private String pickVendorName(int index) {
        return switch (index % 5) {
            case 0 -> "Nhà sách Đại học";
            case 1 -> "Cửa hàng Văn phòng phẩm Minh Tâm";
            case 2 -> "Dịch vụ in ấn Hồng Phát";
            case 3 -> "Trung tâm thiết bị sự kiện Sài Gòn";
            default -> "Quán nước Thanh Xuân";
        };
    }

    private String extensionFromMimeType(String mimeType) {
        return switch (mimeType) {
            case "application/pdf" -> "pdf";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx";
            case "application/vnd.ms-powerpoint" -> "ppt";
            case "application/zip" -> "zip";
            case "image/png" -> "png";
            default -> "dat";
        };
    }

    private record MemberSeed(
            String studentId,
            String fullName,
            Department department,
            String email,
            String phone,
            GenderEnum gender,
            LocalDate dateOfBirth,
            Role role) {
    }
}
