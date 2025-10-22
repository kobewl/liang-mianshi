import api from './request';

// 获取题目列表
export const getQuestionList = (params) => {
  return api.get('/question/page', { params });
};

// 从ES搜索题目列表
export const searchQuestionFromEs = (params) => {
  return api.get('/question/search/page/es', { params });
};

// 根据ID获取题目
export const getQuestionById = (id) => {
  return api.get(`/question/${id}`);
};

// 添加题目
export const addQuestion = (data) => {
  return api.post('/question', data);
};

// 删除题目
export const deleteQuestion = (id) => {
  return api.delete(`/question/${id}`);
};

// 批量删除题目
export const batchDeleteQuestions = (data) => {
  return api.post('/question/batchDelete', data);
};

// 批量将题目添加到题库
export const batchAddQuestionsToRepo = (data) => {
  return api.post('/question/batchAddToRepo', data);
};

// 批量从题库移除题目
export const batchRemoveQuestionsFromRepo = (data) => {
  return api.post('/question/batchRemoveFromRepo', data);
};

// 更新题目
export const updateQuestion = (id, data) => {
  return api.put(`/question/${id}`, data);
};

// 根据标题搜索题目
export const searchQuestionByTitle = (title) => {
  return api.get('/question/page', { params: { title } });
};

// 根据内容搜索题目
export const searchQuestionByContent = (content) => {
  return api.get('/question/page', { params: { content } });
};
