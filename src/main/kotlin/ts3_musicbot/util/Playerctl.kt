package ts3_musicbot.util

fun playerctl(player: String, command: String, extra: String = ""): Pair<Output, Error> {
    val commandRunner = CommandRunner()
    fun dbusGet(property: String) = commandRunner.runCommand(
        "dbus-send --print-reply --dest=org.mpris.MediaPlayer2.$player /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get string:'org.mpris.MediaPlayer2.Player' string:'$property'",
        printOutput = false, printErrors = false
    )

    fun dbusSend(method: String, data: String = "") = commandRunner.runCommand(
        "dbus-send --print-reply --dest=org.mpris.MediaPlayer2.$player /org/mpris/MediaPlayer2 org.mpris.MediaPlayer2.Player.$method" +
                if (data.isNotEmpty()) {
                    " string:'$data'"
                } else {
                    ""
                },
        printOutput = false, printErrors = false
    )

    fun parseMetadata(metadata: String): Map<String, Any> {
        val metadataMap = emptyMap<String, Any>().toMutableMap()
        val subArray = ArrayList<String>()
        var inSubArray = false
        var inEntry = false
        var key = ""
        for (line in metadata.lines()) {
            if (line.startsWith("method return time")) continue
            when {
                line.contains("^\\s+dict entry\\(".toRegex()) -> {
                    inEntry = true
                }

                line.contains("^\\s+string \"".toRegex()) -> {
                    key = line.substringAfter('"').substringBeforeLast('"')
                }

                line.contains("^\\s+variant\\s+\\S+".toRegex()) -> {
                    when (val variant = line.replace("^\\s+variant\\s+".toRegex(), "").substringBefore(' ')) {
                        "array" -> if (inEntry) inSubArray = true

                        "string" -> {
                            val str = line.substringAfter('"').substringBeforeLast('"')
                            if (inSubArray)
                                subArray.add(str)
                            else
                                metadataMap[key] = str
                        }

                        "object" -> if (line.substringAfter("$variant ").startsWith("path")) {
                            metadataMap[key] = line.replace("(^.+\\s+\"|\"$)".toRegex(), "'")
                        }

                        "uint64", "int64" -> metadataMap[key] = line.substringAfter("$variant ").toLong()
                        "double" -> metadataMap[key] = line.substringAfter("$variant ").toFloat()
                        "int32" -> metadataMap[key] = line.substringAfter("$variant ").toInt()

                        else -> println(
                            "Encountered unknown variant \"$variant\" when parsing MPRIS metadata!\n" +
                                    "Metadata:\n$metadata"
                        )
                    }
                }

                line.contains("^\\s+]$".toRegex()) -> {
                    if (inEntry) {
                        inSubArray = false
                        metadataMap[key] = subArray
                        subArray.clear()
                    }
                }

                line.contains("^\\s+\\)$".toRegex()) -> inEntry = false
            }
        }
        return metadataMap
    }
    return when (command) {
        "status" -> {
            val cmd = dbusGet("PlaybackStatus")
            Pair(
                Output(cmd.first.outputText.substringAfter('"').substringBefore('"')),
                cmd.second
            )
        }

        "metadata" -> {
            val metadata = dbusGet("Metadata")
            val formattedMetadata = StringBuilder()
            val parsedOutput = parseMetadata(metadata.first.outputText)
            val trackId = "mpris:trackid"
            val length = "mpris:length"
            val artUrl = "mpris:artUrl"
            val album = "xesam:album"
            val albumArtist = "xesam:albumArtist"
            val artist = "xesam:artist"
            val rating = "xesam:autoRating"
            val discNum = "xesam:discNumber"
            val title = "xesam:title"
            val trackNum = "xesam:trackNumber"
            val url = "xesam:url"
            if (parsedOutput.contains(trackId))
                formattedMetadata.appendLine("$player $trackId\t\t${parsedOutput[trackId]}")
            if (parsedOutput.contains(length))
                formattedMetadata.appendLine("$player $length\t\t\t${parsedOutput[length]}")
            if (parsedOutput.contains(artUrl))
                formattedMetadata.appendLine("$player $artUrl\t\t\t${parsedOutput[artUrl]}")
            if (parsedOutput.contains(album))
                formattedMetadata.appendLine("$player $album\t\t\t${parsedOutput[album]}")
            if (parsedOutput.contains(albumArtist))
                formattedMetadata.appendLine("$player $albumArtist\t${
                    parsedOutput[albumArtist].let { if (it is List<*>) it.joinToString() else "" }
                }")
            if (parsedOutput.contains(artist))
                formattedMetadata.appendLine("$player $artist\t\t${
                    parsedOutput[artist].let { if (it is List<*>) it.joinToString() else "" }
                }")
            if (parsedOutput.contains(rating))
                formattedMetadata.appendLine("$player $rating\t\t${parsedOutput[rating]}")
            if (parsedOutput.contains(discNum))
                formattedMetadata.appendLine("$player $discNum\t\t${parsedOutput[discNum]}")
            if (parsedOutput.contains(title))
                formattedMetadata.appendLine("$player $title\t\t\t${parsedOutput[title]}")
            if (parsedOutput.contains(trackNum))
                formattedMetadata.appendLine("$player $trackNum\t${parsedOutput[trackNum]}")
            if (parsedOutput.contains(url))
                formattedMetadata.appendLine("$player $url\t\t\t${parsedOutput[url]}")
            Pair(Output(formattedMetadata.toString()), metadata.second)
        }

        "stop" -> {
            dbusSend("Stop")
        }

        "pause" -> {
            dbusSend("Pause")
        }

        "play" -> {
            dbusSend("Play")
        }

        "open" -> {
            dbusSend("OpenUri", extra)
        }

        "next" -> {
            dbusSend("Next")
        }

        else -> Pair(Output(""), Error(""))
    }
}
