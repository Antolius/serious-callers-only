import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    idea
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.71" apply false
    id("org.springframework.boot") version "2.2.0.RELEASE" apply false
    id("io.spring.dependency-management") version "1.0.8.RELEASE" apply false
    id("com.palantir.docker") version "0.25.0" apply false
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

allprojects {
    group = "hr.from.josipantolis.seriouscallersonly"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
        testImplementation("org.assertj:assertj-core:3.15.0")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.3.1")
    }
}