package com.dai.network;

public final class DAINetwork {
    private static boolean isServer;

    public DAINetwork(boolean isServer) {
        DAINetwork.isServer = isServer;
    }

    public static boolean isServer() { return isServer; }
}
