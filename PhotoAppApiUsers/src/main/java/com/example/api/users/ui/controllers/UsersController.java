package com.example.api.users.ui.controllers;

import com.example.api.users.service.UsersService;
import com.example.api.users.shared.UserDto;
import com.example.api.users.ui.model.RoleInfoModel;
import com.example.api.users.ui.model.CreateUserRequestModel;
import com.example.api.users.ui.model.CreateUserResponseModel;
import com.example.api.users.ui.model.UserResponseModel;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final Environment env;
    private final UsersService usersService;

    public UsersController(Environment env, UsersService usersService) {
        this.env = env;
        this.usersService = usersService;
    }

    @GetMapping("/status/check")
    public String status() {
        return "Working on port " + env.getProperty("local.server.port") + ", Welcome " + env.getProperty("greeting.user");
    }

    @PostMapping
    public ResponseEntity<CreateUserResponseModel> createUser(@Valid @RequestBody CreateUserRequestModel userDetails) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = modelMapper.map(userDetails, UserDto.class);
        UserDto createdUser = usersService.createUser(userDto);

        CreateUserResponseModel returnValue = modelMapper.map(createdUser, CreateUserResponseModel.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
    }

    /**
     * Set role for user with userId
     * @param roleInfoModel
     * @return
     */
    @PostMapping("/{userId}/role")
    public String setUserAuthority(@PathVariable String userId, @RequestBody RoleInfoModel roleInfoModel) {

        usersService.setUserRole(userId, roleInfoModel);
        return "set user role " + userId;
    }

    @GetMapping(value = "/{userId}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    @PreAuthorize("hasRole('ADMIN') or principal == #userId")
    public ResponseEntity<UserResponseModel> getUser(@PathVariable String userId, 
    		@RequestHeader("Authorization") String authorization) {

        UserDto userDto = usersService.getUserByUserId(userId, authorization);
        UserResponseModel returnValue = new ModelMapper().map(userDto, UserResponseModel.class);
        return ResponseEntity.status(HttpStatus.OK).body(returnValue);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or principal == #userId")
    public String deleteUser(@PathVariable String userId) {

        // Delete user logic here
        return "Deleting user with id " + userId;
    }
}
