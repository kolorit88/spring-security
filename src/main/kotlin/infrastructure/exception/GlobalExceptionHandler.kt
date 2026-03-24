package infrastructure.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.http.MediaType
import shared.exception.BusinessException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import infrastructure.dto.response.error.ConflictErrorResponse
import infrastructure.dto.response.error.ResourceNotFoundErrorResponse
import infrastructure.dto.response.error.ValidationErrorResponse
import org.example.example.infrastructure.dto.response.error.common.ErrorResponse
import org.springframework.validation.FieldError

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = mutableMapOf<String, String>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as? FieldError)?.field ?: error.objectName
            val errorMessage = error.defaultMessage ?: "Invalid value"
            errors[fieldName] = errorMessage
        }

        val errorResponse = ValidationErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "Validation failed",
            error = errors
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val message = when (val cause = ex.cause) {
            is InvalidFormatException -> {
                val fieldName = cause.path.joinToString(".") { it.fieldName }
                "Invalid value for field '$fieldName'. Expected type: ${cause.targetType.simpleName}"
            }
            else -> "Malformed JSON request body. Please check your request format."
        }

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = message
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Invalid input data"
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }

    // Обработка User исключений
    @ExceptionHandler(BusinessException.UserNotFound::class)
    fun handleUserNotFound(ex: BusinessException.UserNotFound): ResponseEntity<ResourceNotFoundErrorResponse> {
        val errorResponse = ResourceNotFoundErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "User Not Found",
            message = ex.message ?: "User not found",
            resourceType = "User"
        )
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }

    // Обработка Dish исключений
    @ExceptionHandler(BusinessException.DishNotFound::class)
    fun handleDishNotFound(ex: BusinessException.DishNotFound): ResponseEntity<ResourceNotFoundErrorResponse> {
        val errorResponse = ResourceNotFoundErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Dish Not Found",
            message = ex.message ?: "Dish not found",
            resourceType = "Dish"
        )
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }

    @ExceptionHandler(BusinessException.DishNameAlreadyExists::class)
    fun handleDishNameExists(ex: BusinessException.DishNameAlreadyExists): ResponseEntity<ConflictErrorResponse> {
        val errorResponse = ConflictErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = "Dish Name Already Exists",
            message = ex.message ?: "Dish name already exists",
            resourceType = "Dish",
            resourceIdentifier = ex.message?.substringAfter(": ") // можно извлечь имя, если нужно
        )
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }

    // Обработка Restaurant исключений
    @ExceptionHandler(BusinessException.RestaurantNotFound::class)
    fun handleRestaurantNotFound(ex: BusinessException.RestaurantNotFound): ResponseEntity<ResourceNotFoundErrorResponse> {
        val errorResponse = ResourceNotFoundErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Restaurant Not Found",
            message = ex.message ?: "Restaurant not found",
            resourceType = "Restaurant"
        )
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }

    @ExceptionHandler(BusinessException.RestaurantNameAlreadyExists::class)
    fun handleRestaurantNameExists(ex: BusinessException.RestaurantNameAlreadyExists): ResponseEntity<ConflictErrorResponse> {
        val errorResponse = ConflictErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = "Restaurant Name Already Exists",
            message = ex.message ?: "Restaurant name already exists",
            resourceType = "Restaurant",
            resourceIdentifier = ex.message?.substringAfter(": ")
        )
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }

    // Обработка Order исключений
    @ExceptionHandler(BusinessException.OrderNotFound::class)
    fun handleOrderNotFound(ex: BusinessException.OrderNotFound): ResponseEntity<ResourceNotFoundErrorResponse> {
        val errorResponse = ResourceNotFoundErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Order Not Found",
            message = ex.message ?: "Order not found",
            resourceType = "Order"
        )
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }

    @ExceptionHandler(BusinessException.InvalidOrderStatusTransition::class)
    fun handleInvalidOrderStatusTransition(ex: BusinessException.InvalidOrderStatusTransition): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Invalid Status Transition",
            message = ex.message ?: "Invalid order status transition"
        )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }

    // Обработка OrderValidationError - возвращаем 400 BAD REQUEST
    @ExceptionHandler(BusinessException.OrderValidationError::class)
    fun handleOrderValidationError(ex: BusinessException.OrderValidationError): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Error",
            message = ex.message ?: "Invalid order data"
        )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }

    // Обработка общих валидационных исключений
    @ExceptionHandler(
        BusinessException.InvalidUserData::class,
        BusinessException.DishValidationError::class,
        BusinessException.UserValidationError::class,
        BusinessException.RestaurantValidationError::class
    )
    fun handleBusinessValidationError(ex: BusinessException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Error",
            message = ex.message ?: "Invalid data provided"
        )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }

    // Обработка Email конфликта
    @ExceptionHandler(BusinessException.EmailAlreadyExists::class)
    fun handleEmailExists(ex: BusinessException.EmailAlreadyExists): ResponseEntity<ConflictErrorResponse> {
        val errorResponse = ConflictErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = "Email Already Exists",
            message = ex.message ?: "Email already exists",
            resourceType = "User",
            resourceIdentifier = ex.message?.substringAfter(": ")
        )
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }

    // Общая обработка исключений
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        ex.printStackTrace()

        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred. Please try again later."
        )

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse)
    }
}