package app.wefridge.wefridge.exceptions

// This code was partially taken (and modified) from: https://stackoverflow.com/questions/45162869/kotlin-throw-custom-exception
class ItemOwnerMissingException: Exception {
    constructor() : super("Owner field is null.")
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)

}