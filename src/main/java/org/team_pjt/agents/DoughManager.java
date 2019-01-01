package org.team_pjt.agents;

import com.google.gson.Gson;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.team_pjt.Objects.BakedGood;
import org.team_pjt.Objects.KneadingPreparingMachine;
import org.team_pjt.Objects.OrderDoughPrep;
import org.team_pjt.messages.ProofingRequest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

//import org.team_pjt.doughprep.mas_maas.agents.BaseAgent;


public class DoughManager extends BaseAgent {
    private AID [] prooferAgents;
    private Vector<OrderDoughPrep> vOrder;
    private String sIdBakery;
    private Vector<KneadingPreparingMachine> vKneadingPreparingMachine;
    private Vector<KneadingPreparingMachine> vDoughPrepTable;
    private Vector<OrderDoughPrep> vPreparationOrders;
    private JSONArray jsaProducts;
    private HashMap<String, HashMap<String, Integer>> hmRecipeProducts;
    private AID aidSchedulerAgent;
    private OrderDoughPrep odpCurrentKneadedOrder;
    private OrderDoughPrep odpCurrentPreparedOrder;
    private HashMap<String, Integer> hmBakedProducts;
    private HashMap<String, Integer> hmPreparedProducts;
    private HashMap<String, Integer> hmSetTimeStep;
    private HashMap<String, Integer> hmPrepTableTimeStep;
    private JSONObject jsoPreparedItems;
    protected void setup() {
        super.setup();
        Object[] oArguments = getArguments();
        if (!readArgs(oArguments)) {
            System.out.println("No parameters given for DoughManager" + getName());
        }
        vOrder = new Vector<>();
        vPreparationOrders = new Vector<>();
        hmBakedProducts = new HashMap<String, Integer>();
        hmPreparedProducts = new HashMap<>();
        hmSetTimeStep = new HashMap<>();
        hmPrepTableTimeStep = new HashMap<>();
        jsoPreparedItems = new JSONObject();
        getProoferAIDs();
        findScheduler();
        System.out.println(getAID().getLocalName() + " is ready.");
        this.register("Dough-manager", "JADE-bakery");
        addBehaviour(new receiveOrder());
    }

    private void findScheduler() {
        DFAgentDescription[] dfSchedulerAgentResult = new DFAgentDescription[0];
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName("scheduler-"+sIdBakery.split("-")[1]);
        template.addServices(sd);
        while (dfSchedulerAgentResult.length == 0) {
            try {
                dfSchedulerAgentResult = DFService.search(this, template);
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }
        aidSchedulerAgent = dfSchedulerAgentResult[0].getName();
        System.out.println("Scheduler agent for " + getName() + " found! - " + aidSchedulerAgent);
    }

    private boolean readArgs(Object [] o) {
        if(o!= null){
            vKneadingPreparingMachine = new Vector<>();
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
            vKneadingPreparingMachine.add(new KneadingPreparingMachine(jsoKneadingMachine.getString("guid")));
        }
    }

    private void readDoughPreparationTable(JSONObject jsoEquipment) {
        vDoughPrepTable = new Vector<>();
        Iterator<Object> iDoughPrepTables = jsoEquipment.getJSONArray("doughPrepTables").iterator();
        while(iDoughPrepTables.hasNext()){
            JSONObject jsoDoughPrepTablenext = (JSONObject) iDoughPrepTables.next();
            vDoughPrepTable.add(new KneadingPreparingMachine(jsoDoughPrepTablenext.getString("guid")));
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
                if (jsoRecipeInfo.getString("action").equals("proofing") || jsoRecipeInfo.getString("action").equals("resting") || jsoRecipeInfo.getString("action").equals("sheeting") || jsoRecipeInfo.getString("action").equals("twisting") || jsoRecipeInfo.getString("action").equals("filling") || jsoRecipeInfo.getString("action").equals("kneading") || jsoRecipeInfo.getString("action").equals("item preparation") || jsoRecipeInfo.getString("action").equals("resting")) {
                    hmRecipe.put(jsoRecipeInfo.getString("action"), jsoRecipeInfo.getInt("duration"));
                }
            }
            hmRecipeProducts.put(sProduct, hmRecipe);
        }
    }

    private class receiveOrder extends CyclicBehaviour {
        private int iKneadingSize = 0;
        private int iCurrentKneadingSize = 0;
        private int iPreparingSize = 0;
        private int iCurrentPreparingSize = 0;
        private JSONObject jsoProofedProducts;
//        boolean isDone = false;
        @Override
        public void action() {
//                if(!getAllowAction()){
//                    return;
//                }
                MessageTemplate acceptedProposalMT = MessageTemplate.and((MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE)), MessageTemplate.MatchSender(aidSchedulerAgent));
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
//                        System.out.println("Order is: " + jsoObject.toString());
                        vOrder.add(new OrderDoughPrep(jsoObject.getString("customerId"), jsoObject.getString("guid"), jsoOrderDate.getInt("day"), jsoOrderDate.getInt("hour"), jsoDeliveryDate.getInt("day"), jsoDeliveryDate.getInt("hour"), bakedGoods));
//                        jsoProofedProducts.toString();
                    }
                    checkWhetherKneadingSizeIsEqual();
                    calculateKneadingTime(odpCurrentKneadedOrder.getBakedGoods(), false);
                    checkWhetherKneadingSizeIsEqual();
                    checkWhetherPreparingSizeIsEqual();
                    if (odpCurrentPreparedOrder != null) {
                        jsoProofedProducts = calculatePreaparationTime(odpCurrentPreparedOrder.getBakedGoods(), false);
                    }
                    checkWhetherPreparingSizeIsEqual();
                    finished();

                }
                else {
                    if (odpCurrentKneadedOrder != null) {
                        calculateKneadingTime(odpCurrentKneadedOrder.getBakedGoods(), false);
                        checkWhetherKneadingSizeIsEqual();
                        checkWhetherPreparingSizeIsEqual();
                        if (odpCurrentPreparedOrder != null) {
                            jsoProofedProducts = calculatePreaparationTime(odpCurrentPreparedOrder.getBakedGoods(), false);
                        }
                        checkWhetherPreparingSizeIsEqual();
                    }
                    finished();
                    block();
                }

        }

        private void checkWhetherPreparingSizeIsEqual() {
            if(iCurrentPreparingSize == iPreparingSize){
                if(odpCurrentPreparedOrder != null && jsoProofedProducts != null){
//                    if(){
                        Iterator<String> iProofedKeys = jsoProofedProducts.keys();
                        while(iProofedKeys.hasNext()){
                            String sGuid = iProofedKeys.next();
                            Integer iQuantiti = (Integer) jsoProofedProducts.get(sGuid);
                            Float fProofingTime = Float.valueOf(hmRecipeProducts.get(sGuid).get("proofing"));
                            Vector<String> vGuids = new Vector<String>();
                            vGuids.add(odpCurrentPreparedOrder.getGuid());
                            Vector<Integer> vProductQuantities = new Vector<>();
                            vProductQuantities.add(iQuantiti);
//                            ProofingRequest prRequest = null;
                            ProofingRequest prRequest = new ProofingRequest(sGuid,vGuids,fProofingTime,vProductQuantities );
                            Gson gson = new Gson();
                            String sProofingMesssage = gson.toJson(prRequest);
                            sendProofingRequest(sProofingMesssage);
                        }
//                    }
                }
                if(vPreparationOrders != null && vPreparationOrders.size() != 0){
                    odpCurrentPreparedOrder = vPreparationOrders.firstElement();
                    vPreparationOrders.remove(odpCurrentPreparedOrder);
                }
            }

        }

        private void checkWhetherKneadingSizeIsEqual() {
            if (iCurrentKneadingSize == iKneadingSize){
                if (odpCurrentKneadedOrder != null) {
                    vPreparationOrders.add(odpCurrentKneadedOrder);
                    vOrder.remove(odpCurrentKneadedOrder);
                    System.out.println(odpCurrentKneadedOrder.getGuid() + "wurde entfernt");
                }
                if (vOrder.iterator().hasNext()) {
                    odpCurrentKneadedOrder = vOrder.firstElement();
                }
            }
        }

        private JSONObject calculatePreaparationTime(Vector<BakedGood> bakedGoods, boolean bPreparing) {
            if (iCurrentPreparingSize == iPreparingSize) {
                hmPreparedProducts.clear();
                iPreparingSize = bakedGoods.size();
                jsoPreparedItems = new JSONObject();
                iCurrentPreparingSize = 0;
            }
//            while (iCurrentPreparingSize < iPreparingSize) {
//                finished();
                if (getAllowAction()) {
                    Iterator<BakedGood> iBakedGoods = bakedGoods.iterator();
                    while (iBakedGoods.hasNext()) {
                        BakedGood bgNext = iBakedGoods.next();
                        HashMap<String, Integer> hmDuration = hmRecipeProducts.get(bgNext.getName());
                        if (hmPreparedProducts.get(bgNext.getName()) == null) {
                            Iterator<KneadingPreparingMachine> iDoughPrepTables = vDoughPrepTable.iterator();
                            while (iDoughPrepTables.hasNext()) {
                                KneadingPreparingMachine dptNext = iDoughPrepTables.next();
                                if (!(dptNext.isbBusy())) {
                                    if (dptNext.getsCurrentKneadedProduct() != null) {
                                        if (hmPreparedProducts.get(dptNext.getsCurrentKneadedProduct()) == null) {
                                            hmPreparedProducts.put(dptNext.getsCurrentKneadedProduct(), 1);
                                            jsoPreparedItems.put(dptNext.getsCurrentKneadedProduct(), dptNext.getiAmount());
                                            iCurrentPreparingSize++;
                                            if (iCurrentPreparingSize == iPreparingSize)
                                                break;
                                        }
                                    }
                                    if (hmPreparedProducts.get(bgNext.getName()) == null && !bPreparing) {
                                        setKneadingTime(bgNext,hmDuration, dptNext, "item preparation", bgNext.getAmount());
                                        break;
                                    }
                                }
                                if (dptNext.getsCurrentKneadedProduct() != null) {
                                    dptNext.setiWorkTime();
                                    dptNext.checkKneadingMachineState(getName());
                                    hmPrepTableTimeStep.put(dptNext.getsCurrentKneadedProduct(),1);
                                }
                            }

                            if (iCurrentPreparingSize == iPreparingSize)
                                hmPrepTableTimeStep.clear();
                                break;
                        }
                    }
                }
            hmPrepTableTimeStep.clear();
//            }
            return jsoPreparedItems;

        }

        private void calculateKneadingTime(Vector<BakedGood> bakedGoods, boolean bPreparing) {

            if (iCurrentKneadingSize == iKneadingSize) {
                hmBakedProducts.clear();
                iKneadingSize = bakedGoods.size();
                iCurrentKneadingSize = 0;
            }
                if (getAllowAction()) {
                    Iterator<BakedGood> iBakedGoods = bakedGoods.iterator();
                    while (iBakedGoods.hasNext()) {
                        BakedGood bgNext = iBakedGoods.next();
                        HashMap<String, Integer> hmDuration = hmRecipeProducts.get(bgNext.getName());
                        Iterator<KneadingPreparingMachine> iKneadingMachines = vKneadingPreparingMachine.iterator();
                        if (hmBakedProducts.get(bgNext.getName()) == null) {
                            while (iKneadingMachines.hasNext()) {
                                KneadingPreparingMachine kmNext = iKneadingMachines.next();
                                if (!(kmNext.isbBusy())) {
                                    if (kmNext.getsCurrentKneadedProduct() != null) {
                                        if (hmBakedProducts.get(kmNext.getsCurrentKneadedProduct()) == null) {
                                            hmBakedProducts.put(kmNext.getsCurrentKneadedProduct(), 1);
                                            iCurrentKneadingSize++;
                                            if (iCurrentKneadingSize == iKneadingSize)
                                                break;
                                        }
                                    }
                                    if (hmBakedProducts.get(bgNext.getName()) == null && !bPreparing) {
                                        setKneadingTime(bgNext, hmDuration, kmNext, "item", 1);
                                        break;
                                    }
                                }
                                if (kmNext.getsCurrentKneadedProduct() != null) {
                                    if (hmSetTimeStep.get(kmNext.getsCurrentKneadedProduct()) == null) {
                                        kmNext.setiWorkTime();
                                        kmNext.checkKneadingMachineState(getName());
                                        hmSetTimeStep.put(kmNext.getsCurrentKneadedProduct(), 1);
                                    }
                                }
                            }
                        }
                        if (iCurrentKneadingSize == iKneadingSize)
                            {
                                hmSetTimeStep.clear();
                                break;
                            }
                    }
            }
            hmSetTimeStep.clear();
        }

        private void setKneadingTime(BakedGood bgNext, HashMap<String, Integer> hmDuration, KneadingPreparingMachine kmNext, String sItem, int iAmount) {
            Set<String> sStrings = hmDuration.keySet();
            Iterator<String> sIterator = sStrings.iterator();
            if (sItem.equals("item")) {
                while (sIterator.hasNext()) {
                    String sTmp = sIterator.next();
    //                String sItem = "item";
                    if (!(sTmp.contains(sItem))) {
                        kmNext.setvTime(hmDuration.get(sTmp));
                    }
                }
            } else {
                kmNext.setvTime(hmDuration.get(sItem));
            }
            kmNext.setsCurrentKneadedProduct(bgNext.getName());
            kmNext.setiAmount(iAmount);
            kmNext.startKneading(getName());
        }

        private void sendProofingRequest(String sContent) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent(sContent);
            msg.setConversationId("proofing-request");
            // Send proofingRequest msg to all prooferAgents
            for (int i=0; i<prooferAgents.length; i++){
                msg.addReceiver(prooferAgents[i]);
            }
            msg.setReplyWith("msg"+System.currentTimeMillis());
            baseAgent.sendMessage(msg);
        }

    }

    public void getProoferAIDs() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Proofer");
        sd.setName("proofer-"+sIdBakery.split("-")[1]);
        template.addServices(sd);
        try {
            DFAgentDescription [] result = DFService.search(this, template);
            System.out.println("Found the following Proofer agents:");
            prooferAgents = new AID [result.length];

            for (int i = 0; i < result.length; ++i) {
                prooferAgents[i] = result[i].getName();
                System.out.println(prooferAgents[i].getName());
            }

        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

}
