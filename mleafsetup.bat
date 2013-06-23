REM - Check if Directory on Local drive (C:) exists, Create if Not.
IF EXIST C:\MovieLeaf GOTO MovieLeaf_EXISTS
MKDIR C:\MovieLeaf
:MovieLeaf_EXISTS
REM - Copy relevant files to Local drive (C:)
copy ISOWriter.exe c:\MovieLeaf
copy downloadhandler.jar c:\MovieLeaf
copy banner.jpg c:\MovieLeaf
copy index.html c:\MovieLeaf
REM - Update Registry settings
cd \
REG ADD HKCU\SOFTWARE\MovieLeaf /v InstalledDrive /t REG_SZ /d %CD% /f
REG ADD HKCU\SOFTWARE\MovieLeaf /v InstalledDirectory /t REG_SZ /d C:\MovieLeaf /f
REG ADD HKCU\SOFTWARE\MovieLeaf /v BootSite /t REG_SZ /d http://127.0.0.1/x.html /f
REM - Call download handler .jar file
cd c:\MovieLeaf
Start downloadhandler.jar
exit
