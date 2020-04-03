plugins {
    application
}

application {
    mainClassName = "org.github.jantolis.seriouscallersonly.app.Main"
}

dependencies {
    implementation(project(":bot"))
    implementation(project(":dsl"))
}