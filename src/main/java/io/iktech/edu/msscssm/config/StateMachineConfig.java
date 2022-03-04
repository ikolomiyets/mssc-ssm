package io.iktech.edu.msscssm.config;

import io.iktech.edu.msscssm.domain.PaymentEvent;
import io.iktech.edu.msscssm.domain.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

import static io.iktech.edu.msscssm.domain.PaymentEvent.*;
import static io.iktech.edu.msscssm.domain.PaymentState.*;
import static io.iktech.edu.msscssm.services.PaymentServiceImpl.PAYMENT_ID_HEADER;

@Slf4j
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {
    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.AUTH)
                .end(PRE_AUTH_ERROR)
                .end(PaymentState.AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions
                .withExternal()
                .source(NEW)
                .target(NEW)
                .event(PRE_AUTHORIZE)
                .action(preAuthAction())
            .and()
                .withExternal()
                .source(NEW)
                .target(PRE_AUTH)
                .event(PRE_AUTH_APPROVED)
            .and()
                .withExternal().source(NEW).target(PRE_AUTH_ERROR).event(PRE_AUTH_DECLINED)
            .and()
                .withExternal().source(PRE_AUTH).target(PRE_AUTH).event(AUTHORIZE)
                .action(authAction())
            .and()
                .withExternal().source(PRE_AUTH).target(AUTH).event(AUTH_APPROVED)
            .and()
                .withExternal().source(PRE_AUTH).target(AUTH_ERROR).event(AUTH_DECLINED);

    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info(String.format("stateChanged(from: %s, to: %s", from, to));
            }
        };

        config.withConfiguration().listener(adapter);
    }

    private Action<PaymentState, PaymentEvent> preAuthAction() {
        return context -> {
            System.out.println("PreAuth was called");
            if (new Random().nextInt(10) < 8) {
                System.out.println("Approved");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PRE_AUTH_APPROVED).setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER)).build());
            } else {
                System.out.println("Declined: no credit!");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PRE_AUTH_DECLINED).setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER)).build());
            }
        };
    }

    private Action<PaymentState, PaymentEvent> authAction() {
        return context -> {
            System.out.println("Auth was called");
            if (new Random().nextInt(10) < 7) {
                System.out.println("Auth Approved");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(AUTH_APPROVED).setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER)).build());
            } else {
                System.out.println("Auth Declined: authorization error!");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(AUTH_DECLINED).setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER)).build());
            }
        };
    }
}
