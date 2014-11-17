/* Copyright 2014 Danish Maritime Authority.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.maritimecloud.serviceregistry.command.serviceinstance;

import net.maritimecloud.common.infrastructure.axon.AbstractAxonCqrsIT;
import net.maritimecloud.serviceregistry.command.organization.CreateOrganizationCommand;
import net.maritimecloud.serviceregistry.command.organization.OrganizationId;
import net.maritimecloud.serviceregistry.command.organization.PrepareServiceSpecificationCommand;
import net.maritimecloud.serviceregistry.command.organization.ProvideServiceInstanceCommand;
import net.maritimecloud.serviceregistry.command.servicespecification.ServiceSpecificationId;
import net.maritimecloud.serviceregistry.query.ServiceInstanceEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test
 * <p>
 * (run with 'mvn failsafe:integration-test')
 * <p>
 * @author Christoffer Børrild
 */
public class ServiceInstanceIT extends AbstractAxonCqrsIT {

    private CreateOrganizationCommand createOrganizationCommand;
    private OrganizationId organizationId;
    private ServiceSpecificationId serviceSpecificationId;
    private PrepareServiceSpecificationCommand prepareServiceSpecificationCommand;
    private ProvideServiceInstanceCommand provideServiceInstanceCommand;

    @Before
    public void setUp() {
        // prepare an organization 
        createOrganizationCommand = generateCreateOrganizationCommand(generateIdentity());
        organizationId = createOrganizationCommand.getOrganizationId();
        // prepare a service specification
        serviceSpecificationId = generateServiceSpecificationId();
        prepareServiceSpecificationCommand
                = new PrepareServiceSpecificationCommand(organizationId, serviceSpecificationId, A_NAME, A_SUMMARY);
        // Prepare a service instance 
        provideServiceInstanceCommand = generateProvideServiceInstanceCommand();
    }

    /**
     * This test should show that an organization is able to publish a Service Instance of a specific Service Specification:
     */
    @Test
    public void provideServiceInstance() {

        // Given an organization with a Service Specification
        commandGateway().sendAndWait(createOrganizationCommand);
        commandGateway().sendAndWait(prepareServiceSpecificationCommand);

        // When the Organization publishes a Service Instance
        commandGateway().sendAndWait(provideServiceInstanceCommand);

        // Then the service instance is visible in views 
        assertEquals(1, serviceInstanceQueryRepository.count());
        assertTrue(serviceInstanceQueryRepository.exists(provideServiceInstanceCommand.getServiceInstanceId().identifier()));

        final ServiceInstanceEntry originalInstance = serviceInstanceQueryRepository.findOne(provideServiceInstanceCommand.getServiceInstanceId().identifier());
        assertEquals(A_NAME, originalInstance.getName());
        assertEquals(A_SUMMARY, originalInstance.getSummary());
    }

    @Test
    public void changeNameAndSummary() {

        // Given an organization with a Service Specification and a provided Service Instance
        commandGateway().sendAndWait(createOrganizationCommand);
        commandGateway().sendAndWait(prepareServiceSpecificationCommand);
        commandGateway().sendAndWait(provideServiceInstanceCommand);
        assertEquals(1, serviceInstanceQueryRepository.count());

        // When the name and summary are changed 
        commandGateway().sendAndWait(
                new ChangeServiceInstanceNameAndSummaryCommand(
                        provideServiceInstanceCommand.getServiceInstanceId(),
                        ANOTHER_NAME,
                        ANOTHER_SUMMARY));

        // Then the service instance is visible in views 
        final ServiceInstanceEntry instance = serviceInstanceQueryRepository.findOne(provideServiceInstanceCommand.getServiceInstanceId().identifier());
        assertEquals(ANOTHER_NAME, instance.getName());
        assertEquals(ANOTHER_SUMMARY, instance.getSummary());
    }

    private ProvideServiceInstanceCommand generateProvideServiceInstanceCommand() {
        return new ProvideServiceInstanceCommand(
                organizationId,
                serviceSpecificationId,
                generateServiceInstanceId(),
                A_NAME,
                A_SUMMARY,
                A_COVERAGE);
    }

}