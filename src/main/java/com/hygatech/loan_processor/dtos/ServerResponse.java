package com.hygatech.loan_processor.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServerResponse {
    private int status;
    private String message;
    private LocalDateTime timeStamp;
}
