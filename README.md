<p align="center">
  <img src="screenshots/DTerminalTheme.png" alt="Dterminal Theme" width="100%">
</p>

<p align="center">
  <img src="https://img.shields.io/github/v/release/dedeadend/DTerminal?style=for-the-badge&color=green&logo=android" alt="Release Badge"/>
  <img src="https://img.shields.io/github/downloads/dedeadend/DTerminal/total?style=for-the-badge&color=orange&logo=github" alt="Downloads Badge"/>
  <img src="https://api.visitorbadge.io/api/visitors?path=dedeadend%2Fdterminal&label=Views&countColor=blue" alt="Views Badge"/>
  <img src="https://img.shields.io/github/license/dedeadend/DTerminal?style=for-the-badge&color=yellow" alt="License Badge"/>
</p>


# 🍃 DTerminal

Forget clunky and dated terminals. DTerminal is a modern, ultra-lightweight **terminal emulator** and **Python scripting workspace** for **Android**, built entirely with **Jetpack Compose**. It seamlessly unifies asynchronous **standard/root shells** with an embedded **Python 3.13 runtime**, offering a powerful and reactive environment for **automation** and advanced scripting on a **highly responsive user interface**.


## ✨ Features

- 💯 **Full Shell Authority**: High-speed, isolated execution for both standard (`sh`) and root (`su`) commands.
- 🐍 **Embedded Python 3.13 Engine**: An offline Python runtime powered by Chaquopy, featuring native cryptography C-extensions. Ready for advanced security tools, SSH automation, and cryptographic scripting.
- 🫧 **Multi-line Batch Execution**: Combine, edit, and execute multiple related shell or Python operations sequentially in a single process run.
- 📝 **Workspace Script Manager**: Save, organize, and quickly recall your frequently used custom automation scripts without repetitive typing.
- 📜 **Persistent Command History**: Instant access to your previously executed shell and Python commands in a clean, scrollable log layout.
- 🪄 **Magic 'py' Interpreter Mode**: Type `py` on the very first line of your input block to instantly switch the entire execution engine into a pure Python environment for all subsequent lines.
- 📦 **Runtime Pip Package Manager**: Download, extract, and update pure Python packages (`none-any.whl`) directly from PyPI on the fly.
- 🔗 **Advanced Argument Parsing**: Execute local scripts seamlessly with standard terminal syntax, including full support for custom inline CLI arguments and quote strings.
- 🎨 **Real-time Modern UI**: Instant dynamic customization of font size and multi-layered typography colors.


## 📸 Screenshots

| Scripts | History |
| :---: | :---: |
| <img src="screenshots/DTerminalScript.png" alt="Dterminal Script"> | <img src="screenshots/DTerminalHistory.png" alt="Dterminal History"> |



## 📥 Getting Started

### Prerequisites

- Android 11.0+ (API 30) or higher
- (Optional) Root access (Magisk / KernelSU / APatch) for root privilege commands

### Installation

1. Download the latest released APK from the [Releases Page](https://github.com/dedeadend/DTerminal/releases/latest).
2. Install downloaded APK file.
3. Enjoy 💚


## 📌 Note

Since DTerminal is a self-signed APK not distributed via the Google Play Store, Google Play Protect may flag it as "Unknown". As an open-source project, you can always audit the source code yourself or build the APK from source to ensure total transparency.


## ⚙️ Custom Commands Reference

DTerminal extends standard shell capabilities with a built-in suite of specialized utility subsystems:

### System Commands
| Command | Usage | Description |
|:---|:---|:---|
| `help` | `help` | Show this command list documentation |
| `about` | `about` | Display app and developer info |
| `clear` / `cls` | `clear` / `cls` | Clear all terminal console logs |
| `sysinfo` | `sysinfo` | Advanced hardware & OS details |
| `uptime` | `uptime` | Show system boot duration |
| `datetime` | `datetime` | Display current date & time |
| `sudo` | `sudo [cmd]` | Simulate root privilege command execution |

### Customization Commands
| Command | Usage | Description |
|:---|:---|:---|
| `font` | `font [size]` | Set terminal font size (5-25) |
| `font def` | `font def` | Reset font size to default (11) |
| `color1` | `color1 [r g b]` | Set Normal text color using RGB values |
| `color1 def` | `color1 def` | Reset Normal text color to default |
| `color2` | `color2 [r g b]` | Set Error text color using RGB values |
| `color2 def` | `color2 def` | Reset Error text color to default |
| `color3` | `color3 [r g b]` | Set Info text color using RGB values |
| `color3 def` | `color3 def` | Reset Info text color to default |

### Text Processing Utilities
| Command | Usage | Description |
|:---|:---|:---|
| `random` | `random [a] [b]` | Generate number between a and b |
| `uuid` | `uuid` | Generate a random UUID v4 string |
| `length` | `length [text]` | Count characters in a string |
| `case` | `case [up/low] [text]` | Convert text to upper/lowercase |
| `wordcount` | `wordcount [text]` | Count words in the given text |
| `sort` | `sort [word1] [word2] ...` | Sort list of words alphabetically |
| `shuffle` | `shuffle [word1] [word2] ...` | Randomly shuffle list of words |
| `reverse` | `reverse [text]` | Reverse character order of text |
| `palindrome`| `palindrome [text]` | Check if text is a palindrome |
| `regex` | `regex [pattern] [text]` | Find regex matches within text |

### Crypto & Encoding Tools
| Command | Usage | Description |
|:---|:---|:---|
| `base64` | `base64 [enc/dec] [text]` | Encode or decode Base64 strings |
| `hash` | `hash [algo] [text]` | Generate md5, sha1, sha256 checksums |
| `url` | `url [enc/dec] [text]` | Encode or decode URL components |
| `rot13` | `rot13 [text]` | Apply ROT13 cipher to text |
| `morse` | `morse [enc/dec] [text]` | Encode or decode Morse code |
| `binary` | `binary [enc/dec] [text]` | Convert text to/from binary stream |
| `hex` | `hex [enc/dec] [text]` | Convert text to/from hex string |
| `ascii` | `ascii [single char]` | Get decimal ASCII code of a char |

### Developer Utilities
| Command | Usage | Description |
|:---|:---|:---|
| `pass` | `pass [length]` | Generate secure random password |
| `json` | `json [validate/format] [txt]` | Validate or pretty-print JSON strings |

### Python Engine Subsystem
| Command | Usage | Description |
|:---|:---|:---|
| `py` | Line-1 execution trigger | Switch execution engine block to interactive Python mode |
| `python` | `python [file_path] [args...]` | Execute a local script from storage with CLI args & quotes |
| `pip install`| `pip install [package]` | Download & install a Pure Python package from PyPI |
| `pip uninstall`| `pip uninstall [package]`| Completely remove an installed package from runtime environment |
| `pip list` | `pip list` | List all user-installed Python packages |


## 🛠 Tech Stack

- **UI**: Jetpack Compose (Material 3 Adaptive Design)
- **Architecture**: MVI (Model-View-Intent) + Clean Architecture + Unidirectional Data Flow (UDF)
- **Concurrency**: Kotlin Coroutines & Flow
- **Python Subsystem**: Chaquopy (Python 3.13 Runtime Embedder)
- **Dependency Injection**: Hilt
- **Database**: Room
- **Build System**: Gradle (Kotlin DSL)


## 🤝 Contributing

Contributions make the open-source community an amazing place to learn, inspire, and create. Any contributions you make are greatly appreciated.

1. **Fork** the Project
2. Create your Feature Branch  
   `git checkout -b feature/AmazingFeature`
3. **Commit** your Changes  
   `git commit -m 'Add some AmazingFeature'`
4. **Push** to the Branch  
   `git push origin feature/AmazingFeature`
5. Open a **Pull Request**


## ♠️ Support

Have questions or need help? Feel free to reach out:

<div align="left">
  <a href="https://t.me/dedeadend" target="_blank">
    <img src="https://img.shields.io/static/v1?message=Telegram&logo=telegram&label=&color=2CA5E0&logoColor=white&labelColor=&style=for-the-badge" height="30" alt="telegram logo"  />
  </a>
  <!-- <a href="https://www.linkedin.com/in/dedeadend" target="_blank">
    <img src="https://img.shields.io/static/v1?message=LinkedIn&logo=linkedin&label=&color=0077B5&logoColor=white&labelColor=&style=for-the-badge" height="30" alt="linkedin logo"  />
  </a> -->
</div>


## ❤️ Donation

If you find this project helpful, please give it a ⭐

You can also support the development by buying me a coffee:

<div align="left">
  <a href="https://nowpayments.io/donation/dedeadend" target="_blank" rel="noreferrer noopener">
     <img src="https://nowpayments.io/images/embeds/donation-button-black.svg" height="40" alt="Crypto donation button by NOWPayments">
  </a>
</div>


## ⚖️ License

Distributed under the GPL-3.0 License. See [LICENSE](LICENSE) for more information.


---


<div align="center">
  Developed with 💚 by <a href="https://github.com/dedeadend">DeDeadend</a>
</div>
