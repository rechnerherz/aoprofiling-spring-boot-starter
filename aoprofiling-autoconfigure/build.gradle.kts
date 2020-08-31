val springBootVersion: String by extra

dependencies {
    api(project(":aoprofiling"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-aop:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    annotationProcessor ("org.springframework.boot:spring-boot-autoconfigure-processor:$springBootVersion")
}
