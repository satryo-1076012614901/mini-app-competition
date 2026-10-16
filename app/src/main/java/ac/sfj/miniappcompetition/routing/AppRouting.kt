package ac.sfj.miniappcompetition.routing

import ac.sfj.miniappcompetition.ui.feature.competition.view.CompetitionDetailScreen
import ac.sfj.miniappcompetition.ui.feature.competition.view.CompetitionListScreen
import ac.sfj.miniappcompetition.ui.feature.competition.viewmodel.CompetitionDetailExit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

/**
 * Graf navigasi aplikasi: setiap route di [AppRoute] dipetakan ke layar di `ui/feature`.
 *
 * Layar tidak mengenal [NavHostController]. Layar menerima argumen route sebagai parameter biasa dan
 * meminta navigasi lewat callback, misalnya `onOpenCompetition` dan `onNavigateBack`.
 */
@Composable
fun AppRouting(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.CompetitionList,
        modifier = modifier,
    ) {
        composable<AppRoute.CompetitionList> { entry ->
            // Hasil dari layar detail yang ditutup otomatis, dikirim lewat SavedStateHandle entri daftar.
            val detailExit by entry.savedStateHandle
                .getStateFlow<CompetitionDetailExit?>(DETAIL_EXIT_KEY, null)
                .collectAsStateWithLifecycle()
            CompetitionListScreen(
                onOpenCompetition = { competitionId ->
                    if (entry.isResumed()) {
                        navController.navigate(AppRoute.CompetitionDetail(competitionId))
                    }
                },
                detailExit = detailExit,
                onDetailExitShown = { entry.savedStateHandle[DETAIL_EXIT_KEY] = null },
            )
        }
        composable<AppRoute.CompetitionDetail> { entry ->
            val route = entry.toRoute<AppRoute.CompetitionDetail>()
            CompetitionDetailScreen(
                competitionId = route.competitionId,
                onNavigateBack = { navController.navigateUp() },
                onExit = { exit ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(DETAIL_EXIT_KEY, exit)
                    navController.navigateUp()
                },
            )
        }
    }
}

/** Kunci SavedStateHandle untuk alasan layar detail ditutup otomatis. */
private const val DETAIL_EXIT_KEY = "competitionDetailExit"

/**
 * Navigasi maju hanya dijalankan dari layar yang sedang aktif, agar ketukan ganda selama animasi
 * perpindahan tidak membuka layar yang sama dua kali.
 */
private fun NavBackStackEntry.isResumed(): Boolean = lifecycle.currentState == Lifecycle.State.RESUMED
