package ru.hse.cs.java2020.task03;

public class ClientErr extends Throwable {
    public ClientErr(String message) {
        super(message);
    }
}
