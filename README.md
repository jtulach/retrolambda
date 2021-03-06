
Retrolambda: Use Lambdas on Java 7
==================================

Just as there was [Retroweaver](http://retroweaver.sourceforge.net/) et al.
for running Java 5 code with generics on Java 1.4, **Retrolambda** lets you
run Java 8 code with lambda expressions on Java 7 or lower. It does this by
transforming your Java 8 compiled bytecode so that it can run on a Java 7
runtime. After the transformation they are just a bunch of normal .class
files, without adding any runtime dependencies.

Adventurous developers may use Retrolambda to backport lambda expressions
even to Java 6 or Java 5. And if you reach Java 5, there are [other
backporting tools](http://en.wikipedia.org/wiki/Java_backporting_tools)
that may let you go down to Java 1.4.

Android developers may also use Retrolambda. Serge Zaitsev has written [an
article about how to do it](http://zserge.com/blog/android-lambda.html).


User Guide
----------

### Getting Started

[Download](https://oss.sonatype.org/content/groups/public/net/orfjackal/retrolambda/retrolambda/)
the latest `retrolambda.jar` from Maven Central.

Use JDK 8 to compile your source code.

Run Retrolambda, using Java 8, on the class files produced by JDK 8. Run
`java -jar retrolambda.jar` without any additional options to see the
instructions (for your convenience they are also shown below).

Your class files should now run on Java 7. Be sure to run comprehensive tests
on Java 7, in case the code accidentally uses Java 8 APIs or language features
that Retrolambda doesn't backport.

```
Usage: java -Dretrolambda.inputDir=? -Dretrolambda.classpath=? [-javaagent:retrolambda.jar] -jar retrolambda.jar

Retrolambda is a backporting tool for classes which use lambda expressions
and have been compiled with Java 8, to run on Java 7 (maybe even Java 5).
See https://github.com/orfjackal/retrolambda

Copyright (c) 2013-2014  Esko Luontola <www.orfjackal.net>
This software is released under the Apache License 2.0.
The license text is at http://www.apache.org/licenses/LICENSE-2.0

Required system properties:

  retrolambda.inputDir
      Input directory from where the original class files are read.

  retrolambda.classpath
      Classpath containing the original class files and their dependencies.

Optional system properties:

  retrolambda.bytecodeVersion
      Major version number for the generated bytecode. For a list, see
      offset 7 at http://en.wikipedia.org/wiki/Java_class_file#General_layout
      Default value is 51 (i.e. Java 7)

  retrolambda.outputDir
      Output directory into where the generated class files are written.
      Defaults to same as retrolambda.inputDir

  retrolambda.includedFiles
      List of files to process, instead of processing all files.
      This is useful for a build tool to support incremental compilation.

If the Java agent is used, then Retrolambda will use it to capture the
lambda classes generated by Java. Otherwise Retrolambda will hook into
Java's internal lambda dumping API, which is more susceptible to suddenly
stopping to work between Java releases.
```


### Maven Plugin

To run Retrolambda using Maven, add the following to your pom.xml:

```xml
<plugin>
    <groupId>net.orfjackal.retrolambda</groupId>
    <artifactId>retrolambda-maven-plugin</artifactId>
    <version>1.6.1</version>
    <executions>
        <execution>
            <goals>
                <goal>process-main</goal>
                <goal>process-test</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

See the [plugin documentation](http://orfjackal.github.io/retrolambda/retrolambda-maven-plugin/plugin-info.html)
for all possible parameters. There is also a usage example in [end-to-end-tests/pom.xml](https://github.com/orfjackal/retrolambda/blob/master/end-to-end-tests/pom.xml)


### Gradle Plugin

[Gradle Retrolamba Plugin](https://github.com/evant/gradle-retrolambda) is developed by Evan Tatarka. See its site for usage instructions.


### Tips

During development, inside an IDE, it's the easiest to use Java 8, without
Retrolamba, to compile and run tests. But in your continuous integration build
you should run tests using the target Java version. For example, you can
configure Maven Surefire Plugin to run tests
[using a different JVM](http://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html#jvm).

I recommend setting up environment variables JAVA8_HOME, JAVA7_HOME etc. and
referring to those variables in the build configuration, instead of relying on
what happens to be the default Java version in JAVA_HOME.

You will need Java 8 for compiling and also for generating Javadocs.


Known Limitations
-----------------

Does not backport Java 8 APIs.

Does not backport Java 8 language features other than lambda expressions.

Does not support serializable lambda expressions. Implementing support for
them would technically be possible, but it would require projects to have a
runtime dependency on a library which would contain a backported copy of
the `java.lang.invoke.SerializedLambda` class. If you really need it, make
a feature request. ;-)

May break if a future JDK 8 build stops generating a new class for each
`invokedynamic` call. Retrolambda works so that it captures the bytecode
that `java.lang.invoke.LambdaMetafactory` generates dynamically, so
optimizations to that mechanism may break Retrolambda.


Version History
---------------

### Upcoming

- Backports static methods on interfaces
  ([Issue #31](https://github.com/orfjackal/retrolambda/issues/31))

### Retrolambda 1.6.1 (2014-08-25)

- Fixed a crash when trying backport classes which are nominally the same
  as those included in the JRE, but which have different bytecode
  ([Issue #29](https://github.com/orfjackal/retrolambda/issues/29))

### Retrolambda 1.6.0 (2014-08-20)

- Does not anymore require the use of a Java agent
  ([Issue #27](https://github.com/orfjackal/retrolambda/issues/27))
- Maven plugin: by default run Retrolambda in the same process as Maven,
  making it a bit faster. If Maven is not running under Java 8, then will
  fall back to forking the process and using the Java agent mechanism

### Retrolambda 1.5.0 (2014-07-19)

- Maven plugin: use the [JDK from Maven Toolchains](http://maven.apache.org/plugins/maven-toolchains-plugin/toolchains/jdk.html)
  if available. The `java8home` configuration parameter overrides this
  ([Issue #24](https://github.com/orfjackal/retrolambda/pull/24))

### Retrolambda 1.4.0 (2014-07-04)

- Added an optional `-Dretrolambda.includedFiles` parameter to support the
  incremental compilers of build tools
  ([Issue #23](https://github.com/orfjackal/retrolambda/pull/23))
- Decides which lambda classes to save based on the current class being
  processed, instead of the class loader that loaded the lambda class
  ([Issue #21](https://github.com/orfjackal/retrolambda/issues/21))

### Retrolambda 1.3.0 (2014-06-04)

- Maven plugin: made the input and output directories configurable
  ([Issue #20](https://github.com/orfjackal/retrolambda/issues/20))
- Maven plugin: by default use the current JRE for running Retrolambda.
  For the old behavior, add `<java8home>${env.JAVA8_HOME}</java8home>`
  to the plugin configuration

### Retrolambda 1.2.3 (2014-05-19)

- Android: Fixed NoSuchMethodError when calling a private method to which
  there is a method reference
  ([Issue #18](https://github.com/orfjackal/retrolambda/issues/18))
- Fixed the possibility of accidentally overriding private methods to which
  there is method reference
  ([Issue #19](https://github.com/orfjackal/retrolambda/issues/19))

### Retrolambda 1.2.2 (2014-05-15)

- Fixed method references to private methods; will now make them
  package-private the same way as lambda implementation methods
  ([Issue #17](https://github.com/orfjackal/retrolambda/issues/17))

### Retrolambda 1.2.1 (2014-05-04)

- Fixed the Retrolambda Maven plugin not using the project's classpath
  ([Issue #16](https://github.com/orfjackal/retrolambda/issues/16))
- Maven plugin: save `retrolambda.jar` under `target/retrolambda/`
- Suppress false warning about class initializer methods on interfaces

### Retrolambda 1.2.0 (2014-05-02)

- Maven plugin for running Retrolambda
  (thanks, [Dave Moten](https://github.com/davidmoten))

### Retrolambda 1.1.4 (2014-03-29)

- Removes from interfaces bridge methods which were generated by JDK 8 e.g.
  when an interface overrides a method and refines its return type
  ([Issue #13](https://github.com/orfjackal/retrolambda/issues/13))

### Retrolambda 1.1.3 (2014-03-25)

- Fixed incompatibility with the Eclipse JDT compiler, version Kepler SR2
  with the Java 8 support patch 1.0.0.v20140317-1959
  ([Issue #12](https://github.com/orfjackal/retrolambda/issues/12))

### Retrolambda 1.1.2 (2014-01-08)

- Updated to work with JDK 8 Early Access Build b121 (2013-12-19)
  ([Issue #3](https://github.com/orfjackal/retrolambda/issues/3))

### Retrolambda 1.1.1 (2013-11-27)

- Show help if the `-javaagent` parameter is missing
  ([Issue #2](https://github.com/orfjackal/retrolambda/issues/2))

### Retrolambda 1.1.0 (2013-07-25)

- Create only one instance of lambdas which do not capture arguments; i.e.
  the same optimization as what JDK 8 does
- Start the sequence number of lambda classes from one (e.g.
  `com.example.Foo$$Lambda$1`) for each enclosing class

### Retrolambda 1.0.0 (2013-07-23)

- Backports lambda expressions and method handles to Java 7 and older
- Tested to work with JDK 8 Early Access Build b99 (2013-07-19)
