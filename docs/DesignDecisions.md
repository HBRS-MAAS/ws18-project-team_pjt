# Design Decisions
In this file all design decisions are explained

## OrderProcessing-Stage
* Splitting OrderProcessing and scheduling
  * The functionality of OrderProcessing and SchedulerAgent is totally different. 
  * The main idea of OrderProcessing was to deal with all messages which need to be exchanged between agents of different stages.
  * The task of the scheduler should be to check if a specific bakery can produce products of an order in time.
  * This specification isn't implemented partly. For example the SchedulerAgent propagates the scheduled orders and OrderProcessing checks if products are available.
   The main reason was to reduce communication between those two Agents.

* OrderProcessing:
  * Why a TimeManager Behaviour:
    * The TimeManager Behaviour is responsible for checking if it's allowed to perform an action and to check if the agent should be terminated.
    * In TimeManager "finished()" is called in order to get to the next time step.
    * The condition if(!order_received) prevents the OrderProcessingAgent to step forward as long as an order is processed. Thus not more than one time step is "lost".
  * distributeFullOrder Behaviour:
    * This Behaviour propagates the full received order to all agents, which belong to the same bakery and receive those messages.
  * OfferRequestServer Behaviour:
    * This Behaviour is responsible for the whole communication from and to the customer and from and to the SchedulerAgent.
    
* SchedulerAgent:
  * TimeManager Behaviour:
    * Same reasons as in OrderProcessing
  * isNewOrderChecker Behaviour:
    * Checks for new orders every timeStep
  * receiveOrder Behaviour:
    * This Behaviour receives a new order, schedules it and adds it to queue if it's feasible.

## DoughPreparation-Stage

## Visualization