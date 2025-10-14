package com.mianshi.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mianshi.backend.entity.User;
import com.mianshi.backend.mapper.UserMapper;
import com.mianshi.backend.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
