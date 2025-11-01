package asia.hombre.neorust.exception

/**
 * Thrown when a binary target with the same name and build profile has been registered more than once.
 *
 * @since 0.4.0
 * @author Ron Lauren Hombre
 */
class DuplicateBinaryTargetException(msg: String): RuntimeException(msg)