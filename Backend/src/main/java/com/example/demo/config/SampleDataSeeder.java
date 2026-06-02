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
import com.example.demo.domain.model.event.EventRegistration;
import com.example.demo.domain.model.event.EventRegistrationId;
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
import com.example.demo.domain.repository.event.EventRegistrationRepository;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final EventRegistrationRepository eventRegistrationRepository;
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
                            EventRegistrationRepository eventRegistrationRepository,
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
        this.eventRegistrationRepository = eventRegistrationRepository;
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
            normalizeSeedDocumentTimeline();
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
        List<EventRegistration> eventRegistrations = seedEventRegistrations(events, members);
        List<Document> documents = seedDocuments(members, subjects, documentTypes);
        List<Notification> notifications = seedNotifications(members);
        seedNotificationRecipients(notifications, members);
        seedTransactions(events, members, eventRegistrations);
        seedSystemSettings(members);
        seedAuditLogs(members, events);
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
                "Triết học Mac - Lenin",
                "Lập trình hướng đối tượng",
                "Cơ sở dữ liệu",
                "Phân tích thiết kế hệ thống",
                "Kiến trúc phần mềm",
                "An toàn thông tin",
                "Trí tuệ nhân tạo",
                "Mạng máy tính",
                "Công nghệ phần mềm nâng cao",
                "Chính trị",
                "Anh văn chuyên ngành");

        List<Subject> subjects = new ArrayList<>();
        for (String name : names) {
            subjects.add(Subject.builder().subjectName(name).build());
        }
        return subjectRepository.saveAll(subjects);
    }

    private List<DocumentType> seedDocumentTypes() {
        List<String> names = List.of(
                "Giáo trình môn học",
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
        Department computerEngineering = departments.get(5);

        List<MemberSeed> seeds = List.of(
                        // ── Khóa 22 (ĐÃ DUYỆT) ──────────────────────────────────────────────────
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
                        new MemberSeed("22130013", "Phan Văn Tài", computerScience, "phanvantai@club.local", "0901000013", GenderEnum.MALE, LocalDate.of(2004, 12, 19), memberRole),
                        new MemberSeed("22130014", "Vũ Thị Hà", computerScience, "vuha@club.local", "0901000014", GenderEnum.FEMALE, LocalDate.of(2005, 3, 1), memberRole),
                        new MemberSeed("22130015", "Trần Thanh Sơn", informationSystem, "thanhson@club.local", "0901000015", GenderEnum.MALE, LocalDate.of(2004, 10, 6), memberRole),
                        new MemberSeed("22130016", "Lý Mỹ Linh", informationSystem, "mylinh@club.local", "0901000016", GenderEnum.FEMALE, LocalDate.of(2004, 1, 30), memberRole),
                        new MemberSeed("22130017", "Phạm Đức Long", informationSystem, "duclong@club.local", "0901000017", GenderEnum.MALE, LocalDate.of(2004, 8, 28), memberRole),
                        new MemberSeed("22130018", "Ngô Hải Đăng", informationSystem, "haidang@club.local", "0901000018", GenderEnum.MALE, LocalDate.of(2004, 4, 8), memberRole),
                        new MemberSeed("22130019", "Mai Khánh Vy", networking, "khanhvy@club.local", "0901000019", GenderEnum.FEMALE, LocalDate.of(2005, 5, 25), memberRole),
                        new MemberSeed("22130020", "Đinh Quốc Cường", networking, "quoccuong@club.local", "0901000020", GenderEnum.MALE, LocalDate.of(2004, 7, 7), memberRole),
                        // ── Khóa 23 (ĐÃ DUYỆT) ──────────────────────────────────────────────────
                        new MemberSeed("23130021", "Huỳnh Ngọc Mai", networking, "ngocmai@club.local", "0901000021", GenderEnum.FEMALE, LocalDate.of(2005, 6, 4), memberRole),
                        new MemberSeed("23130022", "Tạ Minh Quân", networking, "minhquan@club.local", "0901000022", GenderEnum.MALE, LocalDate.of(2005, 8, 13), memberRole),
                        new MemberSeed("23130023", "Cao Phương Nhi", ai, "phuongnhi@club.local", "0901000023", GenderEnum.FEMALE, LocalDate.of(2005, 9, 17), memberRole),
                        new MemberSeed("23130024", "Trương Nhật Minh", ai, "nhatminh@club.local", "0901000024", GenderEnum.MALE, LocalDate.of(2005, 11, 2), memberRole),
                        new MemberSeed("23130025", "Đoàn Bảo Châu", ai, "baochau@club.local", "0901000025", GenderEnum.FEMALE, LocalDate.of(2005, 12, 20), memberRole),
                        new MemberSeed("23130026", "Hồ Gia Bảo", software, "giabao@club.local", "0901000026", GenderEnum.MALE, LocalDate.of(2005, 3, 18), memberRole),
                        new MemberSeed("23130027", "Nguyễn Hà My", computerScience, "hamy@club.local", "0901000027", GenderEnum.FEMALE, LocalDate.of(2005, 4, 9), memberRole),
                        new MemberSeed("23130028", "Lâm Tuấn Kiệt", informationSystem, "tuankiet@club.local", "0901000028", GenderEnum.MALE, LocalDate.of(2005, 7, 29), memberRole),
                        // ── Khóa 24 (ĐÃ DUYỆT) ──────────────────────────────────────────────────
                        new MemberSeed("24130029", "Phùng Minh Khang", networking, "minhkhang@club.local", "0901000029", GenderEnum.MALE, LocalDate.of(2006, 1, 6), memberRole),
                        new MemberSeed("24130030", "Đỗ Khánh Linh", ai, "khanhlinh@club.local", "0901000030", GenderEnum.FEMALE, LocalDate.of(2006, 2, 14), memberRole),
                        new MemberSeed("24130031", "Nguyễn Nhật Hà", software, "nhatha@club.local", "0901000031", GenderEnum.FEMALE, LocalDate.of(2006, 5, 23), memberRole),
                        new MemberSeed("24130032", "Trần Duy Phúc", computerScience, "duyphuc@club.local", "0901000032", GenderEnum.MALE, LocalDate.of(2006, 8, 8), memberRole),
                        new MemberSeed("24130033", "Lê Bảo Ngọc", informationSystem, "baongoc@club.local", "0901000033", GenderEnum.FEMALE, LocalDate.of(2006, 9, 10), memberRole),
                        new MemberSeed("24130034", "Võ Minh Triết", networking, "minhtriet@club.local", "0901000034", GenderEnum.MALE, LocalDate.of(2006, 10, 12), memberRole),
                        new MemberSeed("24130035", "Phạm Hoài An", ai, "hoaian@club.local", "0901000035", GenderEnum.FEMALE, LocalDate.of(2006, 11, 15), memberRole),
                        new MemberSeed("24130036", "Bùi Quang Huy", software, "quanghuy@club.local", "0901000036", GenderEnum.MALE, LocalDate.of(2006, 12, 3), memberRole),
                        new MemberSeed("24130037", "Ngô Thùy Dương", computerScience, "thuyduong@club.local", "0901000037", GenderEnum.FEMALE, LocalDate.of(2006, 4, 26), memberRole),
                        new MemberSeed("24130038", "Đặng Quốc Việt", informationSystem, "quocviet@club.local", "0901000038", GenderEnum.MALE, LocalDate.of(2006, 6, 18), memberRole),
                        new MemberSeed("24130039", "Lương Gia Huy", networking, "giahuy@club.local", "0901000039", GenderEnum.MALE, LocalDate.of(2006, 7, 21), memberRole),
                        new MemberSeed("24130040", "Tô Minh Nguyệt", ai, "minhnguyet@club.local", "0901000040", GenderEnum.FEMALE, LocalDate.of(2006, 9, 28), memberRole),
                        // ── Khóa 24 (ĐANG CHỜ - 10 thành viên mới) ──────────────────────────────
                        new MemberSeed("24130046", "Đinh Thái Bình", software, "thaibinhk24@club.local", "0901000046", GenderEnum.MALE, LocalDate.of(2006, 1, 17), memberRole, ApprovalStatusEnum.PENDING, "Chờ xác minh thông tin sinh viên khóa 24"),
                        new MemberSeed("24130047", "Nguyễn Thu Hương", computerScience, "thuhuongk24@club.local", "0901000047", GenderEnum.FEMALE, LocalDate.of(2006, 3, 5), memberRole, ApprovalStatusEnum.PENDING, "Chờ ban học thuật xác nhận ngành học"),
                        new MemberSeed("24130048", "Trần Minh Đức", informationSystem, "minhduck24@club.local", "0901000048", GenderEnum.MALE, LocalDate.of(2006, 4, 22), memberRole, ApprovalStatusEnum.PENDING, "Hồ sơ đăng ký mới, chờ phỏng vấn"),
                        new MemberSeed("24130049", "Lê Phương Thảo", networking, "phuongthao@club.local", "0901000049", GenderEnum.FEMALE, LocalDate.of(2006, 5, 11), memberRole, ApprovalStatusEnum.PENDING, "Chờ kiểm tra thông tin lớp sinh hoạt"),
                        new MemberSeed("24130050", "Phạm Đức Anh", ai, "ducanhk24@club.local", "0901000050", GenderEnum.MALE, LocalDate.of(2006, 6, 30), memberRole, ApprovalStatusEnum.PENDING, "Chờ bổ sung minh chứng email trường"),
                        new MemberSeed("24130051", "Võ Ngọc Trân", software, "ngoctrank24@club.local", "0901000051", GenderEnum.FEMALE, LocalDate.of(2006, 7, 14), memberRole, ApprovalStatusEnum.PENDING, "Chờ duyệt đơn tham gia CLB lần 2"),
                        new MemberSeed("24130052", "Hoàng Bảo Long", computerScience, "baolongk24@club.local", "0901000052", GenderEnum.MALE, LocalDate.of(2006, 8, 3), memberRole, ApprovalStatusEnum.PENDING, "Chờ xác nhận từ ban chủ nhiệm"),
                        new MemberSeed("24130053", "Đỗ Thị Lan", informationSystem, "thilanck24@club.local", "0901000053", GenderEnum.FEMALE, LocalDate.of(2006, 9, 19), memberRole, ApprovalStatusEnum.PENDING, "Chờ hoàn thiện form đăng ký trực tuyến"),
                        new MemberSeed("24130054", "Ngô Tuấn Vũ", computerEngineering, "tuanvuk24@club.local", "0901000054", GenderEnum.MALE, LocalDate.of(2006, 10, 8), memberRole, ApprovalStatusEnum.PENDING, "Chờ ban kỹ thuật xem xét hồ sơ"),
                        new MemberSeed("24130055", "Trương Mỹ An", ai, "myank24@club.local", "0901000055", GenderEnum.FEMALE, LocalDate.of(2006, 11, 27), memberRole, ApprovalStatusEnum.PENDING, "Chờ phỏng vấn ngắn với trưởng ban học thuật"),
                        // ── Khóa 25 (ĐANG CHỜ - 5 thành viên cũ) ────────────────────────────────
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
                    .approvalNote(seed.approvalNote() != null ? seed.approvalNote() : index < 2 ? "Tai khoan ban chu nhiem" : "Da duyet ho so thanh vien")
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
                case 0 -> "President@123";
                case 1 -> "VicePresident@123";
                case 2 -> "AcademicHead@123";
                case 3 -> "CommunicationHead@123";
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
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
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
                new EventSeed("Workshop UI/UX cho sản phẩm sinh viên", "Phòng C204", -48, 13, 16, 3_000_000L, 65, "Ban thiết kế", "TECH", EventStatusEnum.Finished, ApprovalStatusEnum.APPROVED, "Thực hành wireframe, user flow và prototype bằng Figma."),
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
                new EventSeed("Workshop DevOps và CI/CD cơ bản", "Phòng lab B201", 72, 8, 11, 3_800_000L, 70, "Ban kỹ thuật", "TECH", EventStatusEnum.NotStarted, ApprovalStatusEnum.REJECTED, "Giới thiệu pipeline, kiểm thử tự động và triển khai ứng dụng mẫu."),
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
                    : seed.status() == EventStatusEnum.InProgress
                            ? now.minusMinutes(15)
                    : seed.dayOffset() <= 0
                            ? eventDate.minusDays(1).atTime(15, (eventNumber * 5) % 60)
                            : today.minusDays(eventNumber % 5).atTime(15, (eventNumber * 5) % 60);
            boolean hasEvaluation = seed.status() == EventStatusEnum.Evaluated;
            LocalDateTime startTime = seed.status() == EventStatusEnum.InProgress
                    ? now.minusHours(1)
                    : eventDate.atTime(seed.startHour(), 0);
            LocalDateTime endTime = seed.status() == EventStatusEnum.InProgress
                    ? now.plusHours(2)
                    : eventDate.atTime(seed.endHour(), 0);
            events.add(Event.builder()
                    .eventId(String.format("EVT%03d", eventNumber))
                    .eventName(seed.name())
                    .location(seed.location())
                    .eventDate(eventDate)
                    .startTime(startTime)
                    .endTime(endTime)
                    .estimatedCost(BigDecimal.valueOf(seed.estimatedCost()))
                    .capacity(seed.capacity())
                    .tag(seed.tag())
                    .status(seed.status())
                    .reqStatus(seed.reqStatus())
                    .organizer(seed.organizer())
                    .description(seed.description())
                    .evaluatedBy(hasEvaluation ? evaluator : null)
                    .evaluationDate(hasEvaluation ? eventDate.plusDays(1).atTime(17, 0) : null)
                    .evaluationContent(hasEvaluation ? "Tổng kết: " + seed.name() + " đặt mục tiêu chuyên môn và ghi nhận phản hồi để cải thiện lần sau." : null)
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

    private List<EventRegistration> seedEventRegistrations(List<Event> events, List<Member> members) {
        List<EventRegistration> registrations = new ArrayList<>();
        for (int eventIndex = 0; eventIndex < events.size(); eventIndex++) {
            Event event = events.get(eventIndex);
            if (event.getReqStatus() != ApprovalStatusEnum.APPROVED
                    || event.getStatus() == EventStatusEnum.Cancelled) {
                continue;
            }

            int registrationCount = switch (event.getStatus()) {
                case Evaluated, Finished -> 10 + (eventIndex % 6);
                case InProgress -> 8 + (eventIndex % 4);
                case NotStarted -> 5 + (eventIndex % 5);
                default -> 0;
            };
            LocalDateTime registrationBase = event.getStartTime() == null
                    ? event.getEventDate().atTime(8, 0).minusDays(14)
                    : event.getStartTime().minusDays(14);
            for (int offset = 0; offset < registrationCount && offset < members.size(); offset++) {
                Member member = members.get((eventIndex * 3 + offset) % members.size());
                LocalDateTime registeredAt = registrationBase.plusHours(offset * 3L);
                boolean attended = event.getStatus() == EventStatusEnum.Evaluated
                        || event.getStatus() == EventStatusEnum.Finished;
                registrations.add(EventRegistration.builder()
                        .id(new EventRegistrationId(event.getEventId(), member.getMemberId()))
                        .event(event)
                        .member(member)
                        .registeredAt(registeredAt)
                        .attended(attended)
                        .attendedAt(attended ? event.getEndTime().plusMinutes(10 + offset) : null)
                        .build());
            }
        }
        return eventRegistrationRepository.saveAll(registrations);
    }

    private List<Document> seedDocuments(List<Member> members, List<Subject> subjects, List<DocumentType> documentTypes) {
        List<Document> documents = new ArrayList<>();
        List<String> documentFileUrls = new ArrayList<>();
        List<ResourceFolderSeed> folders = resourceFolderSeeds();
        int index = 1;
        for (ResourceFolderSeed folder : folders) {
            List<MaterialSeed> materials = materialsForFolder(folder.folderId());
            for (int item = 0; item < 5; item++) {
                MaterialSeed material = materials.get(item % materials.size());
                Member proposer = members.get(index % members.size());
                Member approver = members.get((index + 1) % 2);
                LocalDateTime createdAt = seedDocumentTimestamp(index);
                documents.add(Document.builder()
                        .documentName(material.title())
                        .type(documentTypes.get(item % documentTypes.size()))
                        .subject(findSubjectByName(subjects, folder.subjectName()))
                        .status(DocumentStatus.WORKING)
                        .reqStatus(ApprovalStatusEnum.APPROVED)
                        .lookupFolderId(folder.folderId())
                        .version("2." + item)
                        .source("Tự biên soạn")
                        .note("Tài liệu thực tế từ kho UIT SoftwareEngineering Subjects, phân loại vào thư mục " + folder.label() + ".")
                        .proposedBy(proposer)
                        .approvedBy(approver)
                        .approvedAt(createdAt.plusHours(4))
                        .createdAt(createdAt)
                        .updatedAt(createdAt.plusDays(2))
                        .build());
                documentFileUrls.add(seedDocumentFileUrl(folder.folderId(), index, material));
                index++;
            }
        }
        seedDocumentReviewQueue(documents, subjects, documentTypes, members, index);
        for (int item = 0; item < 25; item++) {
            documentFileUrls.add(null);
        }
        List<Document> savedDocuments = documentRepository.saveAll(documents);
        seedDocumentFiles(savedDocuments, documentFileUrls);
        return savedDocuments;
    }

    private void seedDocumentFiles(List<Document> documents, List<String> fileUrls) {
        List<DocumentFile> files = new ArrayList<>();
        for (int index = 0; index < documents.size(); index++) {
            Document document = documents.get(index);
            String fileUrl = index < fileUrls.size() ? fileUrls.get(index) : null;
            String fileName = fileUrl != null
                    ? fileNameFromUrl(fileUrl, "tai-lieu-" + document.getDocumentId())
                    : "tai-lieu-" + document.getDocumentId() + ".pdf";
            String mimeType = mimeTypeFromFileName(fileName);
            LocalDateTime uploadedAt = document.getCreatedAt() != null
                    ? document.getCreatedAt().plusHours(1)
                    : seedDocumentTimestamp(index + 1);
            files.add(DocumentFile.builder()
                    .document(document)
                    .fileUrl(fileUrl != null ? fileUrl : "/uploads/documents/seed-document-" + document.getDocumentId() + ".pdf")
                    .fileName(fileName)
                    .fileSize(650_000L + (index * 18_000L))
                    .mimeType(mimeType)
                    .uploadedAt(uploadedAt)
                    .build());
        }
        documentFileRepository.saveAll(files);
    }

    private void normalizeSeedDocumentTimeline() {
        List<Document> seededDocuments = documentRepository.findAll().stream()
                .filter(document -> "Tự biên soạn".equalsIgnoreCase(document.getSource()))
                .sorted((left, right) -> Long.compare(
                        left.getDocumentId() == null ? 0L : left.getDocumentId(),
                        right.getDocumentId() == null ? 0L : right.getDocumentId()))
                .toList();

        if (seededDocuments.isEmpty()) {
            return;
        }

        List<DocumentFile> seededFiles = documentFileRepository.findAll().stream()
                .filter(file -> file.getDocument() != null
                        && file.getDocument().getDocumentId() != null
                        && "Tự biên soạn".equalsIgnoreCase(file.getDocument().getSource()))
                .sorted((left, right) -> Long.compare(
                        left.getDocument().getDocumentId(),
                        right.getDocument().getDocumentId()))
                .toList();

        Map<Long, Integer> documentIndexMap = new HashMap<>();
        for (int index = 0; index < seededDocuments.size(); index++) {
            Document document = seededDocuments.get(index);
            if (document.getDocumentId() != null) {
                documentIndexMap.put(document.getDocumentId(), index);
            }
        }

        for (int index = 0; index < seededDocuments.size(); index++) {
            Document document = seededDocuments.get(index);
            LocalDateTime createdAt = seedDocumentTimestamp(index);
            LocalDateTime updatedAt = createdAt.plusDays(2);
            LocalDateTime approvedAt = document.getApprovedAt();
            if (document.getReqStatus() == ApprovalStatusEnum.APPROVED) {
                approvedAt = createdAt.plusHours(4);
            } else if (document.getReqStatus() == ApprovalStatusEnum.REQUESTED_CHANGES) {
                approvedAt = null;
                updatedAt = createdAt.plusHours(6);
            } else if (document.getReqStatus() == ApprovalStatusEnum.PENDING) {
                approvedAt = null;
                updatedAt = createdAt.plusHours(1);
            }
            if (document.getDocumentId() != null) {
                documentRepository.updateSeedTimeline(document.getDocumentId(), createdAt, updatedAt, approvedAt);
            }
        }

        for (int index = 0; index < seededFiles.size(); index++) {
            DocumentFile file = seededFiles.get(index);
            Document document = file.getDocument();
            Integer documentIndex = document.getDocumentId() == null
                    ? null
                    : documentIndexMap.get(document.getDocumentId());
            if (documentIndex == null) {
                continue;
            }
            LocalDateTime uploadedAt = seedDocumentTimestamp(documentIndex).plusHours(1);
            String fileUrl = seedDocumentFileUrl(
                    document.getLookupFolderId() != null ? document.getLookupFolderId() : "review",
                    documentIndex + 1,
                    new MaterialSeed(
                            document.getDocumentName(),
                            file.getFileUrl()
                    ));
            if (file.getFileId() != null) {
                documentFileRepository.updateSeedTimeline(file.getFileId(), uploadedAt, fileUrl);
            }
        }
    }

    private void seedDocumentReviewQueue(
            List<Document> documents,
            List<Subject> subjects,
            List<DocumentType> documentTypes,
            List<Member> members,
            int startIndex) {

        // ── 5 tai lieu REQUESTED_CHANGES ────────────────────────────────────────
        List<DocumentReviewSeed> requestedChangesSeeds = List.of(
                new DocumentReviewSeed("Đề xuất cập nhật slide Luật sở hữu trí tuệ", "Chính trị", 1, ApprovalStatusEnum.REQUESTED_CHANGES, DocumentStatus.FIXING, "Cần bổ sung phần trích dẫn văn bản pháp luật mới nhất."),
                new DocumentReviewSeed("Bản sửa giáo trình Anh văn 2 - Unit 5 Presentation", "Anh văn", 0, ApprovalStatusEnum.REQUESTED_CHANGES, DocumentStatus.FIXING, "Cần chuẩn hóa định dạng bài tập nghe và đáp án."),
                new DocumentReviewSeed("Bộ bài tập SQL nâng cao bản chỉnh sửa", "Cơ sở dữ liệu", 4, ApprovalStatusEnum.REQUESTED_CHANGES, DocumentStatus.FIXING, "Cần thêm dữ liệu mẫu cho phần truy vấn lồng nhau."),
                new DocumentReviewSeed("Tài liệu thực hành React Hooks bản cập nhật", "Công nghệ phần mềm nâng cao", 2, ApprovalStatusEnum.REQUESTED_CHANGES, DocumentStatus.FIXING, "Cần tách rõ ví dụ useEffect và useMemo."),
                new DocumentReviewSeed("Slide Kiến trúc Microservices bản chỉnh sửa", "Kiến trúc phần mềm", 1, ApprovalStatusEnum.REQUESTED_CHANGES, DocumentStatus.FIXING, "Cần bổ sung sơ đồ triển khai Docker Compose và giải thích từng service.")
        );
        // ── 20 tai lieu PENDING ──────────────────────────────────────────────────
        List<DocumentReviewSeed> pendingSeeds = List.of(
                new DocumentReviewSeed("Đề xuất tài liệu nhập môn Python cho thành viên mới", "Nhập môn lập trình", 2, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Tài liệu mới chờ admin duyệt thêm vào kho."),
                new DocumentReviewSeed("Đề xuất ngân hàng câu hỏi Xác suất thống kê", "Xác suất thống kê", 4, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Bộ câu hỏi trắc nghiệm phục vụ ôn tập giữa kỳ."),
                new DocumentReviewSeed("Đề xuất slide An toàn thông tin web cơ bản", "An toàn thông tin", 1, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Slide chuyên đề bảo mật web cho buổi sinh hoạt CLB."),
                new DocumentReviewSeed("Đề xuất tài liệu IELTS Reading Foundation", "Anh văn", 0, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Tài liệu ngoại ngữ mới cho nhóm luyện chứng chỉ."),
                new DocumentReviewSeed("Giáo trình Lập trình hướng đối tượng với Java", "Lập trình hướng đối tượng", 0, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Giáo trình tự biên soạn, chờ duyệt để thêm vào kho chính thức."),
                new DocumentReviewSeed("Slide Phân tích và thiết kế hệ thống - Chương 3", "Phân tích thiết kế hệ thống", 1, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Slide bổ sung chương 3 còn thiếu trong kho hiện tại."),
                new DocumentReviewSeed("Bài tập thực hành Mạng máy tính - Lab 4", "Mạng máy tính", 4, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Bài lab mới cho phần cấu hình VLAN và routing."),
                new DocumentReviewSeed("Tài liệu tham khảo Trí tuệ nhân tạo - Deep Learning cơ bản", "Trí tuệ nhân tạo", 2, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Tổng hợp lý thuyết neural network cơ bản cho thành viên mới."),
                new DocumentReviewSeed("Đề thi mẫu Cấu trúc rời rạc - Học kỳ 1 2024", "Cấu trúc rời rạc", 3, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Đề thi tham khảo từ thành viên khóa trên, chờ kiểm duyệt nội dung."),
                new DocumentReviewSeed("Báo cáo mẫu đồ án môn Công nghệ phần mềm nâng cao", "Công nghệ phần mềm nâng cao", 5, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Template báo cáo đồ án theo yêu cầu mới của khoa."),
                new DocumentReviewSeed("Slide Triết học Mác - Lênin - Chương 2 bổ sung", "Triết học Mác - Lênin", 1, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Bổ sung nội dung chương 2 còn sơ sài trong kho cũ."),
                new DocumentReviewSeed("Tài liệu ôn tập cuối kỳ Cơ sở dữ liệu tổng hợp", "Cơ sở dữ liệu", 2, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Tổng hợp lý thuyết và bài tập ôn tập từ nhiều nguồn."),
                new DocumentReviewSeed("Hướng dẫn sử dụng Git nâng cao cho nhóm dự án", "Kiến trúc phần mềm", 2, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Tài liệu thực hành Git Flow, rebase và cherry-pick cho nhóm làm dự án."),
                new DocumentReviewSeed("Slide Docker và Containerization cho sinh viên CNTT", "Công nghệ phần mềm nâng cao", 1, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Slide giới thiệu Docker, image và container, chờ duyệt trước buổi workshop."),
                new DocumentReviewSeed("Bộ đề thi thử TOEIC trình độ B1", "Anh văn", 3, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Bộ đề thi thử 4 kỹ năng, phục vụ nhóm ôn luyện chứng chỉ."),
                new DocumentReviewSeed("Tài liệu thực hành Wireshark - Phân tích gói tin", "Mạng máy tính", 4, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Hướng dẫn bắt và phân tích gói tin HTTP, TCP bằng Wireshark."),
                new DocumentReviewSeed("Giáo trình An toàn thông tin - Bảo mật ứng dụng web", "An toàn thông tin", 0, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Giáo trình mới bổ sung phần OWASP Top 10 và thực hành phòng chống."),
                new DocumentReviewSeed("Slide Machine Learning - Hồi quy tuyến tính và Logistic", "Trí tuệ nhân tạo", 1, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Slide chuyên đề Machine Learning cơ bản, trình bày tại buổi seminar tháng tới."),
                new DocumentReviewSeed("Tài liệu tham khảo Xác suất thống kê ứng dụng trong CNTT", "Xác suất thống kê", 2, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Tổng hợp ứng dụng thực tế của xác suất thống kê trong phân tích dữ liệu."),
                new DocumentReviewSeed("Hướng dẫn viết báo cáo nghiên cứu khoa học sinh viên", "Chính trị", 5, ApprovalStatusEnum.PENDING, DocumentStatus.WORKING, "Tài liệu hướng dẫn cách chọn đề tài, viết đề cương và trình bày kết quả nghiên cứu khoa học sinh viên."));

        // Them REQUESTED_CHANGES truoc
        for (int item = 0; item < requestedChangesSeeds.size(); item++) {
            DocumentReviewSeed seed = requestedChangesSeeds.get(item);
            int index = startIndex + item;
            Member proposer = members.get(index % members.size());
            Member reviewer = members.get((index + 1) % 2);
            LocalDateTime createdAt = seedDocumentTimestamp(index);
            documents.add(Document.builder()
                    .documentName(seed.name())
                    .type(documentTypes.get(seed.typeIndex() % documentTypes.size()))
                    .subject(findSubjectByName(subjects, seed.subjectName()))
                    .status(seed.documentStatus())
                    .reqStatus(seed.reqStatus())
                    .lookupFolderId(null)
                    .version("1.0")
                    .source("Tự biên soạn")
                    .note(seed.note())
                    .proposedBy(proposer)
                    .approvedBy(reviewer)
                    .approvedAt(null)
                    .createdAt(createdAt)
                    .updatedAt(createdAt.plusHours(6))
                    .build());
        }

        // Them PENDING sau
        int pendingStartIndex = startIndex + requestedChangesSeeds.size();
        for (int item = 0; item < pendingSeeds.size(); item++) {
            DocumentReviewSeed seed = pendingSeeds.get(item);
            int index = pendingStartIndex + item;
            Member proposer = members.get(index % members.size());
            LocalDateTime createdAt = seedDocumentTimestamp(index);
            documents.add(Document.builder()
                    .documentName(seed.name())
                    .type(documentTypes.get(seed.typeIndex() % documentTypes.size()))
                    .subject(findSubjectByName(subjects, seed.subjectName()))
                    .status(seed.documentStatus())
                    .reqStatus(seed.reqStatus())
                    .lookupFolderId(null)
                    .version("1.0")
                    .source("Tự biên soạn")
                    .note(seed.note())
                    .proposedBy(proposer)
                    .approvedBy(null)
                    .approvedAt(null)
                    .createdAt(createdAt)
                    .updatedAt(createdAt.plusHours(1))
                    .build());
        }
    }

    private List<Notification> seedNotifications(List<Member> members) {
        List<Notification> notifications = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(25);
        for (int index = 1; index <= 20; index++) {
            notifications.add(Notification.builder()
                    .title("Thông báo hoạt động số " + index)
                    .content("Nội dung thông báo mẫu cho thành viên đột biến " + index)
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

    private void seedTransactions(List<Event> events, List<Member> members, List<EventRegistration> eventRegistrations) {
        List<Transaction> transactions = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        for (int monthOffset = 11; monthOffset >= 0; monthOffset--) {
            YearMonth dueMonth = currentMonth.minusMonths(monthOffset);
            String description = monthlyDueDescription(dueMonth);
            LocalDateTime dueCreatedAt = dueMonth.atDay(1).atTime(8, 0);
            for (int index = 0; index < members.size(); index++) {
                Member member = members.get(index);
                Member creator = members.get(index % 2);
                TransactionStatus status = pickMonthlyDueStatus(monthOffset, index);
                Member approver = isPaidStatus(status) ? members.get((index + 1) % 2) : null;
                LocalDateTime paidAt = dueCreatedAt.plusDays(Math.min(24, 2 + ((index * 2 + monthOffset) % 24)))
                        .plusHours(index % 9)
                        .plusMinutes((index * 7) % 60);

                transactions.add(Transaction.builder()
                        .transactionId(String.format("DUE-FUND-%s-%03d", dueMonth.format(MONTH_ID_FORMAT), member.getMemberId()))
                        .member(member)
                        .counterpartyName(member.getFullName())
                        .type(TransactionType.INCOME)
                        .amount(MONTHLY_FUND_AMOUNT)
                        .description(description)
                        .transactionDate(dueCreatedAt)
                        .status(status)
                        .createdBy(creator)
                        .approvedBy(approver)
                        .createdAt(dueCreatedAt)
                        .updatedAt(isPaidStatus(status) || status == TransactionStatus.PROCESSING ? paidAt : dueCreatedAt)
                        .approvedAt(status == TransactionStatus.PROCESSING
                                ? paidAt
                                : (approver == null ? null : paidAt))
                        .build());
            }
        }

        seedEventFeeTransactions(transactions, eventRegistrations, members);

        for (int index = 1; index <= events.size(); index++) {
            Event event = events.get(index - 1);
            Member owner = members.get((index + 3) % members.size());
            Member creator = members.get(index % 2);
            Member approver = members.get((index + 1) % 2);
            LocalDateTime sponsorIncomeAt = event.getEventDate().minusDays(7).atTime(14 + (index % 4), (index * 11) % 60);
            LocalDateTime expenseAt = event.getEventDate().plusDays(1).atTime(10 + (index % 6), (index * 7) % 60);
            long sponsorshipIncome = index % 3 == 0 ? 1_800_000L + (index % 5) * 450_000L : 0L;
            long operatingExpense = 700_000L + (index % 7) * 220_000L + index * 55_000L;
            long venueExpense = event.getEstimatedCost() == null
                    ? 0L
                    : Math.max(350_000L, event.getEstimatedCost().longValue() / 3);
            TransactionStatus sponsorshipStatus = pickEventSponsorshipStatus(event);
            TransactionStatus expenseStatus = pickEventExpenseStatus(event, index);

            if (sponsorshipIncome > 0 && event.getReqStatus() == ApprovalStatusEnum.APPROVED) {
                transactions.add(Transaction.builder()
                        .transactionId(String.format("TRX-EVT-%03d-SPONSOR", index))
                        .event(event)
                        .counterpartyName(pickSponsorName(index))
                        .type(TransactionType.INCOME)
                        .amount(BigDecimal.valueOf(sponsorshipIncome))
                        .description("Tài trợ cho sự kiện: " + event.getEventName())
                        .transactionDate(sponsorIncomeAt)
                        .status(sponsorshipStatus)
                        .createdBy(creator)
                        .approvedBy(isPaidStatus(sponsorshipStatus) ? approver : null)
                        .createdAt(sponsorIncomeAt)
                        .updatedAt(sponsorIncomeAt.plusHours(2))
                        .approvedAt(isPaidStatus(sponsorshipStatus) ? sponsorIncomeAt.plusHours(5) : null)
                        .build());
            }

            transactions.add(Transaction.builder()
                    .transactionId(String.format("TRX-EVT-%03d-OUT", index))
                    .event(event)
                    .member(owner)
                    .counterpartyName(pickVendorName(index))
                    .type(TransactionType.Expense)
                    .amount(BigDecimal.valueOf(operatingExpense))
                    .description("Chi phí tổ chức " + event.getEventName())
                    .transactionDate(expenseAt)
                    .status(expenseStatus)
                    .createdBy(creator)
                    .approvedBy(isPaidStatus(expenseStatus) ? approver : null)
                    .createdAt(expenseAt)
                    .updatedAt(expenseAt.plusHours(2))
                    .approvedAt(isPaidStatus(expenseStatus) ? expenseAt.plusHours(6) : null)
                    .build());

            if (venueExpense > 0 && event.getStatus() != EventStatusEnum.Cancelled) {
                LocalDateTime venueExpenseAt = event.getEventDate().minusDays(1).atTime(16, (index * 13) % 60);
                transactions.add(Transaction.builder()
                        .transactionId(String.format("TRX-EVT-%03d-VENUE", index))
                        .event(event)
                        .counterpartyName("Đối tác địa điểm và thiết bị")
                        .type(TransactionType.Expense)
                        .amount(BigDecimal.valueOf(venueExpense))
                        .description("Tạm ứng địa điểm và thiết bị: " + event.getEventName())
                        .transactionDate(venueExpenseAt)
                        .status(expenseStatus)
                        .createdBy(creator)
                        .approvedBy(isPaidStatus(expenseStatus) ? approver : null)
                        .createdAt(venueExpenseAt)
                        .updatedAt(venueExpenseAt.plusHours(2))
                        .approvedAt(isPaidStatus(expenseStatus) ? venueExpenseAt.plusHours(5) : null)
                        .build());
            }
        }
        seedMonthlyOperatingTransactions(transactions, members, currentMonth);
        transactionRepository.saveAll(transactions);
    }

    private void seedEventFeeTransactions(
            List<Transaction> transactions,
            List<EventRegistration> registrations,
            List<Member> members) {
        for (EventRegistration registration : registrations) {
            Event event = registration.getEvent();
            Member member = registration.getMember();
            BigDecimal feeAmount = eventParticipationFee(event);
            if (feeAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            LocalDateTime registeredAt = registration.getRegisteredAt() == null
                    ? event.getStartTime().minusDays(7)
                    : registration.getRegisteredAt();
            TransactionStatus status = pickEventIncomeStatus(event);
            Member approver = event.getEvaluatedBy() != null
                    ? event.getEvaluatedBy()
                    : members.get(Math.floorMod(event.getEventId().hashCode(), 2));
            transactions.add(Transaction.builder()
                    .transactionId(String.format(
                            "DUE-EVENT-%s-%03d",
                            event.getEventId(),
                            member.getMemberId()))
                    .event(event)
                    .member(member)
                    .counterpartyName(member.getFullName())
                    .type(TransactionType.INCOME)
                    .amount(feeAmount)
                    .description("Phí tham gia sự kiện: " + event.getEventName())
                    .transactionDate(registeredAt)
                    .status(status)
                    .createdBy(member)
                    .approvedBy(isPaidStatus(status) ? approver : null)
                    .createdAt(registeredAt)
                    .updatedAt(isPaidStatus(status) ? registeredAt.plusHours(6) : registeredAt)
                    .approvedAt(isPaidStatus(status) ? registeredAt.plusHours(6) : null)
                    .build());
        }
    }

    private void seedMonthlyOperatingTransactions(
            List<Transaction> transactions,
            List<Member> members,
            YearMonth currentMonth) {
        for (int monthOffset = 11; monthOffset >= 0; monthOffset--) {
            YearMonth month = currentMonth.minusMonths(monthOffset);
            int monthValue = month.getMonthValue();
            Member creator = members.get(monthValue % 2);
            Member approver = members.get((monthValue + 1) % 2);
            long partnerIncome = 900_000L + (monthValue % 4) * 250_000L + (12 - monthOffset) * 80_000L;
            long fixedExpense = 650_000L + (monthValue % 5) * 140_000L;
            LocalDateTime incomeAt = month.atDay(Math.min(24, 12 + (monthValue % 8))).atTime(9, monthValue * 3 % 60);
            LocalDateTime expenseAt = month.atDay(Math.min(26, 18 + (monthValue % 7))).atTime(15, monthValue * 5 % 60);

            transactions.add(Transaction.builder()
                    .transactionId(String.format("TRX-MONTH-%s-PARTNER", month.format(MONTH_ID_FORMAT)))
                    .counterpartyName("Đối tác học thuật tháng " + monthValue)
                    .type(TransactionType.INCOME)
                    .amount(BigDecimal.valueOf(partnerIncome))
                    .description(String.format("Đóng góp đối tác học thuật tháng %02d/%d", monthValue, month.getYear()))
                    .transactionDate(incomeAt)
                    .status(TransactionStatus.COMPLETED)
                    .createdBy(creator)
                    .approvedBy(approver)
                    .createdAt(incomeAt)
                    .updatedAt(incomeAt.plusHours(2))
                    .approvedAt(incomeAt.plusHours(6))
                    .build());

            transactions.add(Transaction.builder()
                    .transactionId(String.format("TRX-MONTH-%s-OPS", month.format(MONTH_ID_FORMAT)))
                    .counterpartyName("Văn phòng phẩm và nhu cầu CLB")
                    .type(TransactionType.Expense)
                    .amount(BigDecimal.valueOf(fixedExpense))
                    .description(String.format("Chi phí vận hành CLB tháng %02d/%d", monthValue, month.getYear()))
                    .transactionDate(expenseAt)
                    .status(TransactionStatus.COMPLETED)
                    .createdBy(creator)
                    .approvedBy(approver)
                    .createdAt(expenseAt)
                    .updatedAt(expenseAt.plusHours(2))
                    .approvedAt(expenseAt.plusHours(6))
                    .build());
        }
    }

    private void seedSystemSettings(List<Member> members) {
        List<SystemSetting> settings = List.of(
                SystemSetting.builder().settingKey("club.name").settingValue("CLB Học thuật CNTT").description("Tên hiển thị của CLB").updatedBy(members.get(0)).updatedAt(LocalDateTime.now().minusDays(5)).build(),
                SystemSetting.builder().settingKey("member.defaultRole").settingValue("Thành viên").description("Vai trò mặc định khi duyệt thành viên").updatedBy(members.get(0)).updatedAt(LocalDateTime.now().minusDays(5)).build(),
                SystemSetting.builder().settingKey("document.approvalRequired").settingValue("true").description("Tài liệu mới cần qua duyệt").updatedBy(members.get(0)).updatedAt(LocalDateTime.now().minusDays(4)).build(),
                SystemSetting.builder().settingKey("event.autoArchiveDays").settingValue("30").description("Số ngày tự động lưu trữ sự kiện").updatedBy(members.get(1)).updatedAt(LocalDateTime.now().minusDays(4)).build(),
                SystemSetting.builder().settingKey("notification.defaultMethod").settingValue("SYSTEM").description("ênh gửi mặc định").updatedBy(members.get(1)).updatedAt(LocalDateTime.now().minusDays(3)).build(),
                SystemSetting.builder().settingKey("finance.maxPendingDays").settingValue("14").description("Số ngày tối đa cho giao dịch chờ duyệt").updatedBy(members.get(0)).updatedAt(LocalDateTime.now().minusDays(2)).build());
        systemSettingRepository.saveAll(settings);
    }

    private void seedAuditLogs(List<Member> members, List<Event> events) {
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
        events.stream()
                .filter(event -> event.getStatus() == EventStatusEnum.Evaluated)
                .filter(event -> event.getEvaluationContent() != null && !event.getEvaluationContent().isBlank())
                .forEach(event -> logs.add(AuditLog.builder()
                        .entityType("EVENT")
                        .entityId(event.getEventId())
                        .actionType("EVALUATE")
                        .oldValue("{\"status\":\"Finished\",\"evaluationContent\":null}")
                        .newValue("{\"status\":\"Evaluated\",\"evaluationContent\":\"" + event.getEvaluationContent() + "\"}")
                        .performedBy(event.getEvaluatedBy())
                        .performedAt(event.getEvaluationDate())
                        .build()));
        auditLogRepository.saveAll(logs);
    }

    private BigDecimal eventParticipationFee(Event event) {
        if (event == null || event.getReqStatus() != ApprovalStatusEnum.APPROVED) {
            return BigDecimal.ZERO;
        }
        return switch (event.getTag()) {
            case "CERT" -> BigDecimal.valueOf(120_000L);
            case "SOCIAL" -> BigDecimal.valueOf(80_000L);
            case "TECH" -> event.getEstimatedCost() != null
                    && event.getEstimatedCost().compareTo(BigDecimal.valueOf(4_000_000L)) > 0
                    ? BigDecimal.valueOf(100_000L)
                    : BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }

    private TransactionStatus pickEventIncomeStatus(Event event) {
        if (event.getReqStatus() != ApprovalStatusEnum.APPROVED) return TransactionStatus.PENDING;
        if (event.getStatus() == EventStatusEnum.Cancelled) return TransactionStatus.REFUNDED;
        if (event.getStatus() == EventStatusEnum.NotStarted) return TransactionStatus.PENDING;
        return TransactionStatus.COMPLETED;
    }

    private TransactionStatus pickEventSponsorshipStatus(Event event) {
        if (event.getReqStatus() == ApprovalStatusEnum.REJECTED) return TransactionStatus.CANCELLED;
        if (event.getReqStatus() != ApprovalStatusEnum.APPROVED) return TransactionStatus.PENDING;
        if (event.getStatus() == EventStatusEnum.Cancelled) return TransactionStatus.REFUNDED;
        if (event.getStatus() == EventStatusEnum.NotStarted) return TransactionStatus.PENDING;
        return TransactionStatus.COMPLETED;
    }

    private TransactionStatus pickEventExpenseStatus(Event event, int index) {
        if (event.getReqStatus() == ApprovalStatusEnum.REJECTED) return TransactionStatus.CANCELLED;
        if (event.getReqStatus() != ApprovalStatusEnum.APPROVED) return TransactionStatus.PENDING;
        if (event.getStatus() == EventStatusEnum.Cancelled) return TransactionStatus.CANCELLED;
        if (event.getStatus() == EventStatusEnum.NotStarted) return TransactionStatus.PENDING;
        if (event.getStatus() == EventStatusEnum.InProgress) return TransactionStatus.APPROVED;
        return index % 9 == 0 ? TransactionStatus.APPROVED : TransactionStatus.COMPLETED;
    }

    private TransactionStatus pickMonthlyDueStatus(int monthOffset, int memberIndex) {
        if (monthOffset == 0) {
            if (memberIndex < 7) return TransactionStatus.PROCESSING;
            if (memberIndex < 10) return TransactionStatus.PENDING;
            if (memberIndex < 12) return TransactionStatus.REJECTED;
        }
        return TransactionStatus.COMPLETED;
    }

    private boolean isPaidStatus(TransactionStatus status) {
        return status == TransactionStatus.APPROVED || status == TransactionStatus.COMPLETED;
    }

    private String monthlyDueDescription(YearMonth month) {
        return String.format("Dong quy thang %02d/%d", month.getMonthValue(), month.getYear());
    }

    private String pickVendorName(int index) {
        return switch (index % 5) {
            case 0 -> "Nhà sách Phương Nam";
            case 1 -> "Cửa hàng văn phòng phẩm Kim Phát";
            case 2 -> "Dịch vụ in ấn Hồng Phát";
            case 3 -> "Trung tâm thiết bị sự kiện Sài Gòn";
            default -> "Quán nước Thanh Xuân";
        };
    }

    private String pickSponsorName(int index) {
        return switch (index % 4) {
            case 0 -> "FPT Software Academy";
            case 1 -> "VNG Campus";
            case 2 -> "TMA Solutions";
            default -> "Bosch Global Software Technologies";
        };
    }

    private LocalDateTime seedDocumentTimestamp(int offset) {
        LocalDateTime latestMonthStart = LocalDateTime.of(2026, 5, 1, 9, 0);
        int monthOffset = Math.floorMod(offset, 6);
        int cycleIndex = offset / 6;
        int[] daySlots = {2, 6, 10, 14, 18, 22, 26, 28};
        int day = daySlots[Math.floorMod(cycleIndex, daySlots.length)];
        return latestMonthStart
                .minusMonths(monthOffset)
                .withDayOfMonth(Math.min(day, latestMonthStart.minusMonths(monthOffset).toLocalDate().lengthOfMonth()))
                .withHour(8 + (offset % 8))
                .withMinute((offset * 7) % 60)
                .withSecond(0)
                .withNano(0);
    }

    private String seedDocumentFileUrl(String folderId, int index, MaterialSeed material) {
        String fileName = fileNameFromUrl(material.url(), material.title());
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        String extension = dotIndex > 0 ? fileName.substring(dotIndex).toLowerCase() : ".pdf";
        String safeBaseName = baseName.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (safeBaseName.isBlank()) {
            safeBaseName = "tai-lieu";
        }
        return "/uploads/documents/seed/" + folderId + "/" + String.format("%03d-%s%s", index, safeBaseName, extension);
    }

    private String fileNameFromUrl(String url, String fallbackName) {
        String decoded = URLDecoder.decode(url, StandardCharsets.UTF_8);
        int slashIndex = decoded.lastIndexOf('/');
        String fileName = slashIndex >= 0 ? decoded.substring(slashIndex + 1) : decoded;
        return fileName.isBlank() ? fallbackName + ".pdf" : fileName;
    }

    private String mimeTypeFromFileName(String fileName) {
        String normalized = fileName.toLowerCase();
        if (normalized.endsWith(".pdf")) return "application/pdf";
        if (normalized.endsWith(".ppt") || normalized.endsWith(".pptx")) return "application/vnd.ms-powerpoint";
        if (normalized.endsWith(".doc") || normalized.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (normalized.endsWith(".xls") || normalized.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (normalized.endsWith(".zip")) return "application/zip";
        return "application/octet-stream";
    }

    private Subject findSubjectByName(List<Subject> subjects, String subjectName) {
        return subjects.stream()
                .filter(subject -> subject.getSubjectName().equalsIgnoreCase(subjectName))
                .findFirst()
                .orElse(subjects.get(Math.floorMod(subjectName.hashCode(), subjects.size())));
    }

    private List<ResourceFolderSeed> resourceFolderSeeds() {
        return List.of(
                new ResourceFolderSeed("tu-tuong-ho-chi-minh", "Tu tuong Ho Chi Minh", "Chinh tri"),
                new ResourceFolderSeed("triet-hoc-mac-lenin", "Triet hoc Mac - Lenin", "Chinh tri"),
                new ResourceFolderSeed("kinh-te-chinh-tri", "Kinh te Chinh tri Mac - Lenin", "Chinh tri"),
                new ResourceFolderSeed("chu-nghia-xa-hoi-khoa-hoc", "Chu nghia xa hoi khoa hoc", "Chinh tri"),
                new ResourceFolderSeed("lich-su-dang", "Lich su Dang Cong san Viet Nam", "Chinh tri"),
                new ResourceFolderSeed("phap-luat-dai-cuong", "Phap luat dai cuong", "Chinh tri"),
                new ResourceFolderSeed("giai-tich", "Giai tich", "Xac suat thong ke"),
                new ResourceFolderSeed("dai-so-tuyen-tinh", "Dai so tuyen tinh", "Xac suat thong ke"),
                new ResourceFolderSeed("cau-truc-roi-rac", "Cau truc roi rac", "Cau truc roi rac"),
                new ResourceFolderSeed("xac-suat-thong-ke", "Xac suat thong ke", "Xac suat thong ke"),
                new ResourceFolderSeed("nhap-mon-lap-trinh", "Nhap mon lap trinh", "Nhap mon lap trinh"),
                new ResourceFolderSeed("anh-van-1", "Anh van 1", "Anh van"),
                new ResourceFolderSeed("anh-van-2", "Anh van 2", "Anh van"),
                new ResourceFolderSeed("anh-van-3", "Anh van 3", "Anh van"),
                new ResourceFolderSeed("ky-thuat-phan-mem", "Ky thuat phan mem", "Kien truc phan mem"),
                new ResourceFolderSeed("truyen-thong-da-phuong-tien", "Truyen thong da phuong tien", "Cong nghe phan mem nang cao"),
                new ResourceFolderSeed("he-thong-thong-tin-chuyen-nganh", "He thong thong tin", "Phan tich thiet ke he thong"),
                new ResourceFolderSeed("thuong-mai-dien-tu", "Thuong mai dien tu", "Co so du lieu"),
                new ResourceFolderSeed("khoa-hoc-may-tinh-chuyen-nganh", "Khoa hoc may tinh", "Tri tue nhan tao"),
                new ResourceFolderSeed("tri-tue-nhan-tao", "Tri tue nhan tao", "Tri tue nhan tao"),
                new ResourceFolderSeed("cong-nghe-thong-tin", "Cong nghe thong tin", "Cong nghe phan mem nang cao"),
                new ResourceFolderSeed("khoa-hoc-du-lieu", "Khoa hoc du lieu", "Tri tue nhan tao"),
                new ResourceFolderSeed("an-toan-thong-tin", "An toan thong tin", "An toan thong tin"),
                new ResourceFolderSeed("mang-may-tinh-truyen-thong-du-lieu", "Mang may tinh va truyen thong du lieu", "Mang may tinh"),
                new ResourceFolderSeed("ky-thuat-may-tinh-chuyen-nganh", "Ky thuat may tinh", "Mang may tinh"),
                new ResourceFolderSeed("thiet-ke-vi-mach", "Thiet ke vi mach", "Kien truc phan mem"));
    }

    private List<MaterialSeed> materialsForFolder(String folderId) {
        return switch (folderId) {
            case "tu-tuong-ho-chi-minh", "triet-hoc-mac-lenin", "kinh-te-chinh-tri",
                    "chu-nghia-xa-hoi-khoa-hoc", "lich-su-dang", "phap-luat-dai-cuong" -> selectedMaterials(
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/SE301 - Phat trien phan mem ma nguon mo/Bai giang/2. Open Source Licensing - Contract - Law.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/SE301 - Phat trien phan mem ma nguon mo/Bai giang/5. Legal impacts of OS and FS licenses.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/EC002 - Quan tri doanh nghiep/Chapter 03 Enterprise and Business Environment.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/EC002 - Quan tri doanh nghiep/Chapter 04 Enterprise and Governance.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/EC002 - Quan tri doanh nghiep/Chapter 10 International Business Administration.pdf");
            case "giai-tich" -> selectedMaterials(
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/MA006 - Giai tich/De on thi cuoi ky.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/MA003 - Dai so tuyen tinh/Bai giang/Chuong 4 - Khong gian Euclide.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/MA003 - Dai so tuyen tinh/Bai giang/Chuong 5 - Gia tri rieng vector rieng cheo hoa ma tran.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/MA003 - Dai so tuyen tinh/On thi/Cuoi ky/01.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/MA003 - Dai so tuyen tinh/On thi/Giua ky/De on tap.pdf");
            case "dai-so-tuyen-tinh", "xac-suat-thong-ke" -> selectedMaterials(
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/MA003 - Dai so tuyen tinh/Bai giang/Chuong 1 - Ma tran.ppt",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/MA003 - Dai so tuyen tinh/Bai giang/Chuong 1 - Dinh thuc.ppt",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/MA003 - Dai so tuyen tinh/Bai giang/Chuong 2 - He phuong trinh tuyen tinh.ppt",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/MA003 - Dai so tuyen tinh/Bai tap/Chuong 1.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/MA003 - Dai so tuyen tinh/De cuong mon hoc.pdf");
            case "cau-truc-roi-rac" -> selectedMaterials(
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/MA004 - Cau truc roi rac/Bai giang/Chuong 1. Co so logic.pptx",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/MA004 - Cau truc roi rac/Bai giang/Chuong 2. Phep dem.pptx",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/MA004 - Cau truc roi rac/Bai giang/Chuong 5. Do thi phan 1.ppt",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/MA004 - Cau truc roi rac/Bai tap/Chuong 5.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/MA004 - Cau truc roi rac/Bai tap/Chuong 6.pdf");
            case "nhap-mon-lap-trinh" -> selectedMaterials(
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT001 - Nhap mon lap trinh/Bai giang/00. Tong quan ve may tinh va phan mem may tinh.pptx",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT001 - Nhap mon lap trinh/Bai giang/01. Thuat toan.pptx",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT001 - Nhap mon lap trinh/Bai giang/04. Cau truc lap.pptx",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT001 - Nhap mon lap trinh/On thi/Giua ky.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT001 - Nhap mon lap trinh/On thi/Cuoi ky.pdf");
            case "anh-van-1", "anh-van-2", "anh-van-3" -> selectedMaterials(
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/EC002 - Quan tri doanh nghiep/Chapter 00 Introduction to the course.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/EC002 - Quan tri doanh nghiep/Chapter 01 Overview of An Enterprise.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/EC002 - Quan tri doanh nghiep/Chapter 02 Types of Enterprise Organization.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/EC002 - Quan tri doanh nghiep/Chapter 06 Human Resource Management in Enterprise.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/EC002 - Quan tri doanh nghiep/Chapter 08 Quality Management in The Enterprise.pdf");
            case "he-thong-thong-tin-chuyen-nganh", "thuong-mai-dien-tu", "khoa-hoc-du-lieu" -> selectedMaterials(
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT004 - Co so du lieu/Bai giang/[01] Tong Quan Ve CSDL.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT004 - Co so du lieu/Bai giang/[02] Mo Hinh E-R.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT004 - Co so du lieu/Bai giang/[05] Ngon Ngu SQL.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT004 - Co so du lieu/Thuc hanh/Bai thuc hanh 01.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT004 - Co so du lieu/De thi tham khao/De Thi Cuoi Ky Mon CSDL HK1 2020-2021 (Final).pdf");
            case "an-toan-thong-tin", "mang-may-tinh-truyen-thong-du-lieu" -> selectedMaterials(
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT005 - Nhap mon mang may tinh/Thuc hanh/01. Wireshark Getting Started.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT005 - Nhap mon mang may tinh/Thuc hanh/02. HTTP Protocol.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT005 - Nhap mon mang may tinh/Thuc hanh/03. TCP and UDP.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT005 - Nhap mon mang may tinh/Thuc hanh/06. Scanning WPA-WPA2 Passwords.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/SE301 - Phat trien phan mem ma nguon mo/Bai giang/6. No open-source license.pdf");
            case "ky-thuat-may-tinh-chuyen-nganh", "thiet-ke-vi-mach" -> selectedMaterials(
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT012 - To chuc va Cau truc May tinh II/Thuc hanh/Bai thuc hanh 1.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT012 - To chuc va Cau truc May tinh II/Thuc hanh/Bai thuc hanh 2.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT012 - To chuc va Cau truc May tinh II/Thuc hanh/Bai thuc hanh 3.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT012 - To chuc va Cau truc May tinh II/Thuc hanh/Huong dan su dung Logisim.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/IT012 - To chuc va Cau truc May tinh II/Thuc hanh/Lap trinh hop ngu MIPS.pdf");
            default -> selectedMaterials(
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/SE104 - Nhap mon Cong nghe phan mem/Bai giang/Chuong 1.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/SE104 - Nhap mon Cong nghe phan mem/Bai giang/Chuong 2/Chuong 2.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/SE104 - Nhap mon Cong nghe phan mem/Bai giang/Chuong 3/Chuong 3.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/SE214 - Cong nghe phan mem chuyen sau/Bai giang/Chuong 02. Quy trinh phat trien phan mem.pdf",
                    "https://github.com/phanxuanquang/UIT_SoftwareEngineering_Subjects/blob/main/SE357 - Ky thuat phan tich yeu cau/Bai giang/Requirement Engineering.pdf");
        };
    }

    private List<MaterialSeed> selectedMaterials(String... urls) {
        List<MaterialSeed> materials = new ArrayList<>();
        for (String url : urls) {
            materials.add(new MaterialSeed(titleFromMaterialUrl(url), url));
        }
        return materials;
    }

    private String titleFromMaterialUrl(String url) {
        String decoded = URLDecoder.decode(url, StandardCharsets.UTF_8);
        String fileName = decoded.substring(decoded.lastIndexOf('/') + 1);
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
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

    private record MaterialSeed(
            String title,
            String url) {
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