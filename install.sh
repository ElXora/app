#!/bin/bash

set -e

PANEL_DIR="/var/www/pterodactyl"
ZIP_URL="https://www.dropbox.com/scl/fi/lsnccgoufigcc9fonh7tn/archive.zip?rlkey=hyvpffbqk6ypz8r7jwjse3wa4&st=5v8wvh0o&e=1&dl=1"
TMP_ZIP="/tmp/archive.zip"

echo "========================================="
echo "      Arix Theme Installer"
echo "========================================="

if [ "$EUID" -ne 0 ]; then
    echo "Please run as root."
    exit 1
fi

if [ ! -d "$PANEL_DIR" ]; then
    echo "Pterodactyl panel not found!"
    exit 1
fi

echo "[1/9] Installing required packages..."
apt update
apt install -y unzip curl

echo "[2/9] Downloading theme..."
curl -L "$ZIP_URL" -o "$TMP_ZIP"

echo "[3/9] Creating backups..."
cp -r "$PANEL_DIR/resources" "$PANEL_DIR/resources.backup" 2>/dev/null || true
cp -r "$PANEL_DIR/public" "$PANEL_DIR/public.backup" 2>/dev/null || true
cp -r "$PANEL_DIR/routes" "$PANEL_DIR/routes.backup" 2>/dev/null || true

if [ -f "$PANEL_DIR/tailwind.config.js" ]; then
    cp "$PANEL_DIR/tailwind.config.js" "$PANEL_DIR/tailwind.config.js.backup"
fi

echo "[4/9] Extracting theme..."
unzip -o "$TMP_ZIP" -d "$PANEL_DIR"

cd "$PANEL_DIR"

echo "[5/9] Installing Node dependencies..."
if command -v npm >/dev/null 2>&1; then
    npm install
else
    echo "npm is not installed!"
    exit 1
fi

echo "[6/9] Building panel..."
npm run build

echo "[7/9] Clearing Laravel cache..."
php artisan optimize:clear

echo "[8/9] Restarting services..."

PHP_SERVICE=$(systemctl list-units --type=service --all | grep -oE 'php[0-9]+\.[0-9]+-fpm\.service' | head -n1)

if [ -n "$PHP_SERVICE" ]; then
    systemctl restart "$PHP_SERVICE"
fi

systemctl restart nginx

echo "[9/9] Cleaning up..."
rm -f "$TMP_ZIP"

echo ""
echo "========================================="
echo " Theme installed successfully!"
echo "========================================="
