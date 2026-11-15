package com.example.data

import com.example.model.Category

object CategoriesRepository {

    const val MIN_WORD_LENGTH = 3
    const val MAX_WORD_LENGTH = 8

    val categories: List<Category> = listOf(
        // Category 1 Animals includes the exact 8 words shown in Image 1 (Level 1 Animals)
        category(1, "Animals", "\uD83D\uDC3E", listOf("CAT", "DOG", "BIRD", "FISH", "LION", "TIGER", "ELEPHANT", "KANGAROO", "BEAR", "WOLF", "FOX", "ZEBRA")),
        category(2, "Food", "\uD83C\uDF55", listOf("PIZZA", "BREAD", "RICE", "SOUP", "TACO", "PASTA", "SALAD", "CAKE", "CHEESE", "APPLE")),
        category(3, "Sports", "\u26BD", listOf("GOLF", "RUGBY", "TENNIS", "BOXING", "SKIING", "HOCKEY", "SOCCER", "JUDO", "RACING", "POLO")),
        category(4, "Countries", "\uD83C\uDF0D", listOf("INDIA", "JAPAN", "CHINA", "SPAIN", "FRANCE", "BRAZIL", "CHILE", "EGYPT", "KENYA", "NEPAL")),
        category(5, "Colors", "\uD83C\uDFA8", listOf("RED", "BLUE", "GREEN", "PINK", "BLACK", "WHITE", "BROWN", "GOLD", "GRAY", "ORANGE")),
        category(6, "Fruits", "\uD83C\uDF4E", listOf("APPLE", "MANGO", "GRAPE", "LEMON", "PEACH", "PLUM", "KIWI", "PEAR", "GUAVA", "MELON")),
        category(7, "Space", "\uD83D\uDE80", listOf("MOON", "STAR", "SUN", "MARS", "EARTH", "COMET", "ORBIT", "VENUS", "NOVA", "SOLAR")),
        category(8, "Ocean Life", "\uD83C\uDF0A", listOf("SHARK", "WHALE", "CRAB", "CORAL", "SQUID", "SEAL", "OTTER", "TUNA", "CLAM", "EEL")),
        category(9, "Music", "\uD83C\uDFB5", listOf("PIANO", "DRUMS", "GUITAR", "FLUTE", "BASS", "JAZZ", "ROCK", "OPERA", "CHORD", "TEMPO")),
        category(10, "Movies", "\uD83C\uDFAC", listOf("ACTOR", "SCENE", "DRAMA", "COMEDY", "ACTION", "HORROR", "FILM", "STAR", "CAST", "PLOT")),
        category(11, "Nature", "\uD83C\uDF3F", listOf("RIVER", "FOREST", "OCEAN", "DESERT", "JUNGLE", "VALLEY", "ISLAND", "MARSH", "PLANT", "STONE")),
        category(12, "Technology", "\uD83D\uDCBB", listOf("PHONE", "ROBOT", "TABLET", "SCREEN", "MOUSE", "CHIP", "LASER", "MODEM", "PIXEL", "CODE")),
        category(13, "Jobs", "\uD83D\uDC54", listOf("DOCTOR", "TEACHER", "PILOT", "CHEF", "NURSE", "FARMER", "ACTOR", "WRITER", "JUDGE", "ARTIST")),
        category(14, "Clothing", "\uD83D\uDC57", listOf("SHIRT", "PANTS", "DRESS", "SHOES", "SOCKS", "HAT", "SCARF", "COAT", "SKIRT", "BOOTS")),
        category(15, "Vegetables", "\uD83E\uDD55", listOf("CARROT", "POTATO", "TOMATO", "ONION", "GARLIC", "CORN", "BEANS", "PEAS", "CHILLI", "RADISH")),
        category(16, "Transport", "\uD83D\uDE97", listOf("TRAIN", "PLANE", "TRUCK", "CYCLE", "SCOOTER", "BOAT", "SHIP", "TAXI", "BUS", "METRO")),
        category(17, "Weather", "\u26C5", listOf("CLOUD", "RAIN", "STORM", "WIND", "FOG", "SNOW", "SUNNY", "FROST", "THUNDER", "BREEZE")),
        category(18, "School", "\uD83D\uDCDA", listOf("BOOK", "PENCIL", "PAPER", "CLASS", "DESK", "RULER", "ERASER", "BOARD", "EXAM", "TEACHER")),
        category(19, "Home", "\uD83C\uDFE0", listOf("TABLE", "CHAIR", "SOFA", "BED", "LAMP", "DOOR", "WINDOW", "CLOCK", "SHELF", "PLATE")),
        category(20, "Insects", "\uD83E\uDD8B", listOf("ANT", "BEE", "FLY", "MOTH", "WASP", "BEETLE", "CRICKET", "MANTIS", "TERMITE", "SPIDER")),
        category(21, "Body Parts", "\uD83E\uDDE0", listOf("ARM", "LEG", "HAND", "FOOT", "HEAD", "EYES", "NOSE", "HEART", "BRAIN", "TEETH")),
        category(22, "Emotions", "\uD83D\uDE42", listOf("HAPPY", "JOY", "ANGRY", "CALM", "PROUD", "SHY", "BRAVE", "PEACE", "WORRY", "EXCITED")),
        category(23, "Sea & Beach", "\uD83C\uDFD6\uFE0F", listOf("SAND", "WAVE", "SHELL", "BEACH", "TIDE", "COAST", "DUNE", "SURF", "SHORE", "PEARL")),
        category(24, "Gadgets", "\uD83D\uDD0C", listOf("PHONE", "WATCH", "RADIO", "CAMERA", "SPEAKER", "DRONE", "LAPTOP", "MOUSE", "ROUTER", "CABLE")),
        category(25, "Flowers", "\uD83C\uDF38", listOf("ROSE", "LILY", "TULIP", "DAISY", "LOTUS", "JASMINE", "POPPY", "IRIS", "ORCHID", "VIOLET")),
    )

    private fun category(id: Int, name: String, emoji: String, rawWords: List<String>): Category =
        Category(
            id = id,
            name = name,
            emoji = emoji,
            words = validateWords(rawWords),
        )

    fun validateWords(rawWords: List<String>): List<String> =
        rawWords
            .asSequence()
            .map { it.trim().uppercase() }
            .filter { word ->
                word.length in MIN_WORD_LENGTH..MAX_WORD_LENGTH &&
                    word.all { ch -> ch in 'A'..'Z' }
            }
            .distinct()
            .toList()

    fun getById(categoryId: Int): Category =
        categories.firstOrNull { it.id == categoryId } ?: categories.first()
}
