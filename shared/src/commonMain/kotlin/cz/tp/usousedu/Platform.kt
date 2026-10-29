package cz.tp.usousedu

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform