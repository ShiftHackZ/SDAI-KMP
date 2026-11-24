package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.datasource.DownloadableModelDataSource
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository

/**
 * Catalog and owned-model operations used by domain download, selection and storage workflows.
 *
 * @param remoteDataSource Strict remote catalog fetch and archive transfer operations.
 * @param localDataSource Cached model metadata with current owned-file state.
 *
 * @author Dmitriy Moroz
 */
internal class DownloadableModelRepositoryImpl(
    private val remoteDataSource: DownloadableModelDataSource.Remote,
    private val localDataSource: DownloadableModelDataSource.Local,
) : DownloadableModelRepository {
    /**
     * Reads current model metadata and filesystem completion without network recovery.
     *
     * @param id Stable catalog identity.
     * @author Dmitriy Moroz
     */
    override suspend fun getById(id: String): LocalAiModel = localDataSource.getById(id)

    /**
     * Resolves a downloaded model through the local store without changing file ownership.
     *
     * @param model Completed catalog download whose runtime checkpoint is requested.
     * @author Dmitriy Moroz
     */
    override fun getModelPath(model: LocalAiModel): String = localDataSource.getModelPath(model)


    /**
     * Streams the selected catalog source and publishes completion only after its complete model payload is activated.
     *
     * @param id identifier of the target entity.
     * @param url remote URL used by the operation.
     *
     * @author Dmitriy Moroz
     */
    override fun download(id: String, url: String) = remoteDataSource.download(id, url)

    /**
     * Removes only the owned model payload; MNN removal waits for active inference leases.
     *
     * @param id identifier of the target entity.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun delete(id: String) = localDataSource.delete(id)

    /**
     * Persists exactly one fetched provider catalog and propagates every fetch/save failure.
     *
     * @param type Runtime family to refresh independently.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun fetchCatalog(type: LocalAiModel.Type) {
        localDataSource.save(remoteDataSource.fetchCatalog(type))
    }

    /**
     * Reads current cached metadata with platform file and selection state, without network access.
     *
     * @param type Runtime family whose cache is requested.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun readCachedCatalog(type: LocalAiModel.Type): List<LocalAiModel> =
        when (type) {
            LocalAiModel.Type.Upscaler -> localDataSource.getAllUpscalers()
            LocalAiModel.Type.ONNX -> getAllOnnx()
            LocalAiModel.Type.MediaPipe -> getAllMediaPipe()
            LocalAiModel.Type.Sdxl -> getAllSdxl()
            LocalAiModel.Type.CoreMl -> getAllCoreMl()
            LocalAiModel.Type.Bonsai -> getAllBonsai()
            LocalAiModel.Type.Mnn -> getAllMnn()
        }

    /**
     * Reads MNN model metadata with current completion and independent selection state.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAllMnn() = localDataSource.getAllMnn()

    /**
     * Observes cached MNN model states combined with provider selection changes.
     *
     * @author Dmitriy Moroz
     */
    override fun observeAllMnn() = localDataSource.observeAllMnn()

    /**
     * Reads cached ONNX choices with current owned-file and selection state.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAllOnnx(): List<LocalAiModel> {
        return localDataSource.getAllOnnx()
    }

    /**
     * Reads available MediaPipe model metadata with current local download and selection state.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAllMediaPipe(): List<LocalAiModel> {
        return localDataSource.getAllMediaPipe()
    }

    /**
     * Reads stable-diffusion.cpp model metadata with current local download and selection state.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAllSdxl(): List<LocalAiModel> {
        return localDataSource.getAllSdxl()
    }

    /**
     * Reads Core ML model metadata with current local download and selection state.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAllCoreMl(): List<LocalAiModel> {
        return localDataSource.getAllCoreMl()
    }

    /**
     * Reads Bonsai model metadata with current local download and selection state.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAllBonsai(): List<LocalAiModel> {
        return localDataSource.getAllBonsai()
    }

    /**
     * Observes cached ONNX models combined with current selection and completion state.
     *
     * @author Dmitriy Moroz
     */
    override fun observeAllOnnx() = localDataSource.observeAllOnnx()

    /**
     * Observes cached stable-diffusion.cpp models combined with current selection and completion state.
     *
     * @author Dmitriy Moroz
     */
    override fun observeAllSdxl() = localDataSource.observeAllSdxl()

    /**
     * Observes cached Core ML models combined with current selection and completion state.
     *
     * @author Dmitriy Moroz
     */
    override fun observeAllCoreMl() = localDataSource.observeAllCoreMl()

    /**
     * Observes cached Bonsai models combined with current selection and completion state.
     *
     * @author Dmitriy Moroz
     */
    override fun observeAllBonsai() = localDataSource.observeAllBonsai()

}
