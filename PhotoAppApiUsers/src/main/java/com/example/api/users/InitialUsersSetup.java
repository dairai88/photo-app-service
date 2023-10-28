package com.example.api.users;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.api.users.data.AuthorityEntity;
import com.example.api.users.data.AuthorityRepository;
import com.example.api.users.data.RoleEntity;
import com.example.api.users.data.RoleRepository;
import com.example.api.users.shared.Roles;

import jakarta.transaction.Transactional;

@Component
public class InitialUsersSetup {

    private final Logger LOG = LoggerFactory.getLogger(InitialUsersSetup.class);

    private final AuthorityRepository authorityRepository;
    private final RoleRepository roleRepository;

    public InitialUsersSetup(AuthorityRepository authorityRepository,
            RoleRepository roleRepository) {
        this.authorityRepository = authorityRepository;
        this.roleRepository = roleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationEvent(ApplicationReadyEvent event) {

        LOG.info("From Application Ready Event");

        AuthorityEntity readAuthorityEntity = createAuthority("READ");
        AuthorityEntity writeAuthorityEntity = createAuthority("WRITE");
        AuthorityEntity deleteAuthorityEntity = createAuthority("DELETE");

        createRole(Roles.ROLE_USER.name(), List.of(readAuthorityEntity, writeAuthorityEntity));
        createRole(Roles.ROLE_ADMIN.name(), List.of(readAuthorityEntity, writeAuthorityEntity, deleteAuthorityEntity));
    }

    @Transactional
    private AuthorityEntity createAuthority(String name) {

        Optional<AuthorityEntity> authorityOptional = authorityRepository.findByName(name);

        if (authorityOptional.isPresent()) {
            return authorityOptional.get();
        }

        AuthorityEntity authorityEntity = new AuthorityEntity(name);
        authorityRepository.save(authorityEntity);

        LOG.info("Created authority, id {}, name {}.", authorityEntity.getId(), authorityEntity.getName());

        return authorityEntity;
    }

    @Transactional
    private RoleEntity createRole(String name, Collection<AuthorityEntity> authorities) {

        Optional<RoleEntity> roleOptional = roleRepository.findByName(name);

        if (roleOptional.isPresent()) {
            return roleOptional.get();
        }

        RoleEntity roleEntity = new RoleEntity(name, authorities);
        roleRepository.save(roleEntity);

        LOG.info("Created role, id {}, name {}.", roleEntity.getId(), roleEntity.getName());

        return roleEntity;
    }
}
