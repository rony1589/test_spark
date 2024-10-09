# Test Spark
## Necesidad
1. Utilizar intellij para crear una app de Spark utilizando Scala y Maven, no hay restricción de versiones
2. Crear 3 dataframes, df1, df2 y df3
3. Reproducir las operaciones que se ilustran en la grafica
   ![grafico_necesidad.png](src%2Fmain%2Fresources%2Fgrafico_necesidad.png)
4.	Buscar formas de optimizar el proceso para obtener df5

## Solución
Este proyecto es una aplicación de Spark optimizada para trabajar con DataFrames. Utiliza joins eficientes y caché para mejorar el rendimiento.

## Requisitos
- Scala 2.12.x
- Apache Spark 3.3.x
- Java 8 o superior

## Instalación
Para instalar el proyecto, clona el repositorio y ejecuta:

```bash
sbt compile
```
## Ejecución
Para ejecutar la aplicación, usa el siguiente comando:

```bash
sbt run
```

## Configuración log4j2
El archivo log4j2.properties esta en src/main/resources/ y se utiliza para la configuración del logging:

- Nivel de log: El nivel de log por defecto se establece en ERROR para reducir la cantidad de mensajes generados. Puedes cambiar esto a INFO, WARN, o DEBUG para obtener más detalles.
- Appender de consola: Se configura un appender de consola para mostrar los logs en la salida estándar.

## Estructura
```bash
test_spark
├── README.md
├── src/
│   ├── main/
│   │   ├── scala/
│   │   │   └── spark_app.scala
│   │   └── resources/
│   │       └── log4j2.properties
│   └── test/
└── build.sbt
```
## Explicación
Para entender cómo se llega al resultado de df5, analizaremos cada paso y cómo se relacionan los DataFrames:
```bash
1. Creación de DataFrames:
   df1 Contiene 4000 filas con columnas id y value.
   df2: Contiene 1000 filas con columnas id y info.
   df3: Contiene 10 filas con columnas id y extra.

2. Left Join entre df1 y df2
-  En este paso, se realiza un left join entre df1 y df2 basado en la columna id.
-  df4 contendrá todas las filas de df1 y las filas coincidentes de df2. Si no hay coincidencia, info será null.

3. Cacheo de df4
-  df4.cache() almacena df4 en memoria para optimizar futuras operaciones, ya que se reutilizará en el siguiente join.

4. Join entre df4 y df3
-  Aquí se realiza un join entre df4 (que tiene 4000 filas) y df3 (que tiene 10 filas) usando broadcast(df3).
-  El uso de broadcast es una optimización para el caso en que uno de los DataFrames es pequeño (como df3). Esto evita el movimiento de datos grandes al ejecutar el join, haciendo que la operación sea más rápida.
-  df5 contendrá las filas de df4 que tienen coincidencias en df3, incluyendo las columnas id, value y extra.

5. Persistencia de df5
-  df5.persist(StorageLevel.MEMORY_ONLY) almacena df5 en memoria, lo que es eficiente en este caso porque los datos son pequeños.

6. df5 contendrá solo las filas de df4 que tienen coincidencias en df3, lo que significa que solo tendrá filas con id en el rango de 1 a 10, dado que df3 solo tiene esos ids.

**Optimizaciones incluidas**
-  Se seleccionan solo las columnas necesarias y utilizaamos broadcast.
-  Particiones para optimizar el rendimiento(Se usa cuando los df son grandes)
-  Uso de persist y cache para df pequeños en caso de se grandes se debe usar df.persist(StorageLevel.MEMORY_AND_DISK)

```
## Resultado.
![resultado.png](src%2Fmain%2Fresources%2Fresultado.png)