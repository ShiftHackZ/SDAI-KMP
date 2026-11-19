package com.shifthackz.aisdv1.data.preference.backup

import com.shifthackz.aisdv1.data.preference.KeyValueStore
import com.shifthackz.aisdv1.data.preference.readProviderSelection
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.domain.entity.NetworkTimeout
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.HuggingFaceModel
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupPreferences
import com.shifthackz.aisdv1.domain.entity.backup.BackupValue
import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage
import com.shifthackz.aisdv1.domain.gateway.backup.BackupPreferencesGateway

/**
 * Reads and checks portable preference batches without publishing partial or untrusted writes.
 * @param store Shared typed preference owner, also used by normal settings/configuration.
 * @param preferences Effective user settings, including defaults that have never been explicitly saved.
 * @author Dmitriy Moroz
 */
internal class BackupPreferencesGatewayImpl(
    private val store: KeyValueStore,
    private val preferences: PreferenceManager
) : BackupPreferencesGateway {
    override fun recoverPendingWrite(): Set<BackupCategory> {
        val previous = read()
        val authorizationCommit = store.getString("key_authorization_commit_id")
        val selection = store.getString("key_provider_selection_state")
        if (!store.retryPendingWrite()) return emptySet()
        val current = read()
        return buildSet {
            if (previous.providers != current.providers || authorizationCommit != store.getString("key_authorization_commit_id") || selection != store.getString(
                    "key_provider_selection_state"
                )
            ) add(BackupCategory.PROVIDERS)
            if (previous.settings != current.settings) add(BackupCategory.SETTINGS)
        }
    }

    override fun filterSupported(preferences: BackupPreferences) = BackupPreferences(
        providers = preferences.providers.mapNotNull { (provider, values) ->
            val schema = PortablePreferenceSchema.providers[provider] ?: return@mapNotNull null
            provider to filterFields(values, schema)
        }.toMap(),
        settings = filterFields(preferences.settings, PortablePreferenceSchema.settings),
    )

    private fun filterFields(
        values: Map<String, BackupValue>,
        schema: Map<String, PortablePreferenceField>
    ): Map<String, BackupValue> = values.filter { (id, value) ->
        val field = schema[id] ?: return@filter false
        val hasCorrectType = when (field.type) {
            PortablePreferenceType.TEXT -> value is BackupValue.Text
            PortablePreferenceType.FLAG -> value is BackupValue.Flag
            PortablePreferenceType.INTEGER -> value is BackupValue.Integer
        }
        require(hasCorrectType) { "Invalid portable preference type" }
        when (id) {
            "backend" -> (value as BackupValue.Text).value in MnnBackend.entries.map { it.key }
            "colorToken" -> (value as BackupValue.Text).value in ColorToken.entries.map { it.name }
            "darkToken" -> (value as BackupValue.Text).value in DarkThemeToken.entries.map { it.name }
            "galleryGrid" -> (value as BackupValue.Integer).value in Grid.entries.indices
            "connectionTimeout", "generationTimeout" -> (value as BackupValue.Integer).value in NetworkTimeout.entries.map { it.seconds }
            "textToImageSteps", "imageToImageSteps" -> (value as BackupValue.Integer).value > 0
            "textToImageCfg", "imageToImageCfg" -> (value as BackupValue.Text).value.toFloatOrNull()
                ?.let { it.isFinite() && it >= 0 } == true

            "language" -> (value as BackupValue.Text).value in setOf(
                "",
                "en",
                "de",
                "ru",
                "tr",
                "uk",
                "zh"
            )

            else -> true
        }
    }

    override fun read() = BackupPreferences(
        providers = PortablePreferenceSchema.providers.mapValues { (_, fields) -> readFields(fields) }
            .filter { (provider, fields) -> hasProviderConfiguration(provider, fields) },
        settings = mapOf(
            "dynamicColors" to BackupValue.Flag(preferences.designUseSystemColorPalette),
            "systemDarkTheme" to BackupValue.Flag(preferences.designUseSystemDarkTheme),
            "darkTheme" to BackupValue.Flag(preferences.designDarkTheme),
            "colorToken" to BackupValue.Text(preferences.designColorToken),
            "darkToken" to BackupValue.Text(preferences.designDarkThemeToken),
            "language" to BackupValue.Text(preferences.languageCode),
            "galleryGrid" to BackupValue.Integer(preferences.galleryGrid.ordinal),
            "autoSave" to BackupValue.Flag(preferences.autoSaveAiResults),
            "saveToMediaStore" to BackupValue.Flag(preferences.saveToMediaStore),
            "advancedOptions" to BackupValue.Flag(preferences.formAdvancedOptionsAlwaysShow),
            "taggedInput" to BackupValue.Flag(preferences.formPromptTaggedInput),
            "connectionTimeout" to BackupValue.Integer(preferences.connectionTimeout.seconds),
            "generationTimeout" to BackupValue.Integer(preferences.generationRequestTimeout.seconds),
            "dimScreen" to BackupValue.Flag(preferences.shouldDimScreen),
            "backgroundGeneration" to BackupValue.Flag(preferences.backgroundGeneration),
            "monitorConnection" to BackupValue.Flag(preferences.monitorConnectivity),
            "textToImageSteps" to BackupValue.Integer(preferences.textToImageSamplingSteps),
            "textToImageCfg" to BackupValue.Text(preferences.textToImageCfgScale.toString()),
            "imageToImageSteps" to BackupValue.Integer(preferences.imageToImageSamplingSteps),
            "imageToImageCfg" to BackupValue.Text(preferences.imageToImageCfgScale.toString()),
        ),
    )

    override fun writeProviders(
        providers: Map<String, Map<String, BackupValue>>,
        authorizationCommitId: String?
    ) {
        val fields = providers.flatMap { (provider, values) ->
            val schema = PortablePreferenceSchema.providers[provider].orEmpty()
            values.mapNotNull { (id, value) -> schema[id]?.let { it to value } }
        }.toMap()
        val strings = mutableMapOf<String, String>()
        val flags = mutableMapOf<String, Boolean>()
        collect(fields, strings, flags, mutableMapOf())
        authorizationCommitId?.let { strings["key_authorization_commit_id"] = it }
        val selection = store.readProviderSelection()
        val source = selection.first.key
        if (providers.containsKey(source) && (authorizationCommitId != null || fields.any { (field, value) ->
                readField(
                    field
                ) != value
            })) {
            val revision = selection.second
            check(revision < Long.MAX_VALUE) { "Provider revision exhausted" }
            strings["key_provider_selection_state"] = "$source|${revision + 1}"
        }
        store.putConfigurationDurably(strings, flags)
    }

    override fun writeSettings(settings: Map<String, BackupValue>) {
        val fields =
            settings.mapNotNull { (id, value) -> PortablePreferenceSchema.settings[id]?.let { it to value } }
                .toMap()
        val strings = mutableMapOf<String, String>()
        val flags = mutableMapOf<String, Boolean>()
        val integers = mutableMapOf<String, Int>()
        collect(fields, strings, flags, integers)
        store.putSettingsDurably(strings, flags, integers)
    }

    override fun readStartupStage(): BackupStartupStage =
        BackupStartupStage.entries.firstOrNull { it.name == store.getString("key_backup_startup_stage") }
            ?: if (store.getBoolean("key_on_boarding_complete")) BackupStartupStage.COMPLETE else BackupStartupStage.INVITATION

    override fun writeStartupStage(stage: BackupStartupStage) {
        if (stage == BackupStartupStage.INVITATION) {
            store.putConfigurationDurably(
                mapOf("key_backup_startup_stage" to stage.name),
                mapOf("key_on_boarding_complete" to true)
            )
        } else store.putStringDurably("key_backup_startup_stage", stage.name)
    }

    /**
     * Distinguishes legacy complete-snapshot defaults from saved provider or partial setup data.
     * Normal configuration commits materialize every provider's empty/default fields; those alone
     * cannot make a provider configured or consume the shared Basic Auth association.
     * @param provider Stable provider wire identifier.
     * @param fields Persisted portable values, retaining explicit defaults for an existing configuration.
     * @author Dmitriy Moroz
     */
    private fun hasProviderConfiguration(
        provider: String,
        fields: Map<String, BackupValue>
    ): Boolean =
        fields.any { (id, value) ->
            when (value) {
                is BackupValue.Flag -> value.isTrue
                is BackupValue.Integer -> value.value != 0
                is BackupValue.Text -> {
                    val implicitDefault = when {
                        provider == "open_ai_compatible" && id == "model" -> Configuration().openAiCompatibleModel
                        provider == "hugging_face" && id == "model" -> HuggingFaceModel.default.alias
                        provider == "local_mnn" && id == "backend" -> MnnBackend.CPU.key
                        else -> ""
                    }
                    value.value.isNotBlank() && value.value != implicitDefault
                }

                BackupValue.Unsupported -> false
            }
        }

    private fun readFields(fields: Map<String, PortablePreferenceField>): Map<String, BackupValue> =
        fields.filterValues { store.contains(it.key) }.mapValues { (_, field) -> readField(field) }

    private fun readField(field: PortablePreferenceField): BackupValue = when (field.type) {
        PortablePreferenceType.TEXT -> BackupValue.Text(store.getString(field.key))
        PortablePreferenceType.FLAG -> BackupValue.Flag(store.getBoolean(field.key))
        PortablePreferenceType.INTEGER -> BackupValue.Integer(store.getInt(field.key))
    }

    private fun collect(
        fields: Map<PortablePreferenceField, BackupValue>,
        strings: MutableMap<String, String>,
        flags: MutableMap<String, Boolean>,
        integers: MutableMap<String, Int>
    ) {
        fields.forEach { (field, value) ->
            when {
                field.type == PortablePreferenceType.TEXT && value is BackupValue.Text -> strings[field.key] =
                    value.value

                field.type == PortablePreferenceType.FLAG && value is BackupValue.Flag -> flags[field.key] =
                    value.isTrue

                field.type == PortablePreferenceType.INTEGER && value is BackupValue.Integer -> integers[field.key] =
                    value.value

                else -> error("Invalid portable preference type")
            }
        }
    }
}
