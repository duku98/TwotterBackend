package com.akaci.twotterbackend.application.service.crud.impl;

import com.akaci.twotterbackend.application.service.crud.UserCrudService;
import com.akaci.twotterbackend.domain.Account;
import com.akaci.twotterbackend.domain.Profile;
import com.akaci.twotterbackend.domain.User;
import com.akaci.twotterbackend.persistence.entity.UserJpaEntity;
import com.akaci.twotterbackend.persistence.mapper.UserEntityMapper;
import com.akaci.twotterbackend.persistence.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class UserCrudServiceImpl implements UserCrudService {

    private static final Logger LOGGER = LogManager.getLogger(UserCrudServiceImpl.class);

    private final UserRepository userRepository;

    public UserCrudServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public User createUserFromAccount(User user, Account account) {
        Profile newProfileFromUser = createProfileFromUser(user);

        // insert profile and account to the user
        user.setProfile(newProfileFromUser);
        user.setAccount(account);

        UserJpaEntity userJpaEntity = UserEntityMapper.toJpaEntity(user);
        userRepository.save(userJpaEntity);
        return UserEntityMapper.toDomain(userJpaEntity);
    }


    private Profile createProfileFromUser(User user) {
        return new Profile(user.getUsername(), null);
    }
}
