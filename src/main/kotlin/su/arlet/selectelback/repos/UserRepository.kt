package su.arlet.selectelback.repos

import org.springframework.data.jpa.repository.JpaRepository
import su.arlet.selectelback.core.User

interface UserRepository : JpaRepository<User, Long> {}
