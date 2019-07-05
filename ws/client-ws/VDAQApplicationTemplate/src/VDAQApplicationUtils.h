/*
 * VDAQApplicationUtils.h
 *
 *  Created on: Jun 13, 2019
 *      Author: simon
 */

#ifndef VDAQAPPLICATIONUTILS_H_
#define VDAQAPPLICATIONUTILS_H_

#include <sys/types.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/wait.h>
#include <sys/shm.h>
#include <string.h>
#include <stdbool.h>

/*
 * 4 byte status
 * 4 byte still alive counter
 * 4 byte external control
 *
 * status:
 * 	0: idle
 * 	1: running
 *
 * 	100: error
 *
 * 	external control:
 * 	0: start
 * 	1: stop
 * */

#define SIGINT 2

#define SHMID_START 1000
#define MemSize 32
#define FILEPATH "/home/simon/test/vdaq/"

int32_t* mem;
volatile int32_t internalCounter = 0;
int shmid;
bool internalRunning = true;
bool running = true;
char filename[100];

static void sigHandler(int sigNr){
	internalRunning = false;
	running = false;
	cleanup();
	exit(0);
}

char* getFilePath(char filename[]){
	char filepath[100];
	strcpy(filepath, FILEPATH);
	strcat(filepath, filename);
	return filepath;
}

void* init(char application_name[]){
	signal(SIGINT, sigHandler);
	strcpy(filename, FILEPATH);
	strcat(filename, application_name);
	strcat(filename, ".txt");
	FILE* fp = fopen(filename, "w");
	//FILE* fp = fopen("/home/simon/test/vdaq/VDAQTest.txt", "w");
	if(fp < 0){
		printf("could not open file %s", filename);
		return (void*)-1;
	}

	printf("try to allocate memory in shared segment (%d bytes)\n", MemSize);

	int i;

	int key;
	for(i = SHMID_START; i < (2*SHMID_START); i++){
		shmid = shmget(i, MemSize, IPC_CREAT|IPC_EXCL|0666);
		//shmid = shmget(i, MemSize, IPC_CREAT|0666);			//for test purpose, to remove previously generated shared memories
		if (shmid < 0) {
			printf("could not get shared memory\n");
		}else{
			key = i;
			//shmctl(shmid, IPC_RMID, NULL);						//for test purpose, to remove previously generated shared memories
			break;
		}
	}

	if(shmid < 0){
		return (void*)-2;
	}

	mem =(int32_t*) shmat(shmid, NULL, 0);
	if (mem == (int32_t*)-1) {
			printf("could not map\n");
			exit(EXIT_FAILURE);
	}

	/* got shared memory here */

	fprintf(fp, "%s\n%d", application_name, key);
	fclose(fp);

	pid_t pid = fork();
	switch(pid){
		case -1:
			printf("error forking");
		case 0:		// child process, looks for new application files and sends their status
			runningCounter();
			break;

		default:
			break;// parent process, receives control messages, handles operations
	}

}

void runningCounter(){
	while(internalRunning){
		mem[1] = internalCounter++;
		if(mem[2] == 0){
			running = true;
			mem[0] = 1;
		}else if(mem[2] == 1){
			running = false;
			mem[0] = 0;
		}
		usleep(800000);
	}
}

void cleanup(){
	shmctl(shmid, IPC_RMID, NULL);
	int status = remove(filename);
	if(status == 0) printf("deleted application file\n");
	printf("cleaned up successfully\n");
}

void writeStatus(int32_t status){
	mem[0] = status;
	//mem[1] = internalCounter;
	//mem[2] = 0;
	//mem[3] = 0;
}


#endif /* VDAQAPPLICATIONUTILS_H_ */
