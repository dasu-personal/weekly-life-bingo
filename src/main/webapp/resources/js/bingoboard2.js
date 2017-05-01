function submitSave() {
	var boardname = document.getElementById("boardnameSave").value;
	var password = document.getElementById("passwordSave").value;
	// Returns successful data submission message when the entered information
	// is stored in database.
	var xhttp = new XMLHttpRequest();

	xhttp.open('POST', 'bingosaveboard');
	xhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
	xhttp.onreadystatechange = function() {
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			parseBoardModelXml(xhttp);

			closeSaveBoard();
		}
	};
	xhttp.send(encodeURI('boardname=' + boardname + '&password=' + password));
}

function submitLoad() {
	var boardname = document.getElementById("boardnameLoad").value;
	var password = document.getElementById("passwordLoad").value;
	// Returns successful data submission message when the entered information
	// is stored in database.
	var xhttp = new XMLHttpRequest();

	xhttp.open('POST', 'bingoloadboard');
	xhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
	xhttp.onreadystatechange = function() {
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			parseBoardModelXml(xhttp);
			closeLoadBoard();
		}
	};
	xhttp.send(encodeURI('boardname=' + boardname + '&password=' + password));
}

function openEditSquare(squareIndex) {
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() { 
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			prepareEditSquare(xhttp, squareIndex);
		    document.getElementById( "editSquare" ).style.width = "100%";
		}
	};
	xhttp.open("GET", "bingogetsquare?index="+squareIndex, true);
	xhttp.send();
}

function prepareEditSquare(http, squareIndex) {
	document.getElementById("squareDescription").innerHTML = http.responseXML.getElementsByTagName("description")[0].childNodes[0].nodeValue;
	
	document.getElementById( "fillButton" ).setAttribute( "onClick", "javascript: changeSquareType("+squareIndex+",'FILLED');" );
	document.getElementById( "partialButton" ).setAttribute( "onClick", "javascript: changeSquareType("+squareIndex+",'PARTIAL');" );
	document.getElementById( "emptyButton" ).setAttribute( "onClick", "javascript: changeSquareType("+squareIndex+",'EMPTY');" );

}

function openSaveBoard() {
	document.getElementById("saveOperation").style.width = "100%";
}

function closeSaveBoard() {

	document.getElementById("saveOperation").style.width = "0%";
	document.getElementById("boardnameSave").value;
	document.getElementById("passwordSave").value;
}

function openLoadBoard() {
	document.getElementById("loadOperation").style.width = "100%";
}
function closeLoadBoard() {

	document.getElementById("loadOperation").style.width = "0%";
	document.getElementById("boardnameLoad").value;
	document.getElementById("passwordLoad").value;
}

function closeEditSquare() {
    document.getElementById("editSquare").style.width = "0%";
}
function openFileBoard() {
	document.getElementById("fileBoard").style.width = "100%";
}
function closeFileBoard() {
	document.getElementById("fileBoard").style.width = "0%";
}
function openGameNotification() {
	document.getElementById("gameNotification").style.width = "100%";
}
function closeGameNotification() {
	document.getElementById("gameNotification").style.width = "0%";
}

function changeSquareType(squareIndex, markerType) {
	// document.getElementById('circlemark'+squareIndex).className = 'marker'+markerType;
	// in practice we will want to check if changing the square causes a bingo
	
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() { 
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			parseBoardModelXml(xhttp);
			closeEditSquare();
		}
	};
	xhttp.open("GET", "bingoupdatesquare?index="+squareIndex+"&squarestatus="+markerType, true);
	xhttp.send();

}

function parseBoardModelXml(xml) {
	var xmlDoc = xml.responseXML;
	squares = xmlDoc.getElementsByTagName("square");
	for (var i = 0; i < squares.length; i++) {
		var square = squares[i];
		var title = square.getElementsByTagName("title");
		
		document.getElementById("squaretitle"+i).innerHTML = square.getElementsByTagName("title")[0].childNodes[0].nodeValue;
		document.getElementById("circlemark"+i).className = "marker" + square.getElementsByTagName("status")[0].childNodes[0].nodeValue;
	}
	var announce = xmlDoc.getElementsByTagName("announce");
	if (announce.length == 1) {
		document.getElementById("overlay-message").innerHTML = announce[0].childNodes[0].nodeValue;
		openGameNotification();
	}
	
}

function generateNewBoard() {
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() { 
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			parseBoardModelXml(xhttp);
		}
	};
	xhttp.open("GET", "bingogeneratenewboard", true);
	xhttp.send();
		  
}

function clearAllMarkers() {
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() { 
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			parseBoardModelXml(xhttp);
		}
	};
	xhttp.open("GET", "bingoclearmarkers", true);
	xhttp.send();
}

