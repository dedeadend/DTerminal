import io
import json
import os
import re
import shlex
import shutil
import sys
import traceback
import urllib.request
import zipfile


class AndroidStdoutRedirector:
    def __init__(self, kotlin_listener):
        self.listener = kotlin_listener
        self.buffer = ""

    def write(self, text):
        if not text:
            return

        self.buffer += text

        if "\n" in self.buffer:
            lines = self.buffer.split("\n")
            self.buffer = lines[-1]
            for line in lines[:-1]:
                self.listener.onStdout(line + "\n")

    def flush(self):
        if self.buffer:
            self.listener.onStdout(self.buffer)
            self.buffer = ""


class DummyStdin:
    def readline(self):
        return "\n"

    def read(self, *args, **kwargs):
        return ""


def get_installed_db(packages_dir):
    db_path = os.path.join(packages_dir, "installed_packages.json")
    if os.path.exists(db_path):
        try:
            with open(db_path, "r", encoding="utf-8") as f:
                return json.load(f)
        except:
            return {}
    return {}


def save_installed_db(packages_dir, db):
    db_path = os.path.join(packages_dir, "installed_packages.json")
    try:
        with open(db_path, "w", encoding="utf-8") as f:
            json.dump(db, f, ensure_ascii=False, indent=4)
        return True
    except:
        return False


def safe_network_download_and_install(package_name, packages_dir):
    clean_name = re.split(r'[<>=!]', package_name)[0].strip().lower()
    db = get_installed_db(packages_dir)
    if clean_name in db:
        print(f"[pip] Requirement already satisfied: {clean_name}")
        return True

    print(f"[pip] Fetching metadata for '{clean_name}' from PyPI...")
    pypi_url = f"https://pypi.org/pypi/{clean_name}/json"

    try:
        with urllib.request.urlopen(pypi_url, timeout=10) as response:
            data = json.loads(response.read().decode('utf-8'))

        urls = data['urls']
        wheel_url = None
        filename = None

        for u in urls:
            if u['filename'].endswith('.whl') and 'none-any' in u['filename']:
                wheel_url = u['url']
                filename = u['filename']
                break

        if not wheel_url:
            print(f"[pip] Error: Could not find a Pure Python wheel for '{clean_name}'.")
            return False

        print(f"[pip] Downloading {filename}...")
        os.makedirs(packages_dir, exist_ok=True)
        zip_file_path = os.path.join(packages_dir, filename)

        urllib.request.urlretrieve(wheel_url, zip_file_path)
        print(f"[pip] Installing and extracting '{clean_name}'...")

        top_level_items = set()
        with zipfile.ZipFile(zip_file_path, 'r') as zip_ref:
            for name in zip_ref.namelist():
                parts = name.split('/')
                if parts[0]:
                    top_level_items.add(parts[0])
            zip_ref.extractall(packages_dir)

        os.remove(zip_file_path)
        db[clean_name] = list(top_level_items)
        save_installed_db(packages_dir, db)
        print(f"[pip] Successfully installed '{clean_name}'!")

        requires_dist = data['info'].get('requires_dist')
        if requires_dist:
            for req in requires_dist:
                if 'extra ==' in req or 'sys_platform' in req:
                    continue
                dep_name = re.split(r'[<>=! \(\)]', req)[0].strip()
                if dep_name:
                    safe_network_download_and_install(dep_name, packages_dir)
        return True
    except Exception as e:
        print(f"[pip] Connection or Installation failed for '{clean_name}': {str(e)}")
        return False


def safe_uninstall(package_name, packages_dir):
    clean_name = package_name.strip().lower()
    db = get_installed_db(packages_dir)

    if clean_name not in db:
        print(f"[pip] Skipping {clean_name} as it is not installed.")
        return False

    print(f"[pip] Uninstalling '{clean_name}'...")
    associated_items = db[clean_name]

    for item in associated_items:
        item_path = os.path.join(packages_dir, item)
        if os.path.exists(item_path):
            if os.path.isdir(item_path):
                shutil.rmtree(item_path)
            else:
                os.remove(item_path)

    if clean_name in db:
        del db[clean_name]
    save_installed_db(packages_dir, db)
    print(f"[pip] Successfully uninstalled '{clean_name}'!")
    return True


def safe_list(packages_dir):
    db = get_installed_db(packages_dir)
    if not db:
        print("No user packages installed.")
        return True

    print(f"{'Package':<25}")
    print("-" * 25)
    for package in sorted(db.keys()):
        print(f"{package:<25}")
    return True


def execute_smart_block(text_block, kotlin_listener, cache_dir_path):
    custom_packages_dir = os.path.join(cache_dir_path, "user_python_packages")

    if custom_packages_dir not in sys.path:
        sys.path.append(custom_packages_dir)

    old_stdout = sys.stdout
    old_stderr = sys.stderr
    old_stdin = sys.stdin

    redirector = AndroidStdoutRedirector(kotlin_listener)
    sys.stdout = redirector
    sys.stderr = redirector
    sys.stdin = DummyStdin()

    globals_dict = {'__name__': '__main__'}
    lines = text_block.split('\n')
    current_python_chunk = []

    def run_pending_python():
        nonlocal current_python_chunk
        if current_python_chunk:
            source_code = '\n'.join(current_python_chunk)
            exec(source_code, globals_dict)
            current_python_chunk = []

    try:
        for line in lines:
            stripped = line.strip()
            if not stripped:
                continue

            if stripped == 'pip list':
                run_pending_python()
                safe_list(custom_packages_dir)

            elif stripped.startswith('pip install '):
                run_pending_python()
                target_package = stripped.replace('pip install ', '').strip()
                safe_network_download_and_install(target_package, custom_packages_dir)

            elif stripped.startswith('pip uninstall '):
                run_pending_python()
                target_package = stripped.replace('pip uninstall ', '').strip()
                safe_uninstall(target_package, custom_packages_dir)

            elif stripped.startswith('python '):
                run_pending_python()

                try:
                    cmd_parts = shlex.split(line)
                except ValueError:
                    cmd_parts = line.split()

                if len(cmd_parts) > 1:
                    file_path = cmd_parts[1]
                    script_args = cmd_parts[1:]
                    old_argv = sys.argv
                    sys.argv = script_args
                    try:
                        with open(file_path, 'r', encoding='utf-8') as f:
                            exec(f.read(), globals_dict)
                    finally:
                        sys.argv = old_argv
            else:
                current_python_chunk.append(line)

        run_pending_python()

    except Exception:
        traceback.print_exc(file=sys.stdout)
    finally:
        sys.stdout = old_stdout
        sys.stderr = old_stderr
        sys.stdin = old_stdin
