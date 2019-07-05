#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <netdb.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <arpa/inet.h>
#include <ifaddrs.h>
#include <math.h>
#include <sys/shm.h>
#include <dirent.h>
#include <stdbool.h>
#include <pthread.h>

#define MemSize 32
#define MAXNUMBERAPPLICATIONS 32
#define FILEPATH "/home/simon/test/vdaq/"

#define SIGINT 2

bool applicationIsValid(int32_t internalCounter, int32_t oldInternalCounter);
void appendStatusToBuffer(char* buffer, char* applicationName, char* status);
void statusTask();
void sendTask();
void sendStatusMessages();
char* getCPUMessage();
char* fillCPUMessage(const char* cpuValue);
char* fillIPMessage();
char* fillStatusMessage();
void flushSocket(int fd);

bool running = true;

static void sigHandler(int sigNr){
	running = false;
	sleep(2);
	printf("exiting");
	exit(0);
}

// Returns hostname for the local computer
void checkHostName(int hostname)
{
    if (hostname == -1)
    {
        perror("gethostname");
        exit(1);
    }
}

// Returns host information corresponding to host name
void checkHostEntry(struct hostent * hostentry)
{
    if (hostentry == NULL)
    {
        perror("gethostbyname");
        exit(1);
    }
}

// Converts space-delimited IPv4 addresses
// to dotted-decimal format
void checkIPbuffer(char *IPbuffer)
{
    if (NULL == IPbuffer)
    {
        perror("inet_ntoa");
        exit(1);
    }
}

int sockfd = 0, n = 0;
volatile char applicationStates[1024];
char recvBuff[1024];
char sendBuff[1024];
char *IPbuffer;

int main(int argc, char *argv[])
{
	signal(SIGINT, sigHandler);

	// Setup socket connection

	struct sockaddr_in serv_addr;

	char *server = "127.0.0.1";

	memset(recvBuff, '0',sizeof(recvBuff));
	if((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0)
	{
		printf("\n Error : Could not create socket \n");
		return 1;
	}

	memset(&serv_addr, '0', sizeof(serv_addr));

	serv_addr.sin_family = AF_INET;
	serv_addr.sin_port = htons(1234);

	if(inet_pton(AF_INET, server, &serv_addr.sin_addr)<=0)
	{
		printf("\n inet_pton error occured\n");
		return 1;
	}

	if( connect(sockfd, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0)
	{
	   printf("\n Error : Connect Failed \n");
	   return 1;
	}

	// Connected with socket

	bzero(sendBuff, sizeof(sendBuff));

	// Read IP Adress + hostname

	char hostbuffer[256];
	struct hostent *host_entry;
	int hostname;

	// To retrieve hostname
	hostname = gethostname(hostbuffer, sizeof(hostbuffer));
	checkHostName(hostname);

	// To retrieve host information
	host_entry = gethostbyname(hostbuffer);
	checkHostEntry(host_entry);

	// To convert an Internet network
	// address into ASCII string
	IPbuffer = inet_ntoa(*((struct in_addr*)host_entry->h_addr_list[0]));

	/*start filecheck and update task*/
	pthread_t appManagementTask;
	pthread_create(&appManagementTask, NULL, statusTask, NULL);
	//statusTask();

	/*start status sender*/
	pthread_t statusSendTask;
	pthread_create(&statusSendTask, NULL, sendTask, NULL);

	/*pid_t pid = fork();
	switch(pid){
		case -1:
			printf("error forking");
			return EXIT_FAILURE;
		case 0:		// child process, looks for new application files and sends their status
			statusTask();
			return EXIT_SUCCESS;

		default:	// parent process, receives control messages, handles operations
			break;
	}*/

	pthread_join(statusSendTask, NULL);
	pthread_join(appManagementTask, NULL);

    close(sockfd);

    return 0;
}

bool applicationIsValid(int32_t internalCounter, int32_t oldInternalCounter){
	if(internalCounter == oldInternalCounter) return false;
	return true;
}

void appendStatusToBuffer(char* buffer, char* applicationName, char* status){
	strcat(buffer,"{\"applicationname\":\"");
	strcat(buffer, applicationName);
	strcat(buffer,"\",\"status\":\"");
	strcat(buffer,status);
	strcat(buffer,"\"},");
}

void sendTask(){
	while(running){
		printf("sending\n");
		sendStatusMessages();
		sleep(1);
	}
}

void statusTask(){
	volatile int32_t* mems[MAXNUMBERAPPLICATIONS];  	//array for shared memories
	FILE* fds[MAXNUMBERAPPLICATIONS];			//Filedescriptors to get shared memory key from each application
	char* applicationNames[MAXNUMBERAPPLICATIONS];
	char* registeredFileNames[MAXNUMBERAPPLICATIONS];
	int keys[MAXNUMBERAPPLICATIONS];
	applicationNames[0] = malloc(100);
	registeredFileNames[0] = malloc(100);
	bool applicationValid[MAXNUMBERAPPLICATIONS];
	int sharedMemoryKeys[MAXNUMBERAPPLICATIONS];	//keys read from application file
	int shmids[MAXNUMBERAPPLICATIONS];				//shared memory ids

	int32_t oldcounters[MAXNUMBERAPPLICATIONS];	//old reported counters from applications
	bzero(oldcounters, MAXNUMBERAPPLICATIONS*sizeof(int32_t));

	char readFileBuffer[100];		//readbuffer from files
	char statusBuffer[1000];		//statusbuffer for sending status

	int numberApplications = 0;


	DIR* d;									//Directory where files at
	struct dirent* dir;

	char* currentApplicationName = malloc(100);
	char* status = malloc(30);

	int i = 0;

	while(running){
		d = opendir(FILEPATH);
		if(d){

			for(i = 0; i < numberApplications; i++){
				applicationValid[i] = false;
			}
			// check for new files in filepath
			while((dir=readdir(d)) != NULL) {
				if(strcmp(dir->d_name, ".")&&strcmp(dir->d_name,"..")){
				//if(i > 2){		//first 2 entries are "." and ".."
					int j = 0;
					bool createNew = true;
					for(j = 0; j < numberApplications+1; j++){	//check if this file/applications is already registered
						if(strcmp(dir->d_name,registeredFileNames[j])==0){
							applicationValid[j] = true;
							createNew = false;
							break;
						}
					}

					if(createNew){
						registeredFileNames[numberApplications] = malloc(100);
						applicationNames[numberApplications] = malloc(100);

						strcpy(currentApplicationName, FILEPATH);
						strcat(currentApplicationName, dir->d_name);

						strcpy(registeredFileNames[numberApplications], dir->d_name);

						fds[numberApplications] = fopen(currentApplicationName, "r");	//open file, read only

						int k = 0;
						size_t len = 0;

						fscanf(fds[numberApplications], "%s", applicationNames[numberApplications]);	//read and save applicationname
						fscanf(fds[numberApplications], "%d", &keys[numberApplications]);				//read and save shared memory key

						applicationValid[numberApplications] = true;

						shmids[numberApplications] = shmget(keys[numberApplications], MemSize, 0666);
						if(shmids[numberApplications] < 0){
							applicationValid[numberApplications] = false;
						}

						mems[numberApplications] = malloc(MemSize);

						mems[numberApplications] = shmat(shmids[numberApplications], NULL, 0);
						if(mems[numberApplications]==(int32_t*)-1){
							applicationValid[numberApplications] = false;
						}
						numberApplications++;

					}
				}
			}
			closedir(d);

			// build status of all active applications

			if(numberApplications > 0){
				strcpy(applicationStates, "[");	//start bracket of json array
				bool hasEntry = false;
				for(i = 0; i < numberApplications; i++){
					if(applicationValid[i]){		//only report status if application is valid
						if(mems[i][0] == 1) strcpy(status,"running");
						else if(mems[i][0] == 0) strcpy(status,"idle");

						if(!applicationIsValid(mems[i][1], oldcounters[i])){
							strcpy(status,"not incrementing");
						}
						oldcounters[i] = mems[i][1];

						appendStatusToBuffer(applicationStates, applicationNames[i], status);
					}else{
						// remove not valid applications and decrease application counter
						int k = 0;
						for(k = i; k < numberApplications-1; k++){
							*mems[k] = *mems[k+1];
							*fds[k] = *fds[k+1];
							*applicationNames[k] = *applicationNames[k+1];
							*registeredFileNames[k] = *registeredFileNames[k+1];
							keys[k] = keys[k+1];
							applicationValid[k] = applicationValid[k+1];
							sharedMemoryKeys[k] = sharedMemoryKeys[k+1];
							shmids[k] = shmids[k+1];
							oldcounters[k] = oldcounters[k+1];
						}
						*applicationNames[numberApplications-1] = "";
						*registeredFileNames[numberApplications-1] = "";
						keys[numberApplications-1] = 0;
						applicationValid[numberApplications-1] = false;
						sharedMemoryKeys[numberApplications-1] = 0;
						shmids[numberApplications-1] = 0;
						oldcounters[numberApplications-1] = 0;
						i--;		//moved everything -1, so i needs to be repeated
						numberApplications--;
					}
				}

				if(applicationStates[strlen(applicationStates)-1]==","){	//remove the last "," to get a valid json packet
					applicationStates[strlen(applicationStates)-1] = 0;
				}

				strcat(applicationStates,"]");	//end bracket of json array
			}else{
				strcpy(applicationStates, "[]");

			}
			//strcpy(statusMemory, applicationStates);	//put actual application states in shared memory
			sleep(1);
		}
	}
}

/*
 * Status message layout
 * {
 * 		"ip":"127.0.1.1",
 * 		"cpu":20.0,
 * 		("ram": 14.0,)
 * 		"applications":[{
 * 			"application1":"running",
 * 			"application2":"idle",
 * 			"application3":"idle"
 * 		}]
 * 	}
 * */

void sendStatusMessages(){
	bzero(sendBuff, 1000);
	strcpy(sendBuff,"{");
	strcat(sendBuff,fillIPMessage());
	strcat(sendBuff,getCPUMessage());
	strcat(sendBuff,fillStatusMessage());
	strcat(sendBuff,"}\r\n");
	printf("sendbuff length: %d\n",strlen(sendBuff));

	send(sockfd, sendBuff, strlen(sendBuff), 0);
}

char* getCPUMessage(){
	long double a[4], b[4], loadavg;
	FILE *fp;
	float cpuUsage = 0.0f;
	char buf[16];
	char* message;

	fp = fopen("/proc/stat","r");
	int i = 0;
	for(i = 0; i < 10; i++){
		fscanf(fp,"%*s %Lf %Lf %Lf %Lf", &a[0], &a[1], &a[2], &a[3]);
		usleep(20000);
		fscanf(fp,"%*s %Lf %Lf %Lf %Lf", &b[0], &b[1], &b[2], &b[3]);

		loadavg = ((b[0]+b[1]+b[2]) - (a[0]+a[1]+a[2]))/((b[0]+b[1]+b[2]+b[3]) - (a[0]+a[1]+a[2]+a[3]));
		loadavg = loadavg*100; // in %
		if(loadavg >= 0 && loadavg <= 100){
			cpuUsage = (float)(loadavg);
			sprintf(buf, "%f", cpuUsage);
			buf[5] = '\0';
			//message = "{\"cpu\":\"21.3\"}\n";
			message = fillCPUMessage(&buf);
		}

		//printf("The current CPU utilization is : %Lf\n",loadavg);
	}

	return message;
}

char* fillCPUMessage(const char* cpuValue){
	static char dest[100];
	strcpy(dest, "\"cpu\":\"");
	strcat(dest, cpuValue);
	strcat(dest, "\",");
	return dest;
}
char* fillIPMessage(){
	static char dest[100];
	strcpy(dest, "\"ip\":\"");
	strcat(dest, IPbuffer);
	strcat(dest, "\",");
	return dest;
}
char* fillStatusMessage(){
	static char dest[1000];
	//strcpy(applicationStates, statusMemory);	//get current applicationstates from shared memory

	strcpy(dest, "\"applications\":");
	strcat(dest, applicationStates);
	//strcat(dest, "");
	return dest;
}

void flushSocket(int fd){
	int flag = 1;
	setsockopt(fd, IPPROTO_TCP, TCP_NODELAY, (char*)&flag, sizeof(flag));
	flag = 0;
	setsockopt(fd, IPPROTO_TCP, TCP_NODELAY, (char*)&flag, sizeof(flag));
}
