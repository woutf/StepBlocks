package com.stepblocks.wear.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CachedStepEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao
}
