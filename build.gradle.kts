plugins {
    kotlin("jvm") version "1.3.71"
}

allprojects {
    group = "org.github.jantolis.seriouscallersonly"
    version = "0.1.0-SNAPSHOT"

    repositories {
        jcenter()
    }
}

configure(subprojects) {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    dependencies {
        implementation(kotlin("stdlib"))
    }
}