package ru.hse.cs.java2020.task03;

public class QueueDetail {
    private String key;
    private long id;

    public QueueDetail(String newKey, long newId) {
        this.key = newKey;
        this.id = newId;
    }

    public String getKey() {
        return key;
    }

    public long getId() {
        return id;
    }
}
