package org.deneme.test;


import org.deneme.client.Client;
import org.deneme.common.Node;
import org.deneme.server.Server;

import java.io.InputStreamReader;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ihsan on 4/18/2015.
 */
public class Main {

    static volatile boolean messagesActive = true;

    public static void main(String[] args) {
        Logger.getLogger("a").setLevel(Level.FINE);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINER);
        Logger.getLogger("a").addHandler(handler);

        String host = System.getProperty("netty.host");
        if(host == null || host.equals("")) {
            host = "localhost";
        }

        int serverPort = 5001;
        String port = System.getProperty("netty.port");
        try {
            serverPort = Integer.parseInt(port);
        }
        catch (Exception ex) {

        }

        Logger.getLogger("a").fine("Starting @" + host + ":" + serverPort);

        Scanner scanner = new Scanner(new InputStreamReader(System.in));
        final Node client = new Client(host, serverPort);
        final Node server = new Server(serverPort);

        String command = "";
        while (!(command = scanner.nextLine()).equals("x")) {
            switch (command) {
                case "s0":
                    bgTask(() -> {
                        server.start();
                    });
                    break;
                case "s1":
                    bgTask(() -> {
                        client.start();
                    });
                    break;
                case "c0":
                    server.stop();
                    break;
                case "c1":
                    client.stop();
                    break;
                case "x0":
                    server.kill();
                    break;
                case "x1":
                    client.kill();
                    break;
                case "t0":
                    server.send("ttt \t \t \t \t \t \t");
                    break;
                case "t1":
                    client.send("ttt \t \t \t \t \t \t");
                    break;
                case "d":
                    messagesActive = !messagesActive;
                    System.out.println(messagesActive ? "Messages Active" : "Messages Not Active");
                    break;
                default:
                    if(command.startsWith("m0")) {
                        message(server, getParams(command));
                    }
                    else if(command.startsWith("m1")) {
                        message(client, getParams(command));
                    }
            }
        }
        System.out.println("Exit...");
        System.exit(0);
    }

    private static int[] getParams(String command) {
        String[] split = command.split("\\s+");
        int count = 1;
        int period = -1;

        try {
            count = Integer.parseInt(split[1]);
            period = Integer.parseInt(split[2]);
        }
        catch (Exception ex) {

        }

        if(period < 1) {
            return new int[] {count};
        }

        return new int[] {count, period};
    }

    private static void message(final Node node, final int[] params) {
        bgTask(() -> {
            Random r = new Random();
            String suffix = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
            do  {
                try {
                    for (int i = 0; i < params[0]; i++) {
                        String prefix = "-";
                        if(r.nextBoolean()) {
                            prefix = "+";
                        }
                        node.send(prefix + i + suffix);
                    }
                    Thread.sleep(params[1] * 1000);
                } catch (Exception ex) {

                }
            }
            while (messagesActive && params.length > 1);
        });
    }

    private static void bgTask(Runnable runnable) {
        new Thread(runnable).start();
    }
}
