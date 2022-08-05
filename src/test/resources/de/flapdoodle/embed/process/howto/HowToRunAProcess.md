# How to run a process

There are some preconditions if you want to start a process. This is an example where you download
an artifact, extract this artifact, provide command line arguments, start the process in an working
directory and, if done, clean up everything.

## provide every transition

This is an generic way to start an process.

```java
${genericSample}
```

```dot
${genericSample.sample.dot}
```

![dot file](HowToRunAProcess.png)

## use a custom class

This is one way to bundle common stuff and only specialize on the differences.

```java
${processFactorySample}
```

```dot
${processFactorySample.sample.dot}
```

![dot file](HowToRunAProcessWithFactory.png)



