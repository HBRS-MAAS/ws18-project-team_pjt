package org.team_pjt.agents;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.team_pjt.doughprep.mas_maas.agents.BaseAgent;
import org.team_pjt.doughprep.mas_maas.objects.BakedGood;
import org.team_pjt.doughprep.mas_maas.objects.Order;
import org.team_pjt.objects.DoughPreparationTable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


public class DoughManager extends BaseAgent {
    private Order order;
    private String sIdBakery;
    private Vector<org.team_pjt.objects.KneadingMachine> vKneadingMachine;
    private Vector<DoughPreparationTable> vDoughPrepTable;
    private JSONArray jsaProducts;
    private HashMap<String, HashMap<String, Integer>> hmRecipeProducts;
    protected void setup() {
        super.setup();
        Object[] oArguments = getArguments();
        if (!readArgs(oArguments)) {
            System.out.println("No parameters given for DoughManager" + getName());
        }
        System.out.println(getAID().getLocalName() + " is ready.");
        this.register("Dough-manager", "JADE-bakery");
        addBehaviour(new receiveOrder());
    }

    private boolean readArgs(Object [] o) {
        if(o!= null){
            vKneadingMachine = new Vector<>();
            JSONObject jsoBakery = new JSONObject(o[0].toString().replace("###",","));
            sIdBakery = jsoBakery.getString("guid");
            JSONObject jsoEquipment = (JSONObject) jsoBakery.get("equipment");
            readDoughPreparationTable(jsoEquipment);
            readKneadingMachines(jsoEquipment);
            readRecipes(jsoBakery);
            return true;
        } else {
            return false;
        }

    }

    private void readKneadingMachines(JSONObject jsoEquipment) {
        Iterator<Object> iKneadingMachines = jsoEquipment.getJSONArray("kneadingMachines").iterator();
        while(iKneadingMachines.hasNext()){
            JSONObject jsoKneadingMachine = (JSONObject) iKneadingMachines.next();
            vKneadingMachine.add(new org.team_pjt.objects.KneadingMachine(jsoKneadingMachine.getString("guid")));
        }
    }

    private void readDoughPreparationTable(JSONObject jsoEquipment) {
        vDoughPrepTable = new Vector<>();
        Iterator<Object> iDoughPrepTables = jsoEquipment.getJSONArray("doughPrepTables").iterator();
        while(iDoughPrepTables.hasNext()){
            JSONObject jsoDoughPrepTablenext = (JSONObject) iDoughPrepTables.next();
            vDoughPrepTable.add(new DoughPreparationTable(jsoDoughPrepTablenext.getString("guid")));
        }
    }

    private void readRecipes(JSONObject jsoBakery) {
        jsaProducts = jsoBakery.getJSONArray("products");
        Iterator<Object> iJsaProducts = jsaProducts.iterator();
        hmRecipeProducts = new HashMap<>();
        while(iJsaProducts.hasNext()){
            JSONObject jsoProductInformation = (JSONObject) iJsaProducts.next();
            String sProduct = jsoProductInformation.getString("guid");
            JSONObject jsoRecipe = (JSONObject) jsoProductInformation.get("recipe");
            Iterator iSteps = jsoRecipe.getJSONArray("steps").iterator();
            HashMap<String, Integer> hmRecipe = new HashMap<>();
            while(iSteps.hasNext()){
                JSONObject jsoRecipeInfo = (JSONObject) iSteps.next();
                if (jsoRecipeInfo.getString("action").equals("kneading") || jsoRecipeInfo.getString("action").equals("item preparation") || jsoRecipeInfo.getString("action").equals("resting")) {
                    hmRecipe.put(jsoRecipeInfo.getString("action"), jsoRecipeInfo.getInt("duration"));
                }
            }
            hmRecipeProducts.put(sProduct, hmRecipe);
        }
    }

    private class receiveOrder extends CyclicBehaviour {
        boolean isDone = false;
        @Override
        public void action() {
//            @ToDo finished() aufrufen und isAllowedAction() aufrufen
//            if (!getAllowAction()) {
//                return;
//            }
                finished();
                MessageTemplate acceptedProposalMT = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);
                ACLMessage aclReceive = receive(acceptedProposalMT);
                if(aclReceive != null ){
                    String sContent = aclReceive.getContent();
                    Vector<BakedGood> bakedGoods = new Vector<BakedGood>();
                    JSONArray jsaArray = null;
                    JSONObject jsoObject = null;
                    jsaArray = new JSONArray(sContent);
                    Iterator<Object> iArray = jsaArray.iterator();
                    while (iArray.hasNext()) {
                        jsoObject = (JSONObject) iArray.next();
                        JSONObject jsoDeliveryDate = jsoObject.getJSONObject("deliveryDate");
                        JSONObject jsoOrderDate = jsoObject.getJSONObject("orderDate");
                        JSONObject jsoProducts = jsoObject.getJSONObject("products");
                        Iterator<String> iKeys = jsoProducts.keys();
                        while(iKeys.hasNext()){
                            String sNext = iKeys.next();
                            jsoProducts.get(sNext);
                            bakedGoods.add(new BakedGood(sNext, (Integer) jsoProducts.get(sNext)));
                        }
                        order = new Order(jsoObject.getString("customerId"), jsoObject.getString("guid"), jsoOrderDate.getInt("day"), jsoOrderDate.getInt("hour"), jsoDeliveryDate.getInt("day"), jsoDeliveryDate.getInt("hour"), bakedGoods);
                        calculateKneadingAndPreparationTime(bakedGoods);
                        calculatePreaparationTime(bakedGoods);
                        new JSONArray(bakedGoods);
//                        myAgent.addBehaviour(new receiveOrder());
//                        isDone = true;
                    }
                    bakedGoods = null;
                    order = null;
                } else {
                    block();
                }

        }

        private void calculatePreaparationTime(Vector<BakedGood> bakedGoods) {
            HashMap<String, Integer> hmPreparedProducts = new HashMap<String, Integer>();
//            hmPreparedProducts.
            int iSize = bakedGoods.size();
            int iCurrentSize = 0;
            while (iCurrentSize < iSize) {
                Iterator<BakedGood> iBakedGoods = bakedGoods.iterator();
                while(iBakedGoods.hasNext()){
                    BakedGood bgNext = iBakedGoods.next();
                    HashMap<String, Integer> hmDuration = hmRecipeProducts.get(bgNext.getName());
                    if(hmPreparedProducts.get(bgNext.getName()) == null){
                        Iterator<org.team_pjt.objects.DoughPreparationTable> iDoughPrepTables = vDoughPrepTable.iterator();
                        while(iDoughPrepTables.hasNext()){
                            org.team_pjt.objects.DoughPreparationTable dptNext = iDoughPrepTables.next();
                            if(!(dptNext.isBusy())){
                                if (dptNext.getsCurrentPreparedProduct() != null) {
                                    hmPreparedProducts.put(bgNext.getName(), 1);
                                    iCurrentSize ++;
                                    if(iCurrentSize == iSize)
                                        break;
                                }
                                dptNext.setiAmountOfItem(bgNext.getAmount());
                                dptNext.setiRestingTime(hmDuration.get("resting"));
                                dptNext.setsCurrentPreparedProduct(bgNext.getName());
                                Thread t = new Thread(dptNext);
                                t.start();
                            }
                        }
                        if(iCurrentSize == iSize)
                            break;
                    }
                }
            }

        }

        private void calculateKneadingAndPreparationTime(Vector<BakedGood> bakedGoods) {
            HashMap<String, Integer> hmBakedProducts = new HashMap<String, Integer>();

            int iSize = bakedGoods.size();
            int iCurrentSize = 0;
            while (iCurrentSize < iSize) {
                Iterator<BakedGood> iBakedGoods = bakedGoods.iterator();
                while(iBakedGoods.hasNext()){
                    BakedGood bgNext = iBakedGoods.next();
                    HashMap<String, Integer> hmDuration = hmRecipeProducts.get(bgNext.getName());
                    Iterator<org.team_pjt.objects.KneadingMachine> iKneadingMachines = vKneadingMachine.iterator();
                    if (hmBakedProducts.get(bgNext.getName()) == null) {
                        while(iKneadingMachines.hasNext()){
                            org.team_pjt.objects.KneadingMachine kmNext = iKneadingMachines.next();
                            if (!(kmNext.isbBusy()) ){
                                if (kmNext.getsCurrentKneadedProduct() != null) {
                                    hmBakedProducts.put(kmNext.getsCurrentKneadedProduct(), 1);
                                    iCurrentSize++;
                                    if(iCurrentSize == iSize)
                                        break;
                                }
                                kmNext.setiKneadingTime(hmDuration.get("kneading"));
                                kmNext.setiRestingTime(hmDuration.get("resting"));
                                kmNext.setsCurrentKneadedProduct(bgNext.getName());
                                Thread t = new Thread(kmNext);
                                t.start();
    //                            bakedGoods.remove(bgNext);
    //                            iBakedGoods = bakedGoods.iterator();
                                break;
                            }
                        }
                    }
                    if(iCurrentSize == iSize)
                        break;
                }
            }
        }
//        public boolean done() {
//            if(isDone){
//                finished();
//                return isDone;
//            }
//            return false;
//        }
    }

//    protected void takeDown() {
//        System.out.println(getAID().getLocalName() + ": Terminating.");
//        this.deRegister();
//    }
//
//    public void queueOrder(Order order) {
//        // Add productStatus to the needsKneading WorkQueue
//
//        for(BakedGood bakedGood : order.getBakedGoods()) {
//
//            String guid = order.getGuid();
//            String status = NEEDS_KNEADING;
//            int amount = bakedGood.getAmount();
//            Product product = bakery.findProduct(bakedGood.getName());
//            ProductStatus productStatus = new ProductStatus(guid, status, amount, product);
//
//            needsKneading.addProduct(productStatus);
//
//        }
//    }
//
//    public KneadingRequest createKneadingRequestMessage() {
//        // Checks the needsKneading workqueue and creates a KneadingRequestMessage
//
//        Vector<ProductStatus> products = needsKneading.getProductBatch();
//        KneadingRequest kneadingRequest = null;
//
//        if (products != null) {
//
//            Vector<String> guids = new Vector<String>();
//
//            for (ProductStatus productStatus : products) {
//                guids.add(productStatus.getGuid());
//
//            }
//            String productType = products.get(0).getProduct().getGuid();
//            float kneadingTime = products.get(0).getProduct().getRecipe().getActionTime(Step.KNEADING_TIME);
//
//            kneadingRequest = new KneadingRequest(guids, productType, kneadingTime);
//        }
//
//        return kneadingRequest;
//    }
//
//    public void queuePreparation(String productType, Vector<String> guids ) {
//        // Add productStatus to the needsPreparation WorkQueue
//
//        for (String guid : guids) {
//
//            int amount = -1;
//            String status = NEEDS_PREPARATION;
//            Product product = bakery.findProduct(productType);
//            Order order = orders.get(guid);
//
//            for(BakedGood bakedGood : order.getBakedGoods()) {
//                if (bakedGood.getName().equals(productType)) {
//                    amount = bakedGood.getAmount();
//                }
//
//            }
//            ProductStatus productStatus = new ProductStatus(guid, status, amount, product);
//            needsPreparation.addProduct(productStatus);
//        }
//    }
//
//    public PreparationRequest createPreparationRequestMessage() {
//        // Checks the needsPreparaion WorkQueue and creates a preparationRequestMessage
//        Vector<ProductStatus> products = needsPreparation.getProductBatch();
//
//        PreparationRequest preparationRequest = null;
//
//        if (products != null) {
//
//            Vector<String> guids = new Vector<String>();
//            Vector<Integer> productQuantities = new Vector<Integer>();
//            Vector<Step> steps = new Vector<Step>();
//
//
//
//            for (ProductStatus productStatus : products) {
//                guids.add(productStatus.getGuid());
//                productQuantities.add(productStatus.getAmount());
//            }
//
//            String productType = products.get(0).getProduct().getGuid();
//            steps = products.get(0).getProduct().getRecipe().getPreparationSteps();
//
//            preparationRequest = new PreparationRequest(guids, productType, productQuantities, steps);
//        }
//
//        return preparationRequest;
//
//    }
//
//    public void queueProofing(String productType, Vector<String> guids ) {
//        // Add productStatus to the needsProofing WorkQueue
//
//        for (String guid : guids) {
//
//            int amount = -1;
//            String status = NEEDS_PROOFING;
//            Product product = bakery.findProduct(productType);
//            Order order = orders.get(guid);
//
//            for(BakedGood bakedGood : order.getBakedGoods()) {
//                if (bakedGood.getName().equals(productType)) {
//                    amount = bakedGood.getAmount();
//                }
//
//            }
//            ProductStatus productStatus = new ProductStatus(guid, status, amount, product);
//            needsProofing.addProduct(productStatus);
//        }
//    }
//
//    public ProofingRequest createProofingRequestMessage() {
//        // Checks the needsProofing WorkQueue and creates a proofingRequestMessage
//        Vector<ProductStatus> products = needsProofing.getProductBatch();
//
//        ProofingRequest proofingRequest = null;
//
//        if (products != null) {
//
//            Vector<String> guids = new Vector<String>();
//            Vector<Integer> productQuantities = new Vector<Integer>();
//
//            for (ProductStatus productStatus : products) {
//                guids.add(productStatus.getGuid());
//                productQuantities.add(productStatus.getAmount());
//            }
//
//            String productType = products.get(0).getProduct().getGuid();
//
//            float proofingTime = products.get(0).getProduct().getRecipe().getActionTime(Step.PROOFING_TIME);
//
//            proofingRequest = new ProofingRequest(productType, guids, proofingTime, productQuantities);
//        }
//
//        return proofingRequest;
//
//    }
//
//    public void getbakery(){
//
//        String jsonDir = "src/main/resources/config/dough_stage_communication/";
//        try {
//            System.out.println("Working Directory = " + System.getProperty("user.dir"));
//            String bakeryFile = new Scanner(new File(jsonDir + "bakery.json")).useDelimiter("\\Z").next();
//            Vector<Bakery> bakeries = JSONConverter.parseBakeries(bakeryFile);
//            for (Bakery bakery : bakeries)
//            {
//                this.bakery = bakery;
//            }
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//
//    public void getOrderProcessingAIDs() {
//        DFAgentDescription template = new DFAgentDescription();
//        ServiceDescription sd = new ServiceDescription();
//
//        sd.setType("Order-processing");
//        template.addServices(sd);
//        try {
//            DFAgentDescription [] result = DFService.search(this, template);
//            System.out.println("Found the following Order-processing agents:");
//            orderProcessingAgents = new AID [result.length];
//
//            for (int i = 0; i < result.length; ++i) {
//                orderProcessingAgents[i] = result[i].getName();
//                System.out.println(orderProcessingAgents[i].getName());
//            }
//
//        }
//        catch (FIPAException fe) {
//            fe.printStackTrace();
//        }
//    }
//
//    public void getProoferAIDs() {
//        DFAgentDescription template = new DFAgentDescription();
//        ServiceDescription sd = new ServiceDescription();
//
//        sd.setType("Proofer");
//        template.addServices(sd);
//        try {
//            DFAgentDescription [] result = DFService.search(this, template);
//            System.out.println("Found the following Proofer agents:");
//            prooferAgents = new AID [result.length];
//
//            for (int i = 0; i < result.length; ++i) {
//                prooferAgents[i] = result[i].getName();
//                System.out.println(prooferAgents[i].getName());
//            }
//
//        }
//        catch (FIPAException fe) {
//            fe.printStackTrace();
//        }
//    }
//
//    public void getPreparationTableAIDS() {
//        DFAgentDescription template = new DFAgentDescription();
//        ServiceDescription sd = new ServiceDescription();
//
//        sd.setType("Preparation-table");
//        template.addServices(sd);
//        try {
//            DFAgentDescription [] result = DFService.search(this, template);
//            System.out.println("Found the following Preparation-table agents:");
//            preparationTableAgents = new AID [result.length];
//
//            for (int i = 0; i < result.length; ++i) {
//                preparationTableAgents[i] = result[i].getName();
//                System.out.println(preparationTableAgents[i].getName());
//            }
//
//        }
//        catch (FIPAException fe) {
//            fe.printStackTrace();
//        }
//    }
//
//    public void getKneadingMachineAIDs() {
//        DFAgentDescription template = new DFAgentDescription();
//        ServiceDescription sd = new ServiceDescription();
//
//        sd.setType("Kneading-machine");
//        template.addServices(sd);
//        try {
//            DFAgentDescription [] result = DFService.search(this, template);
//            System.out.println("Found the following Kneading-machine agents:");
//            kneadingMachineAgents = new AID [result.length];
//
//            for (int i = 0; i < result.length; ++i) {
//                kneadingMachineAgents[i] = result[i].getName();
//                System.out.println(kneadingMachineAgents[i].getName());
//            }
//
//        }
//        catch (FIPAException fe) {
//            fe.printStackTrace();
//        }
//    }
//
//    /* This is the behavior used for receiving orders */
//    private class ReceiveOrders extends CyclicBehaviour {
//        public void action() {
//            // baseAgent.finished(); //call it if there are no generic behaviours
//            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
//            ACLMessage msg = myAgent.receive(mt);
//            if (msg != null) {
//                ACLMessage reply = msg.createReply();
//                reply.setPerformative(ACLMessage.CONFIRM);
//                reply.setContent("Order was received");
//                baseAgent.sendMessage(reply);
//                // TODO convert String to order object
//                // Add the order to the HashMap of orders. Trigger the doughPreparation (send Kneading request, etc)
//
//            }
//            else {
//                block();
//            }
//        }
//    }
//
//    /* This is the behavior used for receiving kneading notification messages */
//    private class ReceiveKneadingNotification extends CyclicBehaviour {
//        public void action() {
//
//            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
//                MessageTemplate.MatchConversationId("kneading-notification"));
//
//            ACLMessage msg = baseAgent.receive(mt);
//
//            if (msg != null) {
//
//                System.out.println("-------> " + getAID().getLocalName()+" Received Kneading Notification from " + msg.getSender());
//
//                String kneadingNotificationString = msg.getContent();
//
//                System.out.println("-----> Kneading notification " + kneadingNotificationString);
//
//                ACLMessage reply = msg.createReply();
//
//                reply.setPerformative(ACLMessage.CONFIRM);
//
//                reply.setContent("Kneading Notification was received");
//
//                baseAgent.sendMessage(reply);
//
//                // Convert kneadingNotificationString to kneadingNotification object
//                KneadingNotification kneadingNotification = JSONConverter.parseKneadingNotification(kneadingNotificationString);
//
//                String productType = kneadingNotification.getProductType();
//
//                Vector<String> guids = kneadingNotification.getGuids();
//
//                System.out.println("-----> product type " + productType);
//
//                System.out.println("-----> guid " + guids);
//
//                // Add guids with this productType to the queuePreparation
//                queuePreparation(productType, guids);
//
//                // Create preparationRequestMessage with the information in the queuePreparation
//                PreparationRequest preparationRequestMessage = createPreparationRequestMessage();
//
//                // Convert preparationRequestMessage to String
//                Gson gson = new Gson();
//
//                String preparationRequestString = gson.toJson(preparationRequestMessage);
//
//                // Send preparationRequestMessage
//                addBehaviour(new RequestPreparation(preparationRequestString, preparationTableAgents));
//
//            }
//            else {
//                block();
//            }
//        }
//    }
//
//    /* This is the behaviour used for receiving preparation notification */
//    private class ReceivePreparationNotification extends CyclicBehaviour {
//        public void action() {
//            // baseAgent.finished(); //call it if there are no generic behaviours
//
//            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
//                MessageTemplate.MatchConversationId("preparation-notification"));
//
//            ACLMessage msg = baseAgent.receive(mt);
//
//            if (msg != null) {
//
//                System.out.println("-------> " + getAID().getLocalName()+" Received Preparation Notification from " + msg.getSender());
//
//                String preparationNotificationString = msg.getContent();
//
//                ACLMessage reply = msg.createReply();
//
//                reply.setPerformative(ACLMessage.CONFIRM);
//
//                reply.setContent("Preparation Notification was received");
//
//                baseAgent.sendMessage(reply);
//
//                // Convert preparationNotificationString to preparationNotification object
//
//                PreparationNotification preparationNotification = JSONConverter.parsePreparationNotification(preparationNotificationString);
//
//                String productType = preparationNotification.getProductType();
//                Vector<String> guids = preparationNotification.getGuids();
//
//                // Add guids with this productType to the queueProofing
//                queueProofing(productType, guids);
//
//                // Create proofingRequestMessage with the information in the queueProofing
//                ProofingRequest proofingRequestMessage = createProofingRequestMessage();
//
//                // Convert proofingRequestMessage to String
//                Gson gson = new Gson();
//                String proofingRequestString = gson.toJson(proofingRequestMessage);
//
//                // Send preparationRequestMessage
//                addBehaviour(new RequestProofing(proofingRequestString, prooferAgents));
//
//
//            }
//            else {
//                block();
//            }
//        }
//
//
//    }
//
//    //This is the behaviour used for sensing a KneadingRequest
//    private class RequestKneading extends Behaviour{
//        private String kneadingRequest;
//        private AID [] kneadingMachineAgents;
//        private MessageTemplate mt;
//        // private ACLMessage msg;
//        private int step = 0;
//
//        public RequestKneading(String kneadingRequest, AID [] kneadingMachineAgents){
//            this.kneadingRequest = kneadingRequest;
//            this.kneadingMachineAgents = kneadingMachineAgents;
//        }
//        public void action(){
//            //blocking action
//            if (!baseAgent.getAllowAction()) {
//                return;
//            }
//            switch(step){
//            case 0:
//
//                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//                msg.setContent(kneadingRequest);
//                msg.setConversationId("kneading-request");
//
//                // Send kneadingRequest msg to all kneadingMachineAgents
//                for (int i=0; i<kneadingMachineAgents.length; i++){
//                    msg.addReceiver(kneadingMachineAgents[i]);
//                }
//                msg.setReplyWith("msg"+System.currentTimeMillis());
//                baseAgent.sendMessage(msg);  // calling sendMessage instead of send
//
//                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("kneading-request"),
//                        MessageTemplate.MatchInReplyTo(msg.getReplyWith()));
//
//                System.out.println(getLocalName()+" Sent kneadingRequest" + kneadingRequest);
//                step = 1;
//                break;
//
//            default:
//                break;
//            }
//        }
//        public boolean done(){
//            // baseAgent.finished();
//            return step == 1;
//        }
//    }
//
//    //This is the behaviour used for sending a PreparationRequest
//    private class RequestPreparation extends Behaviour{
//        private String preparationRequest;
//        private AID [] preparationTableAgents;
//        private MessageTemplate mt;
//        private ACLMessage msg;
//        private int step = 0;
//
//        public RequestPreparation(String preparationRequest, AID [] preparationTableAgents){
//            this.preparationRequest = preparationRequest;
//            this.preparationTableAgents = preparationTableAgents;
//        }
//        public void action(){
//            //blocking action
//            if (!baseAgent.getAllowAction()) {
//                return;
//            }
//            switch(step){
//            case 0:
//                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//                msg.setContent(preparationRequest);
//                msg.setConversationId("preparation-request");
//                // Send kneadingRequest msg to all preparationTableAgents
//                for (int i=0; i<preparationTableAgents.length; i++){
//                    msg.addReceiver(preparationTableAgents[i]);
//                }
//
//                msg.setReplyWith("msg"+System.currentTimeMillis());
//
//                baseAgent.sendMessage(msg);  // calling sendMessage instead of send
//
//                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("preparation-request"),
//                        MessageTemplate.MatchInReplyTo(msg.getReplyWith()));
//
//                System.out.println(getLocalName()+" Sent preparationRequest" + preparationRequest);
//
//                step = 1;
//
//                break;
//            case 1:
//                ACLMessage reply = baseAgent.receive(mt);
//
//                if (reply != null) {
//
//                    if (reply.getPerformative() == ACLMessage.CONFIRM) {
//                        System.out.println(getAID().getLocalName() + " Received confirmation from " + reply.getSender());
//                        step = 2;
//                    }
//                }
//                else {
//                    block();
//                }
//                break;
//
//            default:
//                break;
//            }
//        }
//        public boolean done(){
//            if (step == 2){
//                baseAgent.finished();
//                return true;
//
//            }
//            return false;
//        }
//    }
//
//    // This is the behavior used for sensing a ProofingRequest
//    private class RequestProofing extends Behaviour{
//        private String proofingRequest;
//        private AID [] prooferAgents;
//        private MessageTemplate mt;
//        private ACLMessage msg;
//        private int step = 0;
//
//        public RequestProofing(String proofingRequest, AID [] prooferAgents){
//            this.proofingRequest = proofingRequest;
//            this.prooferAgents = prooferAgents;
//        }
//        public void action(){
//            //blocking action
//            if (!baseAgent.getAllowAction()) {
//                return;
//            }
//            switch(step){
//            case 0:
//                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//                msg.setContent(proofingRequest);
//                msg.setConversationId("proofing-request");
//                // Send proofingRequest msg to all prooferAgents
//                for (int i=0; i<prooferAgents.length; i++){
//                    msg.addReceiver(prooferAgents[i]);
//                }
//                msg.setReplyWith("msg"+System.currentTimeMillis());
//                baseAgent.sendMessage(msg);  // calling sendMessage instead of send
//                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("proofing-request"),
//                        MessageTemplate.MatchInReplyTo(msg.getReplyWith()));
//
//                System.out.println(getLocalName()+" Sent proofingRequest" + proofingRequest);
//                step = 1;
//                break;
//
//            default:
//                break;
//            }
//        }
//        public boolean done(){
//            if (step == 1){
//                baseAgent.finished();
//                // For now the DoughManager terminates after processing one order
//                baseAgent.doDelete();
//                return true;
//
//            }
//            return false;
//        }
//    }

}
