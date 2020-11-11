# Graph Analysis using Hadoop on Azure

The objective of this project is to compute the distribution of a graph's node degree differences. Given the large scale of the graph with around 70 million edges, the computations are performed by a Map-Reduce program in Java using Hadoop on Azure, one of the major cloud computer services.

## Table of Contents

* [Data Source](#data-source)
* [Instructions](#instructions)
* [Keywords](#keywords)
* [License](#license) 

## Data Source

The data set contains a network graph. The network is organized as a directed graph, where an edge is from the source node to the target node. The data are divided into two files in a tab-separated-values (tsv) format, small and large ones, so that the code can be tested against the small one and then used on the large one. Each line in the files represents a single edge consisting of two pieces of information: the source node ID and the target node ID. The lines are already sorted by the source node ID in ascending order. The node IDs are all positive integers.

Due to the size of large.tsv (around 1 GB when unzipped), it is not pushed into this repository. Please download the zipped file from [here](https://drive.google.com/file/d/12zfjscupFXbeLY2CDT8EZ2Q1y_-hknru/view?usp=sharing).

## Instructions

This project makes use of HDInsight on Azure, which is an Apache Hadoop distribution. Please first create and provision a HDInsight Cluster and Storage, by referring to the [documentation](https://docs.microsoft.com/en-us/azure/hdinsight/hdinsight-hadoop-create-linux-clusters-portal).

In order to upload the necessary files to the HDFS-compatible Azure Blob storage, please follow the following steps:

1. Install CLI, as per the instructions [here](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli).
2. Open the terminal (or the command prompt on Windows), and use ```az login``` command to authenticate the Azure subscription.
3. Check out the storage accounts by typing ```az storage account list```.
4. Obtain the "key1" from the output by the following command:
```
az storage account keys list --account-name <storage-account-name> --resource-group <resource-group-name>
```
5. Check out the blob containers by running:
```
az storage container list --account-name <storage-account-name> --account-key <key1-value>
```
6. Upload the data files to the blob container by entering the command as follows:
```
z storage blob upload --account-name <storage-account-name> --account-key <key1-value> --file <data-file-name> --container-name <container-name> --name <new-blob-name>/<data-file-name> 
```
7. Upload the jar file to the blob container by the following command:
```
scp <relative-path>/graph.jar <ssh-username>@<cluster-name>-ssh.azurehdinsight.net
```
8. SSH into the HDInsight cluster by typing:
```
ssh <ssh-username>@<cluster-name>-ssh.azurehdinsight.net
```
9. Run the code on the data files by entering the command as follows:
```
yarn jar graph.jar alfred.graph wasbs://<container-name>@<storage-account-name>.blob.core.windows.net/<new-blob-name>/<data-file-name> wasbs://<container-name>@<storage-account-name>.blob.core.windows.net/output
```
10. If there are multiple output files, merge the files by the following command:
```
hdfs dfs -cat wasbs://<container-name>@<storage-account-name>.blob.core.windows.net/smalloutput/* > <output-file-name>
```
11. Exit to the local machine simply by entering ```exit```.
12. Download the merged file to the local machine by the following scp command:
```
scp <ssh-username>@<cluster-name>-ssh.azurehdinsight.net:/home/<ssh-username>/<output-file-name> <local directory>
```

To compile, simply run the command in the corresponding directory as follows:

```
mvn clean package
```

It will generate a single JAR file in the target directory.

The script accept two arguments. The first argument (args[0]) is the path for the input graph file, whereas the second argument (args[1]) is the path for the output directory. The default outputing mechanism of Hadoop will create multiple files on the specified output directory with names like part-00000, part-00001, etc. These files will be merged and downloaded to a local directory.

The output is a tsv file, in which each line contains (1) unique degree differences obtained from subtracting nodes' in-degree from their out-degree; (2) the number of nodes with the corresponding degree differences. The in-degree of a given node is defined as the number of edges in which the node is the target, whereas the out-degree is defined as the number of edges in which the node is the source. Thus, the degree differences can be a negative interger, for a given node can have lower out-degree but higher in-degree.

## Keywords

Apache Hadoop; Edge; Graph Analytics; In-degree; Microsoft Azure; Node (or Vertex); Out-degree.

## License

This repository is covered under the [MIT License](https://github.com/alfred-kctang/random-forest-pulsar-stars/blob/master/LICENSE).
