package org.deneme.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class Handler extends SimpleChannelInboundHandler<String> {
    private final Node node;

    public Handler(Node node) {
        this.node = node;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.out.println("Active " + ctx.channel());
        node.connected(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Inactive " + ctx.channel());
        node.disconnected();
        super.channelInactive(ctx);

    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
        if (msg.split("\\t").length > 4)
            throw new Exception("Invalid Data");
        node.receive(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println(cause.getMessage());
        ctx.close();
    }

}