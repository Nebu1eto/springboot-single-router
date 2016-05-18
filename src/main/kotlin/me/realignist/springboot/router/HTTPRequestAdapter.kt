package me.realignist.springboot.router

import org.slf4j.LoggerFactory
import org.springframework.util.Assert
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

import javax.servlet.http.HttpServletRequest
import java.lang.reflect.Method
import java.util.*

class HTTPRequestAdapter {
    var host: String = String()
    var path: String = String()
    var contextPath: String = String()
    var servletPath: String = String()
    var queryString: String = String()
    var url: String = String()
    var method: String = String()
    var domain: String = String()
    var remoteAddress: String = String()
    var contentType: String = String()
    var controller: String = String()
    var actionMethod: String = String()
    var port: Int? = null
    var headers: MutableMap<String, Header> = HashMap()
    var routeArgs: Map<String, String>
    var format: String = String()
    var action: String = String()
    @Transient var invokedMethod: Method? = null
    @Transient var controllerClass: Class<Any>? = null
    var args: Map<String, Any> = HashMap()
    var date = Date()
    var secure: Boolean = false

    init {
        this.headers = HashMap<String, Header>()
        this.routeArgs = HashMap<String, String>()
    }

    val base: String
        get() {
            if (port === 80 || port === 443) return String.format("%s://%s", if (secure) "https" else "http", domain).intern()
            return String.format("%s://%s:%s", if (secure) "https" else "http", domain, port).intern()
        }


    fun resolveFormat() {
        if (format === String()) {
            return
        }

        if (headers["accept"] == null) {
            format = "html"
            return
        }

        val accept = headers["accept"]?.value()

        if (accept?.contains("application/xhtml")!! || accept?.contains("text/html")!! || accept?.startsWith("*/*")!!) {
            format = "html"
            return
        }

        if (accept?.contains("application/xml")!! || accept?.contains("text/xml")!!) {
            format = "xml"
            return
        }

        if (accept?.contains("text/plain")!!) {
            format = "txt"
            return
        }

        if (accept?.contains("application/json")!! || accept?.contains("text/javascript")!!) {
            format = "json"
            return
        }

        if (accept?.endsWith("*/*")!!) {
            format = "html"
        }
    }

    inner class Header {
        var name: String = String()
        var values: List<String> = ArrayList<String>()

        fun value(): String {
            return values[0]
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(HTTPRequestAdapter::class.java)

        fun parseRequest(httpServletRequest: HttpServletRequest): HTTPRequestAdapter {
            val request = HTTPRequestAdapter()

            request.method = httpServletRequest.method.intern()
            request.path = if (httpServletRequest.pathInfo != null) httpServletRequest.pathInfo else httpServletRequest.servletPath
            request.servletPath = if (httpServletRequest.servletPath != null) httpServletRequest.servletPath else ""
            request.contextPath = if (httpServletRequest.contextPath != null) httpServletRequest.contextPath else ""
            request.queryString = if (httpServletRequest.queryString == null)
                ""
            else
                httpServletRequest.queryString

            logger.trace("contextPath: " + request.contextPath, " servletPath: " + request.servletPath)
            logger.trace("request.path: " + request.path + ", request.querystring: " + request.queryString)

            if (httpServletRequest.getHeader("Content-Type") != null)
                request.contentType = httpServletRequest.getHeader("Content-Type").split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' }.toLowerCase().intern()
            else
                request.contentType = "text/html".intern()


            if (httpServletRequest.getHeader("X-HTTP-Method-Override") != null)
                request.method = httpServletRequest.getHeader("X-HTTP-Method-Override").intern()

            request.secure = httpServletRequest.isSecure
            request.url = httpServletRequest.requestURI
            request.host = httpServletRequest.getHeader("host")

            if (request.host.contains(":")) {
                request.port = Integer.parseInt(request.host.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
                request.domain = request.host.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            } else {
                request.port = 80
                request.domain = request.host
            }

            request.remoteAddress = httpServletRequest.remoteAddr
            val headersNames = httpServletRequest.headerNames
            while (headersNames.hasMoreElements()) {
                val hd = request.Header()
                hd.name = headersNames.nextElement() as String
                hd.values = ArrayList<String>()

                val enumValues = httpServletRequest.getHeaders(hd.name)
                while (enumValues.hasMoreElements()) (hd.values as ArrayList<String>).add(enumValues.nextElement())

                request.headers.put(hd.name.toLowerCase(), hd)
            }

            request.resolveFormat()
            return request
        }

        val current: HTTPRequestAdapter
            get() {
                val requestAttributes = RequestContextHolder.currentRequestAttributes()
                Assert.notNull(requestAttributes, "Could not find current request via RequestContextHolder")
                val servletRequest = (requestAttributes as ServletRequestAttributes).request
                Assert.state(servletRequest != null, "Could not find current HttpServletRequest")
                return HTTPRequestAdapter.parseRequest(servletRequest)
            }
    }
}
