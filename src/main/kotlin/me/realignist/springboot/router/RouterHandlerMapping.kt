package me.realignist.springboot.router

import me.realignist.springboot.router.exceptions.NoRouteFoundException
import me.realignist.springboot.router.exceptions.RouteFileParsingException
import me.realignist.springboot.router.support.RouterHandlerResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.core.io.Resource
import org.springframework.stereotype.Controller
import org.springframework.util.Assert
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.handler.AbstractHandlerMapping

import javax.servlet.http.HttpServletRequest
import java.io.IOException
import java.util.ArrayList
import java.util.Arrays

class RouterHandlerMapping : AbstractHandlerMapping() {
    var routeFiles: List<String>? = null
    var isAutoReloadEnabled = false
    private val methodResolver: RouterHandlerResolver

    init {
        this.methodResolver = RouterHandlerResolver()
    }

    fun reloadRoutesConfiguration () {
        val fileResources = ArrayList<Resource>()

        try {
            for (fileName in this.routeFiles!!)
                fileResources.addAll(Arrays.asList(*applicationContext.getResources(fileName)))

            Router.detectChanges(fileResources)
        } catch (ex: IOException) {
            throw RouteFileParsingException("Could not read route configuration files", ex)
        }
    }

    @Throws(BeansException::class)
    override fun initApplicationContext () {
        super.initApplicationContext()

        this.methodResolver.setCachedControllers(applicationContext.getBeansWithAnnotation(Controller::class.java))
        val fileResources = ArrayList<Resource>()

        try {
            for (fileName in this.routeFiles!!)
                fileResources.addAll(Arrays.asList(*applicationContext.getResources(fileName)))

            Router.load(fileResources)
        } catch (e: IOException) {
            throw RouteFileParsingException("Could not read route configuration files", e)
        }
    }

    @Throws(Exception::class)
    override fun getHandlerInternal (request: HttpServletRequest): Any {
        val handler: HandlerMethod?
        if (this.isAutoReloadEnabled) this.reloadRoutesConfiguration()

        try {
            val rq = HTTPRequestAdapter.parseRequest(request)
            val route = Router.route(rq)
            logger.debug(String.format("Looking up handler method for path %s (%s %s %s)", route.path, route.method, route.path, route.action))
            handler = this.methodResolver.resolveHandler(route, rq.action, rq)
            request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, rq.routeArgs)
            request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, route.pattern.toString())
        } catch (nrfe: NoRouteFoundException) {
            handler = null
            logger.trace("no route found for method[" + nrfe.method + "] and path[" + nrfe.path + "]")
        }

        return handler!!
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RouterHandlerMapping::class.java)
    }
}
