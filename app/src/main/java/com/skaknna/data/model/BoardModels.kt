package com.skaknna.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// ─── Domain Model ────────────────────────────────────────────────────────────
/**
 * The core domain object that the UI consumes.
 * Completely independent of Room or Firebase annotations.
 */
data class Board(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val fen: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val userId: String? = null
)

// ─── Local Database Entity (Room) ─────────────────────────────────────────────
/**
 * The Room entity used for local persistence.
 */
@Entity(tableName = "saved_boards")
data class SavedBoardEntity(
    @PrimaryKey val id: String,
    val name: String,
    val fen: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean,
    val userId: String?
)

// ─── Remote Data Transfer Object (Firebase Firestore) ─────────────────────────
/**
 * The model stored in Firestore.
 * Using default values because Firestore requires an empty constructor.
 */
data class BoardDto(
    val id: String = "",
    val name: String = "",
    val fen: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val userId: String? = null
    // Notice isSynced is missing here because it's only a local tracking matter.
)

// ─── Mappers ─────────────────────────────────────────────────────────────────

fun SavedBoardEntity.toDomain() = Board(
    id = id,
    name = name,
    fen = fen,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = isSynced,
    userId = userId
)

fun Board.toEntity() = SavedBoardEntity(
    id = id,
    name = name,
    fen = fen,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = isSynced,
    userId = userId
)

fun Board.toDto() = BoardDto(
    id = id,
    name = name,
    fen = fen,
    createdAt = createdAt,
    updatedAt = updatedAt,
    userId = userId
)

fun BoardDto.toDomain() = Board(
    id = id,
    name = name,
    fen = fen,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = true, // If it comes from Firebase, it's synced by definition
    userId = userId
)
