# Javaagent to make dacapo's eclipse benchmark work on new Java & on mac

The benchmark doesn't work so well on Java 8 these days, and definitely not if you are using a modified set of Java runtime libraries ([see bug](https://sourceforge.net/p/dacapobench/bugs/100/)).

Compile and run this javaagent (and add it to the classpath) of DaCapo when you run it, and pass `-Declipse.java.home=...` to a valid java 7 directory (to the directory of the JRE).
