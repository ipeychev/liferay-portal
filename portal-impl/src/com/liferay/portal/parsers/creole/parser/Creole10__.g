lexer grammar Creole10;
options {
  language=Java;

}
@header {
/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

 package com.liferay.portal.parsers.creole.parser;
}

T44 : ':' ;
T45 : 'C' ;
T46 : '2' ;
T47 : 'D' ;
T48 : 'o' ;
T49 : 'k' ;
T50 : 'u' ;
T51 : 'W' ;
T52 : 'i' ;
T53 : 'F' ;
T54 : 'l' ;
T55 : 'c' ;
T56 : 'r' ;
T57 : 'G' ;
T58 : 'g' ;
T59 : 'e' ;
T60 : 'J' ;
T61 : 'S' ;
T62 : 'P' ;
T63 : 'M' ;
T64 : 'a' ;
T65 : 't' ;
T66 : 'b' ;
T67 : 'd' ;
T68 : 'n' ;
T69 : 'O' ;
T70 : 'm' ;
T71 : 's' ;
T72 : 'h' ;
T73 : 'p' ;
T74 : 'R' ;
T75 : 'x' ;
T76 : 'T' ;
T77 : 'y' ;
T78 : 'U' ;
T79 : 'X' ;

// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1103
ESCAPE					: '~';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1104
NOWIKI_BLOCK_CLOSE		: NEWLINE  '}}}';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1105
NEWLINE					: ( CR )?  LF
						| CR;
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1107
fragment CR				: '\r';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1108
fragment LF				: '\n';

// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1110
BLANKS					: ( SPACE | TABULATOR )+;
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1111
fragment SPACE			: ' ';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1112
fragment TABULATOR		: '\t';

// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1114
BRACE_CLOSE				: NEWLINE '}';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1115
COLON_SLASH				: ':'  '/';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1116
ITAL					: '//';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1117
NOWIKI_OPEN				: '{{{';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1118
NOWIKI_CLOSE			: '}}}';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1119
LINK_OPEN				: '[[';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1120
LINK_CLOSE				: ']]';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1121
IMAGE_OPEN				: '{{';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1122
IMAGE_CLOSE				: '}}';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1123
FORCED_LINEBREAK		: '\\\\';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1124
EQUAL					: '=';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1125
PIPE					: '|';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1126
POUND					: '#';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1127
DASH					: '-';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1128
STAR					: '*';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1129
SLASH					: '/';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1130
EXTENSION				: '@@';
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1131
TABLE_OF_CONTENTS_OPEN_MARKUP
	:	'<<'
	;
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1134
TABLE_OF_CONTENTS_CLOSE_MARKUP
	:	'>>'
	;
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1137
TABLE_OF_CONTENTS_TEXT
	:	'<<TableOfContents>>'
	;
// $ANTLR src "/home/migue/development/sourcecode/liferay/liferay-portal/portal/portal-impl/src/com/liferay/portal/parsers/creole/grammar/Creole10.g" 1140
INSIGNIFICANT_CHAR		: .;