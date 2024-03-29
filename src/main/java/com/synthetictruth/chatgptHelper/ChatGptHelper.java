package com.synthetictruth.chatgptHelper;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;

public class ChatGptHelper {
    private final String KEY_ENV_VARIABLE = "OPENAI_API_KEY";

    // from https://platform.openai.com/docs/quickstart?context=curl
    private final String OPEN_AI_API_URL = "https://api.openai.com/v1/chat/completions";

    // see models and pricing: https://openai.com/pricing
    private final String MODEL = "gpt-3.5-turbo-0125";

    @Parameter(names = {"-t", "--type"})
    private String type;

    @Parameter(names = {"-p", "--prompt"})
    private String prompt;

    public static void main(String[] args) {
        ChatGptHelper chatGptHelper = new ChatGptHelper();
        JCommander.newBuilder().addObject(chatGptHelper).build().parse(args);
        try {
            chatGptHelper.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void run() throws Exception {
        OkHttpClient client = new OkHttpClient();

        String apiKey = System.getenv(KEY_ENV_VARIABLE);

        MediaType mediaType = MediaType.parse("application/json");
        ChatGptRequest r = ChatGptRequest.builder()
                .model(MODEL)
                .temperature(0.5)
                .max_tokens(50)
                .message(new ChatGptRequest.UserMessage(prompt))
                .build();
        Gson gson = new Gson();

        RequestBody body = RequestBody.create(gson.toJson(r), mediaType);

        Request request = new Request.Builder()
                .url(OPEN_AI_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println(responseBody);
            ChatGptCompletion val = ChatGptCompletion.fromString(responseBody);
            val.getChoices().forEach(choice -> {
                System.out.println(choice.getMessage().content);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}