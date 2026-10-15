package asia.hombre.neorust.internal

fun validatePackageName(name: String): Boolean {
    return name.matches(Regex("^[a-zA-Z][a-zA-Z0-9_-]{0,63}$"))
}

fun String.escapedString(): String {
    return this.replace("\"", "\\\"")
}