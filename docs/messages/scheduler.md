# SchedulerAgent
## Messages received:
* Disconfirm Schedule:
  * Type: DISCONFIRM
  * Sender: SchedulerAgent
  * Receiver: OrderProcessingAgent
  * Content: String -> Scheduling impossible
  
* Propagate accepted Orders:
  * Type: PROPAGATE
  * Sender: OrderProcessingAgent
  * Receiver: allAgents
  * Content: JSONObject -> "sorterOrder": JSONArray containing sorted List of all received and scheduled Orders -> "bakeryId": according bakeryid
  
* Check Scheduler:
  * Type: REQUEST
  * Sender: OrderProcessingAgent
  * Receiver: SchedulerAgent
  * Content: JSONObject -> order with only available products
  
* Inform no new order:
  * Type: INFORM
  * Sender: OrderProcessing
  * Receiver: SchedulerAgent
  * Content: String -> "NO NEW ORDER"
  
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
  
* Propagate acceptedOrdes:
  * Type: PROPAGATE
  * Sender: SchedulerAgent
  * Receiver: All Agents
  * Content: JSONArray -> sorted List of accepted Orders