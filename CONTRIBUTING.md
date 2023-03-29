## Project structure

The whole project is composed by

* ham: The base implementation
* globaltest: This will be run through globaltest.sh/bat. Used to verify the correctness of the -WHOLE- system
* samples: Sample projects
* docker: base docker images
* scripts: all the scripts to run examples, test, build, release and deploy

## Setup

* Install Chrome browser
* Install docker
* If on linux enable java to run on low ports

  sudo setcap CAP_NET_BIND_SERVICE=+eip [YOUR bin/JAVA]

### Ham

This is the main project

The tests are mostly in the API parts, they run a local version of ham

### Globaltest

This is used to

* Two ways to prepare
  * Run globaltest.sh/bat
  * Run the scripts/build/build_release and scripts/build/build_release_samples then extract in place the two tgz to obtain ham, calendar etc under release
* Then you can run the selenium tests

## Pull requests

If you want to contribute just make your pull request 
with a description of the problem solved and the explanation
of the reasons of the implementation inside the docs/contribute folder.

Just create a Markdown file with your github userid. To give an example
i would create a docs/contribute/kendarorg.md file with all the
explanations!

Please-please add unit tests when possible! If it's getting too hard we
can open an issue and refactor!

Feel free!



## Jacoco

When adding a module should change the pom from jacoco adding the 
following parts (e.g with MYMODULE name and ORG.MYMODULE base package)

        <report>
            <executiondata>
                ...
                <fileset dir="${basedir}/../MYMODULE/target">
                    <include name="jacoco*.exec" />
                </fileset>
                ...
            </executiondata>
            <structure name="Integration Tests Coverage Report">
                <sourcefiles encoding="UTF-8">
                    ...
                    <dirset dir="${basedir}/../MYMODULE">
                        <include name="**/src/main/java" />
                    </dirset>
                    ...
                </sourcefiles>
                <classfiles>
                    ...
                    <fileset dir="${basedir}/../MYMODULE/target/classes">
                        <include name="ORG/MYMODULE/**/*" />
                    </fileset>
                    ...
                </classfiles>
            </structure>
