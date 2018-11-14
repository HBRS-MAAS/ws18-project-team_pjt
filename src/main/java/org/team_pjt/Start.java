package org.team_pjt;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Start {
	private static boolean isHost;
	private static String host;
	private static String port;
    private static String path;
    private static final String sOvPrefix = ":org.team_pjt.agents.OvenAgent";
    private static final String sSchPrefix = ":org.team_pjt.agents.SchedulerAgent";
    private static final String sTrPrefix = ":org.team_pjt.agents.TruckAgent";

    public static void main(String[] args) {
    	if (!decodeArguments(args)) {
            isHost = true;
            path = "src/main/resources/random-scenario.json";
		}

        String scenario = readScenarioFile();
        if(scenario == null) {
            System.exit(-1);
        }
        JSONObject json_scenario = new JSONObject(scenario);

        JSONObject meta_data = json_scenario.getJSONObject("meta");

        int duration_days = meta_data.getInt("duration_days");

    	List<String> agents = new Vector<>();
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
        if (isHost) {
            for (int i = 0; i< json_bakeries.length(); ++i) {

                JSONObject joObject = null;
                if (json_bakeries.get(i) instanceof JSONObject) {
                    joObject = (JSONObject) json_bakeries.get(i);
                }
                JSONArray jaOvenArray = null;
                // Parsing Ovens
                if (joObject.get("ovens") instanceof JSONArray) {
                    jaOvenArray = (JSONArray) joObject.get("ovens");
                }
                JSONObject jsOvenDetail = null;
                for (int z = 0; z < jaOvenArray.length(); ++z) {
                    if(jaOvenArray != null && jaOvenArray.get(z) instanceof JSONObject){
                        sb.append(joObject.get("guid")+sOvPrefix);
                        sb.append("(");
                        jsOvenDetail = (JSONObject) jaOvenArray.get(z);
                        sb.append(jsOvenDetail.get("cooling_rate").toString());
                        sb.append(",");
                        sb.append(jsOvenDetail.get("guid").toString());
                        sb.append(",");
                        sb.append(jsOvenDetail.get("heating_rate"));
                        sb.append(",");
                        sb.append(joObject.get("guid"));
                        sb.append(")");
                        sb.append(";");
                    }
                }
                // Parsing Ovens
                // Parsing Scheduler
                sb.append(joObject.get("guid")+sSchPrefix);
                sb.append("(");
                // Parsing BakeryId
                parsingBakeryId(sb, joObject);
                // Parsing BakeryId
                // Parsing Location
                // ToDo Location wird falsch geparsed?
                if (joObject.get("location") instanceof JSONObject) {
                    JSONObject joLocation = (JSONObject) joObject.get("location");
                    sb.append(joLocation.get("y"));
                    sb.append(",");
                    sb.append(joLocation.get("x"));
                    sb.append("#");
                }
                // Parsing Location
                // Parsing Products
                JSONArray jaProductsArray = null;

                if (joObject.get("products") instanceof JSONArray){
                    jaProductsArray = (JSONArray) joObject.get("products");
                }
                JSONObject joProductDetail = null;
                if (jaProductsArray != null) {
                    for (int u = 0; u < jaProductsArray.length(); ++u) {
                        if (jaProductsArray.get(u) instanceof JSONObject) {
                            joProductDetail = (JSONObject) jaProductsArray.get(u);
                            sb.append(joProductDetail.get("guid"));
                            sb.append(",");
                            sb.append(joProductDetail.get("boxing_temp"));
                            sb.append(",");
                            sb.append(joProductDetail.get("sales_price"));
                            sb.append(",");
                            sb.append(joProductDetail.get("breads_per_oven"));
                            sb.append(",");
                            sb.append(joProductDetail.get("breads_per_box"));
                            sb.append(",");
                            sb.append(joProductDetail.get("item_prep_time"));
                            sb.append(",");
                            sb.append(joProductDetail.get("dough_prep_time"));
                            sb.append(",");
                            sb.append(joProductDetail.get("baking_temp"));
                            sb.append(",");
                            sb.append(joProductDetail.get("cooling_rate"));
                            sb.append(",");
                            sb.append(joProductDetail.get("baking_time"));
                            sb.append(",");
                            sb.append(joProductDetail.get("resting_time"));
                            sb.append(",");
                            sb.append(joProductDetail.get("production_cost"));
                            if (u < jaProductsArray.length() - 1){
                                sb.append(",");
                            }
                        }
                    }
                }
                // Parsing Products
                sb.append("#");
                // Parsing Kneading_machines
                JSONArray jaKneadingMachinesArray = null;
                if ((JSONArray) joObject.get("kneading_machines") instanceof JSONArray){
                    jaKneadingMachinesArray = (JSONArray) joObject.get("kneading_machines");
                }
                JSONObject joKneadinMachineDetails = null;
                for (int o = 0; o < jaKneadingMachinesArray.length(); ++o) {
                    if (jaKneadingMachinesArray != null && jaKneadingMachinesArray.get(o) instanceof JSONObject) {
                        joKneadinMachineDetails = (JSONObject) jaKneadingMachinesArray.get(o);
                        sb.append(joKneadinMachineDetails.get("guid"));
                        if(o < jaKneadingMachinesArray.length() - 1){sb.append(",");}
                    }
                }

                // Parsing Kneading_machines
                sb.append(")");
                sb.append(";");
                // Scheduler passing finished
                // Parsing Truck
                JSONArray jaTruckArray = null;
                if (joObject.get("trucks") instanceof JSONArray) {
                    jaTruckArray = (JSONArray) joObject.get("trucks");
                }
                if (jaTruckArray != null) {
                    for (int o = 0; o < jaTruckArray.length(); o++) {
                        if(jaTruckArray.get(o) instanceof JSONObject){
                            JSONObject joTruckDetails = (JSONObject) jaTruckArray.get(o);
                            sb.append(joTruckDetails.get("guid")+sTrPrefix);
                            sb.append("(");
                            sb.append(joObject.get("guid"));
                            sb.append(",");
                            sb.append(joTruckDetails.get("guid"));
                            sb.append(",");
                            sb.append(joTruckDetails.get("load_capacity"));
                            sb.append(",");
                            if (joTruckDetails.get("location") instanceof JSONObject) {
                                JSONObject joLocation = (JSONObject) joTruckDetails.get("location");
                                sb.append(joLocation.get("y"));
                                sb.append(",");
                                sb.append(joLocation.get("x"));
                            }
                            sb.append(")");
                            sb.append(";");
                        }
                    }
                }
    //            sb.append(")");
                // Parsing Truck
                // ToDO Parsing TruckScheduler
                // Parsing TruckScheduler
            }
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
