package vn.iotstar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import vn.iotstar.security.CustomUserDetails;

@Service
@RequiredArgsConstructor
public class SessionRevocationService {
    private final SessionRegistry sessionRegistry;

    public void revokeAfterCommit(Long userId) {
        Runnable revoke = () -> sessionRegistry.getAllPrincipals().stream()
                .filter(CustomUserDetails.class::isInstance)
                .map(CustomUserDetails.class::cast)
                .filter(principal -> userId.equals(principal.getId()))
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .forEach(SessionInformation::expireNow);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { revoke.run(); }
            });
        } else {
            revoke.run();
        }
    }
}
