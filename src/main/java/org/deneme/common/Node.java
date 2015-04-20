package org.deneme.common;

import io.netty.channel.Channel;

/**
 * Created by ihsan on 4/18/2015.
 */
public interface Node {
    void start();

    void kill();

    void stop();

    void send(String message);

    void receive(String message);

    void disconnected();

    void connected(Channel channel);
}
