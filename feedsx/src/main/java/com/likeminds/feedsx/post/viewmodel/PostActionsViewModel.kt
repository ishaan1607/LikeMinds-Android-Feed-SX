package com.likeminds.feedsx.post.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.likeminds.feedsx.LMAnalytics
import com.likeminds.feedsx.posttypes.model.PostViewData
import com.likeminds.feedsx.utils.UserPreferences
import com.likeminds.feedsx.utils.ViewUtils
import com.likeminds.feedsx.utils.coroutine.launchIO
import com.likeminds.likemindsfeed.LMFeedClient
import com.likeminds.likemindsfeed.post.model.DeletePostRequest
import com.likeminds.likemindsfeed.post.model.LikePostRequest
import com.likeminds.likemindsfeed.post.model.PinPostRequest
import com.likeminds.likemindsfeed.post.model.SavePostRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class PostActionsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val lmFeedClient = LMFeedClient.getInstance()

    private val _deletePostResponse = MutableLiveData<String>()
    val deletePostResponse: LiveData<String> = _deletePostResponse

    private val _pinPostResponse = MutableLiveData<String>()
    val pinPostResponse: LiveData<String> = _pinPostResponse

    private val errorMessageChannel = Channel<ErrorMessageEvent>(Channel.BUFFERED)
    val errorMessageEventFlow = errorMessageChannel.receiveAsFlow()

    sealed class ErrorMessageEvent {
        data class LikePost(val postId: String, val errorMessage: String?) : ErrorMessageEvent()
        data class SavePost(val postId: String, val errorMessage: String?) : ErrorMessageEvent()
        data class DeletePost(val errorMessage: String?) : ErrorMessageEvent()
        data class PinPost(val postId: String, val errorMessage: String?) : ErrorMessageEvent()
    }

    // returns user unique id from user prefs
    fun getUserUniqueId(): String {
        return userPreferences.getUserUniqueId()
    }

    // returns uuid from user prefs
    fun getUUID(): String {
        return userPreferences.getUUID()
    }

    //for like/unlike a post
    fun likePost(postId: String) {
        viewModelScope.launchIO {
            val request = LikePostRequest.Builder()
                .postId(postId)
                .build()

            //call like post api
            val response = lmFeedClient.likePost(request)

            //check for error
            if (!response.success) {
                errorMessageChannel.send(
                    ErrorMessageEvent.LikePost(
                        postId,
                        response.errorMessage
                    )
                )
            }
        }
    }

    //for save/un-save a post
    fun savePost(postId: String) {
        viewModelScope.launchIO {
            val request = SavePostRequest.Builder()
                .postId(postId)
                .build()

            //call save post api
            val response = lmFeedClient.savePost(request)

            //check for error
            if (!response.success) {
                errorMessageChannel.send(
                    ErrorMessageEvent.SavePost(
                        postId,
                        response.errorMessage
                    )
                )
            }
        }
    }

    //for delete post
    fun deletePost(
        post: PostViewData,
        reason: String? = null
    ) {
        viewModelScope.launchIO {
            val request = DeletePostRequest.Builder()
                .postId(post.id)
                .deleteReason(reason)
                .build()

            //call delete post api
            val response = lmFeedClient.deletePost(request)

            if (response.success) {
                // sends post deleted event
                sendPostDeletedEvent(post, reason)
                _deletePostResponse.postValue(post.id)
            } else {
                errorMessageChannel.send(ErrorMessageEvent.DeletePost(response.errorMessage))
            }
        }
    }

    /**
     * Triggers when a post is deleted
     **/
    private fun sendPostDeletedEvent(
        post: PostViewData,
        reason: String? = null
    ) {
        /**
         * if [reason] is [null], that it is a self delete
         */
        val userStateString = if (reason == null) {
            "Member"
        } else {
            "Community Manager"
        }

        //uuid of the post creator
        val postCreatorUUID = post.user.sdkClientInfoViewData.uuid
        val map = mapOf(
            "user_state" to userStateString,
            LMAnalytics.Keys.UUID to postCreatorUUID,
            LMAnalytics.Keys.POST_ID to post.id,
            "post_type" to ViewUtils.getPostTypeFromViewType(post.viewType),
        )
        LMAnalytics.track(
            LMAnalytics.Events.POST_DELETED,
            map
        )
    }

    //for pin/unpin post
    fun pinPost(post: PostViewData) {
        viewModelScope.launchIO {
            val request = PinPostRequest.Builder()
                .postId(post.id)
                .build()

            //call pin api
            val response = lmFeedClient.pinPost(request)

            if (response.success) {
                sendPinUnpinPostEvent(post)
                _pinPostResponse.postValue(post.id)
            } else {
                errorMessageChannel.send(
                    ErrorMessageEvent.PinPost(
                        post.id,
                        response.errorMessage
                    )
                )
            }
        }
    }

    /**
     * Triggers when a post is pinned/unpinned
     **/
    private fun sendPinUnpinPostEvent(post: PostViewData) {
        val postCreatorUUID = post.user.sdkClientInfoViewData.uuid
        val map = mapOf(
            "created_by_uuid" to postCreatorUUID,
            LMAnalytics.Keys.POST_ID to post.id,
            "post_type" to ViewUtils.getPostTypeFromViewType(post.viewType),
        )
        if (post.isPinned) {
            LMAnalytics.track(
                LMAnalytics.Events.POST_UNPINNED,
                map
            )
        } else {
            LMAnalytics.track(
                LMAnalytics.Events.POST_PINNED,
                map
            )
        }
    }

    /**
     * Triggers when the current user shares a post
     */
    fun sendPostShared(
        post: PostViewData
    ) {
        val postType = ViewUtils.getPostTypeFromViewType(post.viewType)

        val postCreatorUUID = post.user.sdkClientInfoViewData.uuid
        LMAnalytics.track(
            LMAnalytics.Events.POST_SHARED,
            mapOf(
                "created_by_uuid" to postCreatorUUID,
                LMAnalytics.Keys.POST_ID to post.id,
                "post_type" to postType,
            )
        )
    }
}