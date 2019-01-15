package com.yzccz.rabbit;

import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ChannelC {
    private ChannelPool dataSource;
    private com.rabbitmq.client.Channel channel;

    public ChannelC(com.rabbitmq.client.Channel channel) {
        this.channel = channel;
    }

    public void setDataSource(ChannelPool dataSource) {
        this.dataSource = dataSource;
    }

    public Channel getChannel() {
        return channel;
    }

    public void close() throws IOException, TimeoutException {
        if (dataSource != null) {
            dataSource.returnResource(this);
        } else {
            channel.close();
        }
    }
}
