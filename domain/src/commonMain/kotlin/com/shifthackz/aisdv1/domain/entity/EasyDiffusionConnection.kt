package com.shifthackz.aisdv1.domain.entity

/**
 * Connection captured atomically with provider selection; never retags an older request revision.
 * @param url Selected instance root or reverse-proxy prefix.
 * @param model Exact checkpoint identifier selected for this instance.
 * @param selectionRevision Durable provider-selection revision captured with this connection.
 * @author Dmitriy Moroz
 */
data class EasyDiffusionConnection(val url: String, val model: String, val selectionRevision: Long)
