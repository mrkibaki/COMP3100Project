Macquerie University
Student/Developer: Chenxu Lin
SID: 44469578

This project for building a vanilla version of ds-client,
which dispatches the jobs that are supposed to scheduled to
the server in the ds-server simulation one by one.

MyClient.java basically does three part of the job: 
1. Acknowledgement (hand-shake) between the server and the client;

2. Reads the system list that given from the server side

3. Shedule jobs to largrest core of servers and wait for servers 
to complete them.

Once all jobs are scheduled to the server, it would automatically
close connection between server and client.



 

