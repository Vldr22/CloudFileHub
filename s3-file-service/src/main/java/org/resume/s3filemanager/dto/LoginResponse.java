package org.resume.s3filemanager.dto;

public record LoginResponse(
        String token,
        String login,
        String role) {
}
