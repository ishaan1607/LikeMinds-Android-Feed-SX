package com.likeminds.feedsx.posttypes.view.adapter.databinder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.likeminds.feedsx.databinding.ItemPostMultipleMediaBinding
import com.likeminds.feedsx.overflowmenu.model.OverflowMenuItemViewData
import com.likeminds.feedsx.overflowmenu.view.OverflowMenuPopup
import com.likeminds.feedsx.overflowmenu.view.adapter.OverflowMenuAdapterListener
import com.likeminds.feedsx.posttypes.model.PostViewData
import com.likeminds.feedsx.posttypes.util.PostTypeUtil
import com.likeminds.feedsx.posttypes.view.adapter.PostAdapter.PostAdapterListener
import com.likeminds.feedsx.utils.customview.ViewDataBinder
import com.likeminds.feedsx.utils.model.ITEM_POST_MULTIPLE_MEDIA

class ItemPostMultipleMediaViewDataBinder constructor(
    val listener: PostAdapterListener
) : ViewDataBinder<ItemPostMultipleMediaBinding, PostViewData>(),
    OverflowMenuAdapterListener {

    private lateinit var overflowMenu: OverflowMenuPopup

    override val viewType: Int
        get() = ITEM_POST_MULTIPLE_MEDIA

    override fun createBinder(parent: ViewGroup): ItemPostMultipleMediaBinding {
        overflowMenu = OverflowMenuPopup.create(parent.context, this)

        return ItemPostMultipleMediaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    }

    override fun bindData(
        binding: ItemPostMultipleMediaBinding,
        data: PostViewData,
        position: Int
    ) {

        // handles various actions for the post
        PostTypeUtil.initActionsLayout(
            binding.postActionsLayout,
            data,
            listener,
            position
        )

        if (data.fromPostLiked || data.fromPostSaved) {
            listener.updateFromLikedSaved(position)
            return
        } else {
            // sets items to overflow menu
            PostTypeUtil.setOverflowMenuItems(
                overflowMenu,
                data.menuItems
            )

            // sets the view pager for multiple medias in the post
            PostTypeUtil.initViewPager(binding, data)

            // sets data to the creator frame
            PostTypeUtil.initAuthorFrame(
                binding.authorFrame,
                data,
                overflowMenu
            )

            // sets the text content of the post
            PostTypeUtil.initTextContent(
                binding.tvPostContent,
                data,
                itemPosition = position,
                listener
            )

        }
    }

    // handles the menu item click on the post
    override fun onMenuItemClicked(menu: OverflowMenuItemViewData) {
        overflowMenu.dismiss()
        listener.onPostMenuItemClicked(menu.entityId, menu.title)
    }

}