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
  
## Messages sent:
* Confirm Schedule:
  * Type: CONFIRM
  * Sender: SchedulerAgent
  * Receiver: OrderProcessingAgent
  * Content: String -> Scheduling possible