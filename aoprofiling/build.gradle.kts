plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    mavenCentral()
}

val springBootVersion: String by extra
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.google.guava:guava:28.2-jre")
    implementation("io.github.microutils:kotlin-logging:1.7.8")
    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-aop:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
}
