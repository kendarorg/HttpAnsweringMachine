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
