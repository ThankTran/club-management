package com.example.demo.ai.service.impl;

import com.example.demo.ai.dto.request.HelpAiRequest;
import com.example.demo.ai.dto.response.HelpAiResponse;
import com.example.demo.user.dto.response.UserResponse;
import com.example.demo.ai.service.interfaces.GroqChatClient;
import com.example.demo.ai.service.interfaces.HelpAiService;
import java.text.Normalizer;
import java.util.List;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HelpAiServiceImpl implements HelpAiService {
    static final int MANAGER_PRIORITY_MAX = 1;
    static final String SOURCE_REAL_AI = "REAL_AI";
    static final String SOURCE_FALLBACK = "FALLBACK";

    final GroqChatClient groqChatClient;

    @Override
    public HelpAiResponse ask(HelpAiRequest request, UserResponse currentUser) {
        String message = request == null || request.getMessage() == null ? "" : request.getMessage().trim();
        String page = request == null || request.getPage() == null || request.getPage().isBlank()
                ? "/help"
                : request.getPage().trim();
        boolean manager = isManager(currentUser);

        try {
            String answer = groqChatClient.complete(buildSystemPrompt(currentUser, manager, page), message);
            if (answer != null && !answer.isBlank()) {
                return HelpAiResponse.builder()
                        .intent("GROQ_HELP")
                        .source(SOURCE_REAL_AI)
                        .answer(answer.trim())
                        .suggestions(defaultSuggestions(manager))
                        .build();
            }
        } catch (RuntimeException ignored) {
            // Keep help usable when Groq is not configured or temporarily unavailable.
        }

        return fallback(message, manager);
    }

    private String buildSystemPrompt(UserResponse user, boolean manager, String page) {
        String roleScope = manager
                ? "Người dùng là manager/admin. Được hướng dẫn các trang: dashboard, memberadmin, resourcesadmin, eventadmin, finance, account, settings, home, profile, help."
                : "Người dùng là member. Chỉ được hướng dẫn các trang: home, profile, resourcesuser, eventuser, memberuser, memberdues, help. Không hướng dẫn thao tác quản trị.";

        String displayName = user == null || user.getFullName() == null ? "người dùng" : user.getFullName();
        return """
                Bạn là Trợ lý AI của hệ thống quản lý CLB THMN.
                Luật bắt buộc:
                - Luôn trả lời bằng tiếng Việt, ngắn gọn, thân thiện, theo từng bước nếu cần.
                - Chỉ hướng dẫn cách sử dụng hệ thống CLB: tài khoản, thành viên, sự kiện, tài liệu, tài chính, thông báo, trợ giúp.
                - Không đọc, đoán, hay tiết lộ dữ liệu riêng tư; không yêu cầu token, mật khẩu, API key.
                - Không hướng dẫn bypass phân quyền, hack, sửa token, gọi API trái phép, hay làm thay thao tác cho người dùng.
                - Nếu câu hỏi vượt ngoài quyền hiện tại, nói rõ người dùng không có quyền và gợi ý liên hệ ban quản trị.
                - Nếu câu hỏi ngoài phạm vi CLB, từ chối nhẹ và kêu người dùng hỏi về hệ thống CLB.
                Người dùng hiện tại: %s.
                Trang hiện tại: %s.
                Phạm vi quyền: %s
                """.formatted(displayName, page, roleScope);
    }

    private HelpAiResponse fallback(String message, boolean manager) {
        String normalized = normalize(message);
        if (!manager && containsAny(normalized, "quan ly", "admin", "duyet", "settings", "memberadmin", "resourcesadmin", "eventadmin")) {
            return response("ACCESS_LIMIT",
                    "Tài khoản của bạn không có quyền quản lý mục này. Bạn có thể dùng các trang dành cho thành viên như Hồ sơ, Sự kiện, Tài liệu, Đóng quỹ và Trợ giúp. Nếu cần thao tác quản trị, hãy liên hệ ban quản trị CLB.",
                    defaultSuggestions(false));
        }
        if (containsAny(normalized, "su kien", "event", "dang ky")) {
            return response("EVENT_HELP",
                    "Bạn vào mục Sự kiện dành cho thành viên, chọn sự kiện đang mở và bấm Đăng ký tham gia. Nếu không thấy nút đăng ký, sự kiện có thể đã kết thúc, chưa được duyệt hoặc đã hết chỗ.",
                    List.of("Vì sao tôi không đăng ký được sự kiện?", "Tôi xem sự kiện đã tham gia ở đâu?"));
        }
        if (containsAny(normalized, "tai lieu", "document", "resource", "upload", "tai len")) {
            return response("DOCUMENT_HELP",
                    "Bạn vào mục Tài liệu để xem tài liệu. Nếu muốn gửi tài liệu, hãy dùng trang tài liệu dành cho thành viên, nhập thông tin tài liệu và tải tệp lên; tài liệu sẽ cần được duyệt trước khi hiển thị rộng rãi.",
                    List.of("Tài liệu cần được duyệt như thế nào?", "Vì sao tôi không thấy tài liệu vừa tải lên?"));
        }
        if (containsAny(normalized, "mat khau", "password", "bao mat")) {
            return response("PASSWORD_HELP",
                    "Bạn vào Hồ sơ hoặc Tài khoản, tìm phần bảo mật để đổi mật khẩu. Không chia sẻ mật khẩu cho bất kỳ ai; nếu quên mật khẩu, hãy liên hệ ban quản trị để được hỗ trợ.",
                    List.of("Tôi quên mật khẩu thì làm sao?", "Làm sao để cập nhật thông tin cá nhân?"));
        }
        if (containsAny(normalized, "tai chinh", "quy", "dong quy", "phi")) {
            String answer = manager
                    ? "Bạn có thể vào mục Tài chính để xem tổng quan thu chi và quản lý các khoản đóng góp theo quyền manager."
                    : "Bạn vào mục Đóng quỹ để xem tình trạng đóng quỹ của mình và làm theo hướng dẫn thanh toán hiển thị trong hệ thống.";
            return response("FINANCE_HELP", answer, defaultSuggestions(manager));
        }
        if (containsAny(normalized, "ho so", "profile", "thong tin", "ca nhan")) {
            return response("PROFILE_HELP",
                    "Bạn vào Hồ sơ để xem và cập nhật thông tin cá nhân. Một số thông tin quan trọng có thể cần ban quản trị xác nhận trước khi thay đổi.",
                    List.of("Làm sao để cập nhật thông tin cá nhân?", "Làm sao để đổi mật khẩu?"));
        }
        return response("OUT_OF_SCOPE",
                "Mình chỉ hỗ trợ các câu hỏi liên quan đến hệ thống quản lý CLB như tài khoản, sự kiện, tài liệu, thành viên, tài chính và trợ giúp. Bạn có thể nói rõ hơn vấn đề đang gặp trong hệ thống không?",
                defaultSuggestions(manager));
    }

    private HelpAiResponse response(String intent, String answer, List<String> suggestions) {
        return HelpAiResponse.builder()
                .intent(intent)
                .source(SOURCE_FALLBACK)
                .answer(answer)
                .suggestions(suggestions)
                .build();
    }

    private List<String> defaultSuggestions(boolean manager) {
        if (manager) {
            return List.of("Quản lý thành viên như thế nào?", "Duyệt tài liệu ở đâu?", "Xem tài chính CLB ở đâu?");
        }
        return List.of("Đăng ký sự kiện như thế nào?", "Tải tài liệu lên hệ thống ra sao?", "Đổi mật khẩu ở đâu?");
    }

    private boolean isManager(UserResponse user) {
        return user != null && user.getRolePriority() != null && user.getRolePriority() <= MANAGER_PRIORITY_MAX;
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase();
    }
}
