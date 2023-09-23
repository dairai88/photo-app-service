package com.example.api.users.service;

import com.example.api.users.data.AlbumsServiceClient;
import com.example.api.users.data.UserEntity;
import com.example.api.users.data.UsersRepository;
import com.example.api.users.shared.UserDto;
import com.example.api.users.ui.model.AlbumResponseModel;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AlbumsServiceClient albumsServiceClient;

    public UsersServiceImpl(UsersRepository usersRepository,
                            BCryptPasswordEncoder bCryptPasswordEncoder,
                            AlbumsServiceClient albumsServiceClient) {
        this.usersRepository = usersRepository;
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
		
		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
	}

    @Override
    public UserDto getUserDetailsByEmail(String email) {

        UserEntity userEntity = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        return getModelMapper().map(userEntity, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {

        UserEntity userEntity = usersRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("user with userId " + userId + " is not found"));

        UserDto userDto = getModelMapper().map(userEntity, UserDto.class);

        List<AlbumResponseModel> albumsList = albumsServiceClient.getAlbums(userId);

        userDto.setAlbums(albumsList);
        
        return userDto;
    }

    private ModelMapper getModelMapper() {

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        return modelMapper;
    }
}
