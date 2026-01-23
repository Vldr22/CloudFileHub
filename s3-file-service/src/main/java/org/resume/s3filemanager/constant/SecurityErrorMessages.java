package org.resume.s3filemanager.constant;

import lombok.experimental.UtilityClass;

/**
 * Константы сообщений об ошибках для исключений связанные с безопасностью
 */
@UtilityClass
public class SecurityErrorMessages {

    public static final String WEAK_KEY_MESSAGE = "The provided key is vulnerable to HS256";
    public static final String INVALID_SECRET_KEY = "Invalid secret key";
    public static final String TOKEN_EXPIRED = "Token expired";
    public static final String TOKEN_INVALID = "Invalid token";
    public static final String INVALID_CREDENTIALS = "Invalid username or password";

}
