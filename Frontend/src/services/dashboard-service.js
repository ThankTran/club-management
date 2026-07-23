import api from '../utils/api'

export const getDashboardAPI = () =>
  api.get('dashboard')

export const getDashboardStatsAPI = () =>
  api.get('dashboard/stats')

export const getDashboardOverviewAPI = ({ fromDate, toDate } = {}) =>
  api.get('dashboard/overview', {
    params: {
      ...(fromDate ? { fromDate } : {}),
      ...(toDate ? { toDate } : {}),
    },
  })

export const getDashboardNotificationsAPI = () =>
  api.get('dashboard/notifications')
