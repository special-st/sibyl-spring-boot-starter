package st.special.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Base64

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    val name: String = "",
    val email: String? = null,
)

data class AuthenticatedUser(
    val id: Long,
    val uuid: String,
    val identifierType: String,
    val data: User,
) {
    companion object {
        private val objectMapper = ObjectMapper()

        fun fromHeaders(
            userId: String?,
            userUuid: String?,
            identifierType: String?,
            userDataBase64: String?,
        ): AuthenticatedUser? {
            if (userId.isNullOrBlank() || userUuid.isNullOrBlank()) return null

            val data = if (!userDataBase64.isNullOrBlank()) {
                try {
                    val json = Base64.getDecoder().decode(userDataBase64)
                    objectMapper.readValue(json, User::class.java)
                } catch (_: Exception) {
                    User()
                }
            } else {
                User()
            }

            return AuthenticatedUser(
                id = userId.toLongOrNull() ?: return null,
                uuid = userUuid,
                identifierType = identifierType ?: "UNKNOWN",
                data = data,
            )
        }
    }
}
