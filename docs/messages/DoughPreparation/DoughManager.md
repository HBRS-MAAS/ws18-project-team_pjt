# DoughManager
## Messages received:
* Receive Order from SchedulerAgent:
  * Type: PROPAGATE
  * Sender: SchedulerAgent of bakery
  * Receiver: DoughManager of bakery
  * MessageContent: JSONArray as String -> Order
  ### Content
  ```
  {
   "customerId": String,
   "guid": String,
   "deliveryDate": JSONObject,
   "orderDate": JSONObject,
   "products": JSONObject
  }
  ```
  ### Example Content
  ```
  {
   "customerId":"customer-026",
   "guid":"order-950",
   "deliveryDate":{"hour":12,"day":4},
   "orderDate":{"hour":1,"day":1},
   "products":{"Donut":0,"Bagel":9,"Berliner":6,"Muffin":6,"Bread":6}}
   }
  ```

## Messages send:
* Sends proofed orders to baking stage:
    * Type: PROPAGATE
    * Sender: DoughManager of bakery
    * Receiver: baking stage
    * MessageContent: JSONObject as String -> Order
### Content
```
{
  "product": Integer,
  "product": Integer,
  .
  .
  .
 }
```

### Example Content
```
{
  "Donut":0,
  "Bagel":9,
  "Berliner":6,
  "Muffin":6,
  "Bread":6
 }
```
<!--* Receive noNewOrder from client:-->
  <!--* Type: Inform-->
  <!--* Sender: Client-->
  <!--* Receiver: All OrderProcessingAgents-->
  <!--* ConversationOd: "syncing"-->
  <!--* Content: String -> "no new Order"-->
  <!---->
<!--* Receive accepted proposal:-->
  <!--* Type: ACCEPT_PROPOSAL-->
  <!--* Sender: Client-->
  <!--* Receiver: OrderProcessingAgent-->
  <!--* ConversationId: {orderId}-->
  <!--* Content: JSONObject as String -> Full Order but only containing products available at specified bakery-->
  <!---->
<!--* Receive Reject proposal:-->
  <!--* Type: REJECT_PROPOSAL-->
  <!--* Sender: Client-->
  <!--* Receiver: not chosen OrderProcessingAgent-->
  <!--* ConversationId: {orderId}-->
  <!--* Content: String -> "rejected"-->
  <!---->
<!--* Confirm Schedule:-->
  <!--* Type: CONFIRM-->
  <!--* Sender: SchedulerAgent-->
  <!--* Receiver: OrderProcessingAgent-->
  <!--* Content: String -> Scheduling possible-->
  <!---->
<!--* Disconfirm Schedule:-->
  <!--* Type: DISCONFIRM-->
  <!--* Sender: SchedulerAgent-->
  <!--* Receiver: OrderProcessingAgent-->
  <!--* Content: String -> Scheduling impossible-->
  
<!--## Messages sent:-->
<!--* Proposal to Client:-->
  <!--* Type: PROPOSAL-->
  <!--* Sender: OrderProcessingAgent-->
  <!--* Receiver: Client-->
  <!--* ConversationId: {orderId}-->
  <!--* Content: JSONObject as String -> List of available products with prices (amount of product times sales_price for product)-->
  <!---->
<!--* SchedulerSyncing:-->
  <!--* Type: INFORM-->
  <!--* Sender: OrderProcessingAgent-->
  <!--* Receiver: SchedulerAgent-->
  <!--* Content: String -> "NO NEW ORDER"-->
  <!---->
<!--* DistributeReceivedOrder:-->
  <!--* Type: INFORM-->
  <!--* Sender: OrderProcessingAgent-->
  <!--* Receiver: all Agents-->
  <!--* Content: JSONObject -> Order-->
  <!---->
<!--* Refusal to Client:-->
  <!--* Type: REFUSE-->
  <!--* Sender: OrderProcessingAgent-->
  <!--* Receiver: Client-->
  <!--* Content: String -> reason for refusal (no needed product available or not enough time to produce order)-->
  <!---->
<!--* Check Scheduler:-->
  <!--* Type: REQUEST-->
  <!--* Sender: OrderProcessingAgent-->
  <!--* Receiver: SchedulerAgent-->
  <!--* Content: JSONObject -> order with only available products-->
  <!---->
<!--* Send accepted Order to Scheduler:-->
  <!--* Type: PROPAGATE-->
  <!--* Sender: OrderProcessingAgent-->
  <!--* Receiver: SchedulerAgent-->
  <!--* Content: JSONObject -> Order containing only Products that should be produced by this bakery-->