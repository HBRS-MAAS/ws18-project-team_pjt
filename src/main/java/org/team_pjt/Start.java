package org.team_pjt;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class Start {
	private static boolean isHost;
	private static String host;
	private static String port;
    private static String path;

    public static void main(String[] args) {
    	if (!decodeArguments(args)) {
    		return;
		}
    	List<String> agents = new Vector<>();
    	if(isHost) {
            agents.add("clock:org.team_pjt.agents.SystemClockAgent");
            agents.add("bakery1:org.team_pjt.agents.SystemClockAgent");
            agents.add("c1:org.team_pjt.agents.CustomerAgent");
            agents.add("c2:org.team_pjt.agents.CustomerAgent");
        }
        else {
            agents.add("bakery2:org.team_pjt.agents.SystemClockAgent");
            agents.add("c3:org.team_pjt.agents.CustomerAgent");
            agents.add("c4:org.team_pjt.agents.CustomerAgent");
        }

    	String scenario = readScenarioFile();
    	List<String> cmd = buildCMD(agents, new JSONObject(scenario));
    	System.out.println(cmd.toString());
        System.out.println(cmd.size());
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));
    }

    public static List<String> buildCMD(List<String> agents, JSONObject scenario) {
    	StringBuilder sb = new StringBuilder();
        List<String> cmd = new Vector<>();
        JSONArray customers = scenario.getJSONArray("customers");
        JSONArray bakeries = scenario.getJSONArray("bakeries");
        Iterator<Object> customerIterator = customers.iterator();
        Iterator<Object> bakeryIterator = bakeries.iterator();
    	if(isHost) {

		}
		else {
    	    cmd.add("-container");
            cmd.add("-host");
            cmd.add(host);
            cmd.add("-port");
            cmd.add(port);
		}
        cmd.add("-agents");
		for (String a : agents) {
			sb.append(a);
			if(a.contains("Customer")) {
                sb.append("(");
                sb.append(((JSONObject)customerIterator.next()).toString());
                sb.append(")");
            }
            if(a.contains("Order")) {
                sb.append("(");
                sb.append(((JSONObject)bakeryIterator.next()).toString());
                sb.append(")");
            }
			sb.append(";");
		}
		cmd.add(sb.toString());

    	return cmd;
	}

	public static String readScenarioFile() {
    	String jsonString = null;
		try {
		    StringBuilder sb = new StringBuilder();
		    String line = null;
            FileReader fileReader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
			while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            jsonString = sb.toString();
		}
		catch(IOException e) {
            e.printStackTrace();
            System.out.println("Error reading scenario file!");
		}
		return jsonString;
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
            if (args[i].equals("-path")) {
                path = args[i+1];
                ++i;
            }
			if (args[i].equals("-h")) {
				// TODO: implement help output
				System.out.println("");
			}
		}
		if (!isHost && (port == null || host == null)) {
			System.out.println("instance is not host and host and port have to be specified!");
			return false;
		}
		return true;
	}
}
