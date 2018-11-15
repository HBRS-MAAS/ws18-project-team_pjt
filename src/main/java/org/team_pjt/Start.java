package org.team_pjt;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Start {
	private static boolean isHost;
	private static String host;
	private static String port;
    private static String path;
    private static JSONArray jsaBakeries;
    private static JSONArray jsaTruck;
    private static JSONArray jsaClients;
    private static final String sOvPrefix = ":org.team_pjt.agents.OvenAgent";
    private static final String sSchPrefix = ":org.team_pjt.agents.SchedulerAgent";
    private static final String sTrPrefix = ":org.team_pjt.agents.TruckAgent";
    private static List<String> agents = new Vector<>();
    public static void main(String[] args) {
        String sNewPath = "src/main/resources/";
        path = "src/main/resources/archive/random-scenario.json";
    	if (!decodeArguments(args)) {
            isHost = true;
		}

        String scenario = readScenarioFile(path);
    	// new scenarioFile
        readNewScenarioFile(sNewPath);
        // new scenarioFile
        if(scenario == null) {
            System.exit(-1);
        }
        JSONObject json_scenario = new JSONObject(scenario);

        JSONObject meta_data = json_scenario.getJSONObject("meta");

        int duration_days = meta_data.getInt("duration_days");

//    	List<String> agents = new Vector<>();
    	if(isHost) {
            JSONArray bakeries = json_scenario.getJSONArray("bakeries");
            Iterator<Object> bakery_iterator = bakeries.iterator();
            while (bakery_iterator.hasNext()) {
                JSONObject bakery = (JSONObject) bakery_iterator.next();
                String id = bakery.getString("guid");
                agents.add(id + ":org.team_pjt.agents.OrderProcessing");
            }
            agents.add("c1:org.team_pjt.agents.SystemClockAgent");

        }
        else {
            JSONArray orders = json_scenario.getJSONArray("orders");
            Iterator<Object> order_iterator = orders.iterator();
            while (order_iterator.hasNext()) {
                JSONObject order = (JSONObject) order_iterator.next();
                String id = order.getString("guid");
                agents.add(id + ":org.team_pjt.agents.OrderAgent");
            }
        }
    	List<String> cmd = buildCMD(agents, json_scenario);
//    	  System.out.println(cmd.toString());
//        System.out.println(cmd.size());
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));

    }

    private static void readNewScenarioFile(String sDirectory) {
        File fDir = new File(sDirectory);
        File[] directoryListing = fDir.listFiles();
        if (directoryListing != null) {
            for (File fChild : directoryListing) {
                if(fChild.isFile()){
                    String sReadFile = readScenarioFile(fChild.getAbsolutePath());
                    if(fChild.getName().contains("bakeries")){
                        jsaBakeries = prepareOvenandBakery(sReadFile);
                    }
                    if(fChild.getName().contains("delivery")){
                        jsaTruck = prepareTruck(sReadFile);
                    }
                    if(fChild.getName().contains("clients")){
//                        jsaClients =
                    }
                }
            }
        }
//        return sbBuilder.toString();
    }

    private static JSONArray prepareTruck(String sReadFile) {
        JSONArray joObjectScenario = new JSONArray(sReadFile);
        Iterator<Object> iDeliveryIterator = joObjectScenario.iterator();
        while(iDeliveryIterator.hasNext()){
            JSONObject joDelivery = (JSONObject) iDeliveryIterator.next();
            JSONArray jaTrucks = joDelivery.getJSONArray("trucks");
            Iterator<Object> iIteratorTruckArray= jaTrucks.iterator();
            while(iIteratorTruckArray.hasNext()){
                JSONObject joTruckObject = (JSONObject) iIteratorTruckArray.next();
                agents.add(joTruckObject.get("guid")+sTrPrefix);
            }
        }
        return joObjectScenario;
    }

//    private static JSONArray prepareClients(String sReadFile) {
//        JSONArray joObjectScenario = new JSONArray(sReadFile);
//        Iterator<Object> iCustomerIterator = joObjectScenario.iterator();
//        while(iCustomerIterator.hasNext()){
//            JSONObject jsoCustomer = (JSONObject) iCustomerIterator.next();
//            agents.add(jsoCustomer.get("guid")+":org.team_pjt.agents.OrderProcessing");
//        }
//    }

    private static JSONArray prepareOvenandBakery(String sReadFile) {
        JSONArray joObjectScenario = new JSONArray(sReadFile);
        Iterator<Object> iBakeryIterator = joObjectScenario.iterator();
        while(iBakeryIterator.hasNext()){
                JSONObject joBakery = (JSONObject) iBakeryIterator.next();
                String sGuid = joBakery.getString("guid");
                agents.add(sGuid+sSchPrefix);
                JSONObject joEquipment = joBakery.getJSONObject("equipment");
                JSONArray jaOvens = joEquipment.getJSONArray("ovens");
                Iterator<Object> iJaOveniterator = jaOvens.iterator();
                while(iJaOveniterator.hasNext()){
                        JSONObject joOven = (JSONObject) iJaOveniterator.next();
                        agents.add(sGuid+"#"+joOven.get("guid")+"#"+sOvPrefix);
                }
        }
        return joObjectScenario;
    }

    ;

    public static List<String> buildCMD(List<String> agents, JSONObject scenario) {
    	StringBuilder sb = new StringBuilder();
        List<String> cmd = new Vector<>();
        JSONArray json_customers = scenario.getJSONArray("customers");
        JSONArray json_bakeries = scenario.getJSONArray("bakeries");
        Iterator<Object> customer_iterator = json_customers.iterator();
        Iterator<Object> bakery_iterator = json_bakeries.iterator();
        Iterator<Object> order_iterator = scenario.getJSONArray("orders").iterator();

        Hashtable<String, JSONObject> htCustomers = new Hashtable<>();
        while(customer_iterator.hasNext()) {
            JSONObject json_cust = (JSONObject) customer_iterator.next();
            htCustomers.put(json_cust.getString("guid"), json_cust);
        }

    	if(isHost) {
            cmd.add("-local-port");
            cmd.add("8080");
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
            if(isHost){
                //new scenarion
                if(a.contains("Scheduler")|| a.contains("Oven")){
                    sb.append(a);
                    sb.append("(");
                    sb.append(jsaBakeries.toString().replaceAll(",", "###"));
                    sb.append(")");
                    sb.append(";");
                    continue;
                }
                if(a.contains("Truck")){
                    sb.append(a);
                    sb.append("(");
                    sb.append(jsaTruck.toString().replaceAll(",", "###"));
                    sb.append(")");
                    sb.append(";");
                    continue;
                }
                //new scenarion
            }
			sb.append(a);
			if(a.contains("OrderAgent")) {
                sb.append("(");
                JSONObject order = (JSONObject)order_iterator.next();
                sb.append(order.toString().replaceAll(",", "###"));
                sb.append(",");
                JSONObject customer = htCustomers.get(order.getString("customer_id"));
                sb.append(customer.toString().replaceAll(",", "###"));
                sb.append(")");
            }
            if(a.contains("OrderProcessing")) {
                sb.append("(");
                sb.append(((JSONObject)bakery_iterator.next()).toString().replaceAll(",", "###"));
                sb.append(")");

            }

			sb.append(";");
		}

        cmd.add(sb.toString());

    	return cmd;
	}

    private static void parsingBakeryId(StringBuilder sb, JSONObject joObject) {
//        if (joObject.get("guid") instanceof JSONObject) {
            sb.append(joObject.get("guid"));
//        }
        sb.append("#");
    }

	public static String readScenarioFile(String sPath) {
    	String jsonString = null;
		try {
		    StringBuilder sb = new StringBuilder();
		    String line = null;
            FileReader fileReader = new FileReader(sPath);
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
