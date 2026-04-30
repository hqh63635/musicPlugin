package com.musicfree.android.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.musicfree.android.core.musicfreeContainer
import com.musicfree.android.data.model.PlaylistSheet
import com.musicfree.android.ui.components.MiniPlayerBar
import com.musicfree.android.ui.navigation.AppDestinations
import com.musicfree.android.ui.navigation.BottomDestination
import com.musicfree.android.ui.navigation.bottomDestinations
import com.musicfree.android.ui.screens.FavoritesScreen
import com.musicfree.android.ui.screens.HomeScreen
import com.musicfree.android.ui.screens.PlayerScreen
import com.musicfree.android.ui.screens.PlaylistDetailScreen
import com.musicfree.android.ui.screens.PlaylistsScreen
import com.musicfree.android.ui.screens.ProfileScreen
import com.musicfree.android.ui.screens.SearchScreen
import com.musicfree.android.ui.theme.MusicfreeTheme
import com.musicfree.android.ui.viewmodel.DetailKind
import com.musicfree.android.ui.viewmodel.HomeViewModel
import com.musicfree.android.ui.viewmodel.LibraryViewModel
import com.musicfree.android.ui.viewmodel.PlaylistDetailViewModel
import com.musicfree.android.ui.viewmodel.SearchViewModel
import com.musicfree.android.ui.viewmodel.simpleViewModelFactory

@Composable
fun MusicfreeApp() {
    MusicfreeTheme {
        val context = LocalContext.current
        val container = context.musicfreeContainer
        val navController = rememberNavController()
        val currentEntry by navController.currentBackStackEntryAsState()
        val currentRoute = currentEntry?.destination?.route.orEmpty()
        val playerController = container.playerController
        val playerState by playerController.uiState.collectAsStateWithLifecycle()
        val libraryViewModel: LibraryViewModel = viewModel(
            factory = simpleViewModelFactory {
                LibraryViewModel(container.repository, playerController)
            },
        )
        val libraryState by libraryViewModel.uiState.collectAsStateWithLifecycle()

        Scaffold(
            bottomBar = {
                if (currentRoute != AppDestinations.player) {
                    Column {
                        MiniPlayerBar(
                            state = playerState,
                            onOpen = {
                                navController.navigate(AppDestinations.player)
                            },
                            onTogglePlay = playerController::togglePlay,
                            onNext = playerController::playNext,
                        )
                        NavigationBar {
                            bottomDestinations.forEach { destination ->
                                val selected = currentRoute.startsWith(destination.route)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(destination.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = destination.label,
                                        )
                                    },
                                    label = { Text(destination.label) },
                                )
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = BottomDestination.Home.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(BottomDestination.Home.route) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = simpleViewModelFactory {
                            HomeViewModel(container.repository)
                        },
                    )
                    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
                    HomeScreen(
                        state = homeState,
                        onSearchClick = { navController.navigate(AppDestinations.search) },
                        onPlaySong = { song, queue ->
                            playerController.playSong(song, queue)
                        },
                        onPlayFeatured = { songs ->
                            playerController.setQueue(songs)
                        },
                        onToggleFavorite = homeViewModel::toggleFavorite,
                        onOpenPlaylist = { sheet, kind ->
                            navController.navigate(AppDestinations.buildDetailRoute(kind, sheet))
                        },
                        onTagSelected = homeViewModel::selectTag,
                    )
                }
                composable(BottomDestination.Queue.route) {
                    PlaylistsScreen(
                        state = libraryState,
                        onPlaySong = { index ->
                            playerController.setQueue(libraryState.queue, index)
                        },
                        onRemoveSong = libraryViewModel::removeFromQueue,
                        onClearQueue = libraryViewModel::clearQueue,
                    )
                }
                composable(BottomDestination.Favorites.route) {
                    FavoritesScreen(
                        state = libraryState,
                        onPlaySong = { song ->
                            playerController.playSong(song, libraryState.favorites)
                        },
                        onToggleFavorite = libraryViewModel::toggleFavorite,
                    )
                }
                composable(BottomDestination.Profile.route) {
                    ProfileScreen(
                        state = libraryState,
                        onQualityChanged = libraryViewModel::setQuality,
                        onSourceChanged = libraryViewModel::setSource,
                    )
                }
                composable(AppDestinations.search) {
                    val searchViewModel: SearchViewModel = viewModel(
                        factory = simpleViewModelFactory {
                            SearchViewModel(container.repository)
                        },
                    )
                    val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()
                    SearchScreen(
                        state = searchState,
                        onBack = { navController.popBackStack() },
                        onQueryChanged = searchViewModel::updateQuery,
                        onSubmit = searchViewModel::submitQuery,
                        onTypeSelected = searchViewModel::switchType,
                        onPlaySong = { song, queue ->
                            playerController.playSong(song, queue)
                        },
                        onOpenPlaylist = { sheet ->
                            navController.navigate(AppDestinations.buildDetailRoute("sheet", sheet))
                        },
                        onUseHistory = { keyword ->
                            searchViewModel.updateQuery(keyword)
                            searchViewModel.submitQuery(keyword)
                        },
                        onClearHistory = searchViewModel::clearHistory,
                        onLoadMore = searchViewModel::loadMore,
                        onToggleFavorite = searchViewModel::toggleFavorite,
                    )
                }
                composable(AppDestinations.detail) { backStackEntry ->
                    val kindArg = backStackEntry.arguments?.getString("kind").orEmpty()
                    val sheet = PlaylistSheet(
                        id = Uri.decode(backStackEntry.arguments?.getString("id").orEmpty()),
                        title = Uri.decode(backStackEntry.arguments?.getString("title").orEmpty()),
                        artwork = Uri.decode(backStackEntry.arguments?.getString("artwork").orEmpty()).ifBlank { null },
                        description = Uri.decode(backStackEntry.arguments?.getString("description").orEmpty()).ifBlank { null },
                        artist = Uri.decode(backStackEntry.arguments?.getString("artist").orEmpty()).ifBlank { null },
                        period = Uri.decode(backStackEntry.arguments?.getString("period").orEmpty()).ifBlank { null },
                    )
                    val detailViewModel: PlaylistDetailViewModel = viewModel(
                        key = "detail-${kindArg}-${sheet.id}",
                        factory = simpleViewModelFactory {
                            PlaylistDetailViewModel(
                                repository = container.repository,
                                kind = if (kindArg == "chart") DetailKind.CHART else DetailKind.SHEET,
                                sheet = sheet,
                            )
                        },
                    )
                    val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
                    PlaylistDetailScreen(
                        state = detailState,
                        onBack = { navController.popBackStack() },
                        onPlaySong = { song, queue ->
                            playerController.playSong(song, queue)
                        },
                        onPlayAll = { songs ->
                            playerController.setQueue(songs)
                        },
                        onToggleFavorite = detailViewModel::toggleFavorite,
                    )
                }
                composable(AppDestinations.player) {
                    PlayerScreen(
                        state = playerState,
                        onBack = { navController.popBackStack() },
                        onSeek = playerController::seekTo,
                        onTogglePlay = playerController::togglePlay,
                        onNext = playerController::playNext,
                        onPrevious = playerController::playPrevious,
                    )
                }
            }
        }
    }
}
