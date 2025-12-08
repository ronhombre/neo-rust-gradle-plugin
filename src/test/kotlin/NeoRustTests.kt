/*
 * Copyright 2025 Ron Lauren Hombre (and the neo-rust-gradle-plugin contributors)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 *        and included as LICENSE.txt in this Project.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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