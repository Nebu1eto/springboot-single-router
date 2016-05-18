package me.realignist.springboot.router.support

import me.realignist.springboot.router.HTTPRequestAdapter
import me.realignist.springboot.router.Router
import me.realignist.springboot.router.exceptions.ActionNotFoundException

import org.slf4j.LoggerFactory

import org.springframework.aop.support.AopUtils
import org.springframework.core.BridgeMethodResolver
import org.springframework.web.method.HandlerMethod

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.LinkedHashMap

class RouterHandlerResolver {
    private val cachedControllers = LinkedHashMap<String, Any>()
    private val cachedHandlers = LinkedHashMap<String, HandlerMethod>()

    fun setCachedControllers(controllers: Map<String, Any>) {
        for (key in controllers.keys) this.cachedControllers.put(key.toLowerCase(), controllers[key]!!)
    }

    @Throws(ActionNotFoundException::class)
    fun resolveHandler(route: Router.Route, fullAction: String, req: HTTPRequestAdapter): HandlerMethod {
        val handlerMethod: HandlerMethod
        if (this.cachedHandlers.containsKey(fullAction))
            handlerMethod = this.cachedHandlers[fullAction]!!
        else {
            handlerMethod = this.doResolveHandler(route, fullAction)
            this.cachedHandlers.put(fullAction, handlerMethod)
        }

        return handlerMethod
    }

    @Throws(ActionNotFoundException::class)
    private fun doResolveHandler(route: Router.Route, fullAction: String): HandlerMethod {
        val actionMethod: Method?
        val controllerObject: Any?

        val controller = fullAction.substring(0, fullAction.lastIndexOf(".")).toLowerCase()
        val action = fullAction.substring(fullAction.lastIndexOf(".") + 1)
        controllerObject = cachedControllers[controller]

        if (controllerObject == null) {
            logger.debug("Did not find handler {} for [{} {}]", controller, route.method, route.path)
            throw ActionNotFoundException(fullAction, Exception("Controller $controller not found"))
        }

        actionMethod = findActionMethod(action, controllerObject)
        if (actionMethod == null) {
            logger.debug("Did not find handler method {}.{} for [{} {}]", controller, action, route.method, route.path)
            throw ActionNotFoundException(fullAction,
                    Exception("No method public static void $action() was found in class $controller"))
        }

        return RouterHandler(controllerObject, actionMethod, route)
    }

    private fun findActionMethod(name: String, controller: Any): Method? {
        var targetClass: Class<*>? = AopUtils.getTargetClass(controller)

        while (targetClass!!.name != "java.lang.Object") {
            for (method in targetClass.declaredMethods)
                if (method.name.equals(name, ignoreCase = true) && Modifier.isPublic(method.modifiers))
                    return BridgeMethodResolver.findBridgedMethod(method)

            targetClass = targetClass.getSuperclass()
        }

        return null
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RouterHandlerResolver::class.java)
    }
}
