package dev.datlag.dxvkotool.other

import de.jensklingenberg.ktorfit.create
import de.jensklingenberg.ktorfit.ktorfit
import dev.datlag.dxvkotool.common.systemProperty
import dev.datlag.dxvkotool.network.GitHub
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.apache.commons.io.FileUtils
import org.apache.tika.Tika
import java.time.format.DateTimeFormatter

object Constants {
    const val STEAM_DEFAULT_ROOT = ".local/share/Steam/"
    const val STEAM_SYMLINK_ROOT = ".steam/steam/"
    const val STEAM_FLATPAK_ROOT = ".var/app/com.valvesoftware.Steam/.local/share/Steam/"
    const val STEAM_FLATPAK_SYMLINK_ROOT = ".var/app/com.valvesoftware.Steam/.steam/steam/"

    const val STEAM_WINDOWS_DEFAULT_ROOT = "C:\\Program Files (x86)\\Steam\\"
    const val STEAM_WINDOWS_NEW_ROOT = "C:\\Program Files\\Steam\\"

    const val STEAM_MAC_DEFAULT_ROOT = "Library/Application Support/Steam/"

    const val ACF_ALL_ENDING_WITH_COMMA = "\"\\S+\"\\s+\"(\\S|[ ])*\"(?!(\\s+)?})"
    const val ACF_ALL_ENDING_WITH_COLON = "\"(\\S|[ ])*\"(?!([,]|\\s+(}|])))"
    const val ACF_ALL_ENDING_WITH_PARENTHESIS = "(}|])(?!([,]|\\s+(}|])))"

    const val SYSTEM_DEFAULT_LEGENDARY = ".config/legendary/"
    const val HEROIC_FLATPAK_LEGENDARY = ".var/app/com.heroicgameslauncher.hgl/config/legendary/"

    const val githubApiBaseUrl = "https://api.github.com/"

    const val githubProjectLink = "https://github.com/DATL4G/DXVKoTool"
    const val githubSponsorLink = "https://github.com/sponsors/DATL4G"

    const val dxvkRepoOwner = "begin-theadventure"
    const val dxvkRepo = "dxvk-caches"
    const val dxvkRepoBranch = "main"
    const val dxvkRepoProjectLink = "https://github.com/$dxvkRepoOwner/$dxvkRepo"

    const val GNOME = "GNOME"

    const val BYTE_POWERS_NUMBER = 10
    const val HALF_ALPHA_NUMBER = 0.5F
    const val HALF_PARENT_FRACTION_NUMBER = 0.5F

    const val LINUX_DARK_MODE_CMD = "gsettings get org.gnome.desktop.interface color-scheme"
    const val LINUX_DARK_MODE_LEGACY_CMD = "gsettings get org.gnome.desktop.interface gtk-theme"

    const val KIBIBYTE_SIZE = 1024
    const val BYTE_MULTIPLY_FACTOR = 1000

    const val U32_BYTE_BUFFER_CAPACITY = 4
    const val U24_BYTE_BUFFER_CAPACITY = 3
    const val U8_BYTE_BUFFER_CAPACITY = 1

    const val BYTE_POSITION_1 = 0
    const val BYTE_POSITION_2 = 1
    const val BYTE_POSITION_3 = 2
    const val BYTE_POSITION_4 = 3

    const val BYTE_POSITION_2_SHIFT = 8
    const val BYTE_POSITION_3_SHIFT = 16
    const val BYTE_POSITION_4_SHIFT = 24

    val tikaCore = Tika()

    val json = Json {
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    val githubKtorfit = ktorfit {
        baseUrl(githubApiBaseUrl)
        httpClient(httpClient)
    }

    val githubService = githubKtorfit.create<GitHub>()

    val userDir: String = systemProperty("user.home") ?: FileUtils.getUserDirectoryPath()
    val defaultDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
}
