import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.gradle.plugin-publish") version "1.2.1"
    id("maven-publish")
    id("org.jetbrains.dokka")  version "1.9.20" //KDocs
}

group = "asia.hombre.neorust"
version = "0.1.0"
val officialName = "Neo Rust Gradle Plugin"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
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
            version = project.version.toString()
            description = project.description
            displayName = officialName
            tags.set(listOf("plugin", "gradle", "rust", "cargo"))
        }
    }
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.dokkaJavadoc)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            from(components["kotlin"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set(officialName)
                description.set(project.description)
                url.set("https://github.com/ronhombre/neo-rust-gradle-plugin")
                groupId = project.group.toString()
                artifactId = project.group.toString() + ".gradle.plugin"
                version = project.version.toString()

                licenses {
                    license {
                        name.set("The MIT License")
                    }
                }
                developers {
                    developer {
                        name.set("Ron Lauren Hombre")
                        email.set("ronlauren@hombre.asia")
                    }
                }
                scm {
                    url.set("https://github.com/ronhombre/neo-rust-gradle-plugin")
                }
            }
        }
    }
    repositories {
        mavenLocal()
    }
}

tasks.dokkaHtml.configure {
    dokkaSourceSets {
        named("main") {
            perPackageOption {
                matchingRegex.set(".*")
                includeNonPublic.set(false)
            }
            reportUndocumented.set(true)
        }
    }
}

tasks.withType<DokkaTask>().configureEach {
    val dokkaBaseConfiguration = """
    {
      "footerMessage": "(C) 2024 Ron Lauren Hombre"
    }
    """
    pluginsMapConfiguration.set(
        mapOf(
            "org.jetbrains.dokka.base.DokkaBase" to dokkaBaseConfiguration
        )
    )
}