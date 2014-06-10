#! /bin/sh -e

echo "Installing GrapView"
cd GraphView

gradle clean assemble

mvn install:install-file \
-DgroupId=com.jjoe64.graphview \
-DartifactId=graphview \
-Dversion=3.1.1 \
-DgeneratePom=true \
-Dpackaging=aar \
-Dfile=build/outputs/aar/GraphView.aar \
-DlocalRepositoryPath=../

cd ../

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

echo "Done installing libraries"