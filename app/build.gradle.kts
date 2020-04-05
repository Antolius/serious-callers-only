plugins {
    application
    id("org.springframework.boot")
    kotlin("plugin.spring")
}

application {
    mainClassName = "org.github.jantolis.seriouscallersonly.app.Main"
}

apply {
    plugin("io.spring.dependency-management")
}

dependencies {
    implementation(project(":bot"))

    implementation(kotlin("reflect"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("com.slack.api:bolt-servlet:1.0.3")
}