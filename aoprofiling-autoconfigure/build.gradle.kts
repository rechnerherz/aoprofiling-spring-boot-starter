plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
}

val springBootVersion: String by extra
dependencies {
    api(project(":aoprofiling"))

    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("org.jetbrains.kotlin:kotlin-reflect")

    compileOnly("org.springframework.boot:spring-boot-starter:$springBootVersion")
    compileOnly("org.springframework.boot:spring-boot-starter-aop:$springBootVersion")
    compileOnly("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    annotationProcessor ("org.springframework.boot:spring-boot-autoconfigure-processor:$springBootVersion")
    annotationProcessor ("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")
}
