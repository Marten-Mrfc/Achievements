import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    kotlin("jvm") version "2.2.0-RC3"
    `maven-publish`
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "dev.marten_mrfcyt"
version = "1.0"

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name.toLowerCase()
            version = project.version.toString()
            
            from(components["java"])
        }
    }
}

repositories {
    mavenCentral()
    maven("https://javadoc.jitpack.io")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation("com.github.Marten-Mrfc:MLib:2611a9890f")
    implementation("com.google.code.gson:gson:2.10.1")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

task<ShadowJar>("buildAndMove") {
    dependsOn("shadowJar")

    group = "build"
    description = "Builds the jar and moves it to the server folder"

    doLast {
        val jar = file("build/libs/${project.name}-${version}-all.jar")
        val server = file("server/plugins/${project.name}-${version}.jar")

        if (server.exists()) {
            server.delete()
        }

        jar.copyTo(server, overwrite = true)
    }
}