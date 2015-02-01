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
package net.maritimecloud.identityregistry.command.user;

import javax.annotation.Resource;
import net.maritimecloud.identityregistry.command.api.UnconfirmedUserEmailAddressSupplied;
import net.maritimecloud.identityregistry.command.api.UserEmailAddressVerified;
import net.maritimecloud.identityregistry.command.api.UserRegistered;
import net.maritimecloud.identityregistry.command.api.VerifyEmailAddress;
import net.maritimecloud.portal.application.ApplicationServiceRegistry;
import net.maritimecloud.portal.domain.infrastructure.axon.NoReplayedEvents;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.saga.annotation.AbstractAnnotatedSaga;
import org.axonframework.saga.annotation.EndSaga;
import org.axonframework.saga.annotation.SagaEventHandler;
import org.axonframework.saga.annotation.StartSaga;

/**
 *
 * @author Christoffer Børrild
 */
@NoReplayedEvents
public class ConfirmEmailAddressSaga extends AbstractAnnotatedSaga {

    @Resource
    private transient CommandGateway commandGateway;

    public CommandGateway getCommandGateway() {
        return commandGateway;
    }

    public void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "userId")
    public void handle(UserRegistered event) {

        System.out.println("A new user " + event.getPrefferedUsername() + " registered with the unconfirmed email  " + event.getEmailAddress() + ".");

        // compose and send out welcome and confirm email
        System.out.println("Sending out a confirmation email with email verification code: " + event.getEmailVerificationCode());
        ApplicationServiceRegistry.mailService().sendSignUpActivationMessage(
                event.getEmailAddress(),
                event.getPrefferedUsername(),
                event.getEmailVerificationCode()
        );

        // HACK: FIXME: TODO: 
        // auto-confirm users that fulfil some criteria
        autoConfirmTestUsersEmailAddress_HACK(event.getUserId(), event.getEmailAddress(), event.getEmailVerificationCode());
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "userId")
    public void handle(UnconfirmedUserEmailAddressSupplied event) {

        System.out.println("User " + event.getUsername() + " changed email address to the unconfirmed " + event.getUnconfirmedEmailAddress() + ".");

        // compose and send out confirm change email
        System.out.println("Sending out a confirmation email with email verification code: " + event.getEmailVerificationCode());
        ApplicationServiceRegistry.mailService().sendConfirmChangedEmailAddressMessage(
                event.getUnconfirmedEmailAddress(), 
                event.getUsername(), 
                event.getEmailVerificationCode()
        );

        // HACK: FIXME: TODO: 
        // auto-confirm users that fulfil some criteria
        autoConfirmTestUsersEmailAddress_HACK(event.getUserId(), event.getUnconfirmedEmailAddress(), event.getEmailVerificationCode());
    }

    private String autoConfirmTestUsersEmailAddress_HACK(UserId userId, String emailAddress, String emailVerificationCode) {
        // HACK: FIXME: TODO:
        // supply hardcoded code in odrer to auto-create users for test and demo without reading mails
        if (emailAddress.endsWith("@auto.demo.dma.dk")) {
            commandGateway.send(new VerifyEmailAddress(userId, emailVerificationCode));
        }
        return emailVerificationCode;
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "userId")
    public void handle(UserEmailAddressVerified event) {
        System.out.println("User " + event.getUsername() + " verified the email address " + event.getEmailAddress() + ".");
        System.out.println("User email address was verified: " + event.getEmailAddress() + " by " + event.getUsername());
    }

    // FIXME TODO: add en expire saga trigger to end unanswered saga instances!!!
    //@EndSaga
    //...
}
