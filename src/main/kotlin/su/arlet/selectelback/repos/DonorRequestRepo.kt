package su.arlet.selectelback.repos

import org.springframework.data.repository.CrudRepository
import su.arlet.selectelback.core.DonorRequest

interface DonorRequestRepo : CrudRepository<DonorRequest, Long>