package com.dai.server;

public enum EDAIProtocol {
    None(0),
    Alive(1),
    SpawnPlayer(2),
    SpawnEnemy(3),
    PlayerData(4);

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
