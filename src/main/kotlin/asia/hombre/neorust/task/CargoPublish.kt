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

package asia.hombre.neorust.task

import asia.hombre.neorust.internal.CargoDefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

/**
 * Publish a Cargo crate to a registry
 *
 * This runs `cargo publish`
 *
 * @since 0.1.0
 * @author Ron Lauren Hombre
 */
abstract class CargoPublish @Inject constructor(): CargoDefaultTask() {
    @get:Input
    @get:Optional
    abstract val dryRun: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val noVerify: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val allowDirty: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val token: Property<String>
    @get:Input
    @get:Optional
    abstract val index: Property<String>
    @get:Input
    @get:Optional
    abstract val registry: Property<String>

    init {
        dryRun.convention(false)
        noVerify.convention(false)
        allowDirty.convention(false)
        token.convention("")
        index.convention("")
        registry.convention("crates.io") //TODO: Verify this works
    }

    override fun getInitialArgs(): List<String> {
        return (super.getInitialArgs() as MutableList<String>).apply {
            add("publish")
        }
    }

    override fun compileArgs(): List<String> {
        val args: MutableList<String> = super.compileArgs() as MutableList<String>

        if(dryRun.getOrElse(false))
            args.add("--dry-run")

        if(noVerify.getOrElse(false))
            args.add("--no-verify")

        if(allowDirty.getOrElse(false))
            args.add("--allow-dirty")

        if(token.get().isBlank())
            throw IllegalArgumentException("Token is unspecified explicitly or through Environment Variables!")
        else
            args.addAll(listOf("--token", token.get()))

        if(index.get().isNotBlank())
            args.addAll(listOf("--index", index.get()))

        if(registry.get().isNotBlank())
            args.addAll(listOf("--registry", registry.get()))

        return args
    }
}