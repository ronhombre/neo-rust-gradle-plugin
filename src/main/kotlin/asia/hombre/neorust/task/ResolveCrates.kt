package asia.hombre.neorust.task

import asRustCrate
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Resolves local Gradle subprojects as crates
 *
 * @since 0.4.0
 * @author Ron Lauren Hombre
 */
abstract class ResolveCrates @Inject constructor(): DefaultTask() {
    @get:Inject
    abstract val objects: ObjectFactory

    @get:Internal
    internal abstract val cratesArtifacts: Property<ArtifactCollection>
    @get:Internal
    internal abstract val devCratesArtifacts: Property<ArtifactCollection>
    @get:Internal
    internal abstract val buildCratesArtifacts: Property<ArtifactCollection>
    @get:Internal
    internal abstract val referenceManifestPath: RegularFileProperty
    @get:OutputDirectory
    abstract val resolvedOutput: DirectoryProperty

    @TaskAction
    fun resolveCrates() {
        val outputDir = resolvedOutput.get().asFile

        if(outputDir.exists()) outputDir.deleteRecursively()

        outputDir.mkdirs()

        outputDir.resolve("README.md").writeText("""
            # Rust Crate Object
            The .rc files here are serialized Java objects of an internal class RustCrateObject. Do not touch them.
            ## Reason
            Gradle parallelism and cache configuration discourages in-memory passing of data between tasks. Tasks have
            to write to 'Output Files' which would then become 'Input Files' of other tasks. Only then can data be
            passed between tasks.
        """.trimIndent())
        cratesArtifacts.get().forEach { artifact ->
            outputDir.resolve("crates").apply {
                mkdir()
                writeToFile(this, artifact)
            }
        }

        devCratesArtifacts.get().forEach { artifact ->
            outputDir.resolve("devCrates").apply {
                mkdir()
                writeToFile(this, artifact)
            }
        }

        buildCratesArtifacts.get().forEach { artifact ->
            outputDir.resolve("buildCrates").apply {
                mkdir()
                writeToFile(this, artifact)
            }
        }
    }

    private fun writeToFile(directory: File, artifact: ResolvedArtifactResult) {
        artifact.asRustCrate(objects, referenceManifestPath.get().asFile)?.let { crate ->
            FileOutputStream(directory.resolve("${crate.name.hash()}.rc")).use { outputStream ->
                ObjectOutputStream(outputStream).writeObject(crate.toObject())
            }
        }
    }

    private fun String.hash(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(this.toByteArray())
        return hashBytes.joinToString("") { String.format("%02x", it) }
    }
}