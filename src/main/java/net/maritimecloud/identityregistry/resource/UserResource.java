/* Copyright (c) 2011 Danish Maritime Authority.
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
package net.maritimecloud.identityregistry.resource;

import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.maritimecloud.common.resource.AbstractCommandResource;
import net.maritimecloud.common.resource.JsonCommandHelper;
import static net.maritimecloud.common.resource.JsonCommandHelper.identityIsEmpty;
import static net.maritimecloud.common.resource.RestCommandUtil.resolveCommandName;
import net.maritimecloud.identityregistry.command.api.ChangeUserEmailAddress;
import net.maritimecloud.identityregistry.command.api.ChangeUserPassword;
import net.maritimecloud.identityregistry.command.api.RegisterUser;
import net.maritimecloud.identityregistry.command.api.VerifyEmailAddress;
import net.maritimecloud.identityregistry.query.UserEntry;
import net.maritimecloud.identityregistry.query.UserQueryRepository;
import net.maritimecloud.portal.application.ApplicationServiceRegistry;
import net.maritimecloud.serviceregistry.query.OrganizationMembershipEntry;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * @author Christoffer Børrild
 */
@Path("/api/users")
public class UserResource extends AbstractCommandResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    @Override
    protected CommandGateway commandGateway() {
        return ApplicationServiceRegistry.commandGateway();
    }

    private UserQueryRepository userQueryRepository() {
        return ApplicationServiceRegistry.userQueryRepository();
    }

    private String overwriteIdentity(String commandJSON, String propertyName, String value) {
        return JsonCommandHelper.overwriteIdentity(commandJSON, propertyName, value);
    }

    private String resolveUserIdOrFail(String username) {
        UserEntry userEntry = findByUsername(username);
        assertNotNull(userEntry, "No user found with username " + username);
        return userEntry.getUserId();
    }

    private UserEntry findByUsername(String aUsername) {
        return userQueryRepository().findByUsername(aUsername);
    }

    private static void assertNotNull(Object objectToTestForNull, String message) throws WebApplicationException {
        if (objectToTestForNull == null) {
            LOG.warn("Objct not found. {}", message);
            throw new WebApplicationException(message, Response.Status.NOT_FOUND);
        }
    }

    private void assertSameUser(UserEntry userEntry, String username) {
        if (!userEntry.getUsername().equals(username)) {
            LOG.warn("User identity mismatch. {} != {}", userEntry.getUsername(), username);
            throw new WebApplicationException("User identity mismatch", 404);
        }
    }

    // -------------------------------------------------------
    // -------------------------------------------------------
    // Commands
    // -------------------------------------------------------
    // -------------------------------------------------------
    @POST
    @Consumes(APPLICATION_JSON_CQRS_COMMAND)
    @Produces(MediaType.APPLICATION_JSON)
    //@Path("register")
    public void registerUserPostCommand(@HeaderParam("Content-type") String contentType, @QueryParam("command") @DefaultValue("") String queryCommandName, String commandJSON) {
        LOG.info("User POST command");
        if (identityIsEmpty(commandJSON, contentType)) {
            LOG.info("Empty userId -> AUTO creating UUID. Got:");
            LOG.info("JSON: " + commandJSON);
            commandJSON = overwriteIdentity(commandJSON, "userId", UUID.randomUUID().toString());
        }
        sendAndWait(contentType, queryCommandName, commandJSON,
                RegisterUser.class
        );
    }

    @PUT
    @Consumes(APPLICATION_JSON_CQRS_COMMAND)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{username}")
    public void userPutCommand(
            @HeaderParam("Content-type") String contentType,
            @QueryParam("command") @DefaultValue("") String queryCommandName,
            @PathParam("username") String username,
            String commandJSON
    ) {
        LOG.info("Organization PUT command");

        // only allow anonymous access to VerifyEmailAddress and ChangeUserPassword
        assertUserRole("USER", resolveCommandName(contentType, queryCommandName),
                ChangeUserEmailAddress.class
        );

        String userId = resolveUserIdOrFail(username);
        commandJSON = overwriteIdentity(commandJSON, "userId", userId);
        sendAndWait(contentType, queryCommandName, commandJSON,
                ChangeUserEmailAddress.class,
                ChangeUserPassword.class,
                VerifyEmailAddress.class
        );
    }

    // -------------------------------------------------------
    // -------------------------------------------------------
    // Queries
    // -------------------------------------------------------
    // -------------------------------------------------------
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Page<UserEntry> getUsers(
            @QueryParam("usernamePattern") String usernamePattern,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        // shori.ini allows "anon"-access to open for POST - restrict GET programaticly to USER role
        // (needed by invite-user and admin list users)
        requiresRoles("USER");

        // FIXME: we should hide the email address - it should only be visible from users profile!
        Pageable pageable = new PageRequest(page, size, new Sort(Sort.Direction.DESC, "username"));
        return usernamePattern == null
                ? userQueryRepository().findAll(pageable)
                : userQueryRepository().findByUsernameContainingIgnoreCase(usernamePattern, pageable);
    }
    
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUsersCount(
    ) {
        return "{\"usersCount\":" + userQueryRepository().count() + "}";
    }

    @GET
    @Path("{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserEntry getUser(@PathParam("username") String aUsername) {
        requiresRoles("USER");
        LOG.debug("Called getUser with username " + aUsername);
        LOG.warn("TODO: We should probably not expose this service publicly. Only logged in users should be able to get user info!!! ");

        // FIXME: user should only be able to access own profile unless is an admin 
        UserEntry userEntry = findByUsername(aUsername);
        assertNotNull(userEntry, "User not found");
        return userEntry;
    }

    /**
     * @param aUsername
     * @return A list of memberships of the organizations that the user is a member of
     */
    @GET
    @Path("{username}/orgs")
    @Consumes(MediaType.APPLICATION_JSON)
    public Iterable<OrganizationMembershipEntry> queryOrganizationMemberships(@PathParam("username") String aUsername) {
        requiresRoles("USER");
        return ApplicationServiceRegistry.organizationMembershipQueryRepository().findByUsername(aUsername);
    }

    @GET
    @Path("{username}/exist")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUsernameExist(@PathParam("username") String username) {
        return "{\"usernameExist\":" + (usernameExist(username) ? "true" : "false") + "}";
    }

    private boolean usernameExist(String username) {
        return findByUsername(username) != null;
    }

}
