package com.shifthackz.aisdv1.network.response

import com.shifthackz.aisdv1.network.model.EasyDiffusionModelRaw
import kotlinx.serialization.Serializable

/** Endpoint catalog including tags that distinguish checkpoints from auxiliary models.
 * @param models Flat server catalog containing checkpoint and auxiliary-model entries.
 * @author Dmitriy Moroz
 */
@Serializable
data class EasyDiffusionModels(val models: List<EasyDiffusionModelRaw>)
