package com.skaknna.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.skaknna.data.model.BoardDto
import kotlinx.coroutines.tasks.await

class RemoteBoardService {

    companion object {
        private const val TAG = "RemoteBoardService"
        private const val COLLECTION_USERS  = "users"
        private const val COLLECTION_BOARDS = "boards"
    }

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private fun boardsRef(userId: String) =
        db.collection(COLLECTION_USERS)
          .document(userId)
          .collection(COLLECTION_BOARDS)

    suspend fun getUserBoards(userId: String): Result<List<BoardDto>> {
        return try {
            val snapshot = boardsRef(userId).get().await()
            val boards = snapshot.documents.mapNotNull { it.toObject(BoardDto::class.java) }
            Log.d(TAG, "Fetched ${boards.size} boards for user=$userId")
            Result.success(boards)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching boards for user=$userId", e)
            Result.failure(e)
        }
    }

    suspend fun saveBoard(board: BoardDto): Result<Unit> {
        val userId = board.userId
            ?: return Result.failure(IllegalArgumentException("Cannot save board without userId"))
        return try {
            boardsRef(userId).document(board.id).set(board).await()
            Log.d(TAG, "Board=${board.id} uploaded for user=$userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading board=${board.id}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteBoard(boardId: String, userId: String): Result<Unit> {
        return try {
            boardsRef(userId).document(boardId).delete().await()
            Log.d(TAG, "Board=$boardId deleted for user=$userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting board=$boardId", e)
            Result.failure(e)
        }
    }
}
