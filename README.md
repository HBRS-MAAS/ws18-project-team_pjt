# MAAS Project - team_pjt

Master Branch [![Build Status](https://travis-ci.org/HBRS-MAAS/ws18-project-team_pjt.svg?branch=master)](https://travis-ci.org/HBRS-MAAS/ws18-project-team_pjt)

Develop Branch [![Build Status](https://travis-ci.org/HBRS-MAAS/ws18-project-team_pjt.svg?branch=develop)](https://travis-ci.org/HBRS-MAAS/ws18-project-team_pjt)

Our team developed the OrderProcessing- and DoughPreparation-Stage for the bakery.

## Team Members
* Pascal Maczey - [@Dr4gonbl4de](https://github.com/Dr4gonbl4de)
* Jan Löffelsender - [@janl1992](https://github.com/janl1992)
* Maximilian Mensingn - [@madmax2012](https://github.com/madmax2012)

## Dependencies
* JADE 4.5.0+
* org.json 2.1+
* com.google.gson 2.7+
* JAVA-8-jdk
* JAVAfx 8.0+

## How to run
The Start.java is configured in order to run this project easily.

In order to run our two stages and the customer Stage (which is copied from the upstream) just use:

    gradle run

It will automatically get the dependencies and start JADE with the configured agents.
In case you want to clean you workspace run

    gradle clean
    
If you want to see all possible options how the project can be run use:

     gradle run --args='-isHost -customer -orderProessing -doughPrep'

To change scenario parameters please change the variable scenarioDirectory to the according directory name and the variable endTime to the matching time (when all orders are finished).
Logs were disabled as travis build fails if log size surpasses 4 MB. For enabling logs please uncomment System.out.println in following classes:
* OrderProcessing
* KneadingPreparingMachine
* Proofer
* DoughManager
* SchedulerAgent

## Eclipse
To use this project with eclipse run

    gradle eclipse

This command will create the necessary eclipse files.
Afterwards you can import the project folder.
