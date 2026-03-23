@echo off
echo Copying recipes to all possible locations...

REM Main config directory
if not exist "config\political" mkdir "config\political"
copy "config\political\custom_recipes.json" "config\political\custom_recipes.json" >nul 2>&1

REM Runtime config directory
if not exist "run\config\political" mkdir "run\config\political"
copy "config\political\custom_recipes.json" "run\config\political\custom_recipes.json" >nul 2>&1

REM Root config directory
if not exist "config" mkdir "config"
copy "config\political\custom_recipes.json" "config\custom_recipes.json" >nul 2>&1

REM Run root directory
copy "config\political\custom_recipes.json" "run\custom_recipes.json" >nul 2>&1

REM Current directory
copy "config\political\custom_recipes.json" "custom_recipes.json" >nul 2>&1

echo Files copied to all possible locations!
echo.
echo Locations checked:
echo - config\political\custom_recipes.json
echo - run\config\political\custom_recipes.json  
echo - config\custom_recipes.json
echo - run\custom_recipes.json
echo - custom_recipes.json
echo.
pause
