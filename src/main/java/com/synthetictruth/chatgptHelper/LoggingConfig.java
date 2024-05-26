package com.synthetictruth.chatgptHelper;

import java.util.logging.*;

public class LoggingConfig {

    // Custom formatter to print only the message text
    static class SimpleFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return record.getMessage() + System.lineSeparator();
        }
    }

    public static void configureLogging() {
        // Get the global logger
        Logger rootLogger = Logger.getLogger("");

        // Remove the default handlers
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // Create a new console handler with the custom formatter
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        consoleHandler.setLevel(Level.INFO);

        // Add the custom handler to the logger
        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(Level.INFO);
    }
}
