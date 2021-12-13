package app.wefridge.wefridge.exceptions

class ItemIsSharedWithoutContactEmailException: Exception {
    constructor() : super("The Item was set to 'is shared' though without a contact email.")
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}