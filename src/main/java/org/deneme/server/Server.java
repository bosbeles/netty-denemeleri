package org.deneme.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.ScheduledFuture;
import org.deneme.common.AbstractNode;
import org.deneme.common.Initializer;

import java.util.concurrent.TimeUnit;

/**
 * Created by ihsan on 4/18/2015.
 */
public class Server extends AbstractNode {

    private static int RECONNECT_TIME = 22;
    ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    Channel lastActive;
    Channel serverChannel;
    ScheduledFuture<?> reconnectionFuture;
    private int port;
    private ServerBootstrap b;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public Server(int port) {
        this.port = port;

    }

    @Override
    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(1);

        b = new ServerBootstrap();
        ServerBootstrap serverBootstrap = b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new Initializer(this));

        try {
            serverChannel = b.bind(port).sync().channel();
            serverChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            System.out.println("Interrupt: " + e.getMessage());
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    @Override
    protected void doSend(String message) {
        if(lastActive != null) {
            lastActive.eventLoop().execute(
                    () -> {
                        channels.writeAndFlush(message + "\n");
                    }
            );
        }
    }


    @Override
    public void kill() {
        System.out.println("closing...");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public void stop() {
        kill();
    }

    @Override
    public void disconnected() {
        System.out.println("Disconnected");
        if (reconnectionFuture == null || reconnectionFuture.cancel(false)) {
            reconnectionFuture = serverChannel.eventLoop().schedule(() -> {
                stop();
            }, RECONNECT_TIME, TimeUnit.SECONDS);
        }


    }

    @Override
    public void connected(Channel channel) {
        if (reconnectionFuture == null || reconnectionFuture.cancel(false)) {
            lastActive = channel;
            channels.add(channel);
        }

    }
}
