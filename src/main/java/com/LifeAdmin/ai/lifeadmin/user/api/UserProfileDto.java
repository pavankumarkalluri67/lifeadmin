package com.LifeAdmin.ai.lifeadmin.user.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    private String email;
    private String firstName;
    private String lastName;
    private String timezone;
    private String status;
    private Boolean emailVerified;
}
