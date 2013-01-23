// Get the args
var refs = args["refs"];

//TODO: there must be a better and faster way to do this!
if (refs && refs != "") {
    refs = refs.split(',');
    var ppl = [];
    for each (var ref in refs) {
        var node = search.findNode(ref);
        if (node) {
            ppl.push(node);
        }
    }
    model.peoplelist = ppl;
} else {
    model.peoplelist = [];
}
