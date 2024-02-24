package su.arlet.selectelback.services

import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class PostRequestService(private val restTemplate: RestTemplate) {

    fun sendPostRequest(url: String, requestBody: Any): JSONObject {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val requestEntity = HttpEntity(requestBody, headers)
        val responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String::class.java)

        println("Get response from VK: ${responseEntity.body}")

        return JSONObject(responseEntity.body ?: throw IllegalStateException("Empty response body"))
    }
}
