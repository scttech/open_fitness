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

/**
 * Lets a profile show a custom avatar instead of the plain colored circle: either a bundled
 * preset image (identified by key) or a user-supplied photo (stored on-disk, path recorded here).
 * The two columns are mutually exclusive in practice, but both nullable since "no avatar set" is
 * the common case and needs no backfill.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `avatarPresetKey` TEXT")
        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `avatarFilePath` TEXT")
    }
}

/**
 * Adds the `badges` table recording which achievements each profile has earned. Existing profiles
 * start empty; [com.scttech.android.kotlin.openfitness.data.badge.BadgeAwarder] backfills from
 * their history on next launch.
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `badges` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `profileId` INTEGER NOT NULL,
                `badgeKey` TEXT NOT NULL,
                `earnedAtEpochMillis` INTEGER NOT NULL,
                FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_badges_profileId_badgeKey` ON `badges` (`profileId`, `badgeKey`)")
    }
}

/**
 * Lets each profile choose kg or lb for displaying and entering weights. Existing profiles default
 * to kilograms, matching how everything was shown before; stored values stay in kilograms.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `weightUnit` TEXT NOT NULL DEFAULT 'KG'")
    }
}

/**
 * Adds the calendar: user-scheduled events (one-off or recurring) plus the program days a user
 * has moved off the calendar. Program workout/test days themselves are projected from each
 * program's weekly schedule rather than stored.
 */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `scheduled_events` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `profileId` INTEGER NOT NULL,
                `kind` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `workoutId` INTEGER,
                `programId` INTEGER,
                `dateEpochDay` INTEGER NOT NULL,
                `startMinutes` INTEGER,
                `notes` TEXT NOT NULL,
                `reminderMinutesBefore` INTEGER,
                `repeatDays` TEXT NOT NULL,
                `repeatUntilEpochDay` INTEGER,
                `completedEpochDays` TEXT NOT NULL,
                `skippedEpochDays` TEXT NOT NULL,
                FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`workoutId`) REFERENCES `workouts`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY(`programId`) REFERENCES `programs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_scheduled_events_profileId` ON `scheduled_events` (`profileId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_scheduled_events_workoutId` ON `scheduled_events` (`workoutId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_scheduled_events_programId` ON `scheduled_events` (`programId`)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `program_day_skips` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `profileId` INTEGER NOT NULL,
                `programId` INTEGER NOT NULL,
                `dateEpochDay` INTEGER NOT NULL,
                FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`programId`) REFERENCES `programs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_program_day_skips_profileId` ON `program_day_skips` (`profileId`)")
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_program_day_skips_programId_dateEpochDay` " +
                "ON `program_day_skips` (`programId`, `dateEpochDay`)",
        )
    }
}

/** Adds the Message Center's short-lived copies of sent alerts. */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `messages` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `profileId` INTEGER NOT NULL,
                `dedupeKey` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `body` TEXT NOT NULL,
                `createdAtEpochMillis` INTEGER NOT NULL,
                `isRead` INTEGER NOT NULL,
                `destinationType` TEXT,
                `destinationId` INTEGER,
                FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_profileId` ON `messages` (`profileId`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_messages_dedupeKey` ON `messages` (`dedupeKey`)")
    }
}

/**
 * Lets each profile choose when countdown tick sounds start. Existing profiles move to 5 seconds,
 * down from the previous fixed 10.
 */
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `profiles` ADD COLUMN `timerCountdownSeconds` INTEGER NOT NULL DEFAULT 5")
    }
}

/** Lets a library exercise reference a clip in the bundled 3D animation file. Starter exercises are backfilled at launch. */
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `exercises` ADD COLUMN `animationName` TEXT")
    }
}
