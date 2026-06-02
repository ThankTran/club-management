import heroAvatar  from '../../assets/avatar/avatar.png'

import about1 from '../../assets/about/about1.png'
import about2 from '../../assets/about/about2.png'
import about3 from '../../assets/about/about3.png'
import about4 from '../../assets/about/about4.png'
import about5 from '../../assets/about/about5.png'

import gallery1  from '../../assets/gallery/gallery1.png'
import gallery2  from '../../assets/gallery/gallery2.png'
import gallery3  from '../../assets/gallery/gallery3.png'
import gallery4  from '../../assets/gallery/gallery4.png'
import gallery5  from '../../assets/gallery/gallery5.png'
import gallery6  from '../../assets/gallery/gallery6.png'
import gallery7  from '../../assets/gallery/gallery7.png'
import gallery8  from '../../assets/gallery/gallery8.png'
import gallery9  from '../../assets/gallery/gallery9.png'

import member1 from '../../assets/member/member1.png'
import member2 from '../../assets/member/member2.png'
import member3 from '../../assets/member/member3.png'
import member4 from '../../assets/member/member4.png'
import member5 from '../../assets/member/member5.png'

export const navLinks = [
  { label: 'Trang chủ',  href: '#hero' },
  { label: 'Giới thiệu', href: '#about' },
  { label: 'Sự kiện',    href: '#events' },
  { label: 'Thành viên', href: '#members' },
  { label: 'Liên hệ',    href: '#footer' },
];

export const heroData = {
  avatar: heroAvatar,  // ảnh hero
  slides: [
    {
      badge: 'Câu Lạc Bộ Học Thuật THMN',
      title: ['Kết nối tri thức, xây dựng tương lai'],
      description: 'Giới thiệu về CLB học thuật — nơi hội tụ những sinh viên đam mê nghiên cứu, học hỏi và cùng nhau phát triển trong môi trường học thuật năng động.',
      cta: 'Tìm hiểu thêm',
    },
    {
      description: 'CLB được thành lập vào năm 2018 bởi nhóm sinh viên có cùng chí hướng. Hơn 6 năm xây dựng và phát triển, chúng tôi tự hào là một trong những CLB học thuật lớn nhất trường.',
    },
    {
      description: 'Chúng tôi tin rằng tri thức không chỉ để tích lũy mà còn để lan tỏa. Mỗi thành viên là một ngọn lửa nhỏ, cùng nhau thắp sáng cả một thế hệ.',
    },
  ],
  stats: { value: '500+', label: 'Thành viên' },
};

export const aboutData = {
  title: 'Giới thiệu',
  description:
    'Đây không chỉ là nơi học hỏi, đây còn là môi trường để bạn kết nối, chia sẻ và phát triển, cùng nhau xây dựng một cộng đồng sinh viên năng động, sáng tạo và đầy cảm hứng.',
  cards: [
    { label: 'Nghiên cứu\nkhoa học', color: '#1a3d2b', height: 200, img: about2 },
    { label: 'Hội thảo\nchuyên đề',  color: '#2d6a4f', height: 160, img: about3 },
    { label: 'Kết nối\nmạng lưới',   color: '#74c69d', height: 210, img: about1 },
    { label: 'Phát triển\nkỹ năng',   color: '#3d5a6c', height: 175, img: about4 },
    { label: 'Học bổng &\ncơ hội',    color: '#52b788', height: 190, img: about5 },
  ],
};

export const eventsData = {
  title: 'Sự kiện',
  description: 'Giới thiệu về các hoạt động sự kiện của CLB — từ hội thảo, tọa đàm đến các buổi workshop thực tế bổ ích.',
  items: [
    {
      title: 'Hội thảo Nghiên cứu Khoa học 2024',
      desc: 'Sự kiện thường niên quy tụ hơn 200 sinh viên tham gia.',
    },
    {
      title: 'Workshop Kỹ năng mềm',
      desc: 'Loạt workshop chuyên sâu về thuyết trình, làm việc nhóm và quản lý thời gian.',
    },
    {
      title: 'Tọa đàm với chuyên gia',
      desc: 'Chuỗi buổi giao lưu cùng các chuyên gia đầu ngành hàng đầu Việt Nam.',
    },
  ],
  cta: 'Xem thêm sự kiện',
};

export const galleryData = {
  title: 'Khoảnh khắc đáng nhớ',
  description: 'Những hình ảnh ghi lại các hoạt động nổi bật của CLB trong suốt hành trình phát triển.',
  images: [
    { id: 1,  label: 'Học thuật',        img: gallery8 },
    { id: 2,  label: 'Workshop',            img: gallery2 },
    { id: 3,  label: 'Giao lưu',            img: gallery3 },
    { id: 4,  label: 'Ngoại khóa',          img: gallery4 },
    { id: 5,  label: 'Tọa đàm',             img: gallery5 },
    { id: 6,  label: 'Văn nghệ',            img: gallery6 },
    { id: 7,  label: 'Kết nạp thành viên',  img: gallery7 },
    { id: 8,  label: 'Sinh hoạt CLB',       img: gallery1 },
    { id: 9,  label: 'Teambuilding',           img: gallery9 },
  ],
  cta: 'Đăng nhập ngay',
};

export const footerData = {
  clubName: 'CLB Học Thuật THMN',
  tagline: 
    'Nơi hội tụ những người trẻ yêu tri thức, luôn khao khát học hỏi và không ngừng vươn lên. Tại đây, mỗi ý tưởng đều được lắng nghe, mỗi nỗ lực đều được ghi nhận, và mỗi thành viên đều có cơ hội cùng nhau tạo nên những giá trị ý nghĩa cho cộng đồng.',
  quickAccess: [
    { label: 'Trang chủ',  href: '#hero' },
    { label: 'Giới thiệu', href: '#about' },
    { label: 'Sự kiện',    href: '#events' },
    { label: 'Thành viên', href: '#members' },
  ],
  support: [
    { label: 'Liên hệ',         href: '#' },
    { label: 'FAQ',              href: '#' },
    { label: 'Hỗ trợ kỹ thuật', href: '#' },
  ],
  legal: [
    { label: 'Chính sách bảo mật', href: '#' },
    { label: 'Điều khoản sử dụng', href: '#' },
  ],
};

export const membersData = [
  { id: 1, name: 'Nguyễn Văn A', role: 'Chủ nhiệm CLB',          img: member1 },
  { id: 2, name: 'Trần Thị B',   role: 'Phó chủ nhiệm',          img: member3 },
  { id: 3, name: 'Lê Văn C',     role: 'Trưởng ban học thuật',    img: member4 },
  { id: 4, name: 'Phạm Thị D',   role: 'Trưởng ban truyền thông', img: member5 },
  { id: 5, name: 'Hoàng Văn E',  role: 'Trưởng ban sự kiện',     img: member2 },
];
