import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.storage.StorageLevel
import org.apache.log4j.{Level, Logger}


object spark_app {
  def main(args: Array[String]): Unit = {
    // Configurar el nivel de logging
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    // Crear sesión de Spark
    val spark = SparkSession.builder()
      .appName("Optimized Spark DataFrames")
      .master("local[*]") // Ejecutar localmente con todos los cores
      .getOrCreate()

    import spark.implicits._

    // Crear df1 con 4000 filas
    // Ajusta el número de particiones según tus necesidades
    val df1 = (1 to 4000).map(i => (i, s"value_$i")).toDF("id", "value").repartition(4)

    // Crear df2 con 1000 filas
    val df2 = (1 to 1000).map(i => (i, s"info_$i")).toDF("id", "info").repartition(4)

    // Crear df3 con 10 filas
    val df3 = (1 to 10).map(i => (i, s"small_$i")).toDF("id", "extra")

    // Realizar Left Join entre df1 y df2
    // Optimización: seleccionar solo columnas necesarias
    // Ajusta el número de particiones según tus necesidades
    val df4 = df1.join(df2, Seq("id"), "left")
      .select(df1("id"), df1("value"), df2("info"))
      .repartition(4)

    // Cachear df4 ya que será reutilizado
    df4.cache()

    // Realizar Join entre df4 y df3 con optimización de broadcast
    // Optimización: seleccionar solo columnas necesarias
    val df5 = df4.join(broadcast(df3), Seq("id"))
      .select(df4("id"), df4("value"), df3("extra"))

    // Persistir df5 en memoria (solo memoria si los datos son pequeños)
    df5.persist(StorageLevel.MEMORY_ONLY)

    // Mostrar los resultados finales (solo primeros 10)
    // Mostrar solo 10 filas de df4
    println("Resultado Final: DataFrame df4 (4000 filas)")
    //df4.show(10, truncate = false)

    // Mostrar solo 10 filas de df5
    println("Resultado Final: DataFrame df5 (10 filas)")
    df5.show(10, truncate = false)

    // Limpiar caché y persistencia
    df4.unpersist()
    df5.unpersist()

    // Detener la sesión de Spark
    spark.stop()
  }
}