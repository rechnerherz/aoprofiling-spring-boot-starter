# AOProfiling Spring Boot Starer

Aspect-oriented profiling in Spring Boot.

This Spring Boot Starter provides a convenient way to log and profile
 all public method calls of Spring controller, service, and repository beans.

Just add the starter to your project and enable it in application properties 
and see everything that is happening in your Spring app, and how long each method takes to execute.

The logging can be fine-tuned, and it can also print a summary of execution times.

A typical output may look like this:

```
2020-09-01 10:28:03.502 TRACE 18257 --- [   scheduling-1] a.r.aoprofiling.ProfilingAspect          : ┌ ScheduledService.pendingBookingMailScheduler()
2020-09-01 10:28:03.520 TRACE 18257 --- [           main] a.r.aoprofiling.ProfilingAspect          : ┌ TestDataService.run()
2020-09-01 10:28:03.524 TRACE 18257 --- [   scheduling-1] a.r.aoprofiling.ProfilingAspect          : ├─ BookingService.pendingBookingMailsScheduler(2020-09-01T08:18:03.510217Z, 2020-09-01T08:23:03.510217Z)
2020-09-01 10:28:03.533 TRACE 18257 --- [   scheduling-1] a.r.aoprofiling.ProfilingAspect          : ├── BookingRepository.findPendingByDateRange(2020-09-01T08:18:03.510217Z, 2020-09-01T08:23:03.510217Z)
2020-09-01 10:28:03.538  INFO 18257 --- [           main] at.rechnerherz.tnt.init.TestDataService  : Skip populating data
2020-09-01 10:28:03.541 TRACE 18257 --- [           main] a.r.aoprofiling.ProfilingAspect          : └ TestDataService.run() returned void — execution time: 21 ms
2020-09-01 10:28:03.541 TRACE 18257 --- [           main] a.r.aoprofiling.ProfilingAspect          : ┌ ScheduledService.run()
2020-09-01 10:28:03.542 TRACE 18257 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├─ SitemapService.generateSitemapAndRobotsTxt(2020-09-01T08:28:03.542382Z)
2020-09-01 10:28:03.558 TRACE 18257 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├── IntegrationRepository.internalFindAll()
2020-09-01 10:28:03.641 TRACE 18257 --- [   scheduling-1] a.r.aoprofiling.ProfilingAspect          : ├── BookingRepository.findPendingByDateRange(2020-09-01T08:18:03.510217Z, 2020-09-01T08:23:03.510217Z) returned empty ArrayList — execution time: 108 ms
2020-09-01 10:28:03.642 TRACE 18257 --- [   scheduling-1] a.r.aoprofiling.ProfilingAspect          : ├─ BookingService.pendingBookingMailsScheduler(2020-09-01T08:18:03.510217Z, 2020-09-01T08:23:03.510217Z) returned void — execution time: 118 ms
2020-09-01 10:28:03.642 TRACE 18257 --- [   scheduling-1] a.r.aoprofiling.ProfilingAspect          : └ ScheduledService.pendingBookingMailScheduler() returned void — execution time: 140 ms
2020-09-01 10:28:03.696 TRACE 18257 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├── IntegrationRepository.internalFindAll() returned ArrayList containing 5 Integration — execution time: 138 ms
2020-09-01 10:28:03.755 TRACE 18257 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├── BundleRepository.findByActiveTrueAndCompanyIn(PersistentSet containing 3 Company)
2020-09-01 10:28:03.871 TRACE 18257 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├── BundleRepository.findByActiveTrueAndCompanyIn(PersistentSet containing 3 Company) returned ArrayList containing 41 Bundle — execution time: 116 ms
2020-09-01 10:28:04.044 TRACE 18257 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├─ SitemapService.generateSitemapAndRobotsTxt(2020-09-01T08:28:03.542382Z) returned void — execution time: 502 ms
2020-09-01 10:28:04.045 TRACE 18257 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├─ CSBusWatchService.deleteOldBackups(2020-09-01T08:28:03.542382Z)
2020-09-01 10:28:04.049 DEBUG 18257 --- [           main] a.r.tnt.external.aws.FileActionHandler   : Deleting files in /home/dario/.tnt/tnt-api/backup/csbus older than 2020-03-05T08:28:03.542382Z
2020-09-01 10:28:04.110 TRACE 18257 --- [           main] a.r.aoprofiling.ProfilingAspect          : ├─ CSBusWatchService.deleteOldBackups(2020-09-01T08:28:03.542382Z) returned void — execution time: 65 ms
2020-09-01 10:28:04.111 TRACE 18257 --- [           main] a.r.aoprofiling.ProfilingAspect          : └ ScheduledService.run() returned void — execution time: 570 ms

```
 
# Usage

Add `at.rechnerherz:aoprofiling-starter:1.0.0` to your Gradle or Maven Project. 

Enable the auto-configuration and set the log level to `trace` to see the logging output.

For example, in your `application-development.properties`:

    # Enable AOP profiling
    at.rechnerherz.aoprofiling.enabled=true
    
    # AOP method profiling log level
    logging.level.at.rechnerherz.aoprofiling.ProfilingAspect=trace

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

