package master.utils;

import master.exceptions.FailedRequestException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class RequestHelper {
    private static HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.of(10, ChronoUnit.SECONDS))
            .build();

    public static byte[] sendGetRequest(String url) {
        try {
            HttpRequest request = getRequestBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() >= 300)
                throw new FailedRequestException("Invalid response from server: [" + response.statusCode() + "] " + new String(response.body()));
            return response.body();
        } catch (InterruptedException | IOException e) {
            throw new FailedRequestException("Can't send request to url " + url + ", [" + e.getClass() + "] " + e.getMessage(), e);
        }
    }

    public static String sendGetRequestAsString(String url) {
        return new String(sendGetRequest(url));
    }

    public static String sendPostRequest(String url, String payload) {
        try {
            HttpRequest request = getRequestBuilder().POST(HttpRequest.BodyPublishers.ofString(payload)).uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300)
                throw new FailedRequestException("Invalid response from server: [" + response.statusCode() + "] " + response.body());
            return response.body();
        } catch (Exception e) {
            throw new FailedRequestException("Can't send request to url " + url + ", [" + e.getClass() + "] " + e.getMessage(), e);
        }
    }

    private static HttpRequest.Builder getRequestBuilder() {
        return HttpRequest.newBuilder().timeout(Duration.of(10, ChronoUnit.SECONDS)).header("content-type", "application/json");
    }
}
