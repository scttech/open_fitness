package com.scttech.android.kotlin.openfitness.domain.template

import com.scttech.android.kotlin.openfitness.domain.model.Exercise
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseCategory
import com.scttech.android.kotlin.openfitness.domain.model.ExerciseEquipment
import kotlinx.datetime.Instant

/** A reference library of common exercises with form notes, seeded on first launch. */
object StarterExercises {

    fun all(): List<Exercise> = listOf(
        Exercise(
            name = "Push-Up",
            category = ExerciseCategory.CHEST,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Hands slightly wider than shoulders, body in a straight line from head to heels. " +
                "Lower until your chest nearly touches the floor, elbows at about 45° from your torso, then press back up.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Pull-Up",
            category = ExerciseCategory.BACK,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Hands just outside shoulder width, palms facing away. Start from a dead hang and pull until " +
                "your chin clears the bar, leading with your chest, then lower under control to a full hang.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Bodyweight Squat",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Feet shoulder-width apart, toes slightly out. Sit back and down, keeping your chest up and " +
                "knees tracking over your toes, until thighs are at least parallel to the floor.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Air Squat",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Same movement as a bodyweight squat, typically performed for speed/reps in a circuit - " +
                "prioritize hitting full depth every rep over speed.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Back Squat",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Bar racked across the upper traps, feet shoulder-width. Brace your core, break at the hips " +
                "and knees together, and squat to at least parallel before driving back up through mid-foot.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Deadlift",
            category = ExerciseCategory.BACK,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Bar over mid-foot, shins near the bar, flat back, chest up. Drive through the floor with " +
                "your legs while keeping the bar close, locking out hips and knees together at the top.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Kettlebell Swing",
            category = ExerciseCategory.FULL_BODY,
            equipment = ExerciseEquipment.KETTLEBELL,
            formNotes = "Hinge from the hips, not the knees. Snap your hips forward explosively to send the bell to " +
                "chest height, letting the arms stay relaxed - it's a hip drive, not a shoulder lift.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Burpee",
            category = ExerciseCategory.FULL_BODY,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Drop to a plank, perform a push-up, jump the feet back to the hands, then explode upward " +
                "into a jump with arms overhead.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Mountain Climber",
            category = ExerciseCategory.CORE,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "From a high plank, drive knees alternately toward your chest at a brisk pace while keeping " +
                "your hips low and core braced.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Plank",
            category = ExerciseCategory.CORE,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Forearms and toes on the floor, body in a straight line from head to heels. Squeeze glutes " +
                "and brace your abs - don't let your hips sag or pike up.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Sit-Up",
            category = ExerciseCategory.CORE,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Knees bent, feet anchored or flat on the floor. Curl your torso up to vertical, then lower " +
                "with control back to the floor.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Row (Erg/Rower)",
            category = ExerciseCategory.CARDIO,
            equipment = ExerciseEquipment.CARDIO_MACHINE,
            formNotes = "Drive with the legs first, then swing the torso back, then pull the arms in; reverse the " +
                "order on the recovery - arms, then torso, then legs.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Farmer's Carry",
            category = ExerciseCategory.FULL_BODY,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Pick up a heavy weight in each hand, stand tall with shoulders back, and walk with short, " +
                "controlled steps without letting the weights swing.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Overhead Press",
            category = ExerciseCategory.SHOULDERS,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Bar at shoulder height, grip just outside shoulders. Brace your core and press straight up, " +
                "tucking your head through at the top to finish with the bar over your ears.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Dumbbell Bench Press",
            category = ExerciseCategory.CHEST,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Lie flat, dumbbells at chest height with elbows at about 45°. Press up until arms are " +
                "extended without locking out aggressively, then lower under control.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Bent-Over Row",
            category = ExerciseCategory.BACK,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Hinge to roughly 45°, flat back, bar hanging at arm's length. Pull the bar to your lower " +
                "ribs, squeezing your shoulder blades together, then lower with control.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Lunge",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Step forward into a long stride, lowering the back knee toward the floor while keeping the " +
                "front shin close to vertical, then push back to standing.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Dip",
            category = ExerciseCategory.ARMS,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Support yourself on parallel bars, lean slightly forward, and lower until your shoulders " +
                "drop just below your elbows, then press back to full lockout.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Jumping Jack",
            category = ExerciseCategory.CARDIO,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Jump feet out while raising arms overhead, then jump back to the starting position - keep " +
                "a light, quick rhythm.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Jump Rope",
            category = ExerciseCategory.CARDIO,
            equipment = ExerciseEquipment.OTHER,
            formNotes = "Small jumps just high enough to clear the rope, turning it mainly with the wrists rather " +
                "than the whole arm.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Stretching / Mobility Flow",
            category = ExerciseCategory.MOBILITY,
            equipment = ExerciseEquipment.OTHER,
            formNotes = "Move slowly through end-range positions for the joints you're about to train; hold each " +
                "position for a few relaxed breaths rather than bouncing.",
            createdAt = TEMPLATE_EPOCH,
        ),
    )

    private val TEMPLATE_EPOCH = Instant.fromEpochMilliseconds(0L)
}
