package org.example.trivial.data

import org.example.trivial.model.Category
import org.example.trivial.model.Difficulty
import org.example.trivial.model.Question

object QuestionBank {

    private val allQuestions = mutableListOf<Question>()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        // HISTORIA - FÁCIL
        allQuestions.add(Question(1, Category.HISTORIA, Difficulty.FACIL,
            "¿En qué año descubrió Cristóbal Colón América?",
            listOf("1492", "1500", "1480", "1510"),
            0, "Colón llegó a América el 12 de octubre de 1492"))

        allQuestions.add(Question(2, Category.HISTORIA, Difficulty.FACIL,
            "¿Quién fue el primer presidente de Estados Unidos?",
            listOf("Abraham Lincoln", "George Washington", "Thomas Jefferson", "John Adams"),
            1, "George Washington fue el primer presidente (1789-1797)"))

        // HISTORIA - MEDIA
        allQuestions.add(Question(3, Category.HISTORIA, Difficulty.MEDIA,
            "¿En qué año cayó el Muro de Berlín?",
            listOf("1987", "1989", "1991", "1985"),
            1, "El Muro de Berlín cayó el 9 de noviembre de 1989"))

        allQuestions.add(Question(4, Category.HISTORIA, Difficulty.MEDIA,
            "¿Quién fue el líder de la Revolución Cubana?",
            listOf("Che Guevara", "Fidel Castro", "Raúl Castro", "Camilo Cienfuegos"),
            1, "Fidel Castro lideró la Revolución Cubana en 1959"))

        // HISTORIA - DIFÍCIL
        allQuestions.add(Question(5, Category.HISTORIA, Difficulty.DIFICIL,
            "¿En qué año se firmó el Tratado de Versalles?",
            listOf("1918", "1919", "1920", "1917"),
            1, "El Tratado de Versalles se firmó el 28 de junio de 1919"))

        // CIENCIA - FÁCIL
        allQuestions.add(Question(6, Category.CIENCIA_NATURALEZA, Difficulty.FACIL,
            "¿Cuántos planetas hay en el sistema solar?",
            listOf("7", "8", "9", "10"),
            1, "Hay 8 planetas desde que Plutón fue reclasificado en 2006"))

        allQuestions.add(Question(7, Category.CIENCIA_NATURALEZA, Difficulty.FACIL,
            "¿Qué gas respiramos principalmente?",
            listOf("Oxígeno", "Nitrógeno", "Dióxido de carbono", "Hidrógeno"),
            1, "El aire está compuesto por 78% de nitrógeno"))

        // CIENCIA - MEDIA
        allQuestions.add(Question(8, Category.CIENCIA_NATURALEZA, Difficulty.MEDIA,
            "¿Cuál es el planeta más grande del sistema solar?",
            listOf("Saturno", "Júpiter", "Neptuno", "Urano"),
            1, "Júpiter tiene un diámetro de 139,820 km"))

        allQuestions.add(Question(9, Category.CIENCIA_NATURALEZA, Difficulty.MEDIA,
            "¿Cuántos huesos tiene el cuerpo humano adulto?",
            listOf("186", "206", "226", "196"),
            1, "El cuerpo humano adulto tiene 206 huesos"))

        // CIENCIA - DIFÍCIL
        allQuestions.add(Question(10, Category.CIENCIA_NATURALEZA, Difficulty.DIFICIL,
            "¿Cuál es la velocidad de la luz en el vacío?",
            listOf("299,792 km/s", "300,000 km/s", "299,000 km/s", "298,792 km/s"),
            0, "La velocidad de la luz es exactamente 299,792,458 m/s"))

        // DEPORTES - FÁCIL
        allQuestions.add(Question(11, Category.DEPORTES, Difficulty.FACIL,
            "¿Cuántos jugadores tiene un equipo de fútbol en el campo?",
            listOf("10", "11", "12", "9"),
            1, "Un equipo de fútbol tiene 11 jugadores en el campo"))

        allQuestions.add(Question(12, Category.DEPORTES, Difficulty.FACIL,
            "¿En qué deporte se usa una raqueta?",
            listOf("Fútbol", "Baloncesto", "Tenis", "Natación"),
            2, "El tenis se juega con raqueta"))

        // DEPORTES - MEDIA
        allQuestions.add(Question(13, Category.DEPORTES, Difficulty.MEDIA,
            "¿Quién ganó el Mundial de Fútbol 2018?",
            listOf("Brasil", "Alemania", "Francia", "Argentina"),
            2, "Francia ganó el Mundial de Rusia 2018"))

        allQuestions.add(Question(14, Category.DEPORTES, Difficulty.MEDIA,
            "¿Cuántos Grand Slams hay en el tenis?",
            listOf("3", "4", "5", "6"),
            1, "Hay 4 Grand Slams: Australia, Roland Garros, Wimbledon y US Open"))

        // DEPORTES - DIFÍCIL
        allQuestions.add(Question(15, Category.DEPORTES, Difficulty.DIFICIL,
            "¿En qué año ganó España su primera Copa del Mundo?",
            listOf("2006", "2008", "2010", "2012"),
            2, "España ganó su primer Mundial en Sudáfrica 2010"))

        // GEOGRAFÍA - FÁCIL
        allQuestions.add(Question(16, Category.GEOGRAFIA, Difficulty.FACIL,
            "¿Cuál es la capital de España?",
            listOf("Barcelona", "Madrid", "Sevilla", "Valencia"),
            1, "Madrid es la capital de España"))

        allQuestions.add(Question(17, Category.GEOGRAFIA, Difficulty.FACIL,
            "¿En qué continente está Egipto?",
            listOf("Asia", "Europa", "África", "América"),
            2, "Egipto está en el continente africano"))

        // GEOGRAFÍA - MEDIA
        allQuestions.add(Question(18, Category.GEOGRAFIA, Difficulty.MEDIA,
            "¿Cuál es el río más largo del mundo?",
            listOf("Nilo", "Amazonas", "Yangtsé", "Mississippi"),
            1, "El Amazonas es el río más largo con 6,992 km"))

        allQuestions.add(Question(19, Category.GEOGRAFIA, Difficulty.MEDIA,
            "¿Cuántos países hay en África?",
            listOf("48", "52", "54", "56"),
            2, "África tiene 54 países reconocidos"))

        // GEOGRAFÍA - DIFÍCIL
        allQuestions.add(Question(20, Category.GEOGRAFIA, Difficulty.DIFICIL,
            "¿Cuál es la capital de Australia?",
            listOf("Sídney", "Melbourne", "Canberra", "Brisbane"),
            2, "Canberra es la capital de Australia, no Sídney"))

        // ARTE Y LITERATURA - FÁCIL
        allQuestions.add(Question(21, Category.ARTE_LITERATURA, Difficulty.FACIL,
            "¿Quién pintó la Mona Lisa?",
            listOf("Picasso", "Van Gogh", "Leonardo da Vinci", "Miguel Ángel"),
            2, "Leonardo da Vinci pintó la Mona Lisa entre 1503-1519"))

        allQuestions.add(Question(22, Category.ARTE_LITERATURA, Difficulty.FACIL,
            "¿Quién escribió Don Quijote de la Mancha?",
            listOf("Lope de Vega", "Miguel de Cervantes", "García Lorca", "Calderón"),
            1, "Miguel de Cervantes escribió Don Quijote en 1605"))

        // ARTE Y LITERATURA - MEDIA
        allQuestions.add(Question(23, Category.ARTE_LITERATURA, Difficulty.MEDIA,
            "¿Qué movimiento artístico fundó Pablo Picasso?",
            listOf("Surrealismo", "Cubismo", "Impresionismo", "Expresionismo"),
            1, "Picasso fue cofundador del Cubismo"))

        allQuestions.add(Question(24, Category.ARTE_LITERATURA, Difficulty.MEDIA,
            "¿Quién escribió 'Cien años de soledad'?",
            listOf("García Márquez", "Cortázar", "Borges", "Vargas Llosa"),
            0, "Gabriel García Márquez escribió esta obra en 1967"))

        // ARTE Y LITERATURA - DIFÍCIL
        allQuestions.add(Question(25, Category.ARTE_LITERATURA, Difficulty.DIFICIL,
            "¿En qué año se publicó '1984' de George Orwell?",
            listOf("1948", "1949", "1950", "1984"),
            1, "1984 fue publicado en junio de 1949"))

        // ENTRETENIMIENTO - FÁCIL
        allQuestions.add(Question(26, Category.ENTRETENIMIENTO, Difficulty.FACIL,
            "¿Qué película ganó el Oscar 2020 a Mejor Película?",
            listOf("Joker", "1917", "Parásitos", "Érase una vez en Hollywood"),
            2, "Parásitos hizo historia ganando Mejor Película"))

        allQuestions.add(Question(27, Category.ENTRETENIMIENTO, Difficulty.FACIL,
            "¿Quién canta 'Thriller'?",
            listOf("Prince", "Michael Jackson", "Freddie Mercury", "Elvis Presley"),
            1, "Michael Jackson lanzó Thriller en 1982"))

        // ENTRETENIMIENTO - MEDIA
        allQuestions.add(Question(28, Category.ENTRETENIMIENTO, Difficulty.MEDIA,
            "¿Cuántas películas hay en la saga original de Star Wars?",
            listOf("3", "6", "9", "12"),
            1, "La saga original tiene 6 películas (1977-2005)"))

        allQuestions.add(Question(29, Category.ENTRETENIMIENTO, Difficulty.MEDIA,
            "¿Qué banda lanzó el álbum 'Abbey Road'?",
            listOf("The Rolling Stones", "The Beatles", "Queen", "Led Zeppelin"),
            1, "The Beatles lanzó Abbey Road en 1969"))

        // ENTRETENIMIENTO - DIFÍCIL
        allQuestions.add(Question(30, Category.ENTRETENIMIENTO, Difficulty.DIFICIL,
            "¿En qué año se estrenó la primera película de Harry Potter?",
            listOf("2000", "2001", "2002", "1999"),
            1, "La Piedra Filosofal se estrenó en 2001"))

        // TECNOLOGÍA - FÁCIL
        allQuestions.add(Question(31, Category.TECNOLOGIA, Difficulty.FACIL,
            "¿Qué significa WWW?",
            listOf("World Wide Web", "World Web Wide", "Wide World Web", "Web World Wide"),
            0, "WWW significa World Wide Web"))

        allQuestions.add(Question(32, Category.TECNOLOGIA, Difficulty.FACIL,
            "¿Quién fundó Microsoft?",
            listOf("Steve Jobs", "Bill Gates", "Mark Zuckerberg", "Elon Musk"),
            1, "Bill Gates y Paul Allen fundaron Microsoft en 1975"))

        // TECNOLOGÍA - MEDIA
        allQuestions.add(Question(33, Category.TECNOLOGIA, Difficulty.MEDIA,
            "¿En qué año se lanzó el primer iPhone?",
            listOf("2005", "2007", "2008", "2006"),
            1, "El primer iPhone se lanzó el 29 de junio de 2007"))

        allQuestions.add(Question(34, Category.TECNOLOGIA, Difficulty.MEDIA,
            "¿Qué lenguaje usa Android principalmente?",
            listOf("Swift", "Kotlin", "C++", "JavaScript"),
            1, "Android usa principalmente Kotlin desde 2017"))

        // TECNOLOGÍA - DIFÍCIL
        allQuestions.add(Question(35, Category.TECNOLOGIA, Difficulty.DIFICIL,
            "¿Quién inventó la World Wide Web?",
            listOf("Bill Gates", "Steve Jobs", "Tim Berners-Lee", "Larry Page"),
            2, "Tim Berners-Lee inventó la WWW en 1989"))

        // CONOCIMIENTO GENERAL - FÁCIL
        allQuestions.add(Question(36, Category.CONOCIMIENTO_GENERAL, Difficulty.FACIL,
            "¿Cuántos días tiene una semana?",
            listOf("5", "6", "7", "8"),
            2, "Una semana tiene 7 días"))

        allQuestions.add(Question(37, Category.CONOCIMIENTO_GENERAL, Difficulty.FACIL,
            "¿Cuántos meses tiene un año?",
            listOf("10", "11", "12", "13"),
            2, "Un año tiene 12 meses"))

        // CONOCIMIENTO GENERAL - MEDIA
        allQuestions.add(Question(38, Category.CONOCIMIENTO_GENERAL, Difficulty.MEDIA,
            "¿Cuál es el océano más grande?",
            listOf("Atlántico", "Índico", "Ártico", "Pacífico"),
            3, "El Océano Pacífico cubre más de 165 millones de km²"))

        allQuestions.add(Question(39, Category.CONOCIMIENTO_GENERAL, Difficulty.MEDIA,
            "¿Cuántos continentes hay?",
            listOf("5", "6", "7", "8"),
            2, "Hay 7 continentes en la Tierra"))

        // CONOCIMIENTO GENERAL - DIFÍCIL
        allQuestions.add(Question(40, Category.CONOCIMIENTO_GENERAL, Difficulty.DIFICIL,
            "¿Cuál es el idioma más hablado del mundo?",
            listOf("Inglés", "Mandarín", "Español", "Hindi"),
            1, "El mandarín tiene más de 1.100 millones de hablantes nativos"))
    }

    fun getQuestions(
        count: Int,
        categories: List<Category>,
        difficulty: Difficulty
    ): List<Question> {
        var filteredQuestions = allQuestions.filter { it.category in categories }

        if (difficulty != Difficulty.MIXTA) {
            filteredQuestions = filteredQuestions.filter { it.difficulty == difficulty }
        }

        return filteredQuestions.shuffled().take(count)
    }

    fun getAllQuestions(): List<Question> = allQuestions
}