import java.io.{File, PrintWriter}

import scala.concurrent.Await
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._

/**
 * Created by pnagarjuna on 02/11/15.
 */
object Main {
  def main (args: Array[String]): Unit = {
    println("Starting bikes")
    if (args.length != 1) {
      println("Please provide cmd line argument")
    }

    val filename = args(0)

    write(filename) { writer =>
      import java.util.Scanner
      val scan = new Scanner(System.in)
      while(scan.hasNext) {
        val line = scan.nextLine();
        val dust = line.split(",").map(_.trim)
        fetch(dust(3)) { atom =>
          writer.println(s"${dust(0)}    ${dust(1)}    ${dust(2)}    ${atom.model}    ${atom.year}    ${atom.kms}    ${atom.cost}")
        }
      }
    }
  }

  def fetch(link: String)(process: Atom => Unit): Unit = {
    val f = Utils.getPage(link)
    Await.result(f, 1 minute)
    f onComplete {
      case Success(res) =>
        val html = res.body
        Utils.parse(html) match {
          case Some(list) =>
            list.foreach(process(_))
          case None =>
            println("no items")
        }
      case Failure(th) =>
        th.printStackTrace()
    }
  }


  def write(filename: String)(f: PrintWriter => Unit): Unit = {
    val writer = new PrintWriter(new File(val writer = new PrintWriter(new File(s"${System.getProperty("user.home")}/$filename.csv"))))
    f(writer)
    writer.flush()
    writer.close()
  }
}
