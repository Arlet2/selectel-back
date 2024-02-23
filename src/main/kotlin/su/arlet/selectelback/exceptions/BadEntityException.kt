package su.arlet.selectelback.exceptions;

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class BadEntityException(msg: String) : RuntimeException(msg)