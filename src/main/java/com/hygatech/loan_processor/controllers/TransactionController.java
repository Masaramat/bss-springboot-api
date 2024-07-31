package com.hygatech.loan_processor.controllers;

import com.hygatech.loan_processor.dtos.LoanProductDto;
import com.hygatech.loan_processor.dtos.TransactionDto;
import com.hygatech.loan_processor.entities.Transaction;
import com.hygatech.loan_processor.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
@Tag(name = "Transactions API", description = "Endpoints for handling transactions")
public class TransactionController {
    private final TransactionService service;

    @PostMapping
    @Operation(summary = "Create transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction Created"),
            @ApiResponse(responseCode = "400", description = "Bad request: Failed validation"),
            @ApiResponse(responseCode = "403", description = "Data integrity violation")
    })
    public ResponseEntity<Transaction> create(@RequestBody TransactionDto transactionDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(transactionDto));
    }
}
