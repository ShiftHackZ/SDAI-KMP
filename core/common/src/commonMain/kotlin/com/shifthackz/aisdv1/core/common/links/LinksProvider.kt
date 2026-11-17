package com.shifthackz.aisdv1.core.common.links

/**
 * Supplies absolute web destinations for provider setup, project information and community actions.
 * Platform composition roots may override these URLs for their build configuration.
 *
 * @author Dmitriy Moroz
 */
interface LinksProvider {
    /**
     * AI Horde service home page for learning about crowdsourced generation.
     *
     * @author Dmitriy Moroz
     */
    val hordeUrl: String

    /**
     * AI Horde registration page for obtaining an account and API key.
     *
     * @author Dmitriy Moroz
     */
    val hordeSignUpUrl: String

    /**
     * Hugging Face inference documentation for configuring a hosted provider.
     *
     * @author Dmitriy Moroz
     */
    val huggingFaceUrl: String

    /**
     * OpenAI API key management page used during provider setup.
     *
     * @author Dmitriy Moroz
     */
    val openAiInfoUrl: String

    /**
     * Stability AI platform page for managing hosted generation access.
     *
     * @author Dmitriy Moroz
     */
    val stabilityAiInfoUrl: String

    /**
     * Fal.ai dashboard for creating and managing generation API keys.
     *
     * @author Dmitriy Moroz
     */
    val falAiInfoUrl: String

    /**
     * ArliAI quick-start documentation for configuring its image API.
     *
     * @author Dmitriy Moroz
     */
    val arliAiInfoUrl: String

    /**
     * SDAI privacy policy opened from Settings and onboarding.
     *
     * @author Dmitriy Moroz
     */
    val privacyPolicyUrl: String

    /**
     * SDAI support page listing ways to contribute to project development.
     *
     * @author Dmitriy Moroz
     */
    val donateUrl: String

    /**
     * SDAI home page with downloads, provider information and community links.
     *
     * @author Dmitriy Moroz
     */
    val projectWebsiteUrl: String

    /**
     * Developer home page linked from the Settings community section.
     *
     * @author Dmitriy Moroz
     */
    val developerWebsiteUrl: String

    /**
     * Canonical SDAI source repository for browsing code and contributing.
     *
     * @author Dmitriy Moroz
     */
    val gitHubSourceUrl: String

    /**
     * License text in the canonical SDAI source repository.
     *
     * @author Dmitriy Moroz
     */
    val licenseUrl: String

    /**
     * AUTOMATIC1111 setup documentation for configuring a compatible server.
     *
     * @author Dmitriy Moroz
     */
    val setupInstructionsUrl: String

    /**
     * SwarmUI documentation for configuring a compatible generation server.
     *
     * @author Dmitriy Moroz
     */
    val swarmUiInfoUrl: String

    /**
     * Endpoint used when provider setup selects the demonstration server.
     *
     * @author Dmitriy Moroz
     */
    val demoModeUrl: String

    /**
     * SDAI Telegram channel for project updates and announcements.
     *
     * @author Dmitriy Moroz
     */
    val telegramCommunityUrl: String

    /**
     * SDAI Discord invitation for community discussion and support.
     *
     * @author Dmitriy Moroz
     */
    val discordCommunityUrl: String

    /**
     * Public SDAI LinkedIn page for project milestones and release announcements.
     *
     * @author Dmitriy Moroz
     */
    val linkedInCommunityUrl: String
}
