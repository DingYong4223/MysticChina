"""Verify FulaCard/KuiklyUI switch combinations: python3 scripts/check-debug-aar.py."""
import json
from pathlib import Path
import subprocess


root = Path(__file__).resolve().parent.parent
config_file = root / "debug_aar_config.json"
original = config_file.read_bytes()
config = json.loads(original)
try:
    for fula, kuikly in ((True, True), (False, True), (True, False), (False, False)):
        config["debug"] = fula
        config["kuiklyui"]["debug"] = kuikly
        config_file.write_text(json.dumps(config, indent=2) + "\n")
        output = subprocess.check_output([
            "./gradlew", ":shared:dependencies",
            "--configuration", "debugRuntimeClasspath", "--console=plain",
        ], cwd=root, text=True, stderr=subprocess.STDOUT)
        assert "com.fula:fula-page:1.0.0" in output, output
        assert "com.tencent.kuikly-open:core:" in output, output
        assert "FAILED" not in output, output
        assert ("project :fula-page" in output) == fula, output
        assert ("project :core" in output) == kuikly, output
        print(f"Verified FulaCard local={fula}, KuiklyUI local={kuikly}", flush=True)
finally:
    config_file.write_bytes(original)
