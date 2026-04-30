$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$classpathEntries = @(
    (Join-Path $projectRoot 'target\classes'),
    (Join-Path $env:USERPROFILE '.m2\repository\com\mysql\mysql-connector-j\8.0.32\mysql-connector-j-8.0.32.jar'),
    (Join-Path $env:USERPROFILE '.m2\repository\com\google\protobuf\protobuf-java\3.21.9\protobuf-java-3.21.9.jar'),
    (Join-Path $env:USERPROFILE '.m2\repository\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar'),
    (Join-Path $env:USERPROFILE '.m2\repository\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar'),
    (Join-Path $env:USERPROFILE '.m2\repository\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2.jar'),
    (Join-Path $env:USERPROFILE '.m2\repository\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2-win.jar'),
    (Join-Path $env:USERPROFILE '.m2\repository\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2.jar'),
    (Join-Path $env:USERPROFILE '.m2\repository\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar'),
    (Join-Path $env:USERPROFILE '.m2\repository\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2.jar'),
    (Join-Path $env:USERPROFILE '.m2\repository\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2-win.jar'),
    (Join-Path $env:USERPROFILE '.m2\repository\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2.jar'),
    (Join-Path $env:USERPROFILE '.m2\repository\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar')
)

$missing = $classpathEntries | Where-Object { -not (Test-Path $_) }
if ($missing) {
    Write-Error ("Missing runtime dependency files:`n" + ($missing -join "`n"))
}

$classpath = $classpathEntries -join ';'
$java = $null
if ($env:JAVA_HOME) {
    $java = Join-Path $env:JAVA_HOME 'bin\java.exe'
}
if (-not $java -or -not (Test-Path $java)) {
    $java = 'java'
}

$javaArgs = @(
    '-Dfile.encoding=UTF-8',
    '-cp',
    $classpath,
    'piJava.mains.Launcher'
)

& $java @javaArgs
