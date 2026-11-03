CORRECCION ANDROID LINT: 302 HARDCODEDTEXT
Sistema de Gestion de Partidas de Dardos

ORIGEN: rama main, commit 6c5b92fd941cf02d3427cbdfa15fb82a34d8da99
MODIFICACIONES: 23 archivos layout/menu y res/values/strings.xml.
302 sustituciones de textos XML por @string y 188 nuevos recursos.
NO SE MODIFICA NINGUN ARCHIVO JAVA.

IMPORTANTE
Este ZIP contiene el parche COMPLETO, no una copia de los XML ya editados.
Se aplica a tus ficheros actuales y produce exactamente esos XML modificados.
No hace push ni commit a GitHub. Conserva el commit de respaldo previo.

COMO APLICAR EN WINDOWS (una sola vez)
1. Extrae los tres archivos del ZIP en la carpeta raiz de tu proyecto
   Juegosdedardos (donde esta .git y gradlew).
2. Haz doble clic en APLICAR_CAMBIOS.bat.
   Como alternativa, abre PowerShell en esa carpeta.
3. Ejecuta:
   powershell -ExecutionPolicy Bypass -File .\APLICAR_CAMBIOS.ps1
4. Se genera tambien Dardos_XML_Corregidos.zip con los 24 XML completos.
   Si Git confirma la aplicacion correcta, ejecuta:
   .\gradlew :app:assembleDebug
   .\gradlew :app:lintDebug
5. Comprueba que HardcodedText pasa de 302 a 0 si la version es la misma.

PARA REVERTIR (si NO has hecho otros cambios posteriores):
   git restore -- app/src/main/res/layout app/src/main/res/menu app/src/main/res/values/strings.xml

Si alguna ruta ha cambiado desde el commit de referencia, git apply --check
se detendra ANTES de modificar ningun archivo.

INCLUIDO:
 - lint_hardcoded_302.patch: 302 sustituciones y strings.xml completo
 - APLICAR_CAMBIOS.ps1 y .bat: validacion y aplicacion en el proyecto local
 - strings.xml_resultado: vista del archivo final esperado
 - manifiesto_cambios.json: ruta, linea, nombre del recurso, texto original
 - comprobaciones.txt: inventario de incidencias por archivo

Los mensajes, estilos, IDs y atributos no relacionados con el texto
permanecen sin cambios. No se internacionaliza Java (SetTextI18n).
