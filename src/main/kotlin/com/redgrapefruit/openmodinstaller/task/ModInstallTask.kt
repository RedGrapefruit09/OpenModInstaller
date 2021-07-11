package com.redgrapefruit.openmodinstaller.task

import com.redgrapefruit.openmodinstaller.consts.assetCache
import com.redgrapefruit.openmodinstaller.data.ReleaseEntry
import okhttp3.*
import java.io.File
import java.io.FileOutputStream

/**
 * A [Task] which handles automatic installation of mods
 */
object ModInstallTask : Task<DefaultPreLaunchTaskContext, ModInstallLaunchContext, DefaultPostLaunchTaskContext> {
    override fun launch(context: ModInstallLaunchContext) {
        context.apply {
            val path = "$modsFolder/$jarName.jar"
            if (File(path).exists())

                try {
                    downloadFile(entry.url, path)
                } catch (exception: Exception) {
                    // TODO: Handle with a popup RenderTask (lucsoft)
                }
        }
    }
}

/**
 * An event handling cache misses for use in OkHttp
 */
private class Event(
    /**
     * The requested download URL
     */
    val url: String) : EventListener() {

    override fun cacheMiss(call: Call) {
        super.cacheMiss(call)

        println("Loading asset: $url")
    }

    override fun cacheHit(call: Call, response: Response) = hit()
    override fun cacheConditionalHit(call: Call, cachedResponse: Response) = hit()

    private fun hit() {
        println("Loading asset from cache: $url")
    }
}


/**
 * Raw [downloadFile] that returns a [ByteArray], not decoded to a [String]
 */
fun downloadFileInBytes(
    input: String,
): ByteArray {
    // Init OkHttp client
    val client = OkHttpClient.Builder().cache(assetCache).eventListener(Event(input)).build()
    // Make request
    val request = Request.Builder().url(input).build()
    // Call
    return client.newCall(request).execute().body!!.bytes()
}

/**
 * Downloads a file from the Internet using the OkHttp client
 */
fun downloadFile(
    /**
     * The input URL. Recommended to provide full URLs (e.g. not `github.com`, but `https://www.github.com`)
     */
    input: String,
    /**
     * The output URI
     */
    output: String,
) {
    val content = downloadFileInBytes(input)

    File(output).createNewFile()

    FileOutputStream(output).use { stream ->
        stream.write(content)
    }
}

/**
 * The [LaunchTaskContext] for [ModInstallTask]
 */
data class ModInstallLaunchContext(
    /**
     * The target mods folder
     */
    val modsFolder: String,
    /**
     * The linked [ReleaseEntry]
     */
    val entry: ReleaseEntry,
    /**
     * The filename of the target JAR.
     *
     * If `mod`, the target file will be `mod.jar`
     */
    val jarName: String
) : LaunchTaskContext
