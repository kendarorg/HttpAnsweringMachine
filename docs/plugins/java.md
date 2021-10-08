The system support the addition of java plugins. 

You can get a basic pom for a plugin [here](java/pom.xml). All the important staff are marked
with the "CHANGEME" string. You'll event notice the "copy-jar-dependencies-to-target" task:
this is used to copy the needed dependencies near the target so that the main application
loader can use them.

The "copy-dependencies" task is the one that writes all "real" dependencies on the
"target/classes/lib" directory from which the "copy-jar-dependencies-to-target" will
take the data and where you could find the real dependencies.

    IMPORTANT
    You should copy all dependencies, even the tranisitive ones, in the example pom is 
    even included the "asm-5.0.4.jar" that was not identified as a direct dependency. Do
    some run to check what is needed

The jar and the dependencies should be placed into the [libs directory](../local/basic.md)

* [Filters](java/jfilters.md)
* [Static pages](java/jstatic.md)