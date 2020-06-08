package ru.hse.cs.java2020.task03;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Optional;

public class TrackerClient {

    private static final int NOT_FOUND_ERROR = 404;
    private static final int FORBIDDEN = 403;
    private static final int UNAUTHORIZED_ERROR = 401;
    private static final int CREATED_CODE = 201;
    private static final int SUCCESS_CODE = 200;
    private static TrackerClient cl = null;
    private final HttpClient client;

    private TrackerClient() {
        client = HttpClient.newHttpClient();
    }

    public static TrackerClient getTrackerClient() {
        if (cl == null) {
            cl = new TrackerClient();
        }
        return cl;
    }

    Optional<String> createTask(String oauthToken, String orgID, String name, String description,
                                Optional<String> user, String queueID) {
        JSONObject requestJSON = new JSONObject();
        requestJSON.put("summary", name);
        requestJSON.put("description", description);
        requestJSON.put("queue", queueID);
        user.ifPresent(s -> requestJSON.put("assignee", s));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tracker.yandex.net/v2/issues?"))
                .headers("Content-type", "application/json" ,"Authorization", "OAuth " + oauthToken, "X-Org-Id", orgID)
                .POST(HttpRequest.BodyPublishers.ofString(requestJSON.toString()))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.err.println(response.statusCode() + " " + oauthToken);
            if (response.statusCode() == CREATED_CODE) {
                JSONObject obj = new JSONObject(response.body());
                return Optional.of(obj.getString("key"));
            } else {
                System.err.println(response.body());
                return Optional.empty();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error");
            return Optional.empty();
        }
    }

    ArrayList<String> findMyTasks(String oauthToken, String orgID, String user, String pageNum) throws ClientErr {
        JSONObject request = new JSONObject();
        request.put("filter", new JSONObject().put("assignee", user));
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tracker.yandex.net/v2/issues/_search?order=+updatedAt&perPage=5&page="
                        + pageNum))
                .headers("Authorization", "OAuth " + oauthToken, "X-Org-Id", orgID)
                .POST(HttpRequest.BodyPublishers.ofString(request.toString()))
                .build();
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != SUCCESS_CODE) {
                System.err.println(response.body());
            }
            JSONArray responseJSON = new JSONArray(response.body());
            ArrayList<String> result = new ArrayList<>();
            for (int i = 0; i != responseJSON.length(); ++i) {
                result.add(responseJSON.getJSONObject(i).getString("key"));
            }
            return result;
        } catch (IOException | InterruptedException exc) {
            System.err.println(exc.getMessage());
            throw new ClientErr(exc.getMessage());
        }
    }

    TaskDetails getTaskDetail(String oauthToken, String orgID, String task)
            throws java.io.IOException, java.lang.InterruptedException, AuthorizeErr, ClientErr {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tracker.yandex.net/v2/issues/" + task))
                .headers("Authorization", "OAuth " + oauthToken, "X-Org-Id", orgID)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        long status = response.statusCode();
        if (status == UNAUTHORIZED_ERROR || status == FORBIDDEN) {
            throw new AuthorizeErr(response.body());
        } else if (status == NOT_FOUND_ERROR) {
            throw new ClientErr(response.body());
        }

        TaskDetails res = new TaskDetails();
        JSONObject obj = new JSONObject(response.body());
        res.setName(obj.getString("key"));
        res.setDescription(obj.getString("summary"));
        if (obj.has("assignee")) {
            res.setExecutor(obj.getJSONObject("assignee").getString("display"));
        } else {
            res.setExecutor(null);
        }
        res.setAuthor(obj.getJSONObject("createdBy").getString("display"));
        if (obj.has("followers")) {
            JSONArray followers = obj.getJSONArray("followers");
            for (int i = 0; i != followers.length(); ++i) {
                res.addObserver(followers.getJSONObject(i).getString("display"));
            }
        }
        HttpRequest commentRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tracker.yandex.net/v2/issues/" + task + "/comments"))
                .headers("Authorization", "OAuth " + oauthToken, "X-Org-Id", orgID)
                .GET()
                .build();

        response = client.send(commentRequest, HttpResponse.BodyHandlers.ofString());
        JSONArray comments = new JSONArray(response.body());
        for (int i = 0; i != comments.length(); ++i) {
            JSONObject comment = comments.getJSONObject(i);
            res.addComment(new Commentary(comment.getJSONObject("createdBy").getString("display"),
                    comment.getString("text")));
        }
        return res;
    }

    ArrayList<QueueDetail> getQueues(String oauthToken, String orgID)
            throws java.io.IOException, java.lang.InterruptedException, ClientErr {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tracker.yandex.net/v2/queues?"))
                .headers("Authorization", "OAuth " + oauthToken, "X-Org-Id", orgID)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == NOT_FOUND_ERROR) {
            throw new ClientErr(response.body());
        }
        ArrayList<QueueDetail> result = new ArrayList<>();
        JSONArray queues = new JSONArray(response.body());
        for (int i = 0; i != queues.length(); ++i) {
            JSONObject elem = queues.getJSONObject(i);
            result.add(new QueueDetail(elem.getString("key"), elem.getInt("id")));
        }
        return result;
    }
}
