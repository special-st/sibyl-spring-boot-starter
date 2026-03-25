package st.special.sibyl.auth

import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.server.ResponseStatusException
import st.special.common.AuthenticatedUser

class AuthUserArgumentResolver : HandlerMethodArgumentResolver {

    companion object {
        private const val HEADER_USER_ID = "X-User-Id"
        private const val HEADER_USER_UUID = "X-User-Uuid"
        private const val HEADER_IDENTIFIER_TYPE = "X-User-Identifier-Type"
        private const val HEADER_USER_DATA = "X-User-Data"
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(RequestUserId::class.java)
            || parameter.hasParameterAnnotation(RequestUserUuid::class.java)
            || parameter.hasParameterAnnotation(RequestUser::class.java)
            || parameter.hasParameterAnnotation(RequestIdentifierType::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val userId = webRequest.getHeader(HEADER_USER_ID)
        val userUuid = webRequest.getHeader(HEADER_USER_UUID)
        val identifierType = webRequest.getHeader(HEADER_IDENTIFIER_TYPE)
        val userData = webRequest.getHeader(HEADER_USER_DATA)

        if (parameter.hasParameterAnnotation(RequestUserId::class.java)) {
            val id = userId?.toLongOrNull()
                ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid $HEADER_USER_ID")
            return id
        }

        if (parameter.hasParameterAnnotation(RequestUserUuid::class.java)) {
            if (userUuid.isNullOrBlank()) {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing $HEADER_USER_UUID")
            }
            return userUuid
        }

        if (parameter.hasParameterAnnotation(RequestIdentifierType::class.java)) {
            if (identifierType.isNullOrBlank()) {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing $HEADER_IDENTIFIER_TYPE")
            }
            return identifierType
        }

        if (parameter.hasParameterAnnotation(RequestUser::class.java)) {
            val user = AuthenticatedUser.fromHeaders(userId, userUuid, identifierType, userData)
                ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication headers")
            return user
        }

        return null
    }
}
