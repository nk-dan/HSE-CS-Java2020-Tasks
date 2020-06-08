package ru.hse.cs.java2020.task03;

import java.util.ArrayList;
import java.util.Optional;

public class TaskDetails {
    private String name;
    private String description;
    private String author;
    private Optional<String> executor;
    private ArrayList<String> observers;
    private ArrayList<Commentary> comments;

    TaskDetails() {
        observers = new ArrayList<>();
        comments = new ArrayList<>();
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    void addComment(Commentary info) {
        comments.add(info);
    }

    void addObserver(String observers) {
        this.observers.add(observers);
    }

    public ArrayList<String> getObservers() {
        return observers;
    }

    public ArrayList<Commentary> getComments() {
        return comments;
    }

    public Optional<String> getExecutor() {
        return executor;
    }

    public void setExecutor(String newAssignedTo) {
        this.executor = Optional.ofNullable(newAssignedTo);
    }

}