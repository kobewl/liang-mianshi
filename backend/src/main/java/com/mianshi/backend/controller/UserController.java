package com.mianshi.backend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mianshi.backend.dto.UserDTO;
import com.mianshi.backend.entity.User;
import com.mianshi.backend.service.UserService;
import com.mianshi.backend.vo.ApiResponse;
import com.mianshi.backend.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    public ApiResponse<Long> createUser(@Valid @RequestBody UserDTO userDTO) {
        User user = BeanUtil.copyProperties(userDTO, User.class);
        userService.save(user);
        return ApiResponse.success(user.getId());
    }

    @Operation(summary = "更新用户", description = "根据 ID 更新用户信息")
    @PutMapping("/{id}")
    public ApiResponse<Boolean> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        User user = BeanUtil.copyProperties(userDTO, User.class);
        user.setId(id);
        return ApiResponse.success(userService.updateById(user));
    }

    @Operation(summary = "删除用户", description = "根据 ID 删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        return ApiResponse.success(userService.removeById(id));
    }

    @Operation(summary = "获取用户详情", description = "根据 ID 获取用户详情")
    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getById(id);
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        return ApiResponse.success(userVO);
    }

    @Operation(summary = "分页查询用户", description = "分页查询用户列表")
    @GetMapping("/page")
    public ApiResponse<Page<UserVO>> pageUsers(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size) {
        Page<User> page = userService.page(new Page<>(current, size), new QueryWrapper<>());
        Page<UserVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(BeanUtil.copyToList(page.getRecords(), UserVO.class));
        return ApiResponse.success(voPage);
    }
}
