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
package org.apache.ofbiz.fiscal.util

import java.sql.Timestamp

import org.apache.ofbiz.base.util.UtilDateTime

/**
 * Resolves the named "dateRange" options used by the fiscal UI (reporting form
 * and payment search form) into a concrete {@code [fromDate, thruDate]} pair.
 *
 * <p>Supported keys: {@code THIS_MONTH}, {@code LAST_MONTH}, {@code THIS_YEAR},
 * {@code LAST_YEAR}. Any other (or empty) key returns {@code null}.</p>
 */
final class FiscalDateRangeResolver {

    private FiscalDateRangeResolver() { }

    /**
     * @return a map with {@code fromDate} and {@code thruDate} Timestamps,
     *         or {@code null} if {@code dateRange} is empty / unrecognised.
     */
    static Map<String, Timestamp> resolve(String dateRange, TimeZone timeZone, Locale locale) {
        if (!dateRange) {
            return null
        }
        Timestamp now = UtilDateTime.nowTimestamp()
        Timestamp fromDate
        Timestamp thruDate
        switch (dateRange) {
            case 'THIS_MONTH':
                fromDate = UtilDateTime.getMonthStart(now, timeZone, locale)
                thruDate = UtilDateTime.getMonthEnd(now, timeZone, locale)
                break
            case 'LAST_MONTH':
                fromDate = UtilDateTime.getMonthStart(now, 0, -1, timeZone, locale)
                thruDate = UtilDateTime.getMonthEnd(fromDate, timeZone, locale)
                break
            case 'THIS_YEAR':
                fromDate = UtilDateTime.getYearStart(now, timeZone, locale)
                thruDate = UtilDateTime.getYearEnd(now, timeZone, locale)
                break
            case 'LAST_YEAR':
                fromDate = UtilDateTime.getYearStart(now, 0, -1, timeZone, locale)
                thruDate = UtilDateTime.getYearEnd(fromDate, timeZone, locale)
                break
            default:
                return null
        }
        return [fromDate: fromDate, thruDate: thruDate]
    }
}
