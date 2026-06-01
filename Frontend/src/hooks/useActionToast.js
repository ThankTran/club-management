import { useCallback, useRef, useState } from 'react';

const DEFAULT_SUCCESS_DURATION = 2500;
const DEFAULT_ERROR_DURATION = 3500;

export default function useActionToast() {
  const [toast, setToast] = useState(null);
  const timerRef = useRef(null);

  const clearTimer = () => {
    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
  };

  const show = useCallback((message, type = 'info', duration = 0) => {
    clearTimer();
    const id = Date.now();
    setToast({ id, message, type });

    if (duration > 0) {
      timerRef.current = setTimeout(() => {
        setToast((prev) => (prev?.id === id ? null : prev));
      }, duration);
    }
  }, []);

  const hide = useCallback(() => {
    clearTimer();
    setToast(null);
  }, []);

  const showPending = useCallback((message) => show(message, 'info', 0), [show]);
  const showSuccess = useCallback(
    (message, duration = DEFAULT_SUCCESS_DURATION) => show(message, 'success', duration),
    [show],
  );
  const showError = useCallback(
    (message, duration = DEFAULT_ERROR_DURATION) => show(message, 'error', duration),
    [show],
  );

  return {
    toast,
    showPending,
    showSuccess,
    showError,
    hide,
  };
}

