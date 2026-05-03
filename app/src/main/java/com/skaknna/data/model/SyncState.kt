package com.skaknna.data.model

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val uploadedCount: Int = 0, val downloadedCount: Int = 0) : SyncState()
    data class Error(val message: String) : SyncState()
}
