var scriptFailed = false;

// Check the arguments
if (behaviour.args == null) {
	scriptFailed = true;
} else {
	if (behaviour.args.length == 1) {
		var site = behaviour.args[0].child;
		site.addAspect("vgr:cleanable");
		site.properties["vgr:startDate"] = new Date();
		site.save();
	} else {
		scriptFailed = true;
	}
}