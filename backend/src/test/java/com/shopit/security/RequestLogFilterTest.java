package com.shopit.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.shopit.entity.AppUser;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(OutputCaptureExtension.class)
class RequestLogFilterTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void logsRequestMetadataButNeverBodyHeadersOrQueryString(CapturedOutput output) throws Exception {
        AppUser user = new AppUser();
        user.setId(77L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/orders");
        request.setQueryString("token=query-secret");
        request.addHeader("Authorization", "Bearer header-secret");
        request.setContent("payment-token=body-secret".getBytes(StandardCharsets.UTF_8));
        request.setAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE, "trace-123");

        MockHttpServletResponse response = new MockHttpServletResponse();
        new RequestLogFilter().doFilter(request, response, (incoming, outgoing) ->
                ((MockHttpServletResponse) outgoing).setStatus(201));

        assertThat(output)
                .contains("http_request")
                .contains("method=POST")
                .contains("path=/api/orders")
                .contains("status=201")
                .contains("userId=77")
                .contains("traceId=trace-123")
                .doesNotContain("query-secret")
                .doesNotContain("header-secret")
                .doesNotContain("body-secret")
                .doesNotContain("payment-token");
    }
}
