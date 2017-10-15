package models.mains

import java.io.PrintWriter
import models.SchemaDefinition
import sangria.renderer.SchemaRenderer

object GenerateSchemaGraphql {
  def main(args: Array[String]) {
    val schemaGraphql: String = SchemaRenderer.renderSchema(SchemaDefinition.schema)
    new PrintWriter("./ui/src/data/schema.graphql") {
      write(schemaGraphql)
      close()
    }
  }
}
