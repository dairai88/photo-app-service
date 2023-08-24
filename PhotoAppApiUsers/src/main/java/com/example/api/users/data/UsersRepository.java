package com.example.api.users.data;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface UsersRepository extends CrudRepository<UserEntity, Long> {
	
	Optional<UserEntity> findByEmail(String email);
}
