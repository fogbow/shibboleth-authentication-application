#!/bin/bash
DIR_PATH=$(pwd)

CONTAINER_BASE_PATH="/root/shibboleth-authentication-application"
# files reference in the container
CONTAINER_FILES_DIR_PATH=$CONTAINER_BASE_PATH"/files"

SHIB_AUTH_APP_CONF_FILE="shibboleth-authentication-application.conf"
SHIB_AUTH_APP_DIR="services/shibboleth-authentication-application"
SERVICES_FILE="services.conf"

BASE_DIR="services/shibboleth-authentication-application"
CONF_FILES_DIR=$DIR_PATH/"conf-files"
GENERAL_CONF_FILE_PATH=$CONF_FILES_DIR/"general.conf"
SERVICE_PROVIDER_CONF_DIR=$CONF_FILES_DIR"/service-provider-confs"

BASE_CONF_FILES_DIR=$DIR_PATH/$BASE_DIR/"conf-files"
mkdir -p $BASE_CONF_FILES_DIR

SHIB_HTTP_PORT_PATTERN="shib_http_port"
SHIB_HTTP_PORT=$(grep $SHIB_HTTP_PORT_PATTERN $GENERAL_CONF_FILE_PATH | awk -F "=" '{print $2}')

# --------------- Service Provider Configuration ---------------#
GENERAL_SERVICE_PROVIDER_RNP_CERTIFICATE_PATTERN="service_provider_rnp_certificate"
GENERAL_SERVICE_PROVIDER_RNP_KEY_PATTERN="service_provider_rnp_key"
GENERAL_SERVICE_PROVIDER_DOMAIN_PATTERN="service_provider_domain"
GENERAL_DESCOVERY_SERVICE_URL_PATTERN="descovery_service_url"
GENERAL_DESCOVERY_SERVICE_METADATA_PATTERN="descovery_service_metadata_url"

GENERAL_DESCOVERY_SERVICE_URL_VALUE=$(grep $GENERAL_DESCOVERY_SERVICE_URL_PATTERN $GENERAL_CONF_FILE_PATH | awk -F "=" '{print $2}')
GENERAL_SERVICE_PROVIDER_DOMAIN_VALUE=$(grep $GENERAL_SERVICE_PROVIDER_DOMAIN_PATTERN $GENERAL_CONF_FILE_PATH | awk -F "=" '{print $2}')
GENERAL_DESCOVERY_SERVICE_METADATA_VALUE=$(grep $GENERAL_DESCOVERY_SERVICE_METADATA_PATTERN $GENERAL_CONF_FILE_PATH | awk -F "=" '{print $2}')

# Moving files to deployment directory
## Moving Service Provider certificate and key
DEFAULT_SERVICE_PROVIDER_RNP_CERTIFICATE_NAME=$GENERAL_SERVICE_PROVIDER_DOMAIN_VALUE".crt"
DEFAULT_SERVICE_PROVIDER_RNP_KEY_NAME=$GENERAL_SERVICE_PROVIDER_DOMAIN_VALUE".key"
SERVICE_PROVIDER_RNP_CERTIFICATE_PATH=$(grep $GENERAL_SERVICE_PROVIDER_RNP_CERTIFICATE_PATTERN $GENERAL_CONF_FILE_PATH | awk -F "=" '{print $2}')
SERVICE_PROVIDER_RNP_KEY_PATH=$(grep $GENERAL_SERVICE_PROVIDER_RNP_KEY_PATTERN $GENERAL_CONF_FILE_PATH | awk -F "=" '{print $2}')
yes | cp -f $SERVICE_PROVIDER_RNP_CERTIFICATE_PATH $BASE_CONF_FILES_DIR"/"$DEFAULT_SERVICE_PROVIDER_RNP_CERTIFICATE_NAME
yes | cp -f $SERVICE_PROVIDER_RNP_KEY_PATH $BASE_CONF_FILES_DIR"/"$DEFAULT_SERVICE_PROVIDER_RNP_KEY_NAME

DEFAULT_SP_FILE_CONF_NAME="default.conf"
SHIBBOLETH_SP2_SP_FILE_CONF_NAME="shibboleth-sp2.conf"
SHIBBOLETH_XML_SP_FILE_CONF_NAME="shibboleth2.xml"

## Moving SP files deploy
yes | cp -f $GENERAL_CONF_FILE_PATH $BASE_CONF_FILES_DIR"/"
yes | cp -f $SERVICE_PROVIDER_CONF_DIR"/attribute-map.xml" $BASE_CONF_FILES_DIR"/"
yes | cp -f $SERVICE_PROVIDER_CONF_DIR"/attribute-policy.xml" $BASE_CONF_FILES_DIR"/"
yes | cp -f $SERVICE_PROVIDER_CONF_DIR"/"$DEFAULT_SP_FILE_CONF_NAME $BASE_CONF_FILES_DIR"/"
yes | cp -f $SERVICE_PROVIDER_CONF_DIR"/"$SHIBBOLETH_SP2_SP_FILE_CONF_NAME $BASE_CONF_FILES_DIR"/"
yes | cp -f $SERVICE_PROVIDER_CONF_DIR"/"$SHIBBOLETH_XML_SP_FILE_CONF_NAME $BASE_CONF_FILES_DIR"/"

SERVICE_PROVIDER_DOMAIN_REPLACE="@SERVICE_PROVIDER_DOMAIN@"
SERVICE_PROVIDER_CERTIFICATE_PATH_REPLACE="@SERVICE_PROVIDER_CERTIFICATE_PATH@"
SERVICE_PROVIDER_KEY_PATH_REPLACE="@SERVICE_PROVIDER_KEY_PATH@"
SHIBBOLETH_AUTHENTICATION_APPLICATION_PORT_REPLACE="@SHIBBOLETH_AUTHENTICATION_APPLICATION_PORT@"
DESCOVERY_SERVICE_REPLACE="@DESCOVERY_SERVICE@"
DESCOVERY_SERVICE_METADATA_REPLACE="@DESCOVERY_SERVICE_METADATA@"

# Replacing values
## Replacing default.conf
sed -i "s/$SERVICE_PROVIDER_DOMAIN_REPLACE/$GENERAL_SERVICE_PROVIDER_DOMAIN_VALUE/g" $BASE_CONF_FILES_DIR"/"$DEFAULT_SP_FILE_CONF_NAME
## Replacing shibboleth-sp2.conf
sed -i "s#$SERVICE_PROVIDER_DOMAIN_REPLACE#$GENERAL_SERVICE_PROVIDER_DOMAIN_VALUE#g" $BASE_CONF_FILES_DIR"/"$SHIBBOLETH_SP2_SP_FILE_CONF_NAME 
sed -i "s#$SERVICE_PROVIDER_CERTIFICATE_PATH_REPLACE#/etc/ssl/certs/$DEFAULT_SERVICE_PROVIDER_RNP_CERTIFICATE_NAME#g" $BASE_CONF_FILES_DIR"/"$SHIBBOLETH_SP2_SP_FILE_CONF_NAME 
sed -i "s#$SERVICE_PROVIDER_KEY_PATH_REPLACE#/etc/ssl/private/$DEFAULT_SERVICE_PROVIDER_RNP_KEY_NAME#g" $BASE_CONF_FILES_DIR"/"$SHIBBOLETH_SP2_SP_FILE_CONF_NAME 
sed -i "s#$SHIBBOLETH_AUTHENTICATION_APPLICATION_PORT_REPLACE#$SHIB_HTTP_PORT#g" $BASE_CONF_FILES_DIR"/"$SHIBBOLETH_SP2_SP_FILE_CONF_NAME 
## Replacing shibboleth2.xml
sed -i "s#$SERVICE_PROVIDER_DOMAIN_REPLACE#$GENERAL_SERVICE_PROVIDER_DOMAIN_VALUE#g" $BASE_CONF_FILES_DIR"/"$SHIBBOLETH_XML_SP_FILE_CONF_NAME 
sed -i "s#$SERVICE_PROVIDER_CERTIFICATE_PATH_REPLACE#/etc/ssl/certs/$DEFAULT_SERVICE_PROVIDER_RNP_CERTIFICATE_NAME#g" $BASE_CONF_FILES_DIR"/"$SHIBBOLETH_XML_SP_FILE_CONF_NAME 
sed -i "s#$SERVICE_PROVIDER_KEY_PATH_REPLACE#/etc/ssl/private/$DEFAULT_SERVICE_PROVIDER_RNP_KEY_NAME#g" $BASE_CONF_FILES_DIR"/"$SHIBBOLETH_XML_SP_FILE_CONF_NAME
sed -i "s#$DESCOVERY_SERVICE_REPLACE#$GENERAL_DESCOVERY_SERVICE_URL_VALUE#g" $BASE_CONF_FILES_DIR"/"$SHIBBOLETH_XML_SP_FILE_CONF_NAME 
sed -i "s#$DESCOVERY_SERVICE_METADATA_REPLACE#$GENERAL_DESCOVERY_SERVICE_METADATA_VALUE#g" $BASE_CONF_FILES_DIR"/"$SHIBBOLETH_XML_SP_FILE_CONF_NAME 

# --------------- Shibboleth-Authenticatio-Application ---------------#

# Moving keys
GENERAL_SHIB_PRIVATE_KEY_PATTERN="ship_private_key_path"
GENERAL_AS_PUBLIC_KEY_PATTERN="as_public_key_path"

SHIB_PRIVATE_KEY_NAME="/shib_private.key"
AS_PUBLIC_KEY_NAME="/as_private.key"

# Moving files to deployment directory
GENERAL_SHIB_PRIVATE_KEY_PATH=$(grep $GENERAL_SHIB_PRIVATE_KEY_PATTERN $GENERAL_CONF_FILE_PATH | awk -F "=" '{print $2}')
GENERAL_AS_PUBLIC_KEY_PATH=$(grep $GENERAL_AS_PUBLIC_KEY_PATTERN $GENERAL_CONF_FILE_PATH | awk -F "=" '{print $2}')
yes | cp -f $GENERAL_SHIB_PRIVATE_KEY_PATH $BASE_CONF_FILES_DIR$SHIB_PRIVATE_KEY_NAME
yes | cp -f $GENERAL_AS_PUBLIC_KEY_PATH $BASE_CONF_FILES_DIR$AS_PUBLIC_KEY_NAME

# Moving conf file to deployment directory
yes | cp -f $CONF_FILES_DIR/$SHIB_AUTH_APP_CONF_FILE ./$SHIB_AUTH_APP_DIR/$SHIB_AUTH_APP_CONF_FILE

yes | cp -f $CONF_FILES_DIR/$SERVICES_FILE $SHIB_AUTH_APP_DIR/$SERVICES_FILE

# setting shibb auth app file properties
sed -i "s#.*$SHIB_HTTP_PORT_PATTERN=.*#$SHIB_HTTP_PORT_PATTERN=$SHIB_HTTP_PORT#" $SHIB_AUTH_APP_DIR/$SHIB_AUTH_APP_CONF_FILE

FOGBOW_GUI_URL_PATTERN="fogbow_gui_url"
FOGBOW_GUI_URL=$(grep $FOGBOW_GUI_URL_PATTERN $GENERAL_CONF_FILE_PATH | awk -F "=" '{print $2}')
sed -i "s#.*$FOGBOW_GUI_URL_PATTERN=.*#$FOGBOW_GUI_URL_PATTERN=$FOGBOW_GUI_URL#" $SHIB_AUTH_APP_DIR/$SHIB_AUTH_APP_CONF_FILE

SHIB_PRIVATE_KEY_PATTERN="ship_private_key_path"
SHIB_PRIVATE_KEY=$CONTAINER_FILES_DIR_PATH$SHIB_PRIVATE_KEY_NAME
sed -i "s#.*$SHIB_PRIVATE_KEY_PATTERN=.*#$SHIB_PRIVATE_KEY_PATTERN=$SHIB_PRIVATE_KEY#" $SHIB_AUTH_APP_DIR/$SHIB_AUTH_APP_CONF_FILE

AS_PUBLIC_KEY_PATTERN="as_public_key_path"
AS_PUBLIC_KEY=$CONTAINER_FILES_DIR_PATH$AS_PUBLIC_KEY_NAME
sed -i "s#.*$AS_PUBLIC_KEY_PATTERN=.*#$AS_PUBLIC_KEY_PATTERN=$AS_PUBLIC_KEY#" $SHIB_AUTH_APP_DIR/$SHIB_AUTH_APP_CONF_FILE

SERVICE_PROVIDER_MACHINE_IP_PATTERN="service_provider_machine_ip"
SERVICE_PROVIDER_MACHINE_IP=$(grep $SERVICE_PROVIDER_MACHINE_IP_PATTERN $GENERAL_CONF_FILE_PATH | awk -F "=" '{print $2}')
sed -i "s#.*$SERVICE_PROVIDER_MACHINE_IP_PATTERN=.*#$SERVICE_PROVIDER_MACHINE_IP_PATTERN=$FOGBOW_GUI_URL#" $SHIB_AUTH_APP_DIR/$SHIB_AUTH_APP_CONF_FILE