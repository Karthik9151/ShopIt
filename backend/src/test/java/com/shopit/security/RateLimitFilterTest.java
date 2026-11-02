package com.shopit.security;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

class RateLimitFilterTest {
    @Test
    void registrationIpLimitReturns429AndRetryAfterOnSixthRequest() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(
                new RateLimitService(true, 30, 1000), true, false);

        for (int index = 0; index < 5; index++) {
            MockHttpServletRequest request = registrationRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = new MockFilterChain();

            filter.doFilter(request, response, chain);

            assertThat(response.getStatus()).isNotEqualTo(429);
        }

        MockHttpServletResponse limitedResponse = new MockHttpServletResponse();
        filter.doFilter(registrationRequest(), limitedResponse, new MockFilterChain());

        assertThat(limitedResponse.getStatus()).isEqualTo(429);
        assertThat(limitedResponse.getHeader("Retry-After")).isNotBlank();
        assertThat(limitedResponse.getContentAsString()).contains("Too many requests");
    }

    private MockHttpServletRequest registrationRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/auth/register");
        request.setRemoteAddr("192.0.2.10");
        return request;
    }
}
