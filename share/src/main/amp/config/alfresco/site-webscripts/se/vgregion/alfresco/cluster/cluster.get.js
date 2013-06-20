var backend = remote.call("/vgr/probe");

var frontend = config.global['probe-host'] != null ? config.global['probe-host'].value : "localhost";

model.backend = backend;
model.frontend = frontend;