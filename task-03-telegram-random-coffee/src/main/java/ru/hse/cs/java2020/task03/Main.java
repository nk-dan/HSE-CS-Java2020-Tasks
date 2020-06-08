package ru.hse.cs.java2020.task03;

import com.pengrad.telegrambot.TelegramBot;

public class Main {
    public static void main(String[] args) {
        DataBase db = new DataBase("//localhost:5432/postgres");
        TrackerClient trackerClient = TrackerClient.getTrackerClient();
        TelegramBot bot = new TelegramBot("1238312089:AAFcq4-CIX4yc_uemyWBVbdYzTJuWmRyeNY\n");
        Bot trackerBot = new Bot(bot, db, trackerClient);
        Runtime.getRuntime().addShutdownHook(new Thread(db::disconnect));
        trackerBot.run();
    }
}
