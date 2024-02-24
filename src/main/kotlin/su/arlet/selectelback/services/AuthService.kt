package su.arlet.selectelback.services

import at.favre.lib.crypto.bcrypt.BCrypt
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import su.arlet.selectelback.core.Tokens
import su.arlet.selectelback.core.User
import su.arlet.selectelback.exceptions.IncorrectPasswordError
import su.arlet.selectelback.exceptions.UnauthorizedError
import su.arlet.selectelback.exceptions.UserExistsError
import su.arlet.selectelback.exceptions.UserNotFoundError
import su.arlet.selectelback.repos.TokenRepo
import su.arlet.selectelback.repos.UserRepo
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.io.path.Path

val staticFilesPath = Path("static", "images")

@Component
class AuthService @Autowired constructor(
    private val tokenRepo: TokenRepo,
    private val userRepo: UserRepo,
) {
    private val PASSWORD_HASH_COST = 12
    private val key =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode("qwerty1234567aAbada241312dfabasdjfajoifdasighiaiu249812y31hrkqh1k4jh12ued8c7hjkfjkagh782h4faiufhq87hq8aghq172h8hfaw87fgha8gha78rqufhq972h41uihdq87w6gf1wr1h872rh8f7h172rh872rh1f2"));
    private val defaultAvatarURL = "https://petdonor.ru/avatar/default-avatar-user.jpg"

    fun getAccessToken(request: HttpServletRequest): String {
        return request.getHeader("Authorization")?.replace("bearer ", "", true) ?: ""
    }

    fun getUserID(request: HttpServletRequest): Long {
        return getUserID(getAccessToken(request))
    }

    fun getUserID(accessToken: String): Long {
        val tokenInfo = verifyToken(accessToken)

        return tokenInfo.subject.toLong()
    }

    fun verifyToken(token: String): Claims {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        } catch (e: JwtException) {
            throw UnauthorizedError()
        }
    }

    fun verifyToken(request: HttpServletRequest): Claims {
        return verifyToken(getAccessToken(request))
    }

    fun refreshToken(accessToken: String, refreshToken: String): Pair<String, String> {
        verifyToken(accessToken)
        verifyToken(refreshToken)

        val userID = getUserID(accessToken)

        return Pair(createAccessToken(userID), createRefreshToken(userID))
    }

    private fun createAccessToken(userID: Long): String {
        return Jwts.builder()
            .subject(userID.toString())
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(60 * 60 * 60)))
            .signWith(key)
            .compact()
    }

    private fun createRefreshToken(userID: Long): String {
        return Jwts.builder()
            .subject(userID.toString())
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(60 * 60 * 60)))
            .signWith(key)
            .compact()
    }

    fun removeTokens(userID: Long) {
        tokenRepo.deleteById(userID)
    }

    fun register(login: String, email: String, password: String): Pair<String, String> {
        if (userRepo.existsUserByLoginOrEmail(login, email)) {
            throw UserExistsError()
        }

        val entity = userRepo.save(
            User(
                created = LocalDate.now(),
                email = email,
                login = login,
                passwordHash = hashPassword(password),
                lastActive = LocalDateTime.now(),
                avatar = defaultAvatarURL,
            )
        )

        return createTokenById(entity.id)
    }

    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(PASSWORD_HASH_COST, password.toCharArray())
    }

    fun passwordsEquals(password: String, passwordHash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), passwordHash).verified
    }

    fun login(loginOrEmail: String, password: String): Pair<String, String> {
        val user =
            if (userRepo.existsUserByLogin(loginOrEmail)) {
                userRepo.getUserByLogin(loginOrEmail)
            } else if (userRepo.existsUserByEmail(loginOrEmail)) {
                userRepo.getUserByEmail(loginOrEmail)
            } else {
                throw UserNotFoundError()
            }

        if (user.passwordHash == null || !passwordsEquals(password, user.passwordHash!!)) {
            throw IncorrectPasswordError()
        }

        return createTokenById(user.id)
    }

    fun createTokenById(userId: Long): Pair<String, String> {
        val accessToken = createAccessToken(userId)
        val refreshToken = createRefreshToken(userId)

        tokenRepo.save(
            Tokens(
                userID = userId,
                accessToken = accessToken,
                refreshToken = refreshToken,
            )
        )

        return Pair(accessToken, refreshToken)
    }
}