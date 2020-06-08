package ru.hse.cs.java2020.task03;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class Bot {

    private final TelegramBot bot;
    private final DataBase db;
    private final TrackerClient client;

    public Bot(TelegramBot tgBot, DataBase usersDb, TrackerClient trackerClient) {
        bot = tgBot;
        db = usersDb;
        client = trackerClient;
    }

    public void processUpdate(long chatId, String[] request) {
        if (request[0].equals(("/help"))) {
            sendHelp(chatId);
            return;
        }
        switch (request[0]) {
            case "/start":
                authorize(chatId, request);
                break;
            case "/createTask":
                createTask(chatId, request);
                break;
            case "/getTask":
                getTask(chatId, request);
                break;
            case "/getMyTasks":
                getMyTasks(chatId, request);
                break;
            case "/getQueues":
                processGetQueues(chatId);
                break;
            default:
                sendHelp(chatId);
                break;
        }
    }

    public void run() {
        db.connect();
        bot.setUpdatesListener(updates -> {
            for (var update : updates) {
                if (update.message() != null) {
                    long chatId = update.message().chat().id();
                    String body = update.message().text();
                    String[] request = body.split("#");
                    processUpdate(chatId, request);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, e -> {
            if (e.response() != null) {
                // god bad response from telegram
                e.response().errorCode();
                e.response().description();
            } else {
                // probably network error
                e.printStackTrace();
            }
        });
    }

    public void authorize(long chatId, String[] request) {
        if (request.length != 4) {
            sendMessage(chatId, "Invalid format. Look at /help");
        } else {
            db.insert(chatId, new UserData(request[1], request[2], request[3]));
            sendMessage(chatId, "Saved");
        }
    }

    private void sendMessage(long chatId, String text) {
        bot.execute(new SendMessage(chatId, text));
    }

    public void getTask(long chatId, String[] request) {
        Optional<UserData> userInfo = db.get(chatId);
        if (userInfo.isEmpty()) {
            authError(chatId);
            return;
        }
        if (request.length != 2) {
            sendHelp(chatId);
        } else {
            try {
                TaskDetails task = client.getTaskDetail(userInfo.get().getToken(), userInfo.get().getOrg(), request[1]);
                sendMessage(chatId, "Task: " + task.getName()
                        + "\nDescription: " + task.getDescription() + "\nAuthor: " + task.getAuthor());
                Optional<String> assignedTo = task.getExecutor();
                assignedTo.ifPresent(s -> sendMessage(chatId, "Assigned to: " + s));
                ArrayList<String> observers = task.getObservers();
                if (observers.size() > 0) {
                    sendMessage(chatId, "Observer list:");
                    for (var obs : observers) {
                        sendMessage(chatId, obs);
                    }
                }
                ArrayList<Commentary> comments = task.getComments();
                if (comments.size() > 0) {
                    sendMessage(chatId, "comments:");
                    for (var comment : comments) {
                        sendMessage(chatId, comment.getAuthor() + " : " + comment.getComment());
                    }
                }
            } catch (IOException | InterruptedException exc) {
                sendMessage(chatId, "Error");
            } catch (AuthorizeErr exc) {
                sendMessage(chatId, "Not authorized");
            } catch (ClientErr exc) {
                sendMessage(chatId, "Task not found");
            }
        }
    }

    public void createTask(long chatId, String[] request) {
        Optional<UserData> userInfo = db.get(chatId);
        if (userInfo.isEmpty()) {
            authError(chatId);
            return;
        }
        if (request.length < 4) {
            sendHelp(chatId);
        } else {
            Optional<String> created;
            if (request.length == 4) {
                created = client.createTask(userInfo.get().getToken(), userInfo.get().getOrg(), request[1],
                        request[2], Optional.empty(), request[3]);
            } else {
                created = client.createTask(userInfo.get().getToken(), userInfo.get().getOrg(), request[1],
                        request[2], Optional.of(request[4]), request[3]);
            }
            if (created.isPresent()) {
                sendMessage(chatId, "Task created " + created.get());
            } else {
                sendMessage(chatId, "Error");
            }
        }
    }

    public void getMyTasks(long chatId, String[] request) {
        var userInfo = db.get(chatId);
        if (userInfo.isEmpty()) {
            authError(chatId);
            return;
        }
        try {
            String pageNum = "1";
            if (request.length > 1) {
                pageNum = request[1];
            }
            ArrayList<String> tasks = client.findMyTasks(userInfo.get().getToken(), userInfo.get().getOrg(),
                    userInfo.get().getLogin(), pageNum);

            for (int i = 0; i != tasks.size(); ++i) {
                sendMessage(chatId, tasks.get(i));
            }
        } catch (ClientErr exc) {
            sendMessage(chatId, "Error");
            System.err.println(exc.getMessage());
        }
    }

    void authError(long chatId) {
        sendMessage(chatId, "Not authorized");
    }

    public void processGetQueues(long chatId) {
        try {
            Optional<UserData> userInfo = db.get(chatId);
            if (userInfo.isEmpty()) {
                authError(chatId);
                return;
            }
            ArrayList<QueueDetail> queues = client.getQueues(userInfo.get().getToken(), userInfo.get().getOrg());
            sendMessage(chatId, "queues:");
            for (var queue : queues) {
                sendMessage(chatId, "name: " + queue.getKey() + " id: " + queue.getId());
            }
        } catch (IOException | InterruptedException | ClientErr exc) {
            sendMessage(chatId, "Error");
        }
    }

    public void sendHelp(long chatId) {
        sendMessage(chatId, "You can interact with Yandex.Tracker (https://yandex.ru/tracker/) using this bot."
                + "To get oAuth token visit https://yandex.ru/dev/connect/tracker/ .\n\n"
                + "List of methods:\n"
                + "authorization - /start <oAuth token> # <X-Org-Id> # <login>\n"
                + "adding new task - /createTask <name> # <description> # <queueId>\n"
                + "get task - /getTask <TaskID>\n"
                + "get queue - /getQueues\n"
                + "get all tasks - /getMyTasks\n"
                + "Use # sign to separate every parameter!\n");
    }

}
