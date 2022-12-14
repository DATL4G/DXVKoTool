package dev.datlag.dxvkotool.model.github

import dev.datlag.dxvkotool.dxvk.DxvkStateCache
import dev.datlag.dxvkotool.model.game.Game
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Base64

@Serializable
data class RepoStructure(
    @SerialName("sha") val sha: String,
    @SerialName("url") val url: String,
    @SerialName("tree") val tree: List<StructureItem>
) {
    fun findMatchingGameItem(
        game: Game,
        ignoreSpaces: Boolean = false,
        ignoreSpecialChars: Boolean = false
    ): Map<DxvkStateCache, StructureItem?> {
        val matchingItems = tree.mapNotNull { item ->
            val pathSplit = item.path.split('/')
            val anyMatching = pathSplit.any {
                var gameString = game.name
                var compareString = it
                if (ignoreSpecialChars) {
                    val regex = "([A-Za-z0-9]|\\s)+".toRegex()
                    gameString = regex.find(gameString)?.value ?: gameString
                    compareString = regex.find(compareString)?.value ?: compareString
                }
                if (ignoreSpaces) {
                    gameString = gameString.replace("\\s+".toRegex(), "")
                    compareString = compareString.replace("\\s+".toRegex(), "")
                }
                gameString.equals(compareString, true)
            }
            if (anyMatching) {
                item
            } else {
                null
            }
        }

        val associated = game.caches.value.associateWith { entry ->
            (matchingItems.firstOrNull { item ->
                item.path.endsWith(entry.file.name, true)
            } ?: matchingItems.firstOrNull { item ->
                item.path.endsWith("${entry.file.name}.md", true)
            } ?: matchingItems.firstOrNull { item ->
                item.path.endsWith("${entry.file.name}.txt", true)
            } ?: matchingItems.firstOrNull { item ->
                item.path.endsWith("${entry.file.name}.tar.xz.md", true)
            } ?: matchingItems.firstOrNull { item ->
                item.path.endsWith("${entry.file.name}.tar.xz.txt", true)
            })
        }

        return associated
    }

    fun findMatchingCacheItem(cache: DxvkStateCache): StructureItem? = tree.firstNotNullOfOrNull {
        if (it.path.equals(cache.associatedRepoItem.value, true)) {
            it
        } else {
            null
        }
    }

    fun toNodeStructure(): List<Node> {
        fun put(struct: MutableList<Node>, root: String, rest: String, item: StructureItem) {
            val tmp = rest.split("/".toRegex(), 2)

            val rootDir = struct.firstOrNull { it.path == root } ?: run {
                val node = Node(root, item)
                struct.add(node)
                node
            }
            if (tmp.size > 1) {
                put(rootDir.childs, tmp[0], tmp[1], item)
            } else {
                rootDir.childs.firstOrNull { it.path == tmp[0] } ?: run {
                    rootDir.childs.add(Node(tmp[0], item))
                }
            }
        }

        val list = tree.map { it }
        val structure: MutableList<Node> = mutableListOf()

        list.forEach {
            val tmp = it.path.split("/".toRegex(), 2)
            if (tmp.size > 1) {
                put(structure, tmp[0], tmp[1], it)
            }
        }

        return structure
    }
}

@Serializable
data class StructureItem(
    @SerialName("path") val path: String,
    @SerialName("mode") val mode: String,
    @SerialName("type") val type: String,
    @SerialName("sha") val sha: String,
    @SerialName("size") val size: Int = -1,
    @SerialName("url") val url: String
)

@Serializable
data class StructureItemContent(
    @SerialName("sha") val sha: String,
    @SerialName("content") private val content: String,
    @SerialName("encoding") val encoding: String
) {
    private fun getContent(): String? {
        return if (encoding.equals("base64", true)) {
            val decoded = runCatching {
                Base64.getDecoder().decode(content)
            }.getOrNull() ?: runCatching {
                Base64.getMimeDecoder().decode(content)
            }.getOrNull() ?: runCatching {
                Base64.getDecoder().decode(content.replace("\n", String()))
            }.getOrNull() ?: runCatching {
                Base64.getMimeDecoder().decode(content.replace("\n", String()))
            }.getOrNull()
            decoded?.let { String(it) }
        } else {
            null
        }
    }

    fun getUrlInContent(): String? {
        val decoded = getContent()
        val preferredRegex = Regex("(?<=(Download:\\s)|(File:\\s))(http(s)?://\\S+)", setOf(RegexOption.IGNORE_CASE))
        val secondaryRegex = Regex("(http(s)?://\\S+)", setOf(RegexOption.IGNORE_CASE))
        return if (decoded != null) {
            preferredRegex.findAll(decoded).map {
                it.value
            }.firstOrNull() ?: secondaryRegex.findAll(decoded).map {
                it.value
            }.firstOrNull()
        } else {
            null
        }
    }
}

fun Collection<RepoStructure>.findMatchingGameItem(game: Game): Map<DxvkStateCache, StructureItem?> {
    return game.caches.value.associateWith { cache ->
        this.firstNotNullOfOrNull { it.findMatchingCacheItem(cache) }
            ?: this.firstNotNullOfOrNull { it.findMatchingGameItem(game)[cache] }
            ?: this.firstNotNullOfOrNull {
                it.findMatchingGameItem(
                    game = game,
                    ignoreSpaces = true,
                    ignoreSpecialChars = false
                )[cache]
            }
            ?: this.firstNotNullOfOrNull {
                it.findMatchingGameItem(
                    game = game,
                    ignoreSpaces = false,
                    ignoreSpecialChars = true
                )[cache]
            }
            ?: this.firstNotNullOfOrNull {
                it.findMatchingGameItem(
                    game = game,
                    ignoreSpaces = true,
                    ignoreSpecialChars = true
                )[cache]
            }
    }
}
