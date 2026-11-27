package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import kotlinx.coroutines.flow.Flow

/**
 * Catalog and owned-model operations used by domain download, selection and storage workflows.
 *
 * @author Dmitriy Moroz
 */
interface DownloadableModelRepository {
    /**
     * Reads a catalog model with its current download and independent selection state.
     * Storage and lookup failures propagate to the caller.
     *
     * @param id Stable catalog identity.
     * @author Dmitriy Moroz
     */
    suspend fun getById(id: String): LocalAiModel

    /**
     * Resolves the owned checkpoint for a completed catalog download.
     *
     * @param model Downloaded catalog model; user-managed custom sources are excluded.
     * @author Dmitriy Moroz
     */
    fun getModelPath(model: LocalAiModel): String

    /**
     * Streams the selected catalog source and publishes completion only after its complete model payload is activated.
     *
     * @param id identifier of the target entity.
     * @param url remote URL used by the operation.
     *
     * @author Dmitriy Moroz
     */
    fun download(id: String, url: String): Flow<DownloadState>

    /**
     * Removes only the owned model payload; MNN removal waits for active inference leases.
     *
     * @param id identifier of the target entity.
     *
     * @author Dmitriy Moroz
     */
    suspend fun delete(id: String)

    /**
     * Reads ONNX catalog models with current downloaded and selected state, including the custom entry outside Play.
     *
     * @author Dmitriy Moroz
     */
    suspend fun getAllOnnx(): List<LocalAiModel>

    /**
     * Fetches and persists one provider's metadata; transport, decoding and storage failures propagate.
     *
     * @param type Runtime family to refresh independently.
     *
     * @author Dmitriy Moroz
     */
    suspend fun fetchCatalog(type: LocalAiModel.Type)

    /**
     * Reads only cached metadata with current owned-file and selection state; never performs network I/O.
     *
     * @param type Runtime family whose cached choices are requested.
     *
     * @author Dmitriy Moroz
     */
    suspend fun readCachedCatalog(type: LocalAiModel.Type): List<LocalAiModel>

    /**
     * Reads MNN model metadata with current completion and independent selection state.
     *
     * @author Dmitriy Moroz
     */
    suspend fun getAllMnn(): List<LocalAiModel>

    /**
     * Observes cached MNN model states combined with provider selection changes.
     *
     * @author Dmitriy Moroz
     */
    fun observeAllMnn(): Flow<List<LocalAiModel>>

    /**
     * Reads available MediaPipe model metadata with current local download and selection state.
     *
     * @author Dmitriy Moroz
     */
    suspend fun getAllMediaPipe(): List<LocalAiModel>

    /**
     * Reads stable-diffusion.cpp model metadata with current local download and selection state.
     *
     * @author Dmitriy Moroz
     */
    suspend fun getAllSdxl(): List<LocalAiModel>

    /**
     * Reads Core ML model metadata with current local download and selection state.
     *
     * @author Dmitriy Moroz
     */
    suspend fun getAllCoreMl(): List<LocalAiModel>

    /**
     * Reads Bonsai model metadata with current local download and selection state.
     *
     * @author Dmitriy Moroz
     */
    suspend fun getAllBonsai(): List<LocalAiModel>

    /**
     * Observes cached ONNX models combined with current selection and completion state.
     *
     * @author Dmitriy Moroz
     */
    fun observeAllOnnx(): Flow<List<LocalAiModel>>

    /**
     * Observes cached stable-diffusion.cpp models combined with current selection and completion state.
     *
     * @author Dmitriy Moroz
     */
    fun observeAllSdxl(): Flow<List<LocalAiModel>>

    /**
     * Observes cached Core ML models combined with current selection and completion state.
     *
     * @author Dmitriy Moroz
     */
    fun observeAllCoreMl(): Flow<List<LocalAiModel>>

    /**
     * Observes cached Bonsai models combined with current selection and completion state.
     *
     * @author Dmitriy Moroz
     */
    fun observeAllBonsai(): Flow<List<LocalAiModel>>
}
