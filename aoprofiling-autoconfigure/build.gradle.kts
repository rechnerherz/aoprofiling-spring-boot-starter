plugins {
    // Kapt: Kotlin annotation processor
    // https://kotlinlang.org/docs/reference/kapt.html
    kotlin("kapt")
}

dependencies {
    api(project(":aoprofiling"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Annotation Processor
    // https://docs.spring.io/spring-boot/docs/current/reference/html/configuration-metadata.html#configuration-metadata-annotation-processor
    kapt("org.springframework.boot:spring-boot-configuration-processor")
}
