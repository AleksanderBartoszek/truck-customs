import scala.collection.immutable.Queue
import java.util.UUID

object Customs:

  // Statistical analysis
  private var accumulatedWaitingTime = 0
  private var checkedTrucks          = 0
  def meanWaitingTime(): Double      = accumulatedWaitingTime.toDouble / checkedTrucks

  // Queues used with two checking stations
  private var currentTruckA: Option[Truck] = None
  private var currentTruckB: Option[Truck] = None
  private var queueA                       = Queue[Truck]()
  private var queueB                       = Queue[Truck]()
  private var documentQueue                = Queue[Truck]()
  def queuesAreEmpty(): Boolean =
    queueA.isEmpty && queueB.isEmpty && documentQueue.isEmpty && currentTruckA.isEmpty && currentTruckB.isEmpty

  /** Function to add truck to processing pipeline
    *
    * @param weightUnits
    *   weight of a truck in units
    * @return
    *   assigned ID
    */
  def arrive(weightUnits: Int): UUID =
    val truck = Truck(weightUnits, UUID.randomUUID())
    documentQueue = documentQueue.enqueue(truck)
    truck.id

  /** Function creating snapshot of current queue status
    *
    * @return
    *   CustomsStatus object with stored queues
    */
  def status(): CustomsStatus =
    CustomsStatus(currentTruckA, currentTruckB, queueA, queueB, documentQueue)

  /** Function invoking simulation parts
    */
  def step(): Unit =
    increaseTimeWaited()
    currentTruckStep()
    gateQueueProgress()
    swapQueues()

  /** Function updating time waited for each truck in queues and ones currently checked
    */
  private def increaseTimeWaited(): Unit =
    currentTruckA.map(_.timeUnitsWaited += 1)
    currentTruckB.map(_.timeUnitsWaited += 1)
    queueA.map(_.timeUnitsWaited += 1)
    queueB.map(_.timeUnitsWaited += 1)

  /** Function updating state of currently processed trucks, moves queues if possible
    */
  private def currentTruckStep(): Unit =
    currentTruckA match
      case None =>
        queueA.dequeueOption match
          case None =>
          case Some(value) =>
            queueA        = value._2
            currentTruckA = Some(value._1)
            value._1.weightUnits -= 1
            if value._1.weightUnits <= 0 then
              checkedTrucks += 1
              accumulatedWaitingTime += value._1.timeUnitsWaited
              currentTruckA = None
      case Some(truck) =>
        truck.weightUnits -= 1
        if truck.weightUnits <= 0 then
          checkedTrucks += 1
          accumulatedWaitingTime += truck.timeUnitsWaited
          currentTruckA = None

    currentTruckB match
      case None =>
        queueB.dequeueOption match
          case None =>
          case Some(value) =>
            queueB        = value._2
            currentTruckB = Some(value._1)
            value._1.weightUnits -= 1
            if value._1.weightUnits <= 0 then
              checkedTrucks += 1
              accumulatedWaitingTime += value._1.timeUnitsWaited
              currentTruckB = None
      case Some(truck) =>
        truck.weightUnits -= 1
        if truck.weightUnits <= 0 then
          checkedTrucks += 1
          accumulatedWaitingTime += truck.timeUnitsWaited
          currentTruckB = None

  /** Function adding next vehicles to the queues if there is space left
    */
  private def gateQueueProgress(): Unit =
    while (queueA.length < 5 || queueB.length < 5) && !documentQueue.isEmpty do
      val deq = documentQueue.dequeueOption match
        case None =>
        case Some(value) =>
          assignToQueue(value._1)
          documentQueue = value._2

  /** Function assigning truck to appropriate (shorter if possible) queue
    *
    * @param truck
    */
  private def assignToQueue(truck: Truck): Unit =
    if queueA.length == 5 && queueB.length < 5 then queueB = queueB.enqueue(truck)
    else if queueA.length < 5 && queueB.length == 5 then queueA = queueA.enqueue(truck)
    else
      queueTimes() match {
        case (a, b) if b < a =>
          queueB = queueB.enqueue(truck)
        case _ =>
          queueA = queueA.enqueue(truck)
      }

  /** Function swapping trucks in queues to improve mean waiting time by stacking heavier trucks into single queue when
    * queues are full or by balancing queues when queues are not full
    */
  private def swapQueues(): Unit =
    if queueA.length == 5 && queueB.length == 5 then swapQueuesWhenFull()
    else balanceQueues()

  /** Function swapping heavier trucks into single queue to maximize amount of trucks processed
    */
  private def swapQueuesWhenFull(): Unit =
    var newA = Queue[Truck]()
    var newB = Queue[Truck]()
    queueA.dequeueOption match
      case None => Queue[Truck]()
      case Some(value) =>
        newA   = newA.enqueue(value._1)
        queueA = value._2
    queueB.dequeueOption match
      case None => Queue[Truck]()
      case Some(value) =>
        newB   = newB.enqueue(value._1)
        queueB = value._2
    while !queueA.isEmpty || !queueB.isEmpty do
      if !queueA.isEmpty && !queueB.isEmpty then
        val deqA = queueA.dequeue
        val deqB = queueB.dequeue
        if deqA._1.weightUnits > deqB._1.weightUnits then
          newA = newA.enqueue(deqB._1)
          newB = newB.enqueue(deqA._1)
        else
          newA = newA.enqueue(deqA._1)
          newB = newB.enqueue(deqB._1)
        queueA = deqA._2
        queueB = deqB._2
      else if queueA.isEmpty then
        val deqB = queueB.dequeue
        newB   = newB.enqueue(deqB._1)
        queueB = deqB._2
      else if queueB.isEmpty then
        val deqA = queueA.dequeue
        newA   = newA.enqueue(deqA._1)
        queueA = deqA._2
    queueA = newA
    queueB = newB

  /** Function balancing remaining time of the queues when there is not enough trucks to fill both queues. It ensures
    * both stations are working for as long as possible, doubling the efficiency
    */
  private def balanceQueues(): Unit =
    var newA = Queue[Truck]()
    var newB = Queue[Truck]()
    queueA.dequeueOption match
      case None => Queue[Truck]()
      case Some(value) =>
        newA   = newA.enqueue(value._1)
        queueA = value._2
    queueB.dequeueOption match
      case None => Queue[Truck]()
      case Some(value) =>
        newB   = newB.enqueue(value._1)
        queueB = value._2
    while !queueA.isEmpty || !queueB.isEmpty do
      if !queueA.isEmpty && !queueB.isEmpty then
        val queueBalance = queueTimes()._1 - queueTimes()._2
        val deqA         = queueA.dequeue
        val deqB         = queueB.dequeue
        val weightDiff   = deqA._1.weightUnits - deqB._1.weightUnits
        val lengthDiff   = queueA.length - queueB.length
        if lengthDiff > 0 && weightDiff > 0 then
          newA = newA.enqueue(deqB._1)
          newB = newB.enqueue(deqA._1)
        else if lengthDiff < 0 && weightDiff < 0 then
          newA = newA.enqueue(deqB._1)
          newB = newB.enqueue(deqA._1)
        else if lengthDiff == 0 && (Math.abs(weightDiff - queueBalance) < Math.abs(queueBalance)) then
          newA = newA.enqueue(deqB._1)
          newB = newB.enqueue(deqA._1)
        else
          newA = newA.enqueue(deqA._1)
          newB = newB.enqueue(deqB._1)
        queueA = deqA._2
        queueB = deqB._2
      else if queueA.isEmpty then
        val deqB = queueB.dequeue
        newB   = newB.enqueue(deqB._1)
        queueB = deqB._2
      else if queueB.isEmpty then
        val deqA = queueA.dequeue
        newA   = newA.enqueue(deqA._1)
        queueA = deqA._2
    queueA = newA
    queueB = newB

  /** Function to check how much time vehicle will be in queue
    *
    * @param truckID
    *   truck to check
    * @return
    *   estimated time to START processing this vehicle or estimated time of shorter queue when vehicle is not yet in
    *   line
    */
  def estimatedWaitingTime(truckID: UUID): Int =
    truckID match {
      case a if queueA.map(e => e.id).contains(truckID) =>
        val queueInFront = queueA.takeWhile(e => e.id != truckID).map(e => e.weightUnits).foldLeft(0)(_ + _)
        currentTruckA match
          case None        => queueInFront
          case Some(value) => queueInFront + value.weightUnits

      case b if queueB.map(e => e.id).contains(truckID) =>
        val queueInFront = queueB.takeWhile(e => e.id != truckID).map(e => e.weightUnits).foldLeft(0)(_ + _)
        currentTruckA match
          case None        => queueInFront
          case Some(value) => queueInFront + value.weightUnits

      case _ => Math.min(queueTimes()._1, queueTimes()._2)
    }

  /** Function to check how long each queue is
    *
    * @return
    *   tuple of combined weight/time needed to process queues (queueA, queueB)
    */
  def queueTimes(): (Int, Int) =
    val timeA = queueA.map(e => e.weightUnits)
    val timeB = queueB.map(e => e.weightUnits)
    (timeA.foldLeft(0)(_ + _), timeB.foldLeft(0)(_ + _))
