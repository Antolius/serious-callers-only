dependencies {
    api(project(":api"))
    api("com.slack.api:bolt:1.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.5")
}
