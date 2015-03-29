#! /bin/sh -e

echo "Installing Android ViewPagerIndicator"
cd Android-ViewPagerIndicator

gradle clean assemble

mvn install:install-file \
-DgroupId=com.viewpagerindicator \
-DartifactId=viewpagerindicator \
-Dversion=2.4.1 \
-DgeneratePom=true \
-Dpackaging=aar \
-Dfile=library/build/outputs/aar/library-2.4.1.aar \
-DlocalRepositoryPath=../

cd ../

echo "Installing SpeedometerView"

mvn install:install-file \
-DgroupId=com.cardiomood.android.speedometer \
-DartifactId=speedometer \
-Dversion=1.0.1 \
-DgeneratePom=true \
-Dpackaging=aar \
-Dfile=SpeedometerView-1.0.1.aar \
-DlocalRepositoryPath=./

echo "Done installing libraries"