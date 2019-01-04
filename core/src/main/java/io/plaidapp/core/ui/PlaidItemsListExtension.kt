/*
 * Copyright 2019 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("PlaidItemsList")

package io.plaidapp.core.ui

import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.data.PlaidItemSorting
import io.plaidapp.core.data.prefs.SourcesRepository
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.domain.StoryWeigher
import io.plaidapp.core.dribbble.data.api.ShotWeigher
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.producthunt.data.api.PostWeigher
import io.plaidapp.core.producthunt.data.api.model.Post
import java.util.Collections

/**
 * Prepares items for display of de-duplicating items and sorting them (depending on the data
 * source).
 */
fun getPlaidItemsForDisplay(oldItems: List<PlaidItem>, newItems: List<PlaidItem>): List<PlaidItem> {
    val itemsToBeDisplayed = oldItems.toMutableList()
    weighItems(newItems.toMutableList())
    deduplicateAndAdd(itemsToBeDisplayed, newItems)
    sort(itemsToBeDisplayed)
    return itemsToBeDisplayed
}

/**
 * Calculate a 'weight' [0, 1] for each data type for sorting. Each data type/source has a
 * different metric for weighing it e.g. Dribbble uses likes etc. but some sources should keep
 * the order returned by the API. Weights are 'scoped' to the page they belong to and lower
 * weights are sorted earlier in the grid (i.e. in ascending weight).
 */
private fun weighItems(items: MutableList<out PlaidItem>?) {
    if (items == null || items.isEmpty()) return

    // TODO this weighting algo assumes that all items in the list are of the same type
    // update this to something better in the future

    // some sources should just use the natural order i.e. as returned by the API as users
    // have an expectation about the order they appear in
    if (SourcesRepository.SOURCE_PRODUCT_HUNT == items[0].dataSource) {
        PlaidItemSorting.NaturalOrderWeigher().weigh(items)
    } else {
        // otherwise use our own weight calculation. We prefer this as it leads to a less
        // regular pattern of items in the grid
        when {
            items[0] is Shot -> ShotWeigher().weigh(items as List<Shot>)
            items[0] is Story -> StoryWeigher().weigh(items as List<Story>)
            items[0] is Post -> PostWeigher().weigh(items as List<Post>)
            else -> throw RuntimeException("unknown item type")
        }
    }
}

/**
 * De-dupe as the same item can be returned by multiple feeds
 */
private fun deduplicateAndAdd(oldItems: MutableList<PlaidItem>, newItems: List<PlaidItem>) {
    val count = oldItems.count()
    newItems.forEach { newItem ->
        var add = true
        for (i in 0 until count) {
            val existingItem = oldItems[i]
            if (newItem == existingItem) {
                add = false
                break
            }
        }
        if (add) {
            oldItems.add(newItem)
        }
    }
}

private fun sort(items: MutableList<out PlaidItem>?) {
    Collections.sort<PlaidItem>(items, PlaidItemSorting.PlaidItemComparator()) // sort by weight
}
