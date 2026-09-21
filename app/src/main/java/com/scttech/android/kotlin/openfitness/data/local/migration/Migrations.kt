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

/**
 * Program goals/results are inherently whole numbers (reps, seconds, meters) - `REAL` allowed
 * fractional values like "13.5 reps" to leak in from `trainingMax * percentage` arithmetic. Moves
 * `programs.goalTarget`/`lastTestResult` and `program_tests.result` to `INTEGER`.
 *
 * `programs.currentPrescription` (and the test-driven fields tied to it) is reset to null rather
 * than migrated: it's a JSON blob that may hold an old fractional `trainingMax`/`basedOnTestResult`,
 * which would fail to decode as the now-`Int` fields. Recording a new test regenerates it cleanly.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE `programs_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `profileId` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `exerciseId` INTEGER,
                `exerciseName` TEXT NOT NULL,
                `goalType` TEXT NOT NULL,
                `goalTarget` INTEGER NOT NULL,
                `config` TEXT NOT NULL,
                `currentPrescription` TEXT,
                `lastTestResult` INTEGER,
                `lastTestedAtEpochMillis` INTEGER,
                `nextTestDueAtEpochMillis` INTEGER,
                `isArchived` INTEGER NOT NULL,
                `createdAtEpochMillis` INTEGER NOT NULL,
                FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `programs_new`
                (`id`, `profileId`, `name`, `exerciseId`, `exerciseName`, `goalType`, `goalTarget`, `config`,
                 `currentPrescription`, `lastTestResult`, `lastTestedAtEpochMillis`, `nextTestDueAtEpochMillis`,
                 `isArchived`, `createdAtEpochMillis`)
            SELECT `id`, `profileId`, `name`, `exerciseId`, `exerciseName`, `goalType`, ROUND(`goalTarget`), `config`,
                   NULL, CASE WHEN `lastTestResult` IS NULL THEN NULL ELSE ROUND(`lastTestResult`) END, NULL, NULL,
                   `isArchived`, `createdAtEpochMillis`
            FROM `programs`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `programs`")
        db.execSQL("ALTER TABLE `programs_new` RENAME TO `programs`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_programs_profileId` ON `programs` (`profileId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_programs_exerciseId` ON `programs` (`exerciseId`)")

        db.execSQL(
            """
            CREATE TABLE `program_tests_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `programId` INTEGER NOT NULL,
                `profileId` INTEGER NOT NULL,
                `testedAtEpochMillis` INTEGER NOT NULL,
                `result` INTEGER NOT NULL,
                `notes` TEXT NOT NULL,
                FOREIGN KEY(`programId`) REFERENCES `programs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `program_tests_new` (`id`, `programId`, `profileId`, `testedAtEpochMillis`, `result`, `notes`)
            SELECT `id`, `programId`, `profileId`, `testedAtEpochMillis`, ROUND(`result`), `notes`
            FROM `program_tests`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `program_tests`")
        db.execSQL("ALTER TABLE `program_tests_new` RENAME TO `program_tests`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_program_tests_programId` ON `program_tests` (`programId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_program_tests_profileId` ON `program_tests` (`profileId`)")
    }
}

/**
 * Timer preferences (countdown sounds, work/rest colors) used to live in a single app-wide
 * DataStore, so changing them for one profile silently changed them for every profile sharing the
 * device. Moves them onto `profiles` itself, where they're naturally per-profile.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `timerSoundEnabled` INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `timerWorkColorArgb` INTEGER")
        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `timerRestColorArgb` INTEGER")
    }
}
