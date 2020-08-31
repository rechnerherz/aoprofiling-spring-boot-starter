plugins {

    // Java Library plugin
    // https://docs.gradle.org/current/userguide/java_library_plugin.html
    `java-library`

    // Kotlin JVM plugin (only used to configure subprojects here)
    // https://kotlinlang.org/docs/reference/using-gradle.html
    kotlin("jvm") version "1.4.0" apply false

    // Kotlin Spring compiler plugin to make classes open by default
    // https://kotlinlang.org/docs/reference/compiler-plugins.html
    kotlin("plugin.spring") version "1.4.0"

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
    apply(plugin = "maven-publish")

    group = "at.rechnerherz"

    repositories {
        jcenter()
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
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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
        println(configurations.runtime.allDependencies)
    }
}

tasks.named("afterReleaseBuild") {
    dependsOn(listOf(
        ":aoprofiling-autoconfigure:publish",
        ":aoprofiling-starter:publish",
        ":aoprofiling:publish"
    ))
}
