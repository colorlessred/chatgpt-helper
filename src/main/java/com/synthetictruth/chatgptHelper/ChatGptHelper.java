package com.synthetictruth.chatgptHelper;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.gson.Gson;
import com.synthetictruth.chatgptHelper.ChatGptCompletion.Choice;
import lombok.extern.java.Log;
import okhttp3.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Log
public class ChatGptHelper {

    public static final int MAX_API_TRIES = 5;

    // env variable that must hold the OPENAI API Key
    private static final String KEY_ENV_VARIABLE = "OPENAI_API_KEY";

    // see models and pricing: https://openai.com/pricing
    private enum Models {
        GPT4O("gpt-4o-2024-05-13"),
        GPT4("gpt-4-turbo-preview"),
        GPT3("gpt-3.5-turbo-0125");

        Models(String code) {
            this.code = code;
        }

        final String code;
    }

    private static final int MAX_TOKENS = 4096;

    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private static final Set<String> AVOID_TERMS = Set.of("www", "com", "http", "https");

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
        LoggingConfig.configureLogging();
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

    private void println(final String message) {
        log.info(message);
    }

    private void println(String format, String... values) {
        log.info(String.format(format, (Object) values));
    }

    private void run() throws Exception {
        String inputText;

        if (inputPrompt != null) {
            inputText = inputPrompt;
        } else if (inputFile != null) {
            inputText = Files.readString(Path.of(inputFile));
        } else {
            inputText = "";
        }

        //
        String TYPE_CARD_PREFIX = """
                create multiple anki cards (removing the eventual cloze).
                Each card should identify small and clear items in the content, that is clear and testable. 
                Don't add information on how to create Anki cards in general.
                Prefix the two card sides with "Front:" and "Back:"
                Use a --- separator to separate the cards.
                Here's the content to be turned into cards:

                %s
                """;
        Prompt prompt = new Prompt(inputText, TYPE_CARD_PREFIX);

        System.out.printf("Prompt: %s\n", prompt.getContent());

        String responseText = callChatGpt(prompt.getContent());

        if (responseText != null) {
            processResponse(responseText, prompt.getSource());
        }
    }

    private String callChatGpt(final String inputText) {
        OkHttpClient client = new OkHttpClient.Builder().
                connectTimeout(30, TimeUnit.SECONDS)  // Set the connection timeout
                .readTimeout(30, TimeUnit.SECONDS)     // Set the read timeout
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        String apiKey = System.getenv(KEY_ENV_VARIABLE);

        if (apiKey == null) {
            log.severe("ChatGPT key not found. Use the env variable " + KEY_ENV_VARIABLE);
            throw new RuntimeException("key not found");
        }

        MediaType mediaType = MediaType.parse("application/json");
        String responseText = null;

        ChatGptRequest chatGptRequest = ChatGptRequest.builder()
                .model(Models.GPT4O.code)
                .temperature(0.5)
                .max_tokens(MAX_TOKENS)
                .message(new ChatGptRequest.UserMessage(inputText))
                .build();
        Gson gson = new Gson();

        RequestBody body = RequestBody.create(gson.toJson(chatGptRequest), mediaType);

        // from https://platform.openai.com/docs/quickstart?context=curl
        String OPEN_AI_API_URL = "https://api.openai.com/v1/chat/completions";
        Request request = new Request.Builder()
                .url(OPEN_AI_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        println("Calling the ChatGTP API");
        try {
            int numTry = 0;
            boolean success = false;
            while (numTry < MAX_API_TRIES && !success) {
                numTry++;
                try (Response response = client.newCall(request).execute()) {
                    final ResponseBody rBody = response.body();
                    String responseBody = (rBody != null) ? rBody.string() : "";
                    System.out.println(responseBody);
                    ChatGptCompletion val = ChatGptCompletion.fromString(responseBody);

                    if (val.hasError()) {
                        throw new RuntimeException(String.format("Error: %s", val.getError().getMessage()));
                    } else {
                        List<Choice> choices = val.getChoices();

                        if (choices != null) {
                            Optional<Choice> reply = choices.stream().findFirst();
                            if (reply.isPresent()) {
                                responseText = reply.get().getMessage().content;
                                success = true;
                            }
                        }
                    }
                } catch (SocketTimeoutException e) {
                    log.severe(String.format("timeout! %d out of %d", numTry, MAX_API_TRIES));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception calling the ChatGPT API", e);
        }
        return responseText;
    }

    /**
     * keep only the unique terms to make the tag shorter
     *
     * @param src
     * @return
     */
    public static String computeTag(String src) {
        if (src == null) {
            return "";
        }

        List<String> tokens = Arrays.stream(src.toLowerCase().split("\\W+")).toList();
        Set<String> uniqueTokens = new HashSet<>();
        List<String> cleanTokens = new ArrayList<>();
        for (String token : tokens) {
            if (!AVOID_TERMS.contains(token) && !uniqueTokens.contains(token)) {
                uniqueTokens.add(token);
                cleanTokens.add(token);
            }
        }
        return "#src_" + String.join("_", cleanTokens);
    }

    private void processResponse(String response, String source) throws IOException {
        // write out the file with the multiple answers
        Files.write(Path.of(outputFile),
                List.of(response),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE);
        println("output written to file %s", outputFile);

        // split the response into multiple files
        String sourceTag = computeTag(source);

        AtomicInteger index = new AtomicInteger(1);
        String timestamp = TIMESTAMP_FORMAT.format(new Date());
        List<Card> cards = Card.parseSource(response);
        int num = 0;
        for (Card card : cards) {
            if (card.isParsedCorrectly()) {
                num++;
                Path path = Path.of(outputFolder).resolve(String.format("%s_%d.md", timestamp, index.getAndIncrement()));
                log.info(String.format("%d) Writing card '%s' to path %s", num, card.getFront(), path.toAbsolutePath()));
                String cardContent = String.format("%s\n%s", sourceTag, card.getContent()).trim();
                Files.write(path, List.of(cardContent), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            }
        }
    }
}