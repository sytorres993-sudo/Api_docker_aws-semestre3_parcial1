param(
    [Parameter(Mandatory = $true)][ValidatePattern('^\d{12}$')][string]$AccountId,
    [string]$Region = 'us-east-1',
    [string]$Repository = 'gimnasio-sara',
    [string]$Tag = '1.0.0'
)
$ErrorActionPreference = 'Stop'
function Assert-Exit([string]$Action) { if ($LASTEXITCODE -ne 0) { throw "Falló: $Action" } }
Get-Command aws -ErrorAction Stop | Out-Null
Get-Command docker -ErrorAction Stop | Out-Null
$registry = "$AccountId.dkr.ecr.$Region.amazonaws.com"
$remoteImage = "${registry}/${Repository}:$Tag"
$actualAccount = aws sts get-caller-identity --query Account --output text
Assert-Exit 'validar credenciales AWS'
if ($actualAccount.Trim() -ne $AccountId) { throw 'La cuenta autenticada no coincide con AccountId.' }
aws ecr describe-repositories --repository-names $Repository --region $Region 1>$null 2>$null
if ($LASTEXITCODE -ne 0) {
    aws ecr create-repository --repository-name $Repository --region $Region --image-scanning-configuration scanOnPush=true | Out-Null
    Assert-Exit 'crear repositorio ECR'
}
aws ecr get-login-password --region $Region | docker login --username AWS --password-stdin $registry
Assert-Exit 'iniciar sesión en ECR'
docker image inspect "gimnasio-sara:$Tag" 1>$null
Assert-Exit 'comprobar imagen local (construye con docker build --load primero)'
docker tag "gimnasio-sara:$Tag" $remoteImage
Assert-Exit 'etiquetar imagen'
docker push $remoteImage
Assert-Exit 'publicar imagen'
Write-Host "Imagen publicada: $remoteImage"
