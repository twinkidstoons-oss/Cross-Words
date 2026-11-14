package com.example.data

/**
 * Curated offline dictionary of common 3-5 letter English words for Bonus Word detection.
 * Lookup is O(1) via HashSet and requires zero network requests.
 */
object BonusDictionary {

    val words: Set<String> by lazy {
        rawWordList
            .asSequence()
            .map { it.trim().uppercase() }
            .filter { it.length >= 3 && it.all { ch -> ch in 'A'..'Z' } }
            .toHashSet()
    }

    fun isBonusWord(word: String, puzzleTargetWords: Set<String>): Boolean {
        val normalized = word.trim().uppercase()
        if (normalized.length < 3) return false
        if (normalized in puzzleTargetWords) return false
        return normalized in words
    }

    private val rawWordList = listOf(
        // 3-letter common English words
        "ACE", "ACT", "ADD", "AGE", "AGO", "AID", "AIM", "AIR", "ALE", "ALL",
        "ALP", "AMP", "AND", "ANT", "ANY", "APE", "APT", "ARC", "ARE", "ARK",
        "ARM", "ART", "ASH", "ASK", "ATE", "AWE", "AXE", "BAD", "BAG", "BAN",
        "BAR", "BAT", "BAY", "BED", "BEE", "BEG", "BET", "BIB", "BID", "BIG",
        "BIN", "BIT", "BOA", "BOB", "BOG", "BOO", "BOW", "BOX", "BOY", "BRA",
        "BUD", "BUG", "BUN", "BUS", "BUT", "BUY", "BYE", "CAB", "CAD", "CAM",
        "CAN", "CAP", "CAR", "CAT", "COB", "COD", "COG", "COP", "COT", "COW",
        "COY", "CRY", "CUB", "CUD", "CUE", "CUP", "CUR", "CUT", "DAB", "DAD",
        "DAM", "DAY", "DEN", "DEW", "DID", "DIE", "DIG", "DIM", "DIN", "DIP",
        "DOC", "DOE", "DOG", "DON", "DOT", "DRY", "DUB", "DUD", "DUE", "DUG",
        "DUH", "DUN", "DYE", "EAR", "EAT", "EBB", "EEL", "EGG", "EGO", "ELK",
        "ELM", "END", "ERA", "ERR", "EVE", "EWE", "EYE", "FAB", "FAD", "FAN",
        "FAR", "FAT", "FAX", "FED", "FEE", "FEN", "FEW", "FIB", "FIG", "FIN",
        "FIR", "FIT", "FIX", "FLU", "FLY", "FOB", "FOE", "FOG", "FOR", "FOX",
        "FRY", "FUN", "FUR", "GAB", "GAG", "GAL", "GAP", "GAS", "GEL", "GEM",
        "GET", "GIG", "GIN", "GNU", "GOB", "GOD", "GOT", "GUM", "GUN", "GUT",
        "GUY", "GYM", "HAD", "HAG", "HAM", "HAS", "HAT", "HAY", "HEM", "HEN",
        "HER", "HEW", "HEX", "HEY", "HID", "HIM", "HIP", "HIS", "HIT", "HOB",
        "HOD", "HOG", "HOP", "HOT", "HOW", "HUB", "HUE", "HUG", "HUM", "HUT",
        "ICE", "ICY", "ILL", "IMP", "INK", "INN", "ION", "IRE", "IRK", "IVY",
        "JAB", "JAG", "JAM", "JAR", "JAW", "JAY", "JET", "JIG", "JOB", "JOG",
        "JOT", "JOY", "JUG", "JUT", "KEG", "KEN", "KEY", "KID", "KIN", "KIT",
        "LAB", "LAC", "LAD", "LAG", "LAP", "LAW", "LAX", "LAY", "LEA", "LED",
        "LEG", "LET", "LID", "LIE", "LIP", "LIT", "LOB", "LOG", "LOP", "LOT",
        "LOW", "LUG", "LYE", "MAD", "MAG", "MAN", "MAP", "MAR", "MAT", "MAW",
        "MAX", "MAY", "MEN", "MET", "MID", "MIX", "MOB", "MOD", "MOM", "MOP",
        "MOW", "MUD", "MUG", "MUM", "NAB", "NAG", "NAP", "NAY", "NET", "NEW",
        "NIB", "NIL", "NIP", "NIT", "NOD", "NOR", "NOT", "NOW", "NUB", "NUN",
        "NUT", "OAF", "OAK", "OAR", "OAT", "ODD", "ODE", "OFF", "OFT", "OIL",
        "OLD", "ONE", "OPT", "ORB", "ORE", "OUR", "OUT", "OVA", "OWE", "OWL",
        "OWN", "PAD", "PAL", "PAN", "PAP", "PAR", "PAT", "PAW", "PAY", "PEA",
        "PEG", "PEN", "PEP", "PER", "PET", "PEW", "PIE", "PIG", "PIN", "PIT",
        "PLY", "POD", "POI", "POP", "POT", "POW", "PRO", "PRY", "PUB", "PUG",
        "PUN", "PUP", "PUS", "PUT", "RAD", "RAG", "RAM", "RAN", "RAP", "RAT",
        "RAW", "RAY", "RED", "REF", "REP", "RIB", "RID", "RIG", "RIM", "RIP",
        "ROB", "ROD", "ROE", "ROT", "ROW", "RUB", "RUG", "RUM", "RUN", "RUT",
        "RYE", "SAC", "SAD", "SAG", "SAP", "SAT", "SAW", "SAY", "SEA", "SET",
        "SEW", "SHE", "SHY", "SIN", "SIP", "SIR", "SIS", "SIT", "SIX", "SKI",
        "SKY", "SLY", "SOB", "SOD", "SON", "SOP", "SOT", "SOW", "SOY", "SPA",
        "SPY", "STY", "SUB", "SUE", "SUM", "SUN", "SUP", "TAB", "TAD", "TAG",
        "TAN", "TAP", "TAR", "TAT", "TAX", "TEA", "TEE", "TEN", "THE", "THY",
        "TIC", "TIE", "TIN", "TIP", "TOE", "TOG", "TOM", "TON", "TOO", "TOP",
        "TOT", "TOW", "TOY", "TRY", "TUB", "TUG", "TUN", "TWO", "URN", "USE",
        "VAN", "VAT", "VET", "VEX", "VIA", "VIE", "VOW", "WAD", "WAG", "WAN",
        "WAR", "WAS", "WAX", "WAY", "WEB", "WED", "WEE", "WET", "WHO", "WHY",
        "WIG", "WIN", "WIT", "WOE", "WOK", "WON", "WOO", "WOW", "WRY", "YAK",
        "YAM", "YAP", "YAW", "YEA", "YES", "YET", "YEW", "YIN", "YIP", "YOU",
        "ZAP", "ZED", "ZEE", "ZEN", "ZIG", "ZIP", "ZIT", "ZOO",

        // 4-letter common English words
        "ABLE", "ACHE", "ACID", "ACNE", "ACRE", "ACTS", "AGED", "AGES", "AIDE", "AIDS",
        "AIMS", "AIRS", "AIRY", "AJAR", "AKIN", "ALLY", "ALMS", "ALOE", "ALSO", "ALTO",
        "AMID", "AMMO", "ANEW", "ANTE", "ANTI", "APEX", "ARCH", "AREA", "ARIA", "ARMY",
        "ARTS", "ARTY", "ASHY", "ASKS", "ATOM", "AUNT", "AURA", "AUTO", "AVID", "AWAY",
        "AWED", "AWRY", "AXES", "AXIS", "BABY", "BACK", "BADE", "BAGS", "BAIL", "BAIT",
        "BAKE", "BALD", "BALE", "BALL", "BALM", "BAND", "BANE", "BANG", "BANK", "BARE",
        "BARK", "BARN", "BARS", "BASE", "BASH", "BASK", "BASS", "BATH", "BATS", "BEAD",
        "BEAK", "BEAM", "BEAN", "BEAR", "BEAT", "BECK", "BEDS", "BEEF", "BEEN", "BEER",
        "BEES", "BEET", "BELL", "BELT", "BEND", "BENT", "BERG", "BEST", "BETA", "BETS",
        "BIAS", "BIDE", "BIDS", "BIKE", "BILE", "BILL", "BIND", "BIRD", "BITE", "BITS",
        "BLAB", "BLAH", "BLAT", "BLED", "BLEW", "BLIP", "BLOB", "BLOC", "BLOG", "BLOT",
        "BLOW", "BLUE", "BLUR", "BOAR", "BOAT", "BODE", "BODY", "BOGS", "BOIL", "BOLD",
        "BOLE", "BOLT", "BOMB", "BOND", "BONE", "BONY", "BOOK", "BOOM", "BOON", "BOOR",
        "BOOT", "BORE", "BORN", "BOSS", "BOTH", "BOUT", "BOWL", "BOWS", "BOYS", "BRAG",
        "BRAN", "BRAS", "BRAT", "BRAY", "BRED", "BREW", "BRIE", "BRIG", "BRIM", "BROW",
        "BUCK", "BUDS", "BUFF", "BUGS", "BULB", "BULK", "BULL", "BUMP", "BUMS", "BUNK",
        "BUNS", "BUNT", "BUOY", "BURN", "BURP", "BURR", "BURY", "BUSH", "BUSK", "BUST",
        "BUSY", "BUTS", "BUYS", "BUZZ", "BYTE", "CABS", "CAFE", "CAGE", "CAGY", "CAKE",
        "CALF", "CALL", "CALM", "CAME", "CAMP", "CAMS", "CANE", "CANS", "CANT", "CAPE",
        "CAPS", "CARD", "CARE", "CARP", "CARS", "CART", "CASE", "CASH", "CASK", "CAST",
        "CATS", "CAVE", "CEDE", "CELL", "CENT", "CHAD", "CHAP", "CHAR", "CHAT", "CHEF",
        "CHEW", "CHIC", "CHIN", "CHIP", "CHOP", "CHOW", "CHUG", "CHUM", "CITE", "CITY",
        "CLAD", "CLAM", "CLAN", "CLAP", "CLAW", "CLAY", "CLEF", "CLIP", "CLOD", "CLOG",
        "CLOP", "CLOT", "CLUB", "CLUE", "COAL", "COAT", "COAX", "COBS", "COCA", "COCK",
        "CODE", "COED", "COIF", "COIL", "COIN", "COKE", "COLD", "COLE", "COLT", "COMA",
        "COMB", "COME", "CONE", "CONK", "COOK", "COOL", "COOP", "COOT", "COPE", "COPS",
        "COPY", "CORD", "CORE", "CORK", "CORN", "COST", "COSY", "COTS", "COUP", "COVE",
        "COWL", "COWS", "COZY", "CRAB", "CRAG", "CRAM", "CRAN", "CRAP", "CRAW", "CREW",
        "CRIB", "CROP", "CROW", "CRUD", "CRUX", "CUBE", "CUBS", "CUDS", "CUED", "CUES",
        "CUFF", "CULL", "CULT", "CUPS", "CURB", "CURD", "CURE", "CURL", "CURT", "CUSP",
        "CUTE", "CUTS", "CYST", "DABS", "DACE", "DADS", "DAFT", "DALE", "DAME", "DAMP",
        "DAMS", "DANE", "DANG", "DANK", "DARE", "DARK", "DARN", "DART", "DASH", "DATA",
        "DATE", "DAUB", "DAWN", "DAYS", "DAZE", "DEAD", "DEAF", "DEAL", "DEAN", "DEAR",
        "DEBT", "DECK", "DEED", "DEEM", "DEEP", "DEER", "DEFT", "DEFY", "DELI", "DELL",
        "DEMO", "DENS", "DENT", "DENY", "DESK", "DEWY", "DIAL", "DICE", "DIED", "DIES",
        "DIET", "DIGS", "DIKE", "DIME", "DIMS", "DINE", "DING", "DINT", "DIPS", "DIRE",
        "DIRT", "DISC", "DISH", "DISK", "DIVE", "DOCK", "DOER", "DOES", "DOFF", "DOGS",
        "DOLE", "DOLL", "DOLT", "DOME", "DONE", "DONG", "DOOM", "DOOR", "DOPE", "DORK",
        "DORM", "DOSE", "DOTE", "DOTH", "DOTS", "DOUR", "DOVE", "DOWN", "DOZE", "DRAB",
        "DRAG", "DRAM", "DRAW", "DRAY", "DREW", "DRIP", "DROP", "DRUG", "DRUM", "DUAL",
        "DUBS", "DUCK", "DUCT", "DUDE", "DUDS", "DUEL", "DUES", "DUET", "DUFF", "DUKE",
        "DULL", "DULY", "DUMB", "DUMP", "DUNE", "DUNG", "DUNK", "DUNS", "DUPE", "DUSK",
        "DUST", "DUTY", "DYED", "DYER", "DYES", "EACH", "EARL", "EARN", "EARS", "EASE",
        "EAST", "EASY", "EATS", "EAVE", "EBBS", "ECHO", "ECRU", "EDDY", "EDGE", "EDGY",
        "EDIT", "EELS", "EGGS", "EGOS", "EKED", "EKES", "ELAN", "ELKS", "ELMS", "ELSE",
        "EMIR", "EMIT", "ENDS", "ENVY", "EPIC", "ERAS", "ERGO", "ERGS", "ERRS", "ERST",
        "ETCH", "EURO", "EVEN", "EVER", "EVES", "EVIL", "EWER", "EWES", "EXAM", "EXEC",
        "EXIT", "EXPO", "EYED", "EYES", "FACE", "FACT", "FADE", "FADS", "FAIL", "FAIN",
        "FAIR", "FAKE", "FALL", "FAME", "FANG", "FANS", "FARE", "FARM", "FARO", "FAST",
        "FATE", "FATS", "FAUN", "FAWN", "FAZE", "FEAR", "FEAT", "FEED", "FEEL", "FEES",
        "FEET", "FELL", "FELT", "FEND", "FERN", "FEST", "FETE", "FEUD", "FIAT", "FIBS",
        "FIEF", "FIFE", "FIGS", "FILE", "FILL", "FILM", "FIND", "FINE", "FINK", "FINS",
        "FIRE", "FIRM", "FIRS", "FISH", "FIST", "FITS", "FIVE", "FIXE", "FIZZ", "FLAB",
        "FLAG", "FLAK", "FLAM", "FLAN", "FLAP", "FLAT", "FLAW", "FLAX", "FLAY", "FLEA",
        "FLED", "FLEE", "FLEW", "FLEX", "FLIP", "FLIT", "FLOE", "FLOG", "FLOP", "FLOW",
        "FLUB", "FLUE", "FLUX", "FOAL", "FOAM", "FOBS", "FOCI", "FOES", "FOGS", "FOGY",
        "FOIL", "FOLD", "FOLK", "FOND", "FONT", "FOOD", "FOOL", "FOOT", "FORD", "FORE",
        "FORK", "FORM", "FORT", "FOUL", "FOUR", "FOWL", "FOXY", "FRAT", "FRAY", "FREE",
        "FRET", "FRIG", "FROG", "FROM", "FUEL", "FULL", "FUME", "FUND", "FUNK", "FURL",
        "FURS", "FURY", "FUSE", "FUSS", "FUZE", "FUZZ", "GAFF", "GAGE", "GAGS", "GAIN",
        "GAIT", "GALA", "GALE", "GALL", "GALS", "GAME", "GAMS", "GAMY", "GANG", "GAPE",
        "GAPS", "GARB", "GASH", "GASP", "GATE", "GAUD", "GAVE", "GAWK", "GAZE", "GEAR",
        "GEEK", "GELS", "GEMS", "GENE", "GENT", "GERM", "GETS", "GIBE", "GIFT", "GIGS",
        "GILD", "GILL", "GILT", "GIMP", "GINS", "GIRD", "GIRL", "GIRT", "GIST", "GIVE",
        "GLAD", "GLEE", "GLEN", "GLIB", "GLOB", "GLOM", "GLOP", "GLOW", "GLUE", "GLUG",
        "GLUM", "GLUT", "GNAR", "GNAT", "GNAW", "GNUS", "GOAD", "GOAL", "GOAT", "GOBS",
        "GODS", "GOER", "GOES", "GOLD", "GOLF", "GONE", "GONG", "GOOD", "GOOF", "GOOK",
        "GOON", "GOOP", "GORE", "GORY", "GOSH", "GOTH", "GOUT", "GOWN", "GRAB", "GRAD",
        "GRAM", "GRAN", "GRAY", "GREW", "GREY", "GRID", "GRIM", "GRIN", "GRIP", "GRIT",
        "GROG", "GROW", "GRUB", "GRUE", "GRUM", "GUFF", "GULF", "GULL", "GULP", "GUMS",
        "GUNK", "GUNS", "GURU", "GUSH", "GUST", "GUTS", "GUYS", "GYMS", "GYRO",
    )
}
