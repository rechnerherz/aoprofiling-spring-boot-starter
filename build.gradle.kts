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

    // Dokka: Kotlin documentation engine
    // https://github.com/Kotlin/dokka
    // https://kotlinlang.org/docs/reference/kotlin-doc.html
    id("org.jetbrains.dokka") version "1.4.0-rc"

    // Gradle Release plugin
    // https://github.com/researchgate/gradle-release
    id("net.researchgate.release") version "2.8.1"

    // Maven Publish plugin
    // https://docs.gradle.org/current/userguide/publishing_maven.html
    `maven-publish`
}

// Always download Gradle sources and documentation (distribution type all)
// https://docs.gradle.org/current/userguide/gradle_wrapper.html#customizing_wrapper
tasks.named<Wrapper>("wrapper") {
    distributionType = Wrapper.DistributionType.ALL
}

repositories {
    // Gradle plugin portal needed for dokka 1.4.0-rc
    // https://github.com/Kotlin/dokka
    gradlePluginPortal()

    // jcenter repository
    // https://bintray.com/bintray/jcenter
    jcenter()
}

release {
    buildTasks = listOf("releaseBuild")
}

tasks.register("releaseBuild") {
    dependsOn(subprojects.map { it.tasks.findByName("build") })
}

tasks.named("afterReleaseBuild") {
    dependsOn(subprojects.map { it.tasks.findByName("publish") })
}

allprojects {
    group = "at.rechnerherz"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "maven-publish")
    apply(plugin = "io.spring.dependency-management")

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

    // Java source/target compatibility
    // https://docs.gradle.org/current/userguide/java_plugin.html#other_convention_properties
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
        val sourcesJar by registering(Jar::class) {
            dependsOn(JavaPlugin.CLASSES_TASK_NAME)
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        val javadocJar by registering(Jar::class) {
            dependsOn("dokkaJavadoc")
            archiveClassifier.set("javadoc")
            from(javadoc)
        }

        artifacts {
            archives(sourcesJar)
            archives(javadocJar)
            archives(jar)
        }
    }

    publishing {
        publications {
            register<MavenPublication>("mavenJava") {
                from(components["java"])
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])

                pom {
                    name.set("AOProfiling Spring Boot Starter")
                    description.set("Aspect-oriented profiling Spring Boot starter.")
                    url.set("https://github.com/rechnerherz/aoprofiling-spring-boot-starter")

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
                        connection.set("https://github.com/rechnerherz/aoprofiling-spring-boot-starter.git")
                        developerConnection.set("git@github.com:rechnerherz/aoprofiling-spring-boot-starter.git")
                        url.set("https://github.com/rechnerherz/aoprofiling-spring-boot-starter")
                    }
                }
            }
        }
    }
}
