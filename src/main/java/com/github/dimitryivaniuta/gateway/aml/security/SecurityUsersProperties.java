package com.github.dimitryivaniuta.gateway.aml.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Demo Basic Auth users loaded from application.yml (`security.users`).
 * In production, replace with external IdP / LDAP / OIDC.
 */
@ConfigurationProperties(prefix = "security")
public record SecurityUsersProperties(List<UserConfig> users) {
  public record UserConfig(String username, String password, List<String> roles) {}
}
