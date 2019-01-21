# Start Class Proposal

## Starting

Possible to give commandline arguments:

|command            |effect                                             |
| ------------------|:-------------------------------------------------:|
|-isHost            |starts a main Container                            |
|-localPort {port}  |sets the Port on which the host ist listening      |
|-host {host}       |setting a host address for an peripheral container |
|-port {port}       |sets the port to connect to                        |
|-customer          |enables customer agents to start                   |
|-orderProcessing   |enables orderProcessing agents to start            |
|-doughPrep         |enables doughPrep agents to start                  |
|-baking            |enables baking agents to start                     |
|-packaging         |enables packaging agents to start                  |
|-delivery          |enables delivery agents to start                   |
|-visualization     |enables visualization agents to start              |

## Default arguments

In default the program will be executed as host (main-container) and starting only the TimeKeeperAgent. The port is set to 8133 in default.

When starting a peripheral container in default localhost is set as host using port 8133.

## Adding agents to start

In order to add the agents of each group, every group should implement a class which inherits from Initializer. Then each group has to add their own Initializer in
the Start class at the according place:

 ```
 
 if(customerStage) {

 }
 if(orderProcessingStage) {
     Initializer init = new orderProcessingInitializer();
     sb.append(init.initialize());
 }
 if(doughPrepStage) {

 }
 if(bakingStage) {

 }
 if(packagingStage) {

 }
 if(deliveryStage) {

 }
 if(visualizationStage) {

 }
 
```

The class "Initializer" has only the method "initialize()" which returns a string, that will be appended to the command String.