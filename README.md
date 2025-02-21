# Orange Plant Processor

The purpose of this lab is to experiment with multithreaded processing and how it can simulate an orange juice bottling
factory.

## Processing Descriptions

![screenshot](img/UML.png)

In order to successfully complete this project, we created the classes and functions as shown in the above UML diagram.
These classes meet the project requirements as they allow for multiple plants to be created with their own thread,
multiple workers to operate at each plant, and a main controller thread that controls the workings of the system.

I encountered numerous challenges while completing this lab. The biggest challenge was determining how I was going to
create multiple threads and control the locks for each individual thread. I didn't want to have one lock on one thread
end up locking numerous threads (this would not be a correct mutex implementation). I ended up finding a
[Medium](https://medium.com/@peterlee2068/concurrency-and-parallelism-in-java-f625bc9b0ca4)
article with very helpful documentation to guide one through the proper locking process.

Another major challenge was that I spent a lot of time debugging my program as I was getting no oranges processed
results. This made no sense as all of my functions looked to clearly start the plant and received oranges. However, upon
closer observation, I found that I had forgotten to _start_ the plants, meaning that they were not getting the
oranges, so nothing could be processed. Thankfully, it was an easy fix, just took some fresh eyes and a migraine to
solve.

## To Run the Application

This application can be run using Ant. To run, please follow these direction.

1. Open the terminal application of your choice (terminal on MacOS, command line on Windows, etc.)
2. Enter the following commands:

   a. Navigate to the root folder where you'd like these files to go:

   Example: `cd Desktop`

   b. Clone the git repository to your computer:

   `git clone https://github.com/abbyymaureen/OrangePlant.git`

   c. To compile and create a JAR file:

   `ant jar`

   d. To run the jar file:

   `ant run`

   e. To clean up the terminal and remove build artifacts:

   `ant clean`

## References

- [Concurrency and Parallelism in Java](https://medium.com/@peterlee2068/concurrency-and-parallelism-in-java-f625bc9b0ca4) -
  A helpful article on Java concurrency, multithreading, and parallelization.
