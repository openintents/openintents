To use this JAR file in another project:

1) In the package explorer, right-click on your project, select
   "Properties", then "Java Build Path"/"Libraries".

2) "Add External JARs...". Select 
   lib/openintents-lib-x.x.x.jar

------

How to create the JAR file in Eclipse:
(you need the OpenIntents source code for this)

1) In the package explorer, right-click on project "OpenIntents" /
   "Export...", then "Java"/"JAR file".

2) Deselect ALL files, and only select the following classes:
   OpenIntents/src/org.openintents.provider/
     * ALL FILES in this directory.
       (ContentIndex.java, Hardware.java, Location.java, News.java,
        RSSFeed.java, Shopping.java, Tag.java)

   OpenIntents/src/org.openintents.hardware/
     * Sensors.java
     * SensorSimulatorClient.java

   OpenIntents/src/org.openintents/
     * OpenIntents.java
   
3) Select the export destination:
   lib/openintents-lib-x.x.x.jar

4) Click Finish.


