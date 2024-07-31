package com.hygatech.loan_processor.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LoanDisbursementDto {
    private Long loanId;
    private Long userId;
}