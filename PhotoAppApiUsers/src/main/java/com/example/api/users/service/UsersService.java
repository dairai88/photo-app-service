package com.example.api.users.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.api.users.shared.UserDto;

public interface UsersService extends UserDetailsService {
    UserDto createUser(UserDto userDetails);
}
