package models.mains

import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import com.typesafe.config.ConfigFactory

object SlickDatabaseGenerator extends App {
  val conf = ConfigFactory.load()
  val db = Database.forURL(conf.getString("slick.dbs.default.db.url"), user = conf.getString("slick.dbs.default.db.user"), password = conf.getString("slick.dbs.default.db.password"), driver = conf.getString("slick.dbs.default.db.driver"))
  Await.ready(db.run(models.Tables.schema.create), Duration.Inf)
}