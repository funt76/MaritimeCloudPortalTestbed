// This code was generated by net.maritimecloud.common.cqrs.contract.SourceGenerator
// Generated Code is based on the contract defined in net.maritimecloud.serviceregistry.command.ServiceRegistryContract
// Please modify the contract instead of this file!
package net.maritimecloud.serviceregistry.command.api;

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;
import net.maritimecloud.common.cqrs.contract.Event;
import net.maritimecloud.serviceregistry.command.organization.OrganizationId;
import net.maritimecloud.serviceregistry.command.serviceinstance.ServiceInstanceId;

/**
 * GENERATED CLASS!
 * @see net.maritimecloud.serviceregistry.command.ServiceRegistryContract#serviceInstanceAliasRegistrationDenied
 */
@Event
public class ServiceInstanceAliasRegistrationDenied {

    @TargetAggregateIdentifier
    private final OrganizationId organizationId;
    private final ServiceInstanceId serviceInstanceId;
    private final String alias;

    public ServiceInstanceAliasRegistrationDenied(
            OrganizationId organizationId,
            ServiceInstanceId serviceInstanceId,
            String alias
    ) {
        this.organizationId = organizationId;
        this.serviceInstanceId = serviceInstanceId;
        this.alias = alias;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
    }

    public ServiceInstanceId getServiceInstanceId() {
        return serviceInstanceId;
    }

    public String getAlias() {
        return alias;
    }

}

