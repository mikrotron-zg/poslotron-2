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
package hr.mikrotron.poslotron.party

import org.apache.ofbiz.base.util.UtilCodec
import org.apache.ofbiz.base.util.UtilValidate
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityFunction
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.util.EntityQuery

/*
 * Populates `suppliers` for the ListSupplier grid.
 *
 * Queries PartyRoleAndPartyDetail (Party + PartyRole + PartyGroup [+ Person])
 * so groupName is always present for ordering, regardless of whether the
 * user typed a filter. This avoids the performFindParty quirk where
 * sortField=groupName fails when the groupName filter is empty (PartyGroup
 * is not joined in that case).
 */

List conds = [
        EntityCondition.makeCondition('partyTypeId', 'PARTY_GROUP'),
        EntityCondition.makeCondition('roleTypeId', 'SUPPLIER'),
        EntityCondition.makeCondition('statusId', 'PARTY_ENABLED'),
]

String partyIdFilter = parameters.partyId
if (UtilValidate.isNotEmpty(partyIdFilter)) {
    conds << EntityCondition.makeCondition(
            EntityFunction.upperField('partyId'),
            EntityOperator.LIKE,
            EntityFunction.upper('%' + partyIdFilter + '%'))
}

String groupNameFilter = parameters.groupName
if (UtilValidate.isNotEmpty(groupNameFilter)) {
    conds << EntityCondition.makeCondition(
            EntityFunction.upperField('groupName'),
            EntityOperator.LIKE,
            EntityFunction.upper('%' + groupNameFilter + '%'))
}

// Honor column-header sort clicks. OFBiz grids submit sortField=<name> for
// ascending and sortField=-<name> for descending. DB-backed columns are
// sorted in SQL; computed columns (productCount, zeroQohCount) are sorted
// in-memory after enrichment. Default ordering is groupName ascending and
// partyId is always appended as a deterministic tiebreaker.
Set dbSortFields = ['partyId', 'groupName', 'createdDate'] as Set
Set computedSortFields = ['productCount', 'zeroQohCount'] as Set
Set allowedSortFields = dbSortFields + computedSortFields

String requestedSort = parameters.sortField
String sortField = 'groupName'
boolean descending = false
if (UtilValidate.isNotEmpty(requestedSort)) {
    String bare = requestedSort.startsWith('-') ? requestedSort.substring(1) : requestedSort
    if (allowedSortFields.contains(bare)) {
        sortField = bare
        descending = requestedSort.startsWith('-')
    }
}

// SQL orderBy uses the requested field only when it lives on the entity;
// otherwise fall back to the default (groupName) and let the in-memory
// post-sort step apply the user's choice.
String dbSortField = dbSortFields.contains(sortField) ? sortField : 'groupName'
boolean dbSortDescending = dbSortFields.contains(sortField) && descending
String primaryOrder = (dbSortDescending ? '-' : '') + dbSortField
List orderBy = [primaryOrder]
if (dbSortField != 'partyId') {
    orderBy << 'partyId'
}

List suppliers = EntityQuery.use(delegator)
        .from('PartyRoleAndPartyDetail')
        .where(EntityCondition.makeCondition(conds, EntityOperator.AND))
        .orderBy(orderBy)
        .distinct()
        .queryList()

// Batch-load primary web URL and primary email for the supplier set so we
// don't issue per-row queries. PartyContactDetailByPurpose joins PCM + PCMP
// + ContactMech and exposes infoString plus filterByDate-aware date fields.
Map<String, String> webByPartyId = [:]
Map<String, String> emailByPartyId = [:]
if (suppliers) {
    List partyIds = suppliers*.partyId.unique()

    List webRows = EntityQuery.use(delegator)
            .from('PartyContactDetailByPurpose')
            .where(EntityCondition.makeCondition([
                    EntityCondition.makeCondition('partyId', EntityOperator.IN, partyIds),
                    EntityCondition.makeCondition('contactMechPurposeTypeId', 'PRIMARY_WEB_URL'),
            ], EntityOperator.AND))
            .filterByDate()
            .queryList()
    webRows.each { row ->
        if (row.infoString && !webByPartyId.containsKey(row.partyId)) {
            webByPartyId[row.partyId] = row.infoString
        }
    }

    List emailRows = EntityQuery.use(delegator)
            .from('PartyContactDetailByPurpose')
            .where(EntityCondition.makeCondition([
                    EntityCondition.makeCondition('partyId', EntityOperator.IN, partyIds),
                    EntityCondition.makeCondition('contactMechPurposeTypeId', 'PRIMARY_EMAIL'),
            ], EntityOperator.AND))
            .filterByDate()
            .queryList()
    emailRows.each { row ->
        if (row.infoString && !emailByPartyId.containsKey(row.partyId)) {
            emailByPartyId[row.partyId] = row.infoString
        }
    }
}

// Per-supplier product roll-ups: total products supplied + how many of those
// have zero quantity-on-hand across all inventory items. SupplierProduct rows
// are filtered by their availableFromDate / availableThruDate so only
// currently-active offerings are counted; discontinued products
// (salesDiscontinuationDate IS NOT NULL) are excluded via the joined
// SupplierProductAndProduct view entity.
Map<String, Set<String>> productsByPartyId = [:]
if (suppliers) {
    List partyIds = suppliers*.partyId.unique()
    EntityQuery.use(delegator)
            .from('SupplierProductAndProduct')
            .where(EntityCondition.makeCondition([
                    EntityCondition.makeCondition('partyId', EntityOperator.IN, partyIds),
                    EntityCondition.makeCondition('salesDiscontinuationDate', EntityOperator.EQUALS, null),
            ], EntityOperator.AND))
            .filterByDate('availableFromDate', 'availableThruDate')
            .queryList()
            .each { sp ->
                productsByPartyId
                        .computeIfAbsent(sp.partyId as String) { new HashSet<String>() }
                        .add(sp.productId as String)
            }
}

// Sum quantityOnHandTotal per productId across all InventoryItem rows for the
// products supplied by this page of suppliers. A productId that has no
// InventoryItem row at all also counts as zero on hand.
Set<String> allSuppliedProductIds = productsByPartyId.values().collectMany { it } as Set
Map<String, BigDecimal> qohByProductId = [:]
if (allSuppliedProductIds) {
    EntityQuery.use(delegator)
            .from('InventoryItem')
            .where(EntityCondition.makeCondition('productId', EntityOperator.IN, new ArrayList(allSuppliedProductIds)))
            .queryList()
            .each { ii ->
                BigDecimal qoh = (ii.quantityOnHandTotal ?: BigDecimal.ZERO) as BigDecimal
                qohByProductId.merge(ii.productId as String, qoh) { a, b -> a + b }
            }
}

// Build the rendered HTML for the two action columns. The HtmlFormMacroLibrary
// emits ${description} unescaped, so we control the full anchor markup here
// (target="_blank", rel="nofollow noreferrer noopener") and HTML-encode the
// data values to neutralize any XSS in the stored infoString.
def htmlEncoder = UtilCodec.getEncoder('html')
String webLabel = htmlEncoder.encode((uiLabelMap?.PoslotronWebPage ?: 'Web page') as String)
String emailLabel = htmlEncoder.encode((uiLabelMap?.PoslotronSendEmail ?: 'Send e-mail') as String)

List enriched = suppliers.collect { gv ->
    Map row = new LinkedHashMap(gv.getAllFields())
    String partyId = gv.partyId
    String webUrl = webByPartyId[partyId]
    String email = emailByPartyId[partyId]
    row.webPageHtml = webUrl
            ? '<a href="' + htmlEncoder.encode(webUrl) + '" target="_blank" rel="nofollow noreferrer noopener">' + webLabel + '</a>'
            : ''
    row.emailHtml = email
            ? '<a href="mailto:' + htmlEncoder.encode(email) + '" rel="nofollow noreferrer">' + emailLabel + '</a>'
            : ''

    Set<String> productIds = productsByPartyId[partyId] ?: Collections.<String>emptySet()
    row.productCount = productIds.size()
    row.zeroQohCount = (int) productIds.count { pid ->
        BigDecimal total = qohByProductId[pid]
        total == null || total.signum() <= 0
    }
    return row
}

// Computed columns are sorted in-memory because they don't exist on the
// underlying entity. Tiebreaker on groupName + partyId keeps ordering stable.
if (computedSortFields.contains(sortField)) {
    int dir = descending ? -1 : 1
    enriched.sort { a, b ->
        int cmp = ((a[sortField] ?: 0) as Integer) <=> ((b[sortField] ?: 0) as Integer)
        if (cmp == 0) {
            cmp = ((a.groupName ?: '') as String) <=> ((b.groupName ?: '') as String)
        }
        if (cmp == 0) {
            cmp = ((a.partyId ?: '') as String) <=> ((b.partyId ?: '') as String)
        }
        return cmp * dir
    }
}

context.suppliers = enriched
