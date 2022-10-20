package dev.datlag.dxvkotool.model.game

import dev.datlag.dxvkotool.common.createBackup
import dev.datlag.dxvkotool.common.runSuspendCatching
import dev.datlag.dxvkotool.db.DB
import dev.datlag.dxvkotool.dxvk.DxvkStateCache
import dev.datlag.dxvkotool.model.game.cache.CacheInfo
import dev.datlag.dxvkotool.model.game.steam.AppManifest
import dev.datlag.dxvkotool.model.github.findMatchingGameItem
import dev.datlag.dxvkotool.network.OnlineDXVK
import dev.datlag.dxvkotool.other.Constants
import dev.datlag.dxvkotool.other.MergeException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import java.io.File

sealed class Game(
    open val name: String,
    open val path: File,
    open val caches: MutableStateFlow<List<DxvkStateCache>>
) {

    val cacheInfoCollector by lazy {
        combine(
            OnlineDXVK.dxvkRepoStructureFlow,
            caches.flatMapLatest {
                combine(it.map { cache -> cache.associatedRepoItem }) { list ->
                    list
                }
            }
        ) { t1, t2, ->
            t1 to t2
        }.transformLatest { (repoStructures, _) ->
            val matchingCacheWithItem = repoStructures.findMatchingGameItem(this@Game)
            matchingCacheWithItem.forEach { (t, u) ->
                val cacheInfo = if (u == null) {
                    CacheInfo.None
                } else {
                    val downloadUrl = runCatching {
                        Constants.githubService.getStructureItemContent(u.url.replace(Constants.githubApiBaseUrl, String()))
                    }
                    val contentUrl = downloadUrl.getOrNull()?.getUrlInContent()
                    CacheInfo.Url(contentUrl)
                }
                if (t.info.value is CacheInfo.Error.InvalidEntries) {
                    (t.info.value as? CacheInfo.Error.InvalidEntries)?.afterFixInfo = cacheInfo
                } else {
                    t.info.emit(cacheInfo)
                }
            }
            return@transformLatest emit(matchingCacheWithItem)
        }
    }

    suspend fun mergeCache(cache: DxvkStateCache) = runSuspendCatching {
        val downloadCache = (cache.info.value as? CacheInfo.Download.Cache?) ?: throw MergeException.NoFileFound

        val combinedCache = downloadCache.combinedCache
        val combineResult = combinedCache.writeTo(combinedCache.file, true).isSuccess
        val currentCaches = caches.value.toMutableList()
        val cacheIndex = currentCaches.indexOf(cache)
        combinedCache.info.emit(CacheInfo.Merged(combineResult))

        if (cacheIndex >= 0) {
            currentCaches[cacheIndex] = combinedCache
        }

        caches.emit(currentCaches)
    }

    suspend fun repairCache(cache: DxvkStateCache) = runSuspendCatching {
        cache.writeTo(cache.file, false).isSuccess
        cache.info.emit((cache.info.value as? CacheInfo.Error.InvalidEntries)?.afterFixInfo ?: CacheInfo.Loading.Url)
    }

    private suspend fun restoreFile(cache: DxvkStateCache, restoreFile: File) = runSuspendCatching {
        val backupFile = cache.file.createBackup()
        val backupSuccess = cache.file.renameTo(backupFile)
        val renamed = restoreFile.renameTo(cache.file)
        if (!renamed) {
            cache.file.delete()
            return@runSuspendCatching restoreFile.renameTo(cache.file) && backupSuccess
        }
        return@runSuspendCatching backupSuccess
    }

    suspend fun restoreBackup(cache: DxvkStateCache, restoreFile: File) = runSuspendCatching {
        val success = restoreFile(cache, restoreFile)
        val restoreCache = DxvkStateCache.fromFile(cache.file).getOrThrow()

        val currentCaches = caches.value.toMutableList()
        val cacheIndex = currentCaches.indexOf(cache)
        if (cacheIndex >= 0) {
            currentCaches[cacheIndex] = restoreCache
        }
        caches.emit(currentCaches)

        success
    }

    data class Steam(
        val manifest: AppManifest,
        override val path: File,
        override val caches: MutableStateFlow<List<DxvkStateCache>>
    ) : Game(manifest.name, path, caches)

    data class Other(
        override val name: String,
        override val path: File,
        val isEpicGame: Boolean,
        override val caches: MutableStateFlow<List<DxvkStateCache>>
    ) : Game(name, path, caches)
}
