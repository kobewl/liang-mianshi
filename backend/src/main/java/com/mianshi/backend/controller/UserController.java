package com.mianshi.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mianshi.backend.model.dto.user.UserAddDTO;
import com.mianshi.backend.model.dto.user.UserUpdateDTO;
import com.mianshi.backend.model.dto.user.UserQueryDTO;
import com.mianshi.backend.model.dto.user.UserLoginDTO;
import com.mianshi.backend.model.vo.user.UserVO;
import com.mianshi.backend.model.vo.user.UserLoginVO;
import com.mianshi.backend.service.UserService;
import com.mianshi.backend.model.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    @PostMapping("/create")
    public ApiResponse<Long> createUser(@Valid @RequestBody UserAddDTO userAddDTO) {
        return ApiResponse.success(userService.addUser(userAddDTO));
    }

    @Operation(summary = "更新用户", description = "根据 ID 更新用户信息")
    @PutMapping("/{id}")
    public ApiResponse<Boolean> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        return ApiResponse.success(userService.updateUser(id, userUpdateDTO));
    }

    @Operation(summary = "删除用户", description = "根据 ID 删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        return ApiResponse.success(userService.removeById(id));
    }

    @Operation(summary = "获取用户详情", description = "根据 ID 获取用户详情")
    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @Operation(summary = "分页查询用户", description = "分页查询用户列表")
    @GetMapping("/page")
    public ApiResponse<Page<UserVO>> pageUsers(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "用户账号") @RequestParam(required = false) String userAccount,
            @Parameter(description = "用户昵称") @RequestParam(required = false) String userName,
            @Parameter(description = "用户角色") @RequestParam(required = false) String userRole) {
        UserQueryDTO queryDTO = new UserQueryDTO();
        queryDTO.setCurrent(current.intValue());
        queryDTO.setSize(size.intValue());
        queryDTO.setUserAccount(userAccount);
        queryDTO.setUserName(userName);
        queryDTO.setUserRole(userRole);
        return ApiResponse.success(userService.pageUsers(queryDTO));
    }

    @Operation(summary = "用户登录", description = "用户登录接口")
    @PostMapping("/login")
    public ApiResponse<UserLoginVO> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        return ApiResponse.success(userService.login(userLoginDTO));
    }

    @Operation(summary = "获取当前登录用户", description = "根据Token获取当前登录用户信息")
    @GetMapping("/current")
    public ApiResponse<UserVO> getCurrentUser(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(userService.getUserById(userId));
    }

    @Operation(summary = "用户登出", description = "用户登出接口（客户端删除Token即可）")
    @PostMapping("/logout")
    public ApiResponse<Boolean> logout() {
        // Token是无状态的，登出只需要客户端删除Token即可
        // 如果需要实现Token黑名单，可以在这里添加相关逻辑
        return ApiResponse.success(true);
    }

    /**
     * 新增用户签到
     *
     * @param request
     * @return boolean 是否签到
     */
    @Operation(summary = "新增用户签到", description = "新增用户签到")
    @PostMapping("/add/sign_in")
    public ApiResponse<Boolean> addSignIn(HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        if(userId == null) {
            return ApiResponse.error("必须登录才能签到");
        }
        return ApiResponse.success(userService.addUserSignIn(userId));
    }

}
