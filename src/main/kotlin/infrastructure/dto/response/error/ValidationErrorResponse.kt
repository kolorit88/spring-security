package infrastructure.dto.response.error
import org.example.example.infrastructure.dto.response.error.common.ErrorResponse
import java.time.LocalDateTime

class ValidationErrorResponse(
      status: Int,
      message: String? = null,
      val errors: Map<String, String>,
      timestamp: LocalDateTime = LocalDateTime.now()
): ErrorResponse(status, message, timestamp=timestamp)