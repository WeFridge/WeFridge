package app.wefridge.wefridge.exceptions

class InternetUnavailableException: Exception {
    constructor() : super("Internet unavailable")
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}