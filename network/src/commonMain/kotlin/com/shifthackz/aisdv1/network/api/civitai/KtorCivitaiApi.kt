package com.shifthackz.aisdv1.network.api.civitai

import io.ktor.client.request.forms.FormDataContent
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.recordUsageBytes
import com.shifthackz.aisdv1.network.client.setTrackedTextBody
import com.shifthackz.aisdv1.network.client.trackUsage
import com.shifthackz.aisdv1.network.client.trackedBodyAsText
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiAccountResponse
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiTokenResponse
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiUploadResponse
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiPostRequest
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiPostResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.intOrNull

/**
 * Implements Civitai's official presign → object PUT → atomic post.createWithImages protocol.
 * The isolated client has no logger, auth defaults, redirects or retry plugin.
 *
 * @param client Dedicated native HTTP client supplied by the composition root.
 * @param clientId Public mobile client ID supplied by shared build configuration.
 * @param redirectUri Exact registered native OAuth callback URI.
 * @author Dmitriy Moroz
 */
internal class KtorCivitaiApi(
    private val client: HttpClient,
    private val clientId: String,
    private val redirectUri: String,
) : CivitaiApi {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    /**
     * Exchanges a one-time authorization code with the registered public client's PKCE verifier.
     *
     * @param code Civitai authorization code.
     * @param verifier Original browser-session PKCE verifier.
     * @author Dmitriy Moroz
     */
    override suspend fun exchange(code: String, verifier: String): CivitaiTokenResponse = token(
        parameters {
            append("grant_type", "authorization_code")
            append("code", code)
            append("code_verifier", verifier)
            append("redirect_uri", redirectUri)
        }
    )

    /**
     * Rotates the saved refresh token
     * old tokens become invalid on success.
     *
     * @param refreshToken Most recently committed refresh token.
     * @author Dmitriy Moroz
     */
    override suspend fun refresh(refreshToken: String): CivitaiTokenResponse = token(
        parameters {
            append("grant_type", "refresh_token")
            append("refresh_token", refreshToken)
        }
    )

    /**
     * Revokes this application's refresh token and its access tokens.
     *
     * @param refreshToken Current saved refresh token.
     * @author Dmitriy Moroz
     */
    override suspend fun revoke(refreshToken: String) {
        val response =
            submitTrackedForm("https://auth.civitai.com/api/auth/oauth/revoke", parameters {
                append("token", refreshToken)
                append("token_type_hint", "refresh_token")
                append("client_id", clientId)
            })
        response.trackedBodyAsText(NetworkUsageCategory.CIVITAI)
        check(response.status.isSuccess()) { "CIVITAI_REVOKE_FAILED" }
    }

    /**
     * Calls the fixed OAuth token endpoint without body logging, redirects or retries.
     *
     * @param fields Complete grant-specific form fields.
     * @author Dmitriy Moroz
     */
    private suspend fun token(fields: Parameters): CivitaiTokenResponse {
        val response =
            submitTrackedForm("https://auth.civitai.com/api/auth/oauth/token", Parameters.build {
                appendAll(fields)
                append("client_id", clientId)
            })
        val body = response.trackedBodyAsText(NetworkUsageCategory.CIVITAI)
        check(response.status.isSuccess()) { "CIVITAI_TOKEN_FAILED" }
        val tokens = json.decodeFromString<CivitaiTokenResponse>(body)
        check(tokens.accessToken.isNotBlank() && tokens.refreshToken?.isNotBlank() != false && tokens.expiresIn > 0)
        return tokens
    }

    /**
     * Counts encoded OAuth form bytes without logging or retaining credential values.
     *
     * @param destination Fixed Civitai authentication endpoint selected by the caller.
     * @param fields Complete grant or revocation form, including the registered public client.
     * @author Dmitriy Moroz
     */
    private suspend fun submitTrackedForm(destination: String, fields: Parameters): HttpResponse {
        val form = FormDataContent(fields)
        recordUsageBytes(NetworkUsageCategory.CIVITAI, form.bytes().size.toLong())
        return client.post(destination) {
            trackUsage(NetworkUsageCategory.CIVITAI)
            setBody(form)
        }
    }

    /**
     * Resolves the identity authorized by a user key.
     *
     * @param key Civitai user API key.
     * @author Dmitriy Moroz
     */
    override suspend fun account(key: String): CivitaiAccountResponse {
        val value = trpc("user.getSelfStatus", key, JsonNull, true)
        val id =
            value.jsonObject["id"]?.jsonPrimitive?.longOrNull ?: error("CIVITAI_INVALID_ACCOUNT")
        val name =
            value.jsonObject["username"]?.jsonPrimitive?.content ?: error("CIVITAI_INVALID_ACCOUNT")
        return CivitaiAccountResponse(id, name)
    }

    /**
     * Allocates an object-upload URL.
     *
     * @param key User API key or OAuth access token.
     * @author Dmitriy Moroz
     */
    override suspend fun presign(key: String): CivitaiUploadResponse {
        val response = client.post("https://civitai.com/api/v1/image-upload") {
            bearerAuth(key)
            setTrackedTextBody(NetworkUsageCategory.CIVITAI, "{}")
        }
        val body = response.trackedBodyAsText(NetworkUsageCategory.CIVITAI)
        check(response.status.isSuccess()) { "CIVITAI_HTTP_${response.status.value}" }
        return json.decodeFromString<CivitaiUploadResponse>(body)
    }

    /**
     * Sends original output bytes without bearer credentials.
     *
     * @param destination Short-lived HTTPS object-upload URL returned by Civitai.
     * @param bytes Original encoded output bytes.
     * @param mime Actual encoded-image MIME type.
     * @author Dmitriy Moroz
     */
    override suspend fun upload(destination: String, bytes: ByteArray, mime: String) {
        val url = Url(destination)
        require(url.protocol == URLProtocol.HTTPS && url.user.isNullOrEmpty() && url.password.isNullOrEmpty())
        recordUsageBytes(NetworkUsageCategory.CIVITAI, bytes.size.toLong())
        val response = client.put(url) {
            trackUsage(NetworkUsageCategory.CIVITAI)
            contentType(ContentType.parse(mime))
            setBody(bytes)
        }
        response.trackedBodyAsText(NetworkUsageCategory.CIVITAI)
        check(response.status.isSuccess()) { "CIVITAI_UPLOAD_FAILED" }
    }

    /**
     * Creates and publishes one post without automatic retries.
     *
     * @param key User API key or OAuth access token.
     * @param request Ordered uploaded image and visible attribution.
     * @author Dmitriy Moroz
     */
    override suspend fun createPost(key: String, request: CivitaiPostRequest): CivitaiPostResponse {
        val body = json.encodeToJsonElement(CivitaiPostRequest.serializer(), request)
        val result = trpc("post.createWithImages", key, body, false)
        return json.decodeFromJsonElement(CivitaiPostResponse.serializer(), result)
    }

    /**
     * Reads either current superjson or devalue response envelopes from Civitai's migrating pools.
     *
     * @param procedure Fixed Civitai procedure name.
     * @param key User key, sent only to the API origin.
     * @param input Known procedure payload.
     * @param isRead Whether the procedure is a GET query.
     * @author Dmitriy Moroz
     */
    private suspend fun trpc(
        procedure: String,
        key: String,
        input: JsonElement,
        isRead: Boolean
    ): JsonElement {
        val wrapped = buildJsonObject { put("json", input) }.toString()
        val response = client.request("https://civitai.com/api/trpc/$procedure") {
            method = if (isRead) HttpMethod.Get else HttpMethod.Post
            bearerAuth(key)
            contentType(ContentType.Application.Json)
            trackUsage(NetworkUsageCategory.CIVITAI)
            if (isRead) parameter(
                "input",
                wrapped
            ) else setTrackedTextBody(NetworkUsageCategory.CIVITAI, wrapped)
        }
        val body = response.trackedBodyAsText(NetworkUsageCategory.CIVITAI)
        check(response.status.isSuccess()) { "CIVITAI_HTTP_${response.status.value}" }
        val envelope = json.parseToJsonElement(body).jsonObject
        check("error" !in envelope) { "CIVITAI_REQUEST_FAILED" }
        val payload = envelope.getValue("result").jsonObject.getValue("data")
        if (payload is JsonObject) return payload["json"] ?: payload
        val flattened = json.parseToJsonElement(payload.jsonPrimitive.content).jsonArray
        val row = flattened.first().jsonObject
        return buildJsonObject {
            // These procedures only consume primitive top-level identity fields.
            for ((name, reference) in row) {
                val index = reference.jsonPrimitive.intOrNull ?: continue
                if (index in flattened.indices && flattened[index] is JsonPrimitive) put(
                    name,
                    flattened[index]
                )
            }
        }
    }
}
