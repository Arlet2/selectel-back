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
    private val VK_APPROVE_URL = "https://api.vk.com/method/auth.exchangeSilentAuthToken"
    private val VK_GET_INFO_URL = "https://api.vk.com/method/users.get"
    private val ACCESS_TOKEN: String = "983ca070983ca070983ca070519b2bff159983c983ca070fde428633e93e2d7d7ded22d"


    fun approveVkLogin(token: String, uuid: String): JSONObject {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val requestBody = "v=5.131&token=${token}&access_token=${ACCESS_TOKEN}&uuid=${uuid}"
        val requestEntity = HttpEntity(requestBody, headers)

        val responseEntity = restTemplate.exchange(VK_APPROVE_URL, HttpMethod.POST, requestEntity, String::class.java)

        val response = JSONObject(responseEntity.body ?: throw IllegalStateException("Empty response body"))

        if (!response.has("response"))
            throw IllegalStateException("Response data not found, messsage from VK: ${response["error"]}")

        return response.getJSONObject("response")
    }

    fun getUserInfo(userToken: String, vkUserId: String): JSONObject {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val requestBody = "access_token=${userToken}&user_ids=${vkUserId}&fields=city"
        val requestEntity = HttpEntity(requestBody, headers)

        val responseEntity = restTemplate.exchange(VK_GET_INFO_URL, HttpMethod.POST, requestEntity, String::class.java)

        println("Get response from vk: ${responseEntity.body}")

        val response = JSONObject(responseEntity.body ?: throw IllegalStateException("Empty response body"))

        if (!response.has("response"))
            throw IllegalStateException("Response data not found, messsage from VK: $response")

        val arr = response.getJSONArray("response")

        if (arr.length() == 0)
            throw IllegalStateException("Response data not found, messsage from VK: $response")

        println("Get user arr: $arr")

        return arr.getJSONObject(0)
    }
}
