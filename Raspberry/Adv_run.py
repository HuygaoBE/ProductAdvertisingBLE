import subprocess
import time
import psutil


def kill(proc_pid):
    process = psutil.Process(proc_pid)
    for proc in process.children(recursive=True):
        proc.kill()
    process.kill()

while(True):
    cmd = "sudo python3 Advertisement.py"
    proc = subprocess.Popen(cmd, shell=True)
    try:
        proc.wait(timeout=7)
    except subprocess.TimeoutExpired:
        kill(proc.pid)
