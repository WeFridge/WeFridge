package app.wefridge.wefridge.application.exceptions

class ItemIsSharedWithoutLocationException: Exception {
    constructor() : super("The Item was set to 'is shared' though without a location.")
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}