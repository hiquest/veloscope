package org.veloscope.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.veloscope.resource.UserEntity;

public final class SecurityHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityHelper.class);

    public static boolean amIAuthorized() {
        Authentication a = myAuthentication();
        if (a == null || !a.isAuthenticated()) {
            return false;
        }

        for (GrantedAuthority authority: a.getAuthorities()) {
            if (authority.getAuthority().equalsIgnoreCase(UserSimpleDetails.REGISTERED_ROLE)) {
                return true;
            }
        }

        return false;
    }

    public static Authentication myAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static void activateSession(UserEntity account) {
        UserSimpleDetails details = new UserSimpleDetails(account);
        SocialAuthToken authToken = new SocialAuthToken(details);
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    public static UserEntity me() {
        if (!amIAuthorized()) {
            return null;
        }

        return ((UserSimpleDetails) SecurityContextHolder.getContext().getAuthentication().getDetails()).getUser();
    }

    private static class SocialAuthToken extends AbstractAuthenticationToken {
        private UserSimpleDetails details;

        public SocialAuthToken(UserSimpleDetails details) {
            super(details.getAuthorities());
            this.details = details;
            setAuthenticated(true);
        }

        @Override
        public Object getCredentials() {
            return details;
        }

        @Override
        public Object getPrincipal() {
            return details;
        }
    }
}
