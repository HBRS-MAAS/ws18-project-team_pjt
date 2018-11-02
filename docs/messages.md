```java
class CommonMessage{
    private String sCustomerId;
    private Date dOrderDate;
    private Date dDeliveryDate;
    private HashMap<String, int> hmProducts;
    public CommonMessage(String sCustomerId, Date dOrderDate, Date dDeliveryDate, HashMap<String, int> hmProducts){
        this.sCustomerId = sCustomerId;
        this.dOrderDate = dOrderDate;
        this.dDeliveryDate = dDeliveryDate;
        this.hmProducts = hmProducts;
    }
}
```