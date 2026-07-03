package com.tuum.corebanking.messaging.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RabbitMQConfigTest {

    private RabbitMQConfig config;

    @BeforeEach
    void setUp() {
        config = new RabbitMQConfig();
    }

    @Test
    void jsonMessageConverterShouldReturnJacksonJsonMessageConverter() {
        MessageConverter converter = config.jsonMessageConverter();

        assertThat(converter).isInstanceOf(JacksonJsonMessageConverter.class);
    }

    @Test
    void rabbitTemplateShouldUseProvidedConnectionFactoryAndMessageConverter() {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        MessageConverter messageConverter = mock(MessageConverter.class);

        RabbitTemplate template = config.rabbitTemplate(connectionFactory, messageConverter);

        assertThat(template.getConnectionFactory()).isEqualTo(connectionFactory);
        assertThat(template.getMessageConverter()).isEqualTo(messageConverter);
    }

    @Test
    void coreEventsExchangeShouldBeDurableNonAutoDeleteWithGivenName() {
        DirectExchange exchange = config.coreEventsExchange("core-events-exchange");

        assertThat(exchange.getName()).isEqualTo("core-events-exchange");
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
    }

    @Test
    void accountCreatedQueueShouldBeDurableWithExpectedName() {
        Queue queue = config.accountCreatedQueue();

        assertThat(queue.getName()).isEqualTo("account_created_queue");
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    void accountCreatedBindingShouldBindQueueToExchangeWithAccountRoutingKey() {
        Queue queue = config.accountCreatedQueue();
        DirectExchange exchange = config.coreEventsExchange("core-events-exchange");

        Binding binding = config.accountCreatedBinding(queue, exchange);

        assertThat(binding.getDestination()).isEqualTo(queue.getName());
        assertThat(binding.getExchange()).isEqualTo(exchange.getName());
        assertThat(binding.getRoutingKey()).isEqualTo(RabbitMQConfig.ACCOUNT_ROUTING_KEY);
        assertThat(binding.getDestinationType()).isEqualTo(Binding.DestinationType.QUEUE);
    }

    @Test
    void balanceCreatedQueueShouldBeDurableWithExpectedName() {
        Queue queue = config.balanceCreatedQueue();

        assertThat(queue.getName()).isEqualTo("balance_created_queue");
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    void balanceCreatedBindingShouldBindQueueToExchangeWithBalanceInsertRoutingKey() {
        Queue queue = config.balanceCreatedQueue();
        DirectExchange exchange = config.coreEventsExchange("core-events-exchange");

        Binding binding = config.balanceCreatedBinding(queue, exchange);

        assertThat(binding.getDestination()).isEqualTo(queue.getName());
        assertThat(binding.getExchange()).isEqualTo(exchange.getName());
        assertThat(binding.getRoutingKey()).isEqualTo(RabbitMQConfig.BALANCE_INSERT_ROUTING_KEY);
    }

    @Test
    void balanceUpdateQueueShouldBeDurableWithExpectedName() {
        Queue queue = config.balanceUpdateQueue();

        assertThat(queue.getName()).isEqualTo("balance_update_queue");
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    void balanceUpdateBindingShouldBindQueueToExchangeWithBalanceUpdateRoutingKey() {
        Queue queue = config.balanceUpdateQueue();
        DirectExchange exchange = config.coreEventsExchange("core-events-exchange");

        Binding binding = config.balanceUpdateBinding(queue, exchange);

        assertThat(binding.getDestination()).isEqualTo(queue.getName());
        assertThat(binding.getExchange()).isEqualTo(exchange.getName());
        assertThat(binding.getRoutingKey()).isEqualTo(RabbitMQConfig.BALANCE_ROUTING_KEY);
    }

    @Test
    void transactionCreatedQueueShouldBeDurableWithExpectedName() {
        Queue queue = config.transactionCreatedQueue();

        assertThat(queue.getName()).isEqualTo("transaction_created_queue");
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    void transactionCreatedBindingShouldBindQueueToExchangeWithTransactionRoutingKey() {
        Queue queue = config.transactionCreatedQueue();
        DirectExchange exchange = config.coreEventsExchange("core-events-exchange");

        Binding binding = config.transactionCreatedBinding(queue, exchange);

        assertThat(binding.getDestination()).isEqualTo(queue.getName());
        assertThat(binding.getExchange()).isEqualTo(exchange.getName());
        assertThat(binding.getRoutingKey()).isEqualTo(RabbitMQConfig.TRANSACTION_ROUTING_KEY);
    }
}