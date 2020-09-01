
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-web")
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

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_API_KEY")
    setPublications(publicationName)
    pkg.apply {
        repo = bintrayRepo
        name = "aoprofiling"
        desc = projectDescription
        userOrg = bintrayOrganization
        setLicenses(licenseShortName)
        vcsUrl = projectURL
        websiteUrl = projectURL
        issueTrackerUrl = issueURL
//        githubRepo = repoName
//        githubReleaseNotesFile = "README.md"
        version.apply {
            name = project.version.toString()
        }
    }
}
