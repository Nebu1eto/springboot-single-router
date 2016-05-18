package me.realignist.springboot.router.exceptions

class NoRouteFoundException(var method: String, var path: String): RuntimeException("No route found") {
    override fun toString(): String {
        return this.message + " method[" + this.method + "] path[" + this.path + "]"
    }
}
