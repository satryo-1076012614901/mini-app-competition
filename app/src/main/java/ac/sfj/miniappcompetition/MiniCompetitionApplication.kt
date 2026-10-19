package ac.sfj.miniappcompetition

import ac.sfj.miniappcompetition.data.container.CompetitionContainer
import ac.sfj.miniappcompetition.data.container.DefaultCompetitionContainer
import ac.sfj.miniappcompetition.data.container.DefaultMatchContainer
import ac.sfj.miniappcompetition.data.container.DefaultParticipantContainer
import ac.sfj.miniappcompetition.data.container.MatchContainer
import ac.sfj.miniappcompetition.data.container.ParticipantContainer
import ac.sfj.miniappcompetition.data.network.ApiClient
import android.app.Application

/**
 * Titik awal aplikasi yang menyimpan ketiga container. ViewModel factory mengambil repository dari
 * sini, sehingga seluruh layar memakai cache repository yang sama. Ketiga container berbagi satu
 * [ApiClient].
 */
class MiniCompetitionApplication : Application() {
    lateinit var competitionContainer: CompetitionContainer
        private set

    lateinit var participantContainer: ParticipantContainer
        private set

    lateinit var matchContainer: MatchContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val apiClient = ApiClient(
            baseUrl = BuildConfig.API_BASE_URL,
            enableLogging = BuildConfig.DEBUG,
        )
        competitionContainer = DefaultCompetitionContainer(apiClient)
        participantContainer = DefaultParticipantContainer(apiClient)
        matchContainer = DefaultMatchContainer(apiClient)
    }
}
