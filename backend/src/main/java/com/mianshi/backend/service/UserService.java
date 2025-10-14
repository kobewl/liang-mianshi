package com.mianshi.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mianshi.backend.model.entity.User;
import com.mianshi.backend.model.dto.user.UserAddDTO;
import com.mianshi.backend.model.dto.user.UserUpdateDTO;
import com.mianshi.backend.model.dto.user.UserQueryDTO;
import com.mianshi.backend.model.dto.user.UserLoginDTO;
import com.mianshi.backend.model.vo.user.UserVO;
import com.mianshi.backend.model.vo.user.UserLoginVO;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 添加用户
     *
     * @param userAddDTO 用户添加DTO
     * @return 用户ID
     */
    Long addUser(UserAddDTO userAddDTO);

    /**
     * 更新用户
     *
     * @param id 用户ID
     * @param userUpdateDTO 用户更新DTO
     * @return 是否成功
     */
    Boolean updateUser(Long id, UserUpdateDTO userUpdateDTO);

    /**
     * 根据ID获取用户VO
     *
     * @param id 用户ID
     * @return 用户VO
     */
    UserVO getUserById(Long id);

    /**
     * 分页查询用户
     *
     * @param queryDTO 查询条件
     * @return 用户VO分页
     */
    Page<UserVO> pageUsers(UserQueryDTO queryDTO);
    
    /**
     * 用户登录
     *
     * @param userLoginDTO 用户登录DTO
     * @return 用户登录VO
     */
    UserLoginVO login(UserLoginDTO userLoginDTO);
}
