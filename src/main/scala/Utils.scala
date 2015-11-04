import org.jsoup.Jsoup
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

import scala.collection.JavaConversions._
import scala.util.Try

/**
 * Created by pnagarjuna on 02/11/15.
 */

case class Atom(model: String, year: String, kms: String, cost: String)

object Utils {

  def getPage(link: String): Future[WSResponse] = WS.client.url(link).withFollowRedirects(true).get()

  def parse(html: String): Try[List[Atom]] = {
    Try {
      val parsedHtml = Jsoup.parse(html)
      val elements = parsedHtml.getElementsByTag("table").get(0).getElementsByTag("tbody").toList

      val atoms = elements.flatMap { elem =>
        val trs = elem.getElementsByTag("tr")
        trs.toList.tail.map { tr =>
          println(s"tr $tr")
          val tds = tr.getElementsByTag("td")
          Atom(tds.get(0).text(), tds.get(1).text(), tds.get(2).text(), tds.get(3).text())
        }
      }
      atoms
    }
  }
}
