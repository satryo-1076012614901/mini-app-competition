package ac.sfj.miniappcompetition.data.container

import ac.sfj.miniappcompetition.data.network.ApiClient
import ac.sfj.miniappcompetition.data.network.create
import ac.sfj.miniappcompetition.data.repository.CompetitionRepository
import ac.sfj.miniappcompetition.data.repository.NetworkCompetitionRepository
import ac.sfj.miniappcompetition.data.service.CompetitionApiService

/** Container dependensi modul Competition (manual dependency injection). */
interface CompetitionContainer {
    val competitionRepository: CompetitionRepository
}

/** Container yang membuat repository berbasis API; satu instance per aplikasi. */
class DefaultCompetitionContainer(
    private val apiClient: ApiClient,
) : CompetitionContainer {
    override val competitionRepository: CompetitionRepository by lazy {
        NetworkCompetitionRepository(apiClient.create<CompetitionApiService>())
    }
}
