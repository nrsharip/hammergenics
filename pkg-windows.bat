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
:: https://docs.microsoft.com/en-us/windows-server/administration/windows-commands/set_1
:: https://docs.microsoft.com/en-us/windows-server/administration/windows-commands/setx
SET PACKAGE_PATH=%PATH%
SET PACKAGE_WIX=.\wix311-binaries\
SET PACKAGE_OPTIONS=.\jpackage.windows.options
:: https://docs.microsoft.com/en-us/windows-server/administration/windows-commands/if
IF NOT DEFINED PACKAGE_JPACKAGE (SET PACKAGE_JPACKAGE=)
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
jpackage @%PACKAGE_OPTIONS%
ECHO.
SET PATH=%PACKAGE_PATH%

SET "PACKAGE_JPACKAGE="
SET "PACKAGE_WIX="
SET "PACKAGE_PATH="
SET "PACKAGE_OPTIONS="

ECHO finished: %PATH%
ENDLOCAL
