:: *****************************************************************************
:: Copyright 2021 Nail Sharipov (sharipovn@gmail.com)
:: 
:: Licensed under the Apache License, Version 2.0 (the "License");
:: you may not use this file except in compliance with the License.
:: You may obtain a copy of the License at
:: 
:: http://www.apache.org/licenses/LICENSE-2.0
:: 
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.
:: *****************************************************************************

:: https://ss64.com/nt/echo.html
@ECHO OFF
:: https://docs.microsoft.com/en-us/windows-server/administration/windows-commands/setlocal
:: ENABLEEXTENSIONS - Enables the command extensions until the matching endlocal command
:: is encountered, regardless of the setting before the setlocal command was run.
SETLOCAL ENABLEEXTENSIONS

:: https://docs.microsoft.com/en-us/windows-server/administration/windows-commands/if
IF [%1]==[] (
  ECHO            usage: pkg-windows.cmd [type] [path_to_jpackage]
  ECHO             type: app-image, exe, msi
  ECHO path_to_jpackage: most commonly path to the bin folder of JDK 16+ (also could be set via PACKAGE_JPACKAGE)
)

IF [%1]==[] EXIT /B -1

:: https://docs.microsoft.com/en-us/windows-server/administration/windows-commands/if
IF %~1==app-image GOTO :proceed1
IF %~1==exe GOTO :proceed1
IF %~1==msi GOTO :proceed1

ECHO wrong type selected: %PACKAGE_TYPE%
SET PACKAGE_TYPE=
EXIT /B -1

:proceed1
:: https://ss64.com/nt/syntax-args.html
:: %~1 Expand %1 removing any surrounding quotes (")
SET PACKAGE_TYPE=%~1
ECHO type selected: %PACKAGE_TYPE%

:: https://docs.microsoft.com/en-us/windows-server/administration/windows-commands/set_1
:: https://docs.microsoft.com/en-us/windows-server/administration/windows-commands/setx
SET PACKAGE_PATH=%PATH%
SET PACKAGE_WIX=.\wix311-binaries\
SET PACKAGE_OPTIONS1=.\jpackage.options
SET PACKAGE_OPTIONS2=.\jpackage.windows.options

:: https://docs.microsoft.com/en-us/windows-server/administration/windows-commands/if
IF NOT [%2]==[] (
  REM https://ss64.com/nt/syntax-args.html
  REM %~f1 Expand %1 to a Fully qualified path name - C:\utils\MyFile.txt
  SET PACKAGE_JPACKAGE=%~f2
)

:: this part is intended for the manual PACKAGE_JPACKAGE override
:: https://docs.microsoft.com/en-us/windows-server/administration/windows-commands/if
IF NOT DEFINED PACKAGE_JPACKAGE (
  SET PACKAGE_JPACKAGE=
)

:: https://docs.microsoft.com/en-us/windows-server/administration/windows-commands/if
IF DEFINED PACKAGE_JPACKAGE (
  IF EXIST %PACKAGE_JPACKAGE%\jpackage.exe (
     ECHO jpackage version:
     %PACKAGE_JPACKAGE%\jpackage.exe --version
  ) ELSE (
     ECHO jpackage.exe does not exist at the location provided: %PACKAGE_JPACKAGE%
     EXIT /B -1
  )
) ELSE (
  ECHO make sure PACKAGE_JPACKAGE contains the path to jpackage.exe [JDK 16+]
  EXIT /B -1
)

SET PATH=%PACKAGE_JPACKAGE%;%PACKAGE_WIX%;%PATH%

ECHO.
ECHO deleting temporary folder:
RMDIR /s .\tmp

ECHO.
ECHO deleting output folder:
RMDIR /s .\jpackage

ECHO.
ECHO packaging for Windows
ECHO.
ECHO path: %PATH%
ECHO.
jpackage --type "%PACKAGE_TYPE%" @%PACKAGE_OPTIONS1% @%PACKAGE_OPTIONS2%
ECHO.
SET PATH=%PACKAGE_PATH%

SET "PACKAGE_TYPE="
SET "PACKAGE_JPACKAGE="
SET "PACKAGE_WIX="
SET "PACKAGE_PATH="
SET "PACKAGE_OPTIONS1="
SET "PACKAGE_OPTIONS2="

ECHO finished: %PATH%
ENDLOCAL
