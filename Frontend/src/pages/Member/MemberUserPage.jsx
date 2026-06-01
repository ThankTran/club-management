import { useEffect, useMemo, useState } from 'react';

import MemberTable from '../../components/sections/Member/MemberTable';
import MemberUserFilterBar from '../../components/sections/Member/MemberUserFilterBar';
import {
  getMemberDepartmentsAPI,
  getMembersAPI,
  normalizeMemberFromApi,
} from '../../services/member-service';
import { matchesVietnameseSearch, normalizeVietnameseText } from '../../utils/vietnamese-search';
import styles from './MemberUserPage.module.css';

const PAGE_SIZE = 10;
const STUDENT_ID_KEY = 'id';
const APPROVED_STATUS = 'APPROVED';

const matchesMemberSearch = (member, query) => {
  if (!query) return true;

  const normalizedQuery = normalizeVietnameseText(query);
  if (/^\d/.test(normalizedQuery)) {
    return matchesVietnameseSearch(member[STUDENT_ID_KEY], normalizedQuery);
  }

  return Object.entries(member).some(([key, value]) => (
    key !== STUDENT_ID_KEY &&
    value !== null &&
    typeof value !== 'object' &&
    matchesVietnameseSearch(value, normalizedQuery)
  ));
};

export default function MemberUserPage() {
  const [members, setMembers] = useState([]);
  const [departmentOptions, setDepartmentOptions] = useState([]);
  const [search, setSearch] = useState('');
  const [dept, setDept] = useState('Tất cả');
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [apiError, setApiError] = useState('');

  useEffect(() => {
    let ignore = false;

    Promise.allSettled([getMembersAPI(), getMemberDepartmentsAPI()])
      .then(([membersResult, departmentsResult]) => {
        if (ignore) return;

        if (membersResult.status === 'fulfilled' && Array.isArray(membersResult.value)) {
          const approvedMembers = membersResult.value
            .filter((member) => member.reqStatus === APPROVED_STATUS)
            .map(normalizeMemberFromApi);

          setMembers(approvedMembers);
          setApiError('');
        } else {
          setMembers([]);
          setApiError(membersResult.reason?.message || 'Không tải được danh sách thành viên từ API.');
        }

        if (departmentsResult.status === 'fulfilled' && Array.isArray(departmentsResult.value)) {
          setDepartmentOptions(
            departmentsResult.value
              .map((department) => department.departmentName)
              .filter(Boolean),
          );
        }
      })
      .catch((error) => {
        if (ignore) return;
        setMembers([]);
        setApiError(error?.message || 'Không tải được danh sách thành viên từ API.');
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, []);

  const handleSearchChange = (value) => {
    setSearch(value);
    setPage(1);
  };

  const handleDeptChange = (value) => {
    setDept(value);
    setPage(1);
  };

  const filtered = useMemo(() => {
    const q = search.trim();
    return members.filter((m) => {
      const matchSearch = matchesMemberSearch(m, q);
      const matchDept = dept === 'Tất cả' || m.department === dept;
      return matchSearch && matchDept;
    });
  }, [members, search, dept]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const paginated = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>Danh sách thành viên</h1>
          <p className={styles.pageSubtitle}>
            Tra cứu thông tin các thành viên đã được xét duyệt trong câu lạc bộ.
          </p>
        </div>

        <div className={styles.totalBadge}>
          <span className={styles.totalNum}>{members.length.toLocaleString()}</span>
          <span className={styles.totalLabel}>thành viên</span>
        </div>
      </div>

      {apiError && <p className={styles.errorText}>{apiError}</p>}

      <MemberUserFilterBar
        search={search}
        onSearchChange={handleSearchChange}
        dept={dept}
        onDeptChange={handleDeptChange}
        departments={departmentOptions}
      />

      <MemberTable
        members={paginated}
        total={filtered.length}
        page={page}
        totalPages={totalPages}
        pageSize={PAGE_SIZE}
        onPageChange={(p) => { if (p >= 1 && p <= totalPages) setPage(p); }}
        isAdmin={false}
        showRequestStatus={false}
        loading={loading}
      />
    </div>
  );
}
