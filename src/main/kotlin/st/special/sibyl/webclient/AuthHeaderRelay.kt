package st.special.sibyl.webclient

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

class AuthHeaderRelayFilter : ExchangeFilterFunction {

    companion object {
        val RELAY_HEADERS = setOf(
            "X-User-Id",
            "X-User-Uuid",
            "X-User-Identifier-Type",
            "X-User-Data",
        )
    }

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: return next.exchange(request)

        val servletRequest = attributes.request
        val builder = ClientRequest.from(request)

        for (header in RELAY_HEADERS) {
            val value = servletRequest.getHeader(header)
            if (value != null && request.headers().getFirst(header) == null) {
                builder.header(header, value)
            }
        }

        return next.exchange(builder.build())
    }
}
