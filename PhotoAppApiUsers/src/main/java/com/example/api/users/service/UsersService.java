package com.example.api.users.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.api.users.shared.UserDto;
import com.example.api.users.ui.model.RoleInfoModel;

public interface UsersService extends UserDetailsService {

    UserDto createUser(UserDto userDetails);

    UserDto getUserDetailsByEmail(String email);

    UserDto getUserByUserId(String userId, String authorization);

    void setUserRole(String userId, RoleInfoModel roleInfoModel);

}
