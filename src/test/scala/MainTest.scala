import scala.util.Random
class MainTest extends munit.FunSuite:

  test("simulation showcase") {
    val trucks: Seq[Int] = for (i <- 1 to 40) yield Random.nextInt(8)
    trucks.foreach(Customs.arrive(_))
    while !Customs.queuesAreEmpty() do
      Customs.step()
      Customs.status().printUnits()
    println("Mean waiting time: " + Customs.meanWaitingTime())
  }
