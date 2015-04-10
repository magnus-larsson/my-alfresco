// @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/people-finder/people-finder.get.js

/**
Varför?
Yes, VGR vill inte ha default-sök beteendet. Alltså söker man på "niklas ekman" i *standard* alfresco så söker den på "niklas or ekman”, de vill ha ”niklas and ekman”.
**/
model.widgets[0].options.dataWebScript = "vgr/people";
