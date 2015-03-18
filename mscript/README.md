mscript
=======

[ANTLR](http://www.antlr.org/) / Java-based parser and interpreter for MScript, a proprietary scripting language.

Generating the parser Java source code from the ANTLR lexer and parser grammar specifications
---------------------------------------------------------------------------------------------

This project is [Maven](http://maven.apache.org/)-based and as such, the ANTLR (v4) lexer and parser grammar
specifications (i.e. .g4 source files) are located under `src/main/antlr4/com/webmbt/mscript/parse`.

The generation of the parser Java source code obviously needs to be done prior to compiling the project. To do this, one
can either invoke the [ANTLR tool][antlr-tool] from the command line (requires downloading [`antlr-4.x-complete.jar`]
(http://www.antlr.org/download/antlr-4.4-complete.jar) as well as setting the `CLASSPATH` environment variable) or
invoke the Maven `generate-sources` phase, like in the following example (simpler since the ANTLR runtime dependencies
are automatically downloaded / retrieved by Maven):

    mvn generate-sources

The parser Java source code generated in this way can be found under `target/generated-sources/antlr4` (in the
`com.webmbt.mscript.parse` package).

However, if the project is built using Maven (the recommended way), this phase is included by default in the build
process:

    mvn clean install

(One could also generate the Java source code in various IDEs by using specific ANTLR plugins but this varies from one
IDE / plugin to another.)

[antlr-tool]: https://theantlrguy.atlassian.net/wiki/display/ANTLR4/ANTLR+Tool+Command+Line+Options "ANTLR tool"
