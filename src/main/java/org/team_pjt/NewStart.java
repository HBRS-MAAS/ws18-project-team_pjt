package org.team_pjt;

import java.util.List;
import java.util.Vector;

public class NewStart {
    private static boolean isHost = false;
    private static String host = "localhost";
    private static String port = "8133";
    private static String localPort = "8133";

    private static boolean customerStage = false;
    private static boolean orderProcessingStage = false;
    private static boolean doughPrepStage = false;
    private static boolean bakingStage = false;
    private static boolean packagingStage = false;
    private static boolean deliveryStage = false;
    private static boolean visualizationStage = false;

    public static void main(String[] args) {
        if(!decodeArguments(args)) {
            isHost = true;
            System.out.println("No arguments given. Using default arguments!");
        }

        List<String> cmd = buildCMD();
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));
    }

    public static List<String> buildCMD() {
        StringBuilder sb = new StringBuilder();
        List<String> cmd = new Vector<>();

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
        }
        if(orderProcessingStage) {
            Initializer init = new orderProcessingInitializer();
            sb.append(init.initialize());
        }
        if(doughPrepStage) {

        }
        if(bakingStage) {

        }
        if(packagingStage) {

        }
        if(deliveryStage) {

        }
        if(visualizationStage) {

        }
        cmd.add(sb.toString());
        return cmd;
    }

    private static boolean decodeArguments(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-isHost")) {
                isHost = true;
                continue;
            }
            if (args[i].equals("-host")) {
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
            }
            if (args[i].equals("-orderProcessing")) {
                orderProcessingStage = true;
            }
            if (args[i].equals("-doughPrep")) {
                doughPrepStage = true;
            }
            if (args[i].equals("-baking")) {
                bakingStage = true;
            }
            if (args[i].equals("-packaging")) {
                packagingStage = true;
            }
            if (args[i].equals("-delivery")) {
                deliveryStage = true;
            }
            if (args[i].equals("-visualization")) {
                visualizationStage = true;
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
