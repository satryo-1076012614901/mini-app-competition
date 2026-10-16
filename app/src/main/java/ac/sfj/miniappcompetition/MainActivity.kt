package ac.sfj.miniappcompetition

import ac.sfj.miniappcompetition.routing.AppRouting
import ac.sfj.miniappcompetition.ui.theme.MiniAppCompetitionTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

/**
 * Satu-satunya activity aplikasi: memasang theme, lalu menyerahkan tampilan ke routing
 * (MainActivity → routing → View).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiniAppCompetitionTheme {
                // Latar di belakang NavHost, agar animasi perpindahan layar tidak memperlihatkan latar window.
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRouting()
                }
            }
        }
    }
}
