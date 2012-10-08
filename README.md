# Dendron

This is a project that Jach developed internally at [DynamoBI](http://www.dynamobi.com/c/)
([Public Repo of other projects](https://github.com/luciddb)) and is now licensed with permission
to the public under the Apache License v2.0.

## Purpose

Dendron is a program to store massive datasets in HBase or DynamoDB and provides functions
for accessing certain data analytics on the data, with complexity guarantees of
O(log(N)^D) for both querying and updating. N is the number of data points along a specific
dimension, D is the dimensionality of the dataset as a whole. So if your dataset is just numbers
stored at X,Y coordinates, with 16 values along each dimension, you can read out aggregations
of the data (such as the SUM of all 256 elements) in at most 16 reads.

The type of analytics currently implemented are COUNT and SUM of integers, but in general any
aggregation operation that follows certain restrictions can be implemented easily on top.
The restrictions are:

Given datum A and datum B, an operator ⨁ , and the inverse operator ⨀ ,
the following equations must hold:
* A ⨁  B = B ⨁  A
* If A ⨁  B = C, then C ⨀  A = B and C ⨀  B = A.

Obviously SUM (+), and the inverse SUBTRACT (-), fit these restrictions for numbers A and B.

While this restriction puts some limits on the practicality of this project, many applications
are just fine with knowing sums, counts, and averages. For the type of problems OLAP systems
are good for, this should also be a good (if not better) fit.

## Current Status

This project will not work for any purpose "out of the box".

This project is in a **defunct** state. It has not even been functionally tested for a few
months, and its dependent libraries likely have updates that need to be applied. (For example,
the version of HBase that the clojure-hbase depended upon during initial development sometimes
has to be installed manually using maven.) The most recent development on it left it
in an in-progress state, which as one would expect is pretty messy.

If you are interested in sponsoring further development on this project, you are welcome to contact
Jach and discuss payment. (Bitcoins welcome!) If you for some reason want to contribute yourself,
make a pull request with your patch.

Jach wants to do the following to this project, in order:

1. Writing blog posts and/or wiki pages that demonstrate the data structure and its algorithms for anyone to understand.
    * Data Cube visualizer for dimensions < 4
    * Simple web page to run queries and see the results
2. Fixing the existing bugs that prevent this program from giving correct output.
    * It seems to *usually* work but sometimes wrong data is produced for certain cases...
3. Making performance improvements.
    * Correctly implement the update algorithm to follow the log(N)^D bound, currently the worst-case is N^D
    * Acquire a deep understanding of Clojure's concurrency model
    * Good performance on large sets of data with more than 4 dimensions
    * Cluster analysis to reduce the size of N for each sub-cube
        * A cursory look at a recently published paper, *Fast Algorithms for the Maximum Clique Problem on Massive Sparse Graphs*, looks promising as a place to start
4. Re-porting to DynamoDB, HBase, and others.

There is no time-table for these tasks and they may in fact never be completed.

## Research

The data structure that this project is derived from is known in the literature as
a Space-Efficient Dynamic Data Cube (SDDC). If you're interested in a collection of PDFs
on it as well as related structures, feel free to send Jach an email. Google is helpful
in finding most of them. As stated above, Jach intends to write introductory material to
understand what's going on, but if that hasn't happened yet and you want to learn, the PDFs
are your friends.

Jach suspects that SDDCs are not used widely in the industry for these reasons:

1. No one has solved the curse of dimensionality.
2. Real-world data is sparse, the SDDC expects dense data, optimizations for sparse data aren't included by default.
3. Programmers are unfamiliar with OLAP systems in general, let alone this particular data structure.
    * There are also in all likelihood much better data structures and algorithms that aren't published anywhere, but only the companies that use them know about them. (*Cough*IBM's g2 sensemaking system *Cough*)

Jach is unaware of any implementations of this in use.
Jach is unaware of any patents with claims in this space, but he wouldn't be surprised if someone
has successfully patented the concept of SUM.
