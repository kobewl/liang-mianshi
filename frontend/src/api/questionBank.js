import api from './request';

// 获取题库列表
export const getQuestionBankList = (params) => {
  return api.get('/question-bank/page', { params });
};

// 根据ID获取题库
export const getQuestionBankById = (id) => {
  return api.get(`/question-bank/${id}`);
};

// 添加题库
export const addQuestionBank = (data) => {
  return api.post('/question-bank', data);
};

// 删除题库
export const deleteQuestionBank = (id) => {
  return api.delete(`/question-bank/${id}`);
};

// 更新题库
export const updateQuestionBank = (id, data) => {
  return api.put(`/question-bank/${id}`, data);
};

// 根据名称搜索题库
export const searchQuestionBankByName = (bankName) => {
  return api.get('/question-bank/page', { params: { title: bankName } });
};