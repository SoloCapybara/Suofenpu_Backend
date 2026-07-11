package com.suofenpu.backend.common.aspect;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RequestLogAspect {
    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);
    private static final int MAX_LOG_TEXT_LEN = 2000;
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
        "authorization",
        "avatarurl",
        "code",
        "cookie",
        "openid",
        "password",
        "phone",
        "secret",
        "sessionkey",
        "token",
        "unionid"
    );

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Around("execution(* com.suofenpu.backend..*Controller.*(..))")
    public Object logControllerRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.currentTimeMillis();
        RequestInfo requestInfo = currentRequestInfo();
        String handler = joinPoint.getSignature().toShortString();
        String argsText = truncate(toJsonSafe(maskSensitive(filterArgs(joinPoint))));

        log.info(
            "接口请求开始：method={} path={} query={} ip={} handler={} args={}",
            requestInfo.method(),
            requestInfo.path(),
            emptyToDash(requestInfo.query()),
            requestInfo.clientIp(),
            handler,
            argsText
        );

        try {
            Object result = joinPoint.proceed();
            long costMs = System.currentTimeMillis() - startedAt;
            int status = responseStatus(result, requestInfo.attributes());
            String resultText = truncate(toJsonSafe(maskSensitive(result)));
            log.info(
                "接口请求结束：method={} path={} status={} costMs={} handler={} response={}",
                requestInfo.method(),
                requestInfo.path(),
                status,
                costMs,
                handler,
                resultText
            );
            return result;
        } catch (Throwable ex) {
            long costMs = System.currentTimeMillis() - startedAt;
            log.error(
                "接口请求异常：method={} path={} costMs={} handler={} error={}",
                requestInfo.method(),
                requestInfo.path(),
                costMs,
                handler,
                ex.getMessage(),
                ex
            );
            throw ex;
        }
    }

    private Map<String, Object> filterArgs(ProceedingJoinPoint joinPoint) {
        Map<String, Object> argMap = new LinkedHashMap<>();
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return argMap;
        }

        String[] parameterNames = parameterNames(joinPoint);
        for (int i = 0; i < args.length; i += 1) {
            Object arg = args[i];
            if (shouldIgnoreArg(arg)) {
                continue;
            }
            String name = parameterNames != null && i < parameterNames.length ? parameterNames[i] : "arg" + i;
            argMap.put(name, arg);
        }
        return argMap;
    }

    private String[] parameterNames(ProceedingJoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            return methodSignature.getParameterNames();
        }
        return null;
    }

    private boolean shouldIgnoreArg(Object arg) {
        if (arg == null) {
            return false;
        }
        if (arg instanceof ServletRequest || arg instanceof ServletResponse || arg instanceof MultipartFile) {
            return true;
        }
        if (arg instanceof MultipartFile[] || arg instanceof byte[]) {
            return true;
        }
        if (arg instanceof Object[] objectArray) {
            return Arrays.stream(objectArray).allMatch(MultipartFile.class::isInstance);
        }
        return false;
    }

    private Object maskSensitive(Object value) {
        if (value == null) {
            return null;
        }
        JsonNode tree = objectMapper.valueToTree(value);
        maskNode(tree, "");
        return tree;
    }

    private void maskNode(JsonNode node, String fieldName) {
        if (node == null) {
            return;
        }
        if (node instanceof ObjectNode objectNode) {
            objectNode.fieldNames().forEachRemaining(name -> {
                JsonNode child = objectNode.get(name);
                if (isSensitiveField(name)) {
                    objectNode.put(name, maskText(child == null || child.isNull() ? null : child.asText()));
                    return;
                }
                maskNode(child, name);
            });
            return;
        }
        if (node instanceof ArrayNode arrayNode) {
            arrayNode.forEach(child -> maskNode(child, fieldName));
        }
    }

    private boolean isSensitiveField(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        String normalized = name.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
        return SENSITIVE_FIELDS.stream().anyMatch(normalized::contains);
    }

    private String toJsonSafe(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            return String.valueOf(value);
        }
    }

    private String truncate(String text) {
        if (text == null) {
            return "null";
        }
        if (text.length() <= MAX_LOG_TEXT_LEN) {
            return text;
        }
        return text.substring(0, MAX_LOG_TEXT_LEN) + "...(truncated)";
    }

    private int responseStatus(Object result, ServletRequestAttributes attributes) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getStatusCode().value();
        }
        if (attributes != null && attributes.getResponse() != null) {
            return attributes.getResponse().getStatus();
        }
        return 200;
    }

    private RequestInfo currentRequestInfo() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null || attributes.getRequest() == null) {
            return new RequestInfo("-", "-", "", "-", null);
        }
        HttpServletRequest request = attributes.getRequest();
        return new RequestInfo(
            request.getMethod(),
            request.getRequestURI(),
            request.getQueryString(),
            clientIp(request),
            attributes
        );
    }

    private String clientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return Arrays.stream(xForwardedFor.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElse(xForwardedFor);
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        String remoteAddr = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        return remoteAddr;
    }

    private String emptyToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String maskText(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        if (value.length() <= 8) {
            return value.charAt(0) + "***" + value.charAt(value.length() - 1);
        }
        return value.substring(0, 4) + "***" + value.substring(value.length() - 4);
    }

    private record RequestInfo(
        String method,
        String path,
        String query,
        String clientIp,
        ServletRequestAttributes attributes
    ) {
    }
}
