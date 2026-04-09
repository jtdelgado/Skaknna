package com.skaknna.data.repository

import com.skaknna.data.local.BoardDao
import com.skaknna.data.model.Board
import com.skaknna.data.model.toDomain
import com.skaknna.data.model.toDto
import com.skaknna.data.model.toEntity
import com.skaknna.data.remote.RemoteBoardService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Single Source of Truth for the Board data.
 * All reads go through the local DB via [getAllBoards].
 * All writes are saved locally first, then synced asynchronously.
 */
class BoardRepository(
    private val localDao: BoardDao,
    private val remoteService: RemoteBoardService
) {

    /**
     * Reactive stream of all boards. UI should collect this.
     */
    val allBoards: Flow<List<Board>> = localDao.getAllBoards()
        .map { entities -> entities.map { it.toDomain() } }
        .catch {
            // Handle any unexpected SQLite errors gracefully
            emit(emptyList())
        }

    /**
     * Get a single board by ID (useful when navigating to the editor detail screen).
     */
    suspend fun getBoardById(id: String): Board? {
        return localDao.getBoardById(id)?.toDomain()
    }

    /**
     * Offline-first save:
     * 1. Save to local DB with isSynced = false.
     * 2. Attempt to upload to Remote Service.
     * 3. If upload succeeds, mark as isSynced = true.
     */
    suspend fun saveBoard(board: Board) {
        val entityToSave = board.toEntity().copy(
            isSynced = false,
            updatedAt = System.currentTimeMillis()
        )
        localDao.insertBoard(entityToSave)

        // Only attempt cloud sync if the board is associated with a user
        if (board.userId != null) {
            val result = remoteService.saveBoard(board.toDto())
            if (result.isSuccess) {
                localDao.updateBoard(entityToSave.copy(isSynced = true))
            }
        }
    }

    /**
     * Initial sync when app opens or user logs in.
     * 1. Fetch remote boards.
     * 2. Merge with local boards based on updatedAt timestamp.
     * 3. Send unsynced local boards to remote.
     */
    suspend fun syncWithCloud(userId: String) {
        // Fetch remote
        val remoteResult = remoteService.getUserBoards(userId)
        val remoteBoards = remoteResult.getOrNull() ?: return

        // 1. Download/Merge from Cloud to Local
        for (dto in remoteBoards) {
            val local = localDao.getBoardById(dto.id)
            if (local == null) {
                // Not in local -> insert
                localDao.insertBoard(dto.toDomain().toEntity())
            } else {
                // Conflict resolution: Remote is newer
                if (dto.updatedAt > local.updatedAt) {
                    localDao.updateBoard(dto.toDomain().toEntity())
                }
            }
        }

        // 2. Upload from Local to Cloud
        val unsynced = localDao.getUnsyncedBoards()
        for (entity in unsynced) {
            val result = remoteService.saveBoard(entity.toDomain().toDto())
            if (result.isSuccess) {
                localDao.updateBoard(entity.copy(isSynced = true))
            }
        }
    }

    /**
     * Delete a board locally and then remotely.
     */
    suspend fun deleteBoard(boardId: String, userId: String?) {
        localDao.deleteBoardById(boardId)
        if (userId != null) {
            remoteService.deleteBoard(boardId, userId)
        }
    }
}
