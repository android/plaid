/*
 * Copyright 2018 Google, Inc.
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

package io.plaidapp.core.designernews.domain

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.domain.model.Comment
import io.plaidapp.core.designernews.data.users.model.User
import io.plaidapp.core.designernews.parentComment
import io.plaidapp.core.designernews.parentCommentWithReplies
import io.plaidapp.core.designernews.parentCommentWithRepliesWithoutReplies
import io.plaidapp.core.designernews.parentCommentWithoutReplies
import io.plaidapp.core.designernews.reply1
import io.plaidapp.core.designernews.reply1NoUser
import io.plaidapp.core.designernews.replyWithReplies1
import io.plaidapp.core.designernews.user1
import io.plaidapp.core.designernews.user2
import io.plaidapp.core.designernews.data.users.UserRepository
import io.plaidapp.core.designernews.domain.model.CommentWithReplies
import io.plaidapp.test.shared.provideFakeCoroutinesContextProvider
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import java.io.IOException

/**
 * Tests for [CommentsUseCase] where all the dependencies are mocked.
 */
class CommentsUseCaseTest {
    private val commentsWithRepliesUseCase = Mockito.mock(CommentsWithRepliesUseCase::class.java)
    private val userRepository = Mockito.mock(UserRepository::class.java)
    private val repository = CommentsUseCase(
        commentsWithRepliesUseCase,
        userRepository,
        provideFakeCoroutinesContextProvider()
    )

    @Test
    fun getComments_noReplies_whenCommentsAnUserRequestsSuccessful() = runBlocking {
        // Given that the comments request responds with success
        val ids = listOf(11L)
        withComment(replyWithReplies1, ids)
        // Given that the user request responds with success
        withUsers(setOf(user1), setOf(111L))
        var result: Result<List<Comment>>? = null

        // When getting the replies
        repository.getComments(ids) { it -> result = it }

        // Then the correct list is received
        assertEquals(Result.Success(listOf(reply1)), result)
    }

    @Test
    fun getComments_noReplies_whenCommentsRequestFailed() = runBlocking {
        // Given that the commentsWithRepliesUseCase responds with failure
        val resultError = Result.Error(IOException("Comment error"))
        val ids = listOf(11L)
        Mockito.`when`(commentsWithRepliesUseCase.getCommentsWithReplies(ids)).thenReturn(resultError)
        var result: Result<List<Comment>>? = null

        // When getting the comments
        repository.getComments(ids) { it -> result = it }

        // Then the result is not successful
        assertNotNull(result)
        assertTrue(result is Result.Error)
    }

    @Test
    fun getComments_multipleReplies_whenCommentsAndUsersRequestsSuccessful() = runBlocking {
        // Given that:
        // When requesting replies for ids 1 from commentsWithRepliesUseCase we get the parent comment but
        // without replies embedded (since that's what the next call is doing)
        val parentIds = listOf(1L)
        withComment(parentCommentWithReplies, parentIds)
        withUsers(setOf(user1, user2), setOf(111L, 222L))
        var result: Result<List<Comment>>? = null

        // When getting the comments from the repository
        repository.getComments(listOf(1L)) { it -> result = it }

        // Then comments were requested for correct ids
        Mockito.verify(commentsWithRepliesUseCase).getCommentsWithReplies(parentIds)
        // Then the correct result is received
        assertEquals(Result.Success(listOf(parentComment)), result)
    }

    @Test
    fun getComments_multipleReplies_whenRepliesRequestFailed() = runBlocking {
        // Given that when requesting replies for ids 1 from commentsWithRepliesUseCase we get the parent comment
        val parentIds = listOf(1L)
        withComment(parentCommentWithRepliesWithoutReplies, parentIds)
        // Given that the user request responds with success
        withUsers(setOf(user2), setOf(222))
        var result: Result<List<Comment>>? = null

        // When getting the comments from the repository
        repository.getComments(listOf(1L)) { it -> result = it }

        // Then comments were requested for correct ids
        Mockito.verify(commentsWithRepliesUseCase).getCommentsWithReplies(parentIds)
        // Then the correct result is received
        assertEquals(Result.Success(arrayListOf(parentCommentWithoutReplies)), result)
    }

    @Test
    fun getComments_whenUserRequestFailed() = runBlocking {
        // Given that:
        // When requesting replies for ids 1 from commentsWithRepliesUseCase we get the parent comment but
        // without replies embedded (since that's what the next call is doing)
        val ids = listOf(11L)
        withComment(replyWithReplies1, ids)
        // Given that the user request responds with failure
        val userError = Result.Error(IOException("User error"))
        Mockito.`when`(userRepository.getUsers(setOf(11L))).thenReturn(userError)
        var result: Result<List<Comment>>? = null

        // When getting the comments from the repository
        repository.getComments(listOf(11L)) { it -> result = it }

        // Then comments were requested for correct ids
        Mockito.verify(commentsWithRepliesUseCase).getCommentsWithReplies(ids)
        // Then the correct result is received
        assertEquals(Result.Success(arrayListOf(reply1NoUser)), result)
    }

    // Given that the users request responds with success
    private fun withUsers(users: Set<User>, ids: Set<Long>) = runBlocking {
        val userResult = Result.Success(users)
        Mockito.`when`(userRepository.getUsers(ids)).thenReturn(userResult)
    }

    private fun withComment(comment: CommentWithReplies, ids: List<Long>) = runBlocking {
        val resultParent = Result.Success(listOf(comment))
        Mockito.`when`(commentsWithRepliesUseCase.getCommentsWithReplies(ids)).thenReturn(resultParent)
    }

    private fun withComments(comment: List<CommentWithReplies>, ids: List<Long>) = runBlocking {
        val resultParent = Result.Success(comment)
        Mockito.`when`(commentsWithRepliesUseCase.getCommentsWithReplies(ids)).thenReturn(resultParent)
    }
}
