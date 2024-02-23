package su.arlet.selectelback.services

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import su.arlet.selectelback.core.Tokens
import su.arlet.selectelback.core.User
import su.arlet.selectelback.repos.TokenRepo
import su.arlet.selectelback.repos.UserRepo
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


@Component
class AuthService @Autowired constructor (
    val tokenRepo: TokenRepo,
    val userRepo : UserRepo,
) {
    private val key = Jwts.SIG.HS256.key().build() // todo: stable key

    fun getAccessToken(request: HttpServletRequest) : String {
        return request.getHeader("Authorization").replace("bearer ", "", true) // todo: fix it
    }

    fun getUserID(request: HttpServletRequest) : Long {
        return getUserID(getAccessToken(request))
    }
    fun getUserID(accessToken: String) : Long {
        val tokenInfo = verifyToken(accessToken)

        return tokenInfo.parseSignedClaims(accessToken).payload.subject.toLong()
    }

    fun verifyToken(token: String) : JwtParser {
        try {
            return Jwts.parser().verifyWith(key).build()
        } catch (e: JwtException) {
            throw RuntimeException()
        }
    }

    fun refreshToken(accessToken: String, refreshToken : String): Pair<String, String> {
        verifyToken(accessToken)
        verifyToken(refreshToken)

        val userID = getUserID(accessToken)

        return Pair(createAccessToken(userID), createRefreshToken(userID))
    }

    fun createAccessToken(userID : Long) : String {
        return Jwts.builder()
            .subject(userID.toString())
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(60 * 60 * 60)))
            .signWith(key)
            .compact()
    }

    fun createRefreshToken(userID: Long) : String {
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

    fun register(login : String, email : String, password : String) : Pair<String, String> {
        if (userRepo.existsUserByLoginOrEmail(login, email)) {
            throw RuntimeException("user exists") // todo: change
        }

        val entity = userRepo.save(User(
               created = LocalDate.now(),
            email = email,
            login = login,
            passwordHash = password,
            lastActive = LocalDateTime.now(),
        ))

        val accessToken = createAccessToken(entity.id)
        val refreshToken = createRefreshToken(entity.id)

        tokenRepo.save(Tokens(
            userID = entity.id,
            accessToken = accessToken,
            refreshToken = refreshToken,
        ))

        return Pair(accessToken, refreshToken)
    }

    fun hashPassword(password: String) : String {
        return password
    }

    fun login(loginOrEmail : String, password : String) : Pair<String, String> {
        val user =
        if (userRepo.existsUserByLogin(loginOrEmail)) {
            userRepo.getUserByLogin(loginOrEmail)
        } else if (userRepo.existsUserByEmail(loginOrEmail)) {
            userRepo.getUserByEmail(loginOrEmail)
        } else {
            throw RuntimeException("user is not existed") // todo: change
        }

        if (hashPassword(password) != user.passwordHash) {
            throw RuntimeException("incorrectPassport") // todo: change
        }

        val accessToken = createAccessToken(user.id)
        val refreshToken = createRefreshToken(user.id)

        tokenRepo.save(Tokens(
            userID = user.id,
            accessToken = accessToken,
            refreshToken = refreshToken,
        ))

        return Pair(accessToken, refreshToken)
    }
}