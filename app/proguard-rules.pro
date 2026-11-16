# GSON + R8: conservar los tipos genéricos y los nombres de los campos
# usados por SharedPreferences y por la transmisión de resultados JSON.
# Sin estas reglas, release puede convertir las listas de DTO en LinkedHashMap.
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# Mantener ESTABLE el formato JSON de los modelos propios a partir de esta versión.
# El historial y las partidas en curso usan estos modelos tanto en Gson como
# en otras pantallas de la aplicación.
-keep class modelos.** { *; }

# Objeto de finalización serializado entre Activity y servicio SQLite.
-keep class utilidades.DatosFinalizacionPartida { *; }
