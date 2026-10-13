//package ac.sfj.miniappcompetition.data.container

//import ac.sfj.miniappcompetition.data.network.ApiClient
//import ac.sfj.miniappcompetition.data.network.create
//import ac.sfj.miniappcompetition.data.repository.NetworkParticipantRepository
//import ac.sfj.miniappcompetition.data.repository.ParticipantRepository
//import ac.sfj.miniappcompetition.data.service.ParticipantApiService

///** Container dependensi modul Participant (manual dependency injection). */
//interface ParticipantContainer {
//    val participantRepository: ParticipantRepository
//}

///** Container yang membuat repository berbasis API; satu instance per aplikasi. */
//class DefaultParticipantContainer(
//    private val apiClient: ApiClient,
//) : ParticipantContainer {
//    override val participantRepository: ParticipantRepository by lazy {
//        NetworkParticipantRepository(apiClient.create<ParticipantApiService>())
//    }
//}
