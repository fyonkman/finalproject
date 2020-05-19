# finalproject
To compile:
javac Peer.java

To use:
Run 'java Peer' on each peer you want in the network.
Use the 'connect hostName' command to make a client connection from the peer exucuting the command to the peer who's host name is specified.
Use the 'split fileName numPieces peerNameWithFile' command to execute the file transfer.

For example, if you were working with three peers, A, B, and C, and A wants the file that C has, you would first call on A- 'connect C'. You would then call on C- 'connect B' as B will act as the transfer node. Finally, you would call on B 'connect A' as B will ultimately pass it's piece onto the destination. If you have multiple intermediary nodes, like node D, you would just call 'connect D' on C, and call 'connect A' on node D.

Once the nodes are connected, on node A, you would call 'split file.txt 2 C', if you were using B and D as the 2 intermediary nodes.
