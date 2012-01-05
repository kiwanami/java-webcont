function decodeURL(str){
	var s0, i, j, s, ss, u, n, f;
	s0 = ""; // decoded str
	for (i = 0; i < str.length; i++){ // scan the source str
		s = str.charAt(i);
		if (s == "+"){s0 += " ";} // "+" should be changed to SP
		else {
			if (s != "%"){s0 += s;} // add an unescaped char
			else{ // escape sequence decoding
				u = 0; // unicode of the character
				f = 1; // escape flag, zero means end of this sequence
				while (true) {
					ss = ""; // local str to parse as int
					for (j = 0; j < 2; j++ ) { // get two maximum hex characters for parse
						sss = str.charAt(++i);
						if (((sss >= "0") && (sss <= "9")) || ((sss >= "a") && (sss <= "f")) || ((sss >= "A") && (sss <= "F"))) {
							ss += sss; // if hex, add the hex character
						} else {--i; break;} // not a hex char., exit the loop
					}
					n = parseInt(ss, 16); // parse the hex str as byte
					if (n <= 0x7f){u = n; f = 1;} // single byte format
					if ((n >= 0xc0) && (n <= 0xdf)){u = n & 0x1f; f = 2;} // double byte format
					if ((n >= 0xe0) && (n <= 0xef)){u = n & 0x0f; f = 3;} // triple byte format
					if ((n >= 0xf0) && (n <= 0xf7)){u = n & 0x07; f = 4;} // quaternary byte format (extended)
					if ((n >= 0x80) && (n <= 0xbf)){u = (u << 6) + (n & 0x3f); --f;} // not a first, shift and add 6 lower bits
					if (f <= 1){break;} // end of the utf byte sequence
					if (str.charAt(i + 1) == "%"){ i++ ;} // test for the next shift byte
					else {break;} // abnormal, format error
				}
				s0 += String.fromCharCode(u); // add the escaped character
			}
		}
	}
	return s0;
}
