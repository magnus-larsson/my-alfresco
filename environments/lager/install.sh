#!/bin/sh
# -------
# Script for installation
# -------
TOMCAT_HOME_REPO=/var/lib/tomcat6

ORIGINALS_DIR=/opt/alfresco
BACKUP_DIR=/opt/alfresco/backup

REPO_AMPS=./amps

echo "Stopping Tomcat . . ."
/etc/init.d/tomcat6 stop
echo ""

echo "Move the alfresco directory and war to the backup directory . . ."
DIRNAME=$(date +%Y%m%d%H%M)
mkdir -p $BACKUP_DIR/$DIRNAME
mv $TOMCAT_HOME_REPO/webapps/alfresco $BACKUP_DIR/$DIRNAME
mv $TOMCAT_HOME_REPO/webapps/alfresco.war $BACKUP_DIR/$DIRNAME
echo ""

echo "Copying fresh alfresco.war to Tomcat . . ."
cp $ORIGINALS_DIR/alfresco.war $TOMCAT_HOME_REPO/webapps
echo ""

echo "Applying amps to Alfresco . . ."
java -jar alfresco-mmt.jar install $REPO_AMPS $TOMCAT_HOME_REPO/webapps/alfresco.war -directory -force -nobackup
java -jar alfresco-mmt.jar list $TOMCAT_HOME_REPO/webapps/alfresco.war
echo ""

echo "Cleaning temporary Alfresco files from Tomcat . . ."
rm -rf $TOMCAT_HOME_REPO/work/Catalina/localhost/alfresco
echo ""

echo "Setting ownership to the tomcat directories to the tomcat user . . ."
chown -R tomcat6:tomcat6 $TOMCAT_HOME_REPO
echo ""

echo "Starting Tomcat . . ."
/etc/init.d/tomcat6 start
echo ""

echo "Tailing log, waiting for startup of Alfresco . . ."
echo ""
tail -f $TOMCAT_HOME_REPO/logs/catalina.out
