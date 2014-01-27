package controllers

import buildinfo.BuildInfo
import play.api.mvc.{Action, Controller}
import java.util.Date

object Management extends Controller {

  def manifest = Action {

    val data = Map(
      "Build" -> BuildInfo.buildNumber,
      "Date" -> new Date(BuildInfo.buildTime).toString
    )

    Ok(data map {
      case (k, v) => s"$k: $v"
    } mkString "\n")
  }
}