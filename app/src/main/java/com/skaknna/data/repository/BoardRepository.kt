package com.skaknna.data.repository

import android.util.Log
import com.skaknna.data.local.BoardDao
import com.skaknna.data.model.Board
import com.skaknna.data.model.toDomain
import com.skaknna.data.model.toDto
import com.skaknna.data.model.toEntity
import com.skaknna.data.remote.RemoteBoardService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class BoardRepository(
    private val localDao: BoardDao,
    private val remoteService: RemoteBoardService
) {

    companion object {
        private const val TAG = "BoardRepository"
    }

    val allBoards: Flow<List<Board>> = localDao.getAllBoards()
        .map { entities -> entities.map { it.toDomain() } }
        .catch {
            Log.e(TAG, "Error reading boards from Room", it)
            emit(emptyList())
        }

    suspend fun getBoardById(id: String): Board? = localDao.getBoardById(id)?.toDomain()

    suspend fun saveBoard(board: Board) {
        val now = System.currentTimeMillis()
        val entity = board.toEntity().copy(isSynced = false, updatedAt = now)
        localDao.insertBoard(entity)
        Log.d(TAG, "Board '${board.name}' saved locally")

        if (board.userId != null) {
            val result = remoteService.saveBoard(entity.toDomain().toDto())
            if (result.isSuccess) {
                localDao.updateBoard(entity.copy(isSynced = true))
                Log.d(TAG, "Board '${board.name}' synced to Firestore immediately")
            } else {
                Log.w(TAG, "Board '${board.name}' deferred sync: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    suspend fun deleteBoard(boardId: String, userId: String?) {
        localDao.deleteBoardById(boardId)
        if (userId != null) {
            val result = remoteService.deleteBoard(boardId, userId)
            if (result.isFailure) {
                Log.w(TAG, "Could not delete board=$boardId from Firestore: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    suspend fun syncWithCloud(userId: String): Pair<Int, Int> {
        val remoteBoards = remoteService.getUserBoards(userId).getOrElse { throw it }
        val remoteIds = remoteBoards.map { it.id }.toSet()

        var downloaded = 0
        for (dto in remoteBoards) {
            val local = localDao.getBoardById(dto.id)
            when {
                local == null -> {
                    localDao.insertBoard(dto.toDomain().toEntity())
                    downloaded++
                }
                dto.updatedAt > local.updatedAt -> {
                    localDao.updateBoard(dto.toDomain().toEntity())
                    downloaded++
                }
            }
        }

        var uploaded = 0
        for (entity in localDao.getUnsyncedBoards()) {
            val withUser = if (entity.userId == null) entity.copy(userId = userId) else entity
            if (remoteService.saveBoard(withUser.toDomain().toDto()).isSuccess) {
                localDao.updateBoard(withUser.copy(isSynced = true))
                uploaded++
            }
        }

        var deletedLocally = 0
        val localUserBoards = localDao.getBoardsByUserId(userId)
        for (local in localUserBoards) {
            if (local.isSynced && !remoteIds.contains(local.id)) {
                localDao.deleteBoardById(local.id)
                deletedLocally++
            }
        }

        Log.d(TAG, "Sync complete: downloaded=$downloaded, uploaded=$uploaded, deletedLocally=$deletedLocally")
        return Pair(uploaded, downloaded)
    }
}
