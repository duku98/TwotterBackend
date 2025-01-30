package com.akaci.twotterbackend.application.service.crud.impl;

import com.akaci.twotterbackend.application.dto.response.twoot.TwootAllResponse;
import com.akaci.twotterbackend.application.dto.response.twoot.TwootResponse;
import com.akaci.twotterbackend.application.service.crud.TwootCrudService;
import com.akaci.twotterbackend.application.service.crud.UserCrudService;
import com.akaci.twotterbackend.domain.commonValidator.TwootCommentValidator;
import com.akaci.twotterbackend.domain.model.Twoot;
import com.akaci.twotterbackend.domain.model.User;
import com.akaci.twotterbackend.exceptions.response.BadRequestExceptionResponse;
import com.akaci.twotterbackend.persistence.entity.TwootJpaEntity;
import com.akaci.twotterbackend.persistence.entity.UserJpaEntity;
import com.akaci.twotterbackend.persistence.mapper.TwootEntityMapper;
import com.akaci.twotterbackend.persistence.repository.TwootRepository;
import com.akaci.twotterbackend.persistence.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


// replace repos with services.
@Service
public class TwootCrudServiceImpl implements TwootCrudService {

    private static final Logger LOGGER = LogManager.getLogger(TwootCrudServiceImpl.class);

    private final TwootRepository twootRepository;
    private final UserRepository userRepository;
    private final UserCrudService userCrudService;

    public TwootCrudServiceImpl(TwootRepository twootRepository, UserRepository userRepository, UserCrudService userCrudService) {
        this.twootRepository = twootRepository;
        this.userRepository = userRepository;
        this.userCrudService = userCrudService;
    }


    @Override
    @Transactional
    public Twoot postNewTwoot(String username, String content) {
        validateContent(content);
        UserJpaEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("username not found"));

        TwootJpaEntity twootEntity = TwootJpaEntity.builder()
                .content(content)
                .postedAt(LocalDateTime.now())
                .author(userEntity)
                .build();

        TwootJpaEntity savedTwoot = twootRepository.save(twootEntity);
        return TwootEntityMapper.toDomain(savedTwoot);
    }

    // TODO MAYBE IT IS HEAVY
    @Override
    public TwootAllResponse getAllTwoots() {
        List<TwootResponse> allTwoots = twootRepository.findAllTwootsWithCounts();
        // error when twoot list empty
        if (allTwoots.isEmpty()) {
            return new TwootAllResponse(
                    0,
                    null,
                    allTwoots

            );
        }
        return new TwootAllResponse(
                allTwoots.size(),
                allTwoots.getFirst().postedAt(),
                allTwoots
        );



    }

    @Override
    public TwootResponse getTwoot(UUID id, String username) {
        Optional<TwootJpaEntity> twootEntity = twootRepository.findById(id);
        if (twootEntity.isEmpty()) throw new BadRequestExceptionResponse("Twoot not found");
        TwootJpaEntity twootJpaEntity = twootEntity.get();
        Set<UserJpaEntity> usersWhoLikedTwoot = twootJpaEntity.getLikedByUsers();
        boolean isLikedByUser = usersWhoLikedTwoot.stream().anyMatch(usr -> usr.getUsername().equals(username));
        return new TwootResponse(
                twootJpaEntity.getId(),
                twootJpaEntity.getAuthor().getUsername(),
                twootJpaEntity.getContent(),
                twootJpaEntity.getLikedByUsers().size(),
                twootJpaEntity.getComments().size(),
                twootJpaEntity.getPostedAt(),
                isLikedByUser
        );

    }

    @Override
    public TwootResponse getTwoot(UUID id) {
        Optional<TwootJpaEntity> twootEntity = twootRepository.findById(id);
        if (twootEntity.isEmpty()) throw new BadRequestExceptionResponse("TWOOT not found");
        TwootJpaEntity twootJpaEntity = twootEntity.get();
        return new TwootResponse(
            twootJpaEntity.getId(),
            twootJpaEntity.getAuthor().getUsername(),
            twootJpaEntity.getContent(),
            twootJpaEntity.getLikedByUsers().size(),
            twootJpaEntity.getComments().size(),
            twootJpaEntity.getPostedAt(),
            false
        );
    }

    @Override
    public TwootAllResponse getAllTwoots(String username) {
        UUID userId = userCrudService.findByUsername(username).getId();
        List<TwootResponse> allTwoots = twootRepository.findAllTwootsWithCounts();
        Set<UUID> likedByUser = twootRepository.findLikedTwootsIdByUserId(userId);
        List<TwootResponse> allTwootsWithLiked = allTwoots.stream()
                .map(twoot -> new TwootResponse(
                        twoot.id(),
                        twoot.author(),
                        twoot.content(),
                        twoot.likes(),
                        twoot.commentNumber(),
                        twoot.postedAt(),
                        likedByUser.contains(twoot.id())
                ))
                .toList();

        return new TwootAllResponse(
                allTwootsWithLiked.size(),
                allTwoots.getFirst().postedAt(),
                allTwootsWithLiked
        );



    }

    private void validateContent(String content) {
        try {
            TwootCommentValidator.validateTwootContent(content);
        } catch (IllegalArgumentException e) {
            throw new BadRequestExceptionResponse(e.getMessage());
        }
    }



}
