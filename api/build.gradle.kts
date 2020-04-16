plugins {
    `maven-publish`
}

dependencies {
    api(kotlin("scripting-common"))
    api(kotlin("scripting-jvm"))
}

val sourcesJar by tasks.registering(Jar::class) {
    this.archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        register<MavenPublication>("library") {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
    repositories {
        mavenLocal()
    }
}
