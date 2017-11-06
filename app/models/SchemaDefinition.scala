package models

import sangria.schema._
import sangria.relay._
import FoodieData._
import FoodieData.ConnectionFactory._
import play.api.libs.json._
import sangria.execution.deferred._
import sangria.marshalling.playJson._

object SchemaDefinition {

  val NodeDefinition(nodeInterface, nodeField, nodesField) =
    Node.definition((globalId: GlobalId, c: Context[FoodieRepo, Unit]) ⇒ {
      if (globalId.typeName == "Food")
        c.ctx.getFood(globalId.id)
      else if (globalId.typeName == "Type")
        c.ctx.getType(globalId.id)
      else
        None
    }, Node.possibleNodeTypes[FoodieRepo, Node](FoodType, TypeType))

  def idFields[T: Identifiable]: List[Field[Unit, T]] = fields[Unit, T](
    Node.globalIdField,
    Field("rawId", StringType, resolve = ctx ⇒ implicitly[Identifiable[T]].id(ctx.value))
  )

  val types: Fetcher[FoodieRepo, TypeData, TypeData, String] =
    Fetcher((ctx: FoodieRepo, ids: Seq[String]) => ctx.loadTypesByFood(ids))(HasId(_.id))

  val byType: Relation[FoodData, FoodData, String] = Relation[FoodData, String]("byType", c => Seq(c.type_id))

  val foods: Fetcher[FoodieRepo, FoodData, FoodData, String] = Fetcher.rel[FoodieRepo, FoodData, FoodData, String](
    (repo, ids) => repo.loadFoods(ids),
    (repo, ids) => repo.loadFoodsByRelation(ids(byType)))(HasId(_.type_id))

  val resolver: DeferredResolver[FoodieRepo] =
    DeferredResolver.fetchers(foods, types)

  lazy val FoodType: ObjectType[Unit, FoodData] = ObjectType(
    "Food",
    "The details of a food",
    interfaces[Unit, FoodData](nodeInterface),
    () => idFields[FoodData] ++
      fields[Unit, FoodData](
        Field("name", StringType, resolve = _.value.name),
        Field("image", OptionType(StringType),
          description = Some("Image CDN URL"),
          resolve = _.value.image),
        Field("type", TypeType, resolve = c => types.defer(c.value.type_id))
      ))

  val ConnectionDefinition(foodEdge, foodConnection) = Connection.definition[FoodieRepo, FoodieConnection, Option[FoodData]]("Food", OptionType(FoodType), connectionFields = List(Field("count", IntType, resolve = ctx => ctx.value.count)))

  val ConnectionDefinition(typeEdge, typeConnection) = Connection.definition[FoodieRepo, FoodieConnection, Option[TypeData]]("Type", OptionType(TypeType), connectionFields = List(Field("count", IntType, resolve = ctx => ctx.value.count)))

  lazy val TypeType: ObjectType[Unit, TypeData] = ObjectType(
    "Type",
    "The details of a type",
    interfaces[Unit, TypeData](nodeInterface),
    () => idFields[TypeData] ++
      fields[Unit, TypeData](
        Field("name", StringType, resolve = _.value.name),
        Field("foods", ListType(FoodType), resolve = c => foods.deferRelSeq(byType, c.value.id))
      ))

  val Id = Argument("id", StringType)

  val Sort = Argument("sort", OptionInputType(StringType), "asc")

  val QueryType = ObjectType("Query", fields[FoodieRepo, Unit](
    Field("food", OptionType(FoodType),
      description = Some("Returns a food with specific `id`."),
      arguments = Id :: Nil,
      resolve = c => c.ctx.getFood(c arg Id)),

    Field("foods", OptionType(foodConnection),
      description = Some("Returns a list of all available foods."),
      arguments = Sort :: Connection.Args.All,
      resolve = c => c.ctx.getFoods(ConnectionArgs(c), c arg Sort)),

    Field("type", OptionType(TypeType),
      description = Some("Returns a type with specific `id`."),
      arguments = Id :: Nil,
      resolve = c => c.ctx.getType(c arg Id)),

    Field("types", OptionType(typeConnection),
      description = Some("Returns a list of all available types."),
      arguments = Sort :: Connection.Args.All,
      resolve = c => c.ctx.getTypes(ConnectionArgs(c), c arg Sort)),

    nodeField
  ))

  implicit val typeAddInputFormat: OFormat[TypeAddInput] = Json.format[TypeAddInput]
  val typeAddInput: InputObjectType[TypeAddInput] = InputObjectType[TypeAddInput]("TypeAddInput", "Input object to add a type.", List(
    InputField("name", StringType)
  ))
  val TypeAddInputArg = Argument("input", typeAddInput)

  implicit val typeEditInputFormat: OFormat[TypeEditInput] = Json.format[TypeEditInput]
  val typeEditInput: InputObjectType[TypeEditInput] = InputObjectType[TypeEditInput]("TypeEditInput", "Input object to edit a type.", List(
    InputField("id", StringType),
    InputField("name", StringType)
  ))
  val TypeEditInputArg = Argument("input", typeEditInput)

  val EditedType = ObjectType("EditedType", fields[Unit, EditedType](
    Field("type", TypeType, resolve = _.value.`type`)
  ))

  implicit val typeDeleteInputFormat: OFormat[TypeDeleteInput] = Json.format[TypeDeleteInput]
  val typeDeleteInput: InputObjectType[TypeDeleteInput] = InputObjectType[TypeDeleteInput]("TypeDeleteInput", "Input object to delete a type.", List(
    InputField("id", IDType)
  ))
  val TypeDeleteInputArg = Argument("input", typeDeleteInput)

  val DeletedType = ObjectType("DeletedType", fields[Unit, DeletedType](
    Field("deletedTypeId", IDType, resolve = _.value.id)
  ))

  implicit val foodAddInputFormat: OFormat[FoodAddInput] = Json.format[FoodAddInput]
  val foodAddInput: InputObjectType[FoodAddInput] = InputObjectType[FoodAddInput]("FoodAddInput", "Input object to add a food.", List(
    InputField("name", StringType),
    InputField("type_id", StringType)
  ))
  val FoodAddInputArg = Argument("input", foodAddInput)

  implicit val foodEditInputFormat: OFormat[FoodEditInput] = Json.format[FoodEditInput]
  val foodEditInput: InputObjectType[FoodEditInput] = InputObjectType[FoodEditInput]("FoodEditInput", "Input object to edit a food.", List(
    InputField("id", StringType),
    InputField("name", StringType),
    InputField("type_id", StringType)
  ))
  val FoodEditInputArg = Argument("input", foodEditInput)

  val EditedFood = ObjectType("EditedFood", fields[Unit, EditedFood](
    Field("food", FoodType, resolve = _.value.`food`)
  ))

  implicit val foodDeleteInputFormat: OFormat[FoodDeleteInput] = Json.format[FoodDeleteInput]
  val foodDeleteInput: InputObjectType[FoodDeleteInput] = InputObjectType[FoodDeleteInput]("FoodDeleteInput", "Input object to delete a food.", List(
    InputField("id", IDType)
  ))
  val FoodDeleteInputArg = Argument("input", foodDeleteInput)

  val DeletedFood = ObjectType("DeletedFood", fields[Unit, DeletedFood](
    Field("deletedFoodId", IDType, resolve = _.value.id)
  ))

  val MutationType = ObjectType("Mutation", fields[FoodieRepo, Unit](
    Field("addType", typeEdge,
      arguments = TypeAddInputArg :: Nil,
      resolve = c => c.ctx.addType(c arg TypeAddInputArg)),
    Field("editType", EditedType,
      arguments = TypeEditInputArg :: Nil,
      resolve = c => c.ctx.editType(c arg TypeEditInputArg)),
    Field("deleteType", DeletedType,
      arguments = TypeDeleteInputArg :: Nil,
      resolve = c => c.ctx.deleteType(c arg TypeDeleteInputArg)),
    Field("addFood", foodEdge,
      arguments = FoodAddInputArg :: Nil,
      resolve = c => c.ctx.addFood(c arg FoodAddInputArg)),
    Field("editFood", EditedFood,
      arguments = FoodEditInputArg :: Nil,
      resolve = c => c.ctx.editFood(c arg FoodEditInputArg)),
    Field("deleteFood", DeletedFood,
      arguments = FoodDeleteInputArg :: Nil,
      resolve = c => c.ctx.deleteFood(c arg FoodDeleteInputArg))
  ))

  val schema = Schema(QueryType, Some(MutationType))

}