package org.team_pjt.utils;

public class Logger {
    private String agent_name;
    private String logging_level;

    public Logger(String agent_name, String logging_level) {
        this.agent_name = agent_name;
        this.logging_level = logging_level;
    }

    public Logger(String agent_name) {
        this.agent_name = agent_name;
        this.logging_level = "debug";
    }

    public void log(LogMessage msg) {
        String msgLogLevel = msg.logging_level;
        switch (logging_level) {
            case "debug":
                System.out.println("[" + logging_level.toUpperCase() + "]" + agent_name + " - " + msg.message);
                break;
            case "release":
                if(msgLogLevel.equals("release")) {
                    System.out.println("[" + logging_level.toUpperCase() + "]" + agent_name + " - " + msg.message);
                }
                break;
            default:
                if(logging_level.equals(msgLogLevel)) {
                    System.out.println("[" + logging_level.toUpperCase() + "]" + agent_name + " - " + msg.message);
                }
                break;
        }
    }

    public static class LogMessage {
        public String message;
        public String logging_level;

        public LogMessage(String message, String logging_level) {
            this.message = message;
            this.logging_level = logging_level;
        }
    }
}
