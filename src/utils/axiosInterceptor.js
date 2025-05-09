import axios from 'axios';
import { authHeader, isTokenExpired, removeToken } from './authHeader';
import { getApiUrl } from '../config';

const axiosInstance = axios.create({
  baseURL: getApiUrl()
});


axiosInstance.interceptors.request.use(
  config => {
    if (isTokenExpired()) {
      removeToken();
    } else {
      const headers = authHeader();
      if (headers.Authorization) {
        config.headers = {
          ...config.headers,
          ...headers
        };
      }
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

axiosInstance.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    if (error.response && error.response.status === 401) {
      removeToken();
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
