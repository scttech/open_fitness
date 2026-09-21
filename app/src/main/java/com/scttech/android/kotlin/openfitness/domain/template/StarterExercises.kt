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
            name = "Mace 360 Swing",
            category = ExerciseCategory.FULL_BODY,
            equipment = ExerciseEquipment.MACE,
            formNotes = "Grip near the base of the handle, arms extended. Circle the mace around your head and " +
                "behind your back in one continuous, controlled loop, switching direction each rep and keeping your " +
                "core braced so your lower back doesn't arch.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Mace 10-to-2 Swing",
            category = ExerciseCategory.SHOULDERS,
            equipment = ExerciseEquipment.MACE,
            formNotes = "Start with the mace overhead like a clock at 12. Swing it down and across your body to the " +
                "\"10\" or \"2\" position, letting the head of the mace lead, then reverse back overhead - keep the " +
                "movement smooth rather than jerky.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Mace Squat",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.MACE,
            formNotes = "Hold the mace by the handle at chest height, head pointing up. Squat to at least parallel " +
                "keeping the mace still and your torso upright - the offset weight will challenge your balance and " +
                "core more than a standard squat.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Mace Front Raise (Around the World)",
            category = ExerciseCategory.SHOULDERS,
            equipment = ExerciseEquipment.MACE,
            formNotes = "Hold the mace at both ends in front of you. Trace a slow circle with the head of the mace " +
                "out to one side, overhead, and around to the other side, keeping your arms just short of locked out " +
                "and your ribs down throughout.",
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

        // ---- Chest ----
        Exercise(
            name = "Incline Push-Up",
            category = ExerciseCategory.CHEST,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Hands on a bench or box, body straight from head to heels. Easier variant of a push-up - " +
                "the higher the surface, the less bodyweight you're moving.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Decline Push-Up",
            category = ExerciseCategory.CHEST,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Feet elevated on a bench or box, hands on the floor. Harder variant that shifts more load " +
                "to the upper chest and shoulders.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Diamond Push-Up",
            category = ExerciseCategory.CHEST,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Hands together under your chest, thumbs and index fingers touching to form a diamond. " +
                "Keep elbows close to your body as you lower - this emphasizes triceps and inner chest.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Barbell Bench Press",
            category = ExerciseCategory.CHEST,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Lie on a bench, grip just outside shoulder width, feet planted. Lower the bar to your " +
                "mid-chest with control, then press up and slightly back until arms are locked out.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Incline Barbell Bench Press",
            category = ExerciseCategory.CHEST,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Same setup as a flat bench press on a 30-45° incline bench. Lower the bar to your upper " +
                "chest to emphasize the front of the shoulders and upper pecs.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Incline Dumbbell Press",
            category = ExerciseCategory.CHEST,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "On an incline bench, press dumbbells up and slightly in over your upper chest, then lower " +
                "until your elbows are just below your torso.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Dumbbell Fly",
            category = ExerciseCategory.CHEST,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Lie flat with dumbbells pressed above your chest, a soft bend in the elbows. Lower the " +
                "weights out to your sides in a wide arc until you feel a stretch, then bring them back together.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Machine Chest Press",
            category = ExerciseCategory.CHEST,
            equipment = ExerciseEquipment.MACHINE,
            formNotes = "Adjust the seat so handles line up with mid-chest. Press forward until arms extend, then " +
                "return under control without letting the weight stack slam.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Pec Deck Fly",
            category = ExerciseCategory.CHEST,
            equipment = ExerciseEquipment.MACHINE,
            formNotes = "Seated with elbows on the pads at chest height. Squeeze your arms together in front of " +
                "your chest, then let them open back out with control.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Cable Crossover",
            category = ExerciseCategory.CHEST,
            equipment = ExerciseEquipment.MACHINE,
            formNotes = "Stand between two high cable pulleys, a slight forward lean. Pull the handles down and " +
                "together in front of your hips in a wide arc, squeezing your chest at the bottom.",
            createdAt = TEMPLATE_EPOCH,
        ),

        // ---- Back ----
        Exercise(
            name = "Chin-Up",
            category = ExerciseCategory.BACK,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Palms facing you, hands about shoulder width. Pull until your chin clears the bar, " +
                "emphasizing the biceps more than a standard pull-up, then lower to a full hang.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Romanian Deadlift",
            category = ExerciseCategory.BACK,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Start standing with the bar at hip height. Push your hips back and lower the bar along " +
                "your legs with a soft knee bend until you feel a hamstring stretch, then drive hips forward to stand.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Sumo Deadlift",
            category = ExerciseCategory.BACK,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Wide stance with toes pointed out, hands inside your knees on the bar. Drive through the " +
                "floor and push your knees out as you stand, keeping your torso more upright than a conventional pull.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Pendlay Row",
            category = ExerciseCategory.BACK,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Bar starts on the floor each rep, torso bent parallel to the ground. Pull explosively to " +
                "your lower ribs, then let it return fully to the floor before the next rep.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Single-Arm Dumbbell Row",
            category = ExerciseCategory.BACK,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "One knee and hand on a bench for support, flat back. Pull the dumbbell to your hip, " +
                "leading with your elbow, then lower under control without twisting your torso.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Seated Cable Row",
            category = ExerciseCategory.BACK,
            equipment = ExerciseEquipment.MACHINE,
            formNotes = "Feet braced, slight lean back from the hips. Pull the handle to your torso while keeping " +
                "your back flat, then let your arms extend fully on the return without rounding forward.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Lat Pulldown",
            category = ExerciseCategory.BACK,
            equipment = ExerciseEquipment.MACHINE,
            formNotes = "Grip slightly wider than shoulders, slight lean back. Pull the bar to your upper chest, " +
                "driving your elbows down and back, then let it rise under control to full arm extension.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Inverted Row",
            category = ExerciseCategory.BACK,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Hang under a bar at chest height, body straight from head to heels. Pull your chest to " +
                "the bar, then lower with control - raise your feet or lower the bar to make it harder.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "T-Bar Row",
            category = ExerciseCategory.BACK,
            equipment = ExerciseEquipment.MACHINE,
            formNotes = "Straddle the bar, torso bent forward with a flat back. Pull the handle to your chest, " +
                "squeezing your shoulder blades together, then lower under control.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Good Morning",
            category = ExerciseCategory.BACK,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Bar on your upper back like a squat, soft knee bend. Hinge forward at the hips until your " +
                "torso is near parallel to the floor, then reverse the motion by driving your hips forward.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Superman",
            category = ExerciseCategory.BACK,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Lie face down, arms extended overhead. Simultaneously lift your arms, chest, and legs off " +
                "the floor, squeezing your lower back and glutes, then lower with control.",
            createdAt = TEMPLATE_EPOCH,
        ),

        // ---- Shoulders ----
        Exercise(
            name = "Dumbbell Shoulder Press",
            category = ExerciseCategory.SHOULDERS,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Seated or standing, dumbbells at shoulder height, palms forward. Press overhead until arms " +
                "are extended without flaring your ribs, then lower under control.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Arnold Press",
            category = ExerciseCategory.SHOULDERS,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Start with dumbbells in front of your shoulders, palms facing you. Press up while rotating " +
                "your palms to face forward, then reverse the rotation on the way down.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Lateral Raise",
            category = ExerciseCategory.SHOULDERS,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Dumbbells at your sides, a slight bend in the elbows. Raise your arms out to shoulder " +
                "height, leading with your elbows, then lower slowly rather than dropping the weight.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Front Raise",
            category = ExerciseCategory.SHOULDERS,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Dumbbells in front of your thighs. Raise one or both arms straight out to shoulder height, " +
                "then lower with control without swinging your torso for momentum.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Rear Delt Fly",
            category = ExerciseCategory.SHOULDERS,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Hinge forward at the hips, dumbbells hanging below your chest. Raise your arms out to the " +
                "sides, squeezing your shoulder blades together, then lower under control.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Upright Row",
            category = ExerciseCategory.SHOULDERS,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Grip just inside shoulder width. Pull the bar straight up along your body to about chest " +
                "height, leading with your elbows, then lower under control.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Face Pull",
            category = ExerciseCategory.SHOULDERS,
            equipment = ExerciseEquipment.BAND,
            formNotes = "Anchor a band at face height. Pull it toward your face, splitting your hands apart and " +
                "driving your elbows back, to target the rear delts and upper back.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Dumbbell Shrug",
            category = ExerciseCategory.SHOULDERS,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Dumbbells at your sides, arms straight. Elevate your shoulders straight up toward your " +
                "ears, hold briefly, then lower with control - avoid rolling your shoulders.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Pike Push-Up",
            category = ExerciseCategory.SHOULDERS,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Hips high in an inverted-V position, hands shoulder width. Lower your head toward the " +
                "floor between your hands, then press back up - a step toward a handstand push-up.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Handstand Push-Up",
            category = ExerciseCategory.SHOULDERS,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Kick up into a handstand against a wall. Lower your head to the floor with control, then " +
                "press back up to full lockout - regress to a pike push-up if this is too advanced.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Band Pull-Apart",
            category = ExerciseCategory.SHOULDERS,
            equipment = ExerciseEquipment.BAND,
            formNotes = "Hold a band with arms extended in front of you at shoulder height. Pull it apart by " +
                "driving your arms out to the sides, squeezing your shoulder blades, then return with control.",
            createdAt = TEMPLATE_EPOCH,
        ),

        // ---- Arms ----
        Exercise(
            name = "Barbell Curl",
            category = ExerciseCategory.ARMS,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Grip shoulder width, elbows pinned to your sides. Curl the bar up without swinging your " +
                "torso, then lower under control to full arm extension.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Dumbbell Bicep Curl",
            category = ExerciseCategory.ARMS,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Dumbbells at your sides, palms forward. Curl one or both arms up while keeping your " +
                "elbows still, then lower slowly rather than letting the weight drop.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Hammer Curl",
            category = ExerciseCategory.ARMS,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Same as a dumbbell curl, but with palms facing each other throughout. Targets the " +
                "forearm and outer bicep more than a standard curl.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Concentration Curl",
            category = ExerciseCategory.ARMS,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Seated, elbow braced against your inner thigh. Curl the dumbbell up with strict form, " +
                "isolating the bicep, then lower with control.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Preacher Curl",
            category = ExerciseCategory.ARMS,
            equipment = ExerciseEquipment.MACHINE,
            formNotes = "Arms braced against the preacher pad, upper arms fixed. Curl the weight up through a full " +
                "range of motion, then lower under control without letting your elbows lift off the pad.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Tricep Pushdown",
            category = ExerciseCategory.ARMS,
            equipment = ExerciseEquipment.MACHINE,
            formNotes = "Elbows pinned to your sides at a high cable pulley. Push the bar or rope down to full " +
                "extension, then let it rise under control without letting your elbows drift forward.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Skull Crusher",
            category = ExerciseCategory.ARMS,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Lying on a bench, bar held above your forehead with elbows pointed at the ceiling. Lower " +
                "the bar toward your forehead by bending only the elbows, then extend back to lockout.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Overhead Tricep Extension",
            category = ExerciseCategory.ARMS,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Hold one dumbbell overhead with both hands, elbows pointed forward. Lower it behind your " +
                "head by bending the elbows, then extend back to full lockout.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Close-Grip Bench Press",
            category = ExerciseCategory.ARMS,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Grip just inside shoulder width, elbows tucked close to your body. Lower the bar to your " +
                "lower chest and press back up - emphasizes triceps more than a standard bench press.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Band Curl",
            category = ExerciseCategory.ARMS,
            equipment = ExerciseEquipment.BAND,
            formNotes = "Stand on the middle of a band, elbows pinned to your sides. Curl the handles up under " +
                "tension, then lower with control back to full arm extension.",
            createdAt = TEMPLATE_EPOCH,
        ),

        // ---- Legs ----
        Exercise(
            name = "Front Squat",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Bar racked across the front of your shoulders, elbows high. Squat to at least parallel " +
                "while keeping your torso upright, then drive back up through mid-foot.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Goblet Squat",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Hold a dumbbell vertically at chest height with both hands. Squat between your knees to " +
                "full depth, keeping your chest up, then drive back to standing.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Bulgarian Split Squat",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Rear foot elevated on a bench, dumbbells at your sides. Lower straight down until your " +
                "back knee nearly touches the floor, then drive up through your front heel.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Walking Lunge",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Dumbbells at your sides. Step forward into a lunge, then bring your back foot through to " +
                "step directly into the next lunge, alternating legs as you travel.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Reverse Lunge",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Step backward into a lunge instead of forward, lowering your back knee toward the floor. " +
                "Easier on the front knee than a forward lunge - push back to standing through your front heel.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Step-Up",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "Dumbbells at your sides, one foot on a bench or box. Drive through that foot to stand " +
                "fully on top, then lower back down with control - avoid pushing off the trailing leg.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Leg Press",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.MACHINE,
            formNotes = "Feet shoulder width on the platform. Lower the sled until your knees reach about 90°, " +
                "then press through your heels back to just short of lockout.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Leg Extension",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.MACHINE,
            formNotes = "Seated with the pad against your shins. Extend your legs to full lockout, squeezing your " +
                "quads, then lower under control back to the start.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Leg Curl",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.MACHINE,
            formNotes = "Lying or seated with the pad against your ankles. Curl your heels toward your glutes, " +
                "then extend back under control without letting the weight stack slam.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Hip Thrust",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Upper back on a bench, bar across your hips. Drive through your heels to raise your hips " +
                "until your torso is parallel to the floor, squeezing your glutes at the top.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Glute Bridge",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Lying on your back, knees bent, feet flat. Drive through your heels to lift your hips, " +
                "squeezing your glutes at the top, then lower with control.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Calf Raise",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Stand tall, feet hip width. Rise onto the balls of your feet as high as you can, pause, " +
                "then lower with control back to the floor.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Box Jump",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Stand a short step from a box, dip your hips, then explode up and land softly on top " +
                "with knees slightly bent - step back down rather than jumping down.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Wall Sit",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Back flat against a wall, knees at about 90°, as if sitting in an invisible chair. Hold " +
                "the position, keeping your weight in your heels, for time.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Pistol Squat",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Balance on one leg, the other extended forward. Squat down to full depth on the standing " +
                "leg while keeping the other leg off the floor, then drive back up.",
            createdAt = TEMPLATE_EPOCH,
        ),

        // ---- Core ----
        Exercise(
            name = "Crunch",
            category = ExerciseCategory.CORE,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Knees bent, hands lightly behind your head. Curl your shoulder blades off the floor using " +
                "your abs, then lower with control - don't pull on your neck.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Bicycle Crunch",
            category = ExerciseCategory.CORE,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Lying on your back, hands behind your head. Bring one elbow to the opposite knee while " +
                "extending the other leg, alternating sides in a smooth pedaling motion.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Russian Twist",
            category = ExerciseCategory.CORE,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Seated, torso leaned back slightly, feet lifted or planted. Rotate your torso side to " +
                "side, tapping the floor beside your hips, keeping your chest up throughout.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Leg Raise",
            category = ExerciseCategory.CORE,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Lying on your back, hands at your sides. Raise your straight legs to vertical using your " +
                "lower abs, then lower them with control without letting your lower back arch off the floor.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Hanging Leg Raise",
            category = ExerciseCategory.CORE,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Hang from a pull-up bar. Raise your legs (straight or bent at the knees) toward your " +
                "chest using your abs, then lower with control without swinging.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Side Plank",
            category = ExerciseCategory.CORE,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Balance on one forearm and the outside edge of your foot, body in a straight line. Hold " +
                "without letting your hips sag, then switch sides.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "V-Up",
            category = ExerciseCategory.CORE,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Lying flat, arms extended overhead. Simultaneously raise your legs and torso to meet in a " +
                "V shape, reaching your hands toward your toes, then lower with control.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Dead Bug",
            category = ExerciseCategory.CORE,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Lying on your back, arms up and knees bent at 90°. Slowly extend one arm and the opposite " +
                "leg toward the floor while keeping your lower back pressed down, then return and switch sides.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Ab Wheel Rollout",
            category = ExerciseCategory.CORE,
            equipment = ExerciseEquipment.OTHER,
            formNotes = "Kneeling, hands on the ab wheel. Roll forward as far as you can control while keeping " +
                "your core braced and back flat, then pull back to the start.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Flutter Kicks",
            category = ExerciseCategory.CORE,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Lying on your back, legs straight and hovering just off the floor. Kick your legs in a " +
                "small, rapid up-and-down alternating rhythm while keeping your lower back pressed down.",
            createdAt = TEMPLATE_EPOCH,
        ),

        // ---- Full Body ----
        Exercise(
            name = "Clean and Press",
            category = ExerciseCategory.FULL_BODY,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Pull the bar from the floor to your shoulders in one explosive motion, then press it " +
                "overhead to lockout - reset and repeat for reps rather than staying racked.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Thruster",
            category = ExerciseCategory.FULL_BODY,
            equipment = ExerciseEquipment.BARBELL,
            formNotes = "Bar racked at your shoulders. Squat to full depth, then use the upward drive to press the " +
                "bar overhead in one continuous motion.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Turkish Get-Up",
            category = ExerciseCategory.FULL_BODY,
            equipment = ExerciseEquipment.KETTLEBELL,
            formNotes = "Lying down with a kettlebell pressed overhead in one arm. Work through the get-up " +
                "sequence to standing while keeping the arm locked out overhead throughout, then reverse it back down.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Kettlebell Clean",
            category = ExerciseCategory.FULL_BODY,
            equipment = ExerciseEquipment.KETTLEBELL,
            formNotes = "Hike the bell back between your legs, then drive your hips forward to pull it up close to " +
                "your body, rotating your hand through so it racks softly on your forearm.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Kettlebell Snatch",
            category = ExerciseCategory.FULL_BODY,
            equipment = ExerciseEquipment.KETTLEBELL,
            formNotes = "Hike the bell back, then drive your hips forward explosively to pull it in one motion " +
                "straight overhead, punching your hand through at the top to lock it out.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Man Maker",
            category = ExerciseCategory.FULL_BODY,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "From a push-up position with hands on dumbbells: row each arm, do a push-up, jump feet to " +
                "hands, then clean and press both dumbbells overhead to finish the rep.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Wall Ball",
            category = ExerciseCategory.FULL_BODY,
            equipment = ExerciseEquipment.OTHER,
            formNotes = "Hold a medicine ball at chest height. Squat to depth, then drive up and throw the ball to " +
                "a target on the wall, catching it on the way back down into the next squat.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Battle Ropes",
            category = ExerciseCategory.FULL_BODY,
            equipment = ExerciseEquipment.OTHER,
            formNotes = "Anchor a heavy rope, one end in each hand, in a quarter-squat stance. Whip the ropes in " +
                "alternating or double waves, keeping your core braced throughout.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Devil's Press",
            category = ExerciseCategory.FULL_BODY,
            equipment = ExerciseEquipment.DUMBBELL,
            formNotes = "From standing, place both dumbbells down and burpee out and back over them, then swing " +
                "them up in one motion into an overhead press.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Bear Crawl",
            category = ExerciseCategory.FULL_BODY,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Hands and feet on the floor, knees hovering just above it. Crawl forward moving opposite " +
                "hand and foot together, keeping your hips low and core braced.",
            createdAt = TEMPLATE_EPOCH,
        ),

        // ---- Cardio ----
        Exercise(
            name = "Outdoor Run",
            category = ExerciseCategory.CARDIO,
            equipment = ExerciseEquipment.OTHER,
            formNotes = "Land with a light, quick cadence under your hips rather than reaching out in front of " +
                "you, and keep your shoulders relaxed.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Treadmill Run",
            category = ExerciseCategory.CARDIO,
            equipment = ExerciseEquipment.CARDIO_MACHINE,
            formNotes = "Set an incline and pace to match your session's goal. Keep your posture tall and avoid " +
                "gripping the handrails, which reduces the workload and throws off your gait.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Stationary Cycling",
            category = ExerciseCategory.CARDIO,
            equipment = ExerciseEquipment.CARDIO_MACHINE,
            formNotes = "Set the seat height so your knee has a slight bend at the bottom of the pedal stroke. " +
                "Pedal at a steady cadence, adjusting resistance to hit your target effort.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Assault Bike",
            category = ExerciseCategory.CARDIO,
            equipment = ExerciseEquipment.CARDIO_MACHINE,
            formNotes = "Drive both the pedals and handles together. Resistance scales with your effort, so pace " +
                "intervals by perceived effort rather than a fixed cadence.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Stair Climber",
            category = ExerciseCategory.CARDIO,
            equipment = ExerciseEquipment.CARDIO_MACHINE,
            formNotes = "Stand tall and take full steps rather than short shuffles. Avoid leaning heavily on the " +
                "rails, which reduces the workload on your legs.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Elliptical",
            category = ExerciseCategory.CARDIO,
            equipment = ExerciseEquipment.CARDIO_MACHINE,
            formNotes = "Keep your posture upright and drive through your whole foot. Use the moving handles to " +
                "add upper-body work, or hold the rails to isolate the legs.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "High Knees",
            category = ExerciseCategory.CARDIO,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Run in place, driving your knees up to hip height as fast as you can while pumping your " +
                "arms, landing on the balls of your feet.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Shuttle Runs",
            category = ExerciseCategory.CARDIO,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Sprint to a line, touch it, and sprint back to the start, repeating between markers at " +
                "increasing distance. Decelerate under control to protect your knees on each turn.",
            createdAt = TEMPLATE_EPOCH,
        ),

        // ---- Mobility ----
        Exercise(
            name = "Cat-Cow",
            category = ExerciseCategory.MOBILITY,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "On hands and knees, alternate between arching your back toward the ceiling (cat) and " +
                "dropping your belly while lifting your chest (cow), moving with your breath.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "World's Greatest Stretch",
            category = ExerciseCategory.MOBILITY,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "From a lunge position, drop your back knee down, plant both hands inside your front foot, " +
                "then rotate your torso and reach one arm toward the ceiling - alternate sides.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Hip Circles",
            category = ExerciseCategory.MOBILITY,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Stand on one leg, lift the other knee, and trace slow circles with your knee in both " +
                "directions to open up the hip joint.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Band Shoulder Dislocate",
            category = ExerciseCategory.MOBILITY,
            equipment = ExerciseEquipment.BAND,
            formNotes = "Hold a band or light pole with a wide overhand grip. Raise it overhead and continue the " +
                "arc behind your back to your hips, keeping your arms as straight as your shoulders allow.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Foam Rolling",
            category = ExerciseCategory.MOBILITY,
            equipment = ExerciseEquipment.OTHER,
            formNotes = "Roll slowly over the target muscle, pausing on tender spots for a few breaths rather than " +
                "rolling quickly back and forth.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "90/90 Hip Stretch",
            category = ExerciseCategory.MOBILITY,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "Seated with front and back legs both bent at 90°. Lean your torso over the front shin to " +
                "stretch that hip, then switch which leg is in front.",
            createdAt = TEMPLATE_EPOCH,
        ),
        Exercise(
            name = "Thoracic Spine Rotation",
            category = ExerciseCategory.MOBILITY,
            equipment = ExerciseEquipment.BODYWEIGHT,
            formNotes = "On hands and knees, place one hand behind your head. Rotate your elbow down toward the " +
                "opposite arm, then open back up toward the ceiling, following the motion with your eyes.",
            createdAt = TEMPLATE_EPOCH,
        ),
    )

    private val TEMPLATE_EPOCH = Instant.fromEpochMilliseconds(0L)
}
