import scala.util.Random
@main
def main() =
  val trucks = Seq(3, 4, 6, 2, 1, 1, 2, 4, 6, 2, 4, 6, 2, 3)
  runSimulation(trucks)
//runRandomSimulation(100, (1, 5))

def runSimulation(trucks: Seq[Int]): Unit =
  trucks.foreach(Customs.arrive(_))
  while !Customs.queuesAreEmpty() do
    Customs.step()
    Customs.status().printUnits()
  println("Mean waiting time: " + Customs.meanWaitingTime())

def runRandomSimulation(truckCount: Int, weightRange: (Int, Int)): Unit =
  val trucks = for i <- 1 to truckCount yield Random.nextInt(weightRange._2 - weightRange._1) + weightRange._1
  trucks.foreach(Customs.arrive(_))
  while !Customs.queuesAreEmpty() do
    Customs.step()
    Customs.status().printUnits()
  println("Mean waiting time: " + Customs.meanWaitingTime())
