package io.iktech.edu.msscssm.config;

import io.iktech.edu.msscssm.domain.PaymentEvent;
import io.iktech.edu.msscssm.domain.PaymentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StateMachineConfigTest {
    @Autowired
    StateMachineFactory<PaymentState, PaymentEvent> factory;

    @Test
    void testNewStateMachine() {
        StateMachine<PaymentState, PaymentEvent> sm = factory.getStateMachine(UUID.randomUUID());
        sm.start();
        assertEquals(PaymentState.NEW, sm.getState().getId());
        sm.sendEvent(PaymentEvent.PRE_AUTHORIZE);
        assertEquals(PaymentState.NEW, sm.getState().getId());
        sm.sendEvent(PaymentEvent.PRE_AUTH_APPROVED);
        assertEquals(PaymentState.PRE_AUTH, sm.getState().getId());
        sm.sendEvent(PaymentEvent.PRE_AUTH_DECLINED);
        assertEquals(PaymentState.PRE_AUTH, sm.getState().getId());
    }
}