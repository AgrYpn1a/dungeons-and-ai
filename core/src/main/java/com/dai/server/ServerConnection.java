package com.dai.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public final class ServerConnection extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ServerConnection.class);
    private ObjectOutputStream out;

    public ServerConnection(ObjectOutputStream out) throws IOException {
        this.out = out;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            // Keeps the connection alive
            // TODO: We need additional logic here
            try {
                Thread.sleep(1000);
				out.writeObject(EDAIProtocol.Alive.name());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
}
