/* Copyright 2015 Danish Maritime Authority.
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
package net.maritimecloud.portal.config;

import java.util.HashMap;
import java.util.Map;
import net.maritimecloud.portal.audit.axon.UserMetaData;
import org.axonframework.auditing.AuditDataProvider;
import org.axonframework.commandhandling.CommandMessage;

/**
 * AuditDataProvider that provides dummy data for integration tests
 * <p>
 * @author Christoffer Børrild
 */
public class IntergrationTestDummyAuditDataProvider implements AuditDataProvider {
    
    public static final String INTEGRATION_TEST_USER = "INTEGRATION_TEST_USER";    

    @Override
    public Map<String, Object> provideAuditDataFor(CommandMessage<?> command) {
        Map<String, Object> metaData = new HashMap<>();
        metaData.put(UserMetaData.USERID, -1);
        metaData.put(UserMetaData.USERNAME, INTEGRATION_TEST_USER);
        metaData.put(UserMetaData.USER_HOST, "0.0.0.0.1");
        return metaData;
    }

}
