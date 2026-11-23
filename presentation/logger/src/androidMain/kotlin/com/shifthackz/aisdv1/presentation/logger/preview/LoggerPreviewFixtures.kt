package com.shifthackz.aisdv1.presentation.logger.preview

/**
 * Deterministic sample log text for inspecting monospace rows and both scroll directions.
 * These strings contain no device logs and never invoke the platform log reader.
 *
 * @author Dmitriy Moroz
 */
internal object LoggerPreviewFixtures {
    const val ShortLog = "12:00:00.000 I/SDAI: Application started"

    val mixedLog = """
        12:00:00.000 I/SDAI: Application started
        12:00:00.042 D/SDAI: Loading preferences
        12:00:00.056 V/SDAI: Theme applied
        12:00:00.120 W/SDAI: Connection unavailable
        12:00:00.180 E/SDAI: Request timed out
        12:00:01.200 I/SDAI: Connection restored
    """.trimIndent()

    val longLog = (1..120).joinToString("\n") { line ->
        "12:00:00.000 D/SDAI: Log entry $line"
    }

    val wideLog = "12:00:00.000 D/SDAI: Parameters: " +
            "model=sample-diffusion, sampler=Euler, steps=30, cfgScale=7.0, " +
            "width=1024, height=1024, seed=123456789, batchSize=1, " +
            "prompt=An illustrated mountain village beneath a clear starry sky"

    val stackTrace = """
        12:00:00.000 E/SDAI: Unable to read server status
        java.io.IOException: Connection timed out
            at sample.client.Connection.read(Connection.kt:42)
            at sample.client.StatusReader.load(StatusReader.kt:18)
        Caused by: java.net.SocketTimeoutException: Read timed out
            at sample.client.Socket.read(Socket.kt:64)
        12:00:01.000 I/SDAI: Retry available
    """.trimIndent()
}
