import java.io.{File, PrintWriter}

import scala.concurrent.Await
import scala.util.{Try, Failure, Success}
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
      println("""usage: sbt "run <filename>" """)
    }

    val filename = args(0)

    write(filename) { (writer, errors) =>
      import java.util.Scanner
      val scan = new Scanner(System.in)
      while(scan.hasNext) {
        val line = scan.nextLine()
        val dust = line.split(",").map(_.trim)
        fetch(dust(3)) { tryAtoms =>
          tryAtoms match {
            case Success(atoms) =>
              atoms.foreach { atom =>
                println(s"writing to file: $atom")
                writer.println(s"${dust(0)}    ${dust(1)}    ${dust(2)}    ${atom.model}    ${atom.year}    ${atom.kms}    ${atom.cost.split("\\s+")(1).split(",").reduce(_ + _)}")
                writer.flush()
              }
            case Failure(fAtoms) =>
              fAtoms match {
                case Not200Exception(status) =>
                  errors.println(s"${dust(3)}    $status    ${Not200Exception.getClass}")
                  errors.flush()
                case ex: Throwable =>
                  errors.println(s"${dust(3)}    200    ${ex.getClass}")
              }
          }
        }
      }
    }
  }

  case class Not200Exception(status: Int) extends Exception

  def fetch(link: String)(result: Try[List[Atom]] => Unit): Unit = {
    val f = Utils.getPage(link)
    Await.result(f, 3 minute)
    f onComplete {
      case Success(res) =>
        val html = res.body
        val statusCode = res.status
        if (statusCode == 200) {
            Utils.parse(html) match {
              case Success(list) =>
                result(Success(list))
              case Failure(th) =>
                result(Failure(th))
            }
        } else {
          result(Failure(Not200Exception(statusCode)))
        }
      case Failure(th) =>
        println("fetching page failed")
        th.printStackTrace()
        result(Failure(th))
    }
  }


  def write(filename: String)(f: (PrintWriter, PrintWriter) => Unit): Unit = {
    val writer = new PrintWriter(new File(s"${System.getProperty("user.home")}/$filename.csv"))
    val errors = new PrintWriter(new File(s"${System.getProperty("user.home")}/${filename}-errors.csv"))
    f(writer, errors)
    writer.flush()
    errors.flush()
    writer.close()
    errors.close()
  }
}
