# DoughManager
## Messages received:
* Receive order vector from SchedulerAgent:
  * Type: PROPAGATE
  * Sender: SchedulerAgent of bakery
  * Receiver: DoughManager of bakery
  * MessageContent: JSONArray as String -> Order
  ### Content
  ```
  [
    {
        "customerId": String,
        "guid": String,
        "deliveryDate": JSONObject,
        "orderDate": JSONObject,
        "products": JSONObject
    },
    .
    .
  ]
  ```
  ### Example Content
  ```
  [
   {
      "customerId":"customer-014",
      "guid":"order-583","deliveryDate":{"hour":17,"day":2},
      "orderDate":{"hour":2,"day":1},
      "products":{"Donut":7,"Bagel":7,"Bread":3}
    },
   {
      "customerId":"customer-001",
      "guid":"order-001",
      "deliveryDate":{"hour":18,"day":2},
      "orderDate":{"hour":4,"day":1},
      "products":{"Donut":7,"Bagel":6,"Berliner":2,"Muffin":2,"Bread":10}
   },
   .
   .
   ]
  ```

## Messages send:
* Sends proofing request to proofer:
    * Type: ACCEPT_PROPOSAL
    * Sender: DoughManager of bakery
    * Receiver: Proofer
    * MessageContent: JSONObject as String -> Order
### Content
```
{
  "proofingTime": Integer,
  "productQuantities": Vector<Integer>,
  "guids": String,
  "productType": String
 }
```

### Example Content
```
{
  "proofingTime":12.0,
  "productQuantities":[1],
  "guids":["order-583"],
  "productType":"Bread"
 }
```