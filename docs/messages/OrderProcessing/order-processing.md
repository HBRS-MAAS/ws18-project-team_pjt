# OrderProcessingAgent
## Messages received:
* Receive Order from client:
  * Type: CFP
  * Sender: Client
  * Receiver: All OrderProcessingAgents
  * ConversationId: {orderId}
  * Content: JSONObject as String -> Order
  
* Receive accepted proposal:
  * Type: ACCEPT_PROPOSAL
  * Sender: Client
  * Receiver: OrderProcessingAgent
  * ConversationId: {orderId}
  * Content: JSONObject as String -> Full Order but only containing products available at specified bakery
  
* Receive Reject proposal:
  * Type: REJECT_PROPOSAL
  * Sender: Client
  * Receiver: not chosen OrderProcessingAgent
  * ConversationId: {orderId}
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
  * ConversationId: {orderId}
  * Content: JSONObject as String -> List of available products with prices (amount of product times sales_price for product)
  
* DistributeReceivedOrder:
  * Type: INFORM
  * Sender: OrderProcessingAgent
  * Receiver: all Agents of same bakery
  * Content: JSONObject -> Order
  
* Refusal to Client:
  * Type: REFUSE
  * Sender: OrderProcessingAgent
  * Receiver: Client
  * Content: String -> reason for refusal (no needed product available or not enough time to produce order)
  
* Check Scheduler:
  * Type: REQUEST
  * Sender: OrderProcessingAgent
  * Receiver: SchedulerAgent
  * Content: JSONObject -> order with only available products
  
* Send accepted Order to Scheduler:
  * Type: PROPAGATE
  * Sender: OrderProcessingAgent
  * Receiver: SchedulerAgent
  * Content: JSONObject -> Order containing only Products that should be produced by this bakery