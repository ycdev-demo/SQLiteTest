#!/bin/sh

kernel_name=`uname -s`
IS_MAC_OS=false
if [ $kernel_name = "Darwin" ]; then
    IS_MAC_OS=true
fi

if ! $IS_MAC_OS; then # for Linux only
    alias greadlink="readlink"
fi

CMD_REAL_PATH=`greadlink -e "$0"`
if [ $? -ne 0 ]; then
    CMD_REAL_PATH="$0"
fi

CMD_DIR=`dirname "$CMD_REAL_PATH"`
SRC_DIR=$CMD_DIR/src
BIN_DIR=$CMD_DIR/bin

mkdir -p $BIN_DIR
javac -classpath json-20210307.jar $SRC_DIR/*.java -d $BIN_DIR && java -classpath json-20210307.jar:$BIN_DIR AmazonReviewsParser $@
rm -rf $BIN_DIR
