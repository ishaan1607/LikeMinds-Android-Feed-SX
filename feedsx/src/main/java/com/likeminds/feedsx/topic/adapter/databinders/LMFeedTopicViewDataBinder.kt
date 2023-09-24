package com.likeminds.feedsx.topic.adapter.databinders

import android.view.LayoutInflater
import android.view.ViewGroup
import com.likeminds.feedsx.databinding.LmFeedItemTopicBinding
import com.likeminds.feedsx.topic.adapter.LMFeedTopicSelectionAdapterListener
import com.likeminds.feedsx.topic.model.LMFeedTopicViewData
import com.likeminds.feedsx.utils.customview.ViewDataBinder
import com.likeminds.feedsx.utils.model.ITEM_TOPIC

class LMFeedTopicViewDataBinder(private val listener: LMFeedTopicSelectionAdapterListener) :
    ViewDataBinder<LmFeedItemTopicBinding, LMFeedTopicViewData>() {
    override val viewType: Int
        get() = ITEM_TOPIC

    override fun createBinder(parent: ViewGroup): LmFeedItemTopicBinding {
        val binding = LmFeedItemTopicBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        setListener(binding)
        return binding
    }

    override fun bindData(
        binding: LmFeedItemTopicBinding,
        data: LMFeedTopicViewData,
        position: Int
    ) {
        binding.apply {
            //set data to binding
            lmFeedTopic = data

            //set topic name
            tvTopicName.text = data.name

            //set isSelected
            ivSelected.isSelected = data.isSelected
        }
    }

    private fun setListener(binding: LmFeedItemTopicBinding) {
       binding.apply {
          root.setOnClickListener {
              val topic = lmFeedTopic ?: return@setOnClickListener
              listener.topicSelected(topic)
          }
       }
    }
}