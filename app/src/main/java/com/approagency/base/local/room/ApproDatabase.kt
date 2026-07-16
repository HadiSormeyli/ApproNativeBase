package com.approagency.base.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.approagency.base.local.room.dao.SessionDao
import com.approagency.base.local.room.entity.SessionEntity

@Database(
    entities = [
        SessionEntity::class
    ],
    exportSchema = false,
    version = 1
)
abstract class ApproDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}