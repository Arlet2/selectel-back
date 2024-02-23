package su.arlet.selectelback.repos

import org.springframework.data.repository.CrudRepository
import su.arlet.selectelback.core.Tokens

interface TokenRepo : CrudRepository<Tokens, Long>