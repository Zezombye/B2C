BCDvar *B2C_add(BCDvar *buffer, BCDvar *a, BCDvar *b) {
	int expA, expB, expDiff, carry = 0, tempadd, biggerExp_int;
	unsigned char tempByte = 0;
	int result[15] = {0};
	BCDvar *smallerExp, *biggerExp;
	
	//Determine if these numbers are negative
	//If both are negative, add their opposites
	//Ex: (-4)+(-3) -> -(4+3)
	if ((*a)[0] > 0x30 && (*b)[0] > 0x30) {
		BCDvar a2,b2;
		memcpy(a2, *a, 9);
		memcpy(b2, *b, 9);
		a2[0] -= 0x50;
		b2[0] -= 0x50;
		B2C_add(buffer, &a2, &b2);
		(*buffer)[0] += 0x50;
		return buffer;
	}
	//If one of them is negative, substract them
	//Ex: (-4)+3 -> 3-4
	if ((*a)[0] > 0x30) {
		BCDvar a2;
		memcpy(a2, *a, 9);
		a2[0] -= 0x50;
		return B2C_sub(buffer, b, &a2);
	}
	if ((*b)[0] > 0x30) {
		BCDvar b2;
		memcpy(b2, *b, 9);
		b2[0] -= 0x50;
		return B2C_sub(buffer, a, &b2);
	}
	
	//First determine which number has a bigger exponent
	expA = getExp(a);
	expB = getExp(b);
	if (expA > expB) {
		smallerExp = b;
		biggerExp = a;
		expDiff = expA-expB;
		biggerExp_int = expA;
	} else {
		smallerExp = a;
		biggerExp = b;
		expDiff = expB-expA;
		biggerExp_int = expB;
	}
	
	//Add the number with the smallest exponent to the one with the biggest
	for (i = 14; i >= 0; i--) {
		tempadd = 0;
		if (i >= expDiff) tempadd += getDigit(smallerExp, i-expDiff);
		tempadd += getDigit(biggerExp, i) + carry;
		if (tempadd >= 10) {
			result[i] = tempadd-10;
			carry = 1;
		} else {
			result[i] = tempadd;
			carry = 0;
		}
		//locate(i+1, 3); printChar(carry+'0');
		//locate(i+1, 4); printChar(tempadd+'0');
	}
	//Increment exponent if necessary
	if (carry == 1) {
		biggerExp_int++;
	}
	//locate(1,6); printChar(biggerExp_int);
	//locate(3,6); printChar(expA+'0');
	//locate(5,6); printChar(expB+'0');
	//Put exponent in buffer
	(*buffer)[0] = (biggerExp_int/100 << 4) + (biggerExp_int%100)/10;
	tempByte = (biggerExp_int%10 << 4) + carry;
	if (carry == 1) {
		(*buffer)[1] = tempByte;
	}
	//Put result in buffer
	for (i=0; i < 15-carry; i++) {
		if ((i+carry)%2) {
			tempByte = result[i] << 4;
			
		} else {
			(*buffer)[(i+carry+1)/2+1] = tempByte + result[i];
			
		}
	}
	//locate(1,2); printBCDvarBytes(buffer);
	//locate(1,1);
	return buffer;
}


BCDvar *B2C_sub(BCDvar *buffer, BCDvar *a, BCDvar *b) {
//Substract a positive number B from a positive number A (A-B).
//A must be greater than B for the algorithm to function.
//However this function does the necessary checks so A and B can be any reals.
	int expA, expB, expDiff, carry = 0, tempsub, compareResult;
	unsigned char tempByte;
	int result[15] = {0};
	//Check for special cases
	//a < 0 and b < 0 -> (-b)-(-a)
	//Ex: (-4)-(-3) -> 3-4
	if ((*a)[0] > 0x30 && (*b)[0] > 0x30) {
		BCDvar a2, b2;
		memcpy(a2, *a, 9);
		memcpy(b2, *b, 9);
		a2[0] -= 0x50;
		b2[0] -= 0x50;
		return B2C_sub(buffer, &b2, &a2);
	}
	//a > 0 and b < 0 -> a+(-b)
	//Ex: 3-(-4) -> 3+4
	if ((*b)[0] > 0x30) {
		BCDvar b2;
		memcpy(b2, *b, 9);
		b2[0] -= 0x50;
		return B2C_add(buffer, a, &b2);
	}
	//a < 0 and b > 0 -> -(a+b)
	//Ex: (-4)-3 -> -(4+3)
	if ((*a)[0] > 0x30) {
		BCDvar a2;
		memcpy(a2, *a, 9);
		a2[0] -= 0x50;
		B2C_add(buffer, &a2, b);
		(*buffer)[0] += 0x50;
		return buffer;
	}
	
	compareResult = B2C_compareBCDvars(a, b);
	//a < b -> -(a-b)
	//Ex : 3-4 -> -(4-3)
	if (compareResult == A_LESS_THAN_B) {
		B2C_sub(buffer, b, a);
		(*buffer)[0] += 0x50;
		return buffer;
		
	//if a=b then a-b = 0
	} else if (compareResult == A_EQUALS_B) {
		return &_0_;
	}
	
	//Determine the exponent difference; A is always the biggest number
	expA = getExp(a);
	expB = getExp(b);
	expDiff = expA-expB;
	
	//Substract
	for (i = 14; i >= 0; i--) {
		tempsub = getDigit(a, i) - carry;
		if (i >= expDiff) tempsub -= getDigit(b, i-expDiff);
		if (tempsub < 0) {
			tempsub += 10;
			carry = 1;
		} else {
			carry = 0;
		}
		result[i] = tempsub;
	}
	
	//Determine new exponent, reuse the var expA and expDiff
	//For example: 34-32 (3.4e1 - 3.2e1) will yield 2 (0.2e1) which must be converted to 2e0
	for (i = 0; result[i] == 0 && i < 15; i++) {
		expA--;
	}
	expDiff = i;
	
	//Put exponent in buffer
	(*buffer)[0] = (expA/100 << 4) + (expA%100)/10;
	tempByte = (expA%10 << 4);
	
	//Put result in buffer
	for (i = 0; i < 15; i++) {
		if (i%2) {
			tempByte = (i < 15-expDiff ? result[i+expDiff] << 4 : 0);
		} else {
			(*buffer)[(i+1)/2+1] = tempByte + (i < 15-expDiff ? result[i+expDiff] : 0);
		}
	}
	
	return buffer;
	
}

BCDvar *B2C_mult(BCDvar *buffer, BCDvar *a, BCDvar *b) {

	//TODO: implement algorithm
	//Here is my attempt at doing it (warning: yields completely false results)
	//Algorithm from wikipedia: https://en.wikipedia.org/wiki/Multiplication_algorithm#Long_multiplication
	/*int expA, expB, i, j, carry, tempDigit, expResult, firstDigitInResult = 0;
	unsigned char tempByte;
	char result[30] = {0};
	expA = getExp(a)%500-100;
	expB = getExp(b)%500-100;
	
	for (i = 15; i > 0; i--) { //digits of b
		carry = 0;
		for (j = 15; j > 0; j--) { //digits of a
			result[i+j-1] += carry + getDigit(a, j-1) + getDigit(b, i-1);
			carry = result[i+j-1]/10;
			result[i+j-1] %= 10;
		}
		result[i+15] += carry;
	}
	
	expResult = expA+expB + 100 + carry;
	
	//Put exponent in buffer
	(*buffer)[0] = (expResult/100 << 4) + (expResult%100)/10;
	tempByte = (expResult%10 << 4);
	
	//Calculate position of first non-zero digit in result
	for (i = 0; result[i] == 0 && i < 30; i++) {
		firstDigitInResult++;
		locate(1,7); Print("test");
	}
	for (i = 0; i < 15; i++) {
		locate(i+1, 4); printChar(result[i]+'0');
		locate(i+1, 5); printChar(result[i+15]+'0');
	}
	//Put result in buffer
	for (i = 0; i < 15; i++) {
		if (i%2) {
			tempByte = result[i+firstDigitInResult] << 4;
		} else {
			(*buffer)[(i+1)/2+1] = tempByte + result[i+firstDigitInResult];
		}
	}*/
	
	const char *function = "A\xA9""B";
	setAlphaVar('A', a);
	setAlphaVar('B', b);
	calcExp(&function, dummyOpCode, buffer, 1);
	
	return buffer;
}

BCDvar *B2C_div(BCDvar *buffer, BCDvar *a, BCDvar *b) {
//TODO: make algorithm for division

	const char *function = "A\xB9""B";
	setAlphaVar('A', a);
	setAlphaVar('B', b);
	calcExp(&function, dummyOpCode, buffer, 1);
	
	return buffer;
}

BCDvar *B2C_pow(BCDvar *buffer, BCDvar *a, BCDvar *b) {
//TODO: make algorithm for division

	const char *function = "A\xA8""B";
	setAlphaVar('A', a);
	setAlphaVar('B', b);
	calcExp(&function, dummyOpCode, buffer, 1);
	
	return buffer;
}

BCDvar *B2C_sqrt(BCDvar *buffer, BCDvar *a, BCDvar *b) {
//TODO: make algorithm for division

	const char *function = "A\x86""B";
	setAlphaVar('A', a);
	setAlphaVar('B', b);
	calcExp(&function, dummyOpCode, buffer, 1);
	
	return buffer;
}

BCDvar *B2C_greaterThan(BCDvar *a, BCDvar *b) {
	if (B2C_compareBCDvars(a, b) == A_GREATER_THAN_B) return &_1_;
	return &_0_;
}

BCDvar *B2C_greaterOrEqualThan(BCDvar *a, BCDvar *b) {
	if (B2C_compareBCDvars(a, b) == A_LESS_THAN_B) return &_0_;
	return &_1_;
}

BCDvar *B2C_lessThan(BCDvar *a, BCDvar *b) {
	if (B2C_compareBCDvars(a, b) == A_LESS_THAN_B) return &_1_;
	return &_0_;
}

BCDvar *B2C_lessOrEqualThan(BCDvar *a, BCDvar *b) {
	if (B2C_compareBCDvars(a, b) == A_GREATER_THAN_B) return &_0_;
	return &_1_;
}

BCDvar *B2C_equalTo(BCDvar *a, BCDvar *b) {
	if (B2C_compareBCDvars(a, b) == A_EQUALS_B) return &_1_;
	return &_0_;
}

BCDvar *B2C_notEqualTo(BCDvar *a, BCDvar *b) {
	if (B2C_compareBCDvars(a, b) == A_EQUALS_B) return &_0_;
	return &_1_;
}

int B2C_compareBCDvars(BCDvar *a, BCDvar *b) {
//Return: A_GREATER_THAN_B (1), A_EQUALS_B (0) or A_LESS_THAN_B (-1)
	int expA, expB, areBothNegatives = 1, tempDigitA, tempDigitB;
	
	//Compare sign
	//If both are negatives we'll compare them then yield the opposite result
	if ((*a)[0] > 0x30 && (*b)[0] > 0x30) {
		areBothNegatives = -1;
	}
	//If a > 0 and b < 0 then a > b
	if ((*a)[0] < 0x30 && (*b)[0] > 0x30) {
		return A_GREATER_THAN_B;
	}
	//If a < 0 and b > 0 then a < b
	if ((*a)[0] > 0x30 && (*b)[0] < 0x30) {
		return A_LESS_THAN_B;
	}
	//Compare exponents
	expA = getExp(a);
	expB = getExp(b);
	if (areBothNegatives) {
		expA -= 500;
		expB -= 500;
	}
	if (expA > expB) {
		return areBothNegatives * A_GREATER_THAN_B;
	} else if (expA < expB) {
		return areBothNegatives * A_LESS_THAN_B;
	}
	
	//Exponents are equal ; compare digits
	for (i = 0; i < 15; i++) {
		tempDigitA = getDigit(a, i);
		tempDigitB = getDigit(b, i);
		if (tempDigitA > tempDigitB) {
			return areBothNegatives * A_GREATER_THAN_B;
		} else if (tempDigitA < tempDigitB) {
			return areBothNegatives * A_LESS_THAN_B;
		}
	}
	//At this point a = b
	return A_EQUALS_B;
}

BCDvar *B2C_not(BCDvar *a) {
	if ((*a)[1]) return &_0_;
	return &_1_;
}
BCDvar *B2C_and(BCDvar *a, BCDvar *b) {
	if ((*a)[1] && (*b)[1]) return &_1_;
	return &_0_;
}
BCDvar *B2C_or(BCDvar *a, BCDvar *b) {
	if ((*a)[1] || (*b)[1]) return &_1_;
	return &_0_;
}
BCDvar *B2C_xor(BCDvar *a, BCDvar *b) {
	//Thanks stackoverflow for optimized xor function
	if (!((*a)[1]) != !((*b)[1])) return &_1_;
	return &_0_;
}

BCDvar *B2C_int(BCDvar *buffer, BCDvar *a) {
	const char *function = "\xA6""A";
	setAlphaVar('A', a);
	calcExp(&function, dummyOpCode, buffer, 1);
	
	return buffer;
}

unsigned char* B2C_convToStr(BCDvar *nb) {
	bcdToStr(nb, stringBuffer);
	return stringBuffer;
}

void B2C_setListRow(unsigned int nbList, unsigned int row, BCDvar *value) {
	memcpy(list[nbList].data[row], *value, sizeof(BCDvar)-3);
}

void B2C_setDimList(unsigned int nbList, unsigned short nbElements) {
	list[nbList].nbElements = nbElements;
	list[nbList].data = calloc(nbElements+1, sizeof(BCDvar));
}

List B2C_newList(int nbElements, ...) {
	List list;
	va_list vaList;
	list.nbElements = nbElements;
	list.data = calloc(nbElements+1, sizeof(BCDvar));
	va_start(vaList, nbElements);
	//memcpy(list.data[0], _0_, sizeof(BCDvar));
	for (i = 1; i <= nbElements; i++) {
		memcpy(list.data[i], va_arg(vaList, BCDvar), sizeof(BCDvar)-3);
	}
	va_end(vaList);
	return list;
}

void B2C_setDimMat(unsigned char matrix, unsigned short x, unsigned short y) {
	mat[matrix].width = x;
	mat[matrix].height = y;
	mat[matrix].data = calloc(x*y, sizeof(BCDvar));
}

void B2C_setMatCase(unsigned char matrix, unsigned int x, unsigned int y, BCDvar *value) {
	memcpy(mat[matrix].data[mat[matrix].height*(x-1)+y-1], *value, sizeof(BCDvar)-3);
}

void B2C_fillMat(BCDvar *value, unsigned char matrix) {
	for (i = 0; i < mat[matrix].width*mat[matrix].height; i++) {
		memcpy(mat[matrix].data[i], *value, sizeof(BCDvar)-3);
	}
}

void B2C_clrMat(unsigned char matrix) {
	free(mat[matrix].data);
	mat[matrix].width = 0;
	mat[matrix].height = 0;
}

unsigned int B2C_convToUInt(BCDvar *nb) {
	unsigned int result = 0;
	//gets the 3rd digit of the exponent - that means it doesn't work for Uints > 10^10
	//however this function is intended for locate, matrixes, lists, etc so it isn't needed
	int power = ((*nb)[1]>>4) + 1; 
	for (i = 1; i <= power; i++) {
		if (i%2) {
			result += ((*nb)[i/2+1]&0xF) * pow(10, power-i);
		} else {
			result += ((*nb)[i/2+1]>>4) * pow(10, power-i);
		}
	}
	return result;
}
int B2C_convToInt(BCDvar *nb) {
	//(almost) copy of B2C_convToUInt, any changes made here must be reflected in the other function
	int result = 0;
	int power = ((*nb)[1]>>4) + 1; 
	for (i = 1; i <= power; i++) {
		if (i%2) {
			result += ((*nb)[i/2+1]&0xF) * pow(10, power-i);
		} else {
			result += ((*nb)[i/2+1]>>4) * pow(10, power-i);
		}
	}
	if ((*nb)[0] > 0x30) { //exponent is higher than 300 so number is negative
		result = -result;
	}
	return result;
}
BCDvar *B2C_getkey() {
	if (!prgmGetkey(&getkeyBuffer)) {
		B2C_exit(NO_ERROR);
	}
	return &getkeyBuffer;
}
#ifdef USES_INTERRUPTION_TIMER
void exitTimerHandler() {
	/*short menuCode = 0x0308;
	putMatrixCode(&menuCode);*/
	if (IsKeyDown(KEY_CTRL_AC)) {
		KillTimer(INTERRUPTION_TIMER);
		B2C_exit(NO_ERROR);
	} else {
		SetTimer(INTERRUPTION_TIMER, 50, (void (*)(void))exitTimerHandler);
	}
}
#endif
void B2C_exit(int exitCode) {
	short menuCode = 0x0308;
	PopUpWin(4);
	
	switch(exitCode) {
		case NO_ERROR:
			locate(5,3); Print((unsigned char*)"Interruption");
			break;
		case MEMORY_ERROR:
			locate(4,3); Print((unsigned char*)"Erreur m\xE6""\x0A""moire");
			break;
		case ARG_ERROR:
			locate(4,3); Print((unsigned char*)"Erreur argument");
			break;
	}
	locate(4,5); Print((unsigned char*)"Appuyer:[EXIT]");
	
	//This syscall could resolve internationalisation problems,
	//however it seems quite buggy (for example: interrupt by pressing
	//AC/on, repress this key, then try to go back to the addin; the
	//popup won't be there and you won't be able to do anything). I also
	//managed to crash the main menu using this syscall.
	
	//dispErrorMessage(exitCode);
	
	while (1) {
		putMatrixCode(&menuCode);
		do {
			GetKey(&key);
		} while (key != KEY_CTRL_EXIT);
	}
}

BCDvar *B2C_rand(BCDvar *buffer) {
	int randDigit;
	char tempByte;
	//Initialise exponent to 99 (-1)
	(*buffer)[0] = 9;
	tempByte = 0x90;
	for (i = 0; i < 15; i++) {
		if (i%2) {
			tempByte = (rand()%10) << 4;
		} else {
			(*buffer)[(i+1)/2+1] = tempByte + (rand()%10);
		}
	}
	return buffer;
}

BCDvar *B2C_ranInt(BCDvar *buffer, BCDvar *a, BCDvar *b) {
	//A + Int (Ran# * (B - A + 1))
	const char *function = "A""\x89""\xA6""(""\xC1""\xA9""(B""\x99""A""\x89""1))";
	setAlphaVar('A', a);
	setAlphaVar('B', b);
	calcExp(&function, dummyOpCode, buffer, 1);
	return buffer;
}
/*
BCDvar *B2C_getAlphaVar(unsigned char var) {
	getAlphaVar(var, alphaVarBuffer);
	return &alphaVarBuffer;
}
*/
BCDvar *B2C_calcExp(unsigned char* exp) {
	calcExp(&exp, dummyOpCode, &expressionBuffer, 1);
	return &expressionBuffer;
}
void B2C_setAlphaVar(BCDvar *alphaVar, BCDvar *value) {
	memcpy(*alphaVar, *value, sizeof(BCDvar)-3);
}
void B2C_setStr(Str *str, Str *value, int isString) {
	free(str->data);
	str->length = value->length;
	str->data = malloc(value->length*2);
	memcpy(str->data, value->data, value->length*2);
	free_str(value);
}
unsigned char* B2C_strToCharArray(Str *str, int isString) {
	int j = 0;
	//Initialize the buffer
	memset(stringBuffer, 0x00, 512);
	//Goes through the string, starting at 2
	for (i = 0; i < str->length; i++) {
		//Skip null bytes
		if ((str->data[i])&0xFF00) {
			stringBuffer[j] = (((str->data[i])&0xFF00)>>8);
			j++;
		}
		//There is normally no need to skip null bytes here
		//if ((str->data[i])&0x00FF) {
		stringBuffer[j] = ((str->data[i])&0x00FF);
		j++;
		//}
	}
	free_str(str);
	return stringBuffer;
}
Str *B2C_charArrayToStr(unsigned char* charArray) {
	int strPos = 0;
	unsigned short currentChar;
	Str *result = malloc(sizeof(Str));
	result->data = calloc(strlen((char*)charArray), 2);
	for (i = 0; charArray[i]; i++) {
		currentChar = 0;
		if (charArray[i] == 0xE5 ||
				charArray[i] == 0xE6 ||
				charArray[i] == 0xE7 ||
				charArray[i] == 0xF7 ||
				charArray[i] == 0xF9 ||
				charArray[i] == 0x7F) {
			currentChar = charArray[i] << 8;
			i++;
		}
		result->data[strPos] = currentChar + charArray[i];
		strPos++;
	}
	result->length = strPos;
	return result;
}

BCDvar *B2C_strCmp(BCDvar *buffer, Str *str1, int isString1, Str *str2, int isString2) {
	//Gotos are evil
	//But sometimes evil is necessary for the greater good
	int j, isString = isString1;
	for (i = 0; i < str1->length && i < str1->length; i++) {
		//Case 1, most common: both characters are single bytes
		if (!(str1->data[i]&0xFF00) && !(str2->data[i]&0xFF00)) {
			if (str1->data[i] < str2->data[i]) {
				goto str1_lt_str2;
			} else if (str1->data[i] > str2->data[i]) {
				goto str1_gt_str2;
			}
		}
		//Case 2: str1[i] is single byte, str2[i] is multi byte
		//They can't be equal because the single byte at str1[i] can't match the multi byte prefix
		else if (!(str1->data[i]&0xFF00)) {
			if (str1->data[i] < (str2->data[i]>>8)) {
				goto str1_lt_str2;
			} else {
				goto str1_gt_str2;
			}
		}
		//Case 3: str1[i] is multi byte, str2[i] is single byte
		else if (!(str2->data[i]&0xFF00)) {
			if ((str1->data[i]>>8) < str2->data[i]) {
				goto str1_lt_str2;
			} else {
				goto str1_gt_str2;
			}
		}
		//Case 4: both are multi bytes
		else {
			if (str1->data[i] < str2->data[i]) {
				goto str1_lt_str2;
			} else if (str1->data[i] > str2->data[i]) {
				goto str1_gt_str2;
			}
		}
	}
	
	//Test for length
	if (str1->length < str2->length) {
		goto str1_lt_str2;
	} else if (str1->length > str2->length) {
		goto str1_gt_str2;
	}
	
	//goto equalStrings;
	//equalStrings:
	buffer = &_0_;
	goto freestrs;
	
	str1_gt_str2:
	buffer = &_1_;
	goto freestrs;
	
	str1_lt_str2:
	buffer = &__1_;
	//goto freestrs;
	
	freestrs:
	free_str(str1);
	isString = isString2;
	free_str(str2);
	return buffer;
}

BCDvar *B2C_strSrc(BCDvar *buffer, Str *str1, int isString1, Str *str2, int isString2, int n) {
	int isString = isString1;
	n--;
	exitIfNeg(n);
	for (i = n; i < str1->length-str2->length+1; i++) {
		if (!memcmp(str1->data+i, str2->data, str2->length*2)) {
			intToBCD(i+1, buffer);
			goto freestrs;
		}
	}
	
	//str_not_found:
	buffer = &_0_;
	
	freestrs:
	free_str(str1);
	isString = isString2;
	free_str(str2);
	return buffer;
}

Str *B2C_strInv(Str *str, int isString) {
	Str *result = malloc(sizeof(Str));
	result->data = malloc(2*str->length);
	result->length = str->length;
	for (i = 0; i < str->length; i++) {
		result->data[str->length-1-i] = str->data[i];
	}
	free_str(str);
	return result;
}

Str *B2C_strJoin(Str *str1, int isString1, Str *str2, int isString2) {
	int isString = isString1;
	Str *result = malloc(sizeof(Str));
	result->data = malloc(2*(str1->length + str2->length));
	result->length = str1->length + str2->length;
	memcpy(result->data, str1->data, str1->length * 2);
	memcpy(result->data+str1->length, str2->data, str2->length * 2);
	free_str(str1);
	isString = isString2;
	free_str(str2);
	return result;
}
BCDvar *B2C_strLen(BCDvar *buffer, Str *str, int isString) {
	intToBCD(str->length, buffer);
	return buffer;
}

Str *B2C_strMid2args(Str *str, int isString, int start) {
	//+1 because strings are 1-indexed in casio
	return B2C_strMid(str, isString, start, str->length+1-start);
}

Str *B2C_strMid(Str *str, int isString, int start, int offset) {
	Str *result = malloc(sizeof(Str));
	start--; //In casio, strings are 1-indexed!
	exitIfNeg(start);
	exitIfNeg(offset);
	if (offset+start > str->length) {
		offset = str->length-start;
	}
	if (start > str->length) {
		start = str->length;
	}
	result->data = malloc(offset * 2);
	result->length = offset;
	//Copy the substring
	memcpy(result->data, str->data+start, 2*offset);
	free_str(str);
	return result;
}
Str *B2C_charAt(Str *str, int isString, int charPos) {
	//Unused at the moment - not tested
	Str *result = malloc(sizeof(Str));
	charPos--; //In casio, strings are 1-indexed!
	exitIfNeg(charPos);
	result->data = malloc(2);
	result->length = 1;
	result->data[0] = str->data[charPos];
	free_str(str);
	return result;
}
Str *B2C_strUpr(Str *str, int isString) {
	Str *result = malloc(sizeof(Str));
	result->data = malloc(str->length * 2);
	memcpy(result->data, str->data, str->length * 2);
	result->length = str->length;
	for (i = 0; i < result->length; i++) {
		if (result->data[i] >= 'a' && result->data[i] <= 'z') {
			result->data[i] -= 32;
		}
	}
	free_str(str);
	return result;
}
Str *B2C_strLwr(Str *str, int isString) { //(almost) copy of B2C_strUpr - any changes made here must be reflected in strUpr
	Str *result = malloc(sizeof(Str));
	result->data = malloc(str->length * 2);
	memcpy(result->data, str->data, str->length * 2);
	result->length = str->length;
	for (i = 0; i < result->length; i++) {
		if (result->data[i] >= 'A' && result->data[i] <= 'Z') {
			result->data[i] += 32;
		}
	}
	free_str(str);
	return result;
}
Str *B2C_strRight(Str *str, int isString, int offset) {
	Str *result = malloc(sizeof(Str));
	exitIfNeg(offset);
	result->data = malloc(offset*2);
	if (offset > str->length) offset=str->length;
	result->length = offset;
	memcpy(result->data, str->data+(str->length-offset), offset*2);
	free_str(str);
	return result;
}
Str *B2C_strLeft(Str *str, int isString, int offset) {
	Str *result = malloc(sizeof(Str));
	exitIfNeg(offset);
	result->data = malloc(offset*2);
	if (offset > str->length) offset=str->length;
	result->length = offset;
	memcpy(result->data, str->data, offset*2);
	free_str(str);
	return result;
}
Str *B2C_strShift(Str *str, int isString, int offset) {
	unsigned short* spaces = malloc(2*abs(offset));
	Str *result = malloc(sizeof(Str));
	for (i = 0; i < abs(offset); i++) {
		spaces[i] = (unsigned short)' ';
	}
	result->length = str->length;
	result->data = malloc(str->length*2);
	if (offset > str->length) {
		offset = str->length;
	} else if (-offset > str->length) {
		offset = -str->length;
	}
	if (offset > 0) {
		memcpy(result->data, str->data+offset, (str->length-offset)*2);
		memcpy(result->data+(str->length-offset), spaces, offset*2);
	} else {
		memcpy(result->data, spaces, -offset*2);
		memcpy(result->data-offset, str->data, (str->length+offset)*2);
	}
	free_str(str);
	return result;
}
Str *B2C_strRotate(Str *str, int isString, int offset) {
	Str *result = malloc(sizeof(Str));
	result->length = str->length;
	result->data = malloc(str->length*2);
	offset %= str->length;
	if (offset > 0) {
		memcpy(result->data, str->data+offset, (str->length-offset)*2);
		memcpy(result->data+(str->length-offset), str->data, offset*2);
	} else {
		memcpy(result->data, str->data+(str->length+offset), -offset*2);
		memcpy(result->data-offset, str->data, (str->length+offset)*2);
	}
	free_str(str);
	return result;
}

//Debug function; is not used with converted programs
void printBCDvarBytes(BCDvar *var) {
	unsigned char str[2] = {0};
	for (i = 0; i < 18; i++) {
		if (i%2) {
			str[0] = '0'+((*var)[i/2]&0x0F);
		} else {
			str[0] = '0'+((*var)[i/2]>>4);
		}
		if (str[0] > '9') {
			str[0] += 'A'-'9'-1;
		}
		Print(str);
	}
}

void printChar(unsigned char c) {
	unsigned char str[2] = {0};
	str[0] = c;
	Print(str);
}