package models.mains

import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.MySQLProfile
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object SlickSchemaCodeGenerator extends App {
  val conf = ConfigFactory.load()
  val db = Database.forURL(conf.getString("slick.dbs.default.db.url"), user = conf.getString("slick.dbs.default.db.user"), password = conf.getString("slick.dbs.default.db.password"), driver = conf.getString("slick.dbs.default.db.driver"))

  import slick.codegen.SourceCodeGenerator

  val modelAction = MySQLProfile.createModel(Some(MySQLProfile.defaultTables))
  val modelFuture = db.run(modelAction)
  val codegenFuture = modelFuture.map(model => new SourceCodeGenerator(model) {

    override def code = "import play.api.libs.json._" + "\n" + super.code

    override def Table = new Table(_) {
      override def EntityType = new EntityType {
        override def code = {
          val args = columns.map(c =>
            c.default.map(v =>
              s"${c.name}: ${c.exposedType} = $v"
            ).getOrElse(
              s"${c.name}: ${c.exposedType}"
            )
          ).mkString(", ")
          if (classEnabled) {
            val prns = (parents.take(1).map(" extends " + _) ++ parents.drop(1).map(" with " + _)).mkString("")
            (if (caseClassFinal) "final " else "") +
              s"""case class $name($args)$prns""" + "\n" +
              s"/** JSON automated mapping for $rawName */" + "\n" +
              s"implicit val ${rawName.substring(0, 1).toLowerCase}${rawName.substring(1)}Format: OFormat[$rawName] = Json.format[$rawName]"
          } else {
            s"""
type $name = $types
/** Constructor for $name providing default values if available in the database schema. */
def $name($args): $name = {
  ${compoundValue(columns.map(_.name))}
}
          """.trim
          }
        }
      }
    }
  })
  Await.ready(codegenFuture.map { codegen =>
    codegen.writeToFile(
      conf.getString("slick.dbs.default.profile").replace("$", ""), "app", "models", "Tables", "Tables.scala"
    )
  }, Duration.Inf)

}