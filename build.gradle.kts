import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "2.0.0"
    id("maven-publish")
    id("org.jetbrains.dokka")  version "2.1.0" //KDocs
    id("org.jetbrains.dokka-javadoc") version "2.1.0"
}

group = "asia.hombre.neorust"
version = "0.5.1"
description = "Build Rust Crates within Gradle and integrate with other Gradle projects"
val officialName = "Neo Rust Gradle Plugin"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
    testCompileOnly(gradleApi())
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        create(project.group.toString()) {
            id = project.group.toString()
            implementationClass = "asia.hombre.neorust.Rust"
            vcsUrl = "https://github.com/ronhombre/neo-rust-gradle-plugin"
            website = "https://github.com/ronhombre/neo-rust-gradle-plugin"
            version = project.version.toString()
            description = project.description
            displayName = officialName
            tags.set(listOf("rust", "cargo"))
        }
    }
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.dokkaGeneratePublicationJavadoc)
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        mavenLocal()
    }
}

dokka {
    pluginsConfiguration.html {
        footerMessage = "Copyright (c) 2025 Ron Lauren Hombre"
    }

    dokkaPublications.html {
        dokkaSourceSets {
            named("main") {
                perPackageOption {
                    matchingRegex.set(".*")
                }
                reportUndocumented.set(true)
                documentedVisibilities(
                    VisibilityModifier.Public,
                    VisibilityModifier.Protected,
                )
            }
        }
    }
}