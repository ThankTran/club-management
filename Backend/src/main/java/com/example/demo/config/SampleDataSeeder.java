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
                "Công nghệ phần mềm nâng cao",
                "Chính trị",
                "Anh văn");

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
                new MemberSeed("24130040", "Tô Minh Nguyệt", ai, "minhnguyet@club.local", "0901000040", GenderEnum.FEMALE, LocalDate.of(2006, 9, 28), memberRole),
                new MemberSeed("25130041", "Nguyễn Hải An", software, "haian@club.local", "0901000041", GenderEnum.MALE, LocalDate.of(2007, 1, 12), memberRole, ApprovalStatusEnum.PENDING, "Hồ sơ đăng ký mới, chờ ban quản lý xét duyệt"),
                new MemberSeed("25130042", "Trần Mỹ Duyên", computerScience, "myduyen@club.local", "0901000042", GenderEnum.FEMALE, LocalDate.of(2007, 3, 8), memberRole, ApprovalStatusEnum.PENDING, "Chờ kiểm tra minh chứng sinh viên"),
                new MemberSeed("25130043", "Lê Quốc Thịnh", informationSystem, "quocthinh@club.local", "0901000043", GenderEnum.MALE, LocalDate.of(2007, 5, 19), memberRole, ApprovalStatusEnum.PENDING, "Chờ phỏng vấn ngắn với ban học thuật"),
                new MemberSeed("25130044", "Phạm Ngọc Bích", networking, "ngocbich@club.local", "0901000044", GenderEnum.FEMALE, LocalDate.of(2007, 7, 24), memberRole, ApprovalStatusEnum.PENDING, "Chờ duyệt đơn tham gia CLB"),
                new MemberSeed("25130045", "Vũ Minh Quân", ai, "minhquan25@club.local", "0901000045", GenderEnum.MALE, LocalDate.of(2007, 10, 2), memberRole, ApprovalStatusEnum.PENDING, "Chờ bổ sung thông tin lớp sinh hoạt"));

        List<Member> members = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        for (int index = 0; index < seeds.size(); index++) {
            MemberSeed seed = seeds.get(index);
            ApprovalStatusEnum requestStatus = seed.reqStatus();
            Member approver = requestStatus == ApprovalStatusEnum.APPROVED && index >= 2 ? members.get(index % 2) : null;
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
                    .reqStatus(requestStatus)
                    .approvalNote(seed.approvalNote() != null ? seed.approvalNote() : index < 2 ? "Tài khoản ban chủ nhiệm" : "Đã duyệt hồ sơ thành viên")
                    .approver(approver)
                    .approvalDate(requestStatus == ApprovalStatusEnum.APPROVED ? createdAt.plusHours(4) : null)
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
        List<User> users = new ArrayList<>();
        for (int index = 0; index < members.size(); index++) {
            String password = switch (index) {
                case 0 -> "StudyHead@123";
                case 1 -> "EventHead@123";
                default -> String.format("Member%02d@123", index - 1);
            };
            users.add(User.create(members.get(index), passwordHasher.hash(password)));
        }
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
        LocalDate today = LocalDate.now();
        List<EventSeed> seeds = List.of(
                new EventSeed("Bootcamp Java Spring Boot cho thành viên mới", "Phòng lab A101", -120, 8, 11, 2_800_000L, 60, "Ban học thuật", "TECH", EventStatusEnum.Evaluated, ApprovalStatusEnum.APPROVED, "Ôn tập Java, REST API và quy trình xây dựng backend cho thành viên mới."),
                new EventSeed("Seminar Phương pháp nghiên cứu khoa học sinh viên", "Hội trường B", -112, 9, 11, 3_200_000L, 120, "Ban học thuật", "ACAD", EventStatusEnum.Finished, ApprovalStatusEnum.APPROVED, "Chia sẻ cách chọn đề tài, viết đề cương và trình bày kết quả nghiên cứu."),
                new EventSeed("Workshop Git và GitHub Flow", "Phòng lab B202", -104, 13, 16, 2_400_000L, 70, "Ban kỹ thuật", "TECH", EventStatusEnum.Evaluated, ApprovalStatusEnum.APPROVED, "Thực hành quản lý source code, pull request và xử lý conflict trong nhóm."),
                new EventSeed("Talkshow Định hướng AI Engineer", "Hội trường A", -96, 18, 20, 4_500_000L, 180, "Ban sự kiện", "ACAD", EventStatusEnum.Finished, ApprovalStatusEnum.APPROVED, "Giao lưu với cựu sinh viên về lộ trình học machine learning và AI engineering."),
                new EventSeed("Ngày hội kết nối thành viên học kỳ mới", "Sân trường khu C", -88, 7, 10, 5_000_000L, 250, "Ban sự kiện", "SOCIAL", EventStatusEnum.Evaluated, ApprovalStatusEnum.APPROVED, "Hoạt động làm quen, chia đội và giới thiệu các nhóm học tập của CLB."),
                new EventSeed("Lớp ôn thi Chứng chỉ MOS Excel", "Phòng máy C101", -80, 8, 11, 3_600_000L, 45, "Ban chứng chỉ", "CERT", EventStatusEnum.Finished, ApprovalStatusEnum.APPROVED, "Luyện tập các dạng bài MOS Excel và kỹ năng thao tác bảng tính."),
                new EventSeed("Mini Hackathon Ứng dụng quản lý học tập", "Innovation Lab", -72, 8, 18, 8_500_000L, 100, "Ban kỹ thuật", "TECH", EventStatusEnum.Evaluated, ApprovalStatusEnum.APPROVED, "Cuộc thi xây dựng prototype ứng dụng phục vụ học tập trong 10 giờ."),
                new EventSeed("Chuyên đề Viết báo cáo và slide học thuật", "Phòng D301", -64, 14, 16, 2_200_000L, 90, "Ban học thuật", "ACAD", EventStatusEnum.Finished, ApprovalStatusEnum.APPROVED, "Hướng dẫn cấu trúc báo cáo, thiết kế slide và cách bảo vệ kết quả."),
                new EventSeed("Chương trình mentoring đồ án môn học", "Phòng sinh hoạt CLB", -56, 18, 20, 1_800_000L, 50, "Ban học thuật", "OTHER", EventStatusEnum.Evaluated, ApprovalStatusEnum.APPROVED, "Kết nối thành viên năm trên hỗ trợ nhóm đang làm đồ án môn học."),
                new EventSeed("Workshop UI UX cho sản phẩm sinh viên", "Phòng C204", -48, 13, 16, 3_000_000L, 65, "Ban thiết kế", "TECH", EventStatusEnum.Finished, ApprovalStatusEnum.APPROVED, "Thực hành wireframe, user flow và prototype bằng Figma."),
                new EventSeed("Lớp ôn thi TOEIC đầu ra", "Phòng B105", -40, 18, 20, 3_400_000L, 80, "Ban chứng chỉ", "CERT", EventStatusEnum.Cancelled, ApprovalStatusEnum.APPROVED, "Lớp ôn tập TOEIC theo dạng đề đọc hiểu và nghe hiểu cho thành viên."),
                new EventSeed("Sinh hoạt chuyên đề Cloud Computing", "Phòng D105", -32, 9, 11, 2_700_000L, 85, "Ban kỹ thuật", "TECH", EventStatusEnum.Evaluated, ApprovalStatusEnum.APPROVED, "Giới thiệu cloud, container và cách triển khai ứng dụng cơ bản."),
                new EventSeed("Bàn tròn Chia sẻ kinh nghiệm thực tập", "Phòng sinh hoạt CLB", -24, 18, 20, 2_000_000L, 70, "Ban sự kiện", "SOCIAL", EventStatusEnum.Finished, ApprovalStatusEnum.APPROVED, "Thành viên khóa trên chia sẻ cách tìm thực tập, viết CV và phỏng vấn."),
                new EventSeed("Workshop SQL và Thiết kế cơ sở dữ liệu", "Phòng lab A102", -16, 8, 11, 2_900_000L, 75, "Ban học thuật", "TECH", EventStatusEnum.Evaluated, ApprovalStatusEnum.APPROVED, "Thực hành mô hình ERD, chuẩn hóa và viết truy vấn SQL."),
                new EventSeed("Hội thảo An toàn thông tin cơ bản", "Hội trường B", -8, 14, 17, 4_100_000L, 140, "Ban kỹ thuật", "ACAD", EventStatusEnum.Finished, ApprovalStatusEnum.APPROVED, "Giới thiệu các rủi ro bảo mật phổ biến và thực hành phòng tránh."),
                new EventSeed("Workshop React và Component Design", "Phòng lab B203", 0, 8, 11, 3_200_000L, 70, "Ban kỹ thuật", "TECH", EventStatusEnum.InProgress, ApprovalStatusEnum.APPROVED, "Xây dựng giao diện React theo component, state và props."),
                new EventSeed("Buổi review đề cương nghiên cứu khoa học", "Phòng sinh hoạt CLB", 0, 14, 16, 1_500_000L, 45, "Ban học thuật", "ACAD", EventStatusEnum.InProgress, ApprovalStatusEnum.APPROVED, "Góp ý đề cương nghiên cứu trước khi nộp cho khoa."),
                new EventSeed("Training Ban tổ chức sự kiện học thuật", "Phòng C201", 6, 18, 20, 1_900_000L, 55, "Ban sự kiện", "OTHER", EventStatusEnum.NotStarted, ApprovalStatusEnum.PENDING, "Tập huấn lập kế hoạch, điều phối nhân sự và quản lý rủi ro sự kiện."),
                new EventSeed("Seminar Data Analyst Roadmap", "Hội trường A", 12, 9, 11, 4_000_000L, 160, "Ban học thuật", "ACAD", EventStatusEnum.NotStarted, ApprovalStatusEnum.APPROVED, "Giới thiệu lộ trình học Excel, SQL, BI và Python cho data analyst."),
                new EventSeed("Lớp ôn thi Chứng chỉ FE Developer", "Phòng lab C202", 18, 13, 16, 4_600_000L, 50, "Ban chứng chỉ", "CERT", EventStatusEnum.NotStarted, ApprovalStatusEnum.APPROVED, "Ôn tập HTML, CSS, JavaScript và React cho chứng chỉ frontend."),
                new EventSeed("Cuộc thi Thuật toán hằng tháng", "Phòng máy A201", 24, 8, 11, 3_700_000L, 90, "Ban học thuật", "TECH", EventStatusEnum.NotStarted, ApprovalStatusEnum.APPROVED, "Thi lập trình giải thuật theo đội và công bố bảng xếp hạng nội bộ."),
                new EventSeed("Talkshow Kỹ năng học nhóm hiệu quả", "Phòng D302", 30, 18, 20, 2_100_000L, 100, "Ban sự kiện", "SOCIAL", EventStatusEnum.NotStarted, ApprovalStatusEnum.REQUESTED_CHANGES, "Chia sẻ cách phân công, theo dõi tiến độ và phản hồi trong nhóm học tập."),
                new EventSeed("Workshop Python cho phân tích dữ liệu", "Phòng lab C201", 36, 13, 16, 3_900_000L, 65, "Ban kỹ thuật", "TECH", EventStatusEnum.NotStarted, ApprovalStatusEnum.APPROVED, "Thực hành pandas, trực quan hóa dữ liệu và xử lý tập dữ liệu nhỏ."),
                new EventSeed("Ngày hội tài liệu và học liệu mở", "Thư viện trường", 42, 8, 10, 2_300_000L, 120, "Ban học thuật", "OTHER", EventStatusEnum.NotStarted, ApprovalStatusEnum.APPROVED, "Giới thiệu kho tài liệu, quy trình đóng góp và chuẩn hóa học liệu."),
                new EventSeed("Lớp ôn thi IELTS Foundation", "Phòng B204", 48, 18, 20, 4_200_000L, 45, "Ban chứng chỉ", "CERT", EventStatusEnum.NotStarted, ApprovalStatusEnum.PENDING, "Lớp nền tảng IELTS cho thành viên cần chuẩn bị chứng chỉ ngoại ngữ."),
                new EventSeed("Hackathon Giải pháp số cho CLB", "Innovation Lab", 54, 8, 18, 9_800_000L, 110, "Ban kỹ thuật", "TECH", EventStatusEnum.NotStarted, ApprovalStatusEnum.APPROVED, "Phát triển sản phẩm số hỗ trợ quản lý thành viên, tài liệu và sự kiện."),
                new EventSeed("Chuyên đề Machine Learning ứng dụng", "Hội trường B", 60, 14, 17, 5_200_000L, 150, "Ban học thuật", "ACAD", EventStatusEnum.NotStarted, ApprovalStatusEnum.APPROVED, "Trình bày quy trình xây dựng mô hình học máy và đánh giá kết quả."),
                new EventSeed("Gala tổng kết hoạt động học thuật", "Sân khấu hội trường A", 66, 18, 21, 7_500_000L, 220, "Ban sự kiện", "SOCIAL", EventStatusEnum.NotStarted, ApprovalStatusEnum.APPROVED, "Tổng kết các hoạt động học thuật, vinh danh nhóm học tập tích cực."),
                new EventSeed("Workshop DevOps và CI CD cơ bản", "Phòng lab B201", 72, 8, 11, 3_800_000L, 70, "Ban kỹ thuật", "TECH", EventStatusEnum.NotStarted, ApprovalStatusEnum.REJECTED, "Giới thiệu pipeline, kiểm thử tự động và triển khai ứng dụng mẫu."),
                new EventSeed("Diễn đàn Sinh viên nghiên cứu và khởi nghiệp", "Hội trường lớn", 78, 9, 12, 6_800_000L, 260, "Ban học thuật", "ACAD", EventStatusEnum.NotStarted, ApprovalStatusEnum.APPROVED, "Kết nối nhóm nghiên cứu, mentor và các ý tưởng sản phẩm từ đồ án sinh viên."));

        for (int index = 0; index < seeds.size(); index++) {
            EventSeed seed = seeds.get(index);
            int eventNumber = index + 1;
            Member evaluator = members.get(eventNumber % 2);
            LocalDate eventDate = today.plusDays(seed.dayOffset());
            LocalDateTime createdAt = seed.dayOffset() <= 0
                    ? eventDate.minusDays(18).atTime(9 + (eventNumber % 4), (eventNumber * 7) % 60)
                    : today.minusDays(20L - (eventNumber % 10)).atTime(9 + (eventNumber % 4), (eventNumber * 7) % 60);
            LocalDateTime updatedAt = seed.status() == EventStatusEnum.Cancelled
                    ? eventDate.minusDays(5).atTime(16, 30)
                    : seed.dayOffset() <= 0
                            ? eventDate.minusDays(1).atTime(15, (eventNumber * 5) % 60)
                            : today.minusDays(eventNumber % 5).atTime(15, (eventNumber * 5) % 60);
            boolean hasEvaluation = seed.status() == EventStatusEnum.Evaluated || seed.status() == EventStatusEnum.Finished;
            events.add(Event.builder()
                    .eventId(String.format("EVT%03d", eventNumber))
                    .eventName(seed.name())
                    .location(seed.location())
                    .eventDate(eventDate)
                    .startTime(eventDate.atTime(seed.startHour(), 0))
                    .endTime(eventDate.atTime(seed.endHour(), 0))
                    .estimatedCost(BigDecimal.valueOf(seed.estimatedCost()))
                    .capacity(seed.capacity())
                    .tag(seed.tag())
                    .status(seed.status())
                    .reqStatus(seed.reqStatus())
                    .organizer(seed.organizer())
                    .description(seed.description())
                    .evaluatedBy(hasEvaluation ? evaluator : null)
                    .evaluationDate(hasEvaluation ? eventDate.plusDays(1).atTime(17, 0) : null)
                    .evaluationContent(hasEvaluation ? "Tổng kết: " + seed.name() + " đạt mục tiêu chuyên môn và ghi nhận phản hồi để cải tiến lần sau." : null)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
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
        List<ResourceFolderSeed> folders = resourceFolderSeeds();
        List<String> titlePrefixes = List.of(
                "Giáo trình tổng hợp",
                "Slide bài giảng",
                "Bộ bài tập thực hành",
                "Đề cương ôn tập cuối kỳ",
                "Ngân hàng câu hỏi trắc nghiệm");

        int index = 1;
        LocalDateTime baseTime = LocalDateTime.now().minusDays(120);
        for (ResourceFolderSeed folder : folders) {
            for (int item = 0; item < titlePrefixes.size(); item++) {
                Member proposer = members.get(index % members.size());
                Member approver = members.get((index + 1) % 2);
                LocalDateTime createdAt = baseTime.plusDays(index).withHour(8 + (index % 8)).withMinute((index * 7) % 60);
            documents.add(Document.builder()
                    .documentName(titlePrefixes.get(item) + " - " + folder.label())
                    .type(documentTypes.get(item % documentTypes.size()))
                    .subject(findSubjectByName(subjects, folder.subjectName()))
                    .status(DocumentStatus.WORKING)
                    .reqStatus(ApprovalStatusEnum.APPROVED)
                    .lookupFolderId(folder.folderId())
                    .version("2." + item)
                    .source("https://drive.google.com/drive/folders/seed-" + folder.folderId() + "/document-" + (item + 1))
                    .note("Tài liệu học tập đã duyệt cho thư mục " + folder.label() + ".")
                    .proposedBy(proposer)
                    .approvedBy(approver)
                    .approvedAt(createdAt.plusHours(4))
                    .createdAt(createdAt)
                    .updatedAt(createdAt.plusDays(2))
                    .build());
                index++;
            }
        }
        seedDocumentReviewQueue(documents, subjects, documentTypes, members, index);
        return documentRepository.saveAll(documents);
    }

    private void seedDocumentFiles(List<Document> documents) {
        List<String> mimeTypes = List.of(
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-powerpoint",
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        List<DocumentFile> files = new ArrayList<>();
        for (int index = 0; index < documents.size(); index++) {
            Document document = documents.get(index);
            String extension = extensionFromMimeType(mimeTypes.get(index % mimeTypes.size()));
            files.add(DocumentFile.builder()
                    .document(document)
                    .fileUrl("https://drive.google.com/file/d/seed-document-" + document.getDocumentId() + "/view")
                    .fileName("tai-lieu-" + document.getDocumentId() + "." + extension)
                    .fileSize(650_000L + (index * 18_000L))
                    .mimeType(mimeTypes.get(index % mimeTypes.size()))
                    .uploadedAt(LocalDateTime.now().minusDays(Math.max(1, documents.size() - index)))
                    .build());
        }
        documentFileRepository.saveAll(files);
    }

    private void seedDocumentReviewQueue(
            List<Document> documents,
            List<Subject> subjects,
            List<DocumentType> documentTypes,
            List<Member> members,
            int startIndex) {
        List<DocumentReviewSeed> seeds = List.of(
                new DocumentReviewSeed("Đề xuất cập nhật slide Luật sở hữu trí tuệ", "Chính trị", 1, ApprovalStatusEnum.REQUESTED_CHANGES, DocumentStatus.FIXING, "Cần bổ sung phần trích dẫn văn bản pháp luật mới nhất."),
                new DocumentReviewSeed("Bản sửa giáo trình Anh văn 2 - Unit 5 Presentation", "Anh văn", 0, ApprovalStatusEnum.REQUESTED_CHANGES, DocumentStatus.FIXING, "Cần chuẩn hóa định dạng bài tập nghe và đáp án."),
                new DocumentReviewSeed("Bộ bài tập SQL nâng cao bản chỉnh sửa", "Cơ sở dữ liệu", 4, ApprovalStatusEnum.REQUESTED_CHANGES, DocumentStatus.FIXING, "Cần thêm dữ liệu mẫu cho phần truy vấn lồng nhau."),
                new DocumentReviewSeed("Tài liệu thực hành React Hooks bản cập nhật", "Công nghệ phần mềm nâng cao", 2, ApprovalStatusEnum.REQUESTED_CHANGES, DocumentStatus.FIXING, "Cần tách rõ ví dụ useEffect và useMemo."),
                new DocumentReviewSeed("Đề xuất tài liệu nhập môn Python cho thành viên mới", "Nhập môn lập trình", 2, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Tài liệu mới chờ admin duyệt thêm vào kho."),
                new DocumentReviewSeed("Đề xuất ngân hàng câu hỏi Xác suất thống kê", "Xác suất thống kê", 4, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Bộ câu hỏi trắc nghiệm phục vụ ôn tập giữa kỳ."),
                new DocumentReviewSeed("Đề xuất slide An toàn thông tin web cơ bản", "An toàn thông tin", 1, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Slide chuyên đề bảo mật web cho buổi sinh hoạt CLB."),
                new DocumentReviewSeed("Đề xuất tài liệu IELTS Reading Foundation", "Anh văn", 0, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Tài liệu ngoại ngữ mới cho nhóm luyện chứng chỉ."));

        LocalDateTime baseTime = LocalDateTime.now().minusDays(14);
        for (int item = 0; item < seeds.size(); item++) {
            DocumentReviewSeed seed = seeds.get(item);
            int index = startIndex + item;
            Member proposer = members.get(index % members.size());
            Member reviewer = members.get((index + 1) % 2);
            LocalDateTime createdAt = baseTime.plusDays(item).withHour(9 + (item % 5)).withMinute((item * 11) % 60);
            documents.add(Document.builder()
                    .documentName(seed.name())
                    .type(documentTypes.get(seed.typeIndex() % documentTypes.size()))
                    .subject(findSubjectByName(subjects, seed.subjectName()))
                    .status(seed.documentStatus())
                    .reqStatus(seed.reqStatus())
                    .lookupFolderId(null)
                    .version("1.0")
                    .source("https://drive.google.com/drive/folders/review-queue/document-" + index)
                    .note(seed.note())
                    .proposedBy(proposer)
                    .approvedBy(seed.reqStatus() == ApprovalStatusEnum.REQUESTED_CHANGES ? reviewer : null)
                    .approvedAt(null)
                    .createdAt(createdAt)
                    .updatedAt(createdAt.plusHours(6))
                    .build());
        }
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
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx";
            case "application/zip" -> "zip";
            case "image/png" -> "png";
            default -> "dat";
        };
    }

    private Subject findSubjectByName(List<Subject> subjects, String subjectName) {
        return subjects.stream()
                .filter(subject -> subject.getSubjectName().equalsIgnoreCase(subjectName))
                .findFirst()
                .orElse(subjects.get(Math.floorMod(subjectName.hashCode(), subjects.size())));
    }

    private List<ResourceFolderSeed> resourceFolderSeeds() {
        return List.of(
                new ResourceFolderSeed("tu-tuong-ho-chi-minh", "Tư tưởng Hồ Chí Minh", "Chính trị"),
                new ResourceFolderSeed("triet-hoc-mac-lenin", "Triết học Mác - Lênin", "Chính trị"),
                new ResourceFolderSeed("kinh-te-chinh-tri", "Kinh tế Chính trị Mác - Lênin", "Chính trị"),
                new ResourceFolderSeed("chu-nghia-xa-hoi-khoa-hoc", "Chủ nghĩa xã hội khoa học", "Chính trị"),
                new ResourceFolderSeed("lich-su-dang", "Lịch sử Đảng Cộng sản Việt Nam", "Chính trị"),
                new ResourceFolderSeed("phap-luat-dai-cuong", "Pháp luật đại cương", "Chính trị"),
                new ResourceFolderSeed("giai-tich", "Giải tích", "Xác suất thống kê"),
                new ResourceFolderSeed("dai-so-tuyen-tinh", "Đại số tuyến tính", "Xác suất thống kê"),
                new ResourceFolderSeed("cau-truc-roi-rac", "Cấu trúc rời rạc", "Cấu trúc rời rạc"),
                new ResourceFolderSeed("xac-suat-thong-ke", "Xác suất thống kê", "Xác suất thống kê"),
                new ResourceFolderSeed("nhap-mon-lap-trinh", "Nhập môn lập trình", "Nhập môn lập trình"),
                new ResourceFolderSeed("anh-van-1", "Anh văn 1", "Anh văn"),
                new ResourceFolderSeed("anh-van-2", "Anh văn 2", "Anh văn"),
                new ResourceFolderSeed("anh-van-3", "Anh văn 3", "Anh văn"),
                new ResourceFolderSeed("ky-thuat-phan-mem", "Kỹ thuật phần mềm", "Kiến trúc phần mềm"),
                new ResourceFolderSeed("truyen-thong-da-phuong-tien", "Truyền thông đa phương tiện", "Công nghệ phần mềm nâng cao"),
                new ResourceFolderSeed("he-thong-thong-tin-chuyen-nganh", "Hệ thống thông tin", "Phân tích thiết kế hệ thống"),
                new ResourceFolderSeed("thuong-mai-dien-tu", "Thương mại điện tử", "Cơ sở dữ liệu"),
                new ResourceFolderSeed("khoa-hoc-may-tinh-chuyen-nganh", "Khoa học máy tính", "Trí tuệ nhân tạo"),
                new ResourceFolderSeed("tri-tue-nhan-tao", "Trí tuệ nhân tạo", "Trí tuệ nhân tạo"),
                new ResourceFolderSeed("cong-nghe-thong-tin", "Công nghệ thông tin", "Công nghệ phần mềm nâng cao"),
                new ResourceFolderSeed("khoa-hoc-du-lieu", "Khoa học dữ liệu", "Trí tuệ nhân tạo"),
                new ResourceFolderSeed("an-toan-thong-tin", "An toàn thông tin", "An toàn thông tin"),
                new ResourceFolderSeed("mang-may-tinh-truyen-thong-du-lieu", "Mạng máy tính và truyền thông dữ liệu", "Mạng máy tính"),
                new ResourceFolderSeed("ky-thuat-may-tinh-chuyen-nganh", "Kỹ thuật máy tính", "Mạng máy tính"),
                new ResourceFolderSeed("thiet-ke-vi-mach", "Thiết kế vi mạch", "Kiến trúc phần mềm"));
    }

    private record MemberSeed(
            String studentId,
            String fullName,
            Department department,
            String email,
            String phone,
            GenderEnum gender,
            LocalDate dateOfBirth,
            Role role,
            ApprovalStatusEnum reqStatus,
            String approvalNote) {
        private MemberSeed(
                String studentId,
                String fullName,
                Department department,
                String email,
                String phone,
                GenderEnum gender,
                LocalDate dateOfBirth,
                Role role) {
            this(studentId, fullName, department, email, phone, gender, dateOfBirth, role, ApprovalStatusEnum.APPROVED, null);
        }
    }

    private record EventSeed(
            String name,
            String location,
            int dayOffset,
            int startHour,
            int endHour,
            long estimatedCost,
            int capacity,
            String organizer,
            String tag,
            EventStatusEnum status,
            ApprovalStatusEnum reqStatus,
            String description) {
    }

    private record ResourceFolderSeed(
            String folderId,
            String label,
            String subjectName) {
    }

    private record DocumentReviewSeed(
            String name,
            String subjectName,
            int typeIndex,
            ApprovalStatusEnum reqStatus,
            DocumentStatus documentStatus,
            String note) {
    }
}
