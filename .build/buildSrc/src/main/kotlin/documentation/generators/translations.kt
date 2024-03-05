package documentation.generators

import java.util.MissingResourceException
import java.util.ResourceBundle

private val components = ResourceBundle.getBundle("i18n/components")
private val groups = ResourceBundle.getBundle("i18n/groups")
private val systems = ResourceBundle.getBundle("i18n/systems")

fun componentName(id: String): String = components.getStringOrDefault(id)
fun groupName(id: String): String = groups.getStringOrDefault(id)
fun systemName(id: String): String = systems.getStringOrDefault(id)

private fun ResourceBundle.getStringOrDefault(key: String, default: String = key): String =
    try {
        getString(key)
    } catch (e: MissingResourceException) {
        default
    }
