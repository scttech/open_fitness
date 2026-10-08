package com.scttech.android.kotlin.openfitness.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns all on-disk storage for user-supplied profile avatars: decoding/downscaling incoming
 * gallery or camera images into a small persistent JPEG, and the FileProvider-backed temp file
 * a camera capture intent writes a full-resolution photo into before it's resampled down.
 */
@Singleton
class AvatarImageStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val avatarsDir: File
        get() = File(context.filesDir, "avatars").apply { mkdirs() }

    private val capturesDir: File
        get() = File(context.cacheDir, "avatar_captures").apply { mkdirs() }

    /** Creates an empty file for the camera to write into, returning a FileProvider content Uri. */
    fun createCaptureUri(): Uri {
        val file = File(capturesDir, "capture_${System.currentTimeMillis()}.jpg")
        file.createNewFile()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /**
     * Reads [source] (a gallery content:// Uri or our own capture Uri - both open identically via
     * the ContentResolver), downsamples it during decode to at most [MAX_DIMENSION_PX] on its
     * longest side (never allocating a full-resolution bitmap), re-encodes as JPEG, and writes it
     * to a fresh persistent file for [profileId]. Returns the absolute path, or null if the source
     * couldn't be read/decoded - callers should leave the profile's avatar unchanged in that case.
     */
    fun persistAvatarFrom(source: Uri, profileId: Long): String? {
        val bitmap = decodeSampledBitmap(source) ?: return null
        val outFile = File(avatarsDir, "avatar_${profileId}_${System.currentTimeMillis()}.jpg")
        return try {
            FileOutputStream(outFile).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out) }
            outFile.absolutePath
        } catch (e: Exception) {
            outFile.delete()
            null
        } finally {
            bitmap.recycle()
        }
    }

    /** Best-effort delete of a previously-persisted avatar file; safe to call on any path/null. */
    fun deleteAvatarFile(path: String?) {
        if (path != null) File(path).delete()
    }

    /** Two-pass decode: measure bounds first, compute inSampleSize, then decode at that size - avoids
     * allocating a full-resolution bitmap for a multi-megapixel camera photo. */
    private fun decodeSampledBitmap(uri: Uri): Bitmap? = try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: return null

        val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_DIMENSION_PX)
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sampleSize })
        }
    } catch (e: Exception) {
        null
    } catch (e: OutOfMemoryError) {
        null
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var sampleSize = 1
        var longestSide = maxOf(width, height)
        while (longestSide / 2 >= maxDimension) {
            sampleSize *= 2
            longestSide /= 2
        }
        return sampleSize
    }

    private companion object {
        const val MAX_DIMENSION_PX = 512
        const val JPEG_QUALITY = 85
    }
}
