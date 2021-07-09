#!/bin/bash
# *****************************************************************************
# Copyright 2021 Nail Sharipov (sharipovn@gmail.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# *****************************************************************************

OPTIONS1="./jpackage.options"
OPTIONS2="./jpackage.ubuntu.options"

# https://tldp.org/LDP/abs/html/comparison-ops.html
if [ $# -ne 2 ]
  then
    echo "           usage: pkg-ubuntu.sh [type] [path_to_jpackage]"
    echo "            type: app-image, rpm, deb"
    echo "path_to_jpackage: most commonly path to the bin folder of JDK 16+"
    exit
fi

if [ "$1" != "app-image" ] && [ "$1" != "rpm" ] && [ "$1" != "deb" ] 
  then
    echo "wrong type selected: $1"
    exit
fi

if [ -f "$2/jpackage" ]
  then
    echo "jpackage version:"
    $2/jpackage --version
  else
    echo jpackage does not exist at the location provided: $2
    exit
fi

# dpkg --list - to list the packages installed
# dpkg --list hammergenics
# sudo apt-get autoremove fakeroot - to remove fakeroot

if ! command -v fakeroot &> /dev/null
  then
    echo "fakeroot is not installed, trying to install"
    
   #Command 'fakeroot' not found, but can be installed with:
    sudo apt install fakeroot     # version 1.25.3-1.1ubuntu2, or
   #sudo apt install fakeroot-ng  # version 0.18-4.1
   #sudo apt install pseudo       # version 1.9.0+git20200626+067950b-2
fi

if ! command -v fakeroot
  then
    echo "there were problems with the fakeroot installation"
    exit
fi

# Error: java.io.IOException: Cannot run program "objcopy": error=2, No such file or directory
if ! command -v objcopy &> /dev/null
  then
   #Command 'objcopy' not found, but can be installed with:
    sudo apt install binutils
fi

if ! command -v objcopy
  then
    echo "there were problems with the binutils installation"
    exit
fi

if [ -d "./tmp" ] 
  then 
    echo "deleting temporary folder"
    rm -r ./tmp
fi

if [ -d "./jpackage" ] 
  then 
    echo "deleting output folder"
    rm -r ./jpackage
fi

$2/jpackage --type "$1" @"$OPTIONS1" @"$OPTIONS2"

