#include <stdio.h>

double ratio=1;

int inframes=0;
int outframes=0;

int output=0;
int debug=0;

unsigned int last_picture_number=0;

#define WINDOW 80

int get_bit(unsigned char *b,int o)
{
	int byte=o>>3;
	int bit=o&7;
	int r=0;

	if (b[byte]&(1<<(7-bit))) r=1;

	if (debug>2) fprintf(stderr,"bit %d is %d\n",o,r);

	return r;
}

int get_bits(unsigned char *b,int c,int *o)
{
	int r=0;
	while(c) {
		r=r<<1;
		r|=get_bit(b,*o); 	
		(*o)++;
		c--;
	}
	return r;
}

int main(int argc, char **argv)
{
	// Look for 0x00 0x00 0x80-0x83 0xXX
	// The 23-29th bits are the number of frames skipped since last time
	// If we add in imaginary frames, we will have the net result of slowing
	// The video down - if ffmpeg used these fields!

	unsigned char b[WINDOW];
	int offset;
	int i;

	FILE* inputFile = NULL;
	FILE* outputFile = NULL;
	
	while(inputFile == NULL) {
		inputFile = fopen(argv[1], "r");
	}
	
	outputFile = fopen(argv[2], "w");

	for(i=0;i<WINDOW;i++) b[i]=(unsigned char)fgetc(inputFile);
	offset=4;

	while(1)
	{
		while(feof(inputFile)) {
			fprintf(stderr, "No more bytes, sleep... ");
			sleep(2);
			fprintf(stderr, "Done\n");
			fflush(stderr);
		}
		if (b[0]=='m'&&b[1]=='d'&&b[2]=='a'&&b[3]=='t') {
			int j;
			for(i=0;i<WINDOW-4;i++) b[i]=b[i+4];
			for(j=0;j<4;j++)
			{
				b[WINDOW-1-(4-j)]=(unsigned char)fgetc(inputFile);
				offset++;
			}
			output=1;
		}
		if ((b[0]==0x3c)&&(b[32]==0x3c)&&(b[64]==0x3c))
		{
			/* Audio sample. Consider writing out to a file.
			   Eat sample. */
			int j;
			fprintf(stderr,"Skipping audio sample at %x\n",offset);
			output=33;
		}
		if (b[0]==0&&b[1]==0&&(b[2]>=0x80&&b[2]<=0x83))
		{
			int bit_offset=0;

			int psc=get_bits(b,22,&bit_offset);
			int tr=get_bits(b,8,&bit_offset);
			int j1=get_bits(b,2,&bit_offset);
			int split_screen=get_bits(b,1,&bit_offset);
			int document_camera=get_bits(b,1,&bit_offset);
			int freeze_release=get_bits(b,1,&bit_offset);
			int source_format=get_bits(b,3,&bit_offset);
			int pctype=get_bits(b,1,&bit_offset);
			int umv_mode=get_bits(b,1,&bit_offset);
			int sac_mode=get_bits(b,1,&bit_offset);
			int ap_mode=get_bits(b,1,&bit_offset);
			int pbframe=get_bits(b,1,&bit_offset);
			if (debug) {
				fprintf(stderr,"psc=%06x, tr=%d, src_fmt=%d\n",psc,tr,source_format);
				if (pbframe) fprintf(stderr,"PB-Frame\n"); else fprintf(stderr,"P-Frame\n");
			}

			if( (last_picture_number&~0xFF)+tr < last_picture_number)
				tr+= 256;
			unsigned int picture_number= (last_picture_number&~0xFF) + tr;

			if (debug) fprintf(stderr,"Missing %d frames (%d -- %d).\n",
					picture_number-last_picture_number,last_picture_number,picture_number);

			/*
			   A minimal H.263 frame:
			   0x00 0x00 1 00000 xxxx xxxx(tr) 1 0 0 0 0 111 1(inter picture) 0 0 0 0 00000(quant)
			   0(cpm) 000(trb) 00(dbquant) 0(pei)
			   (this is where macroblocks would go)
			   0x00 0x00 1 11111(eos) <zero bits to byte align>

			   0x00 0x00 0x80 0x02 0x1e 0x00
			   0x00
			   0x00 0x01 0xf8 
			 */

			output=1;

			if (last_picture_number) {
				last_picture_number++;
				while(last_picture_number<picture_number) {
					usleep(50*1000);
					if (debug) fprintf(stderr,"Inserted dummy frame %d\n",last_picture_number);
					if (output) fprintf(outputFile,"%c%c%c%c%c%c%c%c%c%c",0x00,0x00,0x80,0x02,0x1e,0x00,0x00,0x00,0x01,0xf8);
					last_picture_number++;
				}
			} else last_picture_number=picture_number;

			int pquant=get_bits(b,5,&bit_offset);

			if (debug) fprintf(stderr,"pquant=%02x\n",pquant);

			int cpm=get_bits(b,1,&bit_offset);

			if (debug) fprintf(stderr,"cpm=%d\n",cpm);

			int psbi=0;
			if (cpm) psbi=get_bits(b,2,&bit_offset);

			int trb_offset=bit_offset;
			int trb=0;
			if (pbframe) trb=get_bits(b,3,&bit_offset);
			if (debug) fprintf(stderr,"trb=%d\n",trb);

		}

		if (output==1) fputc(b[0],outputFile);
		if (output>1) output--;
		for(i=0;i<WINDOW-1;i++) b[i]=b[i+1];
		b[WINDOW-1]=(unsigned char)fgetc(inputFile);
		offset++;
	}
	if (output==1) for(i=0;i<WINDOW-1;i++) fputc(b[i],outputFile);
	return 0;
}
