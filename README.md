# AOProfiling Spring Boot Starer

Aspect-oriented profiling in Spring Boot.

This Spring Boot Starter provides a convenient way to log and profile
 all public method calls of Spring controller, service, and repository beans.

Just add the starter to your project and enable it in application properties 
and see everything that is happening in your Spring app, and how long each method takes to execute.

The logging can be fine-tuned, and it can also print a summary of execution times.

A typical output may look like this:

```
2020-09-01 14:37:38.708 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : ┌ TestDataService.run()
2020-09-01 14:37:38.719  INFO 57904 --- [           main] a.r.example.init.TestDataService         : Populating test data ...
2020-09-01 14:37:38.988 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├─ AdminRepository.internalSave(Admin{id=null, username=admin@example.com})
2020-09-01 14:37:39.155 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├── AccountRepository.findAuthenticated()
2020-09-01 14:37:39.161 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├── AccountRepository.findAuthenticated() returned null — execution time: 6 ms
2020-09-01 14:37:39.307 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├─ AdminRepository.internalSave(Admin{id=null, username=admin@example.com}) returned Admin{id=1, username=admin@example.com} — execution time: 319 ms
2020-09-01 14:37:39.582 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├─ CustomerRepository.internalSave(Customer{id=null, username=customer@example.com})
2020-09-01 14:37:39.772 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├── AccountRepository.findAuthenticated()
2020-09-01 14:37:39.776 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├── AccountRepository.findAuthenticated() returned null — execution time: 4 ms
2020-09-01 14:37:39.803 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├─ CustomerRepository.internalSave(Customer{id=null, username=customer@example.com}) returned Customer{id=2, username=customer@example.com} — execution time: 221 ms
2020-09-01 14:37:39.805 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├─ DocumentService.saveResource(class path resource [dev/sample.pdf], PDF, test-pdf)
2020-09-01 14:37:39.807 DEBUG 57904 --- [           main] a.r.e.domain.document.DocumentService    : Copying class path resource [dev/sample.pdf] to URL [file:/home/dario/.example/example-api/documents/ac16758a-dbd2-4a94-af5d-2be4774ee33f.pdf]
2020-09-01 14:37:39.816 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├─ DocumentService.saveResource(class path resource [dev/sample.pdf], PDF, test-pdf) returned Document{id=null, uuid=ac16758a-dbd2-4a94-af5d-2be4774ee33f} — execution time: 11 ms
2020-09-01 14:37:39.850 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├─ DocumentRepository.internalSave(Document{id=null, uuid=ac16758a-dbd2-4a94-af5d-2be4774ee33f})
2020-09-01 14:37:39.863 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├── AccountRepository.findAuthenticated()
2020-09-01 14:37:39.864 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├── AccountRepository.findAuthenticated() returned null — execution time: 1 ms
2020-09-01 14:37:39.885 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├─ DocumentRepository.internalSave(Document{id=null, uuid=ac16758a-dbd2-4a94-af5d-2be4774ee33f}) returned Document{id=1, uuid=ac16758a-dbd2-4a94-af5d-2be4774ee33f} — execution time: 35 ms
2020-09-01 14:37:39.886  INFO 57904 --- [           main] a.r.example.init.TestDataService         : Populating test data done
2020-09-01 14:37:39.902 TRACE 57904 --- [           main] a.r.aoprofiling.ProfilingAspect          : └ TestDataService.run() returned void — execution time: 1194 ms
```
 
# Usage

Add `at.rechnerherz:aoprofiling-starter:1.0.0` to your Gradle or Maven Project. 

Enable the auto-configuration and set the log level for the `ProfilingAspect` and `ProfilingSummaryAspect` 
to `trace`/`debug` to see the logging output.

For example, in your `application-development.properties`:

    # Enable AOP profiling
    at.rechnerherz.aoprofiling.enabled=true
    
    # AOP profiling log levels
    logging.level.at.rechnerherz.aoprofiling.ProfilingAspect=trace
    logging.level.at.rechnerherz.aoprofiling.ProfilingSummaryAspect=debug

To avoid any overhead in production, only enable it in development. 
It makes no sense to run this in a production environment, it would produce way too much logging output.

To make sure the ProfilingAspect is executed before (around) transactions, set the
transaction advisor order to a lower priority (higher number) than the `profilingAspectOrder` (default -1),
e.g. with `@EnableTransactionManagement(order = 0)` in your JPA configuration.

# Fine-tuning

You can exclude a class or method from being profiled, either by annotating it with `@NoProfiling`
or with the `ignore` property.
 
The `ignore` property takes a comma-separated list of simple class name `.` method name. For example:

    # Don't profile the following methods:
    at.rechnerherz.aoprofiling.ignore=AccountRepository.findAuthenticated,AccountController.authenticatedAccount

You can change the log output using the `mode` and `truncate` properties.

- `mode=tree` uses tree drawing glyphs
- `mode=verbose` prints on multiple lines
- `mode=plain` does neither

Strings will be truncated to at most `truncate` number of characters, unless mode is `verbose`.

# Profiling Summary

You can print a summary of execution times by annotation a method with `@ProfilingSummary`.

Use `clearBefore=true` (default) to clear the statistics before the method execution 
(i.e. to only print a summary of the current execution). Set it to `false` to aggregate execution times.

# How it works

The `ProfilingAspect` traces the execution of public methods:

- within `org.springframework.stereotype.Service` classes

- within `org.springframework.stereotype.Controller` classes

- within `org.springframework.web.bind.annotation.RestController` classes

- within `org.springframework.data.rest.webmvc.BasePathAwareController` classes

    (this also includes `org.springframework.data.rest.webmvc.RepositoryRestController` classes)

- within `org.springframework.data.repository.Repository` and sub-classes

- within `org.springframework.data.rest.core.annotation.RepositoryEventHandler` classes

When using Spring proxy-based AOP, it only works for non-final public methods of non-final public Spring beans,
when they are not self invocated.

You can read more about Spring AOP in their [documentation](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop).

# Dependencies

The starter is written in Kotlin. It can also be used in a Java (1.8+) project. 

It depends on `spring-boot-starter-aop`, `spring-boot-starter-web`, and SLF4J for logging.

# License

Apache License, Version 2.0

# Thanks

Thanks to [roamingthings](https://github.com/roamingthings/workbench-spring-boot-starter-gradle), 
this project helped me to figure out how to set up this as a Spring Boot starter library and publish it to Maven.

