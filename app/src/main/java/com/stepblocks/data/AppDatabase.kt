package com.stepblocks.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stepblocks.data.converters.Converters

@Database(
    entities = [
        TimeBlock::class,
        Template::class,
        DayAssignment::class,
        DailyProgress::class,
        BlockProgress::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun templateDao(): TemplateDao
    abstract fun timeBlockDao(): TimeBlockDao
    abstract fun dayAssignmentDao(): DayAssignmentDao
    abstract fun dailyProgressDao(): DailyProgressDao
    abstract fun blockProgressDao(): BlockProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "step_blocks_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
