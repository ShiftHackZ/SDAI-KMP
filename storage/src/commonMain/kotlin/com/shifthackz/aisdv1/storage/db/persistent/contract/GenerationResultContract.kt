package com.shifthackz.aisdv1.storage.db.persistent.contract

/**
 * Stable Room names shared by full generation rows, metadata projections and gallery queries.
 *
 * @author Dmitriy Moroz
 */
internal object GenerationResultContract {
    /**
     * Saved generation rows shared by gallery, backup and publication metadata.
     *
     * @author Dmitriy Moroz
     */
    const val TABLE = "generation_results"

    /**
     * Opaque content identity changed whenever the image is replaced.
     *
     * @author Dmitriy Moroz
     */
    const val IMAGE_REVISION = "image_revision"

    /**
     * Index supporting stable newest-first gallery paging.
     *
     * @author Dmitriy Moroz
     */
    const val CREATED_AT_INDEX = "index_generation_results_created_at"

    /**
     * Persistent gallery row identity.
     *
     * @author Dmitriy Moroz
     */
    const val ID = "id"

    /**
     * Original encoded output image payload.
     *
     * @author Dmitriy Moroz
     */
    const val IMAGE_BASE_64 = "image_base_64"

    /**
     * Optional encoded input image payload.
     *
     * @author Dmitriy Moroz
     */
    const val ORIGINAL_IMAGE_BASE_64 = "original_image_base_64"

    /**
     * Original image creation time in epoch milliseconds.
     *
     * @author Dmitriy Moroz
     */
    const val CREATED_AT = "created_at"

    /**
     * Stored text-to-image or image-to-image operation key.
     *
     * @author Dmitriy Moroz
     */
    const val GENERATION_TYPE = "generation_type"

    /**
     * Positive generation prompt.
     *
     * @author Dmitriy Moroz
     */
    const val PROMPT = "prompt"

    /**
     * Negative generation prompt.
     *
     * @author Dmitriy Moroz
     */
    const val NEGATIVE_PROMPT = "negative_prompt"

    /**
     * Recorded output width in pixels.
     *
     * @author Dmitriy Moroz
     */
    const val WIDTH = "width"

    /**
     * Recorded output height in pixels.
     *
     * @author Dmitriy Moroz
     */
    const val HEIGHT = "height"

    /**
     * Requested sampling iteration count.
     *
     * @author Dmitriy Moroz
     */
    const val SAMPLING_STEPS = "sampling_steps"

    /**
     * Recorded guidance scale.
     *
     * @author Dmitriy Moroz
     */
    const val CFG_SCALE = "cfg_scale"

    /**
     * Whether face restoration was requested.
     *
     * @author Dmitriy Moroz
     */
    const val RESTORE_FACES = "restore_faces"

    /**
     * Recorded sampling algorithm.
     *
     * @author Dmitriy Moroz
     */
    const val SAMPLER = "sampler"

    /**
     * Resolved primary generation seed.
     *
     * @author Dmitriy Moroz
     */
    const val SEED = "seed"

    /**
     * Recorded variation seed.
     *
     * @author Dmitriy Moroz
     */
    const val SUB_SEED = "sub_seed"

    /**
     * Recorded variation-seed strength.
     *
     * @author Dmitriy Moroz
     */
    const val SUB_SEED_STRENGTH = "sub_seed_strength"

    /**
     * Recorded image-to-image denoising strength.
     *
     * @author Dmitriy Moroz
     */
    const val DENOISING_STRENGTH = "denoising_strength"

    /**
     * Whether the image is hidden from the ordinary gallery.
     *
     * @author Dmitriy Moroz
     */
    const val HIDDEN = "hidden"

    /**
     * Whether the image is marked as a favorite.
     *
     * @author Dmitriy Moroz
     */
    const val LIKED = "liked"

    /**
     * Model name captured when the image was generated.
     *
     * @author Dmitriy Moroz
     */
    const val MODEL_NAME = "model_name"

    /**
     * Provider catalog key captured at generation time.
     *
     * @author Dmitriy Moroz
     */
    const val PROVIDER_KEY = "provider_key"

    /**
     * Execution mode captured for this generation.
     *
     * @author Dmitriy Moroz
     */
    const val ACCELERATION = "acceleration"

    /**
     * Originating device name retained across backup transfers.
     *
     * @author Dmitriy Moroz
     */
    const val DEVICE_NAME = "device_name"

    /**
     * Originating platform and OS version.
     *
     * @author Dmitriy Moroz
     */
    const val OS_VERSION = "os_version"

    /**
     * Elapsed generation time in milliseconds.
     *
     * @author Dmitriy Moroz
     */
    const val GENERATION_DURATION_MILLIS = "generation_duration_millis"

    /**
     * Confirmed public Civitai post URL for this image.
     *
     * @author Dmitriy Moroz
     */
    const val CIVITAI_URL = "civitai_url"

    /**
     * Dedicated super-resolution checkpoint provenance.
     *
     * @author Dmitriy Moroz
     */
    const val UPSCALER_MODEL_NAME = "upscaler_model_name"
}
