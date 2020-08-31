plugins {
    `java-library`
}

// Define all transitive (mandatory) dependency for consumer projects
dependencies {
    api(project(":aoprofiling-autoconfigure"))
    api(project(":aoprofiling"))
}
