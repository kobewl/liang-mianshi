package com.mianshi.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mianshi.backend.constants.RedissonConstants;
import com.mianshi.backend.model.entity.User;
import com.mianshi.backend.model.dto.user.UserAddDTO;
import com.mianshi.backend.model.dto.user.UserUpdateDTO;
import com.mianshi.backend.model.dto.user.UserQueryDTO;
import com.mianshi.backend.model.dto.user.UserLoginDTO;
import com.mianshi.backend.model.vo.user.UserVO;
import com.mianshi.backend.model.vo.user.UserLoginVO;
import com.mianshi.backend.mapper.UserMapper;
import com.mianshi.backend.service.UserService;
import com.mianshi.backend.utils.JwtUtils;
import com.mianshi.backend.utils.PasswordUtils;
import jakarta.annotation.Resource;
import org.redisson.api.RBitSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.*;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private RedissonClient redissonClient;


    @Override
    public Long addUser(UserAddDTO userAddDTO) {
        // 1. 参数校验
        if (userAddDTO == null) {
            throw new IllegalArgumentException("用户信息不能为空");
        }

        // 2. 检查必填字段
        if (!StringUtils.hasText(userAddDTO.getUserAccount())) {
            throw new IllegalArgumentException("用户账号不能为空");
        }
        if (!StringUtils.hasText(userAddDTO.getUserPassword())) {
            throw new IllegalArgumentException("用户密码不能为空");
        }

        // 3. 检查账号长度
        if (userAddDTO.getUserAccount().length() < 4 || userAddDTO.getUserAccount().length() > 20) {
            throw new IllegalArgumentException("用户账号长度必须在4-20个字符之间");
        }

        // 4. 检查密码长度
        if (userAddDTO.getUserPassword().length() < 6 || userAddDTO.getUserPassword().length() > 20) {
            throw new IllegalArgumentException("用户密码长度必须在6-20个字符之间");
        }

        // 5. 检查账号是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAddDTO.getUserAccount());
        User existUser = this.getOne(queryWrapper);
        if (existUser != null) {
            throw new IllegalArgumentException("用户账号已存在");
        }

        // 6. 设置默认角色
        if (!StringUtils.hasText(userAddDTO.getUserRole())) {
            userAddDTO.setUserRole("user");
        }

        // 7. 转换并保存用户
        User user = BeanUtil.copyProperties(userAddDTO, User.class);
        // 加密密码
        user.setUserPassword(PasswordUtils.encryptPassword(user.getUserPassword()));
        this.save(user);
        return user.getId();
    }

    @Override
    public Boolean updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID不能为空或小于等于0");
        }
        if (userUpdateDTO == null) {
            throw new IllegalArgumentException("用户更新信息不能为空");
        }

        // 2. 检查用户是否存在
        User existUser = this.getById(id);
        if (existUser == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 3. 如果更新账号，检查账号是否已被其他用户使用
        if (StringUtils.hasText(userUpdateDTO.getUserAccount()) &&
                !userUpdateDTO.getUserAccount().equals(existUser.getUserAccount())) {
            // 检查账号长度
            if (userUpdateDTO.getUserAccount().length() < 4 || userUpdateDTO.getUserAccount().length() > 20) {
                throw new IllegalArgumentException("用户账号长度必须在4-20个字符之间");
            }

            // 检查账号是否已存在
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUserAccount, userUpdateDTO.getUserAccount())
                    .ne(User::getId, id);
            User userWithSameAccount = this.getOne(queryWrapper);
            if (userWithSameAccount != null) {
                throw new IllegalArgumentException("用户账号已存在");
            }
        }

        // 4. 如果更新密码，检查密码长度
        if (StringUtils.hasText(userUpdateDTO.getUserPassword()) &&
                (userUpdateDTO.getUserPassword().length() < 6 || userUpdateDTO.getUserPassword().length() > 20)) {
            throw new IllegalArgumentException("用户密码长度必须在6-20个字符之间");
        }

        // 5. 转换并更新用户
        User user = BeanUtil.copyProperties(userUpdateDTO, User.class);
        user.setId(id);
        // 如果更新密码，则加密新密码
        if (StringUtils.hasText(userUpdateDTO.getUserPassword())) {
            user.setUserPassword(PasswordUtils.encryptPassword(user.getUserPassword()));
        }
        return this.updateById(user);
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = this.getById(id);
        if (user == null) {
            return null;
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    public Page<UserVO> pageUsers(UserQueryDTO queryDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(StringUtils.hasText(queryDTO.getUserAccount()), User::getUserAccount, queryDTO.getUserAccount())
                .like(StringUtils.hasText(queryDTO.getUserName()), User::getUserName, queryDTO.getUserName())
                .eq(StringUtils.hasText(queryDTO.getUserRole()), User::getUserRole, queryDTO.getUserRole());

        Page<User> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), queryWrapper);

        Page<UserVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(BeanUtil.copyToList(page.getRecords(), UserVO.class));
        return voPage;
    }

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        // 1. 参数校验
        if (userLoginDTO == null) {
            throw new IllegalArgumentException("登录信息不能为空");
        }

        if (!StringUtils.hasText(userLoginDTO.getUserAccount())) {
            throw new IllegalArgumentException("用户账号不能为空");
        }

        if (!StringUtils.hasText(userLoginDTO.getUserPassword())) {
            throw new IllegalArgumentException("用户密码不能为空");
        }

        // 2. 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userLoginDTO.getUserAccount());
        User user = this.getOne(queryWrapper);

        // 3. 校验用户是否存在
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 4. 校验密码
        if (!PasswordUtils.checkPassword(userLoginDTO.getUserPassword(), user.getUserPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        // 5. 生成Token
        String token = JwtUtils.generateToken(user.getId(), user.getUserAccount(), user.getUserRole());

        // 6. 转换为VO并返回
        UserLoginVO userLoginVO = BeanUtil.copyProperties(user, UserLoginVO.class);
        userLoginVO.setToken(token);
        return userLoginVO;
    }

    @Override
    public Boolean logout(String token) {
        return null;
    }

    /**
     * 添加用户的
     *
     * @param userId 用户ID
     * @return
     */
    @Override
    public Boolean addUserSignIn(Long userId) {
        LocalDateTime nowData = LocalDateTime.now();
        String key = RedissonConstants.getUserSignInRedisKey(nowData.getYear(), userId);
        // 获取 Reids 的 BitMap
        RBitSet signInBitSet = redissonClient.getBitSet(key);
        // 获取当前日期是今年的第几天，作为偏移量（从 0 开始计数）
        int offset = nowData.getDayOfYear() - 1;
        // 查询当天有没有签到
        if (!signInBitSet.get(offset)) {
            // 没有签到，则设置当天的位为 1
            signInBitSet.set(offset, true);
            return true;
        }
        return true;
    }

    @Override
    public List<Integer> getUserSignIn(Long userId, Integer year) {
        // 1. 判断 year 是否为空
        if (year == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
        }
        String key = RedissonConstants.getUserSignInRedisKey(year, userId);

        // 2. 获取 Reids 的 BitMap
        RBitSet signInBitSet = redissonClient.getBitSet(key);
        // 加载 Bitset 到内存中，避免后续读取时发送多次请求
        BitSet bitSet = signInBitSet.asBitSet();

        // 3. 构造返回结果
        // 使用 LinkedHashMap 的原因：
        // 1. 保留插入顺序
        // 2. 避免日期排序
        // Map<LocalDate, Boolean> result = new LinkedHashMap<>();

        // 统计签到的日期
        List<Integer> dayList = new ArrayList<>();
        // 从索引 0 开始遍历，因为 Bitset 的索引是从 0 开始的，查找下一个被设置为 1的位
        int index = bitSet.nextSetBit(0);
        while (index > 0) {
            dayList.add(index);
            // 继续查找下一个被设置为 1 的位
            index = bitSet.nextSetBit(index + 1);
        }
        return dayList;
    }
}
