package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.data.mappers.mapDomainToEntity
import com.shifthackz.aisdv1.data.mappers.mapEntityToDomain
import com.shifthackz.aisdv1.domain.datasource.DownloadableModelDataSource
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.storage.db.persistent.dao.LocalModelDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Reads local model metadata and combines it with filesystem download state.
 *
 * @param dao Room DAO that stores local model metadata.
 * @param preferenceManager Preferences source for currently selected local model ids.
 * @param buildInfoProvider Build metadata used to filter custom model placeholders.
 * @param fileStore Platform file store that resolves whether model files are downloaded.
 * @throws IllegalStateException when delegated local model IO cannot complete.
 *
 * @author Dmitriy Moroz
 */
internal class DownloadableModelLocalDataSource(
    private val dao: LocalModelDao,
    private val preferenceManager: PreferenceManager,
    private val buildInfoProvider: BuildInfoProvider,
    private val fileStore: DownloadableModelFileStore,
) : DownloadableModelDataSource.Local {
    /**
     * Reads the dedicated upscaler family without mixing it with generation checkpoints.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAllUpscalers() =
        dao.queryByType(LocalAiModel.Type.Upscaler.key).mapEntityToDomain().withLocalData()

    /**
     * Resolves a completed catalog payload through the platform file store.
     *
     * @param model Downloaded catalog entry whose owned checkpoint is needed.
     * @author Dmitriy Moroz
     */
    override fun getModelPath(model: LocalAiModel): String {
        require(model.downloaded && model.sources.isNotEmpty())
        return fileStore.resolvePath(model)
    }


    /**
     * Reads MNN model metadata with current completion and independent selection state.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAllMnn() = (dao.queryByType(LocalAiModel.Type.Mnn.key)
        .mapEntityToDomain() + if (buildInfoProvider.type != BuildType.PLAY) listOf(LocalAiModel.CustomMnn) else emptyList())
        .withLocalData()

    /**
     * Observes cached MNN model states combined with provider selection changes.
     *
     * @author Dmitriy Moroz
     */
    override fun observeAllMnn(): Flow<List<LocalAiModel>> =
        dao.observeByType(LocalAiModel.Type.Mnn.key)
            .combine(preferenceManager.observe()) { entities, _ ->
                (entities.mapEntityToDomain() + if (buildInfoProvider.type != BuildType.PLAY) listOf(
                    LocalAiModel.CustomMnn
                ) else emptyList())
                    .withLocalData()
            }

    /**
     * Reads ONNX catalog and allowed custom entries with current file and selected state.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAllOnnx() = dao
        .queryByType(LocalAiModel.Type.ONNX.key)
        .mapEntityToDomain()
        .let { models ->
            buildList {
                addAll(models)
                if (buildInfoProvider.type != BuildType.PLAY) {
                    add(LocalAiModel.CustomOnnx)
                }
            }
        }
        .withLocalData()

    /**
     * Reads available MediaPipe model metadata with current local download and selection state.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAllMediaPipe() = dao
        .queryByType(LocalAiModel.Type.MediaPipe.key)
        .mapEntityToDomain()
        .let { models ->
            buildList {
                addAll(models)
                if (buildInfoProvider.type != BuildType.PLAY) {
                    add(LocalAiModel.CustomMediaPipe)
                }
            }
        }
        .withLocalData()

    /**
     * Reads stable-diffusion.cpp model metadata with current local download and selection state.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAllSdxl() = dao
        .queryByType(LocalAiModel.Type.Sdxl.key)
        .mapEntityToDomain()
        .let { models ->
            buildList {
                addAll(models)
                if (buildInfoProvider.type != BuildType.PLAY) {
                    add(LocalAiModel.CustomSdxl)
                }
            }
        }
        .withLocalData()

    /**
     * Reads Core ML model metadata with current local download and selection state.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAllCoreMl() = dao
        .queryByType(LocalAiModel.Type.CoreMl.key)
        .mapEntityToDomain()
        .withLocalData()

    /**
     * Reads Bonsai model metadata with current local download and selection state.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAllBonsai() = dao
        .queryByType(LocalAiModel.Type.Bonsai.key)
        .mapEntityToDomain()
        .withLocalData()

    /**
     * Resolves a catalog or supported custom identity and attaches its current file and selection state; missing identities fail.
     *
     * @param id identifier of the target entity.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getById(id: String): LocalAiModel {
        val chain = when (id) {
            LocalAiModel.CustomMnn.id -> LocalAiModel.CustomMnn
            LocalAiModel.CustomOnnx.id -> LocalAiModel.CustomOnnx
            LocalAiModel.CustomMediaPipe.id -> LocalAiModel.CustomMediaPipe
            LocalAiModel.CustomSdxl.id -> LocalAiModel.CustomSdxl
            LocalAiModel.CustomCoreMl.id -> LocalAiModel.CustomCoreMl
            LocalAiModel.CustomBonsai.id -> LocalAiModel.CustomBonsai
            else -> dao
                .queryById(id)
                .mapEntityToDomain()
        }
        return chain.withLocalData()
    }

    /**
     * Resolves the currently selected ONNX identity; absent metadata remains a failure.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getSelectedOnnx() = runCatching {
        getById(preferenceManager.localOnnxModelId)
    }.getOrElse {
        throw IllegalStateException("No selected model.", it)
    }

    /**
     * Resolves the currently selected stable-diffusion.cpp identity; absent metadata remains a failure.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getSelectedSdxl() = runCatching {
        getById(preferenceManager.localSdxlModelId)
    }.getOrElse {
        throw IllegalStateException("No selected model.", it)
    }

    /**
     * Resolves the currently selected Core ML identity; absent metadata remains a failure.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getSelectedCoreMl() = runCatching {
        getById(preferenceManager.localCoreMlModelId)
    }.getOrElse {
        throw IllegalStateException("No selected model.", it)
    }

    /**
     * Resolves the currently selected Bonsai identity; absent metadata remains a failure.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getSelectedBonsai() = runCatching {
        getById(preferenceManager.localBonsaiModelId)
    }.getOrElse {
        throw IllegalStateException("No selected model.", it)
    }

    /**
     * Observes cached ONNX models combined with current selection and completion state.
     *
     * @author Dmitriy Moroz
     */
    override fun observeAllOnnx(): Flow<List<LocalAiModel>> = dao
        .observeByType(LocalAiModel.Type.ONNX.key)
        .combine(preferenceManager.observe()) { entities, _ ->
            buildList {
                addAll(entities.mapEntityToDomain())
                if (buildInfoProvider.type != BuildType.PLAY) add(LocalAiModel.CustomOnnx)
            }
                .withLocalData()
        }

    /**
     * Observes cached stable-diffusion.cpp models combined with current selection and completion state.
     *
     * @author Dmitriy Moroz
     */
    override fun observeAllSdxl(): Flow<List<LocalAiModel>> = dao
        .observeByType(LocalAiModel.Type.Sdxl.key)
        .combine(preferenceManager.observe()) { entities, _ ->
            buildList {
                addAll(entities.mapEntityToDomain())
                if (buildInfoProvider.type != BuildType.PLAY) {
                    add(LocalAiModel.CustomSdxl)
                }
            }
                .withLocalData()
        }

    /**
     * Observes cached Core ML models combined with current selection and completion state.
     *
     * @author Dmitriy Moroz
     */
    override fun observeAllCoreMl(): Flow<List<LocalAiModel>> = dao
        .observeByType(LocalAiModel.Type.CoreMl.key)
        .combine(preferenceManager.observe()) { entities, _ ->
            entities
                .mapEntityToDomain()
                .withLocalData()
        }

    /**
     * Observes cached Bonsai models combined with current selection and completion state.
     *
     * @author Dmitriy Moroz
     */
    override fun observeAllBonsai(): Flow<List<LocalAiModel>> = dao
        .observeByType(LocalAiModel.Type.Bonsai.key)
        .combine(preferenceManager.observe()) { entities, _ ->
            entities
                .mapEntityToDomain()
                .withLocalData()
        }

    /**
     * Upserts catalog metadata without persisting synthetic custom-model entries.
     *
     * @param list Fetched provider catalog metadata to upsert; synthetic custom entries are not persisted.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun save(list: List<LocalAiModel>) {
        dao.insertList(
            list
                .filterNot { it.id in customModelIds }
                .mapDomainToEntity(),
        )
    }

    /**
     * Removes only the owned model payload; MNN removal waits for active inference leases.
     *
     * @param id identifier of the target entity.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun delete(id: String) {
        fileStore.delete(id)
    }

    /**
     * Attaches filesystem completion and independently persisted model selection without changing catalog metadata.
     *
     * @author Dmitriy Moroz
     */
    private fun List<LocalAiModel>.withLocalData() = map { model -> model.withLocalData() }

    /**
     * Attaches filesystem completion and independently persisted model selection without changing catalog metadata.
     *
     * @author Dmitriy Moroz
     */
    private fun LocalAiModel.withLocalData() = copy(
        downloaded = fileStore.isDownloaded(this),
        selected = when (this.type) {
            LocalAiModel.Type.Upscaler -> preferenceManager.upscalerModelId == id
            LocalAiModel.Type.Mnn -> preferenceManager.localMnnModelId == id
            LocalAiModel.Type.ONNX -> preferenceManager.localOnnxModelId == id
            LocalAiModel.Type.MediaPipe -> preferenceManager.localMediaPipeModelId == id
            LocalAiModel.Type.Sdxl -> preferenceManager.localSdxlModelId == id
            LocalAiModel.Type.CoreMl -> preferenceManager.localCoreMlModelId == id
            LocalAiModel.Type.Bonsai -> preferenceManager.localBonsaiModelId == id
        },
    )

    private companion object {
        val customModelIds = setOf(
            LocalAiModel.CustomMnn.id,
            LocalAiModel.CustomOnnx.id,
            LocalAiModel.CustomMediaPipe.id,
            LocalAiModel.CustomSdxl.id,
            LocalAiModel.CustomCoreMl.id,
            LocalAiModel.CustomBonsai.id,
        )
    }
}
