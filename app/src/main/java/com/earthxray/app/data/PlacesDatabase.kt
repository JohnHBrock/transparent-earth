package com.earthxray.app.data

object PlacesDatabase {
    val places = listOf(
        // Major Cities - North America
        Place("New York", 40.7128, -74.0060, "USA", PlaceType.CITY, 8336817),
        Place("Los Angeles", 34.0522, -118.2437, "USA", PlaceType.CITY, 3979576),
        Place("Chicago", 41.8781, -87.6298, "USA", PlaceType.CITY, 2693976),
        Place("Houston", 29.7604, -95.3698, "USA", PlaceType.CITY, 2320268),
        Place("Phoenix", 33.4484, -112.0740, "USA", PlaceType.CITY, 1680992),
        Place("Philadelphia", 39.9526, -75.1652, "USA", PlaceType.CITY, 1584064),
        Place("San Antonio", 29.4241, -98.4936, "USA", PlaceType.CITY, 1547253),
        Place("San Diego", 32.7157, -117.1611, "USA", PlaceType.CITY, 1423851),
        Place("Dallas", 32.7767, -96.7970, "USA", PlaceType.CITY, 1343573),
        Place("San Francisco", 37.7749, -122.4194, "USA", PlaceType.CITY, 881549),
        Place("Seattle", 47.6062, -122.3321, "USA", PlaceType.CITY, 753675),
        Place("Miami", 25.7617, -80.1918, "USA", PlaceType.CITY, 467963),
        Place("Washington DC", 38.9072, -77.0369, "USA", PlaceType.CAPITAL, 705749),
        Place("Toronto", 43.6532, -79.3832, "Canada", PlaceType.CITY, 2930000),
        Place("Vancouver", 49.2827, -123.1207, "Canada", PlaceType.CITY, 675218),
        Place("Montreal", 45.5017, -73.5673, "Canada", PlaceType.CITY, 1780000),
        Place("Mexico City", 19.4326, -99.1332, "Mexico", PlaceType.CAPITAL, 9209944),

        // South America
        Place("São Paulo", -23.5505, -46.6333, "Brazil", PlaceType.CITY, 12325232),
        Place("Rio de Janeiro", -22.9068, -43.1729, "Brazil", PlaceType.CITY, 6748000),
        Place("Buenos Aires", -34.6037, -58.3816, "Argentina", PlaceType.CAPITAL, 3075000),
        Place("Lima", -12.0464, -77.0428, "Peru", PlaceType.CAPITAL, 9751717),
        Place("Bogotá", 4.7110, -74.0721, "Colombia", PlaceType.CAPITAL, 7412566),
        Place("Santiago", -33.4489, -70.6693, "Chile", PlaceType.CAPITAL, 5614000),
        Place("Caracas", 10.4806, -66.9036, "Venezuela", PlaceType.CAPITAL, 1943901),
        Place("Brasília", -15.8267, -47.9218, "Brazil", PlaceType.CAPITAL, 3015268),

        // Europe
        Place("London", 51.5074, -0.1278, "UK", PlaceType.CAPITAL, 9002488),
        Place("Paris", 48.8566, 2.3522, "France", PlaceType.CAPITAL, 2165423),
        Place("Berlin", 52.5200, 13.4050, "Germany", PlaceType.CAPITAL, 3769495),
        Place("Madrid", 40.4168, -3.7038, "Spain", PlaceType.CAPITAL, 3223334),
        Place("Rome", 41.9028, 12.4964, "Italy", PlaceType.CAPITAL, 2872800),
        Place("Barcelona", 41.3851, 2.1734, "Spain", PlaceType.CITY, 1620343),
        Place("Vienna", 48.2082, 16.3738, "Austria", PlaceType.CAPITAL, 1911191),
        Place("Amsterdam", 52.3676, 4.9041, "Netherlands", PlaceType.CAPITAL, 872680),
        Place("Stockholm", 59.3293, 18.0686, "Sweden", PlaceType.CAPITAL, 975551),
        Place("Copenhagen", 55.6761, 12.5683, "Denmark", PlaceType.CAPITAL, 799033),
        Place("Moscow", 55.7558, 37.6173, "Russia", PlaceType.CAPITAL, 12506468),
        Place("Istanbul", 41.0082, 28.9784, "Turkey", PlaceType.CITY, 15460000),
        Place("Athens", 37.9838, 23.7275, "Greece", PlaceType.CAPITAL, 664046),
        Place("Prague", 50.0755, 14.4378, "Czech Republic", PlaceType.CAPITAL, 1309000),
        Place("Warsaw", 52.2297, 21.0122, "Poland", PlaceType.CAPITAL, 1790658),
        Place("Lisbon", 38.7223, -9.1393, "Portugal", PlaceType.CAPITAL, 504718),

        // Asia
        Place("Tokyo", 35.6762, 139.6503, "Japan", PlaceType.CAPITAL, 13960000),
        Place("Beijing", 39.9042, 116.4074, "China", PlaceType.CAPITAL, 21540000),
        Place("Shanghai", 31.2304, 121.4737, "China", PlaceType.CITY, 27058000),
        Place("Hong Kong", 22.3193, 114.1694, "China", PlaceType.CITY, 7496981),
        Place("Seoul", 37.5665, 126.9780, "South Korea", PlaceType.CAPITAL, 9733509),
        Place("Singapore", 1.3521, 103.8198, "Singapore", PlaceType.CAPITAL, 5685807),
        Place("Bangkok", 13.7563, 100.5018, "Thailand", PlaceType.CAPITAL, 10539000),
        Place("Mumbai", 19.0760, 72.8777, "India", PlaceType.CITY, 20411000),
        Place("Delhi", 28.6139, 77.2090, "India", PlaceType.CAPITAL, 32941000),
        Place("Bangalore", 12.9716, 77.5946, "India", PlaceType.CITY, 12765000),
        Place("Jakarta", -6.2088, 106.8456, "Indonesia", PlaceType.CAPITAL, 10562088),
        Place("Manila", 14.5995, 120.9842, "Philippines", PlaceType.CAPITAL, 1846513),
        Place("Ho Chi Minh City", 10.8231, 106.6297, "Vietnam", PlaceType.CITY, 8993082),
        Place("Hanoi", 21.0285, 105.8542, "Vietnam", PlaceType.CAPITAL, 8053663),
        Place("Taipei", 25.0330, 121.5654, "Taiwan", PlaceType.CAPITAL, 2646204),
        Place("Kuala Lumpur", 3.1390, 101.6869, "Malaysia", PlaceType.CAPITAL, 1768000),
        Place("Dubai", 25.2048, 55.2708, "UAE", PlaceType.CITY, 3331420),
        Place("Tel Aviv", 32.0853, 34.7818, "Israel", PlaceType.CITY, 460613),
        Place("Jerusalem", 31.7683, 35.2137, "Israel", PlaceType.CAPITAL, 936425),

        // Middle East
        Place("Riyadh", 24.7136, 46.6753, "Saudi Arabia", PlaceType.CAPITAL, 7676654),
        Place("Tehran", 35.6892, 51.3890, "Iran", PlaceType.CAPITAL, 8896000),
        Place("Baghdad", 33.3152, 44.3661, "Iraq", PlaceType.CAPITAL, 7216000),
        Place("Cairo", 30.0444, 31.2357, "Egypt", PlaceType.CAPITAL, 20900604),

        // Africa
        Place("Lagos", 6.5244, 3.3792, "Nigeria", PlaceType.CITY, 14862000),
        Place("Johannesburg", -26.2041, 28.0473, "South Africa", PlaceType.CITY, 5635127),
        Place("Cape Town", -33.9249, 18.4241, "South Africa", PlaceType.CITY, 4617560),
        Place("Nairobi", -1.2864, 36.8172, "Kenya", PlaceType.CAPITAL, 4397073),
        Place("Kinshasa", -4.3217, 15.3125, "DR Congo", PlaceType.CAPITAL, 14970460),
        Place("Casablanca", 33.5731, -7.5898, "Morocco", PlaceType.CITY, 3359818),
        Place("Addis Ababa", 9.0320, 38.7469, "Ethiopia", PlaceType.CAPITAL, 3384569),

        // Oceania
        Place("Sydney", -33.8688, 151.2093, "Australia", PlaceType.CITY, 5312000),
        Place("Melbourne", -37.8136, 144.9631, "Australia", PlaceType.CITY, 5078000),
        Place("Brisbane", -27.4698, 153.0251, "Australia", PlaceType.CITY, 2560720),
        Place("Perth", -31.9505, 115.8605, "Australia", PlaceType.CITY, 2085973),
        Place("Auckland", -36.8485, 174.7633, "New Zealand", PlaceType.CITY, 1657200),
        Place("Wellington", -41.2865, 174.7762, "New Zealand", PlaceType.CAPITAL, 415000),

        // Famous Landmarks
        Place("Statue of Liberty", 40.6892, -74.0445, "USA", PlaceType.LANDMARK),
        Place("Eiffel Tower", 48.8584, 2.2945, "France", PlaceType.LANDMARK),
        Place("Great Wall of China", 40.4319, 116.5704, "China", PlaceType.LANDMARK),
        Place("Taj Mahal", 27.1751, 78.0421, "India", PlaceType.LANDMARK),
        Place("Machu Picchu", -13.1631, -72.5450, "Peru", PlaceType.LANDMARK),
        Place("Christ the Redeemer", -22.9519, -43.2105, "Brazil", PlaceType.LANDMARK),
        Place("Colosseum", 41.8902, 12.4922, "Italy", PlaceType.LANDMARK),
        Place("Stonehenge", 51.1789, -1.8262, "UK", PlaceType.LANDMARK),
        Place("Pyramids of Giza", 29.9792, 31.1342, "Egypt", PlaceType.LANDMARK),
        Place("Sydney Opera House", -33.8568, 151.2153, "Australia", PlaceType.LANDMARK),
        Place("Big Ben", 51.5007, -0.1246, "UK", PlaceType.LANDMARK),
        Place("Golden Gate Bridge", 37.8199, -122.4783, "USA", PlaceType.LANDMARK),
        Place("Burj Khalifa", 25.1972, 55.2744, "UAE", PlaceType.LANDMARK),
        Place("Mount Everest", 27.9881, 86.9250, "Nepal", PlaceType.LANDMARK),
        Place("Niagara Falls", 43.0896, -79.0849, "Canada/USA", PlaceType.LANDMARK),
        Place("Grand Canyon", 36.1069, -112.1129, "USA", PlaceType.LANDMARK),
        Place("Angkor Wat", 13.4125, 103.8670, "Cambodia", PlaceType.LANDMARK),
        Place("Petra", 30.3285, 35.4444, "Jordan", PlaceType.LANDMARK),
        Place("Sagrada Familia", 41.4036, 2.1744, "Spain", PlaceType.LANDMARK),
        Place("Acropolis", 37.9715, 23.7257, "Greece", PlaceType.LANDMARK)
    )

    fun searchPlaces(query: String): List<Place> {
        if (query.isBlank()) return emptyList()

        val lowerQuery = query.lowercase().trim()
        return places.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.country.lowercase().contains(lowerQuery)
        }.sortedByDescending { place ->
            when {
                place.name.lowercase() == lowerQuery -> 100
                place.name.lowercase().startsWith(lowerQuery) -> 50
                else -> place.population
            }
        }
    }

    fun getPlaceByName(name: String): Place? {
        return places.find { it.name.equals(name, ignoreCase = true) }
    }
}
