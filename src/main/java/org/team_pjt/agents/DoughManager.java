package org.team_pjt.agents;

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
//import org.team_pjt.doughprep.mas_maas.agents.BaseAgent;
import org.team_pjt.objects.BakedGood;
import org.team_pjt.objects.DoughPreparationTable;
import org.team_pjt.objects.OrderDoughPrep;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;


public class DoughManager extends BaseAgent {
    private Vector<OrderDoughPrep> vOrder;
    private String sIdBakery;
    private Vector<org.team_pjt.objects.KneadingMachine> vKneadingMachine;
    private Vector<DoughPreparationTable> vDoughPrepTable;
    private JSONArray jsaProducts;
    private HashMap<String, HashMap<String, Integer>> hmRecipeProducts;
    private AID aidSchedulerAgent;
    private int iSize = 0;
    private int iCurrentSize = 0;
    private OrderDoughPrep odpCurrentOrder;
    private HashMap<String, Integer> hmBakedProducts;
    private HashMap<String, Integer> hmSetTimeStep;
    protected void setup() {
        super.setup();
        Object[] oArguments = getArguments();
        if (!readArgs(oArguments)) {
            System.out.println("No parameters given for DoughManager" + getName());
        }
        vOrder = new Vector<>();
        hmBakedProducts = new HashMap<String, Integer>();
        hmSetTimeStep = new HashMap<>();
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
                if (jsoRecipeInfo.getString("action").equals("proofing") || jsoRecipeInfo.getString("action").equals("resting") || jsoRecipeInfo.getString("action").equals("sheeting") || jsoRecipeInfo.getString("action").equals("twisting") || jsoRecipeInfo.getString("action").equals("filling") || jsoRecipeInfo.getString("action").equals("kneading") || jsoRecipeInfo.getString("action").equals("item preparation") || jsoRecipeInfo.getString("action").equals("resting")) {
                    hmRecipe.put(jsoRecipeInfo.getString("action"), jsoRecipeInfo.getInt("duration"));
                }
            }
            hmRecipeProducts.put(sProduct, hmRecipe);
        }
    }

    private class receiveOrder extends CyclicBehaviour {
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
                        System.out.println("Order is: " + jsoObject.toString());
                        vOrder.add(new OrderDoughPrep(jsoObject.getString("customerId"), jsoObject.getString("guid"), jsoOrderDate.getInt("day"), jsoOrderDate.getInt("hour"), jsoDeliveryDate.getInt("day"), jsoDeliveryDate.getInt("hour"), bakedGoods));
                        if (iCurrentSize == iSize) {
                            odpCurrentOrder = vOrder.lastElement();
                            calculateKneadingAndPreparationTime(odpCurrentOrder.getBakedGoods());
                        } else {
                            calculateKneadingAndPreparationTime(odpCurrentOrder.getBakedGoods());
                        }
                        finished();
//                        JSONObject jsoProofedProducts = calculatePreaparationTime(bakedGoods);
//                        jsoProofedProducts.toString();
                    }
                    bakedGoods = null;
//                    vOrder = null;

                }
                else {
                    if (odpCurrentOrder != null) {
                        calculateKneadingAndPreparationTime(odpCurrentOrder.getBakedGoods());
                    }
                    finished();
                    block();
                }

        }

        private JSONObject calculatePreaparationTime(Vector<BakedGood> bakedGoods) {
            HashMap<String, Integer> hmPreparedProducts = new HashMap<String, Integer>();
//            hmPreparedProducts.
            if (iCurrentSize == iSize) {
                iSize = bakedGoods.size();
                iCurrentSize = 0;
            }
            JSONObject jsoPreparedItems = new JSONObject();
//            while (iCurrentSize < iSize) {
//                finished();
                if (getAllowAction()) {
                    Iterator<BakedGood> iBakedGoods = bakedGoods.iterator();
                    while (iBakedGoods.hasNext()) {
                        BakedGood bgNext = iBakedGoods.next();
                        HashMap<String, Integer> hmDuration = hmRecipeProducts.get(bgNext.getName());
                        if (hmPreparedProducts.get(bgNext.getName()) == null) {
                            Iterator<DoughPreparationTable> iDoughPrepTables = vDoughPrepTable.iterator();
                            while (iDoughPrepTables.hasNext()) {
                                DoughPreparationTable dptNext = iDoughPrepTables.next();
                                if (!(dptNext.isBusy())) {
                                    if (dptNext.getsCurrentPreparedProduct() != null) {
                                        if (hmPreparedProducts.get(dptNext.getsCurrentPreparedProduct()) == null) {
                                            hmPreparedProducts.put(dptNext.getsCurrentPreparedProduct(), 1);
                                            jsoPreparedItems.put(dptNext.getsCurrentPreparedProduct(), dptNext.getiAmountOfItem());
                                            iCurrentSize++;
                                            if (iCurrentSize == iSize)
                                                break;
                                        }
                                    }
                                    if (hmPreparedProducts.get(bgNext.getName()) == null) {
                                        dptNext.setiAmountOfItem(bgNext.getAmount());
                                        dptNext.setiRestingTime(hmDuration.get("resting"));
                                        dptNext.setsCurrentPreparedProduct(bgNext.getName());
                                        dptNext.startPreparing();
                                        break;
                                    }
                                }
                                if (dptNext.getsCurrentPreparedProduct() != null) {
                                    dptNext.setiWorkTime();
                                    dptNext.checkPreparingStage();
                                }
                            }

                            if (iCurrentSize == iSize)
                                break;
                        }
                    }
                    finished();
                }
//            }
            return jsoPreparedItems;

        }

        private void calculateKneadingAndPreparationTime(Vector<BakedGood> bakedGoods) {

            if (iCurrentSize == iSize) {
                hmBakedProducts.clear();
                iSize = bakedGoods.size();
                iCurrentSize = 0;
            }
//            while (iCurrentSize < iSize) {
//                finished();
                if (getAllowAction()) {
                    Iterator<BakedGood> iBakedGoods = bakedGoods.iterator();
                    while (iBakedGoods.hasNext()) {
                        BakedGood bgNext = iBakedGoods.next();
                        HashMap<String, Integer> hmDuration = hmRecipeProducts.get(bgNext.getName());
                        Iterator<org.team_pjt.objects.KneadingMachine> iKneadingMachines = vKneadingMachine.iterator();
                        if (hmBakedProducts.get(bgNext.getName()) == null) {
                            while (iKneadingMachines.hasNext()) {
                                org.team_pjt.objects.KneadingMachine kmNext = iKneadingMachines.next();
                                if (!(kmNext.isbBusy())) {
                                    if (kmNext.getsCurrentKneadedProduct() != null) {
                                        if (hmBakedProducts.get(kmNext.getsCurrentKneadedProduct()) == null) {
                                            hmBakedProducts.put(kmNext.getsCurrentKneadedProduct(), 1);
                                            iCurrentSize++;
                                            if (iCurrentSize == iSize)
                                                break;
                                        }
                                    }
                                    if (hmBakedProducts.get(bgNext.getName()) == null) {
                                        Set<String> sStrings = hmDuration.keySet();
                                        Iterator<String> sIterator = sStrings.iterator();
                                        while (sIterator.hasNext()) {
                                            String sTmp = sIterator.next();
                                            if (!(sTmp.contains("item"))) {
                                                kmNext.setvTime(hmDuration.get(sTmp));
                                            }
                                        }
                                        kmNext.setsCurrentKneadedProduct(bgNext.getName());
                                        kmNext.startKneading(getName());
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
                        if (iCurrentSize == iSize)
                            {
                                hmSetTimeStep.clear();
                                break;
                            }
                    }
//                    finished();
//                    break;
//                }
//                finished();
            }
            hmSetTimeStep.clear();
        }
    }


}
