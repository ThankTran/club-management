package com.example.demo.role.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleRequest {
    @NotBlank(message = "Role name must not be empty")
    private String roleName;

    @NotNull(message = "Role priority must not be empty")
    @Min(value = 1, message = "Role priority must be between 1 and 10")
    @Max(value = 10, message = "Role priority must be between 1 and 10")
    private Integer priority;
}
