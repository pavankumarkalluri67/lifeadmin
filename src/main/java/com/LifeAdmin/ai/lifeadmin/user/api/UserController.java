package com.LifeAdmin.ai.lifeadmin.user.api;



import com.LifeAdmin.ai.lifeadmin.common.security.CurrentUserProvider;
import com.LifeAdmin.ai.lifeadmin.user.application.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * UserController: manages user profile operations.
 * Requirement 5
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    public UserController(UserService userService, CurrentUserProvider currentUserProvider) {
        this.userService = userService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile() {
        UserProfileDto profile = userService.getProfile(currentUserProvider.getUserId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(@RequestBody UserProfileUpdateRequest request) {
        UserProfileDto updated = userService.updateProfile(
            currentUserProvider.getUserId(),
            request.getFirstName(),
            request.getLastName(),
            request.getTimezone()
        );
        return ResponseEntity.ok(updated);
    }
}
