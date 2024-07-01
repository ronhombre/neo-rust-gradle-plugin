package asia.hombre.neorust.internal

import asia.hombre.neorust.options.RustTargetOptions
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

open class CargoTargettedTask: CargoDefaultTask() {
    @get:Input
    var lib: Boolean? = null
        get() = field?: getTargetOptions().lib

    @get:Input
    var bin: MutableList<String> = mutableListOf()
        get() = field.ifEmpty { getTargetOptions().bin }

    @get:Input
    var bins: Boolean? = null
        get() = field?: getTargetOptions().bins

    @get:Input
    var example: MutableList<String> = mutableListOf()
        get() = field.ifEmpty { getTargetOptions().example }

    @get:Input
    var examples: Boolean? = null
        get() = field?: getTargetOptions().examples

    @get:Input
    var test: MutableList<String> = mutableListOf()
        get() = field.ifEmpty { getTargetOptions().test }

    @get:Input
    var tests: Boolean? = null
        get() = field?: getTargetOptions().tests

    @get:Input
    var bench: MutableList<String> = mutableListOf()
        get() = field.ifEmpty { getTargetOptions().bench }

    @get:Input
    var benches: Boolean? = null
        get() = field?: getTargetOptions().benches

    @get:Input
    var allTargets: Boolean? = null
        get() = field?: getTargetOptions().allTargets

    @Internal
    internal open fun getTargetOptions(): RustTargetOptions {
        return RustTargetOptions()
    }

    override fun compileArgs(): List<String> {
        val args = mutableListOf<String>()

        args.addAll(super.compileArgs())

        if(lib!!)
            args.add("--lib")

        bin.forEach { bin ->
            args.addAll(listOf("--bin", bin))
        }

        if(bins!!)
            args.add("--bins")

        example.forEach { example ->
            args.addAll(listOf("--example", example))
        }

        if(examples!!)
            args.add("--examples")

        test.forEach { test ->
            args.addAll(listOf("--test", test))
        }

        if(tests!!)
            args.add("--tests")

        bench.forEach { bench ->
            args.addAll(listOf("--bench", bench))
        }

        if(benches!!)
            args.add("--benches")

        if(allTargets!!)
            args.add("--all-targets")

        return args
    }
}