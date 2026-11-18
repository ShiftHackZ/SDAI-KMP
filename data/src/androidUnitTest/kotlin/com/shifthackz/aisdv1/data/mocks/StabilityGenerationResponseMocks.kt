package com.shifthackz.aisdv1.data.mocks

import com.shifthackz.aisdv1.network.response.StabilityGenerationResponse

val mockStabilityGenerationResponse = StabilityGenerationResponse(
    artifacts = listOf(
        StabilityGenerationResponse.Artifact(
            base64 = "base64",
            finishReason = "reasonable reason",
            seed = 5598.0,
        ),
    ),
)

val mockBadStabilityGenerationResponse = StabilityGenerationResponse(emptyList())
