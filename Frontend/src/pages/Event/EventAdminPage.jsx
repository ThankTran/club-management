import { useEffect, useMemo, useRef, useState } from 'react';

import EventForm from '../../components/sections/Event/EventForm';
import exportEventsExcel from '../../utils/Export/exportEventsExcel';

import EventAdminHeader from '../../components/sections/Event/EventAdminHeader';
import EventDeleteConfirmModal from '../../components/sections/Event/EventDeleteConfirmModal';
import ActionToast from '../../components/common/ActionToast/ActionToast';
import EventEvaluationHistoryModal from '../../components/sections/Event/EventEvaluationHistoryModal';
import EventEvaluationModal from '../../components/sections/Event/EventEvaluationModal';
import EventFiscalSummary from '../../components/sections/Event/EventFiscalSummary';
import EventRegistrationModal from '../../components/sections/Event/EventRegistrationModal';
import EventRosterTable from '../../components/sections/Event/EventRosterTable';
import exportEventRegistrationsExcel from '../../utils/Export/exportEventRegistrationsExcel';
import {
  createEventAPI,
  createEventEvaluationAPI,
  deleteEventAPI,
  getEventRegistrationsByEventAPI,
  getEventsAPI,
  normalizeEventRegistrationFromApi,
  normalizeEventFromApi,
  toEventPayload,
  updateEventAPI,
} from '../../services/event-service';
import {
  getMembersAPI,
  normalizeMemberFromApi,
} from '../../services/member-service';
import {
  PAGE_SIZE,
  STATUS_LABEL,
  TAG_FILTERS,
  TAG_LABEL,
} from '../../data/Event/eventAdminData';
import {
  createEvaluationCode,
} from '../../utils/Event/eventAdminUtils';
import styles from './EventAdminPage.module.css';
import useActionToast from '../../hooks/useActionToast';

const getTodayDateInputValue = () => {
  const today = new Date();
  const localDate = new Date(today.getTime() - today.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
};

const isPastEventDate = (date) => {
  if (!date) return false;
  const eventDate = new Date(date);
  if (Number.isNaN(eventDate.getTime())) return false;
  eventDate.setHours(0, 0, 0, 0);

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return eventDate < today;
};

const canAutoCompleteEvent = (event) =>
  ['draft', 'upcoming', 'published'].includes(event.status) && isPastEventDate(event.date);

const buildEvaluationsFromEvents = (events) =>
  events
    .filter((event) => event.evaluation)
    .map((event, index) => ({
      id: createEvaluationCode(event.eventCode, index),
      eventCode: event.eventCode,
      eventTitle: event.title,
      evaluationDate: event.evaluationDate,
      evaluation: event.evaluation,
    }));

const isFinishedEventStatus = (status) => status === 'completed' || status === 'evaluated';

const hasEventEvaluation = (event, evaluations = []) => {
    const hasInline =
        typeof event?.evaluation === 'string' && event.evaluation.trim().length > 0;
    const hasInList = evaluations.some((item) => item.eventCode === event?.eventCode);
    return hasInline || hasInList;
};

const getDisplayStatus = (event, evaluations = []) =>
  isFinishedEventStatus(event.status) && hasEventEvaluation(event, evaluations)
    ? 'evaluated'
    : event.status;

const normalizeRegistrationList = (data) =>
  Array.isArray(data) ? data.map(normalizeEventRegistrationFromApi) : [];

const applyRegistrationCounts = (events, registrationsByEvent) =>
  events.map((event) => {
    const members = registrationsByEvent[event.id];
    return {
      ...event,
      attendance: Array.isArray(members) ? members.length : Number(event.attendance || 0),
    };
  });

const fetchRegistrationsForEvents = async (events) => {
  const results = await Promise.all(
    events.map(async (event) => {
      try {
        const data = await getEventRegistrationsByEventAPI(event.id);
        return {
          eventId: event.id,
          members: normalizeRegistrationList(data),
          failed: false,
        };
      } catch {
        return {
          eventId: event.id,
          members: null,
          failed: true,
        };
      }
    }),
  );

  return {
    registrationsByEvent: results.reduce((acc, item) => {
      if (item.members) acc[item.eventId] = item.members;
      return acc;
    }, {}),
    failed: results.some((item) => item.failed),
  };
};

const countApprovedMembers = (data) =>
  Array.isArray(data)
    ? data.map(normalizeMemberFromApi).filter((member) => member.requestStatus === 'Đã duyệt').length
    : 0;

export default function EventAdminPage() {
  const [events, setEvents] = useState([]);
  const [apiError, setApiError] = useState('');
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [tagFilter, setTagFilter] = useState('all');
  const [dateFromFilter, setDateFromFilter] = useState('');
  const [dateToFilter, setDateToFilter] = useState('');
  const [filterOpen, setFilterOpen] = useState(false);
  const [formOpen, setFormOpen] = useState(false);
  const [editTarget, setEditTarget] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const { toast, showPending, showSuccess, showError } = useActionToast();
  const [dateSort, setDateSort] = useState('nearest');
  const [deleteConfirm, setDeleteConfirm] = useState(null);
  const [evaluationTarget, setEvaluationTarget] = useState(null);
  const [evaluationForm, setEvaluationForm] = useState({ evaluationDate: '', evaluation: '' });
  const [evaluationErrors, setEvaluationErrors] = useState({});
  const [evaluations, setEvaluations] = useState([]);
  const [evaluationHistoryOpen, setEvaluationHistoryOpen] = useState(false);
  const [registrationTarget, setRegistrationTarget] = useState(null);
  const [registeredMembers, setRegisteredMembers] = useState([]);
  const [registrationsByEvent, setRegistrationsByEvent] = useState({});
  const [memberCount, setMemberCount] = useState(0);
  const autoCompletingIdsRef = useRef(new Set());

  useEffect(() => {
    let ignore = false;

    Promise.allSettled([getEventsAPI(), getMembersAPI()])
      .then(async ([eventsResult, membersResult]) => {
        if (eventsResult.status !== 'fulfilled') {
          throw eventsResult.reason;
        }

        const nextEvents = Array.isArray(eventsResult.value)
          ? eventsResult.value.map(normalizeEventFromApi)
          : [];
        const {
          registrationsByEvent: nextRegistrationsByEvent,
          failed,
        } = await fetchRegistrationsForEvents(nextEvents);
        const nextMemberCount = membersResult.status === 'fulfilled'
          ? countApprovedMembers(membersResult.value)
          : 0;

        if (ignore) return;
        setMemberCount(nextMemberCount);
        setRegistrationsByEvent(nextRegistrationsByEvent);
        setEvents(applyRegistrationCounts(nextEvents, nextRegistrationsByEvent));
        setEvaluations(buildEvaluationsFromEvents(nextEvents));
        setApiError(
          failed || membersResult.status !== 'fulfilled'
            ? 'Không tải được một số dữ liệu đăng ký hoặc thành viên.'
            : '',
        );
      })
      .catch((error) => {
        if (ignore) return;
        setApiError(error?.message || 'Không tải được danh sách sự kiện từ API.');
        setEvents([]);
        setEvaluations([]);
        setRegistrationsByEvent({});
        setMemberCount(0);
      });

    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {
    if (!registrationTarget) {
      setRegisteredMembers([]);
      return;
    }

    let ignore = false;
    getEventRegistrationsByEventAPI(registrationTarget.id)
      .then((data) => {
        if (ignore) return;
        const nextMembers = normalizeRegistrationList(data);
        setRegisteredMembers(nextMembers);
        setRegistrationsByEvent((prev) => ({ ...prev, [registrationTarget.id]: nextMembers }));
        setEvents((prev) =>
          prev.map((event) =>
            event.id === registrationTarget.id
              ? { ...event, attendance: nextMembers.length }
              : event,
          ),
        );
      })
      .catch((error) => {
        if (ignore) return;
        setRegisteredMembers([]);
        setApiError(error?.message || 'Không tải được danh sách đăng ký sự kiện.');
      });

    return () => {
      ignore = true;
    };
  }, [registrationTarget]);

  useEffect(() => {
    const overdueEvents = events.filter(
      (event) => canAutoCompleteEvent(event) && !autoCompletingIdsRef.current.has(event.id),
    );

    overdueEvents.forEach((event) => {
      autoCompletingIdsRef.current.add(event.id);
      updateEventAPI(
        event.id,
        toEventPayload({
          ...event,
          status: 'completed',
          evaluationDate: '',
          evaluation: '',
        }),
      )
        .then((updated) => {
          const nextEvent = applyRegistrationCounts(
            [normalizeEventFromApi(updated)],
            registrationsByEvent,
          )[0];
          setEvents((prev) =>
            prev.map((item) =>
              item.id === event.id
                ? { ...nextEvent, status: 'completed', evaluationDate: '', evaluation: '' }
                : item,
            ),
          );
          setEvaluations((prev) => prev.filter((item) => item.eventCode !== event.eventCode));
        })
        .catch((error) => {
          autoCompletingIdsRef.current.delete(event.id);
          setApiError(error?.message || 'KhÃ´ng tá»± Ä‘á»™ng cáº­p nháº­t Ä‘Æ°á»£c sá»± kiá»‡n Ä‘Ã£ quÃ¡ ngÃ y.');
        });
    });
  }, [events, registrationsByEvent]);

  const totalEstimated = events.reduce((sum, event) => sum + (Number(event.estimatedCost) || 0), 0);
  const totalActual = events
    .filter((event) => isFinishedEventStatus(event.status))
    .reduce((sum, event) => sum + (Number(event.estimatedCost) * 0.9 || 0), 0);
  const publishedCount = events.filter((event) => event.status === 'published').length;
  const evaluatedEvents = useMemo(
    () =>
      events.filter(
        (event) => isFinishedEventStatus(event.status) && hasEventEvaluation(event, evaluations),
      ),
    [events, evaluations],
  );

  const filtered = useMemo(() => {
    const query = search.toLowerCase();

    const result = events.filter((event) => {
      const matchSearch =
        event.title.toLowerCase().includes(query) ||
        event.location.toLowerCase().includes(query);
      const matchStatus =
        statusFilter === 'all' ||
        (statusFilter === 'completed'
          ? isFinishedEventStatus(event.status)
          : getDisplayStatus(event, evaluations) === statusFilter);
      const matchTag = tagFilter === 'all' || event.tag === tagFilter;
      const matchDateFrom = !dateFromFilter || event.date >= dateFromFilter;
      const matchDateTo = !dateToFilter || event.date <= dateToFilter;

      return matchSearch && matchStatus && matchTag && matchDateFrom && matchDateTo;
    });

    result.sort((a, b) => {
      const dateA = new Date(a.date);
      const dateB = new Date(b.date);
      return dateSort === 'nearest' ? dateA - dateB : dateB - dateA;
    });

    return result;
  }, [events, evaluations, search, statusFilter, tagFilter, dateFromFilter, dateToFilter, dateSort]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const paginated = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);
  const rosterSummary = useMemo(() => {
    const activityCount = filtered.length;
    const totalAttendance = filtered.reduce((sum, event) => sum + Number(event.attendance || 0), 0);
    const totalMemberSlots = memberCount * activityCount;
    const attendanceRate = totalMemberSlots > 0 ? Math.round((totalAttendance / totalMemberSlots) * 100) : 0;

    return { activityCount, totalAttendance, attendanceRate };
  }, [filtered, memberCount]);
  const openAdd = () => {
    setEditTarget(null);
    setFormOpen(true);
  };

  const openEdit = (event) => {
    setEditTarget(event);
    setFormOpen(true);
  };

  const openRegistrations = (event) => {
    setRegistrationTarget(event);
    setRegisteredMembers(registrationsByEvent[event.id] || []);
  };

  const applyFilters = ({
    statusFilter: nextStatusFilter,
    tagFilter: nextTagFilter,
    dateFromFilter: nextDateFromFilter,
    dateToFilter: nextDateToFilter,
  }) => {
    setStatusFilter(nextStatusFilter);
    setTagFilter(nextTagFilter);
    setDateFromFilter(nextDateFromFilter);
    setDateToFilter(nextDateToFilter);
    setPage(1);
  };

  const handleSubmit = async (formData) => {
    setFormLoading(true);
    try {
      if (editTarget) {
        const updated = await updateEventAPI(editTarget.id, toEventPayload(formData));
        const nextEvent = applyRegistrationCounts(
          [normalizeEventFromApi(updated)],
          registrationsByEvent,
        )[0];
        setEvents((prev) =>
          prev.map((event) => (event.id === editTarget.id ? nextEvent : event)),
        );
      } else {
        const created = await createEventAPI(toEventPayload({ ...formData, status: 'draft' }));
        const nextEvent = { ...normalizeEventFromApi(created), attendance: 0 };
        setRegistrationsByEvent((prev) => ({ ...prev, [nextEvent.id]: [] }));
        setEvents((prev) => [
          nextEvent,
          ...prev,
        ]);
      }
      setFormOpen(false);
      setApiError('');
    } catch (error) {
      setApiError(error?.message || 'Không lưu được sự kiện.');
    } finally {
      setFormLoading(false);
    }
  };

    const updateEventStatus = async (eventId, status) => {
        const current = events.find((event) => event.id === eventId);
        if (!current) return;

        try {
            const shouldResetEvaluation = status === 'completed';
            const updated = await updateEventAPI(
                eventId,
                toEventPayload({
                    ...current,
                    status,
                    evaluationDate: shouldResetEvaluation ? '' : current.evaluationDate,
                    evaluation: shouldResetEvaluation ? '' : current.evaluation,
                }),
            );
            const nextEvent = applyRegistrationCounts(
                [normalizeEventFromApi(updated)],
                registrationsByEvent,
            )[0];
            const nextDisplayEvent = shouldResetEvaluation
                ? { ...nextEvent, status: 'completed', evaluationDate: '', evaluation: '' }
                : nextEvent;

            setEvents((prev) =>
                prev.map((event) => (event.id === eventId ? nextDisplayEvent : event)),
            );
            if (shouldResetEvaluation) {
                setEvaluations((prev) => prev.filter((item) => item.eventCode !== current.eventCode));
            }

            setApiError('');
        } catch (error) {
            setApiError(error?.message || 'Không cập nhật được trạng thái sự kiện.');
        }
    };

  const confirmDelete = async () => {
    if (!deleteConfirm) return;

    const canCancel = ['draft', 'upcoming'].includes(deleteConfirm.status);
    if (!canCancel) {
      showError('Chỉ được hủy khi và chỉ khi sự kiện chưa hoạt động.');
      return;
    }
    try {
      setDeleteLoading(true);
      showPending('Đang hủy sự kiện...');
      const updated = await updateEventAPI(
        deleteConfirm.id,
        toEventPayload({
          ...deleteConfirm,
          status: 'cancelled',
        }),
      );
      const nextEvent = applyRegistrationCounts(
        [normalizeEventFromApi(updated)],
        registrationsByEvent,
      )[0];
      setEvents((prev) => prev.map((event) => (event.id === deleteConfirm.id ? nextEvent : event)));
      setDeleteConfirm(null);
      setApiError('');
      setDeleteLoading(false);
      showSuccess('Đã hủy sự kiện.');
    } catch (error) {
      setDeleteLoading(false);
      showError(error?.message || 'Không hủy được sự kiện.');
    }
  };

  const openEvaluation = (event) => {
    if (!isFinishedEventStatus(event.status)) {
      setApiError('Chỉ có thể tạo hoặc xem phiếu đánh giá cho sự kiện đã kết thúc.');
      return;
    }

    const current = evaluations.find((item) => item.eventCode === event.eventCode);
    const evaluationDate = current?.evaluationDate || event.evaluationDate || getTodayDateInputValue();
    const evaluation = current?.evaluation || event.evaluation || '';
    setEvaluationTarget({ ...event, evaluationDate, evaluation });
    setEvaluationForm({
      evaluationDate,
      evaluation,
    });
    setEvaluationErrors({});
  };

  const openEvaluationFromList = (event) => {
    openEvaluation(event);
  };

  const getEvaluationCode = (event) =>
    evaluations.find((item) => item.eventCode === event.eventCode)?.id ||
    createEvaluationCode(event.eventCode, evaluations.length);

  const closeEvaluation = () => {
    setEvaluationTarget(null);
    setEvaluationForm({ evaluationDate: '', evaluation: '' });
    setEvaluationErrors({});
  };

  const updateEvaluationForm = (field, value) => {
    setEvaluationForm((prev) => ({ ...prev, [field]: value }));
    setEvaluationErrors((prev) => ({ ...prev, [field]: '' }));
  };

  const submitEvaluation = async () => {
    if (!isFinishedEventStatus(evaluationTarget?.status)) {
      setApiError('Chỉ có thể tạo phiếu đánh giá cho sự kiện đã kết thúc.');
      return;
    }

    const errs = {};
    const selectedDate = getTodayDateInputValue();
    const eventEndDate = evaluationTarget?.date ? new Date(evaluationTarget.date) : null;
    const evaluationDate = new Date(selectedDate);

    if (eventEndDate && evaluationDate && evaluationDate <= eventEndDate) {
      errs.evaluationDate = 'Ngày đánh giá phải lớn hơn ngày kết thúc sự kiện';
    }
    if (!evaluationForm.evaluation.trim()) errs.evaluation = 'Vui lòng nhập nội dung đánh giá';

    if (Object.keys(errs).length) {
      setEvaluationErrors(errs);
      return;
    }

    try {
      const saved = await createEventEvaluationAPI({
        eventId: evaluationTarget.id,
        evaluationDate: `${selectedDate}T23:59:59`,
        evaluationContent: evaluationForm.evaluation.trim(),
      });
      const nextItem = {
        id: createEvaluationCode(evaluationTarget.eventCode, evaluations.length),
        eventCode: saved.eventId,
        eventTitle: saved.eventName || evaluationTarget.title,
        evaluationDate: saved.evaluationDate ? String(saved.evaluationDate).slice(0, 10) : selectedDate,
        evaluation: saved.evaluationContent || evaluationForm.evaluation.trim(),
      };

      setEvaluations((prev) => {
        const existed = prev.find((item) => item.eventCode === evaluationTarget.eventCode);
        return existed
          ? prev.map((item) => (item.eventCode === evaluationTarget.eventCode ? { ...nextItem, id: item.id } : item))
          : [...prev, nextItem];
      });
      setEvents((prev) =>
        prev.map((event) =>
          event.id === evaluationTarget.id
            ? { ...event, evaluationDate: nextItem.evaluationDate, evaluation: nextItem.evaluation }
            : event,
        ),
      );
      closeEvaluation();
      setApiError('');
    } catch (error) {
      setApiError(error?.message || 'Không lưu được đánh giá sự kiện.');
    }
  };

  return (
    <div className={styles.page}>
      <ActionToast toast={toast} />
      {apiError && <div className={styles.apiError}>{apiError}</div>}

      <EventAdminHeader
        onExport={() => exportEventsExcel(filtered)}
        onAdd={openAdd}
        onOpenEvaluationList={() => setEvaluationHistoryOpen(true)}
      />

      <EventFiscalSummary totalEstimated={totalEstimated} totalActual={totalActual} />

      <EventRosterTable
        events={paginated}
        filteredCount={filtered.length}
        summary={rosterSummary}
        page={page}
        totalPages={totalPages}
        search={search}
        setSearch={setSearch}
        setPage={setPage}
        filterOpen={filterOpen}
        setFilterOpen={setFilterOpen}
        statusFilter={statusFilter}
        tagFilter={tagFilter}
        dateFromFilter={dateFromFilter}
        dateToFilter={dateToFilter}
        onApplyFilters={applyFilters}
        dateSort={dateSort}
        setDateSort={setDateSort}
        publishedCount={publishedCount}
        statusLabels={STATUS_LABEL}
        tagLabels={TAG_LABEL}
        tagFilters={TAG_FILTERS}
        evaluations={evaluations}
        memberCount={memberCount}
        onEdit={openEdit}
        onEvaluate={openEvaluation}
        onViewRegistrations={openRegistrations}
        onUpdateStatus={updateEventStatus}
        onDelete={setDeleteConfirm}
      />

      <button className={styles.fab} onClick={openAdd} title="Tạo sự kiện mới">
        <svg width="22" height="22" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
          <path d="M12 5v14M5 12h14"/>
        </svg>
      </button>

      <EventForm
        open={formOpen}
        onClose={() => setFormOpen(false)}
        onSubmit={handleSubmit}
        initial={editTarget}
        loading={formLoading}
        existingEvents={events}
        readOnly={editTarget?.status === 'cancelled'}
      />

      <EventEvaluationModal
        event={evaluationTarget}
        form={evaluationForm}
        errors={evaluationErrors}
        evaluationCode={evaluationTarget ? getEvaluationCode(evaluationTarget) : ''}
        onChange={updateEvaluationForm}
        onClose={closeEvaluation}
        onSubmit={submitEvaluation}
      />

      <EventEvaluationHistoryModal
        open={evaluationHistoryOpen}
        events={evaluatedEvents}
        onClose={() => setEvaluationHistoryOpen(false)}
        onSelectEvent={openEvaluationFromList}
      />

      <EventDeleteConfirmModal
        event={deleteConfirm}
        onCancel={() => { if (!deleteLoading) setDeleteConfirm(null); }}
        onConfirm={confirmDelete}
        loading={deleteLoading}
        label="Hủy"
      />

      <EventRegistrationModal
        event={registrationTarget}
        members={registeredMembers}
        onClose={() => setRegistrationTarget(null)}
        onExport={() => exportEventRegistrationsExcel(registrationTarget, registeredMembers)}
      />
    </div>
  );
}
