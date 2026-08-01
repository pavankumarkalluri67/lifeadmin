package com.LifeAdmin.ai.lifeadmin.user.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {
    private String firstName;
    private String lastName;
    private String timezone;
}
