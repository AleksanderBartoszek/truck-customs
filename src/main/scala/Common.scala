import java.util.UUID
import scala.collection.immutable.Queue

case class Truck(var weightUnits: Int, id: UUID = UUID.randomUUID(), var timeUnitsWaited: Int = 0)

case class CustomsStatus(
  currentTruckA: Option[Truck],
  currentTruckB: Option[Truck],
  queueA: Queue[Truck],
  queueB: Queue[Truck],
  gateQueue: Queue[Truck]
):
  def printUnits() =
    printSeparator()
    printf("|  " + currentTruckA.getOrElse(Truck(0)).weightUnits + "  |")
    queueA.map(e => printf("  " + e.weightUnits + "  "))
    printSeparator("-")
    printf("|  " + currentTruckB.getOrElse(Truck(0)).weightUnits + "  |")
    queueB.map(e => printf("  " + e.weightUnits + "  "))
    printSeparator()

  def printSeparator(char: String = "*", length: Int = 30): Unit =
    println()
    println(char.repeat(length))
