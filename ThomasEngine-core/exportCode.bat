@echo off
setlocal enabledelayedexpansion
set "OUTPUT_FILE=ThomasEngine_FullCode.txt"
:: Elenca qui i file da ESCLUDERE (separati da spazio, senza estensione)
set "EXCLUDE=ThomasCredits AudioRes GripperRes KnifeThrowerRes PlayerRes StickFighterRes"

echo --- PROJECT SNAPSHOT (NO COMMENTS) --- > "%OUTPUT_FILE%"
echo Generato il: %date% %time% >> "%OUTPUT_FILE%"

for /f "delims=" %%f in ('dir /s /b "src\*.java"') do (
    set "skip="
    for %%e in (%EXCLUDE%) do (
        echo %%f | findstr /i "%%e.java" >nul && set "skip=1"
    )
    
    if not defined skip (
        echo ========================================== >> "%OUTPUT_FILE%"
        echo FILE: %%f >> "%OUTPUT_FILE%"
        echo ========================================== >> "%OUTPUT_FILE%"
        
        :: Filtra commenti mono-riga e righe vuote
        findstr /r /v "^[[:space:]]*//" "%%f" | findstr /r /v "^[[:space:]]*$" >> "%OUTPUT_FILE%"
        
        echo. >> "%OUTPUT_FILE%"
    )
)

echo Snapshot "pulito" completato: %OUTPUT_FILE%
pause