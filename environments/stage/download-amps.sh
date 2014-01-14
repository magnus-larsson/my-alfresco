#!/bin/bash
# -------
# Script for downloading amp files
# -------

REPO_NAME="alfresco-vgr-repo"
SHARE_NAME="alfresco-vgr-share"

VERSION="2.1.1.1"

REPO_AMP="$REPO_NAME-$VERSION.amp"
SHARE_AMP="$SHARE_NAME-$VERSION.amp"

REPO_AMP_PATH="amps-repo"
SHARE_AMP_PATH="amps-share"

SERVER_URL="https://reda.redpill-linpro.com/alfresco/service/cmis"
SERVER_PATH="Sites/alfresco/documentLibrary/Clients/VGR/Installation"

REPO_DOWNLOAD_URL="$SERVER_URL/p/$SERVER_PATH/$REPO_AMP/content?a=true"
SHARE_DOWNLOAD_URL="$SERVER_URL/p/$SERVER_PATH/$SHARE_AMP/content?a=true"

echo "Username:"
read USERNAME
echo "Password:"
read -s PASSWORD

getHTTPCode () {
    echo $(curl --write-out %{http_code} --silent --user $USERNAME:$PASSWORD $1 -o "$2")
}

response=$(getHTTPCode $REPO_DOWNLOAD_URL $ALFRESCO_REPO_AMPS/$REPO_AMP)

if [ $response -ne 200 ]; then
   echo "Error while downloading the Repo AMP, error code $response"
   exit 1
fi

response=$(getHTTPCode $SHARE_DOWNLOAD_URL $ALFRESCO_SHARE_AMPS/$SHARE_AMP)

if [ $response -ne 200 ]; then
   echo "Error while downloading the Share AMP, error code $response"
   exit 1
fi
