package com.upi.transaction.service;

import com.upi.transaction.client.AccountServiceClient;
import com.upi.transaction.entity.Transaction;
import com.upi.transaction.enums.TransactionStatus;
import com.upi.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceIntegrationTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, accountServiceClient);
    }

    @Test
    void testSuccessfulTransfer() {
        // Arrange
        String senderUpiId = "sender@bank";
        String receiverUpiId = "receiver@bank";
        BigDecimal amount = new BigDecimal("100.00");
        String description = "Test transfer";

        // Mock successful UPI ID validation
        when(accountServiceClient.validateUpiId(senderUpiId)).thenReturn(Mono.just(true));
        when(accountServiceClient.validateUpiId(receiverUpiId)).thenReturn(Mono.just(true));

        // Mock sufficient balance check
        AccountServiceClient.BalanceResponse balanceResponse = 
                new AccountServiceClient.BalanceResponse(new BigDecimal("500.00"), senderUpiId);
        when(accountServiceClient.getBalance(senderUpiId)).thenReturn(Mono.just(balanceResponse));

        // Mock successful debit and credit operations
        AccountServiceClient.BalanceResponse debitResponse = 
                new AccountServiceClient.BalanceResponse(new BigDecimal("400.00"), senderUpiId);
        AccountServiceClient.BalanceResponse creditResponse = 
                new AccountServiceClient.BalanceResponse(new BigDecimal("600.00"), receiverUpiId);

        when(accountServiceClient.updateBalance(eq(senderUpiId), eq(amount.negate()), eq("DEBIT")))
                .thenReturn(Mono.just(debitResponse));
        when(accountServiceClient.updateBalance(eq(receiverUpiId), eq(amount), eq("CREDIT")))
                .thenReturn(Mono.just(creditResponse));

        // Mock transaction save operations
        Transaction pendingTransaction = new Transaction(senderUpiId, receiverUpiId, amount, description, 
                TransactionStatus.PENDING, "TXN123456");
        Transaction successTransaction = new Transaction(senderUpiId, receiverUpiId, amount, description, 
                TransactionStatus.SUCCESS, "TXN123456");

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(pendingTransaction)
                .thenReturn(successTransaction);

        // Act
        Mono<Transaction> result = transactionService.processTransfer(senderUpiId, receiverUpiId, amount, description);
        Transaction transaction = result.block();
        
        // Assert
        assertNotNull(transaction);
        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());
        assertEquals(senderUpiId, transaction.getSenderUpiId());
        assertEquals(receiverUpiId, transaction.getReceiverUpiId());
        assertEquals(amount, transaction.getAmount());

        // Verify interactions
        verify(accountServiceClient).validateUpiId(senderUpiId);
        verify(accountServiceClient).validateUpiId(receiverUpiId);
        verify(accountServiceClient).getBalance(senderUpiId);
        verify(accountServiceClient).updateBalance(senderUpiId, amount.negate(), "DEBIT");
        verify(accountServiceClient).updateBalance(receiverUpiId, amount, "CREDIT");
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void testInsufficientBalanceTransfer() {
        // Arrange
        String senderUpiId = "sender@bank";
        String receiverUpiId = "receiver@bank";
        BigDecimal amount = new BigDecimal("1000.00");
        String description = "Test transfer";

        // Mock successful UPI ID validation
        when(accountServiceClient.validateUpiId(senderUpiId)).thenReturn(Mono.just(true));
        when(accountServiceClient.validateUpiId(receiverUpiId)).thenReturn(Mono.just(true));

        // Mock insufficient balance
        AccountServiceClient.BalanceResponse balanceResponse = 
                new AccountServiceClient.BalanceResponse(new BigDecimal("500.00"), senderUpiId);
        when(accountServiceClient.getBalance(senderUpiId)).thenReturn(Mono.just(balanceResponse));

        // Mock transaction save
        Transaction pendingTransaction = new Transaction(senderUpiId, receiverUpiId, amount, description, 
                TransactionStatus.PENDING, "TXN123456");
        Transaction failedTransaction = new Transaction(senderUpiId, receiverUpiId, amount, description, 
                TransactionStatus.FAILED, "TXN123456");

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(pendingTransaction)
                .thenReturn(failedTransaction);

        // Act & Assert
        Mono<Transaction> result = transactionService.processTransfer(senderUpiId, receiverUpiId, amount, description);
        
        assertThrows(IllegalArgumentException.class, () -> {
            result.block();
        });

        // Verify interactions
        verify(accountServiceClient).validateUpiId(senderUpiId);
        verify(accountServiceClient).validateUpiId(receiverUpiId);
        verify(accountServiceClient).getBalance(senderUpiId);
        verify(accountServiceClient, never()).updateBalance(anyString(), any(BigDecimal.class), anyString());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void testInvalidUpiIdTransfer() {
        // Arrange
        String senderUpiId = "invalid@bank";
        String receiverUpiId = "receiver@bank";
        BigDecimal amount = new BigDecimal("100.00");
        String description = "Test transfer";

        // Mock invalid sender UPI ID
        when(accountServiceClient.validateUpiId(senderUpiId)).thenReturn(Mono.just(false));
        when(accountServiceClient.validateUpiId(receiverUpiId)).thenReturn(Mono.just(true));

        // Mock transaction save
        Transaction pendingTransaction = new Transaction(senderUpiId, receiverUpiId, amount, description, 
                TransactionStatus.PENDING, "TXN123456");
        Transaction failedTransaction = new Transaction(senderUpiId, receiverUpiId, amount, description, 
                TransactionStatus.FAILED, "TXN123456");

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(pendingTransaction)
                .thenReturn(failedTransaction);

        // Act & Assert
        Mono<Transaction> result = transactionService.processTransfer(senderUpiId, receiverUpiId, amount, description);
        
        assertThrows(IllegalArgumentException.class, () -> {
            result.block();
        });

        // Verify interactions
        verify(accountServiceClient).validateUpiId(senderUpiId);
        verify(accountServiceClient).validateUpiId(receiverUpiId);
        verify(accountServiceClient, never()).getBalance(anyString());
        verify(accountServiceClient, never()).updateBalance(anyString(), any(BigDecimal.class), anyString());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void testGetTransactionHistory() {
        // Arrange
        String upiId = "user@bank";
        
        // Act
        transactionService.getTransactionHistory(upiId);
        
        // Assert
        verify(transactionRepository).findAllTransactionsByUpiId(upiId);
    }

    @Test
    void testGetTransactionByReference() {
        // Arrange
        String transactionRef = "TXN123456";
        Transaction transaction = new Transaction("sender@bank", "receiver@bank", 
                new BigDecimal("100.00"), "Test", TransactionStatus.SUCCESS, transactionRef);
        
        when(transactionRepository.findByTransactionRef(transactionRef))
                .thenReturn(Optional.of(transaction));
        
        // Act
        Optional<Transaction> result = transactionService.getTransactionByReference(transactionRef);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(transactionRef, result.get().getTransactionRef());
        verify(transactionRepository).findByTransactionRef(transactionRef);
    }
}