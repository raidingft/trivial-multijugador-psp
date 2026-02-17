package server.data

import server.model.Category
import server.model.Difficulty
import server.model.TriviaQuestion

object QuestionBank {

    private val questions = mutableListOf<TriviaQuestion>()

    init { load() }

    private fun load() {
        // HISTORY
        questions += TriviaQuestion(1, Category.HISTORY, Difficulty.EASY,
            "¿En qué año descubrió Colón América?",
            listOf("1492","1500","1480","1510"), 0,
            "Colón llegó a América el 12 de octubre de 1492")
        questions += TriviaQuestion(2, Category.HISTORY, Difficulty.EASY,
            "¿Quién fue el primer presidente de EE.UU.?",
            listOf("Abraham Lincoln","George Washington","Thomas Jefferson","John Adams"), 1,
            "George Washington fue el primer presidente (1789-1797)")
        questions += TriviaQuestion(3, Category.HISTORY, Difficulty.MEDIUM,
            "¿En qué año cayó el Muro de Berlín?",
            listOf("1987","1989","1991","1985"), 1,
            "El Muro de Berlín cayó el 9 de noviembre de 1989")
        questions += TriviaQuestion(4, Category.HISTORY, Difficulty.MEDIUM,
            "¿Quién lideró la Revolución Cubana?",
            listOf("Che Guevara","Fidel Castro","Raúl Castro","Camilo Cienfuegos"), 1,
            "Fidel Castro lideró la Revolución Cubana en 1959")
        questions += TriviaQuestion(5, Category.HISTORY, Difficulty.HARD,
            "¿En qué año se firmó el Tratado de Versalles?",
            listOf("1918","1919","1920","1917"), 1,
            "El Tratado de Versalles se firmó el 28 de junio de 1919")
        questions += TriviaQuestion(6, Category.HISTORY, Difficulty.HARD,
            "¿Qué batalla marcó el fin de Napoleón?",
            listOf("Austerlitz","Trafalgar","Waterloo","Leipzig"), 2,
            "La batalla de Waterloo en 1815 puso fin al Imperio napoleónico")

        // SCIENCE
        questions += TriviaQuestion(7, Category.SCIENCE, Difficulty.EASY,
            "¿Cuántos planetas hay en el sistema solar?",
            listOf("7","8","9","10"), 1,
            "Hay 8 planetas desde que Plutón fue reclasificado en 2006")
        questions += TriviaQuestion(8, Category.SCIENCE, Difficulty.EASY,
            "¿Qué gas respiramos principalmente?",
            listOf("Oxígeno","Nitrógeno","CO2","Hidrógeno"), 1,
            "El aire es 78% nitrógeno")
        questions += TriviaQuestion(9, Category.SCIENCE, Difficulty.MEDIUM,
            "¿Cuál es el planeta más grande del sistema solar?",
            listOf("Saturno","Júpiter","Neptuno","Urano"), 1,
            "Júpiter tiene un diámetro de 139.820 km")
        questions += TriviaQuestion(10, Category.SCIENCE, Difficulty.MEDIUM,
            "¿Cuántos huesos tiene el cuerpo humano adulto?",
            listOf("186","206","226","196"), 1,
            "El cuerpo humano adulto tiene 206 huesos")
        questions += TriviaQuestion(11, Category.SCIENCE, Difficulty.HARD,
            "¿Cuál es la velocidad de la luz en el vacío?",
            listOf("299.792 km/s","300.000 km/s","299.000 km/s","298.792 km/s"), 0,
            "La velocidad de la luz es exactamente 299.792.458 m/s")
        questions += TriviaQuestion(12, Category.SCIENCE, Difficulty.HARD,
            "¿Cuántos elementos tiene la tabla periódica?",
            listOf("103","108","113","118"), 3,
            "La tabla periódica tiene actualmente 118 elementos")

        // SPORTS
        questions += TriviaQuestion(13, Category.SPORTS, Difficulty.EASY,
            "¿Cuántos jugadores tiene un equipo de fútbol en el campo?",
            listOf("10","11","12","9"), 1,
            "Un equipo de fútbol tiene 11 jugadores")
        questions += TriviaQuestion(14, Category.SPORTS, Difficulty.EASY,
            "¿En qué deporte se usa una raqueta?",
            listOf("Fútbol","Baloncesto","Tenis","Natación"), 2,
            "El tenis se juega con raqueta")
        questions += TriviaQuestion(15, Category.SPORTS, Difficulty.MEDIUM,
            "¿Quién ganó el Mundial de Fútbol 2018?",
            listOf("Brasil","Alemania","Francia","Argentina"), 2,
            "Francia ganó el Mundial de Rusia 2018")
        questions += TriviaQuestion(16, Category.SPORTS, Difficulty.MEDIUM,
            "¿Cuántos Grand Slams hay en el tenis?",
            listOf("3","4","5","6"), 1,
            "Hay 4 Grand Slams: Australia, Roland Garros, Wimbledon y US Open")
        questions += TriviaQuestion(17, Category.SPORTS, Difficulty.HARD,
            "¿En qué año ganó España su primera Copa del Mundo?",
            listOf("2006","2008","2010","2012"), 2,
            "España ganó su primer Mundial en Sudáfrica 2010")

        // GEOGRAPHY
        questions += TriviaQuestion(18, Category.GEOGRAPHY, Difficulty.EASY,
            "¿Cuál es la capital de España?",
            listOf("Barcelona","Madrid","Sevilla","Valencia"), 1,
            "Madrid es la capital de España")
        questions += TriviaQuestion(19, Category.GEOGRAPHY, Difficulty.EASY,
            "¿En qué continente está Egipto?",
            listOf("Asia","Europa","África","América"), 2,
            "Egipto está en el continente africano")
        questions += TriviaQuestion(20, Category.GEOGRAPHY, Difficulty.MEDIUM,
            "¿Cuál es el río más largo del mundo?",
            listOf("Nilo","Amazonas","Yangtsé","Mississippi"), 1,
            "El Amazonas es el río más largo con 6.992 km")
        questions += TriviaQuestion(21, Category.GEOGRAPHY, Difficulty.MEDIUM,
            "¿Cuántos países hay en África?",
            listOf("48","52","54","56"), 2,
            "África tiene 54 países reconocidos")
        questions += TriviaQuestion(22, Category.GEOGRAPHY, Difficulty.HARD,
            "¿Cuál es la capital de Australia?",
            listOf("Sídney","Melbourne","Canberra","Brisbane"), 2,
            "Canberra es la capital, no Sídney")

        // ART_LITERATURE
        questions += TriviaQuestion(23, Category.ART_LITERATURE, Difficulty.EASY,
            "¿Quién pintó la Mona Lisa?",
            listOf("Picasso","Van Gogh","Leonardo da Vinci","Miguel Ángel"), 2,
            "Leonardo da Vinci pintó la Mona Lisa entre 1503-1519")
        questions += TriviaQuestion(24, Category.ART_LITERATURE, Difficulty.EASY,
            "¿Quién escribió Don Quijote de la Mancha?",
            listOf("Lope de Vega","Miguel de Cervantes","García Lorca","Calderón"), 1,
            "Miguel de Cervantes escribió Don Quijote en 1605")
        questions += TriviaQuestion(25, Category.ART_LITERATURE, Difficulty.MEDIUM,
            "¿Qué movimiento artístico fundó Picasso?",
            listOf("Surrealismo","Cubismo","Impresionismo","Expresionismo"), 1,
            "Picasso fue cofundador del Cubismo")
        questions += TriviaQuestion(26, Category.ART_LITERATURE, Difficulty.HARD,
            "¿En qué año se publicó '1984' de Orwell?",
            listOf("1948","1949","1950","1984"), 1,
            "1984 fue publicado en junio de 1949")

        // ENTERTAINMENT
        questions += TriviaQuestion(27, Category.ENTERTAINMENT, Difficulty.EASY,
            "¿Quién canta 'Thriller'?",
            listOf("Prince","Michael Jackson","Freddie Mercury","Elvis"), 1,
            "Michael Jackson lanzó Thriller en 1982")
        questions += TriviaQuestion(28, Category.ENTERTAINMENT, Difficulty.MEDIUM,
            "¿Cuántas películas hay en la saga original de Star Wars?",
            listOf("3","6","9","12"), 1,
            "La saga original tiene 6 películas (1977-2005)")
        questions += TriviaQuestion(29, Category.ENTERTAINMENT, Difficulty.HARD,
            "¿En qué año se estrenó la primera película de Harry Potter?",
            listOf("2000","2001","2002","1999"), 1,
            "La Piedra Filosofal se estrenó en 2001")

        // TECHNOLOGY
        questions += TriviaQuestion(30, Category.TECHNOLOGY, Difficulty.EASY,
            "¿Qué significa WWW?",
            listOf("World Wide Web","World Web Wide","Wide World Web","Web World Wide"), 0,
            "WWW significa World Wide Web")
        questions += TriviaQuestion(31, Category.TECHNOLOGY, Difficulty.EASY,
            "¿Quién fundó Microsoft?",
            listOf("Steve Jobs","Bill Gates","Zuckerberg","Elon Musk"), 1,
            "Bill Gates y Paul Allen fundaron Microsoft en 1975")
        questions += TriviaQuestion(32, Category.TECHNOLOGY, Difficulty.MEDIUM,
            "¿En qué año se lanzó el primer iPhone?",
            listOf("2005","2007","2008","2006"), 1,
            "El primer iPhone se lanzó el 29 de junio de 2007")
        questions += TriviaQuestion(33, Category.TECHNOLOGY, Difficulty.HARD,
            "¿Quién inventó la World Wide Web?",
            listOf("Bill Gates","Steve Jobs","Tim Berners-Lee","Larry Page"), 2,
            "Tim Berners-Lee inventó la WWW en 1989")

        // GENERAL
        questions += TriviaQuestion(34, Category.GENERAL, Difficulty.EASY,
            "¿Cuántos días tiene una semana?",
            listOf("5","6","7","8"), 2,
            "Una semana tiene 7 días")
        questions += TriviaQuestion(35, Category.GENERAL, Difficulty.MEDIUM,
            "¿Cuál es el océano más grande?",
            listOf("Atlántico","Índico","Ártico","Pacífico"), 3,
            "El Océano Pacífico cubre más de 165 millones de km²")
        questions += TriviaQuestion(36, Category.GENERAL, Difficulty.HARD,
            "¿Cuál es el idioma más hablado del mundo?",
            listOf("Inglés","Mandarín","Español","Hindi"), 1,
            "El mandarín tiene más de 1.100 millones de hablantes nativos")
    }

    fun get(count: Int, categories: List<Category>, difficulty: Difficulty): List<TriviaQuestion> {
        var pool = questions.filter { it.category in categories }
        if (difficulty != Difficulty.MIXED) pool = pool.filter { it.difficulty == difficulty }
        return pool.shuffled().take(count)
    }
}
