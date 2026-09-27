package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `conversations` (
                `conversationId` TEXT NOT NULL,
                `productId` INTEGER NOT NULL,
                `productName` TEXT NOT NULL,
                `buyerId` TEXT NOT NULL,
                `buyerName` TEXT NOT NULL,
                `artisanName` TEXT NOT NULL,
                `currentStatus` TEXT NOT NULL DEFAULT 'ENQUIRING',
                `agreedQuantity` INTEGER NOT NULL DEFAULT 0,
                `agreedUnitPrice` REAL NOT NULL DEFAULT 0.0,
                `buyerConfirmed` INTEGER NOT NULL DEFAULT 0,
                `sellerConfirmed` INTEGER NOT NULL DEFAULT 0,
                `lastMessageText` TEXT NOT NULL DEFAULT '',
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`conversationId`)
            )
        """.trimIndent())
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `messages` (
                `messageId` TEXT NOT NULL,
                `conversationId` TEXT NOT NULL,
                `clientMessageId` TEXT NOT NULL,
                `senderId` TEXT NOT NULL,
                `senderType` TEXT NOT NULL,
                `text` TEXT NOT NULL,
                `language` TEXT NOT NULL DEFAULT 'hi',
                `messageType` TEXT NOT NULL DEFAULT 'TEXT',
                `extractedQuantity` INTEGER,
                `extractedPrice` REAL,
                `status` TEXT NOT NULL DEFAULT 'SENT',
                `createdAt` INTEGER NOT NULL,
                PRIMARY KEY(`messageId`)
            )
        """.trimIndent())
    }
}

@Database(
    entities = [
        ProductEntity::class,
        ProductDraftEntity::class,
        SyncOperationEntity::class,
        ConversationEntity::class,
        MessageEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun draftDao(): DraftDao
    abstract fun syncDao(): SyncDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "artisan_ai_database"
                )
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

