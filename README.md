# Javaagent to make dacapo's eclipse benchmark work on new Java & on mac

The benchmark doesn't work so well on Java 8 these days, and definitely not if you are using a modified set of Java runtime libraries ([see bug](https://sourceforge.net/p/dacapobench/bugs/100/)).

For instance, you might run into this head-scratcher:

```
===== DaCapo 9.12 eclipse starting =====
Initialize workspace ...................
Index workspace .....................
Build workspace Unexpected ERROR marker(s):
org.eclipse.update.configurator:
	The project was not built since its build path is incomplete. Cannot find the class file for java.lang.CharSequence. Fix the build path then try building this project
SiteEntry.java:
	The type java.lang.CharSequence cannot be resolved. It is indirectly referenced from required .class files
org.eclipse.text:
	The project was not built since its build path is incomplete. Cannot find the class file for java.util.Comparator. Fix the build path then try building this project
UndoEdit.java:
	The type java.util.Comparator cannot be resolved. It is indirectly referenced from required .class files
org.eclipse.team.core:
	The project was not built since its build path is incomplete. Cannot find the class file for java.lang.CharSequence. Fix the build path then try building this project
StringMatcher.java:
	The type java.lang.CharSequence cannot be resolved. It is indirectly referenced from required .class files
ValidateEditChecker.java:
	Arrays cannot be resolved
ValidateEditChecker.java:
	The import java.util.Arrays cannot be resolved
ParticipantExtensionPoint.java:
	The import java.util.Arrays cannot be resolved
ParticipantExtensionPoint.java:
	Arrays cannot be resolved
TextChange.java:
	The import java.util.Arrays cannot be resolved
TextChange.java:
	Arrays cannot be resolved
TextChange.java:
	Arrays cannot be resolved
TextChange.java:
	Arrays cannot be resolved
DocumentChange.java:
	Cannot reduce the visibility of the inherited method from TextChange
DocumentChange.java:
	Cannot reduce the visibility of the inherited method from TextChange
DocumentChange.java:
	Cannot reduce the visibility of the inherited method from TextChange
DocumentChange.java:
	Cannot reduce the visibility of the inherited method from TextChange
org.eclipse.jdt.core:
	The project was not built since its build path is incomplete. Cannot find the class file for java.lang.CharSequence. Fix the build path then try building this project
JDTCompilerAdapter.java:
	The type java.lang.CharSequence cannot be resolved. It is indirectly referenced from required .class files
org.eclipse.core.variables:
	The project was not built since its build path is incomplete. Cannot find the class file for java.lang.CharSequence. Fix the build path then try building this project
StringSubstitutionEngine.java:
	The type java.lang.CharSequence cannot be resolved. It is indirectly referenced from required .class files
org.eclipse.core.runtime.compatibility:
	The project was not built since its build path is incomplete. Cannot find the class file for java.lang.CharSequence. Fix the build path then try building this project
PluginActivator.java:
	The type java.lang.CharSequence cannot be resolved. It is indirectly referenced from required .class files
org.eclipse.core.runtime:
	The project was not built since its build path is incomplete. Cannot find the class file for java.lang.CharSequence. Fix the build path then try building this project
MultiRule.java:
	The type java.lang.CharSequence cannot be resolved. It is indirectly referenced from required .class files
org.eclipse.core.resources:
	The project was not built since its build path is incomplete. Cannot find the class file for java.lang.CharSequence. Fix the build path then try building this project
ElementTreeIterator.java:
	The type java.lang.CharSequence cannot be resolved. It is indirectly referenced from required .class files
org.eclipse.core.filebuffers:
	The project was not built since its build path is incomplete. Cannot find the class file for java.nio.CharBuffer. Fix the build path then try building this project
ResourceTextFileBuffer.java:
	The type java.nio.CharBuffer cannot be resolved. It is indirectly referenced from required .class files
org.eclipse.core.expressions:
	The project was not built since its build path is incomplete. Cannot find the class file for java.lang.CharSequence. Fix the build path then try building this project
TypeExtensionManager.java:
	The type java.lang.CharSequence cannot be resolved. It is indirectly referenced from required .class files
org.eclipse.ant.core:
	The project was not built since its build path is incomplete. Cannot find the class file for java.lang.CharSequence. Fix the build path then try building this project
AntRunner.java:
	The type java.lang.CharSequence cannot be resolved. It is indirectly referenced from required .class files
org.apache.lucene:
	The project was not built since its build path is incomplete. Cannot find the class file for java.lang.CharSequence. Fix the build path then try building this project
TokenMgrError.java:
	The type java.lang.CharSequence cannot be resolved. It is indirectly referenced from required .class files

--------------------
```

And if you try to use the `-Declipse.java.home=/some/path/to/java7` you'd see

```
===== DaCapo 9.12 eclipse starting =====
Initialize workspace ...................Error creating workspace!
java.lang.NullPointerException
	at org.eclipse.jdt.core.tests.performance.FullSourceWorkspaceTests.setUpFullSourceWorkspace(FullSourceWorkspaceTests.java:339)
	at org.eclipse.jdt.core.tests.performance.FullSourceWorkspaceTests.setup(FullSourceWorkspaceTests.java:103)
	at org.dacapo.eclipse.BenchmarkRunner.start(BenchmarkRunner.java:67)
	at org.eclipse.equinox.internal.app.EclipseAppHandle.run(EclipseAppHandle.java:194)
	at org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher.runApplication(EclipseAppLauncher.java:110)
	at org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher.start(EclipseAppLauncher.java:79)
	at org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher.reStart(EclipseAppLauncher.java:155)
	at org.eclipse.core.runtime.adaptor.EclipseStarter.run(EclipseStarter.java:370)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.dacapo.harness.Eclipse.iterate(Eclipse.java:65)
	at org.dacapo.harness.Benchmark.run(Benchmark.java:166)
	at org.dacapo.harness.TestHarness.runBenchmark(TestHarness.java:218)
	at org.dacapo.harness.TestHarness.main(TestHarness.java:171)
	at Harness.main(Harness.java:17)
```

I have no idea why, but this is some problem in the Eclipse libraries and it returns `null` for the JRE class path under Java 8 when you set JAVA_HOME to a java 7 JVM.

The obvious (after much headbanging) answer is just to force it to return the path to the java 7 `rt.jar`. Which we can do without recompiling DaCapo by just rewriting the method. So, that's what this does.

Compile and run this javaagent (and add it to the classpath) of DaCapo when you run it, and pass `-Declipse.java.home=...` to a valid java 7 directory (to the directory of the JRE). Copy your tzdb.dat file from $JAVA_HOME/lib/ to the java 7 home/lib folder first.


Example:
`/Library/Java/JavaVirtualMachines/jdk1.8.0_71-debug.jdk/Contents/Home/jre/bin/java -Declipse.java.home=/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/jre -javaagent:../../ker/target/dacapo-eclipse-hacker-0.0.1-SNAPSHOT.jar -Xbootclasspath/p:../../dacapo-eclipse-hacker/target/dacapo-eclipse-hacker-0.0.1-SNAPSHOT.jar -jar dacapo-9.12-bach.jar eclipse`
