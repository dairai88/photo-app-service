package com.example.api.users.data;

import org.springframework.data.repository.CrudRepository;
import java.util.Optional;


public interface RoleRepository extends CrudRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(String name);
} 
