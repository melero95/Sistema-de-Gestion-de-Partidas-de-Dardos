# Sistema de Gestión de Partidas y Estadísticas de Dardos

Aplicación Android desarrollada en Java para gestionar partidas de diferentes modalidades de juegos de dardos.

El objetivo principal del proyecto es permitir configurar partidas, registrar tiradas, controlar turnos y rondas, mostrar resultados finales y, posteriormente, almacenar un historial completo con estadísticas.

Este repositorio se utiliza para controlar el desarrollo de la aplicación de forma progresiva mediante Git y GitHub.

## Estado actual del proyecto

Proyecto actualmente en desarrollo.

La aplicación ya cuenta con navegación principal, configuración de partidas, varios modos de juego funcionales, pantalla de resultados, revancha, guardado básico de partida en curso, ajustes predeterminados y pantallas informativas.

Actualmente se está trabajando en la mejora de la persistencia, el historial de partidas, el detalle de partidas guardadas, las estadísticas y la futura base de datos SQLite.

## Funcionalidades implementadas

### Pantalla principal

La aplicación dispone de una pantalla principal con acceso a las secciones principales:

- Continuar partida.
- Nueva partida.
- Historial de partidas.
- Ajustes.
- Reglas.
- Ayuda.
- Acerca de.
- Menú lateral de navegación.

El botón de continuar partida se muestra únicamente cuando existe una partida guardada.

### Configuración de nueva partida

La pantalla de configuración permite:

- Seleccionar el modo de juego.
- Elegir el número máximo de rondas.
- Añadir jugadores.
- Eliminar jugadores.
- Seleccionar el nombre de cada jugador.
- Asignar un color a cada jugador.
- Usar la última configuración guardada.
- Crear una nueva partida.
- Mostrar u ocultar ajustes avanzados.

En los modos de Cricket se fuerza un mínimo de dos jugadores.

### Ajustes avanzados de partida

La pantalla de nueva partida incluye una sección de ajustes adicionales que permite configurar opciones como:

- Mantener o no el multiplicador después de cada dardo.
- Seleccionar el número de dardos por turno.
- Activar cierre con doble para los modos de puntuación.
- Aplicar valores por defecto leídos desde `SharedPreferences`.

Estos ajustes se envían a las pantallas de partida para modificar el comportamiento de la lógica de juego.

### Modos de juego implementados

La aplicación incluye lógica para los siguientes modos:

- `301`
- `501`
- `Cricket`
- `Cut Throat Cricket`
- `Double Down`
- `Around the Clock`
- `Shanghai`

Cada modo tiene su propia pantalla o lógica específica según sus reglas.

> Nota: los modos `301` y `501` se escriben entre comillas de código para evitar que algunos visores Markdown los interpreten como listas numeradas o números romanos.

### Partidas de puntuación

Los modos `301` y `501` permiten:

- Controlar la puntuación de cada jugador.
- Registrar tiradas con multiplicador.
- Restar puntos según el valor del dardo.
- Detectar cuándo un jugador llega a cero.
- Controlar si un jugador se pasa de puntuación.
- Aplicar cierre con doble cuando la opción está activada.
- Avanzar de turno.
- Deshacer la última tirada.
- Finalizar la partida y mostrar el resultado.

### Partidas de Cricket

Los modos `Cricket` y `Cut Throat Cricket` permiten:

- Registrar marcas sobre los objetivos 20, 19, 18, 17, 16, 15 y Bull.
- Controlar cierres por jugador.
- Mostrar el progreso de cierre de cada número.
- Gestionar puntuaciones.
- Detectar el final de la partida cuando un jugador ha cerrado todos los objetivos y va por delante.
- Deshacer tiradas.
- Avanzar turnos.
- Iniciar revancha manteniendo la configuración necesaria.

### Partidas por rondas

Los modos `Double Down`, `Around the Clock` y `Shanghai` utilizan una lógica específica basada en rondas u objetivos.

#### Double Down

Double Down incluye:

- Rondas fijas.
- Puntuación inicial.
- Registro de aciertos simples, dobles y triples.
- Penalización cuando el jugador falla todos los dardos de la ronda.

#### Around the Clock

Around the Clock funciona como una carrera de objetivos:

- Cada jugador tiene un objetivo actual.
- Al acertar, el jugador avanza al siguiente objetivo.
- Si acierta con doble, avanza dos posiciones.
- El marcador muestra el objetivo actual de cada jugador.
- El resultado final se envía según el orden de finalización.

#### Shanghai

Shanghai incluye:

- Objetivo único por ronda.
- Suma de puntos según aciertos simples, dobles o triples.
- Control de rondas.
- Clasificación final por puntuación.

### Pantalla de resultados

La pantalla de resultados muestra:

- Ganador de la partida.
- Clasificación final.
- Posición de cada jugador.
- Puntuación final.
- Colores asociados a los jugadores.
- Iconos personalizados de copa, plata y bronce.
- Botón para volver al inicio.
- Botón de revancha.
- Acceso previsto a estadísticas.

La opción de revancha permite iniciar una nueva partida usando los datos de la partida finalizada.

### Guardado de partida

La aplicación utiliza `SharedPreferences` para guardar información básica sobre partidas en curso.

Actualmente se guarda si existe una partida activa para poder mostrar u ocultar el botón de continuar partida desde la pantalla principal.

También se están utilizando clases de estado para almacenar y recuperar información de partidas según el tipo de juego.

### Última configuración

La aplicación permite guardar y recuperar la última configuración usada para crear una partida.

Esto facilita iniciar nuevas partidas con los mismos jugadores, colores, modo de juego y ajustes avanzados.

### Ajustes predeterminados

La pantalla de ajustes permite preparar valores por defecto que después se aplican en la configuración de nueva partida.

Entre los valores gestionados se encuentran:

- Jugador por defecto.
- Modo de juego por defecto.
- Número de dardos por turno.
- Mantener multiplicador.
- Cierre con doble.
- Orden aleatorio de turnos.

### Pantallas informativas

La aplicación incluye pantallas informativas para mejorar la navegación y la documentación interna de la app:

- Pantalla de reglas.
- Pantalla de ayuda.
- Pantalla de acerca de.

La pantalla de reglas incluye un selector para mostrar todas las reglas o filtrar por modo de juego.

## Tecnologías utilizadas

- Java.
- Android Studio.
- XML.
- Android SDK.
- AppCompat.
- Material Components.
- ConstraintLayout.
- RecyclerView.
- SharedPreferences.
- SQLite.
- Git.
- GitHub.

## Estructura del proyecto

El proyecto está organizado en varios paquetes principales:

```text
activities/
    Contiene las pantallas principales de la aplicación.

adapters/
    Contiene los adaptadores usados para listas, clasificaciones y elementos dinámicos.

modelos/
    Contiene las clases de datos utilizadas por la aplicación.

sqlite/
    Contendrá las clases relacionadas con la base de datos SQLite.
```

## Pantallas principales

Actualmente el proyecto cuenta con las siguientes pantallas:

- `MainActivity`
- `ConfigurarNuevaPartidaActivity`
- `PartidaPuntosActivity`
- `PartidaCriquetActivity`
- `PartidaRondasActivity`
- `ResultadoActivity`
- `AjustesActivity`
- `ReglasActivity`
- `AyudaActivity`
- `AcercaDeActivity`

También están previstas o en desarrollo:

- `HistorialPartidasActivity`
- `DetallePartidaActivity`
- Pantallas de estadísticas.
- Pantallas de récords.

## Modelos y clases de apoyo

El proyecto utiliza diferentes clases para representar los datos de la aplicación.

Entre ellas se encuentran:

- Jugadores.
- Resultados de jugadores.
- Estados de partida.
- Datos necesarios para clasificaciones.
- Datos enviados entre actividades mediante `Intent`.
- Clases auxiliares para partidas en curso.

También se utilizan listas de jugadores, colores, puntuaciones, posiciones e índices originales para mantener la información correctamente al pasar de una pantalla a otra.

## Navegación

La navegación principal se realiza desde:

- Botones de la pantalla principal.
- Menú lateral.
- Botones de acción dentro de cada pantalla.
- Intents entre actividades.

La aplicación permite volver al inicio desde la pantalla de resultados y confirmar la salida cuando el usuario intenta abandonar una partida en curso.

## Diseño de la interfaz

La interfaz utiliza un diseño visual personalizado con:

- Fondo propio.
- Toolbar superior.
- Menú lateral.
- Botones personalizados.
- Colores por jugador.
- Tarjetas visuales.
- Iconos propios.
- Pantalla de resultados con trofeo y clasificación.

Los colores principales de la aplicación son azul y dorado.

## Persistencia de datos

Actualmente se utilizan `SharedPreferences` para:

- Detectar si existe una partida guardada.
- Guardar la última configuración utilizada.
- Mantener información básica de partidas en curso.
- Guardar ajustes predeterminados de la aplicación.

Como mejora futura se utilizará SQLite para almacenar:

- Partidas finalizadas.
- Jugadores.
- Tiradas.
- Resultados.
- Estadísticas.
- Historial completo.
- Récords.

## Decisión sobre estadísticas de jugadores

La tabla de jugadores se utilizará únicamente para datos propios del jugador, como identificador, nombre, estado activo y preferencias.

No se almacenarán estadísticas acumuladas directamente en la tabla de jugadores, como victorias, derrotas, partidas jugadas o porcentajes.

Las estadísticas se calcularán mediante consultas sobre las partidas, resultados y tiradas almacenadas en la base de datos.

## Funcionalidades pendientes

Las principales tareas pendientes son:

- Completar la pantalla de ajustes.
- Implementar el historial de partidas.
- Implementar el detalle de una partida guardada.
- Guardar partidas finalizadas en SQLite.
- Crear estadísticas generales.
- Crear estadísticas por jugador.
- Crear récords.
- Mejorar el sistema de guardado y recuperación de partidas.
- Añadir sonidos y vibración.
- Añadir más opciones de configuración.
- Mejorar las animaciones.
- Pulir la interfaz en distintas resoluciones.
- Revisar y limpiar código.
- Añadir documentación interna al código.

## Objetivos del proyecto

Los objetivos principales del proyecto son:

- Crear una aplicación Android funcional para partidas de dardos.
- Practicar el desarrollo de aplicaciones móviles en Java.
- Aplicar navegación entre actividades.
- Gestionar datos entre pantallas.
- Usar componentes visuales personalizados.
- Implementar lógica real de juego.
- Guardar información de partidas.
- Preparar una base para estadísticas e historial.
- Mantener un repositorio organizado con control de versiones.

## Control de versiones

El desarrollo se está registrando mediante Git y GitHub.

La intención es realizar commits progresivos para dejar constancia de la evolución del proyecto, desde la estructura inicial hasta la implementación de cada funcionalidad.

Ejemplos de commits recomendados:

```text
Añadir pantalla principal con navegación
Implementar configuración de nueva partida
Añadir lógica de partidas 301 y 501
Implementar modo Cricket
Añadir pantalla de resultados
Guardar estado básico de partida en SharedPreferences
Preparar pantalla de ajustes
Añadir pantalla de reglas
Actualizar README con el estado actual del proyecto
```

## Próximos pasos

Las siguientes mejoras previstas son:

1. Terminar el sistema de guardado de partidas en curso.
2. Implementar SQLite para historial y estadísticas.
3. Crear la pantalla de historial de partidas.
4. Crear la pantalla de detalle de partida.
5. Añadir estadísticas por jugador.
6. Añadir récords.
7. Revisar el diseño visual final.
8. Preparar documentación final del proyecto.

## Autor

Proyecto desarrollado por Luis como aplicación Android de gestión de partidas y estadísticas de dardos.
