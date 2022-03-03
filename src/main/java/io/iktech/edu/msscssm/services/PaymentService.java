package io.iktech.edu.msscssm.services;

import io.iktech.edu.msscssm.domain.Payment;
import io.iktech.edu.msscssm.domain.PaymentEvent;
import io.iktech.edu.msscssm.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {
    Payment newPayment(Payment payment);
    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> authorise(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);
}
