import api from './request';

// 用户登录
export const login = (data) => {
  return api.post('/user/login', data);
};

// 用户注册
export const register = (data) => {
  return api.post('/user/create', data);
};

// 获取用户信息
export const getUserInfo = () => {
  return api.get('/user/current');
};

// 获取用户列表
export const getUserList = (params) => {
  return api.get('/user/page', { params });
};

// 添加用户
export const addUser = (data) => {
  return api.post('/user', data);
};

// 删除用户
export const deleteUser = (id) => {
  return api.delete(`/user/${id}`);
};

// 更新用户
export const updateUser = (id, data) => {
  return api.put(`/user/${id}`, data);
};

// 根据ID获取用户
export const getUserById = (id) => {
  return api.get(`/user/${id}`);
};

// 用户登出
export const logout = () => {
  return api.post('/user/logout');
};