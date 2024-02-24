package su.arlet.selectelback.repos

import org.springframework.data.jpa.repository.JpaRepository
import su.arlet.selectelback.core.User

interface UserRepo : JpaRepository<User, Long> {
    fun existsUserByVkUserId(vkUserId: String) : Boolean
    fun existsUserByLoginOrEmail(login: String, email: String) : Boolean
    fun existsUserByLogin(login: String): Boolean
    fun existsUserByEmail(email: String): Boolean
    fun getUserByVkUserId(vkUserId: String): User
    fun getUserByLogin(login: String): User
    fun getUserByEmail(email: String): User
}