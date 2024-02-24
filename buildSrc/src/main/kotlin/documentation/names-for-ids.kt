package documentation

fun systemName(id: String): String =
    when (id) {
        "platform" -> "Platform"
        "other-project" -> "Other Project"
        else -> id
    }

fun contextName(id: String): String =
    when (id) {
        "application" -> "Application"
        else -> id
    }

fun componentName(id: String): String =
    when (id) {
        "frontend" -> "Frontend"
        "backend-service-1" -> "Backend Service #1"
        "backend-service-1-database" -> "database"
        "backend-service-2" -> "Backend Service #2"
        "external-service-1" -> "External Service #1"
        "external-service-2" -> "External Service #2"
        "external-service-3" -> "External Service #3"
        else -> id
    }
