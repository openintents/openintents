How to create the JAR file under Eclipse:

1) In the package explorer, right-click on project "OpenIntents" /
   "Export...", then "Java"/"JAR file".

2) Select the following classes:
   OpenIntents/src/org.openintents/OpenIntents.java
                   org.openintents.locations/LocationsProvider.java
                   org.openintents.provider/Location.java
                                            Tag.java
                   org.openintents.tags/TagsProvider.java

   and deselect ALL other files.

3) Select the export destination:
   openintents/lib/openintents-x.x.x.jar

4) Click Finish.


------
To use this JAR file in another project:

1) In the package explorer, right-click on project "OpenIntents" /
   "Properties", then "Java Build Path"/"Libraries".

2) "Add External JARs...". Select 
   openintents/lib/openintents-x.x.x.jar
