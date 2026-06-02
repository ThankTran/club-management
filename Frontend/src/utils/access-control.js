const MANAGER_PRIORITY_MAX = 1;
const SHARED_ROUTES = ["/home", "/help", "/profile"];

export const MEMBER_ROUTES = [
  "/profile",
  "/help",
  "/resourcesuser",
  "/eventuser",
  "/memberuser",
  "/memberdues",
];

export const MANAGER_ROUTES = [
  "/dashboard",
  "/memberadmin",
  "/resourcesadmin",
  "/eventadmin",
  "/finance",
  "/memberdues",
  "/account",
  "/settings",
];

export const isAuthenticated = (user, token) => Boolean(user && token);

export const isManager = (user) => {
  const priority = Number(user?.rolePriority);
  return Number.isFinite(priority) && priority <= MANAGER_PRIORITY_MAX;
};

export const getDefaultPath = (user) => (isManager(user) ? "/dashboard" : "/home");

export const canAccessPath = (path, user, token) => {
  if (!path || path === "/" || path === "/signin") return true;
  if (!isAuthenticated(user, token)) return false;

  const isSharedRoute = SHARED_ROUTES.some((route) => path === route || path.startsWith(`${route}/`));
  const isManagerRoute = MANAGER_ROUTES.some((route) => path === route || path.startsWith(`${route}/`));
  const isMemberRoute = MEMBER_ROUTES.some((route) => path === route || path.startsWith(`${route}/`));

  if (isManager(user)) {
    return isSharedRoute || isManagerRoute;
  }

  return isSharedRoute || isMemberRoute;
};
