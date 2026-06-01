import React, { useState, useEffect, useRef } from "react";
import styles from "./HelpPage.module.css";


import searchIcon from "../../assets/icons/search.svg";
import helpIcon from "../../assets/icons/help.svg";
import membersIcon from "../../assets/icons/members.svg";
import eventsIcon from "../../assets/icons/events.svg";
import resourcesIcon from "../../assets/icons/resources.svg";
import financeIcon from "../../assets/icons/finance.svg";
import VERIFY_ICON from "../../assets/icons/verify.svg";
import infoIcon from "../../assets/icons/infor.svg";
import { askHelpAiAPI } from "../../services/help-ai-service";

const FAQ_DATA = [
  {
    id: 1,
    category: "member",
    question: "Làm thế nào để cập nhật thông tin học tập và chuyên ngành?",
    answer: "Bạn có thể truy cập vào trang 'Trang cá nhân' bằng cách bấm vào avatar ở thanh Navbar hoặc menu, sau đó chọn tab 'Học tập & Chuyên môn'. Tại đây, hãy bấm nút 'Chỉnh sửa hồ sơ' để sửa đổi ngành học, điểm GPA, ban hoạt động và các kỹ năng chuyên môn, sau đó lưu lại.",
  },
  {
    id: 2,
    category: "event",
    question: "Quy trình đăng ký tham gia một sự kiện mới như thế nào?",
    answer: "Bạn hãy chọn mục 'Sự kiện' trên Sidebar để xem danh sách các sự kiện đang diễn ra. Bấm chọn sự kiện mong muốn và nhấn nút 'Đăng ký tham gia'. Hệ thống sẽ tự động cập nhật lịch sử và gửi thông báo xác nhận qua email của bạn.",
  },
  {
    id: 3,
    category: "resource",
    question: "Làm cách nào để tải tài liệu học thuật lên hệ thống?",
    answer: "Vào mục 'Tài liệu' trên Sidebar. Nếu bạn là Quản trị viên hoặc được cấp quyền tải tài liệu, bạn sẽ thấy nút 'Thêm tài liệu'. Hãy điền đầy đủ các thông tin như tên tài liệu, môn học, mô tả ngắn và tải lên file (định dạng PDF, DOCX, ZIP...). Tài liệu sẽ hiển thị cho mọi người sau khi được duyệt.",
  },
  {
    id: 4,
    category: "finance",
    question: "Tôi có thể theo dõi tình hình tài chính và quỹ CLB ở đâu?",
    answer: "Hãy chọn mục 'Thu chi' trên Sidebar. Tại đây bạn sẽ thấy biểu đồ tổng quan quỹ CLB, danh sách các khoản thu đóng góp quỹ và lịch sử chi tiêu minh bạch được cập nhật bởi Ban Tài chính định kỳ hàng tháng.",
  },
  {
    id: 5,
    category: "account",
    question: "Làm sao để thay đổi mật khẩu và cài đặt nhận thông báo?",
    answer: "Bạn vào 'Trang cá nhân', chọn tab 'Bảo mật & Cài đặt'. Tại đây có biểu mẫu đổi mật khẩu và các nút gạt (toggle) để bật/tắt nhận thông báo qua Email hoặc thông báo hoạt động nội bộ CLB.",
  },
  {
    id: 6,
    category: "member",
    question: "Tôi muốn tham gia Ban Học thuật hoặc Ban Truyền thông của CLB thì làm thế nào?",
    answer: "Vào các đợt tuyển thành viên (thường ở đầu học kỳ), Ban Chủ nhiệm sẽ đăng thông báo tuyển dụng trên trang chủ. Bạn có thể nộp đơn trực tuyến hoặc liên hệ trực tiếp trưởng ban học thuật để được hướng dẫn chi tiết về quy trình ứng tuyển.",
  },
  {
    id: 7,
    category: "resource",
    question: "Có giới hạn dung lượng và định dạng cho tài liệu tải lên không?",
    answer: "Có, hệ thống giới hạn dung lượng tối đa là 20MB cho mỗi file tài liệu học thuật để đảm bảo tài nguyên lưu trữ. Các định dạng được hỗ trợ bao gồm PDF, DOCX, XLSX, PPTX, ZIP, và RAR.",
  },
];

const CATEGORIES = [
  { id: "all", label: "Tất cả câu hỏi", icon: helpIcon },
  { id: "member", label: "Thành viên", icon: membersIcon },
  { id: "event", label: "Sự kiện", icon: eventsIcon },
  { id: "resource", label: "Tài liệu", icon: resourcesIcon },
  { id: "finance", label: "Thu chi", icon: financeIcon },
];

export default function HelpPage() {


  const [searchQuery, setSearchQuery] = useState("");
  const [activeCategory, setActiveCategory] = useState("all");
  const [expandedFaq, setExpandedFaq] = useState(null);

  // Right Column Tab: "ai" | "form"
  const [activeRightTab, setActiveRightTab] = useState("ai");

  // Feedback Form State
  const [formState, setFormState] = useState({
    name: "",
    email: "",
    topic: "general",
    message: "",
  });
  const [IS_SUBMITTING, setIsSubmitting] = useState(false);
  const [SUBMIT_SUCCESS, setSubmitSuccess] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");

  // AI Agent States
  const [chatMessages, setChatMessages] = useState([
    {
      id: "bot-welcome-txt",
      sender: "bot",
      text: "Chào khách iu thanh__tr 🌹, hôm nay bạn cần CLB hỗ trợ về điều gì, hãy hỏi ngay để được giải đáp nhanh chóng nhất nha! 🎉🥰",
      isWelcome: true
    },
    {
      id: "bot-welcome-opt",
      sender: "bot",
      isOptions: true
    }
  ]);
  const [isTyping, setIsTyping] = useState(false);
  const [chatInputValue, setChatInputValue] = useState("");
  const [optionsOffset, setOptionsOffset] = useState(0);
  const [isSpinning, setIsSpinning] = useState(false);
  const chatEndRef = useRef(null);
  const chatContainerRef = useRef(null);

  // Auto scroll chat
  useEffect(() => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTo({
        top: chatContainerRef.current.scrollHeight,
        behavior: "smooth"
      });
    }
  }, [chatMessages, isTyping]);

  // AI agent responses dictionary
  const AI_ANSWERS = {
    "Làm thế nào để cập nhật thông tin học tập và chuyên ngành?":
      "Để cập nhật chuyên ngành và học tập, bạn làm theo các bước sau nhé:\n1. Bấm vào Avatar góc trên cùng bên phải màn hình để chọn **Hồ sơ**.\n2. Chọn tab **Học tập & Chuyên môn**.\n3. Tại mục Kỹ năng học thuật, bạn có thể gõ tìm kiếm và nhấn nút **+ Thêm tag** để cập nhật nhanh chóng! 🎓",

    "Quy trình đăng ký tham gia một sự kiện mới như thế nào?":
      "Quy trình đăng ký sự kiện vô cùng đơn giản:\n1. Chọn mục **Sự kiện** trên Sidebar bên trái.\n2. Duyệt qua danh sách sự kiện và bấm vào sự kiện bạn muốn tham gia.\n3. Nhấn **Đăng ký tham gia**. Hệ thống sẽ tự động gửi email xác nhận và lưu lịch sử vào hoạt động của bạn! 📅",

    "Làm cách nào để tải tài liệu học thuật lên hệ thống?":
      "Để chia sẻ tài liệu hữu ích cho CLB:\n1. Di chuyển tới mục **Tài liệu** trên thanh điều hướng.\n2. Bấm nút **Thêm tài liệu** ở góc trên.\n3. Điền các trường thông tin (tên môn, mô tả) và tải lên file (định dạng PDF, DOCX, ZIP dưới 20MB).\n*Lưu ý: Tài liệu sẽ hiển thị công khai sau khi được Ban Quản Trị duyệt nhé!* 📚",

    "Tôi có thể theo dõi tình hình tài chính và quỹ CLB ở đâu?":
      "Tất cả các khoản thu chi đều minh bạch tại CLB:\n1. Hãy truy cập mục **Thu chi** trên Sidebar.\n2. Tại đây có biểu đồ trực quan, danh sách đóng góp quỹ và nhật ký chi tiêu của Ban Tài chính được cập nhật mỗi tháng! 💰",

    "Làm sao để thay đổi mật khẩu và cài đặt nhận thông báo?":
      "Để nâng cao bảo mật tài khoản:\n1. Vào trang **Cá nhân** bằng menu góc phải.\n2. Chọn tab **Bảo mật & Cài đặt**.\n3. Tại đây bạn có thể đổi mật khẩu mới hoặc gạt các toggle bật/tắt nhận thông báo qua Email hoặc hệ thống! 🔑",

    "Tôi muốn tham gia Ban Học thuật hoặc Ban Truyền thông của CLB thì làm thế nào?":
      "Chào mừng bạn đến với các Ban hoạt động của CLB! Đơn đăng ký thường mở vào đầu học kỳ trên Trang chủ. Bạn hãy cập nhật thông tin CV trong Hồ sơ cá nhân trước, sau đó nộp đơn ứng tuyển online trực tiếp hoặc liên hệ các Trưởng Ban để được phỏng vấn nhé! 🤝",

    "Có giới hạn dung lượng và định dạng cho tài liệu tải lên không?":
      "Dung lượng tối đa được hỗ trợ là **20MB** mỗi tệp để bảo toàn máy chủ. Các định dạng được phép tải lên bao gồm: `PDF`, `DOCX`, `XLSX`, `PPTX`, `ZIP`, `RAR`. Chú ý quét virus trước khi đăng bạn nhé! 💾",

    "Tôi muốn liên hệ gấp với Ban Quản Trị?":
      "Bạn có thể liên hệ trực tiếp với Ban Quản Trị qua các kênh ưu tiên sau:\n- 📞 Hotline: **0987654321**\n- ✉️ Email: **24521092@gm.uit.edu.vn**\n- 🏢 Văn phòng: **Tầng 7, tòa E, trường ĐH Công nghệ Thông tin (UIT)**. Chúng mình luôn sẵn sàng lắng nghe bạn! 🏢",

    "Làm sao để đóng lệ phí sinh hoạt CLB?":
      "Để đóng quỹ thành viên CLB:\n1. Vào mục **Thu chi**.\n2. Tìm phần đóng lệ phí định kỳ.\n3. Bấm **Thanh toán trực tuyến** hoặc chuyển khoản theo cú pháp hướng dẫn kèm mã QR được tạo tự động nhé! 💳",
  };

  const ALL_AGENT_QUESTIONS = Object.keys(AI_ANSWERS);

  const SUGGESTION_PILLS = [
    { label: "Lỗi khóa/giới hạn tài khoản 🔒", query: "Tôi muốn liên hệ gấp với Ban Quản Trị?" },
    { label: "Tham gia sự kiện 📅", query: "Quy trình đăng ký tham gia một sự kiện mới như thế nào?" },
    { label: "Tải tài liệu 📚", query: "Làm cách nào để tải tài liệu học thuật lên hệ thống?" },
    { label: "Thay đổi thông tin cá nhân 🎓", query: "Làm thế nào để cập nhật thông tin học tập và chuyên ngành?" },
    { label: "Thay mật khẩu 🔑", query: "Làm sao để thay đổi mật khẩu và cài đặt nhận thông báo?" }
  ];

  const getDisplayedQuestions = () => {
    const list = [];
    for (let i = 0; i < 4; i++) {
      list.push(ALL_AGENT_QUESTIONS[(optionsOffset + i) % ALL_AGENT_QUESTIONS.length]);
    }
    return list;
  };

  const handleRotateQuestions = () => {
    setIsSpinning(true);
    setOptionsOffset(prev => prev + 4);
    setTimeout(() => {
      setIsSpinning(false);
    }, 500);
  };

  const getAiAnswer = async (userQuery) => {
    try {
      const response = await askHelpAiAPI(userQuery, "/help");
      if (response?.answer) {
        return {
          answer: response.answer,
          source: response.source || "FALLBACK"
        };
      }
    } catch {
      return {
        answer: "AI đang bận, bạn thử lại sau hoặc gửi yêu cầu hỗ trợ.",
        source: "FALLBACK"
      };
    }

    const query = userQuery.toLowerCase().trim();
    if (!query) return { answer: null, source: "FALLBACK" };

    // Check exact or direct match of questions
    for (const question of ALL_AGENT_QUESTIONS) {
      if (question.toLowerCase().includes(query) || query.includes(question.toLowerCase())) {
        return { answer: AI_ANSWERS[question], source: "FALLBACK" };
      }
    }

    // Keyword match logic
    if (query.includes("sự kiện") || query.includes("event") || query.includes("đăng ký")) {
      return { answer: AI_ANSWERS["Quy trình đăng ký tham gia một sự kiện mới như thế nào?"], source: "FALLBACK" };
    }
    if (query.includes("tài liệu") || query.includes("resource") || query.includes("tải lên") || query.includes("pdf")) {
      if (query.includes("dung lượng") || query.includes("giới hạn") || query.includes("mb") || query.includes("định dạng")) {
        return { answer: AI_ANSWERS["Có giới hạn dung lượng và định dạng cho tài liệu tải lên không?"], source: "FALLBACK" };
      }
      return { answer: AI_ANSWERS["Làm cách nào để tải tài liệu học thuật lên hệ thống?"], source: "FALLBACK" };
    }
    if (query.includes("cập nhật") || query.includes("thông tin") || query.includes("chuyên ngành") || query.includes("major") || query.includes("học tập")) {
      return { answer: AI_ANSWERS["Làm thế nào để cập nhật thông tin học tập và chuyên ngành?"], source: "FALLBACK" };
    }
    if (query.includes("tài chính") || query.includes("quỹ") || query.includes("thu chi") || query.includes("tiền") || query.includes("phí")) {
      if (query.includes("đóng") || query.includes("nộp")) {
        return { answer: AI_ANSWERS["Làm sao để đóng lệ phí sinh hoạt CLB?"], source: "FALLBACK" };
      }
      return { answer: AI_ANSWERS["Tôi có thể theo dõi tình hình tài chính và quỹ CLB ở đâu?"], source: "FALLBACK" };
    }
    if (query.includes("mật khẩu") || query.includes("cài đặt") || query.includes("bảo mật") || query.includes("thông báo")) {
      return { answer: AI_ANSWERS["Làm sao để thay đổi mật khẩu và cài đặt nhận thông báo?"], source: "FALLBACK" };
    }
    if (query.includes("tuyển") || query.includes("tham gia ban") || query.includes("truyền thông") || query.includes("học thuật") || query.includes("vào clb")) {
      return { answer: AI_ANSWERS["Tôi muốn tham gia Ban Học thuật hoặc Ban Truyền thông của CLB thì làm thế nào?"], source: "FALLBACK" };
    }
    if (query.includes("liên hệ") || query.includes("admin") || query.includes("hotline") || query.includes("email") || query.includes("chủ nhiệm") || query.includes("gấp")) {
      return { answer: AI_ANSWERS["Tôi muốn liên hệ gấp với Ban Quản Trị?"], source: "FALLBACK" };
    }

    return {
      answer: "Cảm ơn bạn đã đặt câu hỏi. Hệ thống chưa tìm thấy tài liệu hướng dẫn khớp hoàn toàn với câu hỏi của bạn.\n\nBạn có thể gửi yêu cầu trực tiếp qua tab **Gửi yêu cầu hỗ trợ** bên cạnh hoặc liên hệ nhanh với Ban Quản trị qua Hotline **0987654321** để được giải đáp tức thì nhé! 💖",
      source: "FALLBACK"
    };
  };

  const handleSelectQuestion = (question) => {
    if (isTyping) return;

    // User message
    const userMsg = {
      id: `user-${Date.now()}`,
      sender: "user",
      text: question
    };

    setChatMessages(prev => [...prev, userMsg]);
    setIsTyping(true);

    // AI response delay
    setTimeout(async () => {
      const result = await getAiAnswer(question);
      const botMsg = {
        id: `bot-${Date.now()}`,
        sender: "bot",
        text: result.answer,
        source: result.source
      };
      setChatMessages(prev => [...prev, botMsg]);
      setIsTyping(false);
    }, 750);
  };

  const handleSendCustomText = (e) => {
    e.preventDefault();
    const text = chatInputValue.trim();
    if (!text || isTyping) return;

    const userMsg = {
      id: `user-${Date.now()}`,
      sender: "user",
      text: text
    };

    setChatMessages(prev => [...prev, userMsg]);
    setChatInputValue("");
    setIsTyping(true);

    setTimeout(async () => {
      const result = await getAiAnswer(text);
      const botMsg = {
        id: `bot-${Date.now()}`,
        sender: "bot",
        text: result.answer,
        source: result.source
      };
      setChatMessages(prev => [...prev, botMsg]);
      setIsTyping(false);
    }, 750);
  };

  const toggleFaq = (id) => {
    if (expandedFaq === id) {
      setExpandedFaq(null);
    } else {
      setExpandedFaq(id);
    }
  };

  const HANDLE_FORM_CHANGE = (e) => {
    const { name, value } = e.target;
    setFormState((prev) => ({
      ...prev,
      [name]: value,
    }));
    if (errorMsg) setErrorMsg("");
  };

  const HANDLE_FORM_SUBMIT = (e) => {
    e.preventDefault();
    const { name, email, message } = formState;

    if (!name.trim() || !email.trim() || !message.trim()) {
      setErrorMsg("Vui lòng nhập đầy đủ các trường thông tin bắt buộc.");
      return;
    }

    if (!email.includes("@")) {
      setErrorMsg("Email không đúng định dạng.");
      return;
    }

    setIsSubmitting(true);

    // Simulate API call
    setTimeout(() => {
      setIsSubmitting(false);
      setSubmitSuccess(true);
      setFormState({
        name: "",
        email: "",
        topic: "general",
        message: "",
      });

      // Auto dismiss success screen after 6 seconds
      setTimeout(() => {
        setSubmitSuccess(false);
      }, 6000);
    }, 1500);
  };

  // Filter FAQs
  const filteredFaqs = FAQ_DATA.filter((faq) => {
    const matchesSearch =
      faq.question.toLowerCase().includes(searchQuery.toLowerCase()) ||
      faq.answer.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory =
      activeCategory === "all" || faq.category === activeCategory;
    return matchesSearch && matchesCategory;
  });

  return (
    <div className={styles.container}>
      {/* Help Banner Section */}
      <div className={styles.banner}>
        <div className={styles.bannerOverlay}></div>
        <div className={styles.bannerContent}>
          <h1 className={styles.bannerTitle}>Chúng tôi có thể giúp gì cho bạn?</h1>
          <p className={styles.bannerSubtitle}>
            Tìm kiếm câu trả lời nhanh chóng hoặc gửi yêu cầu hỗ trợ đến Ban quản trị câu lạc bộ.
          </p>
          {activeRightTab === "form" && (
            <div className={styles.searchBox}>
              <img src={searchIcon} alt="Search" className={styles.searchIcon} />
              <input
                type="text"
                placeholder="Nhập từ khóa tìm kiếm (ví dụ: đóng quỹ, tài liệu, sự kiện...)"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className={styles.searchInput}
              />
              {searchQuery && (
                <button
                  className={styles.clearSearch}
                  onClick={() => setSearchQuery("")}
                  title="Xóa tìm kiếm"
                >
                  ×
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Main Grid */}
      <div className={`${styles.gridContent} ${activeRightTab === "ai" ? styles.gridFullWidth : ""}`}>
        {/* Left Column: FAQ Accordion & Category Tabs */}
        <div className={`${styles.faqColumn} ${activeRightTab === "ai" ? styles.faqColumnHidden : ""}`}>
          <h2 className={styles.sectionTitle}>
            <img src={infoIcon} alt="FAQs" className={styles.sectionTitleIcon} />
            Câu hỏi thường gặp
          </h2>

          {/* Categories Tab Bar */}
          <div className={styles.categoryTabs}>
            {CATEGORIES.map((cat) => (
              <button
                key={cat.id}
                className={`${styles.tabBtn} ${activeCategory === cat.id ? styles.activeTab : ""
                  }`}
                onClick={() => {
                  setActiveCategory(cat.id);
                  setExpandedFaq(null);
                }}
              >
                <img src={cat.icon} alt="" className={styles.tabIcon} />
                <span>{cat.label}</span>
              </button>
            ))}
          </div>

          {/* FAQ Accordion List */}
          <div className={styles.faqList}>
            {filteredFaqs.length > 0 ? (
              filteredFaqs.map((faq) => {
                const isOpen = expandedFaq === faq.id;
                return (
                  <div
                    key={faq.id}
                    className={`${styles.faqItem} ${isOpen ? styles.faqOpen : ""}`}
                  >
                    <button
                      className={styles.faqQuestionBtn}
                      onClick={() => toggleFaq(faq.id)}
                      aria-expanded={isOpen}
                    >
                      <span className={styles.faqQuestionText}>{faq.question}</span>
                      <span className={styles.faqArrow}>{isOpen ? "▲" : "▼"}</span>
                    </button>
                    <div
                      className={styles.faqAnswerContainer}
                      style={{
                        maxHeight: isOpen ? "200px" : "0",
                        opacity: isOpen ? 1 : 0,
                      }}
                    >
                      <p className={styles.faqAnswerText}>{faq.answer}</p>
                    </div>
                  </div>
                );
              })
            ) : (
              <div className={styles.noResults}>
                <p>Không tìm thấy câu hỏi phù hợp với từ khóa của bạn.</p>
                <button
                  className={styles.resetSearchBtn}
                  onClick={() => {
                    setSearchQuery("");
                    setActiveCategory("all");
                  }}
                >
                  Xóa bộ lọc và thử lại
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Right Column: AI Agent Chat & Traditional Form */}
        <div className={styles.formColumn}>
          {/* Navigation tabs for AI Agent vs Traditional Ticket Form */}
          <div className={styles.rightTabsContainer}>
            <button
              className={`${styles.rightTabBtn} ${activeRightTab === "ai" ? styles.rightTabActive : ""}`}
              onClick={() => setActiveRightTab("ai")}
            >
              <span className={styles.tabIconEmoji}>🤖</span>
              Trợ lý AI Agent
            </button>
            <button
              className={`${styles.rightTabBtn} ${activeRightTab === "form" ? styles.rightTabActive : ""}`}
              onClick={() => setActiveRightTab("form")}
            >
              <span className={styles.tabIconEmoji}>📝</span>
              Gửi yêu cầu
            </button>
          </div>

          {activeRightTab === "ai" ? (
            /* AI Agent Chat Interface (Shopee CS Style) */
            <div className={styles.aiAgentWrapper}>
              {/* Header */}
              <div className={styles.aiAgentHeader}>
                <div className={styles.aiHeaderLeft}>
                  <div className={styles.aiTitleBlock}>
                    <h3 className={styles.aiHeaderTitle}>Trợ lý AI</h3>
                    <div className={styles.aiStatusLabel}>
                      <span className={styles.aiPulseDot}></span>
                      <span>Trực tuyến</span>
                    </div>
                  </div>
                </div>
                <span className={styles.aiPriorityBadge}>⭐ ƯU TIÊN</span>
              </div>

              {/* Chat Scroll Area */}
              <div className={styles.aiChatContent} ref={chatContainerRef}>
                {chatMessages.map((msg) => {
                  if (msg.sender === "bot") {
                    return (
                      <div key={msg.id} className={styles.botMessageRow}>
                        <div className={styles.botAvatar}>
                          <span>AI</span>
                        </div>
                        <div className={styles.botMessageContentBlock}>
                          {msg.isWelcome ? (
                            <div className={styles.botBubbleWelcome}>
                              Chào khách iu <strong>thanh__tr 🌹</strong>, hôm nay bạn cần CLB hỗ trợ về điều gì, hãy hỏi ngay để được giải đáp nhanh chóng nhất nha! 🎉🥰
                            </div>
                          ) : msg.text ? (
                            <div className={styles.botBubbleMessage}>
                              {msg.source && (
                                <span className={`${styles.aiSourceBadge} ${msg.source === "REAL_AI" ? styles.aiSourceReal : styles.aiSourceFallback}`}>
                                  {msg.source === "REAL_AI" ? "Trợ lý" : "Hỗ trợ tự động"}
                                </span>
                              )}
                              {msg.text.split("\n").map((line, lIdx) => (
                                <p key={lIdx} style={{ margin: line.trim() === "" ? "10px 0" : "4px 0" }}>
                                  {line}
                                </p>
                              ))}
                            </div>
                          ) : null}

                          {msg.isOptions && (
                            <div className={styles.botOptionsCard}>
                              <h4 className={styles.optionsTitle}>Bạn muốn hỏi về:</h4>
                              <div className={styles.optionsList}>
                                {getDisplayedQuestions().map((question, qIdx) => (
                                  <button
                                    key={qIdx}
                                    className={styles.optionQuestionLink}
                                    onClick={() => handleSelectQuestion(question)}
                                    disabled={isTyping}
                                  >
                                    {question}
                                  </button>
                                ))}
                              </div>
                              <div className={styles.optionsDivider}></div>
                              <button
                                className={styles.rotateQuestionsBtn}
                                onClick={handleRotateQuestions}
                                disabled={isTyping}
                              >
                                <span className={`${styles.rotateIcon} ${isSpinning ? styles.spinAnimation : ""}`}>🔄</span>
                                Đổi câu hỏi
                              </button>
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  } else {
                    return (
                      <div key={msg.id} className={styles.userMessageRow}>
                        <div className={styles.userBubbleMessage}>{msg.text}</div>
                      </div>
                    );
                  }
                })}

                {isTyping && (
                  <div className={styles.botMessageRow}>
                    <div className={styles.botAvatar}>
                      <span>AI</span>
                    </div>
                    <div className={styles.typingIndicatorBubble}>
                      <span className={styles.typingDot}></span>
                      <span className={styles.typingDot}></span>
                      <span className={styles.typingDot}></span>
                    </div>
                  </div>
                )}
                <div ref={chatEndRef} />
              </div>

              {/* Horizontal Scroll Suggestion Pills Slider */}
              <div className={styles.suggestionPillsScrollWrapper}>
                <div className={styles.suggestionPillsList}>
                  {SUGGESTION_PILLS.map((pill, idx) => (
                    <button
                      key={idx}
                      className={styles.suggestionPillBtn}
                      onClick={() => handleSelectQuestion(pill.query)}
                      disabled={isTyping}
                    >
                      {pill.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Input Area */}
              <form onSubmit={handleSendCustomText} className={styles.aiInputForm}>
                <input
                  type="text"
                  placeholder="Nhập yêu cầu của bạn tại đây nhé"
                  value={chatInputValue}
                  onChange={(e) => setChatInputValue(e.target.value)}
                  className={styles.aiInputField}
                  disabled={isTyping}
                />
                <button
                  type="submit"
                  className={styles.aiSendBtn}
                  disabled={!chatInputValue.trim() || isTyping}
                  title="Gửi câu hỏi"
                >
                  <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                    <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
                  </svg>
                </button>
              </form>

              <div className={styles.aiFooterDisclaimer}>
                Được tạo bởi AI, áp dụng <span className={styles.disclaimerUnderline}>điều khoản dịch vụ</span>.
              </div>
            </div>
          ) : (
            /* Traditional Support / Feedback Form Tab */
            <>
              {/* Quick Contact Cards */}
              <div className={styles.contactSection}>
                <h3 className={styles.sideTitle}>Liên hệ hỗ trợ nhanh</h3>
                <div className={styles.contactCards}>
                  <div className={styles.contactCard}>
                    <span className={styles.contactLabel}>Email Hỗ trợ:</span>
                    <a href="mailto:24521092@gm.uit.edu.vn" className={styles.contactValue}>
                      24521092@gm.uit.edu.vn
                    </a>
                  </div>
                  <div className={styles.contactCard}>
                    <span className={styles.contactLabel}>Hotline:</span>
                    <span className={styles.contactValue}>0987654321</span>
                  </div>
                  <div className={styles.contactCard}>
                    <span className={styles.contactLabel}>Văn phòng CLB:</span>
                    <span className={styles.contactValue}>Tầng 7, tòa E, UIT</span>
                  </div>
                </div>
              </div>

            </>
          )}
        </div>
      </div>
    </div>
  );
}
