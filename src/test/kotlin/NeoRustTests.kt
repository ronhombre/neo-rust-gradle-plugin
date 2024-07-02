import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class NeoRustTests {
    private fun createProjectDir(): File {
        val projectDir = File("build/testProject")
        projectDir.mkdirs()
        projectDir.resolve("settings.gradle.kts").writeText(
            """
                pluginManagement {
                    repositories {
                        mavenLocal()
                    }
                }
            """.trimIndent())
        projectDir.resolve("build.gradle.kts").writeText(
            """
                plugins {
                    id("asia.hombre.neorust") version "0.2.0"
                }
            """.trimIndent())

        return projectDir
    }

    @Test
    fun createPluginTasks() {
        val result = GradleRunner.create()
            .withProjectDir(createProjectDir())
            .withPluginClasspath()
            .withEnvironment(mapOf("NEORUST_TESTING" to "true"))
            .withArguments("tasks")
            .build()

        assertTrue(result.output.contains("resolveRustDependencies"), "Resolving Dependencies task couldn't be found!")
        assertTrue(result.output.contains("generateCargoManifest"), "Manifest Generator task couldn't be found!")
        assertTrue(result.output.contains("benchNeoRust"), "Benchmarking task couldn't be found!")
        assertTrue(result.output.contains("buildNeoRust"), "Building task couldn't be found!")
        assertTrue(result.output.contains("cleanNeoRust"), "Cleaning task couldn't be found!")
        assertTrue(result.output.contains("publishNeoRust"), "Publishing task couldn't be found!")
        assertTrue(result.output.contains("testNeoRust"), "Testing task couldn't be found!")
    }
}