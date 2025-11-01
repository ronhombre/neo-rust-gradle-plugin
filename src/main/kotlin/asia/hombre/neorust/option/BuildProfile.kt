package asia.hombre.neorust.option

enum class BuildProfile {
    /**
     * Inherits the build profile from the project when run.
     */
    DEFAULT,

    /**
     * Builds or executes the binary without the '--release' argument.
     *
     * This is faster and is intended for prototyping.
     */
    DEV,

    /**
     * Builds or executes the binary with the '--release' argument.
     *
     * This is slower but makes a compressed and highly optimized version.
     */
    RELEASE
}