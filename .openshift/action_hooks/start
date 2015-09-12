#!/bin/bash

cd $OPENSHIFT_REPO_DIR

if [[ -z "$OPENSHIFT_INTERNAL_IP" ]]; then export OPENSHIFT_INTERNAL_IP="$OPENSHIFT_DIY_IP"; fi
if [[ -z "$OPENSHIFT_INTERNAL_PORT" ]]; then export OPENSHIFT_INTERNAL_PORT="$OPENSHIFT_DIY_PORT"; fi

nohup java -jar target/httpnettyserver-1.0-SNAPSHOT.jar > ${OPENSHIFT_DIY_LOG_DIR}/trynetty.log 2 > &1 &