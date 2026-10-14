package com.scttech.android.kotlin.openfitness.domain.template

import com.scttech.android.kotlin.openfitness.domain.model.TemplateCategory
import com.scttech.android.kotlin.openfitness.domain.model.Workout
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutExercise
import com.scttech.android.kotlin.openfitness.domain.model.WorkoutStyleConfig
import com.scttech.android.kotlin.openfitness.domain.template.StarterTemplates.followAlongSequence
import kotlinx.datetime.Instant

/**
 * Starter workouts aimed at a specific goal or health concern (bone density, fat loss, balance...).
 * These are general fitness guidance, not medical advice.
 */
internal object StarterGoalTemplates {

    fun all(): List<Workout> = entries.map { it.second }

    fun categories(): Map<String, TemplateCategory> = entries.associate { (category, workout) -> workout.name to category }

    private val entries: List<Pair<TemplateCategory, Workout>> by lazy {
        boneHealth() + fatLoss() + muscleGain() + strength() + conditioning() + mobility() + backPosture() +
            balanceAging() + general()
    }

    private fun template(
        category: TemplateCategory,
        name: String,
        config: WorkoutStyleConfig,
        exercises: List<WorkoutExercise>,
        notes: String,
    ): Pair<TemplateCategory, Workout> = category to Workout(
        profileId = null,
        name = name,
        style = config.style,
        styleConfig = config,
        exercises = exercises,
        notes = notes,
        isTemplate = true,
        createdAt = TEMPLATE_EPOCH,
    )

    private val TEMPLATE_EPOCH = Instant.fromEpochMilliseconds(0L)

    private fun exercises(vararg names: String, reps: Int? = null): List<WorkoutExercise> =
        names.mapIndexed { index, name -> WorkoutExercise(order = index, name = name, targetReps = reps) }

    // ---- Bone health & menopause: heavy loading, impact and balance, which are what bone responds to ----
    private fun boneHealth() = listOf(
        template(
            TemplateCategory.BONE_HEALTH, "Bone Builder: Squat Ladder",
            WorkoutStyleConfig.StepLoading(startWeightKg = 20.0, stepWeightKg = 5.0, setCount = 5, repsPerSet = 5, deloadEverySessions = 4, restSeconds = 90),
            listOf(WorkoutExercise(order = 0, name = "Back Squat", targetReps = 5)),
            "Heavy-ish, low-rep squats load the hips and spine, which is the strongest signal for bone. 5 sets of 5, " +
                "adding 5kg each set, deload every 4th session. Start lighter if you are new to lifting.",
        ),
        template(
            TemplateCategory.BONE_HEALTH, "Bone Builder: Deadlift Ladder",
            WorkoutStyleConfig.StepLoading(startWeightKg = 30.0, stepWeightKg = 10.0, setCount = 4, repsPerSet = 5, deloadEverySessions = 4, restSeconds = 90),
            listOf(WorkoutExercise(order = 0, name = "Deadlift", targetReps = 5)),
            "Loads the hips, spine and grip. Keep your back flat and build weight gradually. Skip if you have " +
                "diagnosed osteoporosis of the spine unless your doctor or a trainer has cleared it.",
        ),
        template(
            TemplateCategory.BONE_HEALTH, "Bone Strong: Lower Body Follow Along",
            WorkoutStyleConfig.FollowAlong(defaultRestSeconds = 20),
            followAlongSequence(
                "Goblet Squat" to 40, "Rest" to 20,
                "Step-Up" to 40, "Rest" to 20,
                "Reverse Lunge" to 40, "Rest" to 20,
                "Glute Bridge" to 40, "Rest" to 20,
                "Calf Raise" to 40, "Rest" to 20,
                "Heel Drop" to 30, "Rest" to 20,
                "Farmer's Carry" to 40,
            ),
            "Hold a dumbbell or kettlebell for the squat, step-ups and lunges. Weight-bearing moves for the hips, " +
                "legs and spine. Repeat the circuit twice if you feel good.",
        ),
        template(
            TemplateCategory.BONE_HEALTH, "Bone Strong: Upper Body & Spine Follow Along",
            WorkoutStyleConfig.FollowAlong(defaultRestSeconds = 20),
            followAlongSequence(
                "Dumbbell Shoulder Press" to 40, "Rest" to 20,
                "Single-Arm Dumbbell Row" to 40, "Rest" to 20,
                "Incline Push-Up" to 40, "Rest" to 20,
                "Band Pull-Apart" to 40, "Rest" to 20,
                "Superman" to 30, "Rest" to 20,
                "Wall Angel" to 40, "Rest" to 20,
                "Chin Tuck" to 30,
            ),
            "Loads the wrists, arms and shoulders and strengthens the back muscles that support the spine. " +
                "Avoids crunches and loaded twisting, which are discouraged with low bone density.",
        ),
        template(
            TemplateCategory.BONE_HEALTH, "Gentle Impact Intervals",
            WorkoutStyleConfig.Tabata(workSeconds = 30, restSeconds = 30, roundsPerCycle = 3, cycles = 1, restBetweenCyclesSeconds = 60),
            exercises("Heel Drop", "Marching in Place", "Step-Up", "Jumping Jack"),
            "Short, low-to-moderate impact bursts - bones respond to quick forces. Swap Jumping Jacks for " +
                "Marching in Place if impact bothers your joints. 3 rounds of 4 moves.",
        ),
        template(
            TemplateCategory.BONE_HEALTH, "Menopause Strength Circuit",
            WorkoutStyleConfig.Emom(repGoal = 8, rounds = 4, restBetweenRoundsSeconds = 60),
            exercises("Goblet Squat", "Dumbbell Shoulder Press", "Romanian Deadlift", "Single-Arm Dumbbell Row"),
            "Strength training helps protect muscle, bone and metabolism through menopause. Pick a weight you can " +
                "do for 8 steady reps with 1-2 left in the tank. 4 rounds, 60s rest between rounds.",
        ),
        template(
            TemplateCategory.BONE_HEALTH, "Pelvic Floor & Core Basics",
            WorkoutStyleConfig.FollowAlong(defaultRestSeconds = 15),
            followAlongSequence(
                "Pelvic Floor Contraction (Kegel)" to 45, "Rest" to 15,
                "Glute Bridge" to 40, "Rest" to 15,
                "Dead Bug" to 40, "Rest" to 15,
                "Bird Dog" to 40, "Rest" to 15,
                "Cat-Cow" to 40, "Rest" to 15,
                "Box Breathing" to 60,
            ),
            "Gentle work for the pelvic floor and deep core, helpful through menopause, after childbirth, " +
                "or any time. Move slowly and breathe.",
        ),
    )

    // ---- Fat loss ----
    private fun fatLoss() = listOf(
        template(
            TemplateCategory.FAT_LOSS, "Metabolic Finisher Tabata",
            WorkoutStyleConfig.Tabata(workSeconds = 40, restSeconds = 20, roundsPerCycle = 5, cycles = 1, restBetweenCyclesSeconds = 60),
            exercises("Kettlebell Swing", "Burpee", "Mountain Climber", "High Knees"),
            "Four big moves, 40s on and 20s off, 5 rounds. Great at the end of a strength session or on its own.",
        ),
        template(
            TemplateCategory.FAT_LOSS, "Strength Circuit Density",
            WorkoutStyleConfig.Density(durationMinutes = 20, targetRounds = null),
            listOf(
                WorkoutExercise(order = 0, name = "Goblet Squat", targetReps = 10),
                WorkoutExercise(order = 1, name = "Push-Up", targetReps = 10),
                WorkoutExercise(order = 2, name = "Bent-Over Row", targetReps = 10),
                WorkoutExercise(order = 3, name = "Kettlebell Swing", targetReps = 15),
            ),
            "Keeps muscle on while you burn calories. As many quality rounds as possible in 20 minutes; try to beat " +
                "your round count next time.",
        ),
        template(
            TemplateCategory.FAT_LOSS, "Burn EMOM",
            WorkoutStyleConfig.Emom(repGoal = 8, rounds = 5, restBetweenRoundsSeconds = 45),
            exercises("Burpee", "Kettlebell Swing", "Reverse Lunge", "Push-Up"),
            "Finish 8 reps early each minute and rest what is left. 5 rounds; the rest shrinks as you fatigue, " +
                "which is the point.",
        ),
        template(
            TemplateCategory.FAT_LOSS, "Walk-Jog Fat Burner",
            WorkoutStyleConfig.FollowAlong(defaultRestSeconds = 15),
            followAlongSequence(
                "Brisk Walk" to 300,
                "Outdoor Run" to 60, "Brisk Walk" to 120,
                "Outdoor Run" to 60, "Brisk Walk" to 120,
                "Outdoor Run" to 60, "Brisk Walk" to 120,
                "Outdoor Run" to 60, "Brisk Walk" to 120,
                "Outdoor Run" to 60, "Brisk Walk" to 120,
                "Outdoor Run" to 60, "Brisk Walk" to 180,
            ),
            "About 27 minutes. A 5-minute warm-up walk, six 1-minute jogs, and walking recoveries. Jog at a " +
                "conversational-to-moderate effort.",
        ),
    )

    // ---- Muscle gain: volume with controlled reps ----
    private fun muscleGain() = listOf(
        template(
            TemplateCategory.MUSCLE_GAIN, "Upper Body Hypertrophy EMOM",
            WorkoutStyleConfig.Emom(repGoal = 10, rounds = 4, restBetweenRoundsSeconds = 60),
            exercises("Dumbbell Bench Press", "Bent-Over Row", "Dumbbell Shoulder Press", "Hammer Curl"),
            "Four paired-up pushing and pulling moves for 10 controlled reps. Use a weight that makes the last 2 " +
                "reps hard, and add weight when all 4 rounds feel clean.",
        ),
        template(
            TemplateCategory.MUSCLE_GAIN, "Lower Body Hypertrophy EMOM",
            WorkoutStyleConfig.Emom(repGoal = 10, rounds = 4, restBetweenRoundsSeconds = 75),
            exercises("Goblet Squat", "Romanian Deadlift", "Walking Lunge", "Calf Raise"),
            "Quads, hamstrings, glutes and calves. Lower slowly (about 3 seconds) for more muscle-building stimulus.",
        ),
        template(
            TemplateCategory.MUSCLE_GAIN, "Arms & Shoulders Density",
            WorkoutStyleConfig.Density(durationMinutes = 15, targetRounds = null),
            listOf(
                WorkoutExercise(order = 0, name = "Dumbbell Bicep Curl", targetReps = 10),
                WorkoutExercise(order = 1, name = "Overhead Tricep Extension", targetReps = 10),
                WorkoutExercise(order = 2, name = "Lateral Raise", targetReps = 12),
            ),
            "A pump finisher: cycle the three moves for as many rounds as you can in 15 minutes with light-to-moderate weights.",
        ),
        template(
            TemplateCategory.MUSCLE_GAIN, "Push-Up Volume Pyramid",
            WorkoutStyleConfig.Pyramid(direction = WorkoutStyleConfig.PyramidDirection.UP_DOWN, startReps = 2, stepReps = 2, peakReps = 12, restSeconds = 45),
            listOf(WorkoutExercise(order = 0, name = "Push-Up")),
            "2-4-6-8-10-12-10-8-6-4-2 reps, 72 push-ups in total, without needing a gym. Slow the lowering phase to make it harder.",
        ),
    )

    // ---- Strength ----
    private fun strength() = listOf(
        template(
            TemplateCategory.STRENGTH, "Bench Press Step-Loading",
            WorkoutStyleConfig.StepLoading(startWeightKg = 30.0, stepWeightKg = 10.0, setCount = 5, repsPerSet = 5, deloadEverySessions = 4, restSeconds = 120),
            listOf(WorkoutExercise(order = 0, name = "Barbell Bench Press", targetReps = 5)),
            "5 sets of 5, adding 10kg each set. Use a spotter or safeties, and deload every 4th session.",
        ),
        template(
            TemplateCategory.STRENGTH, "Overhead Press Step-Loading",
            WorkoutStyleConfig.StepLoading(startWeightKg = 20.0, stepWeightKg = 5.0, setCount = 5, repsPerSet = 5, deloadEverySessions = 4, restSeconds = 120),
            listOf(WorkoutExercise(order = 0, name = "Overhead Press", targetReps = 5)),
            "5 sets of 5, adding 5kg each set. Squeeze your glutes and keep your ribs down so you press, not lean back.",
        ),
        template(
            TemplateCategory.STRENGTH, "Pull-Up Ladder",
            WorkoutStyleConfig.Pyramid(direction = WorkoutStyleConfig.PyramidDirection.UP_DOWN, startReps = 1, stepReps = 1, peakReps = 5, restSeconds = 60),
            listOf(WorkoutExercise(order = 0, name = "Pull-Up")),
            "1-2-3-4-5-4-3-2-1 reps, 25 in total. Swap in Chin-Ups or Inverted Rows if you can't do full pull-ups yet.",
        ),
    )

    // ---- Cardio & conditioning ----
    private fun conditioning() = listOf(
        template(
            TemplateCategory.CONDITIONING, "Jump Rope Tabata",
            WorkoutStyleConfig.Tabata(workSeconds = 20, restSeconds = 10, roundsPerCycle = 8, cycles = 2, restBetweenCyclesSeconds = 60),
            listOf(WorkoutExercise(order = 0, name = "Jump Rope")),
            "Two Tabata blocks of 8 rounds with a minute between. About 9 minutes of skipping.",
        ),
        template(
            TemplateCategory.CONDITIONING, "Assault Bike Sprints",
            WorkoutStyleConfig.Tabata(workSeconds = 30, restSeconds = 60, roundsPerCycle = 6, cycles = 1, restBetweenCyclesSeconds = 60),
            listOf(WorkoutExercise(order = 0, name = "Assault Bike")),
            "6 all-out 30-second sprints with a full minute of easy spinning between. Builds top-end conditioning.",
        ),
        template(
            TemplateCategory.CONDITIONING, "Walk-Jog Beginner Intervals",
            WorkoutStyleConfig.FollowAlong(defaultRestSeconds = 15),
            followAlongSequence(
                "Brisk Walk" to 180,
                "Outdoor Run" to 30, "Brisk Walk" to 90,
                "Outdoor Run" to 30, "Brisk Walk" to 90,
                "Outdoor Run" to 30, "Brisk Walk" to 90,
                "Outdoor Run" to 30, "Brisk Walk" to 90,
                "Outdoor Run" to 30, "Brisk Walk" to 120,
            ),
            "About 15 minutes for newer exercisers or anyone returning after a break. Jog slowly enough to hold a conversation.",
        ),
    )

    // ---- Mobility & recovery ----
    private fun mobility() = listOf(
        template(
            TemplateCategory.MOBILITY, "Morning Mobility Flow",
            WorkoutStyleConfig.FollowAlong(defaultRestSeconds = 10),
            followAlongSequence(
                "Cat-Cow" to 45,
                "World's Greatest Stretch" to 60,
                "Hip Circles" to 40,
                "Thoracic Spine Rotation" to 45,
                "90/90 Hip Stretch" to 60,
                "Band Shoulder Dislocate" to 40,
                "Neck and Shoulder Rolls" to 30,
            ),
            "About 6 minutes to loosen the spine, hips and shoulders before the day or a workout. Move slowly, no pain.",
        ),
        template(
            TemplateCategory.MOBILITY, "Post-Workout Cooldown",
            WorkoutStyleConfig.FollowAlong(defaultRestSeconds = 10),
            followAlongSequence(
                "Foam Rolling" to 90,
                "Child's Pose" to 45,
                "Hamstring Stretch" to 40, "Hamstring Stretch" to 40,
                "Hip Flexor Stretch" to 40, "Hip Flexor Stretch" to 40,
                "Doorway Chest Stretch" to 40,
                "Box Breathing" to 60,
            ),
            "Let your heart rate settle and stretch the muscles you just worked. The paired stretches are the left and right sides.",
        ),
        template(
            TemplateCategory.MOBILITY, "Evening Wind-Down",
            WorkoutStyleConfig.FollowAlong(defaultRestSeconds = 10),
            followAlongSequence(
                "Child's Pose" to 60,
                "Cat-Cow" to 45,
                "Thoracic Spine Rotation" to 45,
                "90/90 Hip Stretch" to 60,
                "Hamstring Stretch" to 40, "Hamstring Stretch" to 40,
                "Box Breathing" to 90,
            ),
            "A calming sequence to release the day's tension and ease into sleep.",
        ),
    )

    // ---- Back, posture & core ----
    private fun backPosture() = listOf(
        template(
            TemplateCategory.BACK_POSTURE, "Desk Break Reset",
            WorkoutStyleConfig.FollowAlong(defaultRestSeconds = 10),
            followAlongSequence(
                "Neck and Shoulder Rolls" to 30,
                "Chin Tuck" to 30,
                "Wall Angel" to 40,
                "Doorway Chest Stretch" to 30,
                "Standing Torso Twist" to 30,
                "Hip Flexor Stretch" to 30, "Hip Flexor Stretch" to 30,
                "Calf Raise" to 30,
            ),
            "A quick 5-minute reset for anyone who sits most of the day. Do it once or twice between long stretches of sitting.",
        ),
        template(
            TemplateCategory.BACK_POSTURE, "Posture Fix Follow Along",
            WorkoutStyleConfig.FollowAlong(defaultRestSeconds = 15),
            followAlongSequence(
                "Band Pull-Apart" to 40, "Rest" to 15,
                "Face Pull" to 40, "Rest" to 15,
                "Rear Delt Fly" to 40, "Rest" to 15,
                "Wall Angel" to 40, "Rest" to 15,
                "Superman" to 30, "Rest" to 15,
                "Chin Tuck" to 30,
            ),
            "Strengthens the upper back and rear shoulders to offset rounded, forward-slumped posture. Needs a light band and dumbbells.",
        ),
        template(
            TemplateCategory.BACK_POSTURE, "Healthy Back Core Stability",
            WorkoutStyleConfig.FollowAlong(defaultRestSeconds = 20),
            followAlongSequence(
                "Cat-Cow" to 40, "Rest" to 15,
                "Bird Dog" to 40, "Rest" to 20,
                "Dead Bug" to 40, "Rest" to 20,
                "Side Plank" to 30, "Rest" to 20,
                "Side Plank" to 30, "Rest" to 20,
                "Glute Bridge" to 40, "Rest" to 20,
                "Pallof Press" to 40,
            ),
            "Builds a stiff, stable trunk without repeatedly bending the spine. Stop if anything causes pain.",
        ),
    )

    // ---- Balance & healthy aging ----
    private fun balanceAging() = listOf(
        template(
            TemplateCategory.BALANCE_AGING, "Fall-Proof Balance Flow",
            WorkoutStyleConfig.FollowAlong(defaultRestSeconds = 15),
            followAlongSequence(
                "Single-Leg Balance" to 30, "Single-Leg Balance" to 30, "Rest" to 15,
                "Tandem Walk" to 40, "Rest" to 15,
                "Sit-to-Stand" to 40, "Rest" to 15,
                "Standing Hip Abduction" to 30, "Standing Hip Abduction" to 30, "Rest" to 15,
                "Calf Raise" to 40, "Rest" to 15,
                "Step-Up" to 40,
            ),
            "Do this near a wall or counter. The paired moves are the left and right sides. Practice a few times a week to improve balance and leg strength.",
        ),
        template(
            TemplateCategory.BALANCE_AGING, "Healthy Aging Strength Circuit",
            WorkoutStyleConfig.Emom(repGoal = 8, rounds = 3, restBetweenRoundsSeconds = 60),
            exercises("Sit-to-Stand", "Wall Push-Up", "Glute Bridge", "Step-Up"),
            "Everyday-strength basics to protect muscle and independence. Progress from wall to incline push-ups when it feels easy.",
        ),
        template(
            TemplateCategory.BALANCE_AGING, "Low-Impact Cardio Circuit",
            WorkoutStyleConfig.FollowAlong(defaultRestSeconds = 20),
            followAlongSequence(
                "Marching in Place" to 60, "Rest" to 15,
                "Step-Up" to 45, "Rest" to 15,
                "Bodyweight Squat" to 45, "Rest" to 15,
                "Standing Hip Abduction" to 30, "Standing Hip Abduction" to 30, "Rest" to 15,
                "Marching in Place" to 60, "Rest" to 15,
                "Wall Push-Up" to 40,
            ),
            "Raises your heart rate without jumping, easy on the knees, hips and back. Ideal for joint issues or a gentle return to exercise.",
        ),
    )

    // ---- General fitness ----
    private fun general() = listOf(
        template(
            TemplateCategory.GENERAL, "Beginner Full-Body Starter",
            WorkoutStyleConfig.FollowAlong(defaultRestSeconds = 30),
            followAlongSequence(
                "Marching in Place" to 60, "Rest" to 20,
                "Bodyweight Squat" to 30, "Rest" to 30,
                "Incline Push-Up" to 30, "Rest" to 30,
                "Glute Bridge" to 30, "Rest" to 30,
                "Bird Dog" to 30, "Rest" to 30,
                "Brisk Walk" to 60,
            ),
            "A gentle first workout: short efforts, long rests, and no equipment. Do it twice through as you get fitter.",
        ),
    )
}
