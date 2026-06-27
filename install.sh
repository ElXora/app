#!/bin/bash

set -e

PANEL_DIR="/var/www/pterodactyl"
ZIP_URL="https://www.dropbox.com/scl/fi/lsnccgoufigcc9fonh7tn/archive.zip?rlkey=hyvpffbqk6ypz8r7jwjse3wa4&st=5v8wvh0o&e=1&dl=1"
ZIP_FILE="/tmp/archive.zip"

echo "======================================"
echo "      Kroxy Theme Installer"
echo "======================================"

if [ "$EUID" -ne 0 ]; then
    echo "Please run as root!"
    exit 1
fi

if [ ! -d "$PANEL_DIR" ]; then
    echo "Pterodactyl panel not found!"
    exit 1
fi

echo "[1/8] Installing requirements..."
apt update
apt install -y curl unzip

echo "[2/8] Downloading theme..."
curl -L "$ZIP_URL" -o "$ZIP_FILE"

echo "[3/8] Creating backups..."
cd "$PANEL_DIR"

[ -d app ] && cp -r app app.backup
[ -d database ] && cp -r database database.backup
[ -d public ] && cp -r public public.backup
[ -d resources ] && cp -r resources resources.backup
[ -d routes ] && cp -r routes routes.backup
[ -f tailwind.config.js ] && cp tailwind.config.js tailwind.config.js.backup

echo "[4/8] Extracting theme..."
unzip -o "$ZIP_FILE" -d "$PANEL_DIR"

echo "[5/8] Installing dependencies..."
npm install --legacy-peer-deps

echo "[6/8] Building..."
npm run build

echo "[7/8] Clearing cache..."
php artisan optimize:clear

echo "[8/8] Restarting services..."

systemctl restart nginx

PHP_SERVICE=$(systemctl list-units --type=service --all | awk '/php.*-fpm/ {print $1; exit}')
if [ -n "$PHP_SERVICE" ]; then
    systemctl restart "$PHP_SERVICE"
fi

rm -f "$ZIP_FILE"

echo ""
echo "======================================"
echo " Theme installed successfully!"
echo "======================================"
