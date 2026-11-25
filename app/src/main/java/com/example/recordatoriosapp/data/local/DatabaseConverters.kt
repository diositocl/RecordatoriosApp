package com.example.recordatoriosapp.data.local

import androidx.room.TypeConverter

// Convierte el enum SyncStatus a texto para guardarlo en SQLite
class DatabaseConverters {

    @TypeConverter
    fun syncStatusToString(status: SyncStatus): String = status.name

    @TypeConverter
    fun stringToSyncStatus(value: String): SyncStatus {
        return runCatching { SyncStatus.valueOf(value) }
            .getOrDefault(SyncStatus.PENDING)
    }
}