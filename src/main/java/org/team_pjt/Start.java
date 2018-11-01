package org.team_pjt;

import java.util.List;
import java.util.Vector;

public class Start {
	private static boolean isHost;
	private static String host;
	private static String port;

    public static void main(String[] args) {
    	if (!decodeArguments(args)) {
    		return;
		}
    	List<String> agents = new Vector<>();
    	agents.add("test:org.team_pjt.agents.SystemClockAgent");

    	List<String> cmd = new Vector<>();
    	cmd.add("-agents");
    	StringBuilder sb = new StringBuilder();
    	for (String a : agents) {
    		sb.append(a);
    		sb.append(";");
    	}
    	cmd.add(sb.toString());
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));
    }

    private static boolean decodeArguments(String[] args) {
    	for (int i = 0; i < args.length; ++i) {
    		if (args[i].equals("isHost")) {
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
			if (args[i].equals("-h")) {
				// TODO: implement help output
				System.out.println("");
			}
		}
		if (!isHost && (port == null || host == null)) {
			System.out.println("if instance id not host, host and port have to be specified!");
			return false;
		}
		return true;
	}
}
