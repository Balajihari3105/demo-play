package controllers

import javax.inject._
import play.api.libs.json.{Format, JsValue, Json, OFormat}
import play.api.mvc._

import java.security.MessageDigest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

    def get()=Action.async{
      Future.successful(Ok("success"))
    }
    def create()=Action.async(parse.raw){
      request =>
      println(request.body.asBytes().map(_.utf8String).getOrElse("NULL q"))
      Future.successful(Ok)
    }
  def index() = Action { implicit request: Request[AnyContent] =>

    Ok("hellow world")
  }


  case class Event(
                    id: Long,
                    entry: Seq[Entry]
                  )

  object Event {
    implicit val eventFormat: OFormat[Event] = Json.format[Event]


  }

  case class Entry(
                    entryId: Long,
                    changes: EntryChange
                  )

  object Entry {
    implicit val entryFormat: OFormat[Entry] = Json.format[Entry]
  }

  case class EntryChange(
                          form_id: Long
                        )

  object EntryChange {
    implicit val entryChangeFormat: OFormat[EntryChange] = Json.format[EntryChange]

  }

  // Object to hold the implicits
  object JsonImplicits {
    // Implicit Format for EntryChange

    // Implicit Format for Entry

    // Implicit Format for Event

  }

  import JsonImplicits._

  def validRequ: Action[JsValue] = Action.async(parse.json){ request =>


    val b =  request.body.toString()
//    val requestBody = request.body.asJson.head.asOpt[]
    val res = validateSigna(request.body.toString, generateToken)

    //    println(s"reqyest body ${request.body}")
    //    println(s"the header ${request.headers.get("X-Hub-Signature-256")}")
    Future {
      Ok(b)
    }
  }
  def validateSigna(body: String, gen: String => String) = {
    gen(body).prependedAll("SHA-256")

  }

  def generateToken(str: String): String = {
    MessageDigest.getInstance("SHA-256")
      .digest(str.getBytes("UTF-8")).mkString
  }
}
