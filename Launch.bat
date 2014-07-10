@echo off
:: Launcher script, tested on Windows 7
echo Charly in Madagascar
echo Do NOT close this window.
echo =========================
set size=0
call :filesize "Afrobob.log"
echo file size is %size%
java -jar Charly_in_Madagascar.jar >> Afrobob.log 2>&1
goto :eof

:: get file size TODO use it and make it more like the linux script (date logging etc.)
:filesize
  set size=%~z1
  exit /b 0
