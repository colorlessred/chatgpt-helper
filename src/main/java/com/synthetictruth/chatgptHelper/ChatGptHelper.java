package com.synthetictruth.chatgptHelper;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.gson.Gson;
import com.synthetictruth.chatgptHelper.ChatGptCompletion.Choice;
import com.synthetictruth.chatgptHelper.MdCardWriter.Card;
import lombok.extern.java.Log;
import okhttp3.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Log
public class ChatGptHelper {

    private final String TYPE_CARD_PREFIX = //
            """
                    create multiple anki cards (removing the eventual cloze).
                    The back card should have minimal information that allows for clear and concise answers.
                    Don't add information on how to create Anki cards in general.
                    Use a --- separator to separate the cards.
                    Here's the content to be turned into cards:

                    """;
    private final String KEY_ENV_VARIABLE = "OPENAI_API_KEY";

    // from https://platform.openai.com/docs/quickstart?context=curl
    private final String OPEN_AI_API_URL = "https://api.openai.com/v1/chat/completions";

    // see models and pricing: https://openai.com/pricing

    private final int MAX_TOKENS = 1000;
    private final String MODEL_GPT4 = "gpt-4-turbo-preview";
    private final String MODEL_GPT3 = "gpt-3.5-turbo-0125";
    private final String MODEL = MODEL_GPT4;

    private final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    @Parameter(names = {"-t", "--type"})
    private String type;

    @Parameter(names = {"-p", "--prompt"})
    private String inputPrompt;

    @Parameter(names = {"-i", "--inputFile"})
    private String inputFile;

    @Parameter(names = {"-o", "--outputFile"})
    private String outputFile;

    @Parameter(names = {"-f", "--outputFolder"})
    private String outputFolder;

    public static void main(String[] args) {
        ChatGptHelper chatGptHelper = new ChatGptHelper();
        JCommander.newBuilder().addObject(chatGptHelper).build().parse(args);
        try {
            validateConfig(chatGptHelper);
            chatGptHelper.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void validateConfig(ChatGptHelper chatGptHelper) {
        if (chatGptHelper.inputPrompt == null && chatGptHelper.inputFile == null) {
            throw new ParameterException("must specify at least one of --prompt (-p) or --inputFile (-i)");
        }

        if (chatGptHelper.outputFile == null) {
            throw new ParameterException("must specify the --outputFile (-o)");
        }

        if (chatGptHelper.outputFolder == null) {
            throw new ParameterException("must specify the --outputFolder (-f)");
        }
    }

    private void println(String message) {
        log.info(message);
    }

    private void println(String format, String... values) {
        log.info(String.format(format, values));
    }

    private void run() throws Exception {
        OkHttpClient client = new OkHttpClient();

        String apiKey = System.getenv(KEY_ENV_VARIABLE);
        MediaType mediaType = MediaType.parse("application/json");
        String prompt;

        if (inputPrompt != null) {
            prompt = inputPrompt;
        } else if (inputFile != null) {
            prompt = Files.readString(Path.of(inputFile));
        } else {
            prompt = "";
        }

        prompt = String.format("%s %s", TYPE_CARD_PREFIX, prompt);

        System.out.printf("Prompt: %s\n", prompt);

        ChatGptRequest chatGptRequest = ChatGptRequest.builder()
                .model(MODEL)
                .temperature(0.5)
                .max_tokens(MAX_TOKENS)
                .message(new ChatGptRequest.UserMessage(prompt))
                .build();
        Gson gson = new Gson();

        RequestBody body = RequestBody.create(gson.toJson(chatGptRequest), mediaType);

        Request request = new Request.Builder()
                .url(OPEN_AI_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        println("Calling the ChatGTP API");
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println(responseBody);
            ChatGptCompletion val = ChatGptCompletion.fromString(responseBody);
            Optional<Choice> reply = val.getChoices().stream().findFirst();
            if (reply.isPresent()) {
                processResponse(reply.get().getMessage().content);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processResponse(String response) throws IOException {
        // write out the file with the multiple answers
        Files.write(Path.of(outputFile),
                response.getBytes(),
                StandardOpenOption.CREATE);
        println("output written to file %s", outputFile);

        // split the response into multiple files
        AtomicInteger index = new AtomicInteger(1);
        String timestamp = TIMESTAMP_FORMAT.format(new Date());
        List<Card> cards = MdCardWriter.parseSource(response);
        for (Card card : cards) {
            if (card.isParsedCorrectly()) {
                Path path = Path.of(outputFolder).resolve(String.format("%s_%d.md", timestamp, index.getAndIncrement()));
                log.info(String.format("Writing card '%s' to path %s", card.getFront(), path.toAbsolutePath()));
                println("Writing card '%s' to path %s", card.getFront(), path.toAbsolutePath().toString());
                Files.write(path, card.getContent().getBytes(), StandardOpenOption.CREATE_NEW);
            }
        }
    }
}