JBoss AS AEjb Subsystem
========================

What is it?
---------------


Prerequisites
-------------

To run this subsystem with the provided build scripts, you will need the following:

1.  Java 1.6, to run JBoss AS and Maven. You can choose from the following:
    * OpenJDK
    * Oracle Java SE
    * Oracle JRockit

2.  Maven 3.0.0 or newer, to build and deploy the examples
    * Follow the official Maven installation guide if you don't already have Maven 3 installed. 
    * If you have Maven installed, you can check the version by running this command in a shell prompt:

	> mvn --version 

3.  The JBoss AS 7 distribution zip
    * For information on how to install and run JBoss, refer to the product documentation.

You can also build the subsystem from Eclipse using JBoss tools. For more information on how to set up Maven and the JBoss tools, refer to https://docs.jboss.org/author/display/AS71/Getting+Started+Developing+Applications+Guide .


Building
-------------------
If you already have Maven 3 installed and setted environment variable ${JBOSS_HOME}

> mvn install


Starting and Stopping JBoss
------------------------------------------
Change to the bin directory after a successful build

> $ cd build/target/jboss-\[version\]/bin

This version does not support domain mode

Start the server in standalone mode

> $ ./standalone.sh

To stop the server, press Ctrl + C, or use the admin console

> $ ./jboss-admin.sh --connect command=:shutdown

More information on the wiki: http://community.jboss.org/wiki/JBossAS7UserGuide

Starting AEjb
-----------------
Modify the standalone.xml in ${JBOSS_HOME}\standalone\configuration.

 * add extension:
   <extension module="org.nju.artemis.aejb"/>
 * add subsystem: 
   <subsystem xmlns="urn:org.nju.artemis.aejb:1.0">
	<client-service jndi-name="java:global/aejb/client"/>
   </subsystem>

Server Manager
----------------------------
Change to the bin directory after JBoss start

> $ cd build/target/jboss-\[version\]/bin

Start the admin console of aejb subsystem

> $ ./jboss-admin.sh

To stop the console, press Ctrl + C

The admin script has following functions:
    * `overview` - 
    * `deploy` -
    * `undeploy` -
    * `block` -
    * `resume` -
    * `replace` -
    * `switch` -


Application supplier
-------------------------

