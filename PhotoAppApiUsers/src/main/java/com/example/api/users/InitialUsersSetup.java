package com.example.api.users;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.api.users.data.AuthorityEntity;
import com.example.api.users.data.AuthorityRepository;
import com.example.api.users.data.RoleEntity;
import com.example.api.users.data.RoleRepository;
import com.example.api.users.data.UserEntity;
import com.example.api.users.data.UsersRepository;
import com.example.api.users.shared.Roles;

import jakarta.transaction.Transactional;

@Component
public class InitialUsersSetup {

    private final Logger LOG = LoggerFactory.getLogger(InitialUsersSetup.class);

    private final AuthorityRepository authorityRepository;
    private final RoleRepository roleRepository;
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public InitialUsersSetup(AuthorityRepository authorityRepository,
            RoleRepository roleRepository,
            UsersRepository usersRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.authorityRepository = authorityRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.usersRepository = usersRepository;  
    }

    /**
     * Create built-in authorities and roles
     * Create one admin user
     * Transactional annotation here ensures that all operations related
     * to db are all involved in one session.
     * @param event
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {

        LOG.info("From Application Ready Event");

        AuthorityEntity readAuthorityEntity = createAuthority("READ");
        AuthorityEntity writeAuthorityEntity = createAuthority("WRITE");
        AuthorityEntity deleteAuthorityEntity = createAuthority("DELETE");

        createRole(Roles.ROLE_USER.name(), List.of(readAuthorityEntity, writeAuthorityEntity));
        RoleEntity adminRoleEntity = createRole(Roles.ROLE_ADMIN.name(), List.of(readAuthorityEntity, writeAuthorityEntity, deleteAuthorityEntity));

        if (adminRoleEntity == null) {
            return;
        }

        createUser("dalei@example.com", List.of(adminRoleEntity));
    }

    private AuthorityEntity createAuthority(String name) {

        Optional<AuthorityEntity> authorityOptional = authorityRepository.findByName(name);

        if (authorityOptional.isPresent()) {
            return authorityOptional.get();
        }

        AuthorityEntity authorityEntity = new AuthorityEntity(name);
        authorityEntity = authorityRepository.save(authorityEntity);

        LOG.info("Created authority, id {}, name {}.", authorityEntity.getId(), authorityEntity.getName());

        return authorityEntity;
    }

    private RoleEntity createRole(String name, Collection<AuthorityEntity> authorities) {

        Optional<RoleEntity> roleOptional = roleRepository.findByName(name);

        if (roleOptional.isPresent()) {
            return roleOptional.get();
        }

        RoleEntity roleEntity = new RoleEntity(name, authorities);
        roleEntity = roleRepository.save(roleEntity);

        LOG.info("Created role, id {}, name {}.", roleEntity.getId(), roleEntity.getName());

        return roleEntity;
    }

    private UserEntity createUser(String email, Collection<RoleEntity> roles) {

        Optional<UserEntity> userOptional = usersRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            return userOptional.get();
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName("dalei");
        userEntity.setLastName("sun");
        userEntity.setEmail(email);
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode("123456789"));
        userEntity.setRoles(roles);

        usersRepository.save(userEntity);

        return userEntity;
    }
}
