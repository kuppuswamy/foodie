package controllers

import javax.inject._
import models.FoodieData.FoodieRepo
import play.api.mvc._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import models.SchemaDefinition
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._
import sangria.marshalling.playJson._
import sangria.execution._
import sangria.parser._
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents)(dbConfigProvider: DatabaseConfigProvider) extends BaseController {
  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def graphiql = Action { implicit request =>
    Ok(views.html.graphiql())
  }

  def graphql(query: String, variables: Option[String], operation: Option[String]): Action[AnyContent] = Action.async(executeQuery(query, variables map parseVariables, operation))

  def graphqlBody: Action[JsValue] = Action.async(parse.json) { request =>
    val query = (request.body \ "query").as[String]
    val operation = (request.body \ "operationName").asOpt[String]
    val variables = (request.body \ "variables").toOption.flatMap {
      case JsString(vars) => Some(parseVariables(vars))
      case obj: JsObject => Some(obj)
      case _ => None
    }
    executeQuery(query, variables, operation)
  }

  private def parseVariables(variables: String) = if (variables.trim == "") Json.obj() else Json.parse(variables).as[JsObject]

  private def executeQuery(query: String, variables: Option[JsObject], operation: Option[String]) =
    QueryParser.parse(query) match {
      // query parsed successfully, time to execute it!
      case Success(queryAst) =>
        Executor.execute(SchemaDefinition.schema, queryAst, new FoodieRepo(),
          operationName = operation,
          variables = variables getOrElse Json.obj(),
          maxQueryDepth = Some(10), deferredResolver = SchemaDefinition.resolver)
          .map(Ok(_))
          .recover {
            case error: QueryAnalysisError ⇒ BadRequest(error.resolveError)
            case error: ErrorWithResolver ⇒ InternalServerError(error.resolveError)
          }
      // can't parse GraphQL query, return error
      case Failure(error: SyntaxError) =>
        Future.successful(BadRequest(Json.obj(
          "syntaxError" -> error.getMessage,
          "locations" -> Json.arr(Json.obj(
            "line" -> error.originalError.position.line,
            "column" -> error.originalError.position.column)))))
      case Failure(error) =>
        throw error
    }
}
