/*
 ============================================================================
 Name        : VDAQApplicationTemplate.c
 Author      : sfink
 Version     :
 Copyright   : 
 Description : Hello World in C, Ansi-style
 ============================================================================
 */

#include <stdio.h>
#include <stdlib.h>
#include "VDAQApplicationUtils.h"

#define APPLICATIONNAME "VDAQAPPLICATIONTEMPLATE"

int main(void) {

	init(APPLICATIONNAME);

	int i = 0;
	int j = 0;
	int32_t status = 1;
	while(1){
		if(running){
			printf("%d; ", status);
			printf("%d", mem[1]);
			printf("\n");
			sleep(1);
		}else{
			sleep(2);
		}
	}

	cleanup();
	return EXIT_SUCCESS;
}
