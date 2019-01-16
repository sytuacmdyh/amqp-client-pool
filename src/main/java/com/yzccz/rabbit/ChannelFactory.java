package com.yzccz.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.yzccz.rabbit.exceptions.ChannelException;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ChannelFactory implements PooledObjectFactory<Channel> {
    private Connection connection;

    public ChannelFactory() {
        this(null);
    }

    public ChannelFactory(String uri) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            if (uri != null) {
                factory.setUri(uri);
            }
            connection = factory.newConnection();
        } catch (Exception e) {
            throw new ChannelException("连接失败", e);
        }
    }

    public PooledObject<Channel> makeObject() throws Exception {
        return new DefaultPooledObject<Channel>(connection.createChannel());
    }

    public void destroyObject(PooledObject<Channel> pooledObject) throws Exception {
        final Channel channel = pooledObject.getObject();
        if (channel.isOpen()) {
            try {
                channel.close();
            } catch (Exception e) {
            }
        }
    }

    public boolean validateObject(PooledObject<Channel> pooledObject) {
        final Channel channel = pooledObject.getObject();
        return channel.isOpen();
    }

    public void activateObject(PooledObject<Channel> pooledObject) throws Exception {

    }

    public void passivateObject(PooledObject<Channel> pooledObject) throws Exception {

    }
}
