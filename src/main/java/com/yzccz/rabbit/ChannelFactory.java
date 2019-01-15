package com.yzccz.rabbit;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.yzccz.rabbit.exceptions.ChannelException;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ChannelFactory implements PooledObjectFactory<ChannelC> {
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

    public PooledObject<ChannelC> makeObject() throws Exception {
        return new DefaultPooledObject<ChannelC>(new ChannelC(connection.createChannel()));
    }

    public void destroyObject(PooledObject<ChannelC> pooledObject) throws Exception {
        final ChannelC channel = pooledObject.getObject();
        if (channel.getChannel().isOpen()) {
            try {
                channel.close();
            } catch (Exception e) {
            }
        }
    }

    public boolean validateObject(PooledObject<ChannelC> pooledObject) {
        final ChannelC channel = pooledObject.getObject();
        return channel.getChannel().isOpen();
    }

    public void activateObject(PooledObject<ChannelC> pooledObject) throws Exception {

    }

    public void passivateObject(PooledObject<ChannelC> pooledObject) throws Exception {

    }
}
