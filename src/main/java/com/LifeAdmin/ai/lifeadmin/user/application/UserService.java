package com.LifeAdmin.ai.lifeadmin.user.application;


import com.LifeAdmin.ai.lifeadmin.auth.domain.TimezoneValidator;
import com.LifeAdmin.ai.lifeadmin.auth.domain.User;
import com.LifeAdmin.ai.lifeadmin.auth.repository.UserRepository;
import com.LifeAdmin.ai.lifeadmin.common.error.ResourceNotFoundException;
import com.LifeAdmin.ai.lifeadmin.common.security.CurrentUserProvider;
import com.LifeAdmin.ai.lifeadmin.user.api.UserProfileDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

/**
 * User_Service: profile retrieval and update.
 * Requirement 5
 */
@Service
@Validated
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(UUID userId, String firstName, String lastName, String timezone) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (firstName != null && !firstName.isBlank()) {
            user.setFirstName(firstName);
        }
        if (lastName != null) {
            user.setLastName(lastName);
        }
        if (timezone != null && !timezone.isBlank()) {
            if (!TimezoneValidator.isValid(timezone)) {
                throw new IllegalArgumentException("Invalid timezone: " + timezone);
            }
            user.setTimezone(timezone);
        }

        userRepository.save(user);
        return mapToDto(user);
    }

    private UserProfileDto mapToDto(User user) {
        return UserProfileDto.builder()
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .timezone(user.getTimezone())
            .status(user.getStatus().toString())
            .emailVerified(user.getEmailVerified())
            .build();
    }
}
