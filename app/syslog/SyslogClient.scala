package syslog

import org.productivity.java.syslog4j.server.{SyslogServerEventIF, SyslogServerIF, SyslogServerEventHandlerIF, SyslogServer}
import play.Logger
import org.joda.time.format.DateTimeFormat

object SyslogClient {

  def start = {
    Logger.info("syslog client: starting")
    val config = SyslogServer.getThreadedInstance("tcp").getConfig
    config.setPort(8888)
    config.addEventHandler(EventHandler)
    Logger.info("syslog client: started")
  }
}

object EventHandler extends SyslogServerEventHandlerIF {

  override def event(syslogServer: SyslogServerIF, event: SyslogServerEventIF) {
    // use an actor here, don't do anything that blocks
    // Logger info(event.getMessage)
    // RobotParser.parse(event.getMessage)
  }
}

/**
 * Example robot parsing 
 */
object RobotParser {

  // threadinfo "now" "client.ip" "geoip.country_code" "geoip.country_name" "req.http.Fastly-SSL" "req.http.host" "req.url" "req.request" "req.http.user-agent" "resp.status"
  private val lineRegex = """^(.*:)?\s+"(.*?)"\s+"(.*?)"\s+"(.*?)"\s+"(.*?)"\s+"(.*?)"\s+"(.*?)"\s+"(.*?)"\s+"(.*?)"\s+"(.*?)"\s+"(.*?)".*$""".r
  
  private val robotList = Seq("googlebot", "mediapartners-google", "adsbot-google", "bingbot", "msn", "yandex", "facebookexternalhit")

  def parse(logline: String) = {
    if (isRobot(logline)) matchBot(logline: String)
  }

  private def isRobot(logline: String): Boolean = {
    val loglineLowerCased = logline.toLowerCase
    robotList.map(robot => loglineLowerCased.contains(robot)).contains(true)
  }

  private def matchBot(syslogLine: String) = {

    syslogLine match {
      case lineRegex(fastlyNode, date, ip, countryCode, countryName, "(null)", host, path, method, userAgent, status) => {
        // do something
      }
      case _ => // Logger.error("Error parsing: " + syslogLine)
    }
  }

  private def toIsoDateString(date: String): String = {
    val fmt = DateTimeFormat.forPattern("E, dd MMM yyyy HH:mm:ss ZZZ")
    fmt.parseDateTime(date).toString
  }

}