package controllers

import play.api.mvc._
import javax.inject.Inject
import scala.concurrent.Future
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import play.api.libs.json.Json

class LeadController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {


  def welcomePage(): Action[AnyContent] = Action.async {
    Future.successful(
      Ok(<html>hello welcome to play deployment in kubernetes</html>))
  }

  // Your secret key from Meta (replace with your actual key)
  private val SECRET_KEY = "abcd1234"
  private val SIGNATURE_HEADER = "X-Hub-Signature-256" // Check Meta's docs for the exact header name
  private val HMAC_SHA256 = "HmacSHA256"

  // Helper to calculate HMAC-SHA256
  private def calculateHmac(payload: String, secret: String): String = {
    val secretKey = new SecretKeySpec(secret.getBytes("UTF-8"), HMAC_SHA256)
    val mac = Mac.getInstance(HMAC_SHA256)
    mac.init(secretKey)
    val hashBytes = mac.doFinal(payload.getBytes("UTF-8"))
    val cgh = hashBytes.map("%02x".format(_)).mkString // Convert to hex string
    println(s"The generated is $cgh")
    cgh
  }

  // Action to handle the lead webhook
  def validateLead() = Action.async(parse.raw) { request =>
    // Get the raw body as a string
    val rawBody = request.body.asBytes().map(_.utf8String).getOrElse("")

    // Get the signature from the header
    val receivedSignatureOpt = request.headers.get(SIGNATURE_HEADER)
    println(s"The recieved is ${receivedSignatureOpt.get}")

    receivedSignatureOpt match {
      case Some(signature) if signature.startsWith("sha256=") =>
        val receivedHash = signature.stripPrefix("sha256=")
        val calculatedHash = calculateHmac(rawBody, SECRET_KEY)

        // Compare the signatures
        if (calculatedHash == receivedHash) {
          // Signature matches! Process the lead
          val jsonBody = Json.parse(rawBody)
          println(s"Valid lead received: $jsonBody")
          Future.successful(Ok("Lead validated"))
        } else {
          // Signature doesn’t match
          Future.successful(Unauthorized("Invalid signature"))
        }

      case _ =>
        // No signature header or wrong format
        Future.successful(BadRequest("Missing or invalid signature header"))
    }
  }
}