package ac.sfj.miniappcompetition.data.container

import ac.sfj.miniappcompetition.data.network.ApiClient
import ac.sfj.miniappcompetition.data.network.create
import ac.sfj.miniappcompetition.data.repository.MatchRepository
import ac.sfj.miniappcompetition.data.repository.NetworkMatchRepository
import ac.sfj.miniappcompetition.data.service.MatchApiService

/** Container dependensi modul Match (manual dependency injection). */
interface MatchContainer {
    val matchRepository: MatchRepository
}

/** Container yang membuat repository berbasis API; satu instance per aplikasi. */
class DefaultMatchContainer(
    private val apiClient: ApiClient,
) : MatchContainer {
    override val matchRepository: MatchRepository by lazy {
        NetworkMatchRepository(apiClient.create<MatchApiService>())
    }
}
