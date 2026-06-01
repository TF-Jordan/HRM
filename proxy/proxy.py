#!/usr/bin/env python3
"""
proxy.py — Agent Proxy Gandal
Collecte les connexions réseau de la VM et les envoie à l'Analyseur via socket TCP.
Expose aussi un endpoint HTTP pour bloquer/débloquer la VM.
"""

import socket
import json
import time
import argparse
import threading
import subprocess
from http.server import BaseHTTPRequestHandler, HTTPServer


# État global du blocage (partagé entre les threads)
ETAT = {"bloquee": False}


def lire_arguments():
    p = argparse.ArgumentParser(description="Agent Proxy — Gandal Datacenter")
    p.add_argument("--analyseur-host", default="127.0.0.1", help="IP du noeud (Analyseur)")
    p.add_argument("--analyseur-port", type=int, default=9999)
    p.add_argument("--vm-id",    required=True, help="Nom de cette VM")
    p.add_argument("--vnet-id",  required=True, help="VNet Proxmox de cette VM")
    p.add_argument("--interval", type=float, default=5.0, help="Secondes entre collectes")
    p.add_argument("--simulate", action="store_true", help="Mode test sans /proc/net/tcp")
    p.add_argument("--src-ip",   default="10.0.1.10", help="IP source pour simulation")
    p.add_argument("--ctrl-host", default="0.0.0.0", help="Interface du serveur de controle HTTP")
    p.add_argument("--ctrl-port", type=int, default=8800, help="Port du serveur de controle HTTP")
    return p.parse_args()


# ── Lecture /proc/net/tcp ────────────────────────────────────────────────────

def hex_vers_ip(hex_addr):
    """Convertit '0F01A8C0' en '192.168.1.15' (little-endian)."""
    partie_ip = hex_addr.split(":")[0]
    n = int(partie_ip, 16)
    return f"{n&0xFF}.{(n>>8)&0xFF}.{(n>>16)&0xFF}.{(n>>24)&0xFF}"

def hex_vers_port(hex_addr):
    """Convertit '0F01A8C0:0050' en '80'."""
    return str(int(hex_addr.split(":")[1], 16))

def lire_proc(fichier, protocole, vm_id, vnet_id):
    connexions = []
    try:
        with open(fichier) as f:
            lignes = f.readlines()[1:]
    except FileNotFoundError:
        return connexions

    for ligne in lignes:
        cols = ligne.split()
        if len(cols) < 3:
            continue
        local_hex  = cols[1]
        remote_hex = cols[2]
        etat       = cols[3] if len(cols) > 3 else ""

        if protocole == "TCP" and etat != "01":
            continue

        dst_ip   = hex_vers_ip(remote_hex)
        dst_port = hex_vers_port(remote_hex)

        if dst_ip == "0.0.0.0" or dst_port == "0":
            continue

        connexions.append({
            "srcip":     hex_vers_ip(local_hex),
            "dstip":     dst_ip,
            "protocol":  protocole,
            "port":      dst_port,
            "vm_id":     vm_id,
            "vnet_id":   vnet_id,
            "timestamp": time.time(),
        })
    return connexions

def collecter(vm_id, vnet_id):
    logs  = lire_proc("/proc/net/tcp", "TCP", vm_id, vnet_id)
    logs += lire_proc("/proc/net/udp", "UDP", vm_id, vnet_id)
    return logs

def simuler(src_ip, vm_id, vnet_id):
    now = time.time()
    return [
        {"srcip": src_ip, "dstip": "10.64.1.20",  "protocol": "TCP", "port": "443",  "vm_id": vm_id, "vnet_id": vnet_id, "timestamp": now},
        {"srcip": src_ip, "dstip": "10.10.0.53",  "protocol": "UDP", "port": "53",   "vm_id": vm_id, "vnet_id": vnet_id, "timestamp": now},
        {"srcip": src_ip, "dstip": "10.64.3.5",   "protocol": "TCP", "port": "80",   "vm_id": vm_id, "vnet_id": vnet_id, "timestamp": now},
        {"srcip": src_ip, "dstip": "10.10.0.1",   "protocol": "TCP", "port": "8006", "vm_id": vm_id, "vnet_id": vnet_id, "timestamp": now},
    ]


# ── Blocage / Deblocage de la VM via iptables ────────────────────────────────
#
# On utilise iptables pour DROPer tout le trafic INPUT, OUTPUT et FORWARD.
# On garde lo (loopback) ouverte pour que le serveur de controle local reste
# joignable depuis la VM elle-meme (utile pour deblocage en local).
# Le port de controle reste accessible sur l'interface choisie (ctrl_port).

CHAINS = ("INPUT", "OUTPUT", "FORWARD")

def _run(cmd):
    return subprocess.run(cmd, capture_output=True, text=True)

def bloquer_vm(ctrl_port):
    """Bloque tout trafic reseau, sauf loopback et port de controle."""
    if ETAT["bloquee"]:
        return True, "VM deja bloquee"

    # Autoriser loopback
    _run(["iptables", "-I", "INPUT",  "1", "-i", "lo", "-j", "ACCEPT"])
    _run(["iptables", "-I", "OUTPUT", "1", "-o", "lo", "-j", "ACCEPT"])

    # Garder le port de controle joignable pour pouvoir debloquer
    _run(["iptables", "-I", "INPUT",  "2", "-p", "tcp", "--dport", str(ctrl_port), "-j", "ACCEPT"])
    _run(["iptables", "-I", "OUTPUT", "2", "-p", "tcp", "--sport", str(ctrl_port), "-j", "ACCEPT"])

    # Tout droper
    for chain in CHAINS:
        r = _run(["iptables", "-P", chain, "DROP"])
        if r.returncode != 0:
            return False, f"Echec iptables -P {chain} DROP: {r.stderr.strip()}"

    ETAT["bloquee"] = True
    print(f"[Proxy] VM BLOQUEE (port de controle {ctrl_port} laisse ouvert)")
    return True, "VM bloquee"

def debloquer_vm():
    """Retablit la politique par defaut ACCEPT."""
    if not ETAT["bloquee"]:
        return True, "VM deja debloquee"
    for chain in CHAINS:
        _run(["iptables", "-P", chain, "ACCEPT"])
    # Nettoyer les regles de loopback / ctrl ajoutees au blocage
    _run(["iptables", "-F", "INPUT"])
    _run(["iptables", "-F", "OUTPUT"])
    ETAT["bloquee"] = False
    print("[Proxy] VM DEBLOQUEE")
    return True, "VM debloquee"


# ── Serveur HTTP de controle ─────────────────────────────────────────────────

class CtrlHandler(BaseHTTPRequestHandler):
    ctrl_port = 8800  # ecrase au demarrage

    def _json(self, code, payload):
        body = json.dumps(payload).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, fmt, *args):
        print(f"[Ctrl] {self.address_string()} - {fmt % args}")

    def do_GET(self):
        if self.path == "/status":
            self._json(200, {"bloquee": ETAT["bloquee"]})
        else:
            self._json(404, {"error": "not found"})

    def do_POST(self):
        if self.path == "/block":
            ok, msg = bloquer_vm(self.ctrl_port)
            self._json(200 if ok else 500, {"ok": ok, "message": msg, "bloquee": ETAT["bloquee"]})
        elif self.path == "/unblock":
            ok, msg = debloquer_vm()
            self._json(200 if ok else 500, {"ok": ok, "message": msg, "bloquee": ETAT["bloquee"]})
        else:
            self._json(404, {"error": "not found"})


def lancer_serveur_ctrl(host, port):
    CtrlHandler.ctrl_port = port
    srv = HTTPServer((host, port), CtrlHandler)
    t = threading.Thread(target=srv.serve_forever, daemon=True)
    t.start()
    print(f"[Ctrl] Endpoint HTTP en ecoute sur {host}:{port}")
    print(f"[Ctrl]   POST /block    -> bloque la VM")
    print(f"[Ctrl]   POST /unblock  -> debloque la VM")
    print(f"[Ctrl]   GET  /status   -> etat courant")
    return srv


# ── Socket vers l'Analyseur ───────────────────────────────────────────────────

def connecter(host, port):
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect((host, port))
        print(f"[Proxy] Connecte a l'Analyseur {host}:{port}")
        return s
    except (ConnectionRefusedError, OSError) as e:
        print(f"[Proxy] Analyseur indisponible ({host}:{port}): {e}")
        return None

def envoyer(sock, log):
    try:
        msg = json.dumps(log) + "\n"
        sock.sendall(msg.encode("utf-8"))
        return True
    except (BrokenPipeError, ConnectionResetError, OSError):
        return False


# ── Boucle principale ─────────────────────────────────────────────────────────

def main():
    args = lire_arguments()
    print("=" * 50)
    print("  Agent Proxy — Gandal Datacenter")
    print(f"  VM    : {args.vm_id}")
    print(f"  VNet  : {args.vnet_id}")
    print(f"  Cible : {args.analyseur_host}:{args.analyseur_port}")
    print(f"  Ctrl  : {args.ctrl_host}:{args.ctrl_port}")
    print(f"  Mode  : {'SIMULATION' if args.simulate else 'REEL'}")
    print("=" * 50)

    lancer_serveur_ctrl(args.ctrl_host, args.ctrl_port)

    sock = None
    while True:
        if ETAT["bloquee"]:
            # VM bloquee: on n'envoie plus rien a l'Analyseur
            if sock is not None:
                try: sock.close()
                except Exception: pass
                sock = None
            time.sleep(args.interval)
            continue

        if sock is None:
            sock = connecter(args.analyseur_host, args.analyseur_port)
            if sock is None:
                print("[Proxy] Retry dans 5s...")
                time.sleep(5)
                continue

        logs = simuler(args.src_ip, args.vm_id, args.vnet_id) if args.simulate \
               else collecter(args.vm_id, args.vnet_id)

        print(f"[Proxy] {len(logs)} connexion(s) collectee(s)")
        for log in logs:
            if envoyer(sock, log):
                print(f"[Proxy]   -> {log['srcip']} -> {log['dstip']}:{log['port']} {log['protocol']}")
            else:
                print("[Proxy] Connexion perdue — reconnexion...")
                sock.close()
                sock = None
                break

        time.sleep(args.interval)

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n[Proxy] Arrete.")
