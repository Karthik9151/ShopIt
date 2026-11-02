package com.shopit.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ApiSurfaceFilterTest {
    @Test
    void disabledApiDocsReturn404AndKeepBaselineHeaders() throws Exception {
        ApiSurfaceFilter filter = new ApiSurfaceFilter(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v3/api-docs");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getContentAsString()).contains("Not found");
        assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeader("X-Frame-Options")).isEqualTo("DENY");
        verify(chain, org.mockito.Mockito.never()).doFilter(request, response);
    }

    @Test
    void enabledApiDocsPassThroughAndStillReceiveBaselineHeaders() throws Exception {
        ApiSurfaceFilter filter = new ApiSurfaceFilter(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("Referrer-Policy"))
                .isEqualTo("strict-origin-when-cross-origin");
        verify(chain).doFilter(request, response);
    }
}
