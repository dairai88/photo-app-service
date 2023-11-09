package com.example.api.users.service;

import com.example.api.users.data.AlbumsServiceClient;
import com.example.api.users.data.AuthorityEntity;
import com.example.api.users.data.RoleEntity;
import com.example.api.users.data.RoleRepository;
import com.example.api.users.data.UserEntity;
import com.example.api.users.data.UsersRepository;
import com.example.api.users.shared.UserDto;
import com.example.api.users.ui.model.AlbumResponseModel;
import com.example.api.users.ui.model.RoleInfoModel;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsersServiceImpl implements UsersService {

    private static final Logger LOG = LoggerFactory.getLogger(UsersServiceImpl.class);

    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AlbumsServiceClient albumsServiceClient;

    public UsersServiceImpl(UsersRepository usersRepository,
            RoleRepository roleRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            AlbumsServiceClient albumsServiceClient) {
        this.usersRepository = usersRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.albumsServiceClient = albumsServiceClient;
    }

    @Override
    public UserDto createUser(UserDto userDetails) {

        userDetails.setUserId(UUID.randomUUID().toString());
        userDetails.setEncryptedPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));

        ModelMapper modelMapper = getModelMapper();

        UserEntity userEntity = modelMapper.map(userDetails, UserEntity.class);

        usersRepository.save(userEntity);

        return modelMapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserEntity userEntity = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Collection<RoleEntity> roles = userEntity.getRoles();

        roles.forEach(role -> {

            authorities.add(new SimpleGrantedAuthority(role.getName()));

            Collection<AuthorityEntity> authorityEntities = role.getAuthorities();
            authorityEntities
                    .forEach(authorityEntity -> authorities.add(new SimpleGrantedAuthority(authorityEntity.getName())));
        });

        return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), authorities);
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {

        UserEntity userEntity = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        return getModelMapper().map(userEntity, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId, String authorization) {

        UserEntity userEntity = usersRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("user with userId " + userId + " is not found"));

        UserDto userDto = getModelMapper().map(userEntity, UserDto.class);

        LOG.debug("Before calling albums microservice");

        List<AlbumResponseModel> albumsList = albumsServiceClient.getAlbums(userId, authorization);

        LOG.debug("After calling albums microservice");

        userDto.setAlbums(albumsList);

        return userDto;
    }

    @Override
    @Transactional
    public void setUserRole(String userId, RoleInfoModel roleInfoModel) {

        UserEntity userEntity = usersRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("user with userId " + userId + " is not found"));

        setUserRole(userEntity, roleInfoModel);

        usersRepository.save(userEntity);
    }

    private ModelMapper getModelMapper() {

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        return modelMapper;
    }

    private void setUserRole(UserEntity userEntity, RoleInfoModel roleInfoModel) {

        if (roleInfoModel == null || roleInfoModel.getRoles() == null || roleInfoModel.getRoles().isEmpty()) {
            return;
        }

        Collection<RoleEntity> roles = new ArrayList<>();
        roleInfoModel.getRoles().forEach(role -> {

            Optional<RoleEntity> roleOptional = roleRepository.findByName("ROLE_" + role.toUpperCase());

            if (roleOptional.isPresent()) {
                roles.add(roleOptional.get());
            } else {
                RoleEntity role1 = new RoleEntity("ROLE_" + role.toUpperCase(), List.of());
                roleRepository.save(role1);
                roles.add(role1);
            }
        });

        userEntity.getRoles().addAll(roles);
    }
}
