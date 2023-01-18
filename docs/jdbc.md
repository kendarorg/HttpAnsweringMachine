## Setup the proxy

* Add your driver inside the "lib" directory to load it automatically at startup
* Open the "Url/Db Rewrites" section and select the JDBC Proxyes tab
* Add the new proxy

<img src="images/dbproxy.gif" width="500"/>

* The first block is the one with the credentials to connect to the real DB
* The second block is the database you will connect to. The connection string is the one shown

    IMPORTANT
    Local login and password are not used yet, just use the original ones

* If you are using Hibernate or similar the DIALECT will be the one of the destination database

    IMPORTANT
    It is good practice to use different local dbs for different applications
    even if they are pointing to the same real db. This is useful to understand
    WHO is using what

## Configuring your application

Check on the repo for the last versions:

* [Releases](https://maven.kendar.org/maven2/releases/org/kendar/janus-driver/)
* [Snapshots](https://maven.kendar.org/maven2/snapshots/org/kendar/janus-driver/)

Set on your config the driver "org.kendar.janus.JdbcDriver"


### On classpath

To run a java application with JanusJdbc driver and to set it to use the HAM proxy
you should first find the META-INF/MANIFEST.MF file inside the jar (open it as if it where a
zip file)

Find the main class (here: org.springframework.boot.loader.JarLauncher)

        Manifest-Version: 1.0
        Created-By: Maven JAR Plugin 3.2.2
        Build-Jdk-Spec: 11
        Implementation-Title: be
        Implementation-Version: 3.7.7
        # This is what we are speaking of
        Main-Class: org.springframework.boot.loader.JarLauncher
        Start-Class: org.kendar.be.MainBe
        Spring-Boot-Version: 2.6.3

Then find all the jar files needed by the app, and the janus jar, and add them on the
classpath! The resulting command line will be

    java OPTIONALJVMPARAMS -cp "ALLJARS" MAINCLASS APPSPECIFICPARAMS


The final command line (for HAM calendar/be sample) will be (on windows)

    java -cp "be-4.1.3-SNAPSHOT.jar;../janus-driver-1.1.4.jar" ^
        org.springframework.boot.loader.JarLauncher ^
        --spring.config.location=file:///%cd%\bedbham.application.properties

### With maven

First you should add to your project the maven repo in the root pom.xml

    <repositories>
        <repository>
            <id>kendar</id>
            <name>Kendar Repository</name>
            <url>https://maven.kendar.org/maven2/releases</url>
            <layout>default</layout>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>

        <repository>
            <id>kendar2</id>
            <name>Kendar Repository2</name>
            <url>https://maven.kendar.org/maven2/snapshots</url>
            <layout>default</layout>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
        <repository>
            <id>central2</id>
            <name>Central Repository2</name>
            <url>https://repo.maven.apache.org/maven2</url>
            <layout>default</layout>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
    </repositories>

Then add the artifact on your project

        <dependency>
            <groupId>org.kendar</groupId>
            <artifactId>janus-driver</artifactId>
            <version>1.0.11-SNAPSHOT</version>
        </dependency>

### With application servers

It depends on your application server, but for example in Tomcat you should put 
the driver into the  TOMCAT_HOME/lib directory
