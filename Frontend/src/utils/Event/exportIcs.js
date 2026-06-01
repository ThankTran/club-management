const pad2 = (value) => String(value).padStart(2, '0');

const parseLocalDateTime = (date, time = '00:00') => {
  if (!date) return null;
  const [year, month, day] = String(date).split('-').map(Number);
  const [hour = 0, minute = 0] = String(time || '00:00').split(':').map(Number);
  if (![year, month, day].every(Number.isFinite)) return null;
  return new Date(year, month - 1, day, hour, minute, 0);
};

const formatUtcDateTime = (date) =>
  `${date.getUTCFullYear()}${pad2(date.getUTCMonth() + 1)}${pad2(date.getUTCDate())}T${pad2(date.getUTCHours())}${pad2(date.getUTCMinutes())}${pad2(date.getUTCSeconds())}Z`;

const formatIcsDate = (date) =>
  `${date.getFullYear()}${pad2(date.getMonth() + 1)}${pad2(date.getDate())}`;

const escapeIcsText = (value = '') =>
  String(value)
    .replace(/\\/g, '\\\\')
    .replace(/\r?\n/g, '\\n')
    .replace(/;/g, '\\;')
    .replace(/,/g, '\\,');

const foldLine = (line) => {
  const chars = [...line];
  const lines = [];
  let current = '';
  let currentLength = 0;

  chars.forEach((char) => {
    const size = new TextEncoder().encode(char).length;
    if (currentLength + size > 75) {
      lines.push(current);
      current = ` ${char}`;
      currentLength = 1 + size;
      return;
    }
    current += char;
    currentLength += size;
  });

  lines.push(current);
  return lines.join('\r\n');
};

const serializeLines = (lines) => `${lines.map(foldLine).join('\r\n')}\r\n`;

export const buildRegisteredEventsIcs = (registeredEvents = []) => {
  const dtstamp = formatUtcDateTime(new Date());
  const eventLines = registeredEvents.flatMap((event) => {
    const start = parseLocalDateTime(event.date, event.time);
    if (!start) return [];

    const lines = [
      'BEGIN:VEVENT',
      `UID:${escapeIcsText(event.id || event.title)}@club-management.local`,
      `DTSTAMP:${dtstamp}`,
      `SUMMARY:${escapeIcsText(event.title || 'Sự kiện câu lạc bộ')}`,
    ];

    if (event.time) {
      const end = event.endTime
        ? parseLocalDateTime(event.date, event.endTime)
        : new Date(start.getTime() + 60 * 60 * 1000);
      const safeEnd = end && end > start ? end : new Date(start.getTime() + 60 * 60 * 1000);
      lines.push(`DTSTART:${formatUtcDateTime(start)}`);
      lines.push(`DTEND:${formatUtcDateTime(safeEnd)}`);
    } else {
      const end = new Date(start);
      end.setDate(end.getDate() + 1);
      lines.push(`DTSTART;VALUE=DATE:${formatIcsDate(start)}`);
      lines.push(`DTEND;VALUE=DATE:${formatIcsDate(end)}`);
    }

    if (event.location) lines.push(`LOCATION:${escapeIcsText(event.location)}`);
    if (event.description) lines.push(`DESCRIPTION:${escapeIcsText(event.description)}`);
    lines.push('END:VEVENT');
    return lines;
  });

  if (!eventLines.length) return '';

  return serializeLines([
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//Club Management//Registered Events//VI',
    'CALSCALE:GREGORIAN',
    'METHOD:PUBLISH',
    ...eventLines,
    'END:VCALENDAR',
  ]);
};
