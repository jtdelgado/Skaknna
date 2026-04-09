package com.skaknna.data.remote

import android.util.Log
import com.skaknna.data.model.BoardDto
import kotlinx.coroutines.delay

/**
 * Mock representation of the Firebase Firestore service.
 * Once the Firebase project is configured, this class will use
 * FirebaseFirestore.getInstance() to connect to the cloud.
 */
class RemoteBoardService {

    companion object {
        private const val TAG = "RemoteBoardService"
    }

    /**
     * Simulates fetching boards from the user's Firestore collection.
     */
    suspend fun getUserBoards(userId: String): Result<List<BoardDto>> {
        Log.d(TAG, "Mock getUserBoards called for user = $userId")
        // Simulate network delay
        delay(500)
        
        // Return an empty list for now. In reality, you would do:
        // db.collection("users").document(userId).collection("boards").get().await()
        return Result.success(emptyList())
    }

    /**
     * Simulates saving or updating a board in Firestore.
     */
    suspend fun saveBoard(board: BoardDto): Result<Unit> {
        Log.d(TAG, "Mock saveBoard called for board = ${board.id}")
        // Simulate network delay
        delay(500)
        
        // In reality: db.collection("users").document(userId).collection("boards").document(board.id).set(board).await()
        return Result.success(Unit)
    }

    /**
     * Simulates deleting a board from Firestore.
     */
    suspend fun deleteBoard(boardId: String, userId: String): Result<Unit> {
        Log.d(TAG, "Mock deleteBoard called for board = $boardId")
        // Simulate network delay
        delay(500)
        
        // In reality: db.collection("users").document(userId).collection("boards").document(boardId).delete().await()
        return Result.success(Unit)
    }
}
