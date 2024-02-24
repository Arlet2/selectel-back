package su.arlet.selectelback.repos;

import org.springframework.data.repository.CrudRepository
import su.arlet.selectelback.core.UnavailableDates

interface UnavailableDatesRepo : CrudRepository<UnavailableDates, Long>