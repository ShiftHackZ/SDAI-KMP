package com.shifthackz.aisdv1.core.common.links

/**
 * Supplies the standard web destinations used by the shared app when no platform override is installed.
 *
 * @author Dmitriy Moroz
 */
object DefaultLinksProvider : LinksProvider {
    /**
     * AI Horde service home page for learning about crowdsourced generation.
     *
     * @author Dmitriy Moroz
     */
    override val hordeUrl: String = "https://stablehorde.net/"

    /**
     * AI Horde registration page for obtaining an account and API key.
     *
     * @author Dmitriy Moroz
     */
    override val hordeSignUpUrl: String = "https://stablehorde.net/register"

    /**
     * Hugging Face inference documentation for configuring a hosted provider.
     *
     * @author Dmitriy Moroz
     */
    override val huggingFaceUrl: String =
        "https://huggingface.co/docs/inference-providers/providers/hf-inference"

    /**
     * OpenAI API key management page used during provider setup.
     *
     * @author Dmitriy Moroz
     */
    override val openAiInfoUrl: String = "https://platform.openai.com/api-keys"

    /**
     * Stability AI platform page for managing hosted generation access.
     *
     * @author Dmitriy Moroz
     */
    override val stabilityAiInfoUrl: String = "https://platform.stability.ai/"

    /**
     * Fal.ai dashboard for creating and managing generation API keys.
     *
     * @author Dmitriy Moroz
     */
    override val falAiInfoUrl: String = "https://fal.ai/dashboard/keys"

    /**
     * ArliAI quick-start documentation for configuring its image API.
     *
     * @author Dmitriy Moroz
     */
    override val arliAiInfoUrl: String = "https://www.arliai.com/quick-start"

    /**
     * SDAI privacy policy opened from Settings and onboarding.
     *
     * @author Dmitriy Moroz
     */
    override val privacyPolicyUrl: String = "https://sdai.moroz.cc/policy.html"

    /**
     * SDAI support page listing ways to contribute to project development.
     *
     * @author Dmitriy Moroz
     */
    override val donateUrl: String = "https://sdai.moroz.cc/donate.html"

    /**
     * SDAI home page with downloads, provider information and community links.
     *
     * @author Dmitriy Moroz
     */
    override val projectWebsiteUrl: String = "https://sdai.moroz.cc"

    /**
     * Developer home page linked from the Settings community section.
     *
     * @author Dmitriy Moroz
     */
    override val developerWebsiteUrl: String = "https://moroz.cc"

    /**
     * Canonical repository URL opened by the app's source-code links.
     *
     * @author Dmitriy Moroz
     */
    override val gitHubSourceUrl: String = "https://github.com/ShiftHackZ/SDAI-KMP"

    /**
     * License page in the canonical repository, opened by the app's license links.
     *
     * @author Dmitriy Moroz
     */
    override val licenseUrl: String = "https://github.com/ShiftHackZ/SDAI-KMP/blob/master/LICENSE"

    /**
     * AUTOMATIC1111 setup documentation for configuring a compatible server.
     *
     * @author Dmitriy Moroz
     */
    override val setupInstructionsUrl: String =
        "https://github.com/AUTOMATIC1111/stable-diffusion-webui/wiki"

    /**
     * SwarmUI documentation for configuring a compatible generation server.
     *
     * @author Dmitriy Moroz
     */
    override val swarmUiInfoUrl: String =
        "https://github.com/mcmonkeyprojects/SwarmUI/tree/master/docs"

    /**
     * Endpoint used when provider setup selects the demonstration server.
     *
     * @author Dmitriy Moroz
     */
    override val demoModeUrl: String = "https://sdai.moroz.cc"

    /**
     * SDAI Telegram channel for project updates and announcements.
     *
     * @author Dmitriy Moroz
     */
    override val telegramCommunityUrl: String = "https://t.me/sdai_app"

    /**
     * SDAI Discord invitation for community discussion and support.
     *
     * @author Dmitriy Moroz
     */
    override val discordCommunityUrl: String = "https://discord.gg/jzdR9m8Ves"

    /**
     * Public SDAI LinkedIn page opened by the shared Settings community section.
     *
     * @author Dmitriy Moroz
     */
    override val linkedInCommunityUrl: String = "https://www.linkedin.com/company/sdai-app/"
}
