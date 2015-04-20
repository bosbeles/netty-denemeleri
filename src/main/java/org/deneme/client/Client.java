package org.deneme.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.deneme.common.AbstractNode;
import org.deneme.common.Initializer;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Created by ihsan on 4/18/2015.
 */
public class Client extends AbstractNode {

    private static int RECONNECT_TIME = 43;

    private Channel channel;
    private int port;
    private String host;
    private boolean terminationExpected;
    private long restartTime;


    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void start() {
        terminationExpected = false;
        start(false);
    }

    @Override
    public void connected(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void disconnected() {
        System.out.println("Channel disconnected");
        if(!terminationExpected) {
            restartTime = Instant.now().toEpochMilli();
            start(true);
        }
    }

    @Override
    public void kill() {
        if (channel != null) {
            System.out.println(channel + " closing...");
            channel.close();
        }
    }

    @Override
    public void stop() {
        terminationExpected = true;
        kill();
    }

    @Override
    public void doSend(String message) {
        channel.eventLoop().execute(()->{
            channel.writeAndFlush(message + "\n");
        });

    }

    private void start(final boolean restart) {
        try {
            EventLoopGroup group = new NioEventLoopGroup(1);

            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new Initializer(Client.this));


            System.out.println("Connecting...");
            ChannelFuture future = b.connect(host, port);

            future.addListener((ChannelFuture channelFuture) -> {
                if (!channelFuture.isSuccess()) {
                    if (restart) {
                        if (!terminationExpected) {
                            if ( (Instant.now().toEpochMilli() - restartTime) < RECONNECT_TIME * 1000) {
                                EventLoop eventLoop = channelFuture.channel().eventLoop();
                                eventLoop.schedule(() -> {
                                    start(true);
                                }, 5, TimeUnit.SECONDS);
                            }
                            else {
                                System.out.println("Reconnection timeout: " + channelFuture.channel());
                            }
                        }
                    } else {
                        System.out.println("Channel not connected: " + channelFuture.channel());
                    }
                }
                else {
                    System.out.println("Channel success: " + channelFuture.channel());
                }
            });

            channel = future.sync().channel();
        } catch (InterruptedException e) {
            System.out.println("Interrupt: " +  e.getMessage());
            e.printStackTrace();
        }
    }
}
