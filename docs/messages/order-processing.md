# OrderProcessingAgent
## Messages received:
* Receive Order from client:
  * Type: CFP
  * Sender: Client
  * Receiver: All OrderProcessingAgents
  * Content: JSONObject as String -> Order
* Receive accepted proposal:
  * Type: ACCEPT_PROPOSAL
  * Sender: Client
  * Receiver: OrderProcessingAgent
  * Content: JSONObject as String -> Full Order but only containing products available at specified bakery
* Receive Reject proposal:
  * Type: REJECT_PROPOSAL
  * Sender: Client
  * Receiver: not chosen OrderProcessingAgent
  * Content: String -> "rejected"
* Confirm Schedule:
  * Type: CONFIRM
  * Sender: SchedulerAgent
  * Receiver: OrderProcessingAgent
  * Content: String -> Scheduling possible
* Disconfirm Schedule:
  * Type: DISCONFIRM
  * Sender: SchedulerAgent
  * Receiver: OrderProcessingAgent
  * Content: String -> Scheduling impossible
  
## Messages sent:
* Proposal to Client:
  * Type: PROPOSAL
  * Sender: OrderProcessingAgent
  * Receiver: Client
  * Content: JSONObject as String -> List of available products with prices (amount of product times sales_price for product)
* Refusal to Client:
  * Type: REFUSE
  * Sender: OrderProcessingAgent
  * Receiver: Client
  * Content: String -> reason for refusal (no needed product available or not enough time to produce order)
* Confirmation of order:
  * Type: CONFIRM
  * Sender: OrderProcessingAgent
  * Receiver: client
  * Content: String -> "order confirmed"
* Check Scheduler:
  * Type: REQUEST
  * Sender: OrderProcessingAgent
  * Receiver: SchedulerAgent
  * Content: JSONObject -> order with only available products
* Propagate received Orders:
  * Type: Propagate
  * Sender: OrderProcessingAgent
  * Receiver: all Agents
  * Content: JSONObject -> received Order
* Propagate accepted Orders:
  * Type: PROPAGATE
  * Sender: OrderProcessingAgent
  * Receiver: all Agents
  * Content: JSONArray -> sorted List of all received Orders -> according bakeryid