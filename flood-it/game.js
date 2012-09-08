var construct = function (isIn, iName, iColor, iAdjNodes) {
    var inVal = isIn, name = iName || '', color = iColor || '#ffffff', adjNodes = iAdjNodes || [];
    return {
        isIn : function () { return inVal; },
        setIn: function (isIn) { inVal = isIn; },
        getName : function () { return name; },
        setName : function (iName) { name = iName; },
        getColor: function () { return color; },
        setColor: function (iColor) { color = iColor; },
        getAdjNodes : function () { return adjNodes; },
        setAdjNodes : function (iAdjNodes) { adjNodes = iAdjNodes; },
        toString : function () { return "isIn=" + inVal + ", name=" + name + ", color=" + color + ", adjNodes=" + adjNodes; }
    };
};

var game = (function () {
    // defining private variables 
    var map = {}, boardSize = 15, nOfColors = 6, unTouched, steps, 
        colors = ["#000000", "#541E32", "#8E3557", "#88A33E", "#F27405", "#F7F2B2", "#203759", "#548094",
                  "#FAF1DF", "#57425C", "#3D3652", "#2C3432", "#A2D939", "#FFF829", "#C2BD86", "#DB3F02"],
        checkGame = function () {
            return unTouched === 0; 
        }, 
        updateSteps = function () {
            $("div.score").text(steps);
        }, 
        traverseBoard = function (parentNode, traverseMap, color, className) {
            var i, adjNodes;
            if (!traverseMap[parentNode.getName()]) {
                traverseMap[parentNode.getName()] = true;
                if (parentNode.isIn()) {
                    parentNode.setColor(color);
                    $("#"+parentNode.getName()).removeClass().addClass(className).addClass("box");
                    adjNodes = parentNode.getAdjNodes();
                    for (i = 0; i < adjNodes.length; i = i + 1) {
                        traverseBoard(adjNodes[i], traverseMap, color, className) ;
                    }
                }
                else {
                    if (parentNode.getColor() === color) {
                        unTouched = unTouched-1;
                        parentNode.setIn(true) ;
                        adjNodes = parentNode.getAdjNodes();                        
                        for (i = 0; i < adjNodes.length; i = i + 1) {
                            traverseBoard(adjNodes[i], traverseMap, color, className) ;
                        }
                    }
                }
            }
        }, 
        paintButtons = function () {
            var buttons = [], i;
            buttons.push("<ul>");
            for (i = 1 ; i <= nOfColors ; i = i + 1) {
                buttons.push("<li class=\"box rounded buttons box_"+i+"\" onclick=\"return game.play('"+colors[i]+"','box_"+i+"')\"> &nbsp;</li>");
            }
            buttons.push("</ul>");
            $("#button-wrapper").prepend(buttons.join(""));
        },
        newBoard = function () {
            var i, j, node, boardHTML = [], rand ;
            map = {};
            unTouched = boardSize*boardSize -1 ;
            steps = 26 ;
            updateSteps();
            for (i = 0 ;  i < boardSize ; i = i + 1) {
                boardHTML.push("<tr class=\"row\">");
                // making a board 
                for (j = 0 ; j < boardSize ; j = j + 1) {
                    node = construct();
                    rand = Math.ceil(((Math.random()*6 + 1)%6));
                    node.setColor(colors[rand]);
                    node.setName(i+"-"+j);
                    map[i+"-"+j]= node;
                    // adding the previous node in the top to the current node's adjeceny list and vice versa
                    if (map[(i-1)+"-"+j]){
                        node.getAdjNodes().push(map[(i-1)+"-"+j]);
                        map[(i-1)+"-"+j].getAdjNodes().push(node);
                    }
                    // adding the previous node in the left to the current node's adjeceny list and vice versa.
                    if (map[i+"-"+(j-1)]){
                        node.getAdjNodes().push(map[i+"-"+(j-1)]);
                        map[i+"-"+(j-1)].getAdjNodes().push(node);
                    }
                    boardHTML.push("<td id = \""+node.getName()+"\" class=\"box box_"+rand+"\"/>");
                }
                boardHTML.push("</tr>");
            }
            $("#board").html(boardHTML.join(""));
            // Setting the root as traversed. 
            play(map["0-0"].getColor(), $("#0-0").attr("class").split(" ")[1]);
            return map["0-0"];
        },
        play = function (color, className){
            steps = steps-1;
            var root = map["0-0"], traverseMap = {} ;
            root.setIn(true);
            // traverseMap[root.name] = true ;
            traverseBoard(root, traverseMap,  color, className);
            updateSteps();
            if (checkGame()) {
                alert("You win");
            }
            else if(steps === 0) {
                alert("You loose");
            }
        };
    // returning an object that will have public methods exposed. 
    return {
        paintButtons : paintButtons,
        newBoard : newBoard,
        play : play
    };
}());

$(function () {
    $("#input, input.submit, input:submit").button();
    game.newBoard();
    game.paintButtons();
});