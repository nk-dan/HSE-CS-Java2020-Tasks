package ru.hse.cs.java2020.task03;

public class Commentary {
    private String author;
    private String comment;

    public Commentary(String author, String text) {
        this.author = author;
        this.comment = text;
    }


    public String getAuthor() {
        return author;

    }

    public String getComment() {
        return comment;

    }
}

