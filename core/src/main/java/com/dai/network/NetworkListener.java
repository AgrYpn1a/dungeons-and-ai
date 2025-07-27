package com.dai.network;

import java.io.ObjectInputStream;
import java.util.Queue;

import com.dai.server.EDAIProtocol;

public final class NetworkListener extends Thread {

    public static class NetworkData {
        public EDAIProtocol type;
        public Object data;
    }

    private final ObjectInputStream  in;
    private final Queue<NetworkData> messageQueue;

    public NetworkListener(ObjectInputStream in, Queue<NetworkData> messageQueue) {
        this.in = in;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                EDAIProtocol type = EDAIProtocol.fromByte(in.readByte());

                switch(type) {
                    case SpawnPlayer: {
                        Object data =  in.readObject();
                        NetworkData nData = new NetworkData();
                        nData.type = type;
                        nData.data = data;
                        messageQueue.offer(nData);
                        break;
                    }

                    case SpawnEnemy: {
                        Object data =  in.readObject();
                        NetworkData nData = new NetworkData();
                        nData.type = type;
                        nData.data = data;
                        messageQueue.offer(nData);
                        break;
                    }

                    default: {
                        continue;
                    }
                }
            } catch(Exception e) { /** TODO: Handle exception */ }
        }
    }
}
