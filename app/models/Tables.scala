package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.MySQLProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import play.api.libs.json._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Food.schema ++ Type.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Food
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(512,true)
   *  @param image Database column image SqlType(VARCHAR), Length(1024,true), Default(None)
   *  @param typeId Database column type_id SqlType(INT) */
  case class FoodRow(id: Int, name: String, image: Option[String] = None, typeId: Int)
  /** JSON automated mapping for FoodRow */
  implicit val foodRowFormat: OFormat[FoodRow] = Json.format[FoodRow]
  /** GetResult implicit for fetching FoodRow objects using plain SQL queries */
  implicit def GetResultFoodRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[String]]): GR[FoodRow] = GR{
    prs => import prs._
    FoodRow.tupled((<<[Int], <<[String], <<?[String], <<[Int]))
  }
  /** Table description of table food. Objects of this class serve as prototypes for rows in queries. */
  class Food(_tableTag: Tag) extends profile.api.Table[FoodRow](_tableTag, Some("foodie"), "food") {
    def * = (id, name, image, typeId) <> (FoodRow.tupled, FoodRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), image, Rep.Some(typeId)).shaped.<>({r=>import r._; _1.map(_=> FoodRow.tupled((_1.get, _2.get, _3, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(512,true) */
    val name: Rep[String] = column[String]("name", O.Length(512,varying=true))
    /** Database column image SqlType(VARCHAR), Length(1024,true), Default(None) */
    val image: Rep[Option[String]] = column[Option[String]]("image", O.Length(1024,varying=true), O.Default(None))
    /** Database column type_id SqlType(INT) */
    val typeId: Rep[Int] = column[Int]("type_id")

    /** Foreign key referencing Type (database name fk_food_type) */
    lazy val typeFk = foreignKey("fk_food_type", typeId, Type)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)

    /** Uniqueness Index over (name) (database name name_UNIQUE) */
    val index1 = index("name_UNIQUE", name, unique=true)
  }
  /** Collection-like TableQuery object for table Food */
  lazy val Food = new TableQuery(tag => new Food(tag))

  /** Entity class storing rows of table Type
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(512,true) */
  case class TypeRow(id: Int, name: String)
  /** JSON automated mapping for TypeRow */
  implicit val typeRowFormat: OFormat[TypeRow] = Json.format[TypeRow]
  /** GetResult implicit for fetching TypeRow objects using plain SQL queries */
  implicit def GetResultTypeRow(implicit e0: GR[Int], e1: GR[String]): GR[TypeRow] = GR{
    prs => import prs._
    TypeRow.tupled((<<[Int], <<[String]))
  }
  /** Table description of table type. Objects of this class serve as prototypes for rows in queries. */
  class Type(_tableTag: Tag) extends profile.api.Table[TypeRow](_tableTag, Some("foodie"), "type") {
    def * = (id, name) <> (TypeRow.tupled, TypeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name)).shaped.<>({r=>import r._; _1.map(_=> TypeRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(512,true) */
    val name: Rep[String] = column[String]("name", O.Length(512,varying=true))

    /** Uniqueness Index over (name) (database name name_UNIQUE) */
    val index1 = index("name_UNIQUE", name, unique=true)
  }
  /** Collection-like TableQuery object for table Type */
  lazy val Type = new TableQuery(tag => new Type(tag))
}
