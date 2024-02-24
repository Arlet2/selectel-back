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
    private val VK_URL = "https://api.vk.com/method/auth.exchangeSilentAuthToken"
    private val ACCESS_TOKEN: String = "983ca070983ca070983ca070519b2bff159983c983ca070fde428633e93e2d7d7ded22d"

    fun sendVkPostRequest(token: String, uuid: String): JSONObject {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val requestBody = "v=5.131&token=${token}&access_token=${ACCESS_TOKEN}&uuid=${uuid}"
        val requestEntity = HttpEntity(requestBody, headers)

        println("Send request: ${requestEntity.body}")

        val responseEntity = restTemplate.exchange(VK_URL, HttpMethod.POST, requestEntity, String::class.java)
        println("Get response from VK: ${responseEntity.body}")

        val response = JSONObject(responseEntity.body ?: throw IllegalStateException("Empty response body"))

        if (!response.has("response"))
            throw IllegalStateException("Response data not found, messsage from VK: ${response["error"]}")

        return JSONObject(response["response"].toString())
    }
}
