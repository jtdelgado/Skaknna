package com.skaknna.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.skaknna.data.model.SavedBoardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {

    /**
     * Get all boards sorted by creation date (newest first).
     * Returns a Flow to enable reactive UI updates whenever the DB changes.
     */
    @Query("SELECT * FROM saved_boards ORDER BY createdAt DESC")
    fun getAllBoards(): Flow<List<SavedBoardEntity>>

    /**
     * Get a specific board by its ID.
     */
    @Query("SELECT * FROM saved_boards WHERE id = :id")
    suspend fun getBoardById(id: String): SavedBoardEntity?

    /**
     * Get all boards that haven't been synced to Firebase yet.
     */
    @Query("SELECT * FROM saved_boards WHERE isSynced = 0")
    suspend fun getUnsyncedBoards(): List<SavedBoardEntity>

    /**
     * Insert or update a board.
     * Often used when receiving fresh data from Firebase or saving a new board.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoard(board: SavedBoardEntity)

    /**
     * Insert multiple boards at once (e.g. initial sync from cloud).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoards(boards: List<SavedBoardEntity>)

    /**
     * Update an existing board.
     */
    @Update
    suspend fun updateBoard(board: SavedBoardEntity)

    /**
     * Delete a board.
     */
    @Query("DELETE FROM saved_boards WHERE id = :id")
    suspend fun deleteBoardById(id: String)
}
