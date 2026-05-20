package com.notfound.bookservice.config;

import com.notfound.bookservice.messaging.SagaMessageTypes;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "bookstore.saga.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMqConfig {

    @Bean
    TopicExchange commandsExchange(SagaStockProperties properties) {
        return new TopicExchange(properties.getCommandsExchange(), true, false);
    }

    @Bean
    TopicExchange eventsExchange(SagaStockProperties properties) {
        return new TopicExchange(properties.getEventsExchange(), true, false);
    }

    @Bean
    Queue bookCommandsQueue(SagaStockProperties properties) {
        return new Queue(properties.getCommandsQueue(), true);
    }

    @Bean
    Binding reserveCommandBinding(Queue bookCommandsQueue, TopicExchange commandsExchange) {
        return BindingBuilder.bind(bookCommandsQueue)
                .to(commandsExchange)
                .with(SagaMessageTypes.RK_RESERVE_COMMAND);
    }

    @Bean
    Binding confirmCommandBinding(Queue bookCommandsQueue, TopicExchange commandsExchange) {
        return BindingBuilder.bind(bookCommandsQueue)
                .to(commandsExchange)
                .with(SagaMessageTypes.RK_CONFIRM_COMMAND);
    }

    @Bean
    Binding releaseCommandBinding(Queue bookCommandsQueue, TopicExchange commandsExchange) {
        return BindingBuilder.bind(bookCommandsQueue)
                .to(commandsExchange)
                .with(SagaMessageTypes.RK_RELEASE_COMMAND);
    }

    @Bean
    MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
