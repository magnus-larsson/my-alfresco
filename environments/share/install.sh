#!/bin/sh
# -------
# Script for installation
# -------
TOMCAT_HOME_SHARE=/var/lib/tomcat6

ORIGINALS_DIR=/opt/alfresco
BACKUP_DIR=/opt/alfresco/backup

SHARE_AMPS=./amps

echo "Stopping Tomcat . . ."
/etc/init.d/tomcat6 stop
echo ""

echo "Move the share directory and war to the backup directory . . ."
DIRNAME=$(date +%Y%m%d%H%M)
mkdir -p $BACKUP_DIR/$DIRNAME
mv $TOMCAT_HOME_SHARE/webapps/share $BACKUP_DIR/$DIRNAME
mv $TOMCAT_HOME_SHARE/webapps/share.war $BACKUP_DIR/$DIRNAME
echo ""

echo "Copying fresh share.war and share.war to Tomcat . . ."
cp $ORIGINALS_DIR/share.war $TOMCAT_HOME_SHARE/webapps
echo ""

echo "Applying amps to Share . . ."
java -jar alfresco-mmt.jar install $SHARE_AMPS $TOMCAT_HOME_SHARE/webapps/share.war -directory -force -nobackup
java -jar alfresco-mmt.jar list $TOMCAT_HOME_SHARE/webapps/share.war
echo ""

echo "Cleaning temporary Share files from Tomcat . . ."
rm -rf $TOMCAT_HOME_SHARE/work/Catalina/localhost/share
echo ""

echo "Setting ownership to the tomcat directories to the tomcat user . . ."
chown -R tomcat6:tomcat6 $TOMCAT_HOME_SHARE
echo ""

echo "Starting Tomcat . . ."
/etc/init.d/tomcat6 start
echo ""

echo "Tailing log, waiting for startup of Share . . ."
echo ""
tail -f $TOMCAT_HOME_SHARE/logs/catalina.out
