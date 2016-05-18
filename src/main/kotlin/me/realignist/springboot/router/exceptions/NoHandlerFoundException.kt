package me.realignist.springboot.router.exceptions

class NoHandlerFoundException(action: String, args: Map<String, Any>) : RuntimeException("No handler found") {
    var action: String
    var args: Map<String, Any>

    init {
        this.action = action
        this.args = args
    }

    override fun toString(): String {
        return this.message + " action [" + this.action + "] args [" + this.args + "]"
    }
}
