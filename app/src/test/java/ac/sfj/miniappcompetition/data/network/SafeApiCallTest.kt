package ac.sfj.miniappcompetition.data.network

import ac.sfj.miniappcompetition.data.dto.CompetitionResponseDto
import ac.sfj.miniappcompetition.data.dto.toModel
import ac.sfj.miniappcompetition.data.model.ApiErrorCode
import ac.sfj.miniappcompetition.data.model.AppError
import ac.sfj.miniappcompetition.data.model.AppResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class SafeApiCallTest {

    private fun httpError(status: Int, body: String, contentType: String = "application/json"): HttpException =
        HttpException(Response.error<Any>(status, body.toResponseBody(contentType.toMediaType())))

    @Test
    fun success_returnsData() = runTest {
        assertEquals(AppResult.Success(42), safeApiCall { 42 })
    }

    @Test
    fun errorBodyWithCode_becomesApiError() = runTest {
        val result = safeApiCall<Unit> {
            throw httpError(409, """{"code":"COMPETITION_NOT_OPEN","message":"Kompetisi tidak berstatus OPEN"}""")
        }

        assertEquals(AppResult.Failure(AppError.Api(ApiErrorCode.COMPETITION_NOT_OPEN, 409)), result)
    }

    @Test
    fun unknownCode_becomesUnknown() = runTest {
        val result = safeApiCall<Unit> { throw httpError(409, """{"code":"NEW_RULE","message":"x"}""") }

        assertEquals(AppResult.Failure(AppError.Api(ApiErrorCode.UNKNOWN, 409)), result)
    }

    @Test
    fun bodyWithoutCode_becomesHttpError() = runTest {
        val html = safeApiCall<Unit> { throw httpError(502, "<html>Bad Gateway</html>", "text/html") }
        val emptyJson = safeApiCall<Unit> { throw httpError(500, "{}") }

        assertEquals(AppResult.Failure(AppError.Http(502)), html)
        assertEquals(AppResult.Failure(AppError.Http(500)), emptyJson)
    }

    @Test
    fun ioException_becomesNetworkError() = runTest {
        assertEquals(AppResult.Failure(AppError.Network), safeApiCall<Unit> { throw IOException("offline") })
        assertEquals(AppResult.Failure(AppError.Network), safeApiCall<Unit> { throw SocketTimeoutException() })
    }

    @Test
    fun invalidContract_becomesInvalidResponse() = runTest {
        val unknownStatus = CompetitionResponseDto(
            id = 1,
            name = "Futsal Cup",
            participantType = "TEAM",
            status = "ARCHIVED",
            createdAt = "2026-10-06T08:00:00Z",
            updatedAt = "2026-10-06T08:00:00Z",
        )
        val invalidTimestamp = unknownStatus.copy(status = "OPEN", createdAt = "06/10/2026")

        assertEquals(AppResult.Failure(AppError.InvalidResponse), safeApiCall { unknownStatus.toModel() })
        assertEquals(AppResult.Failure(AppError.InvalidResponse), safeApiCall { invalidTimestamp.toModel() })
        assertEquals(
            AppResult.Failure(AppError.InvalidResponse),
            safeApiCall<Unit> { throw SerializationException("Unexpected JSON token") },
        )
    }

    @Test
    fun cancellation_isRethrown() = runTest {
        try {
            safeApiCall<Unit> { throw CancellationException("dibatalkan") }
            fail("CancellationException harus diteruskan")
        } catch (expected: CancellationException) {
            assertEquals("dibatalkan", expected.message)
        }
    }
}
