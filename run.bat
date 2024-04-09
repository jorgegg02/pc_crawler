@REM Modificando codificación de caracteres del terminal...
chcp 65001

@REM Borrando archivos compilados anteriormente y los .ser
del *.class
del *.ser

@REM Compilando y ejecutando el programa...
javac -cp .;lib/* pc_Crawler.java Ocurrencia.java
java -cp .;lib/* Crawler ./prueba2

