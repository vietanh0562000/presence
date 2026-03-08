package com.presence.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Stored in Spring Security's context after a Clerk JWT is verified.
 *
 * Retrieve it in any controller with:
 *   @AuthenticationPrincipal ClerkPrincipal principal
 *
 * The userId is Clerk's user ID (e.g. "user_2abc...") — use it as the
 * owner key for all MongoDB documents instead of managing your own users.
 */
@Getter
@AllArgsConstructor
public class ClerkPrincipal {
    private final String userId; // Clerk's user_xxx ID  (JWT "sub" claim)
    private final String email;  // primary email from Clerk token
}
