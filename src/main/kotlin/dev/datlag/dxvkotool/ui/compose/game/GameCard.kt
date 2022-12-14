package dev.datlag.dxvkotool.ui.compose.game

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.datlag.dxvkotool.model.game.Game
import dev.datlag.dxvkotool.ui.compose.AsyncImage
import dev.datlag.dxvkotool.ui.compose.game.cache.GameCache

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun GameCard(game: Game) {
    val caches by game.cacheInfoCollector.collectAsState(emptySet())

    ElevatedCard(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            game,
            modifier = Modifier.fillMaxWidth()
        )
        Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(0.dp, 0.dp, 0.dp, 16.dp)) {
            Text(
                text = game.name,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp, 8.dp),
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = game.path.absolutePath,
                maxLines = 1,
                modifier = Modifier.padding(16.dp, 8.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        caches.forEach {
            GameCache(game, it)
        }
    }
}
