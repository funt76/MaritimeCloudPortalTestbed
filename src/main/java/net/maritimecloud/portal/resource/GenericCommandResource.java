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
package net.maritimecloud.portal.resource;

import net.maritimecloud.common.cqrs.CommandRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import net.maritimecloud.common.cqrs.Command;
import net.maritimecloud.portal.application.ApplicationServiceRegistry;
import static net.maritimecloud.portal.resource.RestCommandUtil.readCommand;
import static net.maritimecloud.portal.resource.RestCommandUtil.resolveCommandName;
import net.maritimecloud.serviceregistry.command.api.ChangeOrganizationNameAndSummary;
import net.maritimecloud.serviceregistry.command.api.CreateOrganization;
import net.maritimecloud.serviceregistry.command.api.PrepareServiceSpecification;
import net.maritimecloud.serviceregistry.command.api.ProvideServiceInstance;
import net.maritimecloud.serviceregistry.command.api.AddServiceInstanceEndpoint;
import net.maritimecloud.serviceregistry.command.api.ChangeServiceInstanceNameAndSummary;
import net.maritimecloud.serviceregistry.command.api.RemoveServiceInstanceEndpoint;
import net.maritimecloud.serviceregistry.command.api.ChangeServiceSpecificationNameAndSummary;
import org.axonframework.commandhandling.CommandExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christoffer Børrild
 */
@Path("/api/command")
public class GenericCommandResource {

    private static final Logger LOG = LoggerFactory.getLogger(GenericCommandResource.class);
    
    public static final String APPLICATION_JSON_CQRS_COMMAND = MediaType.APPLICATION_JSON + ";domain-model=*Command";

    private static final CommandRegistry postCommandsRegistry = new CommandRegistry(
            CreateOrganization.class,
            PrepareServiceSpecification.class,
            AddServiceInstanceEndpoint.class,
            RemoveServiceInstanceEndpoint.class,
            ProvideServiceInstance.class
    );
    private static final CommandRegistry putCommandsRegistry = new CommandRegistry(
            ChangeOrganizationNameAndSummary.class,
            ChangeServiceSpecificationNameAndSummary.class,
            ChangeServiceInstanceNameAndSummary.class
    );
    private static final CommandRegistry deleteCommandsRegistry = new CommandRegistry();
    private static final CommandRegistry patchCommandsRegistry = new CommandRegistry();

    
    public static void sendAndWait(String contentType, String queryCommandName, String commandJSON, Class... classes) {
        sendAndWait(contentType, queryCommandName, new CommandRegistry(classes), commandJSON);
    }
    
    public static void sendAndWait(String contentType, String queryCommandName, CommandRegistry commandRegistry, String commandJSON) throws WebApplicationException {
        try {
            LOG.info("Received command: Cmd={}{}", contentType, queryCommandName);
            LOG.info("JSON: {}", commandJSON);
            Class commandClass = commandRegistry.resolve(resolveCommandName(contentType, queryCommandName));
            Object command = readCommand(commandJSON, commandClass);
            sendAndWait((Command) command);
            
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Throwable ex) {
            LOG.error("Error occured when reading command!", ex);
            throw new WebApplicationException("Error occured when reading command!", ex);
        }
    }

    public static void sendAndWait(Command command) throws WebApplicationException {
        try {
            ApplicationServiceRegistry.commandGateway().sendAndWait(command);
        } catch (CommandExecutionException e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                throw new WebApplicationException("Illegal Argument", e, 400);
            } else {
                LOG.error("Error occured when reading command!", e);
                throw new WebApplicationException("Error occured when reading command!", e);
            }
        } catch (Throwable ex) {
            LOG.error("Error occured when reading command!", ex);
            throw new WebApplicationException("Error occured when reading command!", ex);
        }
    }

    @POST
    @Consumes(APPLICATION_JSON_CQRS_COMMAND)
    @Produces(MediaType.APPLICATION_JSON)
    public void mappedPostCommand(@HeaderParam("Content-type") String contentType, @QueryParam("command") @DefaultValue("") String queryCommandName, String commandJSON) {
        LOG.info("POST command: " + commandJSON);
        sendAndWait(contentType, queryCommandName, postCommandsRegistry, commandJSON);
    }

    @PUT
    @Consumes(APPLICATION_JSON_CQRS_COMMAND)
    @Produces(MediaType.APPLICATION_JSON)
    public void mappedPutCommand(@HeaderParam("Content-type") String contentType, @QueryParam("command") @DefaultValue("") String queryCommandName, String commandJSON) {
        LOG.info("PUT command: " + commandJSON);
        sendAndWait(contentType, queryCommandName, putCommandsRegistry, commandJSON);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CommandEntry> getCommands(@QueryParam("type") @DefaultValue("") String usernamePattern) {
        List<CommandEntry> commands = new ArrayList<>();
        commands.addAll(list("POST", postCommandsRegistry));
        commands.addAll(list("PUT", putCommandsRegistry));
        commands.addAll(list("PATCH", patchCommandsRegistry));
        commands.addAll(list("DELETE", deleteCommandsRegistry));
        LOG.info("Supported commands: " + commands);
        return commands;
    }

    private Collection<? extends CommandEntry> list(String requestType, CommandRegistry commandRegistry) {
        List<CommandEntry> commands = new ArrayList<>();
        commandRegistry.entries().stream().forEach((entrySet) -> {
            String commandName = entrySet.getKey();
            String commandType = entrySet.getValue().getCanonicalName();
            commands.add(new CommandEntry(requestType, commandName, commandType));
        });
        return commands;
    }

    public static class CommandEntry {

        String requestType;
        String commandName;
        String commandType;

        public CommandEntry() {
        }

        public CommandEntry(String requestType, String commandName, String commandType) {
            this.requestType = requestType;
            this.commandName = commandName;
            this.commandType = commandType;
        }

        public String getRequestType() {
            return requestType;
        }

        public void setRequestType(String requestType) {
            this.requestType = requestType;
        }

        public String getCommandName() {
            return commandName;
        }

        public void setCommandName(String commandName) {
            this.commandName = commandName;
        }

        public String getCommandType() {
            return commandType;
        }

        public void setCommandType(String commandType) {
            this.commandType = commandType;
        }

    }

}
