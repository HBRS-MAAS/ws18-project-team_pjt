# SchedulerAgent
## Messages received:
* Propagate accepted Orders:
  * Type: PROPAGATE
  * Sender: OrderProcessingAgent
  * Receiver: SchedulerAgent
  * Content: JSONObject as String -> Order containing only Products that should be produced by this bakery
  
* Check Scheduler:
  * Type: REQUEST
  * Sender: OrderProcessingAgent
  * Receiver: SchedulerAgent
  * Content: JSONObject -> order with only available products
  
* Get Queue Position:
  * Type: REQUEST
  * Sender: Any agent
  * Receiver: SchedulerAgent
  * ConversationId: queue request
  * Content: String -> {orderId}
  
## Messages sent:
* Confirm Schedule:
  * Type: CONFIRM
  * Sender: SchedulerAgent
  * Receiver: OrderProcessingAgent
  * Content: String -> Scheduling possible
  
* Disconfirm Schedule:
  * Type: DISCONFIRM
  * Sender: SchedulerAgent
  * Receiver: OrderProcessingAgent
  * Content: String -> "Scheduling impossible!"
  
* Propagate acceptedOrders:
  * Type: PROPAGATE
  * Sender: SchedulerAgent
  * Receiver: All receiving Agents of same bakery
  * Content: JSONArray -> sorted List of accepted Orders
  
* Queue request reply:
  * Type: INFORM
  * Sender: SchedulerAgent
  * Receiver: Any agent
  * ConversationId: queue request
  * Content: String -> Order position if order in queue else -1
  
* ScheduledOrders reply:
  * Type: INFORM
  * Sender: SchedulerAgent
  * Receiver: Agent which requested
  * Content: JSONArray as String -> All scheduled orders