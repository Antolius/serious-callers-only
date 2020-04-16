plugins {
    application
    id("org.springframework.boot")
    kotlin("plugin.spring")
}

application {
    mainClassName = "hr.from.josipantolis.seriouscallersonly.app.Main"
}

apply {
    plugin("io.spring.dependency-management")
}

ext["okhttp3.version"] = "4.4.1"

dependencies {
    implementation(project(":bot"))

    implementation(kotlin("reflect"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("com.slack.api:bolt-servlet:1.0.3")
}