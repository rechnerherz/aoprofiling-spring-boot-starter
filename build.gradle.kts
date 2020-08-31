plugins {

    // Java Library plugin
    // https://docs.gradle.org/current/userguide/java_library_plugin.html
    `java-library`

    // Kotlin JVM plugin (only used to configure subprojects here)
    // https://kotlinlang.org/docs/reference/using-gradle.html
    kotlin("jvm") version "1.4.0" apply false

    // Kotlin Spring compiler plugin to make classes open by default (only used to configure subprojects here)
    // https://kotlinlang.org/docs/reference/compiler-plugins.html
    kotlin("plugin.spring") version "1.4.0" apply false

    // Spring Boot dependency management
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/
    id("org.springframework.boot") version "2.3.3.RELEASE" apply false

    // Idea plugin
    // https://docs.gradle.org/current/userguide/idea_plugin.html
    id("idea")

    // Gradle Release plugin
    // https://github.com/researchgate/gradle-release
    id("net.researchgate.release") version "2.8.1"

    // Maven Publish plugin
    // https://docs.gradle.org/current/userguide/publishing_maven.html
    `maven-publish`
}

repositories {
    jcenter()
}

group = "at.rechnerherz"

release {
    buildTasks = listOf("releaseBuild")
}

tasks.register("releaseBuild") {
    dependsOn(subprojects.map { it.tasks.findByName("build") })
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "maven-publish")
    apply(plugin = "io.spring.dependency-management")

    group = "at.rechnerherz"

    repositories {
        jcenter()
    }

    // Import the Spring Boot Maven BOM
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/#managing-dependencies-dependency-management-plugin-using-in-isolation
    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // Set options for all Kotlin compilation tasks
    // https://kotlinlang.org/docs/reference/using-gradle.html
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            // Set target version for generate JVM bytecode
            jvmTarget = "1.8"
        }
    }

    tasks {
        val sourcesJar by creating(Jar::class) {
            dependsOn(JavaPlugin.CLASSES_TASK_NAME)
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        artifacts {
            archives(sourcesJar)
            archives(jar)
        }
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifact(tasks["sourcesJar"])

                pom {
                    name.set("AOProfiling Spring Boot Starter")
                    description.set("Aspect-oriented profiling Spring Boot starter.")
                    url.set("https://github.com/rechnerherz/aoprofiling")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("darioseidl")
                            name.set("Dario Seidl")
                            email.set("darioseidl@fastmail.fm")
                        }
                    }
                    scm {
                        connection.set("https://github.com/rechnerherz/aoprofiling.git")
                        developerConnection.set("git@github.com:rechnerherz/aoprofiling.git")
                        url.set("https://github.com/rechnerherz/aoprofiling")
                    }
                }
            }
        }
    }
}

tasks.named("afterReleaseBuild") {
    dependsOn(listOf(
        ":aoprofiling-autoconfigure:publish",
        ":aoprofiling-starter:publish",
        ":aoprofiling:publish"
    ))
}
