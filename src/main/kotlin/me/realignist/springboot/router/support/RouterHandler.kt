package me.realignist.springboot.router.support

import java.lang.reflect.Method
import org.springframework.web.method.HandlerMethod
import me.realignist.springboot.router.Router

class RouterHandler(bean: Any, method: Method, val route: Router.Route) : HandlerMethod(bean, method)