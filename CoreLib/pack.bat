set MYDIR=%CD%
cd bin
REM zip -r ../corelib.jar *
jar -cvf ../corelib.jar *
cd /d %MYDIR%
