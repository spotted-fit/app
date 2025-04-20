package fit.spotted.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform