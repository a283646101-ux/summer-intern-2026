package com.jaiot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 决策请求 DTO
 */
@Data
public class DecisionRequest {
    @NotBlank(message = "deviceId 不能为空")
    private String deviceId;
}
