package org.resume.s3filemanager.constant;

import lombok.experimental.UtilityClass;

/**
 * Константы для JWT аутентификации и безопасности.
 */
@UtilityClass
public class SecurityConstants {

    public static final String TOKEN_TYPE = "JWT";
    public static final String CLAIM_ROLE = "role";
    public static final String COOKIE_NAME = "jwt_token";
    public static final String JWT_PREFIX = "jwt:token::";

    public static final String ANONYMOUS = "anonymous";
    public static final String UNKNOWN = "unknown";
}
