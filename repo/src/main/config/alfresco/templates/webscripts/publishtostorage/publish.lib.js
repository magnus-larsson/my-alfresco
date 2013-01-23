<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/published.lib.js">

/*
 * Common functions for publishing/unpublishing
 *
 */

var Publish = {
        required_properties: [
           "{http://www.vgregion.se/model/1.0}dc.type.record"
        ],
        validfrom_prop: "{http://www.vgregion.se/model/1.0}dc.date.availablefrom",
        validto_prop: "{http://www.vgregion.se/model/1.0}dc.date.availableto",
        publisher_forunit_prop: "{http://www.vgregion.se/model/1.0}dc.publisher.forunit",
        publisher_project_prop: "{http://www.vgregion.se/model/1.0}dc.publisher.project-assignment",
        
		isFolder: function (n) {
			return n.type == '{http://www.alfresco.org/model/content/1.0}folder';
		},

		isDocument: function (n) {
		    return n.type == '{http://www.vgregion.se/model/1.0}document';
		},
		
		_validate_required: function (n) {
            
          for (var i=0; i<Publish.required_properties.length; i++) {
            var prop = n.properties[Publish.required_properties[i]];
            if (!prop || prop == "") {
                return "validation.property.required";
            }
          }
          return true;
        }, 
		
		_validate_dates: function (n) {
            var validto   = n.properties[Publish.validto_prop]; 
            var validfrom = n.properties[Publish.validfrom_prop];
            var now = new Date();
            //validto is in the past, this will not fly
            if (validto && validto.getTime() < now.getTime()) {
                return "validation.validto.past";
            }    
            
            //validto must be after validfrom
            if (validto && validfrom && validto.getTime() < validfrom.getTime()) {
                return "validation.validto.before.validfrom";
            }    

            return true;
        },
        
        _validate_publisher: function (n) {
              var forunit   = n.properties[Publish.publisher_forunit_prop]; 
              var project   = n.properties[Publish.publisher_project_prop];
              logger.log("_validate_publisher");
              logger.log(forunit === "");
              logger.log(project);
              if ((forunit && forunit != "") || (project && project != "")) {
                return true;
              }
              return "validation.publisher.missing";
        },

		//recurses tree applying validation methods
		//and extracting data 
		recurse: function (node,validators,extractor) {
            //state variables
            var statistics = { total: 0, folders: 0, documents: 0, errors: 0};

            //internal recursive function
            var _recurse = function (node,validators,extractor) {
            	var valid = true;
            	if (Publish.isDocument(node)) {
		            statistics.documents += 1;
		            
			        //validators run
		            var error_msgs = [];
			        for (var i=0; i<validators.length; i++) {
			        	var v = validators[i](node);
			        	if (v !== true) {
			        		valid = false;
			        		error_msgs.push(v);
			        	}
			        }
		            
		        } else if (Publish.isFolder(node)) {
		            statistics.folders += 1;
		        }
		        statistics.total += 1;
		
		        if (!valid) {
	                statistics.errors += 1;
	            }
		        
		        //extract an obj to return
                var obj = extractor(node,valid,error_msgs);		        
		        //recurse children
		        if (!node.children || node.children.length == 0) {
		            return obj;
		        }
		        
		        obj.children = [];
		        for each (child in node.children) {
		            if (Publish.isFolder(child) || Publish.isDocument(child)) {
		                obj.children.push(_recurse(child,validators,extractor));
		            }
		        }
		        
		        return obj;    
            };
            
            var root = _recurse(node,validators,extractor);
            return { statistics:statistics, node:root };
		},
		
		
	    publishable: function (node) {
            var validators = [Publish._validate_required,Publish._validate_dates,Publish. _validate_publisher];
                       
            return Publish.recurse(node,validators,Publish._std_extractor);
        },
	
	    revokable: function (node) {
            var validators = []; //no validators
                       
            return Publish.recurse(node,validators,Publish._std_extractor);
        
	    },
	    
	      
		
		_std_extractor: function (node,valid,error_msgs) {
		        
                var pb = Published.isPublished(node);
                var validto   = node.properties[Publish.validto_prop]; 
                var validfrom = node.properties[Publish.validfrom_prop];
                validto = validto?validto.getTime():null;
                validfrom = validfrom?validfrom.getTime():null;
                
                return obj = {
                        "name": node.name,
                        ref: node.nodeRef,
                        published: pb.published,
                        hasbeen: pb.hasbeen,
                        publishedold: pb.publishedold,
                        type: node.type,
                        validated: valid,
                        isFolder: Publish.isFolder(node),
                        valid_from: validfrom,
                        valid_to: validto,
                        error_msgs: error_msgs
                
                };
        },
			
		processJSON: function (json,recursive_func) {
			var nodes = [];
			var statistics = { total: 0, folders: 0, documents: 0, errors: 0};
			if (json.has("nodes")) {
			    
			    var jsonnodes = json.get("nodes");
			    var sites     = json.get("sites");
                
                if (sites) {
			        for (var i=0; i<sites.length(); i++) {
			            var doclib = search.query({
			                 query: 'PATH:"app:company_home/st:sites/cm:'+sites.getString(i)+'/cm:documentLibrary"',
                             language: "fts-alfresco"
			            });
			            if (doclib && doclib.length > 0) {
			                //there should really only be one
			                var res = recursive_func(doclib[0]);
			                nodes.push(res.node);
			                statistics.total     += res.statistics.total;
       			            statistics.folders   += res.statistics.folders;
       			            statistics.documents += res.statistics.documents;
       			            statistics.errors    += res.statistics.errors; 
			            }
			        }
			    }
			    
			    if (jsonnodes) {
			        for (var i=0; i< jsonnodes.length(); i++) {
			            var node = search.findNode(jsonnodes.getString(i));
			            
			            if (node != null && (Publish.isDocument(node) || Publish.isFolder(node))) {       
			                var res = recursive_func(node);
			                nodes.push(res.node);
			                statistics.total     += res.statistics.total;
       			            statistics.folders   += res.statistics.folders;
       			            statistics.documents += res.statistics.documents;
       			            statistics.errors    += res.statistics.errors;
			            }
			        }
			    }
			}
			return { nodes:nodes, statistics:statistics };
		},
		
		processJSONPublishable: function (json) {
		    return Publish.processJSON(json,Publish.publishable);
		},
		
		processJSONRevokable: function (json) {
		    return Publish.processJSON(json,Publish.revokable);
		}
		
};

