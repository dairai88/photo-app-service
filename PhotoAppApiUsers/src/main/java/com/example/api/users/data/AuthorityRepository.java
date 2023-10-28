package com.example.api.users.data;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface AuthorityRepository extends CrudRepository<AuthorityEntity, Long> {
    
    Optional<AuthorityEntity> findByName(String name);
}
