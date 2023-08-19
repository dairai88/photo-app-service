package com.example.api.users.service;

import com.example.api.users.shared.UserDto;

public interface UsersService {
    UserDto createUser(UserDto userDetails);
}
