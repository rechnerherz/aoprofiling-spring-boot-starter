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

    // Bintray plugin
    // https://github.com/bintray/gradle-bintray-plugin
    id("com.jfrog.bintray") version "1.8.5"
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

// Workaround for release plugin Kotlin DSL
// https://github.com/researchgate/gradle-release/issues/281
fun net.researchgate.release.ReleaseExtension.git(configureFn : net.researchgate.release.GitAdapter.GitConfig.() -> Unit) {
    (propertyMissing("git") as net.researchgate.release.GitAdapter.GitConfig).configureFn()
}

release {
    buildTasks = listOf("releaseBuild")
    git {
        // Don't push to remote
        pushToRemote = null
    }
}

tasks.register("releaseBuild") {
    dependsOn(subprojects.map { it.tasks.findByName("build") })
}

tasks.named("afterReleaseBuild") {
    dependsOn(listOf(
        ":aoprofiling:publish",
        ":aoprofiling-autoconfigure:publish",
        ":aoprofiling-spring-boot-starter:publish"
    ))
}

val groupName = "at.rechnerherz"

val projectName = "aoprofiling-spring-boot-starter"
val projectDescription = "Aspect-oriented profiling Spring Boot starter."
val projectURL = "https://github.com/rechnerherz/aoprofiling-spring-boot-starter"

val licenseName = "The Apache License, Version 2.0"
val licenseShortName = "Apache-2.0"
val licenseURL = "https://www.apache.org/licenses/LICENSE-2.0.txt"

val developerID = "darioseidl"
val developerName = "Dario Seidl"

val organizationName = "Rechnerherz"
val organizationURL = "https.//www.rechnerherz.at"

val repoName = "rechnerherz/$projectName"
val repoURL = "https://github.com/$repoName.git"
val repoDevURL = "git@github.com:$repoName.git"

val issueURL = "$projectURL/issues"

val publicationName = "mavenJava"
val bintrayRepo = "maven"
val bintrayOrganization = "rechnerherz"

allprojects {
    group = groupName
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "maven-publish")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.jfrog.bintray")

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
            register<MavenPublication>(publicationName) {
                from(components["java"])
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])

                pom {
                    name.set(projectName)
                    description.set(projectDescription)
                    url.set(projectURL)

                    licenses {
                        license {
                            name.set(licenseName)
                            url.set(licenseURL)
                        }
                    }
                    developers {
                        developer {
                            id.set(developerID)
                            name.set(developerName)
                            organization.set(organizationName)
                            organizationUrl.set(organizationURL)
                        }
                    }
                    scm {
                        connection.set(repoURL)
                        developerConnection.set(repoDevURL)
                        url.set(projectURL)
                    }
                }
            }
        }
    }

    bintray {
        user = System.getenv("BINTRAY_USER")
        key = System.getenv("BINTRAY_API_KEY")
        setPublications(publicationName)
        pkg.apply {
            repo = bintrayRepo
            name = projectName
            desc = projectDescription
            userOrg = bintrayOrganization
            setLicenses(licenseShortName)
            vcsUrl = projectURL
            websiteUrl = projectURL
            issueTrackerUrl = issueURL
            githubRepo = repoName
            githubReleaseNotesFile = "README.md"
            version.apply {
                name = project.version.toString()
            }
        }
    }
}
