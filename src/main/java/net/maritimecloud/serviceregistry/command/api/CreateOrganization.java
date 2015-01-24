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
 * @see net.maritimecloud.serviceregistry.command.ServiceRegistryContract#createOrganization
 */
public class CreateOrganization implements Command {

    @TargetAggregateIdentifier
    private final OrganizationId organizationId;
    private final String primaryAlias;
    private final String name;
    private final String summary;
    private final String url;

    @JsonCreator
    public CreateOrganization(
            @JsonProperty("organizationId") OrganizationId organizationId,
            @JsonProperty("primaryAlias") String primaryAlias,
            @JsonProperty("name") String name,
            @JsonProperty("summary") String summary,
            @JsonProperty("url") String url
    ) {
        Assert.notNull(organizationId, "The organizationId must be provided");
        Assert.notNull(primaryAlias, "The primaryAlias must be provided");
        Assert.notNull(name, "The name must be provided");
        Assert.notNull(summary, "The summary must be provided");
        Assert.notNull(url, "The url must be provided");
        this.organizationId = organizationId;
        this.primaryAlias = primaryAlias;
        this.name = name;
        this.summary = summary;
        this.url = url;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
    }

    public String getPrimaryAlias() {
        return primaryAlias;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public String getUrl() {
        return url;
    }

}

