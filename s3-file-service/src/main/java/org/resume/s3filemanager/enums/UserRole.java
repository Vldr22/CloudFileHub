package org.resume.s3filemanager.enums;

public enum UserRole {
    ADMIN,
    USER;

    private static final String PREFIX = "ROLE_";

    public String getAuthority() {
        return PREFIX + name();
    }
}
