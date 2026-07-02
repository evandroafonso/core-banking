package com.tuum.corebanking.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQConfig {

    public static final String ACCOUNT_ROUTING_KEY = "account.insert";
    public static final String BALANCE_INSERT_ROUTING_KEY = "balance.insert";
    public static final String BALANCE_ROUTING_KEY = "balance.update";
    public static final String TRANSACTION_ROUTING_KEY = "transaction.insert";


    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter(JsonMapper.builder().build());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public DirectExchange coreEventsExchange(
            @Value("${app.rabbitmq.exchange.core-events}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue accountCreatedQueue() {
        return new Queue("account_created_queue", true);
    }

    @Bean
    public Binding accountCreatedBinding(
            Queue accountCreatedQueue,
            DirectExchange coreEventsExchange) {
        return BindingBuilder.bind(accountCreatedQueue)
                .to(coreEventsExchange)
                .with(ACCOUNT_ROUTING_KEY);
    }

    @Bean
    public Queue balanceCreatedQueue() {
        return new Queue("balance_created_queue", true);
    }

    @Bean
    public Binding balanceCreatedBinding(
            Queue balanceCreatedQueue,
            DirectExchange coreEventsExchange) {
        return BindingBuilder.bind(balanceCreatedQueue)
                .to(coreEventsExchange)
                .with(BALANCE_INSERT_ROUTING_KEY);
    }

    @Bean
    public Queue balanceUpdateQueue() {
        return new Queue("balance_update_queue", true);
    }

    @Bean
    public Binding balanceUpdateBinding(
            Queue balanceUpdateQueue,
            DirectExchange coreEventsExchange) {
        return BindingBuilder.bind(balanceUpdateQueue)
                .to(coreEventsExchange)
                .with(BALANCE_ROUTING_KEY);
    }

    @Bean
    public Queue transactionCreatedQueue() {
        return new Queue("transaction_created_queue", true);
    }

    @Bean
    public Binding transactionCreatedBinding(
            Queue transactionCreatedQueue,
            DirectExchange coreEventsExchange) {
        return BindingBuilder.bind(transactionCreatedQueue)
                .to(coreEventsExchange)
                .with(TRANSACTION_ROUTING_KEY);
    }


}
