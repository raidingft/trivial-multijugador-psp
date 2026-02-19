# 🎯 Trivial Multijugador

Juego de preguntas y respuestas tipo Trivial Pursuit desarrollado con Kotlin Multiplatform y Compose Desktop. Soporta modo PVE (contra el servidor) y PVP (contra otros jugadores) con sistema de puntuación avanzado y persistencia de records.

---

## Tabla de Contenidos

1. [Características](#-características)
2. [Instalación y Compilación](#-instalación-y-compilación)
3. [Ejecución](#-ejecución)
4. [Arquitectura del Sistema](#-arquitectura-del-sistema)
5. [Protocolo de Comunicación](#-protocolo-de-comunicación)
6. [Manual de Usuario](#-manual-de-usuario)
7. [Configuración](#-configuración)
8. [Sistema de Puntuación](#-sistema-de-puntuación)
9. [Tecnologías Utilizadas](#-tecnologías-utilizadas)

---

## Características

### Modos de Juego
- **PVE (Player vs Environment)**: Juega solo contra el servidor
- **PVP (Player vs Player)**: Compite contra otros jugadores en tiempo real

### Configuración Flexible
- **Número de preguntas**: 3, 5, 10 o 20 preguntas por partida
- **Dificultad**: Fácil, Media, Difícil o Mixta
- **Modos de juego**:
  - **Por Turnos**: Los jugadores responden alternadamente (solo PVP)
  - **Simultáneo**: Todos ven la misma pregunta al mismo tiempo
  - **Contrarreloj**: Tiempo límite de 10, 15 o 30 segundos por pregunta

### Categorías
- 🏛️ Historia
- 🔬 Ciencia y Naturaleza
- ⚽ Deportes
- 🌍 Geografía
- 🎨 Arte y Literatura
- 🎬 Entretenimiento
- 💻 Tecnología
- 🧠 Conocimiento General

### Sistema de Puntuación Avanzado
- **Puntos base**: 10 puntos por respuesta correcta
- **Multiplicador de dificultad**: ×1.5 (Media), ×2 (Difícil)
- **Bonus de velocidad**: +5 puntos si respondes en menos de 5 segundos
- **Racha**: ×2 multiplicador a partir de 5 respuestas correctas seguidas
- **Bonus PVP**: +5 puntos al responder correctamente más rápido que tu oponente

### Sistema de Records
- Puntuación máxima alcanzada
- Racha más larga de respuestas correctas
- Estadísticas por categoría y dificultad
- Tiempo promedio de respuesta
- Ranking global de jugadores
- Historial de partidas ganadas/perdidas

### Interfaz Gráfica
- Diseño moderno con Compose Desktop
- Feedback visual inmediato (colores verde/rojo)
- Animaciones de racha
- Efectos de sonido personalizables
- Barras de progreso de tiempo y preguntas

---

## Instalación y Compilación

### 1. Clonar el Repositorio
```bash
git clone https://github.com/tu-usuario/TrivialMultijugador.git
cd TrivialMultijugador
```

### 2. Compilar el Proyecto
```bash
gradlew.bat build

```

### 3. Verificar la Compilación
Si la compilación es exitosa, verás:
```
BUILD SUCCESSFUL in Xs
```

---

## Ejecución

### Todo esto en la terminal de intellij

#### Terminal 1: Iniciar el Servidor
```bash
./gradlew :server:run
```

**Salida esperada:**
```
    TRIVIAL MULTIJUGADOR - SERVIDOR   


   Configuración cargada:
   Host: localhost
   Puerto: 5678
   Máximo de clientes: 10
   Servidor iniciado en localhost:5678
```

#### Terminal 2: Iniciar Cliente (Jugador 1)
```bash

./gradlew :composeApp:run
```

#### Terminal 3 (Opcional): Iniciar Cliente (Jugador 2) - Para PVP
```bash
./gradlew :composeApp:run
```

---

## Arquitectura del Sistema

### Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                         CLIENTE                             │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   UI Layer   │  │ Network Layer│  │  Model Layer │      │
│  │              │  │              │  │              │      │
│  │ - LoginScreen│  │ NetworkClient│  │ GameConfig   │      │
│  │ - MenuScreen │  │              │  │ GameModels   │      │
│  │ - GameScreen │  │ TCP Socket   │  │ PlayerScore  │      │
│  │ - Records    │  │ JSON Parser  │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                  │                   │             │
│         └──────────────────┼───────────────────┘             │
│                            │                                 │
└────────────────────────────┼─────────────────────────────────┘
                             │
                    ┌────────▼────────┐
                    │   TCP/IP (JSON) │
                    └────────┬────────┘
                             │
┌────────────────────────────▼─────────────────────────────────┐
│                        SERVIDOR                               │
├───────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Network Layer│  │  Game Logic  │  │  Data Layer  │      │
│  │              │  │              │  │              │      │
│  │TrivialServer │  │ GameSession  │  │QuestionBank  │      │
│  │ClientHandler │  │PvPGameSession│  │RecordsManager│      │
│  │              │  │Matchmaking   │  │              │      │
│  │ Coroutines   │  │              │  │ JSON Files   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                  │                   │             │
│         └──────────────────┼───────────────────┘             │
│                            │                                 │
└────────────────────────────┼─────────────────────────────────┘
                             │
                    ┌────────▼────────┐
                    │  File System    │
                    │                 │
                    │ server.properties│
                    │ records.json    │
                    │ questions.json  │
                    └─────────────────┘
```

### Componentes Principales

#### Servidor (`/server`)
```
server/
├── config/
│   └── ConfigManager.kt         # Gestor de configuración
├── data/
│   ├── QuestionBank.kt          # Banco de preguntas
│   └── RecordsManager.kt        # Gestor de records
├── game/
│   ├── MatchmakingManager.kt    # Emparejamiento PVP
│   └── PvPGameSession.kt        # Lógica de partidas PVP
├── model/
│   ├── GameModels.kt            # Modelos de datos del juego
│   └── ServerConfig.kt          # Modelo de configuración
├── network/
│   ├── TrivialServer.kt         # Servidor TCP principal
│   ├── ClientHandler.kt         # Manejador por cliente
│   └── GameSession.kt           # Lógica de partidas PVE
└── MainServer.kt                # Punto de entrada
```

#### Cliente (`/composeApp`)
```
composeApp/
├── network/
│   ├── NetworkClient.kt         # Cliente TCP
│   └── model/
│       └── NetworkModels.kt     # Modelos de mensajes
├── model/
│   └── GameModels.kt            # Modelos del juego
├── ui/
│   ├── LoginScreen.kt           # Pantalla de login
│   ├── MenuScreen.kt            # Menú principal
│   ├── ConfigScreen.kt          # Configuración
│   ├── RecordsScreen.kt         # Records y estadísticas
│   ├── WaitingMatchScreen.kt    # Espera de matchmaking
│   ├── ServerGameScreen.kt      # Pantalla de juego
│   └── ServerResultsScreen.kt   # Resultados finales
├── sound/
│   └── SoundPlayer.kt           # Sistema de sonidos
├── App.kt                       # Lógica principal
└── main.kt                      # Punto de entrada
```

---

## 📡 Protocolo de Comunicación

### Formato de Mensajes
Todos los mensajes siguen el formato:
```
TIPO_MENSAJE:{"campo1":"valor1","campo2":"valor2"}
```

### Mensajes Cliente → Servidor

#### 1. LOGIN - Conectar al servidor
```json
LOGIN:{"playerName":"NombreJugador"}
```

#### 2. CREATE_TRIVIA - Iniciar partida PVE
```json
CREATE_TRIVIA:{
  "mode": "PVE",
  "questions": 10,
  "categories": ["HISTORIA", "CIENCIA_NATURALEZA"],
  "difficulty": "MEDIA",
  "timeLimit": 15
}
```

#### 3. START_PVP - Buscar partida PVP
```json
START_PVP:{
  "questions": 10,
  "categories": ["HISTORIA", "DEPORTES"],
  "difficulty": "MIXTA",
  "timeLimit": 15
}
```

#### 4. ANSWER - Enviar respuesta
```json
ANSWER:{
  "questionId": 1,
  "selectedOption": 2,
  "timeElapsed": 8500
}
```

#### 5. GET_RECORDS - Solicitar records
```json
GET_RECORDS:{}
```

#### 6. CANCEL_MATCHMAKING - Cancelar búsqueda PVP
```json
CANCEL_MATCHMAKING:{}
```

#### 7. PLAY_AGAIN_REQUEST - Solicitar revancha (PVP)
```json
PLAY_AGAIN_REQUEST:{}
```

#### 8. PLAY_AGAIN_RESPONSE - Responder a revancha
```json
PLAY_AGAIN_RESPONSE:{"accept":true}
```

### Mensajes Servidor → Cliente

#### 1. WELCOME - Confirmación de conexión
```json
WELCOME:{
  "message": "Conectado al servidor",
  "playerId": "uuid-del-jugador"
}
```

#### 2. QUESTION - Enviar pregunta
```json
QUESTION:{
  "id": 1,
  "category": "HISTORIA",
  "difficulty": "MEDIA",
  "question": "¿En qué año cayó el Muro de Berlín?",
  "options": ["1987", "1989", "1991", "1985"],
  "timeLimit": 15,
  "questionNumber": 1,
  "totalQuestions": 10,
  "currentTurnPlayer": "NombreJugador"
}
```

#### 3. ANSWER_RESULT - Resultado de respuesta
```json
ANSWER_RESULT:{
  "questionId": 1,
  "correct": true,
  "correctAnswer": 1,
  "points": 20,
  "explanation": "El Muro de Berlín cayó el 9 de noviembre de 1989"
}
```

#### 4. SCORE_UPDATE - Actualización de puntuaciones
```json
SCORE_UPDATE:{
  "players": [
    {
      "name": "Jugador1",
      "score": 85,
      "streak": 5,
      "correctAnswers": 7
    },
    {
      "name": "Jugador2",
      "score": 70,
      "streak": 0,
      "correctAnswers": 5
    }
  ]
}
```

#### 5. GAME_END - Fin de partida
```json
GAME_END:{
  "winner": "Jugador1",
  "finalScores": [
    {"name": "Jugador1", "score": 135, "streak": 2, "correctAnswers": 9},
    {"name": "Jugador2", "score": 100, "streak": 0, "correctAnswers": 7}
  ],
  "correctAnswers": {
    "Jugador1": 9,
    "Jugador2": 7
  }
}
```

#### 6. RECORDS - Envío de records
```json
RECORDS:{
  "players": {
    "Jugador1": {
      "playerName": "Jugador1",
      "bestScore": 150,
      "gamesWon": 5,
      "gamesLost": 2,
      "maxStreak": 8,
      "totalCorrect": 45,
      "totalAnswered": 70,
      "categoryStats": {...},
      "difficultyStats": {...}
    }
  }
}
```

#### 7. PVP_MATCHED - Oponente encontrado
```json
PVP_MATCHED:{"opponent":"NombreOponente"}
```

#### 8. SEARCHING_MATCH - Buscando oponente
```json
SEARCHING_MATCH:{}
```

#### 9. MATCHMAKING_CANCELLED - Búsqueda cancelada
```json
MATCHMAKING_CANCELLED:{}
```

#### 10. OPPONENT_DISCONNECTED - Oponente desconectado
```json
OPPONENT_DISCONNECTED:{"message":"Tu oponente se ha desconectado"}
```

#### 11. ERROR - Error del servidor
```json
ERROR:{"message":"Descripción del error"}
```

---

## Manual de Usuario

### Inicio Rápido

#### 1. Conectarse al Servidor
1. Asegúrate de que el servidor esté ejecutándose
2. Inicia el cliente (composeApp)
3. Ingresa tu nombre de usuario
4. Presiona "Conectar"

#### 2. Menú Principal
Una vez conectado, verás el menú principal con las siguientes opciones:

##### Jugar Solo (PVE)
- Juega contra el servidor
- Intenta superar tu mejor puntuación
- Ideal para practicar

##### Jugar PVP
- Compite contra otro jugador real
- El sistema te emparejará automáticamente
- Gana quien tenga más puntos al final

##### Records
- Consulta las estadísticas globales
- Ve tu posición en el ranking
- Revisa tus estadísticas personales

##### Configuración
- Personaliza tu experiencia de juego
- Ajusta número de preguntas
- Elige dificultad y modo de juego

##### Salir
- Cierra la aplicación

### Configuración del Juego

#### Número de Preguntas
Elige cuántas preguntas quieres responder por partida:
- **3 preguntas**: Partida rápida (~2 minutos)
- **5 preguntas**: Partida corta (~3 minutos)
- **10 preguntas**: Partida estándar (~7 minutos)
- **20 preguntas**: Partida larga (~15 minutos)

#### Dificultad
- **Fácil**: Preguntas básicas (10 puntos por respuesta)
- **Media**: Preguntas de dificultad intermedia (15 puntos por respuesta)
- **Difícil**: Preguntas complejas (20 puntos por respuesta)
- **Mixta**: Combinación de todas las dificultades

#### Modos de Juego

##### Por Turnos (Solo PVP)
- Los jugadores responden alternadamente
- No puedes responder si no es tu turno
- El indicador muestra de quién es el turno

##### Simultáneo
- Todos ven la misma pregunta al mismo tiempo
- Responde cuando quieras
- El más rápido no obtiene ventaja especial

##### Contrarreloj
- Cada pregunta tiene tiempo límite
- Elige entre 10, 15 o 30 segundos
- Si se acaba el tiempo, cuenta como respuesta incorrecta
- Bonus de velocidad por responder en menos de 5 segundos

### Durante la Partida

#### Interfaz de Juego

**Parte Superior:**
- **Modo de juego**: Indica el modo actual
- **Indicador de turno**: (Solo en modo Por Turnos) Muestra de quién es el turno
- **Panel de puntuación**: Muestra tus puntos, racha y preguntas correctas
- **Tiempo**: (Solo en Contrarreloj) Cuenta regresiva

**Parte Central:**
- **Barra de progreso**: Muestra el avance en la partida
- **Categoría**: Indica la categoría de la pregunta actual
- **Pregunta**: El texto de la pregunta
- **Opciones**: 4 botones (A, B, C, D)

**Feedback:**
- **Verde**: Respuesta correcta 
- **Rojo**: Respuesta incorrecta 
- Se muestra la explicación de la respuesta
- Los puntos ganados aparecen en pantalla

#### Sistema de Racha
- **Racha**: Respuestas correctas consecutivas
- **Bonus de racha**: A partir de 5 correctas seguidas, obtienes el **doble de puntos**
- **Indicador visual**: El icono 🔥 aparece cuando tienes racha de 5+
- **Animación**: La racha se anima visualmente cuando la alcanzas

### Pantalla de Resultados

Al finalizar la partida verás:
- **Resultado**: Si ganaste o perdiste
- **Puntuación final**
- **Respuestas correctas**
- **Racha máxima**
- **Clasificación** (en PVP)

**Opciones:**
- **Revancha** (PVP): Solicita jugar otra partida con el mismo oponente
- **Jugar de nuevo** (PVE): Inicia otra partida con la misma configuración
- **Menú**: Vuelve al menú principal

### Consejos y Trucos

#### Para Maximizar tu Puntuación
1. **Mantén la racha**: A partir de 5 correctas seguidas, duplicas los puntos
2. **Responde rápido**: Menos de 5 segundos = +5 puntos extra
3. **Juega en difícil**: Las preguntas difíciles dan el doble de puntos base
4. **Practica en PVE**: Conoce las preguntas antes de competir en PVP

#### Estrategia en Modo Por Turnos
- Tómate tu tiempo cuando sea tu turno
- Observa el progreso de tu oponente
- Mantén la calma si vas perdiendo

#### Estrategia en Modo Contrarreloj
- Lee la pregunta completa antes de responder
- Ten en cuenta el bonus de velocidad
- No te apresures si no estás seguro

---

## Configuración

### Archivo `server.properties`

Ubicación: `/server/src/main/resources/server.properties`

```properties
# Configuración del Servidor Trivial Multijugador
server.host=localhost
server.port=5678
max.clients=10
```

**Parámetros:**
- `server.host`: Dirección IP del servidor (usar `0.0.0.0` para aceptar conexiones de cualquier IP)
- `server.port`: Puerto TCP (default: 5678)
- `max.clients`: Número máximo de clientes simultáneos

### Archivo `records.json`

Ubicación: `/server/records.json`

Se genera automáticamente. Contiene todas las estadísticas de los jugadores.

**Estructura:**
```json
{
  "players": {
    "NombreJugador": {
      "playerName": "NombreJugador",
      "bestScore": 150,
      "gamesWon": 5,
      "gamesLost": 2,
      "maxStreak": 8,
      "totalCorrect": 45,
      "totalAnswered": 70,
      "categoryStats": {
        "HISTORIA": {"correct": 10, "total": 15},
        "CIENCIA_NATURALEZA": {"correct": 8, "total": 12}
      },
      "difficultyStats": {
        "FACIL": {"correct": 20, "total": 25},
        "MEDIA": {"correct": 15, "total": 30}
      },
      "totalResponseTime": 85000,
      "lastPlayed": 1704067200000
    }
  }
}
```

### Archivo `questions.json`

Ubicación: `/server/src/main/resources/questions.json`

Contiene el banco de preguntas del servidor.

**Estructura:**
```json
{
  "questions": [
    {
      "id": 1,
      "category": "HISTORIA",
      "difficulty": "FACIL",
      "question": "¿En qué año descubrió Cristóbal Colón América?",
      "options": ["1492", "1500", "1480", "1510"],
      "correctAnswer": 0,
      "explanation": "Colón llegó a América el 12 de octubre de 1492"
    }
  ]
}
```

### Sonidos Personalizados

Ubicación: `/composeApp/src/jvmMain/resources/sounds/`

Puedes reemplazar los archivos MP3:
- `correct.mp3`: Sonido de respuesta correcta
- `incorrect.mp3`: Sonido de respuesta incorrecta

Si no hay archivos MP3, el juego usará tonos generados por código.

---

## Sistema de Puntuación

### Puntos Base
- **Respuesta correcta**: 10 puntos

### Multiplicador por Dificultad
- **Fácil**: ×1 (10 puntos)
- **Media**: ×1.5 (15 puntos)
- **Difícil**: ×2 (20 puntos)

### Bonus de Velocidad
- **Menos de 5 segundos**: +5 puntos adicionales

### Multiplicador de Racha
- **5+ correctas seguidas**: ×2 a todos los puntos ganados
- La racha se reinicia al fallar una pregunta

### Bonus PVP
- **Responder correctamente más rápido que tu oponente**: +5 puntos adicionales

### Ejemplo de Cálculo

**Pregunta Difícil + Respuesta en 4 segundos + Racha de 6:**
```
Puntos base:           10
× Dificultad (×2):     20
+ Bonus velocidad:     25
× Racha (×2):          50 puntos totales
```

**Pregunta Media + Respuesta en 8 segundos + Sin racha:**
```
Puntos base:           10
× Dificultad (×1.5):   15 puntos totales
```

---

## Tecnologías Utilizadas

### Backend
- **Kotlin**: Lenguaje principal
- **Kotlinx.coroutines**: Programación asíncrona
- **Kotlinx.serialization**: Serialización JSON
- **ServerSocket (Java)**: Comunicación TCP

### Frontend
- **Kotlin Multiplatform**: Compartir código entre plataformas
- **Compose Multiplatform**: UI declarativa
- **Material3**: Diseño moderno
- **JLayer**: Reproducción de audio MP3

### Build & Deploy
- **Gradle 8.0**: Sistema de construcción
- **Gradle Wrapper**: Versión consistente de Gradle

---

## Solución de Problemas

### El servidor no inicia
**Problema**: `Address already in use`
**Solución**: El puerto 5678 ya está en uso. Cambia el puerto en `server.properties` o cierra la aplicación que lo usa.

### El cliente no se conecta
**Problema**: `Connection refused`
**Soluciones**:
1. Verifica que el servidor esté ejecutándose
2. Revisa que `server.host` y `server.port` coincidan en cliente y servidor
3. Verifica el firewall de Windows

### No se escuchan los sonidos
**Solución**: Los archivos MP3 no están presentes. El juego funcionará con tonos de respaldo.

### La ventana es muy pequeña
**Solución**: El tamaño de ventana es 1200×800 por defecto. Puedes maximizar la ventana o cambiar el tamaño en `main.kt`:
```kotlin
state = WindowState(width = 1400.dp, height = 900.dp)
```

---

## Autores

- Desarrollado por: [Saúl Fernández Torres]
- Curso: Programación de Servicios y Procesos
- Año: 2024-2025
