package models

import Tables._
import com.typesafe.config.ConfigFactory
import models.FoodieData.ConnectionFactory._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.JdbcBackend
import sangria.relay._
import sangria.relay.Connection._
import scala.concurrent.Future
import sangria.execution.UserFacingError
import scala.concurrent.ExecutionContext.Implicits.global

object FoodieData {
  val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("slick.dbs.default", ConfigFactory.load())
  val db: JdbcBackend#DatabaseDef = dbConfig.db

  import dbConfig.profile.api._

  object ConnectionFactory {

    case class FoodieConnection[T](pageInfo: PageInfo, edges: Seq[Edge[T]], count: Int) extends sangria.relay.Connection[T]

    class ConnectionProcessor[E, T](query: Query[E, T, Seq], connectionArgs: ConnectionArgs, count: Int) {

      import connectionArgs._

      private def getOffset(cursor: Option[String], defaultOffset: Int) =
        cursor flatMap cursorToOffset getOrElse defaultOffset

      def validateConnectionArgs(): Unit = {
        first.foreach(f => if (f < 0) throw ConnectionArgumentValidationError("Argument 'first' must be a non-negative integer"))
        last.foreach(l => if (l < 0) throw ConnectionArgumentValidationError("Argument 'last' must be a non-negative integer"))
      }

      var beforeOffset, afterOffset, actualStartOffset, actualEndOffset: Int = 0

      def getQuery: Query[E, T, Seq] = {
        validateConnectionArgs()
        val sliceEnd = count
        beforeOffset = getOffset(before, count)
        afterOffset = getOffset(after, -1)
        val startOffset = math.max(math.max(-1, afterOffset), -1) + 1
        val endOffset = math.min(math.min(sliceEnd, beforeOffset), count)
        actualEndOffset = first.fold(endOffset)(f => math.min(endOffset, startOffset + f))
        actualStartOffset = last.fold(startOffset)(l => math.max(startOffset, actualEndOffset - l))
        val outQuery = if (first.isDefined) {
          if (after.isDefined) if (afterOffset + 1 != 0) query.drop(afterOffset + 1).take(first.get) else query.take(0) else query.take(first.get)
        } else if (last.isDefined) {
          if (before.isDefined) if (beforeOffset != 0) query.drop(beforeOffset - last.get).take(last.get) else query.take(0) else query.drop(Math.max(0, count - last.get)).take(last.get)
        } else query
        outQuery
      }

      def connection(l: Seq[Any]): FoodieConnection[Option[Any]] = {
        val edges = l.zipWithIndex.map {
          case (value, index) => Edge(Option(value), offsetToCursor(actualStartOffset + index))
        }
        val firstEdge = edges.headOption
        val lastEdge = edges.lastOption
        val lowerBound = after.fold(0)(_ => afterOffset + 1)
        val upperBound = before.fold(count)(_ => beforeOffset)
        FoodieConnection(
          PageInfo(
            startCursor = firstEdge map (_.cursor),
            endCursor = lastEdge map (_.cursor),
            hasPreviousPage = last.fold(false)(_ => actualStartOffset > lowerBound),
            hasNextPage = first.fold(false)(_ => actualEndOffset < upperBound)),
          edges, count)
      }
    }

    type RelayConnectionDBIOAction[T] = DBIOAction[FoodieConnection[Option[T]], NoStream, Effect.Read with Effect.Read]

    def inProcess[E, T, V](query: Query[E, T, Seq], connectionArgs: ConnectionArgs, converter: Option[Seq[T] => Seq[V]]): RelayConnectionDBIOAction[Any] = {
      query.length.result flatMap { count =>
        val c = new ConnectionProcessor(query, connectionArgs, count)
        c.getQuery.result map { v => c.connection(if (converter.isDefined) converter.get(v.toList) else v.toList) }
      }
    }

    def process[E, T](query: Query[E, T, Seq], connectionArgs: ConnectionArgs): Future[FoodieConnection[Option[T]]] =
      db.run(inProcess(query, connectionArgs, None).asInstanceOf[RelayConnectionDBIOAction[T]].transactionally)

    def process[E, T, V](query: Query[E, T, Seq], connectionArgs: ConnectionArgs, converter: Seq[T] => Seq[V]): Future[FoodieConnection[Option[V]]] =
      db.run(inProcess(query, connectionArgs, Some(converter)).asInstanceOf[RelayConnectionDBIOAction[V]].transactionally)
  }

  case class FoodData(id: String, name: String, image: Option[String] = None, type_id: String) extends Node

  case class TypeData(id: String, name: String) extends Node

  case class TypeAddInput(name: String)

  case class TypeEditInput(id: String, name: String)

  case class EditedType(`type`: TypeData)

  case class TypeDeleteInput(id: String)

  case class DeletedType(id: String)

  case class FoodAddInput(name: String, type_id: String)

  case class FoodEditInput(id: String, name: String, type_id: String)

  case class EditedFood(`food`: FoodData)

  case class FoodDeleteInput(id: String)

  case class DeletedFood(id: String)

  class FoodieRepo {
    private def foodConvert(food: FoodRow): FoodData = FoodData(id = food.id.toString, name = food.name, image = food.image, type_id = food.typeId.toString)

    private def foodListConvert(list: Seq[FoodRow]): Seq[FoodData] = list.map(foodConvert)

    def getFood(id: String): Future[Option[FoodData]] =
      db.run(Food.filter(_.id === id.toInt).take(1).result) map { v =>
        if (v.nonEmpty) Some(foodConvert(v.head)) else None
      }

    def getFoods(connectionArgs: ConnectionArgs, sort: Option[String]): Future[FoodieConnection[Option[FoodData]]] =
      ConnectionFactory.process(Food sortBy { f => if (sort.contains("desc")) f.id.desc else f.id.asc }, connectionArgs, foodListConvert)

    private def typeConvert(`type`: TypeRow): TypeData = TypeData(id = `type`.id.toString, name = `type`.name)

    private def typeListConvert(list: Seq[TypeRow]): Seq[TypeData] = list.map(o => typeConvert(o))

    def getType(id: String): Future[Option[TypeData]] =
      db.run(Type.filter(_.id === id.toInt).take(1).result) map { v =>
        if (v.nonEmpty) Some(typeConvert(v.head)) else None
      }

    def getTypes(connectionArgs: ConnectionArgs, sort: Option[String]): Future[FoodieConnection[Option[TypeData]]] =
      ConnectionFactory.process(Type sortBy { t => if (sort.contains("desc")) t.id.desc else t.id.asc }, connectionArgs, typeListConvert)

    def loadFoods(ids: Seq[String]): Future[Seq[FoodData]] = {
      val query = for {f <- Food if f.id inSet ids.map(_.toInt)} yield f
      db.run(query.result) map { v => foodListConvert(v) }
    }

    def loadFoodsByRelation(ids: Seq[String]): Future[Seq[FoodData]] = {
      val query = for {f <- Food if f.typeId inSet ids.map(_.toInt)} yield f
      db.run(query.result) map { v => foodListConvert(v) }
    }

    def loadTypesByFood(ids: Seq[String]): Future[Seq[TypeData]] = {
      val query = for {t <- `Type` if t.id inSet ids.map(_.toInt)} yield t
      db.run(query.result) map { v => typeListConvert(v) }
    }

    case class InvalidTypeName(message: String) extends Exception(message) with UserFacingError

    val invalidTypeNameMessage = "Invalid type name. Please try again."

    def addType(t: TypeAddInput): Future[Edge[Option[TypeData]]] = {
      if (t.name.trim.isEmpty) throw InvalidTypeName(invalidTypeNameMessage)
      val dbAction = for {
        typeWithId <- (Type returning Type.map(_.id) into ((`type`, id) => `type`.copy(id = id))) += TypeRow(0, t.name)
        position <- sql"SELECT rank FROM (SELECT t.id, @rownum := @rownum + 1 AS rank FROM type t, (SELECT @rownum := -1) r ORDER BY t.id) t2 WHERE t2.id = ${typeWithId.id}".as[Int]
      } yield (typeWithId, position)
      db.run(dbAction.transactionally).map { r =>
        val (typeWithId, position) = r
        Edge(Some(typeConvert(typeWithId)), offsetToCursor(position.head))
      }
    }

    case class InvalidTypeId(message: String) extends Exception(message) with UserFacingError

    val invalidTypeIdMessage = "Invalid type id. Please try again."

    def editType(t: TypeEditInput): Future[EditedType] = {
      if (t.name.trim.isEmpty) throw InvalidTypeName(invalidTypeNameMessage)
      val globalId = GlobalId.fromGlobalId(t.id)
      if (globalId.isDefined) {
        val rawId = globalId.get.id.toInt
        val q = for {t <- Type if t.id === rawId} yield t.name
        db.run(q.update(t.name)).map { r =>
          if (r == 0) throw InvalidTypeId(invalidTypeIdMessage)
          else EditedType(TypeData(rawId.toString, t.name))
        }
      } else {
        throw InvalidTypeId(invalidTypeIdMessage)
      }
    }

    case class FoodsExist(message: String) extends Exception(message) with UserFacingError

    def foodsExistMessage(typeId: String) = s"There are foods under type #$typeId. Please try again after deleting all foods of this type."

    def deleteType(t: TypeDeleteInput): Future[DeletedType] = {
      val globalId = GlobalId.fromGlobalId(t.id)
      if (globalId.isDefined) {
        val rawId = globalId.get.id.toInt
        val q = Type filter (_.id === rawId)
        db.run(q.delete).map { r =>
          if (r == 0) throw InvalidTypeId(invalidTypeIdMessage)
          else DeletedType(t.id)
        } recover { case _: java.sql.SQLIntegrityConstraintViolationException => throw FoodsExist(foodsExistMessage(t.id)) }
      } else {
        throw InvalidTypeId(invalidTypeIdMessage)
      }
    }

    case class InvalidFoodName(message: String) extends Exception(message) with UserFacingError

    val invalidFoodNameMessage = "Invalid food name. Please try again."

    def addFood(t: FoodAddInput): Future[Edge[Option[FoodData]]] = {
      if (t.name.trim.isEmpty) throw InvalidFoodName(invalidFoodNameMessage)
      val globalTypeId = GlobalId.fromGlobalId(t.type_id)
      if (globalTypeId.isEmpty) throw InvalidTypeId(invalidTypeIdMessage)
      val dbAction = for {
        foodWithId <- (Food returning Food.map(_.id) into ((`food`, id) => `food`.copy(id = id))) += FoodRow(0, t.name, typeId = globalTypeId.get.id.toInt)
        position <- sql"SELECT rank FROM (SELECT t.id, @rownum := @rownum + 1 AS rank FROM food t, (SELECT @rownum := -1) r ORDER BY t.id) t2 WHERE t2.id = ${foodWithId.id}".as[Int]
      } yield (foodWithId, position)
      db.run(dbAction.transactionally) map { r =>
        val (foodWithId, position) = r
        Edge(Some(foodConvert(foodWithId)), offsetToCursor(position.head))
      }
    }

    case class InvalidFoodId(message: String) extends Exception(message) with UserFacingError

    val invalidFoodIdMessage = "Invalid food id. Please try again."

    def editFood(t: FoodEditInput): Future[EditedFood] = {
      if (t.name.trim.isEmpty) throw InvalidFoodName(invalidFoodNameMessage)
      val globalTypeId = GlobalId.fromGlobalId(t.type_id)
      if (globalTypeId.isEmpty) throw InvalidTypeId(invalidTypeIdMessage)
      val globalId = GlobalId.fromGlobalId(t.id)
      if (globalId.isDefined) {
        val rawId = globalId.get.id.toInt
        val q = for {f <- Food if f.id === rawId} yield (f.name, f.typeId)
        val rawTypeId = globalTypeId.get.id
        db.run(q.update((t.name, rawTypeId.toInt))).map { r =>
          if (r == 0) throw InvalidFoodId(invalidFoodIdMessage)
          else EditedFood(FoodData(rawId.toString, t.name, type_id = rawTypeId))
        }
      } else {
        throw InvalidFoodId(invalidFoodIdMessage)
      }
    }

    def deleteFood(t: FoodDeleteInput): Future[DeletedFood] = {
      val globalId = GlobalId.fromGlobalId(t.id)
      if (globalId.isDefined) {
        val rawId = globalId.get.id.toInt
        val q = Food filter (_.id === rawId)
        db.run(q.delete).map { r =>
          if (r == 0) throw InvalidFoodId(invalidFoodIdMessage)
          else DeletedFood(t.id)
        }
      } else {
        throw InvalidFoodId(invalidFoodIdMessage)
      }
    }
  }

}
