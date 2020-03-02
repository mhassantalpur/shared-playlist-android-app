package com.kassette.wifip2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Web sources used.
 *
 * http://www.byteslounge.com/tutorials/java-custom-serialization-example
 */
public class InfoHolder implements Serializable {

    private static final long serialVersionUID = -2518143671167959230L;
    public static final int KEY_SYNC_SONG_ADAPTER = 0;
    public static final int KEY_SPOTIFY_SONG_ADDED = 1;
    public static final int KEY_SONG_FINISHED = 2;
    public static final int KEY_REGISTER_WITH_GROUP_OWNER= 3;
    public static final int KEY_CLEAR = 4;
    public static final int KEY_START_TIMMER = 5;
    public static final int KEY_PAUSE_TIMMER = 6;
    public static final int KEY_DOWN_VOTE = 7;

    private int key;
    private String value;

    public InfoHolder(int key, String value) {
        this.key = key;
        this.value = value;
        validate();
    }

    private void writeObject(ObjectOutputStream o) throws IOException {
        o.writeObject(key);
        o.writeObject(value);
    }

    private void readObject(ObjectInputStream o) throws IOException, ClassNotFoundException {
        key = (int) o.readObject();
        value = (String) o.readObject();
        validate();
    }

    private void validate() {
        if (key < 0 ||
                value == null ||
                value.length() == 0) {

            throw new IllegalArgumentException();
        }
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

}
