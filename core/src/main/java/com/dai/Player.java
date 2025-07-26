package com.dai;

import java.io.Serializable;

public class Player {

    private static int _id = 0;
    private int id;

    public static class PlayerData implements Serializable {
        public String name;

        // TODO: Other stuff
    }

    private PlayerData data;

    public Player(PlayerData data) {
        this.data = data;
        this.id = _id++;
    }

    public int getId() { return this.id; }

    @Override
    public String toString() {
        // TODO
        return this.data.name;
    }

}
