plugins {
    application
    id("org.springframework.boot")
    kotlin("plugin.spring")
    id("com.gorylenko.gradle-git-properties") version "2.2.2"
    id("com.palantir.docker")
}

apply {
    plugin("io.spring.dependency-management")
    plugin("org.springframework.boot")
    plugin("com.gorylenko.gradle-git-properties")
    plugin("com.palantir.docker")
}

application {
    mainClassName = "hr.from.josipantolis.seriouscallersonly.app.Main"
}

tasks {
    val unpack = create<Copy>("unpack") {
        dependsOn += bootJar
        from(zipTree(getByName(bootJar.name).outputs.files.singleFile))
        into("build/dependency")
    }

    docker {
        name = "hr.from.josipantolis/${rootProject.name}"
        copySpec.from(unpack.outputs).into("dependency")
        buildArgs(mapOf(
            "DEPENDENCY" to "dependency",
            "MAIN_CLASS" to application.mainClassName
        ))
    }
}


ext["okhttp3.version"] = "4.4.1"

dependencies {
    implementation(project(":slack-runtime"))

    implementation(kotlin("reflect"))
    
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    implementation("com.slack.api:bolt-servlet:1.0.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
