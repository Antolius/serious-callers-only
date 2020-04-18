package hr.from.josipantolis.seriouscallersonly.api

data class User(
    val id: String
) {
    val mention: String
        get() = "<@$id>"

    object specialMention {
        val here = "<!here|here>"
        val channel = "<!channel>"
        val everyone = "<!everyone>"
    }
}

data class Channel(
    val id: String
) {
    val mention: String
        get() = "<#$id>"
}

data class Thread(
    val id: String
)

data class Command(val cmd: String)

data class MessageText(val txt: String)

data class InputText(val txt: String)