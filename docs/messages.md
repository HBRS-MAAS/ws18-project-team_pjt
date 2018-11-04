```java
//Message From Agent X ->(to) Agent Y
class CommonOrderMessage{ // CustomerAgent->OrderProcessingAgent
                          // Orderagent -> Truckagent
    private Customer cCustomer;
    private Date dOrderDate;
    private Date dDeliveryDate;
    private HashMap<String, int> hmProducts;    
}
     
class SchedulerStatusMessage{ // Scheduleragent->OrderProcessingagent
    private String sSchedulerId;
    private boolean bIsUtilizedtoCapacity;
}

class OrderMessage{ // OrderProcessingagent -> OrderAgent
    private String sSchedulerId;
    private Order oCustomerOrder;
}

class SchedulerUpdateMessage{ // Orderagent -> Scheduleragent
                        // Scheduleragent -> Orderagent
                        // Orderagent -> Visualagent
                        // Truckagent->Orderagent
                        // Orderagent -> Truckscheduleragent
    private Order oCustomerOrder;
}

class Order extends CommonOrderMessage{
    private HashMap<String, int> hmStatus;
}

class SchedulerMessage{ // Scheduleragent -> Kneadingmachineagent ; 
                        // Scheduleragent -> Ovenagent
    private HashMap<String, int> hmProducts;  
}

class TruckStatusMessage{ // Truckagent -> Orderagent
    private String sTruckId;
    private boolean bIsUtilizedtoCapacity;
}

```