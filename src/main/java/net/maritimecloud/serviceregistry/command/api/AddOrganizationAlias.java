// This code was generated by net.maritimecloud.common.cqrs.contract.SourceGenerator
// Generated Code is based on the contract defined in net.maritimecloud.serviceregistry.command.ServiceRegistryContract
// Please modify the contract instead of this file!
package net.maritimecloud.serviceregistry.command.api;

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;
import org.axonframework.common.Assert;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.maritimecloud.common.cqrs.Command;
import net.maritimecloud.serviceregistry.command.organization.OrganizationId;

/**
 * GENERATED CLASS!
 * @see net.maritimecloud.serviceregistry.command.ServiceRegistryContract#addOrganizationAlias
 */
public class AddOrganizationAlias implements Command {

    @TargetAggregateIdentifier
    private final OrganizationId organizationId;
    private final String alias;

    @JsonCreator
    public AddOrganizationAlias(
            @JsonProperty("organizationId") OrganizationId organizationId,
            @JsonProperty("alias") String alias
    ) {
        Assert.notNull(organizationId, "The organizationId must be provided");
        Assert.notNull(alias, "The alias must be provided");
        this.organizationId = organizationId;
        this.alias = alias;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
    }

    public String getAlias() {
        return alias;
    }

}

