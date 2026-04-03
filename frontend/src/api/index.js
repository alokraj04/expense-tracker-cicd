import axios from 'axios';
import toast from 'react-hot-toast';

const api = axios.create({
  baseURL: '/api' // Uses proxy in vite config
});

const authApi = axios.create({
  baseURL: '/auth' // For signup/login
});

// Request interceptor to attach token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');

    console.log("🔐 TOKEN SENT:", token); // debug

    if (token) {
      config.headers = {
        ...config.headers,
        Authorization: `Bearer ${token}`,
      };
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // DO NOT redirect here to prevent race conditions during OAuth load
    return Promise.reject(error);
  }
);

export const authService = {
  login: (data) => authApi.post('/login', data),
  signup: (data) => authApi.post('/signup', data),
};

export const userService = {
  getProfile: () => api.get('/users/profile'),
  updateProfile: (data) => api.put('/users/profile', data),
  requestEmailChange: (newEmail) => api.put(`/users/request-email-change?newEmail=${newEmail}`),
  changePassword: (data) => api.put('/users/change-password', data),
  forgotPassword: (email) => api.post(`/users/forgot-password?email=${email}`),
  resetPassword: (token, newPassword) => api.post(`/users/reset-password?token=${token}&newPassword=${newPassword}`),
  deleteAccount: (password) => api.delete(password ? `/users/delete?password=${password}` : '/users/delete'),
};

export const expenseService = {
  getAll: () => api.get('/expenses'),
  getById: (id) => api.get(`/expenses/${id}`),
  create: (data) => api.post('/expenses', data),
  update: (id, data) => api.put(`/expenses/${id}`, data),
  delete: (id) => api.delete(`/expenses/${id}`),
  getSummary: () => api.get('/expenses/summary'),
  getDateRange: (startDate, endDate) => {
    // Requirements state NO Z suffix and NO milliseconds
    const formatStr = (d) => new Date(d).toISOString().slice(0, 19);
    return api.get(`/expenses/date-range?startDate=${formatStr(startDate)}&endDate=${formatStr(endDate)}`);
  },
  getByCategory: (categoryId) => api.get(`/expenses/category/${categoryId}`),
  uploadReceipt: (id, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post(`/expenses/${id}/receipt`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },
  createRecurring: (data) => api.post('/expenses/recurring', data),
};

export const incomeService = {
  getAll: () => api.get('/incomes'),
  create: (data) => api.post('/incomes', data),
  update: (id, data) => api.put(`/incomes/${id}`, data),
  delete: (id) => api.delete(`/incomes/${id}`),
  getSummary: () => api.get('/incomes/summary'),
};

export const categoryService = {
  getAll: () => api.get('/categories'),
  getById: (id) => api.get(`/categories/${id}`),
  create: (data) => api.post('/categories', data),
  update: (id, data) => api.put(`/categories/${id}`, data),
  delete: (id) => api.delete(`/categories/${id}`),
  getByType: (type) => api.get(`/categories/type/${type}`), // type: INCOME | EXPENSE
};

export const budgetService = {
  getAll: () => api.get('/budgets'),
  create: (data) => api.post('/budgets', data),
  update: (id, data) => api.put(`/budgets/${id}`, data),
  delete: (id) => api.delete(`/budgets/${id}`),
  getDashboard: () => api.get('/budgets/dashboard'),
};

export const analyticsService = {
  getMonthly: () => api.get('/analytics/monthly'),
  getWeekly: () => api.get('/analytics/weekly'),
  getIncomeExpense: () => api.get('/analytics/income-expense'),
  getCategoryBreakdown: () => api.get('/analytics/category-breakdown'),
  downloadReport: () => api.get('/analytics/report/excel', { responseType: 'blob' }),
};

export default api;
