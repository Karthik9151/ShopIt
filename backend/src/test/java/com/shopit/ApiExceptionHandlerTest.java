package com.shopit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shopit.exception.ApiExceptionHandler;
import com.shopit.security.TraceIdFilter;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class ApiExceptionHandlerTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ErrorTestController())
                .setControllerAdvice(new ApiExceptionHandler())
                .addFilters(new TraceIdFilter())
                .build();
    }

    @Test
    void malformedJsonMapsTo400() throws Exception {
        mvc.perform(post("/errors/body").contentType("application/json").content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    void pathTypeMismatchMapsTo400() throws Exception {
        mvc.perform(get("/errors/type/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request parameter"));
    }

    @Test
    void missingRequestParameterMapsTo400() throws Exception {
        mvc.perform(get("/errors/required"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required request parameter is missing"));
    }

    @Test
    void constraintViolationMapsTo400() throws Exception {
        mvc.perform(get("/errors/constraint"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"));
    }

    @Test
    void unsupportedMethodMapsTo405() throws Exception {
        mvc.perform(post("/errors/method"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void missingResourceMapsTo404() throws Exception {
        mvc.perform(get("/errors/resource"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    void missingHandlerMapsTo404() throws Exception {
        mvc.perform(get("/errors/handler"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    void dataIntegrityViolationMapsTo409() throws Exception {
        mvc.perform(get("/errors/integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The request conflicts with existing data"));
    }

    @Test
    void optimisticLockingFailureMapsTo409() throws Exception {
        mvc.perform(get("/errors/optimistic"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The request conflicts with existing data"));
    }

    @Test
    void accessDeniedMapsTo403() throws Exception {
        mvc.perform(get("/errors/denied"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unexpectedFailureHasTraceIdAndGenericMessage() throws Exception {
        mvc.perform(get("/errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("The request could not be completed"))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @RestController
    @RequestMapping("/errors")
    static class ErrorTestController {
        record Body(String name) {}

        @PostMapping("/body")
        String body(@RequestBody Body body) {
            return body.name();
        }

        @GetMapping("/type/{id}")
        String type(@PathVariable Long id) {
            return id.toString();
        }

        @GetMapping("/required")
        String required(@RequestParam String value) {
            return value;
        }

        @GetMapping("/constraint")
        String constraint() {
            throw new ConstraintViolationException(Set.of());
        }

        @GetMapping("/method")
        String method() {
            return "ok";
        }

        @GetMapping("/resource")
        String resource() throws NoResourceFoundException {
            throw new NoResourceFoundException(HttpMethod.GET, "/not-present");
        }

        @GetMapping("/handler")
        String handler() throws NoHandlerFoundException {
            throw new NoHandlerFoundException("GET", "/not-present", new HttpHeaders());
        }

        @GetMapping("/integrity")
        String integrity() {
            throw new DataIntegrityViolationException("private database details");
        }

        @GetMapping("/optimistic")
        String optimistic() {
            throw new OptimisticLockingFailureException("private database details");
        }

        @GetMapping("/denied")
        String denied() {
            throw new AccessDeniedException("forbidden");
        }

        @GetMapping("/unexpected")
        String unexpected() {
            throw new IllegalStateException("private internal details");
        }
    }
}
