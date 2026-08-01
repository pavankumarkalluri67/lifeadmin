package com.LifeAdmin.ai.lifeadmin.obligation.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObligationUpdateRequest {
    private String title;
    private LocalDate dueDate;
    private String priority;
}
