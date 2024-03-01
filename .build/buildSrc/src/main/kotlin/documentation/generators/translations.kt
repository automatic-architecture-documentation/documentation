package documentation.generators

import java.util.MissingResourceException
import java.util.ResourceBundle

private val components = ResourceBundle.getBundle("i18n/components")
private val contexts = ResourceBundle.getBundle("i18n/contexts")
private val systems = ResourceBundle.getBundle("i18n/systems")

fun componentName(id: String): String = components.getStringOrDefault(id)
fun contextName(id: String): String = contexts.getStringOrDefault(id)
fun systemName(id: String): String = systems.getStringOrDefault(id)

private fun ResourceBundle.getStringOrDefault(key: String, default: String = key): String =
    try {
        getString(key)
    } catch (e: MissingResourceException) {
        default
    }
