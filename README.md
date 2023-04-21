# truck-customs task - Aleksander Bartoszek

## How to run

project is built using sbt, with Scala 3.2

To run this application enter sbt shell and type `run` to invoke main function from `Main.scala` file or type `test` to invoke one predefined random simulation test

In `Main.scala` file you can find 2 defined methods to play with my simulation - one expects sequence of trucks to process, second one creates random sequence with specified parameters. Feel free to create custom simulation runners using public functions from `Customs` object.

## What's in the output

To illustrate the algorithm after each step current status of queues is printed to stdout

```
******************************
|  1  |  2    2    2    2    2  
------------------------------
|  2  |  2    3    5    4    3  
******************************

******************************
|  1  |  3    4  
------------------------------
|  0  |  2    3    5  
******************************

******************************
|  0  |
------------------------------
|  0  |
******************************
Mean waiting time: 12.0
```

Stars separate steps, dashes separate two queues.
Inside the queue, from the left: there is one number indicating how much time/weight there is left to check on the current truck and up to 5 trucks in queue. 

Anytime you can call function `Customs.meanWaitingTime()` to get a statistic of trucks checked so far. 

## Algorithm explanation

There are 3 queues and 2 single spaces for trucks.

Firstly trucks are enqueued into `documentQueue` and immediately moved to one of the two main queues `queueA` or `queueB` if there is space. Trucks are assigned in order to shorter queue or to fill last empty space.

then in each step of the simulation 4 things happen:
- `increaseTimeWaited()` - adds one unit of time to each truck in `queueA`, `queueB` and 2 currently occupied spaces.
- `currentTruckStep()` - removes one unit of remaining time/weight in currently processed trucks and moves `queueA` and `queueB` forward if possible
- `gateQueueProgress()` - moves trucks from `documentQueue` to fill main queues if possible
- `swapQueues()` - swaps neighboring trucks following rules:
  - if both queues are full - swaps each heavier truck to one queue to maximize number of trucks processed thus reducing mean time spent for trucks.
  - if queues are not full and one is longer and heavier - swaps heavier trucks to shorter queue
  - finally balances non-full queues of equal length to ensure both checking-stations are working as long as possible

I have no proof this solution is optimal, i'm almost sure there will be some case where i could optimize it but generally it looks promising as far as i've tested.

## What could be improved

Some algorithms aren't as concise as they could be, I went too far trying to use `Queue` instead of some other collection. Initially I thought it will be convenient.
I should have foreseen that swapping trucks between immutable queues is awkward.

## Have a nice day!



