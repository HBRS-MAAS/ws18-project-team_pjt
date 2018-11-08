package org.team_pjt;

import org.json.JSONArray;
import org.json.JSONObject;
import org.team_pjt.Objects.Order;

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

    	List<String> agents = new Vector<>();
    	if(isHost) {
            agents.add("clock:org.team_pjt.agents.SystemClockAgent");
            agents.add("bakery1:org.team_pjt.agents.OrderProcessing");
//            agents.add("c1:org.team_pjt.agents.CustomerAgent");
            agents.add("c2:org.team_pjt.agents.CustomerAgent");
        }
        else {
            agents.add("bakery2:org.team_pjt.agents.OrderProcessing");
            agents.add("c3:org.team_pjt.agents.CustomerAgent");
            agents.add("c4:org.team_pjt.agents.CustomerAgent");
        }

    	String scenario = readScenarioFile();
    	if(scenario == null) {
    	    System.exit(-1);
        }
    	List<String> cmd = buildCMD(agents, new JSONObject(scenario));
//    	  System.out.println(cmd.toString());
//        System.out.println(cmd.size());
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));

    }

    public static List<String> buildCMD(List<String> agents, JSONObject scenario) {
    	StringBuilder sb = new StringBuilder();
        List<String> cmd = new Vector<>();
        Hashtable<String, List<Order>> sortedOrders = getSortedOrders(scenario.getJSONArray("orders"));
        JSONArray customers = scenario.getJSONArray("customers");
        JSONArray bakeries = scenario.getJSONArray("bakeries");
        Iterator<Object> customerIterator = customers.iterator();
        Iterator<Object> bakeryIterator = bakeries.iterator();
    	if(isHost) {
//            cmd.add("-local-host");
//            cmd.add("bilbo");
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
			if(a.contains("Customer")) {
                sb.append("(");
                JSONObject customer = (JSONObject)customerIterator.next();
                sb.append(customer.toString().replaceAll(",", "###"));
                sb.append(",");
                JSONObject orders = new JSONObject();
                orders.put("orders", sortedOrders.get(customer.getString("guid")));
                sb.append(orders.toString().replaceAll(",", "###").replaceAll("customerID", "customer_id").replaceAll("deliveryDate", "delivery_date").replaceAll("orderDate", "order_date"));
                sb.append(")");
            }
            if(a.contains("Order")) {
                sb.append("(");
                sb.append(((JSONObject)bakeryIterator.next()).toString().replaceAll(",", "###"));
                sb.append(")");
            }
			sb.append(";");
		}

        int iOvenPrefix = 0;
        int iSchedulerPrefix = 0;
        int iTruckPrefix = 0;
        for (int i = 0; i< bakeries.length(); ++i) {

            JSONObject joObject = null;
            if (bakeries.get(i) instanceof JSONObject) {
                joObject = (JSONObject) bakeries.get(i);
            }
            JSONArray jaOvenArray = null;
            // Parsing Ovens
            if (joObject.get("ovens") instanceof JSONArray) {
                jaOvenArray = (JSONArray) joObject.get("ovens");
            }
            JSONObject jsOvenDetail = null;
            for (int z = 0; z < jaOvenArray.length(); ++z) {
                if(jaOvenArray != null && jaOvenArray.get(z) instanceof JSONObject){
                    sb.append("o"+iOvenPrefix+sOvPrefix);
                    iOvenPrefix++;
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
            sb.append("s"+iSchedulerPrefix+sSchPrefix);
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
            iSchedulerPrefix++;
            // Scheduler passing finished
            // Parsing Truck
            JSONArray jaTruckArray = null;
            if (joObject.get("trucks") instanceof JSONArray) {
                jaTruckArray = (JSONArray) joObject.get("trucks");
            }
            if (jaTruckArray != null) {
                for (int o = 0; o < jaTruckArray.length(); o++) {
                    if(jaTruckArray.get(o) instanceof JSONObject){
                        sb.append("t"+iTruckPrefix+sTrPrefix);
                        sb.append("(");
                        JSONObject joTruckDetails = (JSONObject) jaTruckArray.get(o);
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
                        iTruckPrefix++;
                    }
                }
            }
//            sb.append(")");
            // Parsing Truck
            // ToDO Parsing TruckScheduler
            // Parsing TruckScheduler
        }
        cmd.add(sb.toString());

    	return cmd;
	}

    private static void parsingBakeryId(StringBuilder sb, JSONObject joObject) {
        if (joObject.get("guid") instanceof JSONObject) {
            sb.append(joObject.get("guid"));
        }
        sb.append("#");
    }

    public static Hashtable<String, List<Order>> getSortedOrders(JSONArray jsonOrders) {
        Hashtable<String, List<Order>> orders = new Hashtable<>();
        Iterator<Object> orderIterator = jsonOrders.iterator();

        while(orderIterator.hasNext()) {
            Set<String> keys = orders.keySet();
            Order o = new Order((orderIterator.next()).toString());
            if (keys.contains(o.getCustomerID())) {
                orders.get(o.getCustomerID()).add(o);
            }
            else {
                List<Order> cOrder = new Vector<>();
                cOrder.add(o);
                orders.put(o.getCustomerID(), cOrder);
            }
        }

        return orders;
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
