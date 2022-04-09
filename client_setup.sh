#!/bin/bash

# Download CMake
wget https://github.com/Kitware/CMake/releases/download/v3.23.0/cmake-3.23.0.tar.gz

# Install CMake
tar xvzf cmake-3.23.0.tar.gz
cd cmake-3.23.0
./bootstrap -- -D CMAKE_USE_OPENSSL=OFF
make
make install
export CMAKE_HOME=$(pwd)
cd ..

# Download Apache Ant
wget https://dlcdn.apache.org//ant/binaries/apache-ant-1.10.12-bin.tar.gz

# Install Apache Ant
tar xvzf apache-ant-1.10.12-bin.tar.gz
cd apache-ant-1.10.12
export ANT_HOME=$(pwd)
cd ..

# Set PATH variable
export PATH=$ANT_HOME/bin:$CMAKE_HOME/bin:$PATH

# Download and install opencv
wget https://github.com/opencv/opencv/archive/refs/tags/4.5.5.tar.gz
tar xvzf 4.5.5.tar.gz
cd opencv-4.5.5
mkdir build
cd build
cmake -DBUILD_SHARED_LIBS=OFF -D BUILD_PERF_TESTS=OFF -D BUILD_opencv_python=OFF -D CMAKE_INSTALL_PREFIX=../../thang_opencv -D PYTHON_DEFAULT_EXECUTABLE=$(which python3) ..
make

printf "done... check %s" $(pwd)

cd ~