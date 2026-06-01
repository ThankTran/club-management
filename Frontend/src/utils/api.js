import axios from 'axios'
import { getToken, removeToken } from './token'

export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

//gắn token vào mỗi request
api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  if (config.data instanceof FormData) {
    if (typeof config.headers.delete === 'function') {
      config.headers.delete('Content-Type')
    } else {
      delete config.headers['Content-Type']
    }
  }
  return config
})

//xử lý lỗi 401
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      if (getToken()) {
        removeToken()
        window.location.href = '/'
      }
    }
    return Promise.reject(error.response?.data || error)
  }
)

export default api
