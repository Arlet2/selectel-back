package su.arlet.selectelback.controllers.filters

import org.springframework.stereotype.Component
@Component
class RangeFilter {
    fun <T : Comparable<T>> inRange(value: T?, greater: T?, greaterEq: T?, less: T?, lessEq: T?, eq: T?): Boolean {
        if (value != null) {
            if (greater != null)
                if (value <= greater)
                    return false
            if (greaterEq != null)
                if (value < greaterEq)
                    return false
            if (less != null)
                if (value >= less)
                    return false
            if (lessEq != null)
                if (value < lessEq)
                    return false
            if (eq != null)
                if (value != eq)
                    return false
            return true
        }

        return greater == null && greaterEq == null && less == null && lessEq == null && eq == null
    }

    fun <T> equal(value: T?, eq: T?): Boolean {
        if (eq != null)
            return eq == value

        return true
    }
}