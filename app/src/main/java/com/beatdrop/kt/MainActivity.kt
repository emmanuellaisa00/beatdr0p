package com.beatdrop.kt

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beatdrop.kt.ui.components.GlassTabBar
import com.beatdrop.kt.ui.components.MiniPlayer
import com.beatdrop.kt.ui.components.TabSpec
import com.beatdrop.kt.ui.screens.*
import com.beatdrop.kt.ui.theme.BeatDropTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeatDropTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Root()
                }
            }
        }
    }
}

private val audioPermission: String
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        android.Manifest.permission.READ_MEDIA_AUDIO
    else android.Manifest.permission.READ_EXTERNAL_STORAGE

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Root(vm: PlayerViewModel = viewModel()) {
    val perm = rememberPermissionState(audioPermission)

    LaunchedEffect(Unit) { vm.connect() }
    LaunchedEffect(perm.status.isGranted) {
        if (perm.status.isGranted) vm.loadLibrary()
    }

    var onboarded by rememberSaveable { mutableStateOf(false) }
    if (!onboarded && !perm.status.isGranted) {
        OnboardingScreen(onGetStarted = { onboarded = true; perm.launchPermissionRequest() })
        return
    }
    if (!perm.status.isGranted) {
        PermissionPrompt(onRequest = { perm.launchPermissionRequest() })
        return
    }
    MainScaffold(vm)
}

private val TABS = listOf(
    TabSpec("library", "Library", Icons.Filled.LibraryMusic),
    TabSpec("discover", "Discover", Icons.Outlined.Explore),
    TabSpec("radio", "Radio", Icons.Filled.Radio),
    TabSpec("dj", "DJ Mode", Icons.Filled.GraphicEq),
)

/** Lightweight overlay stack pushed over the tabs. */
private sealed interface Overlay {
    data class Album(val name: String, val artist: String) : Overlay
    data class Artist(val name: String) : Overlay
    data class Playlist(val name: String) : Overlay
    data object Playlists : Overlay
    data object Stats : Overlay
    data object Settings : Overlay
    data object Eq : Overlay
    data object Search : Overlay
}

@Composable
fun MainScaffold(vm: PlayerViewModel) {
    var tab by remember { mutableStateOf("library") }
    var showNowPlaying by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var overlay by remember { mutableStateOf<Overlay?>(null) }

    val current by vm.current.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    val pos by vm.position.collectAsState()
    val dur by vm.duration.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Box(Modifier.weight(1f)) {
                when (tab) {
                    "library" -> LibraryScreen(
                        vm,
                        onOpenAlbum = { a, ar -> overlay = Overlay.Album(a, ar) },
                        onOpenArtist = { ar -> overlay = Overlay.Artist(ar) },
                        onOpenSettings = { overlay = Overlay.Settings },
                        onOpenPlaylists = { overlay = Overlay.Playlists },
                        onOpenStats = { overlay = Overlay.Stats },
                    )
                    "discover" -> DiscoverScreen(vm, onOpenSearch = { overlay = Overlay.Search })
                    "radio" -> RadioScreen(vm)
                    "dj" -> DJScreen(vm)
                }
            }
        }

        // Mini-player + glass tab bar pinned at bottom
        Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding()) {
            current?.let { t ->
                MiniPlayer(
                    track = t, isPlaying = isPlaying,
                    progress = if (dur > 0) pos.toFloat() / dur else 0f,
                    onToggle = { vm.togglePlay() }, onNext = { vm.next() },
                    onExpand = { showNowPlaying = true },
                )
            }
            GlassTabBar(TABS, tab) { tab = it }
        }

        // ── Overlays ───────────────────────────────────────────────────────────
        when (val o = overlay) {
            is Overlay.Album -> AlbumScreen(vm, o.name, o.artist, onBack = { overlay = null })
            is Overlay.Artist -> ArtistScreen(vm, o.name, onBack = { overlay = null })
            is Overlay.Playlist -> PlaylistDetailScreen(vm, o.name, onBack = { overlay = null })
            Overlay.Playlists -> PlaylistsHost(vm, onBack = { overlay = null }, onOpen = { overlay = Overlay.Playlist(it) })
            Overlay.Stats -> StatsHost(vm, onBack = { overlay = null })
            Overlay.Settings -> SettingsScreen(vm, onBack = { overlay = null }, onOpenEq = { overlay = Overlay.Eq })
            Overlay.Eq -> EqScreen(onBack = { overlay = Overlay.Settings })
            Overlay.Search -> SearchHost(vm, onBack = { overlay = null })
            null -> {}
        }

        if (showNowPlaying && current != null) {
            NowPlayingScreen(vm, onCollapse = { showNowPlaying = false }, onOpenQueue = { showQueue = true })
        }

        if (showQueue) {
            QueueScreen(vm, onClose = { showQueue = false })
        }
    }
}


@Composable
private fun PlaylistsHost(vm: PlayerViewModel, onBack: () -> Unit, onOpen: (String) -> Unit) {
    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
        com.beatdrop.kt.ui.screens.PlaylistsScreen(vm, onOpen = onOpen)
        IconButton(onClick = onBack, modifier = Modifier.statusBarsPadding().padding(4.dp)) {
            Icon(Icons.Filled.ArrowBack, "Back")
        }
    }
}

@Composable
private fun StatsHost(vm: PlayerViewModel, onBack: () -> Unit) {
    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
        com.beatdrop.kt.ui.screens.StatsScreen(vm)
        IconButton(onClick = onBack, modifier = Modifier.statusBarsPadding().padding(4.dp)) {
            Icon(Icons.Filled.ArrowBack, "Back")
        }
    }
}

@Composable
private fun SearchHost(vm: PlayerViewModel, onBack: () -> Unit) {
    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
        com.beatdrop.kt.ui.screens.SearchScreen(vm)
        IconButton(onClick = onBack, modifier = Modifier.statusBarsPadding().padding(4.dp)) {
            Icon(Icons.Filled.ArrowBack, "Back")
        }
    }
}
