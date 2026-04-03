$sourcePath = "src"
$excludeFiles = @("ThomasCredits") # Aggiungi qui altre classi puramente testuali

# Definizione dei due gruppi
$systemClasses = @("ThomasMain", "GameInput","LevelConstants","LevelInfo","ScoreEntity","ThomasControllerListener")
$screenClasses = @("LevelScreen", "Level1Screen","Level2Screen","Level3Screen","Level4Screen","Level5Screen","GameOverScreen","HighScoreScreen","IntermissionLevel2Screen","IntermissionLevel4Screen","IntroLetterScreen","NameEntryScreen", "MainMenuScreen")
$entitiesClasses = @("BoomerangThrower", "Butterfly","Dragon","Enemy","ExplodingBall","FallingHazard","Giant","GrabbingEnemy","Gripper","Hunchback","KnifeThrower","MrX", "Player","Snake","StickFighter","Sylvia","TomTom")
$spriteResClasses =@("Boomerang", "Crow","FloatingScore","HeadProjectile","HitEffect","Knife","MagicFlame","PotProjectile","RopeFall","Trapdoor","AudioRes","BoomerangThrowerRes","ButterflyRes","DragonRes", "ExplodingBallRes","GiantRes","GripperRes","HunchbackRes","KnifeThrowerRes","MrXRes", "PlayerRes","SnakeRes","StickFighterRes","SylviaRes","TomTomRes","TrapdoorRes")

# Se una classe non è in $systemClasses, andrà nel secondo file (Entities)

$outputSystem = "Thomas_1_System.txt"
$outputScreen = "Thomas_2_Screen.txt"
$outputEntities = "Thomas_3_Entities.txt"
$outputSpriteRes = "Thomas_4_SpriteRes.txt"

function Export-Classes($classList, $outputFile, $isSystem) {
    "--- SNAPSHOT $(Get-Date) ---" | Out-File $outputFile
    Get-ChildItem -Path $sourcePath -Filter *.java -Recurse | ForEach-Object {
        $fileName = $_.BaseName
        $inList = $classList -contains $fileName
        
        # Logica: Se isSystem è true, prendi solo quelle in lista. 
        # Se isSystem è false, prendi tutte le altre (tranne escluse).
        if (($isSystem -and $inList -and $excludeFiles -notcontains $fileName)) {
            "`n==========================================`nFILE: $($_.FullName)`n==========================================" | Out-File $outputFile -Append
            $content = Get-Content $_.FullName -Raw
            $content = $content -replace '(?s)/\*.*?\*/', '' # Rimuove /* */
            $content = $content -replace '//.*', ''         # Rimuove //
            $content = ($content -split "`r?`n" | Where-Object { $_.Trim() -ne "" }) -join "`n"
            $content | Out-File $outputFile -Append
        }
    }
}

# Genera i due file
Export-Classes $systemClasses $outputSystem $true
Export-Classes $screenClasses $outputScreen $true
Export-Classes $entitiesClasses $outputEntities $true
Export-Classes $spriteResClasses $outputSpriteRes $true

Write-Host "Snapshot completato! Creati $outputSystem, $outputScreen, $outputSpriteRes e $outputEntities" -ForegroundColor Green
pause
