package org.team_pjt;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

//import org.team_pjt.messages.BakingNotification;
//import org.team_pjt.messages.BakingRequest;
//import org.team_pjt.messages.CoolingRequest;
import org.team_pjt.messages.DoughNotification;
//import org.team_pjt.messages.KneadingNotification;
//import org.team_pjt.messages.KneadingRequest;
//import org.team_pjt.messages.LoadingBayMessage;
//import org.team_pjt.messages.PreparationNotification;
//import org.team_pjt.messages.PreparationRequest;
import org.team_pjt.messages.ProofingRequest;
import org.team_pjt.Objects.BakedGood;
//import org.team_pjt.Objects.Bakery;
//import org.team_pjt.Objects.Batch;
//import org.team_pjt.Objects.Client;
//import org.team_pjt.Objects.DeliveryCompany;
//import org.team_pjt.Objects.DoughPrepTable;
//import org.team_pjt.Objects.Equipment;
//import org.team_pjt.Objects.KneadingMachine;
//import org.team_pjt.Objects.MetaInfo;
//import org.team_pjt.Objects.OrderMas;
//import org.team_pjt.Objects.Oven;
//import org.team_pjt.Objects.Packaging;
//import org.team_pjt.Objects.ProductMas;
//import org.team_pjt.Objects.Recipe;
//import org.team_pjt.Objects.Step;
//import org.team_pjt.Objects.StreetLink;
//import org.team_pjt.Objects.StreetNetwork;
//import org.team_pjt.Objects.StreetNode;
//import org.team_pjt.Objects.Truck;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JSONConverter
{
//    public static void test_parsing()
//    {
//        test_parsing(false);
//    }
//
//    public static void test_parsing(boolean debug)
//    {
//        String sampleDir = "src/main/resources/config/sample/";
//        String doughDir = "src/main/resources/config/dough_stage_communication/";
//        String bakingDir = "src/main/resources/config/baking_stage_communication/";
//        String smallTestDir = "src/main/resources/config/small/";
//
//        try {
//            //System.out.println("Working Directory = " + System.getProperty("user.dir"));
//
//            String bakeryFile = new Scanner(new File(smallTestDir + "bakeries.json")).useDelimiter("\\Z").next();
//            Vector<Bakery> bakeries = parseBakeries(bakeryFile);
//            for (Bakery bakery : bakeries)
//            {
//                if (debug)
//                    System.out.println(bakery);
//            }
//
//            String clientFile = new Scanner(new File(smallTestDir + "clients.json")).useDelimiter("\\Z").next();
//            Vector<Client> clients = parseClients(clientFile);
//            for (Client client : clients)
//            {
//                if (debug)
//                    System.out.println(client);
//            }
//
//            String deliveryCompanyFile = new Scanner(new File(smallTestDir + "delivery.json")).useDelimiter("\\Z").next();
//            Vector<DeliveryCompany> deliveryCompanies = parseDeliveryCompany(deliveryCompanyFile);
//            for (DeliveryCompany deliveryCompany : deliveryCompanies)
//            {
//                if (debug)
//                    System.out.println(deliveryCompany);
//            }
//
//            String metaInfoFile = new Scanner(new File(smallTestDir + "meta.json")).useDelimiter("\\Z").next();
//            MetaInfo metaInfo = parseMetaInfo(metaInfoFile);
//            if (debug)
//                System.out.println(metaInfo);
//
//            String streetNetworkFile = new Scanner(new File(smallTestDir + "street-network.json")).useDelimiter("\\Z").next();
//            StreetNetwork streetNetwork = parseStreetNetwork(streetNetworkFile);
//            if (debug)
//                System.out.println(streetNetwork);
//
//            String doughNotificationString = new Scanner(new File(doughDir + "dough_notification.json")).useDelimiter("\\Z").next();
//            DoughNotification doughtNotification = parseDoughNotification(doughNotificationString);
//            if (debug)
//                System.out.println(doughtNotification);
//
//            String kneadingNotificationString = new Scanner(new File(doughDir + "kneading_notification.json")).useDelimiter("\\Z").next();
//            KneadingNotification kneadingNotification = parseKneadingNotification(kneadingNotificationString);
//            if (debug)
//                System.out.println(kneadingNotification);
//
//            String kneadingRequestString = new Scanner(new File(doughDir + "kneading_request.json")).useDelimiter("\\Z").next();
//            KneadingRequest kneadingRequest = parseKneadingRequest(kneadingRequestString);
//            if (debug)
//                System.out.println(kneadingRequest);
//
//            String preparationNotificationString = new Scanner(new File(doughDir + "preparation_notification.json")).useDelimiter("\\Z").next();
//            PreparationNotification preparationNotification = parsePreparationNotification(preparationNotificationString);
//            if (debug)
//                System.out.println(preparationNotification);
//
//            String preparationRequestString = new Scanner(new File(doughDir + "preparation_request.json")).useDelimiter("\\Z").next();
//            PreparationRequest preparationRequest = parsePreparationRequest(preparationRequestString);
//            if (debug)
//                System.out.println(preparationRequest);
//
//            String proofingRequestString = new Scanner(new File(doughDir + "proofing_request.json")).useDelimiter("\\Z").next();
//            ProofingRequest proofingRequest = parseProofingRequest(proofingRequestString);
//            if (debug)
//                System.out.println(proofingRequest);
//
//            // baking message tests
//            String bakingRequestString = new Scanner(new File(bakingDir + "baking_request.json")).useDelimiter("\\Z").next();
//            BakingRequest bakingRequest = parseBakingRequest(bakingRequestString);
//            if (debug)
//                System.out.println(bakingRequest);
//
//            String bakingNotificationString = new Scanner(new File(bakingDir + "baking_notification.json")).useDelimiter("\\Z").next();
//            BakingNotification bakingNotification = parseBakingNotification(bakingNotificationString);
//            if (debug)
//                System.out.println(bakingNotification);
//
//            String coolingRequestString = new Scanner(new File(bakingDir + "cooling_request.json")).useDelimiter("\\Z").next();
//            CoolingRequest coolingRequest = parseCoolingRequest(coolingRequestString);
//            if (debug)
//                System.out.println(coolingRequest);
//
//            String loadingBayMessageString = new Scanner(new File(bakingDir + "loading_bay_message.json")).useDelimiter("\\Z").next();
//            LoadingBayMessage loadingBayMessage = parseLoadingBayMessage(loadingBayMessageString);
//            if (debug)
//                System.out.println(loadingBayMessage);
//
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//
//    public static Vector<Bakery> parseBakeries(String jsonFile)
//    {
//        JsonElement root = new JsonParser().parse(jsonFile);
//        JsonArray arr = root.getAsJsonArray();
//
//        Vector<Bakery> bakeries = new Vector<Bakery>();
//        for (JsonElement element : arr)
//        {
//            // bakery
//            JsonObject jsonBakery = element.getAsJsonObject();
//            String guid = jsonBakery.get("guid").getAsString();
//            String name = jsonBakery.get("name").getAsString();
//            Point2D location = parseLocation(jsonBakery);
//
//            // products
//            Vector<ProductMas> products = new Vector<ProductMas>();
//            JsonArray jsonProducts = jsonBakery.get("products").getAsJsonArray();
//            for (JsonElement product : jsonProducts)
//            {
//                JsonObject jsonProduct = product.getAsJsonObject();
//                String productGuid = jsonProduct.get("guid").getAsString();
//
//                JsonObject jsonBatch = jsonProduct.get("batch").getAsJsonObject();
//                int breadsPerOven = jsonBatch.get("breadsPerOven").getAsInt();
//                Batch batch = new Batch(breadsPerOven);
//
//                JsonObject jsonRecipe = jsonProduct.get("recipe").getAsJsonObject();
//                int bakingTemp = jsonRecipe.get("bakingTemp").getAsInt();
//
//                JsonArray stepArray = jsonRecipe.get("steps").getAsJsonArray();
//                Vector<Step> steps = new Vector<Step>();
//                for (JsonElement step : stepArray)
//                {
//                    JsonObject jsonStep = step.getAsJsonObject();
//                    String action = jsonStep.get("action").getAsString();
//                    Float duration = jsonStep.get("duration").getAsFloat();
//                    Step aStep = new Step(action, duration);
//                    steps.add(aStep);
//                }
//                Recipe recipe = new Recipe(bakingTemp, steps);
//
//                JsonObject jsonPackaging = jsonProduct.get("packaging").getAsJsonObject();
//                int boxingTemp = jsonPackaging.get("boxingTemp").getAsInt();
//                int breadsPerBox = jsonPackaging.get("breadsPerBox").getAsInt();
//                Packaging packaging = new Packaging(boxingTemp, breadsPerBox);
//
//                Double salesPrice = jsonProduct.get("salesPrice").getAsDouble();
//                Double productionCost = jsonProduct.get("productionCost").getAsDouble();
//
//                ProductMas aProduct = new ProductMas(productGuid, batch, recipe, packaging, salesPrice,  productionCost);
//                products.add(aProduct);
//            }
//
//            // available_products
//            Vector<String> availableProducts = new Vector<String>();
//            JsonArray jsonAvailableProducts = jsonBakery.get("available_products").getAsJsonArray();
//            for (JsonElement product : jsonAvailableProducts)
//            {
//                availableProducts.add(product.getAsString());
//            }
//
//
//            // equipment
//            Vector<Equipment> equipment = new Vector<Equipment>();
//            JsonObject jsonEquipment = jsonBakery.get("equipment").getAsJsonObject();
//            JsonArray ovens = jsonEquipment.get("ovens").getAsJsonArray();
//            for (JsonElement oven : ovens)
//            {
//                JsonObject jsonOven = oven.getAsJsonObject();
//                String ovenGuid = jsonOven.get("guid").getAsString();
//
//                // multiple ways of denoting rates (CamelCase and _ are used)
//                // TODO cleanup and make into a function
//                int coolingRate = -1;
//                if (jsonOven.toString().contains("coolingRate"))
//                {
//                    coolingRate = jsonOven.get("coolingRate").getAsInt();
//                }
//                else if (jsonOven.toString().contains("cooling_rate"))
//                {
//                    coolingRate = jsonOven.get("cooling_rate").getAsInt();
//                }
//
//                // TODO cleanup and make into a function
//                int heatingRate = -1;
//                if (jsonOven.toString().contains("heatingRate"))
//                {
//                    heatingRate = jsonOven.get("heatingRate").getAsInt();
//                }
//                else if (jsonOven.toString().contains("heating_rate"))
//                {
//                    heatingRate = jsonOven.get("heating_rate").getAsInt();
//                }
//
//                Oven anOven = new Oven(ovenGuid, coolingRate, heatingRate);
//                equipment.add(anOven);
//            }
//
//            JsonArray doughPrepTables = jsonEquipment.get("doughPrepTables").getAsJsonArray();
//            for (JsonElement table : doughPrepTables)
//            {
//                JsonObject jsonTable = table.getAsJsonObject();
//                String tableGuid = jsonTable.get("guid").getAsString();
//
//                DoughPrepTable aTable = new DoughPrepTable(tableGuid);
//                equipment.add(aTable);
//            }
//
//            JsonArray kneadingMachines = jsonEquipment.get("kneadingMachines").getAsJsonArray();
//            for (JsonElement kneadingMachine : kneadingMachines)
//            {
//                JsonObject jsonKneadingMachine = kneadingMachine.getAsJsonObject();
//                String kneadingMachineGuid = jsonKneadingMachine.get("guid").getAsString();
//
//                KneadingMachine aMachine = new KneadingMachine(kneadingMachineGuid);
//                equipment.add(aMachine);
//            }
//
//            Bakery bakery = new Bakery(guid, name, location, products, availableProducts, equipment);
//            bakeries.add(bakery);
//        }
//
//        return bakeries;
//    }
//
//    public static Vector<Client> parseClients(String jsonFile)
//    {
//        JsonElement root = new JsonParser().parse(jsonFile);
//        JsonArray arr = root.getAsJsonArray();
//
//        Vector<Client> clients = new Vector<Client>();
//        for (JsonElement element : arr)
//        {
//            JsonObject jsonClient = element.getAsJsonObject();
//            String guid = jsonClient.get("guid").getAsString();
//            int type = jsonClient.get("type").getAsInt();
//            String name = jsonClient.get("name").getAsString();
//            Point2D location = parseLocation(jsonClient);
//
//            // orders
//            Vector<OrderMas> orders = new Vector<OrderMas>();
//            JsonArray jsonOrders = jsonClient.get("orders").getAsJsonArray();
//            for (JsonElement order : jsonOrders)
//            {
//                String jsonOrder = order.toString();
//                orders.add(JSONConverter.parseOrder(jsonOrder));
//            }
//
//            Client aClient = new Client(guid, type, name, location, orders);
//            clients.add(aClient);
//        }
//
//        return clients;
//    }
//
//    public static OrderMas parseOrder(String jsonFile)
//    {
//        JsonElement root = new JsonParser().parse(jsonFile);
//        JsonObject jsonOrder = root.getAsJsonObject();
//
//        String orderGuid = jsonOrder.get("guid").getAsString();
//        JsonObject jsonOrderDate = jsonOrder.get("order_date").getAsJsonObject();
//        String customerId = jsonOrder.get("customer_id").getAsString();
//        int orderDay = jsonOrderDate.get("day").getAsInt();
//        int orderHour = jsonOrderDate.get("hour").getAsInt();
//        JsonObject jsonDeliveryDate = jsonOrder.get("delivery_date").getAsJsonObject();
//        int deliveryDay = jsonDeliveryDate.get("day").getAsInt();
//        int deliveryHour = jsonDeliveryDate.get("hour").getAsInt();
//
//        // products (BakedGood objects)
//        // TODO shouldn't products be an array not an object?
//        // TODO this will need to be reworked in the future when BakedGood is more fleshed out
//        // TODO also this JUST is a bit hacky...
//        Vector<BakedGood> bakedGoods = new Vector<BakedGood>();
//        JsonObject jsonProducts = jsonOrder.get("products").getAsJsonObject();
//        for (String bakedGoodName : BakedGood.bakedGoodNames)
//        {
//            int amount = jsonProducts.get(bakedGoodName).getAsInt();
//            bakedGoods.add(new BakedGood(bakedGoodName, amount));
//        }
//
//        OrderMas anOrder = new OrderMas(customerId, orderGuid, orderDay, orderHour, deliveryDay, deliveryHour, bakedGoods);
//        return anOrder;
//    }
//
//    public static Vector<DeliveryCompany> parseDeliveryCompany(String jsonFile)
//    {
//        JsonElement root = new JsonParser().parse(jsonFile);
//        JsonArray arr = root.getAsJsonArray();
//
//        Vector<DeliveryCompany> companies = new Vector<DeliveryCompany>();
//        for (JsonElement element : arr)
//        {
//            JsonObject jsonDeliveryCompany = element.getAsJsonObject();
//            String guid = jsonDeliveryCompany.get("guid").getAsString();
//            Point2D location = parseLocation(jsonDeliveryCompany);
//
//            Vector<Truck> trucks = new Vector<Truck>();
//            JsonArray jsonTrucks = jsonDeliveryCompany.get("trucks").getAsJsonArray();
//            for (JsonElement truck : jsonTrucks)
//            {
//                JsonObject jsonTruck = truck.getAsJsonObject();
//                String truckGuid = jsonTruck.get("guid").getAsString();
//                int loadCapacity = jsonTruck.get("load_capacity").getAsInt();
//                Point2D truckLocation = parseLocation(jsonDeliveryCompany);
//
//                Truck aTruck = new Truck(truckGuid, loadCapacity, truckLocation);
//                trucks.add(aTruck);
//            }
//
//            DeliveryCompany aCompany = new DeliveryCompany(guid, location, trucks);
//            companies.add(aCompany);
//        }
//
//        return companies;
//    }
//
//    public static MetaInfo parseMetaInfo(String jsonFile)
//    {
//        JsonElement root = new JsonParser().parse(jsonFile);
//
//        JsonObject jsonMetaInfo = root.getAsJsonObject();
//        int bakeries = jsonMetaInfo.get("bakeries").getAsInt();
//        int durationInDays = jsonMetaInfo.get("durationInDays").getAsInt();
//        int products = jsonMetaInfo.get("products").getAsInt();
//        int orders = jsonMetaInfo.get("orders").getAsInt();
//        Vector<Client> customers = new Vector<Client>(); // TODO this will change when the json changes
//
//        // TODO shouldn't the customers be in an array not an Object?
//        Set<Entry<String, JsonElement>> entrySet = jsonMetaInfo.get("customers").getAsJsonObject().entrySet();
//        for(Map.Entry<String,JsonElement> entry : entrySet)
//        {
//            Client customer = new Client();
//            customer.setGuid(entry.getKey());
//            // TODO fix, this is super hacky
//            customer.setType(entry.getValue().getAsInt());
//            customers.add(customer);
//        }
//
//        MetaInfo metaInfo = new MetaInfo(bakeries, customers, durationInDays, products, orders);
//
//        return metaInfo;
//    }
//
//    public static StreetNetwork parseStreetNetwork(String jsonFile)
//    {
//        JsonElement root = new JsonParser().parse(jsonFile);
//        JsonObject jsonStreetNetwork = root.getAsJsonObject();
//        boolean directed = jsonStreetNetwork.get("directed").getAsBoolean();
//
//        Vector<StreetNode> nodes = new Vector<StreetNode>();
//        JsonArray jsonStreetNodes = jsonStreetNetwork.get("nodes").getAsJsonArray();
//        for (JsonElement streetNode : jsonStreetNodes)
//        {
//            JsonObject jsonStreetNode = streetNode.getAsJsonObject();
//            String name = null;
//            if (jsonStreetNode.has("name"))
//                name = jsonStreetNode.get("name").getAsString();
//            String company = null;
//            if (jsonStreetNode.has("company"))
//                company = jsonStreetNode.get("company").getAsString();
//            String type = null;
//           if (jsonStreetNode.has("type"))
//                type = jsonStreetNode.get("type").getAsString();
//            String guid = jsonStreetNode.get("guid").getAsString();
//
//            Point2D location = parseLocation(jsonStreetNode);
//
//            StreetNode aStreetNode = new StreetNode(name, company, location, guid, type);
//            nodes.add(aStreetNode);
//        }
//
//        Vector<StreetLink> links = new Vector<StreetLink>();
//        JsonArray jsonStreetLinks = jsonStreetNetwork.get("links").getAsJsonArray();
//        for (JsonElement streetLink : jsonStreetLinks)
//        {
//            JsonObject jsonStreetLink = streetLink.getAsJsonObject();
//            String source = jsonStreetLink.get("source").getAsString();
//            String guid = jsonStreetLink.get("guid").getAsString();
//            double dist = jsonStreetLink.get("dist").getAsDouble();
//            String target = jsonStreetLink.get("target").getAsString();
//
//            StreetLink aStreetLink = new StreetLink(source, guid, dist, target);
//            links.add(aStreetLink);
//        }
//
//
//        StreetNetwork streetNetwork = new StreetNetwork(directed, nodes, links);
//        return streetNetwork;
//    }

    public static DoughNotification parseDoughNotification(String jsonString)
    {
        JsonElement root = new JsonParser().parse(jsonString);
        JsonObject jsonDoughNotification = root.getAsJsonObject();

        String productType = jsonDoughNotification.get("productType").getAsString();
        Vector<String> guids = new Vector<String>();
        JsonArray jsonGuids = jsonDoughNotification.get("guids").getAsJsonArray();
        for (JsonElement guid : jsonGuids)
        {
            guids.add(guid.getAsString());
        }

        Vector<Integer> productQuantities = new Vector<Integer>();
        JsonArray jsonProductQuantities = jsonDoughNotification.get("productQuantities").getAsJsonArray();
        for (JsonElement productQuantitie : jsonProductQuantities)
        {
            productQuantities.add(productQuantitie.getAsInt());
        }

        DoughNotification doughNotification = new DoughNotification(guids, productType, productQuantities);
        return doughNotification;
    }

//    public static KneadingNotification parseKneadingNotification(String jsonString)
//    {
//        JsonElement root = new JsonParser().parse(jsonString);
//        JsonObject jsonKneadingNotification = root.getAsJsonObject();
//
//        String productType = jsonKneadingNotification.get("productType").getAsString();
//        Vector<String> guids = new Vector<String>();
//        JsonArray jsonGuids = jsonKneadingNotification.get("guids").getAsJsonArray();
//        for (JsonElement guid : jsonGuids)
//        {
//            guids.add(guid.getAsString());
//        }
//
//        KneadingNotification kneadingNotification = new KneadingNotification(guids, productType);
//        return kneadingNotification;
//    }
//
//    public static KneadingRequest parseKneadingRequest(String jsonString)
//    {
//        JsonElement root = new JsonParser().parse(jsonString);
//        JsonObject jsonKneadingRequest = root.getAsJsonObject();
//
//        String productType = jsonKneadingRequest.get("productType").getAsString();
//        Float kneadingTime = jsonKneadingRequest.get("kneadingTime").getAsFloat();
//        Vector<String> guids = new Vector<String>();
//        JsonArray jsonGuids = jsonKneadingRequest.get("guids").getAsJsonArray();
//        for (JsonElement guid : jsonGuids)
//        {
//            guids.add(guid.getAsString());
//        }
//
//        KneadingRequest kneadingRequest = new KneadingRequest(guids, productType, kneadingTime);
//        return kneadingRequest;
//    }
//
//    public static PreparationNotification parsePreparationNotification(String jsonString)
//    {
//        JsonElement root = new JsonParser().parse(jsonString);
//        JsonObject jsonPreparationNotification = root.getAsJsonObject();
//
//        String productType = jsonPreparationNotification.get("productType").getAsString();
//        Vector<String> guids = new Vector<String>();
//        JsonArray jsonGuids = jsonPreparationNotification.get("guids").getAsJsonArray();
//        for (JsonElement guid : jsonGuids)
//        {
//            guids.add(guid.getAsString());
//        }
//
//        PreparationNotification preparationNotification = new PreparationNotification(guids, productType);
//        return preparationNotification;
//    }
//
//    public static PreparationRequest parsePreparationRequest(String jsonString)
//    {
//        JsonElement root = new JsonParser().parse(jsonString);
//        JsonObject jsonPreparationRequest = root.getAsJsonObject();
//
//        String productType = jsonPreparationRequest.get("productType").getAsString();
//
//        Vector<Integer> productQuantities = new Vector<Integer>();
//        JsonArray jsonProductQuantities = jsonPreparationRequest.get("productQuantities").getAsJsonArray();
//        for (JsonElement productQuantity : jsonProductQuantities)
//        {
//            productQuantities.add(productQuantity.getAsInt());
//        }
//
//        Vector<Step> steps = new Vector<Step>();
//        JsonArray jsonSteps = jsonPreparationRequest.get("steps").getAsJsonArray();
//        for (JsonElement step : jsonSteps)
//        {
//            JsonObject jsonStep = step.getAsJsonObject();
//            String action = jsonStep.get("action").getAsString();
//            Float duration = jsonStep.get("duration").getAsFloat();
//
//            Step aStep = new Step(action, duration);
//            steps.add(aStep);
//        }
//
//        Vector<String> guids = new Vector<String>();
//        JsonArray jsonGuids = jsonPreparationRequest.get("guids").getAsJsonArray();
//        for (JsonElement guid : jsonGuids)
//        {
//            guids.add(guid.getAsString());
//        }
//
//        PreparationRequest preparationRequest = new PreparationRequest(guids, productType, productQuantities, steps);
//        return preparationRequest;
//    }

    public static ProofingRequest parseProofingRequest(String jsonString)
    {
        JsonElement root = new JsonParser().parse(jsonString);
        JsonObject jsonProofingRequest = root.getAsJsonObject();

        String productType = jsonProofingRequest.get("productType").getAsString();
        Float proofingTime = jsonProofingRequest.get("proofingTime").getAsFloat();

        Vector<String> guids = new Vector<String>();
        JsonArray jsonGuids = jsonProofingRequest.get("guids").getAsJsonArray();
        for (JsonElement guid : jsonGuids)
        {
            guids.add(guid.getAsString());
        }

        Vector<Integer> productQuantities = new Vector<Integer>();
        JsonArray jsonProductQuantities = jsonProofingRequest.get("productQuantities").getAsJsonArray();
        for (JsonElement productQuantitie : jsonProductQuantities)
        {
            productQuantities.add(productQuantitie.getAsInt());
        }

        ProofingRequest proofingRequest = new ProofingRequest(productType, guids, proofingTime, productQuantities);
        return proofingRequest;
    }

//    public static BakingRequest parseBakingRequest(String jsonString)
//    {
//        JsonElement root = new JsonParser().parse(jsonString);
//        JsonObject jsonBakingRequest = root.getAsJsonObject();
//
//        int bakingTemp = jsonBakingRequest.get("bakingTemp").getAsInt();
//        Float bakingTime = jsonBakingRequest.get("bakingTime").getAsFloat();
//        String productType = jsonBakingRequest.get("productType").getAsString();
//
//        Vector<String> guids = new Vector<String>();
//        JsonArray jsonGuids = jsonBakingRequest.get("guids").getAsJsonArray();
//        for (JsonElement guid : jsonGuids)
//        {
//            guids.add(guid.getAsString());
//        }
//
//        Vector<Integer> productQuantities = new Vector<Integer>();
//        JsonArray jsonProductQuantities = jsonBakingRequest.get("productQuantities").getAsJsonArray();
//        for (JsonElement productQuantitie : jsonProductQuantities)
//        {
//            productQuantities.add(productQuantitie.getAsInt());
//        }
//
//        BakingRequest bakingRequest = new BakingRequest(guids, productType, bakingTemp, bakingTime, productQuantities);
//        return bakingRequest;
//    }
//
//    public static BakingNotification parseBakingNotification(String jsonString)
//    {
//        JsonElement root = new JsonParser().parse(jsonString);
//        JsonObject jsonBakingNotification = root.getAsJsonObject();
//
//        String productType = jsonBakingNotification.get("productType").getAsString();
//        Vector<String> guids = new Vector<String>();
//        JsonArray jsonGuids = jsonBakingNotification.get("guids").getAsJsonArray();
//        for (JsonElement guid : jsonGuids)
//        {
//            guids.add(guid.getAsString());
//        }
//
//        Vector<Integer> productQuantities = new Vector<Integer>();
//        JsonArray jsonProductQuantities = jsonBakingNotification.get("productQuantities").getAsJsonArray();
//        for (JsonElement productQuantitie : jsonProductQuantities)
//        {
//            productQuantities.add(productQuantitie.getAsInt());
//        }
//
//        BakingNotification bakingNotification = new BakingNotification(guids, productType, productQuantities);
//        return bakingNotification;
//    }
//
//    public static CoolingRequest parseCoolingRequest(String jsonString)
//    {
//        JsonElement root = new JsonParser().parse(jsonString);
//
//        CoolingRequest coolingRequest = new CoolingRequest();
//
//        JsonArray jsonCoolingRequests = root.getAsJsonArray();
//        for (JsonElement jsonCoolingRequest : jsonCoolingRequests)
//        {
//            JsonObject CoolingRequeatjson = jsonCoolingRequest.getAsJsonObject();
//            String guid = CoolingRequeatjson.get("guid").getAsString();
//            int quantity = CoolingRequeatjson.get("quantity").getAsInt();
//            float coolingDuration = CoolingRequeatjson.get("coolingDuration").getAsFloat();
//
//            coolingRequest.addCoolingRequest(guid, coolingDuration, quantity);
//        }
//
//        return coolingRequest;
//
//    }
//
//    public static LoadingBayMessage parseLoadingBayMessage(String jsonString)
//    {
//        JsonElement root = new JsonParser().parse(jsonString);
//        JsonObject jsonLoadingBayMessage = root.getAsJsonObject();
//
//        LoadingBayMessage loadingBayMessage = new LoadingBayMessage();
//        JsonArray jsonProducts = jsonLoadingBayMessage.get("products").getAsJsonArray();
//        for (JsonElement product : jsonProducts)
//        {
//            JsonObject jsonProduct = product.getAsJsonObject();
//            String productName = jsonProduct.get("productName").getAsString();
//            int quantity = jsonProduct.get("quantity").getAsInt();
//
//            loadingBayMessage.addProduct(productName, quantity);
//        }
//
//        return loadingBayMessage;
//    }

    public static Point2D parseLocation(JsonObject jsonObject)
    {
        Point2D location = null;
        JsonElement elementLocation = jsonObject.get("location");
        if (!elementLocation.isJsonNull())
        {
            JsonObject jsonLocation = (JsonObject) elementLocation;
            Double x = jsonLocation.get("x").getAsDouble();
            Double y = jsonLocation.get("y").getAsDouble();
            location = new Point2D.Double(x, y);
        }

        return location;
    }
}
