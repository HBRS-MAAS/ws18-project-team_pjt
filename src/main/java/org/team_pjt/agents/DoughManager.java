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
import org.team_pjt.Objects.CurrentKneadedProduct;
import org.team_pjt.Objects.KneadingPreparingMachine;
import org.team_pjt.Objects.OrderDoughPrep;
import org.team_pjt.messages.ProofingRequest;

import java.util.*;

//import org.team_pjt.doughprep.mas_maas.agents.BaseAgent;


public class DoughManager extends BaseAgent {
    private AID [] prooferAgents;
    private Vector <CurrentKneadedProduct> vProductsToKnead;
    private Vector<OrderDoughPrep> vOrder;
    private Vector<OrderDoughPrep> vOrderInterm;
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
    private Vector<CurrentKneadedProduct> vAlreadyKneadedOrders;
    private Vector<CurrentKneadedProduct> vAlreadyPreparedOrders;
    private Vector<CurrentKneadedProduct> vProductsToPrepare;
    private HashMap<String, Integer> hmBakedOrder;
    private HashMap<String, Integer> hmPreparedProducts;
    private HashMap<String, Integer> hmSetTimeStep;
    private HashMap<String, Integer> hmPrepTableTimeStep;
    private HashMap<String, Integer> hmDuplicateProductCheck;
    private JSONObject jsoPreparedItems;
    protected void setup() {
        super.setup();
        Object[] oArguments = getArguments();
        if (!readArgs(oArguments)) {
            System.out.println("No parameters given for DoughManager" + getName());
        }
        vProductsToPrepare = new Vector<>();
        vAlreadyKneadedOrders = new Vector<>();
        vProductsToKnead = new Vector<>();
        vAlreadyPreparedOrders = new Vector<>();
        vOrder = new Vector<>();
        vOrderInterm = new Vector<>();
        vPreparationOrders = new Vector<>();
        hmBakedProducts = new HashMap<String, Integer>();
        hmBakedOrder = new HashMap<String, Integer>();
        hmPreparedProducts = new HashMap<>();
        hmSetTimeStep = new HashMap<>();
        hmPrepTableTimeStep = new HashMap<>();
        jsoPreparedItems = new JSONObject();
        getProoferAIDs();
        findScheduler();
        System.out.println(getAID().getLocalName() + " is ready.");
        this.register("Dough-manager", getName().split("@")[0]);
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
        Vector<JSONObject> vIntermjsoobject = new Vector<>();
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
                if(jsoRecipeInfo.getString("action").equals("proofing")){
                    vIntermjsoobject.add(jsoRecipeInfo);
                    break;
                }else {
                    vIntermjsoobject.add(jsoRecipeInfo);
                }
            }
            Iterator<JSONObject> iIterator = vIntermjsoobject.iterator();
            while(iIterator.hasNext()){
                JSONObject jsoNextFinal = iIterator.next();
//                if (jsoNextFinal.getString("action").equals("proofing") || jsoNextFinal.getString("action").equals("resting") || jsoNextFinal.getString("action").equals("sheeting") || jsoNextFinal.getString("action").equals("twisting") || jsoRecipeInfo.getString("action").equals("filling") || jsoRecipeInfo.getString("action").equals("kneading") || jsoRecipeInfo.getString("action").equals("item preparation") || jsoRecipeInfo.getString("action").equals("resting")) {
                 hmRecipe.put(jsoNextFinal.getString("action"), jsoNextFinal.getInt("duration"));
//                }
                hmRecipeProducts.put(sProduct, hmRecipe);
            }
        }
    }

    private class receiveOrder extends CyclicBehaviour {
        @Override
        public void action() {
            if (getAllowAction()) {
                MessageTemplate acceptedProposalMT = MessageTemplate.and((MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE)), MessageTemplate.MatchSender(aidSchedulerAgent));
                ACLMessage aclReceive = receive(acceptedProposalMT);
                if(aclReceive != null ){
                    String sContent = aclReceive.getContent();
                    Vector<BakedGood> bakedGoods = new Vector<BakedGood>();
                    hmDuplicateProductCheck = new HashMap<>();
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
                            if (hmDuplicateProductCheck.get(sNext)== null) {
                                hmDuplicateProductCheck.put(sNext, (Integer)jsoProducts.get(sNext));
                            } else {
                                Integer iAmount = hmDuplicateProductCheck.get(sNext);
                                iAmount = iAmount + (Integer)jsoProducts.get(sNext);
                                hmDuplicateProductCheck.put(sNext, iAmount);
                            }

                        }
                        Set<Map.Entry<String, Integer>> mEntries = hmDuplicateProductCheck.entrySet();
                        Iterator<Map.Entry<String, Integer>> iEntrySet = mEntries.iterator();
                        bakedGoods = new Vector<>();
                        while(iEntrySet.hasNext()){
                            Map.Entry<String, Integer> mNext = iEntrySet.next();
                            bakedGoods.add(new BakedGood(mNext.getKey(),mNext.getValue()));
                        }
                        vOrderInterm.add(new OrderDoughPrep(jsoObject.getString("customerId"), jsoObject.getString("guid"), jsoOrderDate.getInt("day"), jsoOrderDate.getInt("hour"), jsoDeliveryDate.getInt("day"), jsoDeliveryDate.getInt("hour"), bakedGoods));
                    }
                    vOrder = new Vector<>(vOrderInterm);
                    vOrderInterm.clear();
                    if (getCurrentHour() <= 12) {
                        checkWhetherKneadingSizeIsEqual();
                        if (vProductsToKnead.size() > 0) {
                            calculateKneadingTime(vProductsToKnead);
                        }
                        checkWhetherKneadingSizeIsEqual();
                        if (vProductsToPrepare.size() > 0) {
                            calculatePreaparationTime(vProductsToPrepare);
                        }
                    } else {
//                        System.out.println("No production shift");
                    }
                    finished();
                }
                else {
//                    if (odpCurrentKneadedOrder != null) {
                        if (getCurrentHour() < 12) {
                            checkWhetherKneadingSizeIsEqual();
                            if (vProductsToKnead.size() > 0) {
                                calculateKneadingTime(vProductsToKnead);
                            }
                            checkWhetherKneadingSizeIsEqual();
                            if (vProductsToPrepare.size() > 0) {
                                calculatePreaparationTime(vProductsToPrepare);
                            }
                        } else {
//                            System.out.println("No production shift");
                        }
//                    }
                    finished();
                    block();
                }
            }

        }

        private void sendProofingRequest(JSONObject jsoProofedProducts, String sOrderId) {
                        Iterator<String> iProofedKeys = jsoProofedProducts.keys();
                        while(iProofedKeys.hasNext()){
                            String sGuid = iProofedKeys.next();
                            Integer iQuantiti = (Integer) jsoProofedProducts.get(sGuid);
                            Float fProofingTime = Float.valueOf(hmRecipeProducts.get(sGuid).get("proofing"));
                            Vector<String> vGuids = new Vector<String>();
                            vGuids.add(sOrderId);
                            Vector<Integer> vProductQuantities = new Vector<>();
                            vProductQuantities.add(iQuantiti);
                            ProofingRequest prRequest = new ProofingRequest(sGuid,vGuids,fProofingTime,vProductQuantities );
                            Gson gson = new Gson();
                            String sProofingMesssage = gson.toJson(prRequest);
                            sendProofingRequest(sProofingMesssage);
                        }
                }

        private void checkWhetherKneadingSizeIsEqual() {
            boolean bOrderAlreadyExists = false;
            for (OrderDoughPrep odpKneaded: vOrder) {
                if (odpKneaded.getDeliveryDate() == getCurrentDay()) {
                    Iterator<CurrentKneadedProduct> iProductsToKnead = vProductsToKnead.iterator();
                    while (iProductsToKnead.hasNext()){
                        CurrentKneadedProduct ckpNext = iProductsToKnead.next();
                        if(odpKneaded.getGuid().equals(ckpNext.getsOrderId())){
                            bOrderAlreadyExists = true;
                            break;
                        }
                    }
                    if (!bOrderAlreadyExists) {
                        Vector<BakedGood> vBakedGoods = odpKneaded.getBakedGoods();
                        Iterator<BakedGood> iBakedGoods = vBakedGoods.iterator();
                        while(iBakedGoods.hasNext()){
                            BakedGood bgNext = iBakedGoods.next();
                            vProductsToKnead.add(new CurrentKneadedProduct(odpKneaded.getGuid(), bgNext.getName(), bgNext.getAmount(), odpKneaded.getDeliveryDate()));
                        }
                    }
                }
            }
        }

        private void calculatePreaparationTime(Vector<CurrentKneadedProduct> bakedGoods) {
                    Iterator<CurrentKneadedProduct> iBakedGoods = bakedGoods.iterator();
                    while (iBakedGoods.hasNext()) {
                        CurrentKneadedProduct bgNext = iBakedGoods.next();
                        HashMap<String, Integer> hmDuration = hmRecipeProducts.get(bgNext.getsProduct());
                        if (!isbProductOfOrderAlreadyKneaded(false, bgNext, vAlreadyPreparedOrders)) {
                            Iterator<KneadingPreparingMachine> iDoughPrepTables = vDoughPrepTable.iterator();
                            while (iDoughPrepTables.hasNext()) {
                                KneadingPreparingMachine dptNext = iDoughPrepTables.next();
                                if (!(dptNext.isbBusy())) {
                                    if (dptNext.getsCurrentKneadedProduct() != "") {
                                        if (!isbProductOfOrderAlreadyKneaded(false, bgNext, vAlreadyPreparedOrders)) {
                                            vAlreadyPreparedOrders.add(new CurrentKneadedProduct(dptNext.getsCurrentKneadedOrder(), dptNext.getsCurrentKneadedProduct(), dptNext.getiAmount(), -1));
                                            jsoPreparedItems = new JSONObject();
                                            jsoPreparedItems.put(dptNext.getsCurrentKneadedProduct(), dptNext.getiAmount());
                                            sendProofingRequest(jsoPreparedItems, dptNext.getsCurrentKneadedOrder());
                                            dptNext.setsCurrentKneadedOrder("");
                                            dptNext.setsCurrentKneadedProduct("");
                                        }
                                    }
                                    if (!isbProductOfOrderAlreadyKneaded(false, bgNext, vAlreadyPreparedOrders)) {
                                        checkWhetherAlreadyPreparing(bgNext.getiAmount(), "item preparation", bgNext, hmDuration, dptNext, vDoughPrepTable);
                                    }
                                }
                                if (dptNext.getsCurrentKneadedProduct() != "") {
                                    if (hmPrepTableTimeStep.get(dptNext.getsCurrentKneadedOrder()) == null) {
                                        dptNext.setiWorkTime();
                                        dptNext.checkKneadingMachineState(getName());
                                        hmPrepTableTimeStep.put(dptNext.getsCurrentKneadedProduct(),1);
                                    }
                                }
                            }
                        } else {
                            if (bgNext.getiDeliveryDay() < getCurrentDay()) {
                                bakedGoods.remove(bgNext);
                                iBakedGoods = bakedGoods.iterator();
                            }
                        }
                    }
            hmPrepTableTimeStep.clear();
//            return jsoPreparedItems;

        }

        private void calculateKneadingTime(Vector<CurrentKneadedProduct> bakedGoods) {
                    Iterator<CurrentKneadedProduct> iBakedGoods = bakedGoods.iterator();
                    while (iBakedGoods.hasNext()) {
                        CurrentKneadedProduct bgNext = iBakedGoods.next();
                        HashMap<String, Integer> hmDuration = hmRecipeProducts.get(bgNext.getsProduct());
                        if (!isbProductOfOrderAlreadyKneaded(false, bgNext, vAlreadyKneadedOrders)) {
                            Iterator<KneadingPreparingMachine> iKneadingMachines = vKneadingPreparingMachine.iterator();
                            while (iKneadingMachines.hasNext()) {
                                KneadingPreparingMachine kmNext = iKneadingMachines.next();
                                if (!(kmNext.isbBusy())) {
                                    if (kmNext.getsCurrentKneadedProduct() != "") {
                                        if (!isbProductOfOrderAlreadyKneaded(false, bgNext, vAlreadyKneadedOrders)) {
                                            vAlreadyKneadedOrders.add(new CurrentKneadedProduct(kmNext.getsCurrentKneadedOrder(), kmNext.getsCurrentKneadedProduct(), kmNext.getiAmount(), -1));
                                            vProductsToPrepare.add(new CurrentKneadedProduct(kmNext.getsCurrentKneadedOrder(), kmNext.getsCurrentKneadedProduct(), kmNext.getiAmount(), -1));
                                            kmNext.setsCurrentKneadedOrder("");
                                            kmNext.setsCurrentKneadedProduct("");
                                        }
                                    }
                                    if (!isbProductOfOrderAlreadyKneaded(false, bgNext, vAlreadyKneadedOrders)) {
                                        checkWhetherAlreadyPreparing(1, "item", bgNext, hmDuration, kmNext, vKneadingPreparingMachine);
                                    }
                                }
                                if (kmNext.getsCurrentKneadedProduct() != "") {
                                    if (hmSetTimeStep.get(kmNext.getsCurrentKneadedProduct()) == null) {
                                        kmNext.setiWorkTime();
                                        kmNext.checkKneadingMachineState(getName());
                                        hmSetTimeStep.put(kmNext.getsCurrentKneadedProduct(), 1);
                                    }
                                }
                            }
                        } else {
                            if(bgNext.getiDeliveryDay() < getCurrentDay()){
                                bakedGoods.remove(bgNext);
                                iBakedGoods = bakedGoods.iterator();
                            }
                        }
                    }

            hmSetTimeStep.clear();
        }

        private void checkWhetherAlreadyPreparing(Integer iAmount, String sPreparingItem, CurrentKneadedProduct bgNext, HashMap<String, Integer> hmDuration, KneadingPreparingMachine kmNext, Vector<KneadingPreparingMachine> vKneadingPreparingMachine) {
            boolean bAldreadyPreparing = false;
            Iterator<KneadingPreparingMachine> ivkmMachine = vKneadingPreparingMachine.iterator();
            while(ivkmMachine.hasNext()){
                KneadingPreparingMachine kpmNext = ivkmMachine.next();
                if (kpmNext.getsCurrentKneadedProduct().equals(bgNext.getsProduct()) && kpmNext.getsCurrentKneadedOrder().equals(bgNext.getsOrderId())) {
                    bAldreadyPreparing = true;
                }
            }
            if (!bAldreadyPreparing) {
                setKneadingTime(bgNext.getsProduct(), hmDuration, kmNext, sPreparingItem, iAmount, bgNext.getsOrderId());
            }
        }

        private boolean isbProductOfOrderAlreadyKneaded(boolean bProductOfOrderAlreadyPrepared, CurrentKneadedProduct bgNext, Vector<CurrentKneadedProduct> vAlreadyKneadedOrders) {
            Iterator<CurrentKneadedProduct> iVAlreadyPreparedOrders = vAlreadyKneadedOrders.iterator();
            while(iVAlreadyPreparedOrders.hasNext()){
                CurrentKneadedProduct ckpNext = iVAlreadyPreparedOrders.next();
                if(ckpNext.getsProduct().equals(bgNext.getsProduct()) && ckpNext.getsOrderId().equals(bgNext.getsOrderId())){
                    bProductOfOrderAlreadyPrepared = true;}
            }
            return bProductOfOrderAlreadyPrepared;
        }

        private void setKneadingTime(String sCurrentKneadedProduct, HashMap<String, Integer> hmDuration, KneadingPreparingMachine kmNext, String sItem, int iAmount, String sOrder) {
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
            kmNext.setsCurrentKneadedOrder(sOrder);
            kmNext.setsCurrentKneadedProduct(sCurrentKneadedProduct);
            kmNext.setiAmount(iAmount);
            kmNext.startKneading(getName());
        }

        private void sendProofingRequest(String sContent) {
            ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
//            MessageTemplate acceptedProposalMT = MessageTemplate.and((MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE)), MessageTemplate.MatchSender(aidSchedulerAgent));
//            ACLMessage aclReceive = receive(acceptedProposalMT);
            msg.setContent(sContent);
//            msg.setConversationId("proofing-request");
            // Send proofingRequest msg to all prooferAgents
            for (int i=0; i<prooferAgents.length; i++){
                msg.addReceiver(prooferAgents[i]);
            }
            msg.setReplyWith("msg"+System.currentTimeMillis());
//            System.out.println("Proofing Message was sent");
            baseAgent.sendMessage(msg);
            MessageTemplate mtProoferResponse = MessageTemplate.MatchSender(prooferAgents[0]);
            ACLMessage aclReceive = receive(mtProoferResponse);
            if(aclReceive != null){
                if (aclReceive.getPerformative() == 7){
//                    System.out.println("Proof Request was accepted");
                } else {
//                    System.out.println("Proof Request was declined");
                }
            }

        }

    }

    public void getProoferAIDs() {
        boolean bFound = false;
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Proofer_"+sIdBakery);
        sd.setName("JADE-bakery");
        template.addServices(sd);
        try {
            DFAgentDescription [] result = DFService.search(this, template);
            while (result.length == 0) {
                bFound = true;
                result = getDfAgentDescriptions(template);
            }
            if(!bFound){
                result = getDfAgentDescriptions(template);
            }

        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private DFAgentDescription[] getDfAgentDescriptions(DFAgentDescription template) throws FIPAException {
        DFAgentDescription[] result;
        result = DFService.search(this, template);
        System.out.println("Found the following Proofer agents:");
        prooferAgents = new AID[result.length];

        for (int i = 0; i < result.length; ++i) {
            prooferAgents[i] = result[i].getName();
            System.out.println(prooferAgents[i].getName());
        }
        return result;
    }

}
