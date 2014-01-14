#!/bin/bash
# -------
# Script for setting the correct environment variables during installation
# -------
export ALFRESCO_TOMCAT_HOME_REPO="/var/lib/tomcat6"
export ALFRESCO_TOMCAT_HOME_SHARE="/var/lib/tomcat6"

export ALFRESCO_ORIGINALS_DIR="/opt/alfresco"
export ALFRESCO_BACKUP_DIR="/opt/alfresco/backup"

export ALFRESCO_REPO_AMPS="amps-repo"
export ALFRESCO_SHARE_AMPS="amps-share"

export ALFRESCO_MMT="alfresco-mmt.jar"

export ALFRESCO_TOMCAT_USER="tomcat6"
export ALFRESCO_TOMCAT_GROUP="tomcat6"

export ALFRESCO_TOMCAT_START_SCRIPT="/etc/init.d/tomcat6 start"
export ALFRESCO_TOMCAT_STOP_SCRIPT="/etc/init.d/tomcat6 stop"

export CLAMAV_LOG_DIRECTORY="/var/log/clamav"
export CLAMAV_DAEMON_RESTART_SCRIPT="/etc/init.d/clamav-daemon restart"