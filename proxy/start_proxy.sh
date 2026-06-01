#!/bin/bash
# ============================================================
# start_proxy.sh — Agent Proxy Gandal
# Installe tout et lance le Proxy sur cette VM
#
# USAGE :
#   chmod +x start_proxy.sh
#   sudo ./start_proxy.sh
#
# PRÉREQUIS : Ubuntu/Debian avec accès sudo ou root
#             (iptables est requis pour la fonctionnalité de blocage)
# ============================================================

set -e  # arrêter si une commande échoue

# ── À MODIFIER AVANT DE LANCER ────────────────────────────
ANALYSEUR_HOST="192.168.123.102"  # IP du nœud qui héberge cette VM
ANALYSEUR_PORT="9999"
VM_ID="omega-test-2301"           # Nom de cette VM
VNET_ID="link-2301-2302"          # VNet Proxmox de cette VM
INTERVAL="5"                      # Secondes entre chaque collecte
CTRL_HOST="0.0.0.0"               # Interface du serveur de controle HTTP
CTRL_PORT="8800"                  # Port HTTP pour /block /unblock /status
# ─────────────────────────────────────────────────────────

INSTALL_DIR="/opt/gandal-proxy"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "============================================================"
echo "  Agent Proxy Gandal — Installation et démarrage"
echo "  VM       : $VM_ID"
echo "  VNet     : $VNET_ID"
echo "  Analyseur: $ANALYSEUR_HOST:$ANALYSEUR_PORT"
echo "  Ctrl HTTP: $CTRL_HOST:$CTRL_PORT"
echo "============================================================"
echo ""

# ── ÉTAPE 1 : Mise à jour du système ─────────────────────
echo "[1/6] Mise à jour du système..."
apt-get update -qq

# ── ÉTAPE 2 : Installer Python3 ──────────────────────────
echo "[2/6] Vérification de Python3..."
if ! command -v python3 &>/dev/null; then
    echo "      Installation de Python3..."
    apt-get install -y python3
else
    echo "      Python3 déjà installé : $(python3 --version)"
fi

# ── ÉTAPE 3 : Installer iptables (pour blocage de la VM) ─
echo "[3/6] Vérification de iptables..."
if ! command -v iptables &>/dev/null; then
    echo "      Installation de iptables..."
    apt-get install -y iptables
else
    echo "      iptables déjà installé"
fi

# ── ÉTAPE 4 : Créer le dossier d'installation ────────────
echo "[4/6] Création du dossier $INSTALL_DIR..."
mkdir -p "$INSTALL_DIR"

# Copier proxy.py depuis le dossier du script
if [ -f "$SCRIPT_DIR/proxy.py" ]; then
    cp "$SCRIPT_DIR/proxy.py" "$INSTALL_DIR/proxy.py"
    echo "      proxy.py copié dans $INSTALL_DIR"
else
    echo "[ERREUR] proxy.py introuvable dans $SCRIPT_DIR"
    echo "         Assurez-vous que proxy.py est dans le même dossier que ce script"
    exit 1
fi

# ── ÉTAPE 5 : Test de connectivité vers l'Analyseur ──────
echo "[5/6] Test de connectivité vers $ANALYSEUR_HOST..."
if ping -c 1 -W 3 "$ANALYSEUR_HOST" &>/dev/null; then
    echo "      Nœud $ANALYSEUR_HOST accessible ✓"
else
    echo "      [WARN] $ANALYSEUR_HOST ne répond pas au ping"
    echo "      Vérifiez que l'Analyseur est bien lancé sur le nœud"
    echo "      On continue quand même — le Proxy réessaiera automatiquement"
fi

# ── ÉTAPE 6 : Lancer le Proxy ─────────────────────────────
echo "[6/6] Lancement du Proxy..."
echo ""
echo "  Le Proxy va se connecter à $ANALYSEUR_HOST:$ANALYSEUR_PORT"
echo "  Endpoint de contrôle HTTP : http://$CTRL_HOST:$CTRL_PORT"
echo "    - POST /block    : bloque la VM (drop INPUT/OUTPUT/FORWARD)"
echo "    - POST /unblock  : rétablit le trafic"
echo "    - GET  /status   : état courant"
echo "  Appuyez sur Ctrl+C pour arrêter"
echo ""

exec python3 "$INSTALL_DIR/proxy.py" \
    --analyseur-host "$ANALYSEUR_HOST" \
    --analyseur-port "$ANALYSEUR_PORT" \
    --vm-id          "$VM_ID" \
    --vnet-id        "$VNET_ID" \
    --interval       "$INTERVAL" \
    --ctrl-host      "$CTRL_HOST" \
    --ctrl-port      "$CTRL_PORT"
