package eca.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import eca.client.converter.MessageConverter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Rabbit listener adapter.
 *
 * @author Roman Batygin
 */
@RequiredArgsConstructor
public class RabbitListenerAdapter {

    private final MessageConverter messageConverter;

    /**
     * Setup consumer for outbound messages.
     *
     * @param channel   - channel object
     * @param queueName - queue name to listen
     * @param callback  - callback invokes when message is delivered and converted
     * @param <T>       - message generic type
     * @throws IOException in case of I/O error
     */
    public <T> void basicConsume(Channel channel, String queueName, Consumer<T> callback) throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            T message = messageConverter.fromMessage(delivery.getBody());
            callback.accept(message);
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {
        });
    }
}
