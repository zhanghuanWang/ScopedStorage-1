package ru.vladimir.tilikov.scopedstoragex.data.repositories

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ru.vladimir.tilikov.scopedstoragex.data.Video
import ru.vladimir.tilikov.scopedstoragex.networking.Networking
import ru.vladimir.tilikov.scopedstoragex.utils.haveQ
import ru.vladimir.tilikov.scopedstoragex.utils.haveR
import timber.log.Timber

class VideosRepository(private val context: Context) {

    private var observer: ContentObserver? = null

    fun observeVideos(onChange: () -> Unit) {
        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                onChange()
            }
        }
        context.contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            observer!!
        )
    }

    fun unregisterVideoObserver() {
        observer?.let { context.contentResolver.unregisterContentObserver(it) }
    }

    suspend fun getAllVideos(): List<Video> {
        val videos = mutableListOf<Video>()

        withContext(Dispatchers.IO) {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    videos.add(getVideoByCursor(cursor))
                }
            }
        }

        return videos
    }

    private fun getVideoByCursor(cursor: Cursor): Video {
        val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID))
        val name =
            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
        val size = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
        val uri =
            ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
        val favorite = if (haveR()) {
            cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.IS_FAVORITE))
        } else {
            13
        }
        val isFavorite = when (favorite) {
            0 -> false
            1 -> true
            else -> false
        }

        return Video(id, uri, name, size, isFavorite)
    }

    suspend fun saveVideo(uri: Uri, url: String) {
        withContext(Dispatchers.IO) {
            downloadVideo(uri, url)
        }
    }

    private suspend fun downloadVideo(uri: Uri, url: String) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
//            error("kill download")
            Networking.api
                .getFile(url)
                .byteStream()
                .use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
        }
    }

    suspend fun deleteVideoById(id: Long) {
        withContext(Dispatchers.IO) {
            val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
            deleteVideo(uri)
        }
    }

    suspend fun deleteVideo(uri: Uri) {
        withContext(Dispatchers.IO) {
            context.contentResolver.delete(uri, null, null)
        }
    }
}