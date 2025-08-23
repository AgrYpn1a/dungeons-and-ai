package com.dai.server;

public enum EDAIProtocol {
    None(0),
    Alive(1),
    Connect(2),
    Connected(3),
    SpawnPlayer(4),
    SpawnEnemy(5),
    PlayerData(6);

    public final byte value;

    private EDAIProtocol(int value) {
        this.value = (byte) value;
    }

    public static EDAIProtocol fromByte(byte b) {
        for(EDAIProtocol value : values()) {
            if(value.value == b) {
                return value;
            }
        }

        return EDAIProtocol.None;
     }
}
