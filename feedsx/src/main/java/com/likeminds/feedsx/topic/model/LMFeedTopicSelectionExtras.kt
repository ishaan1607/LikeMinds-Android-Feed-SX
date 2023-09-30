package com.likeminds.feedsx.topic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class LMFeedTopicSelectionExtras private constructor(
    val showAllTopicFilter: Boolean,
    val selectedTopics: List<LMFeedTopicViewData>?
) : Parcelable {

    class Builder {
        private var showAllTopicFilter: Boolean = false
        private var selectedTopics: List<LMFeedTopicViewData>? = null

        fun showAllTopicFilter(showAllTopicFilter: Boolean) =
            apply { this.showAllTopicFilter = showAllTopicFilter }

        fun selectedTopics(selectedTopics: List<LMFeedTopicViewData>?) =
            apply { this.selectedTopics = selectedTopics }

        fun build() = LMFeedTopicSelectionExtras(showAllTopicFilter, selectedTopics)
    }

    fun toBuilder(): Builder {
        return Builder().showAllTopicFilter(showAllTopicFilter)
            .selectedTopics(selectedTopics)
    }
}