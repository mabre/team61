@echo off
:: Launcher script, tested on Windows 7
:: IF YOU SEE THIS TEXT, PLEASE REFER TO THE MANUAL (FAQ)
echo Charly in Madagascar
echo Do NOT close this window.
echo =========================
set size=0
call :filesize "Afrobob.log"
echo file size is %size%
set writable=0
copy /y Launch.bat .writable > NUL 2>&1 && set writable=1
echo %writable%
if "%writable%" == "1" (
	del .writable
	java -jar Charly_in_Madagascar.jar >> Afrobob.log 2>&1

) else (
	echo Cannot write to .
	java -jar Charly_in_Madagascar.jar
)
goto :eof

:: get file size TODO use it and make it more like the linux script (date logging etc.)
:filesize
  set size=%~z1
  exit /b 0
