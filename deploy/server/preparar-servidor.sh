#!/usr/bin/env sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
command -v docker >/dev/null 2>&1 || { echo 'Instala Docker en el servidor antes de continuar.'; exit 1; }
docker compose version >/dev/null
mkdir -p certs
if command -v curl >/dev/null 2>&1; then
    curl --fail --silent --show-error --location --proto '=https' --tlsv1.2 \
        https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem \
        --output certs/global-bundle.pem.tmp
elif command -v wget >/dev/null 2>&1; then
    wget -q https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem -O certs/global-bundle.pem.tmp
else
    echo 'Instala curl o wget para descargar el certificado público de RDS.'
    exit 1
fi
grep -q 'BEGIN CERTIFICATE' certs/global-bundle.pem.tmp || { echo 'La descarga no contiene certificados válidos.'; exit 1; }
mv certs/global-bundle.pem.tmp certs/global-bundle.pem
chmod 644 certs/global-bundle.pem
if [ ! -f .env ]; then
    (umask 077; cp .env.example .env)
fi
chmod 600 .env
echo 'Certificados de RDS preparados. Completa DB_URL, DB_USER y DB_PASSWORD en .env.'
echo 'Después ejecuta: docker compose up --build -d'
