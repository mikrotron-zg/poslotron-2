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
package hr.mikrotron.poslotron.product

import org.apache.ofbiz.base.util.UtilValidate
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.util.EntityQuery

/*
 * Populates `deprecationRows` for the ListProductDeprecation grid.
 *
 * Identifies products that look ripe for deprecation: still "active for sale"
 * (no Product.salesDiscontinuationDate), but unreachable from any catalog
 * (not currently a member of any ProductCategory) and stale in the warehouse
 * (aggregate quantityOnHandTotal == 0 AND aggregate availableToPromiseTotal
 * == 0 across InventoryItem). All filters are hard-coded; the user has no
 * search controls on this page.
 *
 * Result columns: productId, internalName, createdStamp, accountingQuantityTotal.
 * Default order: AQT descending. The grid columns are all marked sortable;
 * sortField=<name> / sortField=-<name> applies an in-memory sort here so the
 * ordering applies before the form widget paginates the list.
 *
 * Note: unlike WarehouseStatus.groovy we do NOT honour the "future-dated
 * discontinuation is still active" leniency. The report is specifically
 * about products whose salesDiscontinuationDate is unset, so anything with
 * a value (past or future) is excluded.
 */

// ===== Step 1: candidate products (no deprecation date) =====
List<String> candidateIds = EntityQuery.use(delegator)
        .from('Product')
        .where(EntityCondition.makeCondition('salesDiscontinuationDate', EntityOperator.EQUALS, null))
        .select('productId')
        .queryList()
        .collect { it.productId as String }

if (!candidateIds) {
    context.deprecationRows = []
    return
}

// ===== Step 2: drop products that are currently a member of any category =====
// "Currently" = active by date (filterByDate). A product whose only category
// memberships have all expired is effectively orphaned and should still
// surface here.
Set<String> categorizedIds = new HashSet<String>()
EntityQuery.use(delegator)
        .from('ProductCategoryMember')
        .where(EntityCondition.makeCondition('productId', EntityOperator.IN, candidateIds))
        .filterByDate()
        .select('productId')
        .queryList()
        .each { categorizedIds << (it.productId as String) }

candidateIds = candidateIds.findAll { !categorizedIds.contains(it) }
if (!candidateIds) {
    context.deprecationRows = []
    return
}

// ===== Step 3: load Product rows we will display =====
// Need internalName and createdStamp on top of productId.
// Also exclude non-physical products (services, digital goods, …) whose
// ProductType.isPhysical == 'N' — they never appear in inventory so the
// QOH/ATP == 0 filter alone would surface every service product.
Set<String> nonPhysicalTypeIds = EntityQuery.use(delegator)
        .from('ProductType')
        .where(EntityCondition.makeCondition('isPhysical', EntityOperator.EQUALS, 'N'))
        .select('productTypeId')
        .queryList()
        .collect { it.productTypeId as String }
        .toSet()

Map<String, Object> productById = [:]
EntityQuery.use(delegator)
        .from('Product')
        .where(EntityCondition.makeCondition('productId', EntityOperator.IN, candidateIds))
        .queryList()
        .each { p ->
            if (!nonPhysicalTypeIds.contains(p.productTypeId as String)) {
                productById[p.productId as String] = p
            }
        }

candidateIds = candidateIds.findAll { productById.containsKey(it) }
if (!candidateIds) {
    context.deprecationRows = []
    return
}

// ===== Step 4: aggregate InventoryItem totals per product =====
// Sum quantityOnHandTotal / availableToPromiseTotal / accountingQuantityTotal
// across all InventoryItem rows. We keep a row only when QOH and ATP both
// sum to exactly zero (signum() == 0); AQT can be anything (including
// negative, which is what we typically want to surface here).
Map<String, Map> invByProduct = [:]
candidateIds.each { pid ->
    invByProduct[pid] = [qoh: BigDecimal.ZERO, atp: BigDecimal.ZERO, aqt: BigDecimal.ZERO]
}
EntityQuery.use(delegator)
        .from('InventoryItem')
        .where(EntityCondition.makeCondition('productId', EntityOperator.IN, candidateIds))
        .queryList()
        .each { ii ->
            Map agg = invByProduct[ii.productId as String]
            if (agg == null) return
            agg.qoh = (agg.qoh as BigDecimal) + ((ii.quantityOnHandTotal ?: BigDecimal.ZERO) as BigDecimal)
            agg.atp = (agg.atp as BigDecimal) + ((ii.availableToPromiseTotal ?: BigDecimal.ZERO) as BigDecimal)
            agg.aqt = (agg.aqt as BigDecimal) + ((ii.accountingQuantityTotal ?: BigDecimal.ZERO) as BigDecimal)
        }

// ===== Step 5: assemble result rows =====
List<Map> rows = []
candidateIds.each { pid ->
    Map agg = invByProduct[pid]
    if ((agg.qoh as BigDecimal).signum() != 0) return
    if ((agg.atp as BigDecimal).signum() != 0) return
    def product = productById[pid]
    if (product == null) return
    rows << [
            productId              : pid,
            internalName           : product.internalName,
            createdStamp           : product.createdStamp,
            accountingQuantityTotal: agg.aqt,
    ]
}

// ===== Step 6: sort =====
// Honor column-header clicks. All result columns are sortable; the default
// is AQT descending. productId is the deterministic tiebreaker.
Set sortableFields = [
        'productId', 'internalName', 'createdStamp', 'accountingQuantityTotal',
] as Set
String requestedSort = parameters.sortField as String
String sortField = 'accountingQuantityTotal'
boolean descending = true
if (UtilValidate.isNotEmpty(requestedSort)) {
    String bare = requestedSort.startsWith('-') ? requestedSort.substring(1) : requestedSort
    if (sortableFields.contains(bare)) {
        sortField = bare
        descending = requestedSort.startsWith('-')
    }
}

int dir = descending ? -1 : 1
Comparator nullSafe = { a, b ->
    if (a == null && b == null) return 0
    if (a == null) return 1   // nulls last on ascending
    if (b == null) return -1
    return (a as Comparable).compareTo(b as Comparable)
} as Comparator

rows.sort { Map a, Map b ->
    int cmp = nullSafe.compare(a[sortField], b[sortField]) * dir
    if (cmp != 0) return cmp
    return nullSafe.compare(a.productId, b.productId)
}

context.deprecationRows = rows
