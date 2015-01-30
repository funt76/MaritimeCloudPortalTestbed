// This code was generated by net.maritimecloud.common.cqrs.contract.SourceGenerator
// Generated Code is based on the contract defined in net.maritimecloud.identityregistry.command.IdentityRegistryContract
// Please modify the contract instead of this file!
package net.maritimecloud.identityregistry.command.api;

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;
import net.maritimecloud.common.cqrs.contract.Event;
import net.maritimecloud.identityregistry.command.user.UserId;

/**
 * GENERATED CLASS!
 * @see net.maritimecloud.identityregistry.command.IdentityRegistryContract#UserAccountActivated
 */
@Event
public class UserAccountActivated {

    @TargetAggregateIdentifier
    private final UserId userId;
    private final String username;

    public UserAccountActivated(
            UserId userId,
            String username
    ) {
        this.userId = userId;
        this.username = username;
    }

    public UserId getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

}
