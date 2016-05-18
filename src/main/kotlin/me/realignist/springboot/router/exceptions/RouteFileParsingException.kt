package me.realignist.springboot.router.exceptions

import org.springframework.beans.BeansException

class RouteFileParsingException: BeansException {
    constructor(msg: String): super(msg) {

    }

    constructor(msg: String, e: Throwable): super(msg, e) {

    }
}
