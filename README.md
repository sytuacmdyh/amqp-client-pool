# amqp-client-pool
java rabbitmq connection pool

## Requirement

java > 1.8

## Usage

```java
import com.yzccz.rabbit.ChannelPool;
import com.rabbitmq.client.Channel;

private void test(){
    final ChannelPool channelPool = new ChannelPool();
    Channel channel = channelPool.getChannel();
    // ... do something with the offical channel
}
```

## Compare

### 1. without pool

![no-pool](img/no-pool.jpeg)

### 2. with pool

![no-pool](img/pool.jpeg)