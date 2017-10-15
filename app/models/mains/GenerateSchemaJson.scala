package models.mains

import java.io.PrintWriter
import models.FoodieData.FoodieRepo
import models.SchemaDefinition
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json
import sangria.execution.Executor
import sangria.introspection._
import sangria.marshalling.playJson._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object GenerateSchemaJson {
  def main(args: Array[String]) {
    val futureOfSchemaJson = Executor.execute(SchemaDefinition.schema, introspectionQuery, userContext = new FoodieRepo())

    val schemaJson = Await.ready(futureOfSchemaJson, 5 second).value.get

    schemaJson match {
      case Success(t) =>
        new PrintWriter("./ui/src/data/schema.json") {
          write(Json.prettyPrint(schemaJson.get))
          close()
        }
      case Failure(t) => println("Could not generate schema.json : " + t.getMessage)
    }
  }
}
