const normalizeText = (value) =>
  String(value ?? '')
    .toLowerCase()
    .trim();

const removeDiacritics = (value) =>
  normalizeText(value)
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '');

const hasDiacritics = (value) => removeDiacritics(value) !== normalizeText(value);

export const matchesVietnameseSearch = (value, query) => {
  const normalizedQuery = normalizeText(query);
  if (!normalizedQuery) {
    return true;
  }

  if (hasDiacritics(normalizedQuery)) {
    return normalizeText(value).includes(normalizedQuery);
  }

  return removeDiacritics(value).includes(removeDiacritics(normalizedQuery));
};

export const normalizeVietnameseText = normalizeText;
