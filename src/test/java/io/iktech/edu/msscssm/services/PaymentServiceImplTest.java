package io.iktech.edu.msscssm.services;

import io.iktech.edu.msscssm.domain.Payment;
import io.iktech.edu.msscssm.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.math.BigDecimal;

import static io.iktech.edu.msscssm.domain.PaymentState.NEW;
import static io.iktech.edu.msscssm.domain.PaymentState.PRE_AUTH;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Transactional
    @Test
    void preAuth() {
        Payment savedPayment = paymentService.newPayment(payment);
        assertEquals(NEW, savedPayment.getState());
        paymentService.preAuth(savedPayment.getId());

        Payment preAuthedPayment = paymentRepository.getById(savedPayment.getId());
        System.out.println(preAuthedPayment.getState());
    }

    @Transactional
    @Test
    @RepeatedTest(10)
    void auth() {
        Payment savedPayment = paymentService.newPayment(payment);
        assertEquals(NEW, savedPayment.getState());
        paymentService.preAuth(savedPayment.getId());

        Payment preAuthedPayment = paymentRepository.getById(savedPayment.getId());
        if (preAuthedPayment.getState() == PRE_AUTH) {
            System.out.println("Pre-authorization succeeded, trying to authorize payment");
            paymentService.authorise(preAuthedPayment.getId());
            Payment authorizedPayment = paymentRepository.getById(savedPayment.getId());
            System.out.println(authorizedPayment.getState());
        } else {
            System.out.println("Pre-authorization failed, cannot proceed");
        }
    }
}