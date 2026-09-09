package com.scttech.android.kotlin.openfitness.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds the Exercise library and Program feature tables. Purely additive - no existing table changes. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `exercises` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `equipment` TEXT NOT NULL,
                `formNotes` TEXT NOT NULL,
                `isCustom` INTEGER NOT NULL,
                `createdAtEpochMillis` INTEGER NOT NULL
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `programs` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `profileId` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `exerciseId` INTEGER,
                `exerciseName` TEXT NOT NULL,
                `goalType` TEXT NOT NULL,
                `goalTarget` REAL NOT NULL,
                `config` TEXT NOT NULL,
                `currentPrescription` TEXT,
                `lastTestResult` REAL,
                `lastTestedAtEpochMillis` INTEGER,
                `nextTestDueAtEpochMillis` INTEGER,
                `isArchived` INTEGER NOT NULL,
                `createdAtEpochMillis` INTEGER NOT NULL,
                FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_programs_profileId` ON `programs` (`profileId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_programs_exerciseId` ON `programs` (`exerciseId`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `program_tests` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `programId` INTEGER NOT NULL,
                `profileId` INTEGER NOT NULL,
                `testedAtEpochMillis` INTEGER NOT NULL,
                `result` REAL NOT NULL,
                `notes` TEXT NOT NULL,
                FOREIGN KEY(`programId`) REFERENCES `programs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_program_tests_programId` ON `program_tests` (`programId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_program_tests_profileId` ON `program_tests` (`profileId`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `program_sessions` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `profileId` INTEGER NOT NULL,
                `programId` INTEGER,
                `programName` TEXT NOT NULL,
                `startedAtEpochMillis` INTEGER NOT NULL,
                `completedAtEpochMillis` INTEGER,
                `loggedSets` TEXT NOT NULL,
                `notes` TEXT NOT NULL,
                FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`programId`) REFERENCES `programs`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_program_sessions_profileId` ON `program_sessions` (`profileId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_program_sessions_programId` ON `program_sessions` (`programId`)")
    }
}
