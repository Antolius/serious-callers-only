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
    implementation(project(":dsl"))

    implementation(kotlin("reflect"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}