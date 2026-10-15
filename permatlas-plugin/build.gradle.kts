plugins {
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    implementation(project(":permatlas-api"))
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("PermAtlas")
    archiveClassifier.set("")
    archiveVersion.set("")
    // 不要 relocate，接口包名必须稳定
}

tasks.build {
    dependsOn(tasks.named("shadowJar"))
}