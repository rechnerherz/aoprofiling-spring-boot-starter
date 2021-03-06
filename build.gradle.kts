plugins {

    // Java Library plugin
    // https://docs.gradle.org/current/userguide/java_library_plugin.html
    `java-library`

    // Kotlin JVM plugin
    // https://kotlinlang.org/docs/reference/using-gradle.html
    kotlin("jvm") version "1.4.0"

    // Kotlin Spring compiler plugin to make classes open by default
    // https://kotlinlang.org/docs/reference/compiler-plugins.html
    kotlin("plugin.spring") version "1.4.0"

    // Spring Boot dependency management
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/
    id("org.springframework.boot") version "2.3.3.RELEASE" apply false

    // Idea plugin
    // https://docs.gradle.org/current/userguide/idea_plugin.html
    id("idea")

    // Dokka: Kotlin documentation engine
    // https://github.com/Kotlin/dokka
    // https://kotlinlang.org/docs/reference/kotlin-doc.html
    id("org.jetbrains.dokka") version "1.4.0"

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

val groupName: String by project

val projectName: String by project
val projectDescription: String by project
val projectURL: String by project

val licenseName: String by project
val licenseShortName: String by project
val licenseURL: String by project

val developerID: String by project
val developerName: String by project

val organizationName: String by project
val organizationURL: String by project

val repoName: String by project
val repoURL: String by project
val repoDevURL: String by project

val issueURL: String by project

val publicationName: String by project
val bintrayRepo: String by project
val bintrayOrganization: String by project

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

    // Enable explicit API mode
    // https://kotlinlang.org/docs/reference/whatsnew14.html#explicit-api-mode-for-library-authors
    kotlin {
        explicitApi()
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
}

// Bintray upload has to be configured separately for each subproject
// https://github.com/bintray/gradle-bintray-plugin/issues/104
tasks.register("bintrayUploadAll") {
    dependsOn(subprojects.map { it.tasks.findByName("bintrayUpload") })
}
