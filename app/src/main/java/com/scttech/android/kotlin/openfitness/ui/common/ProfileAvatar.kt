package com.scttech.android.kotlin.openfitness.ui.common

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.scttech.android.kotlin.openfitness.domain.model.Profile
import com.scttech.android.kotlin.openfitness.ui.theme.AvatarPresets
import com.scttech.android.kotlin.openfitness.ui.theme.ProfileAccentColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Renders a profile's avatar: a custom photo or bundled preset when set, falling back to the
 * original colored-circle placeholder otherwise - zero visual change for profiles with no avatar.
 */
@Composable
fun ProfileAvatar(profile: Profile, size: Dp, modifier: Modifier = Modifier) {
    val fallbackColor = ProfileAccentColors[profile.colorIndex % ProfileAccentColors.size]
    val presetRes = profile.avatarPresetKey?.let { AvatarPresets.resourceFor(it) }
    val avatarFilePath = profile.avatarFilePath

    Box(modifier = modifier.size(size).background(color = fallbackColor, shape = CircleShape)) {
        when {
            presetRes != null -> Image(
                painter = painterResource(presetRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape),
            )
            avatarFilePath != null -> {
                val bitmapState = produceState<ImageBitmap?>(initialValue = null, avatarFilePath) {
                    value = withContext(Dispatchers.IO) {
                        try {
                            BitmapFactory.decodeFile(avatarFilePath)?.asImageBitmap()
                        } catch (e: Exception) {
                            null
                        } catch (e: OutOfMemoryError) {
                            null
                        }
                    }
                }
                bitmapState.value?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(size).clip(CircleShape),
                    )
                }
                // bitmapState.value == null (still loading, or decode failed/file missing) falls
                // through to just the Box's colored background underneath.
            }
        }
    }
}
