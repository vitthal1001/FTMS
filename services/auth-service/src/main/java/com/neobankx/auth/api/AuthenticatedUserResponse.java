package com.neobankx.auth.api;

import java.util.List;

public record AuthenticatedUserResponse(
        String subject,
        String email,
        List<String> roles
) {
}

