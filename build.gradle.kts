import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20"
    application
}

buildscript {
    dependencies {
        classpath("io.realm.kotlin:gradle-plugin:1.14.0-ENCRYPTION-POC-SNAPSHOT")
    }
}
rootProject.extra["realmVersion"] = "1.14.0-ENCRYPTION-POC-SNAPSHOT"

apply(plugin = "io.realm.kotlin")

group = "io.realm.example"
version = "1.0.0"

repositories {
    mavenCentral()
    // Only required for realm-kotlin snapshots
//    maven("https://oss.sonatype.org/content/repositories/snapshots")
    mavenLocal()
}
dependencies {
    implementation("com.jakewharton.fliptables:fliptables:1.1.0")
    implementation("io.realm.kotlin:library-base:${rootProject.extra["realmVersion"]}")
    testImplementation(kotlin("test-junit"))
}
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
application {
    mainClassName = "io.realm.example.MainKt"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.realm.example.MainKt"
    }
    configurations["runtimeClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
