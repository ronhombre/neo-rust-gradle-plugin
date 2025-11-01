package asia.hombre.neorust.option

/**
 * Build Profile used to build and run a binary.
 *
 * @since 0.4.0
 * @author Ron Lauren Hombre
 */
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