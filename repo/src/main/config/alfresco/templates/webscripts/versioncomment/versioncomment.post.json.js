
try {
    var nodeRef = json.get("nodeRef");
    var comment = json.get("comment");
    
    var node = search.findNode(nodeRef);
    var v = node.versionHistory[0]; //take the latest
    var ref = v.getNode().nodeRef;  //get the node

    //find it again but as workspace node, this let's us edit properties
    ref = (ref+"").replace('versionStore://','workspace://'); 
    var vn = search.findNode(ref);

    //set comment and save properties
    vn.properties['{http://www.alfresco.org/model/versionstore/2.0}versionDescription'] = comment;
    vn.save();

    model.result = "Ok";
    
} catch (e) {
    status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, e.toString());
    model.result = e.toString();
}


