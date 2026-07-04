import os
from pip._internal import main as pipmain


def install_package(package_name):
    try:
        exit_code = pipmain(['install', package_name])
        if exit_code == 0:
            return f"Success: {package_name} installed successfully!"
        return f"Error: pip exited with code {exit_code}"
    except Exception as e:
        return f"Exception occurred: {str(e)}"
