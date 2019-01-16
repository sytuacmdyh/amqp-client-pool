package com.yzccz.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.yzccz.rabbit.exceptions.ChannelException;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

public class ChannelPool implements Cloneable {
    private GenericObjectPool<Channel> internalPool;
    public static GenericObjectPoolConfig defaultConfig;

    static {
        defaultConfig = new GenericObjectPoolConfig();
        defaultConfig.setMaxTotal(10);
        defaultConfig.setBlockWhenExhausted(false);
    }

    public ChannelPool() {
        this(defaultConfig, new ChannelFactory());
    }

    public ChannelPool(final GenericObjectPoolConfig poolConfig, ChannelFactory factory) {
        if (this.internalPool != null) {
            try {
                closeInternalPool();
            } catch (Exception e) {
            }
        }

        this.internalPool = new GenericObjectPool<Channel>(factory, poolConfig);
    }

    private void closeInternalPool() {
        try {
            internalPool.close();
        } catch (Exception e) {
            throw new ChannelException("Could not destroy the pool", e);
        }
    }

    public void returnChannel(Channel channel) {
        try {
            if (channel.isOpen()) {
                internalPool.returnObject(channel);
            } else {
                internalPool.invalidateObject(channel);
            }
        } catch (Exception e) {
            throw new ChannelException("Could not return the resource to the pool", e);
        }
    }

    public Channel getChannel() {
        try {
            return internalPool.borrowObject();
        } catch (NoSuchElementException nse) {
            if (null == nse.getCause()) { // The exception was caused by an exhausted pool
                throw new ChannelException("Could not get a resource since the pool is exhausted", nse);
            }
            // Otherwise, the exception was caused by the implemented activateObject() or ValidateObject()
            throw new ChannelException("Could not get a resource from the pool", nse);
        } catch (Exception e) {
            throw new ChannelException("Could not get a resource from the pool", e);
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        testThreadPool();
//        testNormal();
    }

    private static void testNormal() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        final Connection connection = connectionFactory.newConnection();

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    while (true) {
                        Channel channel = connection.createChannel();

                        final String EXCHANGE_NAME = "dyh";
                        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

                        //发送消息
                        channel.basicPublish(EXCHANGE_NAME, "1", null, "s".getBytes());

                        //接收消息
//                        String queueName = channel.queueDeclare().getQueue();
//                        channel.queueBind(queueName, EXCHANGE_NAME, "1");
//                        QueueingConsumer consumer = new QueueingConsumer(channel);
//                        channel.basicConsume(queueName, false, consumer);
//                        QueueingConsumer.Delivery delivery = consumer.nextDelivery(1);
//                        if (delivery != null) {
//                            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//                        }
//                        channel.basicCancel(consumer.getConsumerTag());

                        //关闭资源
                        channel.close();
                    }
                } catch (Exception e) {

                }
            }).start();
        }

    }

    private static void testThreadPool() {
        final ChannelPool channelPool = new ChannelPool();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    while (true) {
                        Channel channel = channelPool.getChannel();

                        final String EXCHANGE_NAME = "dyh";
                        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

                        //发送消息
                        channel.basicPublish(EXCHANGE_NAME, "1", null, "s".getBytes());

                        //接收消息
//                        String queueName = channel.queueDeclare().getQueue();
//                        channel.queueBind(queueName, EXCHANGE_NAME, "1");
//                        QueueingConsumer consumer = new QueueingConsumer(channel);
//                        channel.basicConsume(queueName, false, consumer);
//                        QueueingConsumer.Delivery delivery = consumer.nextDelivery(1);
//                        if (delivery != null) {
//                            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//                        }
//                        channel.basicCancel(consumer.getConsumerTag());

                        //关闭资源
                        channelPool.returnChannel(channel);
                    }
                } catch (Exception e) {

                }
            }).start();
        }

    }
}
