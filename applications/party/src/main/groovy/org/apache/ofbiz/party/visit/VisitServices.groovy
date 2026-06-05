/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ofbiz.party.visit

import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.service.ServiceUtil

import java.sql.Timestamp

/**
 * Force a user to be logged out by flagging UserLogin.hasLoggedOut = "Y" so that
 * the next request from that user is rejected by LoginWorker.checkLogout, and by
 * expiring all currently active Visit records for that userLoginId so the row
 * disappears from the active visits / logged-in users list.
 */
Map forceUserLogout() {
    String userLoginId = parameters.userLoginId
    if (!userLoginId) {
        return ServiceUtil.returnError('userLoginId is required')
    }

    GenericValue targetUserLogin = from('UserLogin').where('userLoginId', userLoginId).queryOne()
    if (!targetUserLogin) {
        return ServiceUtil.returnError("UserLogin not found: ${userLoginId}")
    }

    targetUserLogin.set('hasLoggedOut', 'Y')
    targetUserLogin.store()

    Timestamp now = UtilDateTime.nowTimestamp()
    List<GenericValue> activeVisits = from('Visit')
            .where(EntityCondition.makeCondition([
                    EntityCondition.makeCondition('userLoginId', userLoginId),
                    EntityCondition.makeCondition('thruDate', EntityOperator.EQUALS, null)
            ], EntityOperator.AND))
            .queryList()
    activeVisits.each { GenericValue visit ->
        visit.set('thruDate', now)
        visit.store()
    }

    Map result = ServiceUtil.returnSuccess()
    result.expiredVisitCount = activeVisits.size()
    return result
}
