package com.shifthackz.aisdv1.domain.entity

/**
 * Checkpoint identifier and display name advertised by the server.
 * @param id Complete checkpoint identifier returned by /get/models; preserved verbatim in render requests.
 * @param name Human-readable model label; the exact identifier is also shown.
 * @author Dmitriy Moroz
 */
data class EasyDiffusionModel(val id: String, val name: String)
