package me.realignist.springboot.router.exceptions

class ActionNotFoundException : Exception {
    var action: String? = null

    constructor(action: String, cause: Throwable): super(String.format("Action %s not found", action), cause) {
        this.action = action
    }

    constructor(action: String, message: String) : super(String.format("Action %s not found", action)) {
        this.action = action
    }

    val errorDescription: String
        get() = String.format("Action <strong>%s</strong> could not be found. Error raised is <strong>%s</strong>",
                action, if (cause is ClassNotFoundException)
            "ClassNotFound: " + cause.message else cause?.message)
}
