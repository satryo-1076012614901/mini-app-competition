package ac.sfj.miniappcompetition.ui.shared

import ac.sfj.miniappcompetition.R
import ac.sfj.miniappcompetition.data.model.ApiErrorCode
import ac.sfj.miniappcompetition.data.model.AppError
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pemetaan error ke pesan sesuai Referensi UI hal. 15, ditambah keputusan untuk error di luar tabel. */
class ErrorMessageTest {

    @Test
    fun network_showsConnectionMessageWithRetry() {
        assertEquals(ErrorMessage(R.string.error_network, retryable = true), AppError.Network.toErrorMessage())
    }

    @Test
    fun serverFailures_showServerMessageWithRetry() {
        val expected = ErrorMessage(R.string.error_server, retryable = true)
        assertEquals(expected, AppError.Api(ApiErrorCode.INTERNAL_SERVER_ERROR, httpStatus = 500).toErrorMessage())
        assertEquals(expected, AppError.Http(httpStatus = 500).toErrorMessage())
        assertEquals(expected, AppError.Http(httpStatus = 502).toErrorMessage())
        assertEquals(expected, AppError.InvalidResponse.toErrorMessage())
    }

    @Test
    fun otherHttpFailuresAndUnknownCodes_showRequestFailedWithoutRetry() {
        val expected = ErrorMessage(R.string.error_request_failed)
        assertEquals(expected, AppError.Http(httpStatus = 404).toErrorMessage())
        assertEquals(expected, AppError.Http(httpStatus = 499).toErrorMessage())
        assertEquals(expected, AppError.Api(ApiErrorCode.HTTP_ERROR, httpStatus = 405).toErrorMessage())
        assertEquals(expected, AppError.Api(ApiErrorCode.UNKNOWN, httpStatus = 409).toErrorMessage())
    }

    @Test
    fun competitionCodes_followUiReferenceTable() {
        assertEquals(ErrorMessage(R.string.error_validation), api(ApiErrorCode.VALIDATION_ERROR, 400))
        assertEquals(ErrorMessage(R.string.error_not_found), api(ApiErrorCode.COMPETITION_NOT_FOUND, 404))
        assertEquals(ErrorMessage(R.string.error_competition_not_open), api(ApiErrorCode.COMPETITION_NOT_OPEN, 409))
        assertEquals(ErrorMessage(R.string.participant_type_locked), api(ApiErrorCode.PARTICIPANT_TYPE_LOCKED, 409))
        assertEquals(ErrorMessage(R.string.error_data_conflict), api(ApiErrorCode.DATA_INTEGRITY_VIOLATION, 409))
    }

    private fun api(code: ApiErrorCode, httpStatus: Int): ErrorMessage =
        AppError.Api(code, httpStatus).toErrorMessage()
}
