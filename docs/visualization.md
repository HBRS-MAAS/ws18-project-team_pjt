## What to visualize 
We decided to visualize all orders consisting of one or several products. As there are plenty of orders we decided to display on root level only order IDs - if you want status of products of order ID you have to click on one order ID.  Order ID expands it would look the following way:
<br/><br/> 
Order ID
- CustomerID
- Delivery Date
- Order Date
- Products
  - < ProductGuid > - < Status >
  - < ProductGuid > - < Status >
  - < ProductGuid > - < Status >
  - .
  - .    
## How to visualize Bakery project
There is one shared agent to visualize process progress of bakery.
The visualization agent gets a message for updating status of products of orders every 15 minutes from orderagent.
The message has following entries:
* CustomerID 
* Delivery Date
* Order Date
* Hashmap - key is guid of product, value is status(=stage) of product 

Within our architecture orderagent receives statusupdates for his order from scheduleragent and from truckagents. Orderagent passes statusupdates to visualizationagent.
## Framework
For displaying progress of order we decided to use javaFx. Within JAVA this is the state of the art framework for creating Graphical User Interfaces. 