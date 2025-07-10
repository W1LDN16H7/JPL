# 💥 JPL - JSON Programming Language

🚀 Welcome to the world’s most ridiculous (and awesome) way to write code in JSON. Yes, you read that right. JSON. As code. Not just for configs or not just for data. But as a ridiculous programming language.

# 👾What is JPL?


> ```json
> {
>   "language": "🧠 JPL (JSON as Programming Language) is what happens when you stare at too many curly braces and think, “What if this was a real language?”",
>   "type": "Fun JSON-Based Programming Language",
>   "status": "🔥 Experimental, Minimal, For geeks",
>   "features": [
>     "✅ Pure JSON syntax",
>     "🧩 Print stuff (yes, really)",
>     "📦 Make variables like it’s 1999",
>     "📜 Write loops, if/else, and functions",
>     "🚀 REPL + CLI",
>     "🛠️ Call system functions",
>     "🎭 Pretend you’re living in a JSON utopia",
>     "🧪 All in JSON. No kidding.",
>     "🧙 Are you afraid of braces? You should be.",
>     "🧪 Experimental features like functions, loops, and conditionals"
>   ],
>   "warning": "⚠️ If you're afraid of curly braces, step away!"
> }
> ```



---

⚠️ **Warning for the Bracket-Phobic**

⚠️ **🐍 Python devs, beware!**
> _If curly braces `{}` or square brackets `[]` make you sweat, this JSON jungle isn’t for you!_

> _If you're afraid of `{curly braces}` or `square brackets[]`, turn back now._

> JPL is **99.9% JSON**, **0.1% mercy**.  

> Touch it only if you're brave enough to stare into the abyss of `{}` and live to tell the tale.

---

## 📦 Installation

### The Easy Way (Windows)
- Download the latest [JPL.exe](https://github.com/W1LDN16H7/JPL/releases/download/v1.0.0/jpl.exe)
- Double-click it, or run from terminal:
  ```sh
  jpl.exe --help
  ```

### The Geek Way (Build from Source)
1. Clone this chaotic project:
   ```sh
   git clone https://github.com/W1LDN16H7/JPL.git
   cd jpl
   ```
2. Build it with Maven:
   ```sh
   mvn clean package
   ```
3. Run it:
   ```sh
   java -jar target/JPL-1.0-SNAPSHOT.jar
   ```
4. Build executable with GraalVM (Windows):
   ```sh
    .\native-image -cp picocli-4.7.7.jar  -jar jpl.jar
   ```


---

### Or use the pre-built JAR:
- Download the latest [JPL.jar](https://github.com/W1LDN16H7/JPL/releases/download/v1.0.0/jpl.jar)

## 🎮 Try it out

### REPL mode (yes, it has a REPL 😱):
```sh
jpl.exe or java -jar jpl.jar
```

### Run a JPL program:
```sh
jpl.exe run examples/func.jpl
```

### Compile JPL to real Java code:
```sh
jpl.exe compile may be in the future 
```

### Print help + load some samples:
```sh
jpl.exe help loop
```

---

## 📖 Sample JPL Code (Yes, this is code)

```json
[
  { "let": { "name": "JSON", "times": 3 } },
  {
    "for": {
      "var": "i",
      "from": 1,
      "to": "times",
      "do": [
        { "print": { "add": ["Hello ", "name"] } }
      ]
    }
  }
]
```

**Output:**
```
Hello JSON
Hello JSON
Hello JSON
```

---

## 🦄 Mega Example

Save this as `megaExample.jpl` and run `jpl.exe run megaExample.jpl`:

```json
[
  { "import": "system.jpl" },
  { "print": { "call": { "now": [] } } },
  { "print": { "call": { "osName": [] } } },
  { "print": { "call": { "cpuCount": [] } } },
  { "let": { "a": 9, "b": 4, "name": "JPL" } },
  { "print": { "add": ["\"a + b = \"", { "add": ["a", "b"] }] } },
  { "print": { "mul": ["a", "b"] } },
  { "print": { "div": ["a", "b"] } },
  { "print": { "mod": ["a", "b"] } },
  { "print": { "&": [6, 3] } },
  { "print": { "||": [false, true] } },
  { "print": { "gt": ["a", "b"] } },
  { "def": { "sq": { "params": ["x"], "body": [{ "return": { "mul": ["x", "x"] } }] } } },
  { "print": { "call": { "sq": [5] } } },
  { "def": { "greet": { "params": ["who"], "body": [{ "return": { "add": ["\"Hi, \"", "who"] } }] } } },
  { "print": { "call": { "greet": ["\"JSON Fan\""] } } },
  { "if": { "cond": { "<": ["b", "a"] }, "then": { "print": "\"b < a 👍\"" }, "else": { "print": "\"b >= a 🤔\"" } } },
  { "for": { "var": "i", "from": 1, "to": 3, "step": 1, "do": [ { "print": { "call": { "sq": ["i"] } } } ] } },
  { "print": "\"All features in one! 🎉\"" }
]
```

**Expected Output:**
```
2025-07-09T15:30:00.123
Windows 10
8
"a + b = 13.0"
36.0
2.25
1.0
2
true
true
25
Hi, JSON Fan
b < a 👍
1
4
9
All features in one! 🎉
```

**Example**
---
```
[
// 🚀 Welcome to JPL — JSON Programming Language!

// Import system utilities for fun stuff
{ "import": "system.jpl" },

// Print system info
{ "print": { "call": { "now": [] } } },
{ "print": { "call": { "osName": [] } } },
{ "print": { "call": { "cpuCount": [] } } },

// Define a math function to square a number
{
  "def": {
    "square": {
      "params": ["x"],
      "body": [
        { "return": { "mul": ["x", "x"] } }
      ]
    }
  }
},

// Greet a user
{
  "def": {
    "greet": {
      "params": ["name"],
      "body": [
        { "return": { "add": ["Hello, ", "name"] } }
      ]
    }
  }
},

// Declare variables
{ "let": { "a": 7, "user": "Kapil" } },

// Use greet function and print
{ "print": { "call": { "greet": ["user"] } } },

// Conditional message
{
  "if": {
    "cond": { ">": ["a", 5] },
    "then": { "print": "a is greater than 5" },
    "else": { "print": "a is 5 or less" }
  }
},

// Loop with break and continue
{
  "for": {
    "var": "i",
    "from": 1,
    "to": 10,
    "step": 1,
    "do": [
      { "if": { "cond": { "eq": ["i", 3] }, "then": { "continue": true } } },
      { "if": { "cond": { "gt": ["i", 7] }, "then": { "break": true } } },
      { "print": { "call": { "square": ["i"] } } }
    ]
  }
},

// Fun ending message
{ "print": "🎉 Done with curly braces and JSON fun!" }
]
```


## 📚 Examples & Help Topics

Type:
- `jpl.exe help basics` → Intro & let/print
- `jpl.exe help math` → Arithmetic & logic
- `jpl.exe help if` → Conditionals
- `jpl.exe help loop` → For-loops & controls
- `jpl.exe help func` → Functions & returns
- `jpl.exe help system` → System library

Or explore the `/examples` directory for ready-to-run scripts:

- `a1.jpl` – Example script 1
- `a2.jpl` – Example script 2
- `basic.jpl` – Basic syntax and printing
- `bmath.jpl` – More math operations
- `cmt.jpl` – Comments usage
- `cond.jpl` – Advanced conditionals
- `const.jpl` – Constants
- `func.jpl` – Functions and calls
- `if.jpl` – If/else conditionals
- `loop.jpl` – While loops
- `math.jpl` – Math operations

To run all examples at once (Windows):
```sh
examples/run_all_examples.bat
```
Or run individually:
```sh
jpl.exe run examples/if.jpl
```

---

## 🧩 Built-in Libs (aka JPL "Standard Library")

You can import libs like this:
```json
{ "import": "system.jpl" }
```
Available libraries:
- `system.jpl` ✅ → OS info, time
- `string.jpl` ❌ → String tricks
- `math.jpl` ❌ → Power math
- `func.jpl` ❌ → Sample functions
- `loop.jpl` ❌ → Loop demo
- `basics.jpl` ❌ → Print & let stuff
---

## 🎯 Who is this for?
- JSON geeks who want to troll JavaScript
- Devs who love abuse of data formats
- Educators teaching ASTs / interpreters
- Hackers who want a new esolang
- People who just love chaos. 🧨

---

## 🔧 Future Features (If We’re Bored)
- File IO (read JSON while writing JSON 😵)
- JPL → JVM bytecode (because why not)
- Web-based playground (coming… maybe)
- JPL transpiler to TypeScript, Python, Morse code

---

## 🤝 Contribute & Fork
- Fork on GitHub: https://github.com/W1LDN16H7/JPL
- Add your own .jpl libraries in lib/
- Create VSCode syntax plugins or web playgrounds
- Write issues for bugs, features, or just to say hi
- PRs: new features, docs, examples
- Add your own JPL scripts to examples/
- Write posts about your JPL adventures on X with #jpl
- Fix/Add NEW things in README.md, examples, or code
- Star the project and share it need your support to keep the madness alive!

[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-FFDD00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/kapil7.kumar)

---

## 📜 License & Thanks

MIT License — code it, break it, share it.
Built for JSON geeks who dare to dream in braces. ❤️

---

**Made with ❤️ and pure nonsense by Kapil.**

**Have fun, cause chaos, JPL forever! 🎊**


