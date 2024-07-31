package com.hygatech.loan_processor.services;

import com.hygatech.loan_processor.dtos.TransactionDto;
import com.hygatech.loan_processor.entities.*;
import com.hygatech.loan_processor.exceptions.ObjectNotFoundException;
import com.hygatech.loan_processor.repositories.*;
import com.hygatech.loan_processor.utils.GeneralUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository repository;
    private final AccountRepository accountRepository;
    private final Validator validator;
    private final AdasheSetupRepository adasheSetupRepository;
    private final AdasheCommissionRepository adasheCommissionRepository;

    private final UserRepository userRepository;

    public void createTransaction(Account account, String description, Double amount, String trxNo){
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setTrxNo(trxNo);
        repository.save(transaction);
    }

    public Transaction create(TransactionDto transactionDto){
        String trxNo = GeneralUtils.generateTransactionNumber();
        System.out.println("TransactionDto: " + transactionDto);
        System.out.println("Transaction N");
        Set<ConstraintViolation<TransactionDto>> violations = validator.validate(transactionDto);
        if (!violations.isEmpty()){
            throw new ConstraintViolationException(violations);
        }
        Account updateAccount = getAccount(transactionDto.getAccountId());
        Transaction transaction = new Transaction();
        transaction.setTrxNo(trxNo);
        transaction.setDescription(transactionDto.getDescription());
        transaction.setAccount(updateAccount);
        if(transactionDto.getUserId() != null){
            User user = getUser(transactionDto.getUserId());
            transaction.setUser(user);
        }


        if(transactionDto.getTrxType() == TransactionType.credit){
            if (updateAccount.getAccountType() == AccountType.ADASHE){
                AdasheSetup adasheSetup = getRecentAdasheSetUp();
                if (transactionDto.getAmount() < adasheSetup.getMinimumDeposit()){
                    throw new ObjectNotFoundException("Deposit insufficient");
                }
            }
            transaction.setAmount(transactionDto.getAmount());
            updateAccount.setBalance(updateAccount.getBalance() + transactionDto.getAmount());
        }else if(transactionDto.getTrxType() == TransactionType.debit){
            AdasheSetup adasheSetup = getRecentAdasheSetUp();
            calculateCommission(updateAccount, adasheSetup, transactionDto.getAmount(), trxNo);
            if (transactionDto.getAmount() > updateAccount.getBalance()){
                throw new ObjectNotFoundException("Insufficient Balance");
            }
            transaction.setAmount(-transactionDto.getAmount());
            updateAccount.setBalance(updateAccount.getBalance() - transactionDto.getAmount());
        }

        accountRepository.save(updateAccount);
        System.out.println(updateAccount);
        return repository.save(transaction);
    }

    private Account getAccount(Long id){
        return accountRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("Account not found"));
    }

    private AdasheSetup getRecentAdasheSetUp(){
        return adasheSetupRepository.findFirstByOrderByIdDesc().orElseThrow(() -> new ObjectNotFoundException("Setup not found"));
    }

    private AdasheCommission getLastAdasheCommissionPaid(Long accountId){
        return adasheCommissionRepository.findFirstByAccountIdOrderByIdDesc(accountId).orElse(null);
    }

    private Transaction getLastFirstDeposit(Long accountId){
        return repository.findFirstByAccountIdAndAmountGreaterThanOrderByIdAsc(accountId, 0.00).orElseThrow(() -> new ObjectNotFoundException("No deposit has been made on this account. Insufficient funds"));
    }

    private User getUser(Long userId){
        return userRepository.findById(userId).orElse(null);
    }

    private void calculateCommission(Account account, AdasheSetup adasheSetup, Double amount, String trxId) {
        double commission = (adasheSetup.getCommissionRate() / 100) * amount;
        LocalDateTime lastCommissionDate;
        double notCommissionedSavings;
        Transaction firstDeposit;

        if (getLastAdasheCommissionPaid(account.getId()) == null) {
            firstDeposit = getLastFirstDeposit(account.getId());
            lastCommissionDate = firstDeposit.getTrxDate();
        } else {
            lastCommissionDate = getLastAdasheCommissionPaid(account.getId()).getTrxDate();
        }

        notCommissionedSavings = repository.findSumOfDepositsByAccountIdAndTrxDateGreaterThanEqual(account, lastCommissionDate);
        long daysNotCommissioned = Duration.between(lastCommissionDate, LocalDateTime.now()).toDays();
        double averageDailySavings = notCommissionedSavings / daysNotCommissioned;
        int commissionDays = (int) Math.ceil(amount / averageDailySavings);

        AdasheCommission adasheCommission = new AdasheCommission();
        adasheCommission.setAccount(account);
        adasheCommission.setAmount(commission);
        adasheCommission.setTrxId(trxId);
        adasheCommission.setTrxDate(lastCommissionDate.plusDays(commissionDays));

        adasheCommissionRepository.save(adasheCommission);
    }
}


