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

group = "at.rechnerherz.aoprofiling"

fun Project.envConfig() = object : kotlin.properties.ReadOnlyProperty<Any?, String?> {
    override fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): String? =
        if (extensions.extraProperties.has(property.name)) {
            extensions.extraProperties[property.name] as? String
        } else {
            System.getenv(property.name)
        }
}

release {
    buildTasks = listOf("releaseBuild")
}

tasks.register("releaseBuild") {
    dependsOn(subprojects.map { it.tasks.findByName("build") })
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")

    group = "at.rechnerherz.aoprofiling"

    repositories {
        jcenter()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            suppressWarnings = true
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks {
        val sourcesJar by creating(Jar::class) {
            dependsOn(JavaPlugin.CLASSES_TASK_NAME)
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        val javadocJar by creating(Jar::class) {
            dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
            archiveClassifier.set("javadoc")
            from(javadoc)
        }

        artifacts {
            archives(sourcesJar)
            archives(javadocJar)
            archives(jar)
        }
    }

    tasks.withType<Javadoc> {
        if (JavaVersion.current().isJava8Compatible) {
            (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
        }
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
    }

    publishing {
        repositories {
            maven {
                url = uri("$buildDir/repository")
            }
        }
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])

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
    }
}

tasks.named("afterReleaseBuild") {
    dependsOn(listOf(
        ":aoprofiling-autoconfigure:publish",
        ":aoprofiling-starter:publish",
        ":aoprofiling:publish"
    ))
}
