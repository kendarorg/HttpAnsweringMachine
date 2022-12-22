## JDBC calls

Check on the repo for the last versions:

* [Releases](https://maven.kendar.org/maven2/releases/org/kendar/janus-driver/)
* [Snapshots](https://maven.kendar.org/maven2/snapshots/org/kendar/janus-driver/)

Set on your config the driver "org.kendar.janus.JdbcDriver"


### On classpath

* Download the latest version
* 

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
