package org.team_pjt.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.team_pjt.behaviours.receiveKillMessage;
import org.team_pjt.behaviours.shutdown;
import org.team_pjt.objects.Product;
import org.team_pjt.objects.Location;
//import org.team_pjt.objects.Product;

import java.util.*;

public class SchedulerAgent extends Agent {
    public static final String DURATION = "duration";
    private String sBakeryId;
    private Location lLocation;
    private Hashtable<String, Product> htAvailableProducts;
    private HashMap<String, Float> hmPrepTables;
    private Vector<AID> ovens;
    private HashMap<String, Float> hmKneadingMachine;
    private HashMap<String, Product> hmProducts;
    private String[] sSplit;
    protected void setup(){
        Object[] oArguments = getArguments();
//        String sArguments = prepareArguments(oArguments);
        if (!readArgs(oArguments)) {
            System.out.println("No parameter given for SchedulerAgent " + getName());
        }
        registerAgent();
        ovens = new Vector<>();
        addBehaviour(new receiveKillMessage());
        addBehaviour(new shutdown());
        addBehaviour(new TickerBehaviour(this, 500) {

            @Override
            protected void onTick() {
                ACLMessage aclmReceive = (ACLMessage) myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                if(aclmReceive != null &&(aclmReceive.getPerformative() == ACLMessage.INFORM)){
                    if (aclmReceive.getContent().equals(sBakeryId)){
//                        ovens.put();
                        ovens.add(aclmReceive.getSender());
                    };

                }
            }
        });
//        addBehaviour(new OneShotBehaviour() {
//            @Override
//            public void action() {
//
//            }
//        });
        System.out.println("SchedulerAgent " + getName() + " ready");

    }

    private void registerAgent() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("schedulerbakery");
        sd.setName(this.sBakeryId);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private String prepareArguments(Object[] oArguments) {
        String[] stringArray = Arrays.copyOf(oArguments, oArguments.length, String[].class);
        StringBuilder sbBuilder = new StringBuilder();
        for(int i = 0; i< stringArray.length;i++){
            sbBuilder.append(stringArray[i]);
            if(i < stringArray.length - 1){sbBuilder.append(",");}
        }
        String sArguments = sbBuilder.toString();
        return sArguments;
    }

    private boolean readArgs(Object[] oArgs){
          if(oArgs != null && oArgs.length > 0){
              hmProducts = new HashMap<>();
              Product p = new Product();
              hmKneadingMachine = new HashMap<>();
              hmPrepTables = new HashMap<>();
              JSONArray jsaOvenInfo = new JSONArray(oArgs[0].toString().replaceAll("###",","));
              Iterator<Object> iBakery = jsaOvenInfo.iterator();
              String sName = getName();
              String[] sSplit = sName.split("@");
              sBakeryId = sSplit[0];
              while(iBakery.hasNext()){
                  JSONObject jsoBakeryInfo = (JSONObject) iBakery.next();
                  if (sBakeryId.equals(String.valueOf(jsoBakeryInfo.get("guid")))) {
                      JSONObject jsoEquipment = (JSONObject) jsoBakeryInfo.get("equipment");
                      JSONArray jsaArray = (JSONArray) jsoEquipment.get("kneadingMachines");
                      Iterator<Object> iKneadingMachinesIterator = jsaArray.iterator();
                      while(iKneadingMachinesIterator.hasNext()){
                          JSONObject jsoKneadingMachine = (JSONObject) iKneadingMachinesIterator.next();
                          hmKneadingMachine.put((String) jsoKneadingMachine.get("guid"), (float) -1);
                      }
                      JSONArray jsaProducts = (JSONArray) jsoBakeryInfo.get("products");
                      Iterator<Object> ijsaIterator = jsaProducts.iterator();
                      while(ijsaIterator.hasNext()){
                          JSONObject jsoProdut = (JSONObject) ijsaIterator.next();
                          String sGuid = (String) jsoProdut.get("guid");
                          p.setGuid(sGuid);
                          JSONObject jsoBatch = (JSONObject) jsoProdut.get("batch");
                          p.setBreadsPerOven((Integer) jsoBatch.get("breadsPerOven"));
                          JSONObject jsoRecipe = (JSONObject) jsoProdut.get("recipe");
                          p.setCoolingRate((Integer) jsoRecipe.get("coolingRate"));
                          p.setBakingTemp((Integer) jsoRecipe.get("bakingTemp"));

                          JSONArray jsaRecipe = (JSONArray) jsoRecipe.get("steps");
                          Iterator<Object> iJsaRecipe = jsaRecipe.iterator();
                          JSONObject jsoRecipeStep = null;
                          String sAction = null;
                          int iDuration = 0;
                          while(iJsaRecipe.hasNext()){
                              jsoRecipeStep = (JSONObject) iJsaRecipe.next();
                              sAction = String.valueOf(jsoRecipeStep.get("action"));
                              switch(sAction){
                                  case "kneading": p.setKneadingTime((Integer) jsoRecipeStep.get(DURATION)); break;
                                  case "resting": p.setRestingTime((Integer) jsoRecipeStep.get(DURATION)); break;
                                  case "item preparation": p.setItemPrepTime((Integer) jsoRecipeStep.get(DURATION)); break;
                                  case "cooling": p.setCooling((Integer) jsoRecipeStep.get(DURATION)); break;
                                  case "proofing": p.setProofing((Integer) jsoRecipeStep.get(DURATION)); break;
                                  case "filling": p.setFilling((Integer) jsoRecipeStep.get(DURATION)); break;
                                  case "baking": p.setBaking((Integer) jsoRecipeStep.get(DURATION)); break;
                                  case "decorating": p.setDecorating((Integer) jsoRecipeStep.get(DURATION)); break;
                                  case "sprinkling": p.setSprinkling((Integer) jsoRecipeStep.get(DURATION)); break;
                              }
                              iDuration = iDuration + Integer.valueOf((Integer) jsoRecipeStep.get(DURATION));
                          }
                          p.setRecipeDuration(iDuration);
                          JSONObject jsoPackaging = (JSONObject) jsoProdut.get("packaging");
                          p.setBoxingTemp((Integer) jsoPackaging.get("boxingTemp"));
                          p.setBreadsPerBox((Integer) jsoPackaging.get("breadsPerBox"));
                          p.setSalesPrice((Double) jsoProdut.get("salesPrice"));
                          p.setProductionCost((Double) jsoProdut.get("productionCost"));
                          hmProducts.put(sGuid, p);
//                          Product p = new Product(,jsoProdut.get("guid"))
                      }
                      JSONArray jsaDoughPrepTables = (JSONArray) jsoEquipment.get("doughPrepTables");
                      Iterator<Object> iDoughPrepTable = jsaDoughPrepTables.iterator();
                      while(iDoughPrepTable.hasNext()){
                          JSONObject jsoDoughPrepTable = (JSONObject) iDoughPrepTable.next();
                          hmPrepTables.put((String)jsoDoughPrepTable.get("guid"), (float) -1);
                      }
                      JSONObject jsoLocation = (JSONObject) jsoBakeryInfo.get("location");
                      jsoBakeryInfo.getJSONObject("location").getDouble("y");
                      jsoBakeryInfo.getJSONObject("location").getDouble("x");
                      lLocation = new Location(jsoBakeryInfo.getJSONObject("location").getDouble("y"), jsoBakeryInfo.getJSONObject("location").getDouble("x"));
                  }
              }
              return false;
          }else {return true;}
    }

    }
