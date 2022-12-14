package dev.datlag.dxvkotool.io

import dev.datlag.dxvkotool.common.getParentSafely
import dev.datlag.dxvkotool.model.game.steam.AppManifest
import dev.datlag.dxvkotool.model.game.steam.LibraryConfig
import dev.datlag.dxvkotool.other.Constants
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import java.io.File

object SteamConfig {
    private fun toJson(value: String): String {
        return ("{${value.substringAfter('{')}").replace(Constants.ACF_ALL_ENDING_WITH_COMMA.toRegex()) {
            "${it.value},"
        }.replace(Constants.ACF_ALL_ENDING_WITH_COLON.toRegex()) {
            "${it.value}:"
        }.replace(Constants.ACF_ALL_ENDING_WITH_PARENTHESIS.toRegex()) {
            if (it.next() == null) {
                it.value
            } else {
                "${it.value},"
            }
        }
    }

    fun toAppManifest(file: File) = toAppManifest(file.readText(), (file.getParentSafely() ?: file).absolutePath)

    private fun toAppManifest(value: String, location: String): Result<AppManifest> = runCatching {
        Constants.json.decodeFromString<AppManifest>(toJson(value)).apply {
            this.location = location
        }
    }

    fun toLibraryConfigs(value: String): List<LibraryConfig> {
        val jsonValue = toJson(value)

        val jsonObject: JsonElement? = runCatching {
            Constants.json.decodeFromString<JsonElement>(jsonValue)
        }.getOrNull()

        return jsonObject?.jsonObject?.values?.mapNotNull {
            val jsonElement = runCatching {
                it.jsonObject
            }.getOrNull() ?: it

            runCatching {
                Constants.json.decodeFromJsonElement<LibraryConfig>(jsonElement)
            }.getOrNull()
        } ?: emptyList()
    }
}
