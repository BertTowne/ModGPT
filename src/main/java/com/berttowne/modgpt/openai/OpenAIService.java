package com.berttowne.modgpt.openai;

import com.berttowne.modgpt.config.ConfigService;
import com.berttowne.modgpt.injection.Service;
import com.berttowne.modgpt.openai.api.ModerationRequest;
import com.berttowne.modgpt.openai.api.ModerationResponse;
import com.google.auto.service.AutoService;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Singleton
@AutoService(Service.class)
public class OpenAIService implements Service {

    private static final String MODERATION_ENDPOINT = "https://api.openai.com/v1/moderations";
    private static final String MODERATION_MODEL = "omni-moderation-latest";

    private final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final Gson gson;
    private final ConfigService configService;

    @Inject
    public OpenAIService(final Gson gson, final ConfigService configService) {
        this.configService = configService;
        this.gson = gson;
    }

    /**
     * Sends a moderation request to OpenAI's API
     *
     * @param message The message to moderate
     * @return The moderation response
     * @throws IOException If there's an error communicating with the API
     * @throws InterruptedException If the request is interrupted
     */
    public ModerationResponse moderateMessage(final String message) throws IOException, InterruptedException {
        final String apiKey = configService.getOpenAIKey();

        final ModerationRequest request = new ModerationRequest(message, MODERATION_MODEL);
        final String requestBody = gson.toJson(request);

        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(MODERATION_ENDPOINT))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("API request failed with status code: " + response.statusCode());
        }

        return gson.fromJson(response.body(), ModerationResponse.class);
    }

}