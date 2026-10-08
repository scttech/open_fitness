package com.scttech.android.kotlin.openfitness.ui.common

import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.scttech.android.kotlin.openfitness.ui.theme.AvatarPresets

private enum class AvatarPickerStep { MENU, PRESETS }

/**
 * Entry point for setting a profile avatar: a bundled presets grid, the device's photo gallery
 * (native Photo Picker, no permissions needed on API 33+), or a new camera photo (hidden on
 * devices with no camera).
 */
@Composable
fun AvatarPickerDialog(
    onPresetPicked: (String) -> Unit,
    onImagePicked: (Uri) -> Unit,
    onCreateCaptureUri: () -> Uri,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(AvatarPickerStep.MENU) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    val hasCamera = remember { context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) { onImagePicked(uri); onDismiss() } }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success -> pendingCaptureUri?.takeIf { success }?.let { onImagePicked(it); onDismiss() } }

    when (step) {
        AvatarPickerStep.MENU -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Set avatar") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Choose from presets") },
                        leadingContent = { Icon(Icons.Filled.Face, contentDescription = null) },
                        modifier = Modifier.clickable { step = AvatarPickerStep.PRESETS },
                    )
                    ListItem(
                        headlineContent = { Text("Choose from gallery") },
                        leadingContent = { Icon(Icons.Filled.Collections, contentDescription = null) },
                        modifier = Modifier.clickable {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                    )
                    if (hasCamera) {
                        ListItem(
                            headlineContent = { Text("Take a photo") },
                            leadingContent = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
                            modifier = Modifier.clickable {
                                val uri = onCreateCaptureUri()
                                pendingCaptureUri = uri
                                cameraLauncher.launch(uri)
                            },
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        )

        AvatarPickerStep.PRESETS -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Choose a preset") },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.fillMaxWidth().height(320.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(AvatarPresets.all, key = { it.key }) { preset ->
                        Image(
                            painter = painterResource(preset.drawableRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onPresetPicked(preset.key)
                                    onDismiss()
                                },
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { step = AvatarPickerStep.MENU }) { Text("Back") } },
        )
    }
}
