#!/usr/bin/env sh

if [ ! -f torcs-1.3.1.tar.bz2 ]
then
  wget http://heanet.dl.sourceforge.net/project/torcs/all-in-one/1.3.1/torcs-1.3.1.tar.bz2
fi

if [ ! -f champ2010patch.tgz ]
then
  wget http://iweb.dl.sourceforge.net/project/cig/Championship%202010%20Linux/1.2/champ2010patch.tgz
fi

tar -xvxf torcs-1.3.1.tar.bz2
mv champ2010patch.tgz torcs-1.3.1/
pushd torcs-1.3.1
  ./configure
  sed -i 's/-lpng/-lpng12/g' Make-config
  sed -i 's/#include "png\.h"/#include <libpng12\/png.h>/' src/libs/tgfclient/img.cpp
  #tar -xvxf champ2010patch.tgz
  #cd ./src/ && patch -R -p1 < ../champ2010patch/patch.dat
  #cd ..
  make
  make install
  make datainstall
popd
