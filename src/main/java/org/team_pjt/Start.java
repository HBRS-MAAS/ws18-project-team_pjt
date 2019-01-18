package org.team_pjt;

import java.util.List;
import java.util.Vector;

public class Start {
    private static boolean isHost = true;
    private static String host = "localhost";
    private static String port = "8133";
    private static String localPort = "8133";

    private static boolean customerStage = true;
    private static boolean orderProcessingStage = true;
    private static boolean doughPrepStage = true;
    private static boolean bakingStage = false;
    private static boolean packagingStage = false;
    private static boolean deliveryStage = false;
    private static boolean visualizationStage = false;
    private static boolean noAgentStarting = true;

    private static String endTime = "030.03.00";
    private static String scenarioDirectory = "small";

    public static void main(String[] args) {
        if(!decodeArguments(args)) {
            System.out.println("No arguments given. Using default arguments!");
        }

        List<String> cmd = buildCMD();
        jade.Boot.main(cmd.toArray(new String[0]));
    }

    private static List<String> buildCMD() {
        StringBuilder sb = new StringBuilder();
        List<String> cmd = new Vector<>();

        cmd.add("-jade_domain_df_maxresult");
        cmd.add("10000");

        if(isHost) {
            cmd.add("-local-port");
            cmd.add(localPort);
        }
        else {
            cmd.add("-container");
            cmd.add("-host");
            cmd.add(host);
            cmd.add("-port");
            cmd.add(port);
        }
        cmd.add("-agents");

        if(customerStage) {
            Initializer init = new CustomerInitializer();
            sb.append(init.initialize(scenarioDirectory));
        }
        if(orderProcessingStage) {
            Initializer init = new OrderProcessingInitializer();
            sb.append(init.initialize(scenarioDirectory));
        }
        if(doughPrepStage) {
            Initializer init = new DoughPrepInitializer();
            sb.append(init.initialize(scenarioDirectory));
        }
        if(bakingStage) {
        }
        if(packagingStage) {
        }
        if(deliveryStage) {

        }
        if(visualizationStage) {
        }
        if(isHost) {
            sb.append("timekeeper:org.team_pjt.agents.TimeKeeper(" + scenarioDirectory + ", " + endTime + ");");
            if(noAgentStarting) {
                sb.append("dummy:org.team_pjt.agents.DummyAgent;");
            }
        }
        cmd.add(sb.toString());
        return cmd;
    }

    private static boolean decodeArguments(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-isHost")) {
                isHost = true;
                host = args[i+1];
                ++i;
            }
            if (args[i].equals("-host")) {
                isHost = false;
                host = args[i+1];
                ++i;
            }
            if (args[i].equals("-port")) {
                port = args[i+1];
                ++i;
            }
            if (args[i].equals("-localPort")) {
                localPort = args[i+1];
                ++i;
            }
            if (args[i].equals("-customer")) {
                customerStage = true;
                noAgentStarting = false;
            }
            if (args[i].equals("-orderProcessing")) {
                orderProcessingStage = true;
                noAgentStarting = false;
            }
            if (args[i].equals("-doughPrep")) {
                doughPrepStage = true;
                noAgentStarting = false;
            }
            if (args[i].equals("-baking")) {
                bakingStage = true;
                noAgentStarting = false;
            }
            if (args[i].equals("-packaging")) {
                packagingStage = true;
                noAgentStarting = false;
            }
            if (args[i].equals("-delivery")) {
                deliveryStage = true;
                noAgentStarting = false;
            }
            if (args[i].equals("-visualization")) {
                visualizationStage = true;
                noAgentStarting = false;
            }
            if (args[i].equals("-h")) {
                // TODO: implement help output
                System.out.println();
            }

        }
        if (!isHost && (port == null || host == null)) {
            System.out.println("instance is not host and host and port have to be specified!");
            return false;
        }
        return true;
    }
}