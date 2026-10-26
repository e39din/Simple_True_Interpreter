# Simple True Interpreter — samples

Language version: **0.11.0**. All functions — with `:`. Chains `=>` allow shortening code. **A name must be declared before use** — `= name` (empty container) or `value = name`. Examples work **in the middle of a loop** — termination (`=> :stop`) is not shown.

## How to read

For each sample:
- **True** — the sample
- **Python** — the same in Python
- **Elixir** — the same in Elixir
- **+1 by topic** — one more language
- **`<details>`** — other 8 languages

`::before` and `::after` blocks — setup/teardown for a sample.

## Table of contents

1. [Algorithmic](#algo)
2. [CI / ETL / embedded](#ci)
3. [Agents](#agents)
4. [Destruction](#destroy)
5. [Data](#data)
6. [JSON](#json)
7. [Conditions and jumps](#cond)
8. [Assert](#assert)
9. [Functional](#func)
10. [Recursion via stack](#rec)
11. [Games](#games)
12. [Game designer: RPG balance](#balance)
13. [UI automation](#ui)
14. [Search](#search)

---

## <a name="algo"></a>1. Algorithmic

### 1. Sum of array

**Sums all numbers in a container.**

```true
1, 2, 3, 4, 5 => :sum = result
```
**Result:** `result = "15"`

**Python:**
```python
result = sum([1, 2, 3, 4, 5])
```

**Elixir:**
```elixir
result = [1, 2, 3, 4, 5] |> Enum.sum()
```

**Clojure** (topic: collections):
```clojure
(def result (reduce + [1 2 3 4 5]))
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val result = listOf(1, 2, 3, 4, 5).sum()
```

**Rust:**
```rust
let result: i32 = [1, 2, 3, 4, 5].iter().sum();
```

**Haskell:**
```haskell
let result = sum [1, 2, 3, 4, 5]
```

**Lua:**
```lua
local result = 1 + 2 + 3 + 4 + 5
```

**Erlang:**
```erlang
Result = lists:sum([1, 2, 3, 4, 5]).
```

**JavaScript:**
```javascript
const result = [1, 2, 3, 4, 5].reduce((x, y) => x + y, 0);
```

**Go:**
```go
result := 0
for _, v := range []int{1, 2, 3, 4, 5} { result += v }
```

**C:**
```c
int result = 0;
int arr[] = {1, 2, 3, 4, 5};
for (int i = 0; i < 5; i++) result += arr[i];
```

**Bash:**
```bash
result=0
for n in 1 2 3 4 5; do result=$((result + n)); done
```

</details>

---

### 2. Maximum

**Finds the maximum value.**

```true
3, 7, 2, 9, 4 => :max = result
```
**Result:** `result = "9"`

**Python:**
```python
result = max([3, 7, 2, 9, 4])
```

**Elixir:**
```elixir
result = [3, 7, 2, 9, 4] |> Enum.max()
```

**Clojure** (topic: collections):
```clojure
(def result (apply max [3 7 2 9 4]))
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val result = listOf(3, 7, 2, 9, 4).maxOrNull()
```

**Rust:**
```rust
let result = [3, 7, 2, 9, 4].iter().max();
```

**Haskell:**
```haskell
let result = maximum [3, 7, 2, 9, 4]
```

**Lua:**
```lua
local result = math.max(3, 7, 2, 9, 4)
```

**Erlang:**
```erlang
Result = lists:max([3, 7, 2, 9, 4]).
```

**JavaScript:**
```javascript
const result = Math.max(...[3, 7, 2, 9, 4]);
```

**Go:**
```go
result := 3
for _, v := range []int{7, 2, 9, 4} { if v > result { result = v } }
```

**C:**
```c
int result = 3;
int arr[] = {3, 7, 2, 9, 4};
for (int i = 1; i < 5; i++) if (arr[i] > result) result = arr[i];
```

**Bash:**
```bash
result=3
for n in 7 2 9 4; do [ "$n" -gt "$result" ] && result=$n; done
```

</details>

---

### 3. Hello, world

**Assigns a string to a container.**

```true
Hello, world = result
```
**Result:** `result = "Hello, world"`

**Python:**
```python
result = "Hello, world"
```

**Elixir:**
```elixir
result = "Hello, world"
```

**JavaScript** (topic: strings):
```javascript
const result = "Hello, world";
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val result = "Hello, world"
```

**Rust:**
```rust
let result = "Hello, world";
```

**Haskell:**
```haskell
let result = "Hello, world"
```

**Lua:**
```lua
local result = "Hello, world"
```

**Erlang:**
```erlang
Result = "Hello, world".
```

**Go:**
```go
result := "Hello, world"
```

**Clojure:**
```clojure
(def result "Hello, world")
```

**C:**
```c
const char *result = "Hello, world";
```

**Bash:**
```bash
result="Hello, world"
```

</details>

---

### 4. Palindrome

**Checks if a string is a palindrome (first char == last char).**

```true
racecar = s
s:x:1 = a
s:x:7 = b
= result
a == b ? 1 = result
```
**Result:** `result = "1"`

**Python:**
```python
s = "racecar"
a = s[0]
b = s[-1]
result = 1 if a == b else 0
```

**Elixir:**
```elixir
s = "racecar"
a = String.first(s)
b = String.last(s)
result = if a == b, do: 1, else: 0
```

**JavaScript** (topic: strings):
```javascript
const s = "racecar";
const a = s[0];
const b = s[s.length - 1];
const result = a === b ? 1 : 0;
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val s = "racecar"
val a = s.first()
val b = s.last()
val result = if (a == b) 1 else 0
```

**Rust:**
```rust
let s = "racecar";
let a = s.chars().next().unwrap();
let b = s.chars().last().unwrap();
let result = if a == b { 1 } else { 0 };
```

**Haskell:**
```haskell
let s = "racecar"
let a = head s
let b = last s
let result = if a == b then 1 else 0
```

**Lua:**
```lua
local s = "racecar"
local a = s:sub(1,1)
local b = s:sub(-1)
local result = (a == b) and 1 or 0
```

**Erlang:**
```erlang
S = "racecar",
A = hd(S),
B = lists:last(S),
Result = case A == B of true -> 1; false -> 0 end.
```

**Go:**
```go
s := "racecar"
a := s[0]
b := s[len(s)-1]
result := 0
if a == b { result = 1 }
```

**Clojure:**
```clojure
(let [s "racecar"
      a (first s)
      b (last s)]
  (def result (if (= a b) 1 0)))
```

**C:**
```c
const char *s = "racecar";
char a = s[0];
char b = s[strlen(s) - 1];
int result = (a == b) ? 1 : 0;
```

**Bash:**
```bash
s="racecar"
a="${s:0:1}"
b="${s: -1}"
[ "$a" = "$b" ] && result=1 || result=0
```

</details>

---

### 5. Count lines

**Counts lines in a file.**

```true
input.txt => :get = raw
raw:l => :size = lineCount
```
**Result:** `lineCount = "<number>"`

**Python:**
```python
raw = open("input.txt").read()
line_count = len(raw.splitlines())
```

**Elixir:**
```elixir
{:ok, raw} = File.read("input.txt")
line_count = raw |> String.split("\n") |> length()
```

**Bash** (topic: files):
```bash
line_count=$(wc -l < input.txt)
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val raw = java.io.File("input.txt").readText()
val lineCount = raw.lines().size
```

**Rust:**
```rust
let raw = std::fs::read_to_string("input.txt")?;
let line_count = raw.lines().count();
```

**Haskell:**
```haskell
raw <- readFile "input.txt"
let lineCount = length (lines raw)
```

**Lua:**
```lua
local f = io.open("input.txt", "r")
local raw = f:read("*a")
f:close()
local _, line_count = raw:gsub("\n", "\n")
line_count = line_count + 1
```

**Erlang:**
```erlang
{ok, Bin} = file:read_file("input.txt"),
LineCount = length(string:split(binary_to_list(Bin), "\n", all)).
```

**JavaScript:**
```javascript
const raw = require("fs").readFileSync("input.txt", "utf8");
const lineCount = raw.split("\n").length;
```

**Go:**
```go
raw, _ := os.ReadFile("input.txt")
lineCount := strings.Count(string(raw), "\n") + 1
```

**Clojure:**
```clojure
(def raw (slurp "input.txt"))
(def line-count (count (clojure.string/split-lines raw)))
```

**C:**
```c
FILE *f = fopen("input.txt", "r");
int line_count = 0; char c;
while ((c = fgetc(f)) != EOF) if (c == '\n') line_count++;
fclose(f);
```

</details>

---

### 6. Reverse string

**Reverses a string.**

```true
hello => :reverse = result
```
**Result:** `result = "olleh"`

**Python:**
```python
result = "hello"[::-1]
```

**Elixir:**
```elixir
result = String.reverse("hello")
```

**JavaScript** (topic: strings):
```javascript
const result = [..."hello"].reverse().join("");
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val result = "hello".reversed()
```

**Rust:**
```rust
let result: String = "hello".chars().rev().collect();
```

**Haskell:**
```haskell
let result = reverse "hello"
```

**Lua:**
```lua
local result = string.reverse("hello")
```

**Erlang:**
```erlang
Result = lists:reverse("hello").
```

**Go:**
```go
r := []rune("hello")
for i, j := 0, len(r)-1; i < j; i, j = i+1, j-1 { r[i], r[j] = r[j], r[i] }
result := string(r)
```

**Clojure:**
```clojure
(def result (clojure.string/reverse "hello"))
```

**C:**
```c
/* manual reverse */
```

**Bash:**
```bash
result=$(echo -n "hello" | rev)
```

</details>

---

### 6b. Length via `:s`

**Returns the length of a string.**

```true
hello => :s = result
```
**Result:** `result = "5"`

**Python:**
```python
result = len("hello")
```

**Elixir:**
```elixir
result = String.length("hello")
```

**C** (topic: strings):
```c
int result = strlen("hello");
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val result = "hello".length
```

**Rust:**
```rust
let result = "hello".len();
```

**Haskell:**
```haskell
let result = length "hello"
```

**Lua:**
```lua
local result = #"hello"
```

**Erlang:**
```erlang
Result = length("hello").
```

**JavaScript:**
```javascript
const result = "hello".length;
```

**Go:**
```go
result := len("hello")
```

**Clojure:**
```clojure
(def result (count "hello"))
```

**Bash:**
```bash
result=${#hello}
```

</details>

---

## <a name="ci"></a>2. CI / ETL / embedded

### 7. Smoke test API

**Calls an API and checks if the response contains "ok".**

```true
https://api.example.com/health => :get = response
response, ok => :contains = ok_found
ok_found = result
```
**Result:** `result = "1"` if response contains "ok"

**Python:**
```python
import requests
response = requests.get("https://api.example.com/health").text
ok_found = "1" if "ok" in response else "0"
result = ok_found
```

**Elixir:**
```elixir
{:ok, resp} = HTTPoison.get("https://api.example.com/health")
ok_found = if String.contains?(resp.body, "ok"), do: "1", else: "0"
result = ok_found
```

**Bash** (topic: shell):
```bash
response=$(curl -s https://api.example.com/health)
echo "$response" | grep -q ok && ok_found=1 || ok_found=0
result=$ok_found
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val response = java.net.URL("https://api.example.com/health").readText()
val okFound = if (response.contains("ok")) "1" else "0"
val result = okFound
```

**Rust:**
```rust
let response = reqwest::get("https://api.example.com/health").await?.text().await?;
let ok_found = if response.contains("ok") { "1" } else { "0" };
let result = ok_found;
```

**Haskell:**
```haskell
import Network.HTTP.Simple
response <- getResponseBody <$> httpLBS "https://api.example.com/health"
let okFound = if "ok" `isInfixOf` response then "1" else "0"
let result = okFound
```

**Lua:**
```lua
local http = require("socket.http")
local response = http.request("https://api.example.com/health")
local ok_found = string.find(response, "ok") and "1" or "0"
local result = ok_found
```

**Erlang:**
```erlang
{ok, {{_, 200, _}, _, Body}} = httpc:request("https://api.example.com/health"),
OkFound = case string:str(Body, "ok") of 0 -> "0"; _ -> "1" end,
Result = OkFound.
```

**JavaScript:**
```javascript
const response = await (await fetch("https://api.example.com/health")).text();
const okFound = response.includes("ok") ? "1" : "0";
const result = okFound;
```

**Go:**
```go
resp, _ := http.Get("https://api.example.com/health")
body, _ := io.ReadAll(resp.Body)
okFound := "0"
if strings.Contains(string(body), "ok") { okFound = "1" }
result := okFound
```

**Clojure:**
```clojure
(def response (:body (http/get "https://api.example.com/health")))
(def ok-found (if (clojure.string/includes? response "ok") "1" "0"))
(def result ok-found)
```

**C:**
```c
/* via libcurl */
```

</details>

---

### 8. Embedded calculator

**Calculates a simple expression.**

```true
3 = a
4 = b
a + b * 2 => :math = result
```
**Result:** `result = "11"`

**Python:**
```python
a = 3
b = 4
result = a + b * 2
```

**Elixir:**
```elixir
a = 3
b = 4
result = a + b * 2
```

**C** (topic: embedded):
```c
int a = 3;
int b = 4;
int result = a + b * 2;
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val a = 3
val b = 4
val result = a + b * 2
```

**Rust:**
```rust
let a = 3;
let b = 4;
let result = a + b * 2;
```

**Haskell:**
```haskell
let a = 3
let b = 4
let result = a + b * 2
```

**Lua:**
```lua
local a = 3
local b = 4
local result = a + b * 2
```

**Erlang:**
```erlang
A = 3,
B = 4,
Result = A + B * 2.
```

**JavaScript:**
```javascript
const a = 3;
const b = 4;
const result = a + b * 2;
```

**Go:**
```go
a := 3
b := 4
result := a + b*2
```

**Clojure:**
```clojure
(def a 3)
(def b 4)
(def result (+ a (* b 2)))
```

**Bash:**
```bash
a=3
b=4
result=$((a + b * 2))
```

</details>

---

### 9. ETL count CSV lines

**Reads a CSV, counts lines.**

```true
input.csv => :get = raw
raw:l => :size = rows
```
**Result:** `rows = "<number>"`

**Python:**
```python
raw = open("input.csv").read()
rows = len(raw.splitlines())
```

**Elixir:**
```elixir
{:ok, raw} = File.read("input.csv")
rows = raw |> String.split("\n") |> length()
```

**Bash** (topic: shell):
```bash
rows=$(wc -l < input.csv)
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val raw = java.io.File("input.csv").readText()
val rows = raw.lines().size
```

**Rust:**
```rust
let raw = std::fs::read_to_string("input.csv")?;
let rows = raw.lines().count();
```

**Haskell:**
```haskell
raw <- readFile "input.csv"
let rows = length (lines raw)
```

**Lua:**
```lua
local f = io.open("input.csv", "r")
local raw = f:read("*a"); f:close()
local _, rows = raw:gsub("\n", "\n"); rows = rows + 1
```

**Erlang:**
```erlang
{ok, Bin} = file:read_file("input.csv"),
Rows = length(string:split(binary_to_list(Bin), "\n", all)).
```

**JavaScript:**
```javascript
const raw = require("fs").readFileSync("input.csv", "utf8");
const rows = raw.split("\n").length;
```

**Go:**
```go
raw, _ := os.ReadFile("input.csv")
rows := strings.Count(string(raw), "\n") + 1
```

**Clojure:**
```clojure
(def raw (slurp "input.csv"))
(def rows (count (clojure.string/split-lines raw)))
```

**C:**
```c
FILE *f = fopen("input.csv", "r");
int rows = 0; char c;
while ((c = fgetc(f)) != EOF) if (c == '\n') rows++;
fclose(f);
```

</details>

---

## <a name="agents"></a>3. Agents

### 10. Agent jump to file

**Jumps the agent to another file (no reset).**

```true
^ other.cycle
```

**Python:**
```python
agent.file_name = "other.cycle"
agent.current_step = 1
```

**Elixir:**
```elixir
state = %{state | file: "other.cycle", step: 1}
```

**Erlang** (topic: concurrency):
```erlang
NewState = State#state{file = "other.cycle", step = 1}.
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
agent.fileName = "other.cycle"
agent.currentStep = 1
```

**Rust:**
```rust
agent.file_name = "other.cycle".to_string();
agent.current_step = 1;
```

**Haskell:**
```haskell
let agent' = agent { fileName = "other.cycle", currentStep = 1 }
```

**Lua:**
```lua
agent.file_name = "other.cycle"
agent.current_step = 1
```

**JavaScript:**
```javascript
agent.fileName = "other.cycle";
agent.currentStep = 1;
```

**Go:**
```go
agent.FileName = "other.cycle"
agent.CurrentStep = 1
```

**Clojure:**
```clojure
(def agent' (assoc agent :file-name "other.cycle" :current-step 1))
```

**C:**
```c
strcpy(agent.file_name, "other.cycle");
agent.current_step = 1;
```

**Bash:**
```bash
# not idiomatic
```

</details>

---

### 11. Agent stop

**Stops the agent.**

```true
^ :stop
```

**Python:**
```python
raise SystemExit
```

**Elixir:**
```elixir
exit(:normal)
```

**Erlang** (topic: concurrency):
```erlang
exit(self(), normal).
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
Thread.currentThread().interrupt()
```

**Rust:**
```rust
return;
```

**Haskell:**
```haskell
throwTo self ThreadKilled
```

**Lua:**
```lua
coroutine.close(co)
```

**JavaScript:**
```javascript
return;
```

**Go:**
```go
return
```

**Clojure:**
```clojure
(System/exit 0)
```

**C:**
```c
pthread_exit(NULL);
```

**Bash:**
```bash
exit 0
```

</details>

---

### 12. Agent delete

**Deletes the agent.**

```true
^ :delete
```

**Python:**
```python
del agents[name]
```

**Elixir:**
```elixir
Agent.stop(:"main")
```

**Erlang** (topic: concurrency):
```erlang
exit(Pid, kill).
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
agents.remove(name)
```

**Rust:**
```rust
agents.remove(&name);
```

**Haskell:**
```haskell
modifyIORef agents (Map.delete name)
```

**Lua:**
```lua
agents[name] = nil
```

**JavaScript:**
```javascript
delete agents[name];
```

**Go:**
```go
delete(agents, name)
```

**Clojure:**
```clojure
(swap! agents dissoc name)
```

**C:**
```c
/* remove from array */
```

**Bash:**
```bash
unset agents["$name"]
```

</details>

---

### 13. Agent pause

**Pauses the agent.**

```true
^ :pause
```

**Python:**
```python
pause_event.clear()
```

**Elixir:**
```elixir
:sys.suspend(:main)
```

**Erlang** (topic: concurrency):
```erlang
sys:suspend(MainPid).
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
agent.status = Status.PAUSED
```

**Rust:**
```rust
agent.status = Status::Paused;
```

**Haskell:**
```haskell
writeIORef agentStatus Paused
```

**Lua:**
```lua
agent.status = "PAUSED"
```

**JavaScript:**
```javascript
agent.status = "PAUSED";
```

**Go:**
```go
agent.Status = Paused
```

**Clojure:**
```clojure
(swap! agent assoc :status :paused)
```

**C:**
```c
agent.status = PAUSED;
```

**Bash:**
```bash
kill -STOP "$pid"
```

</details>

---

### 14. Agent resume

**Resumes the agent.**

```true
^ :resume
```

**Python:**
```python
pause_event.set()
```

**Elixir:**
```elixir
:sys.resume(:main)
```

**Erlang** (topic: concurrency):
```erlang
sys:resume(MainPid).
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
agent.status = Status.RUNNING
```

**Rust:**
```rust
agent.status = Status::Running;
```

**Haskell:**
```haskell
writeIORef agentStatus Running
```

**Lua:**
```lua
agent.status = "RUNNING"
```

**JavaScript:**
```javascript
agent.status = "RUNNING";
```

**Go:**
```go
agent.Status = Running
```

**Clojure:**
```clojure
(swap! agent assoc :status :running)
```

**C:**
```c
agent.status = RUNNING;
```

**Bash:**
```bash
kill -CONT "$pid"
```

</details>

---

### 15. Agent reset

**Resets the file.**

```true
^ :reset
```

**Python:**
```python
file.code = start_code
agent.current_step = 1
```

**Elixir:**
```elixir
file = %{file | code: start_code}
agent = %{agent | step: 1}
```

**JavaScript** (topic: state):
```javascript
file.code = startCode;
agent.currentStep = 1;
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
tab.code = startCode
agent.currentStep = 1
```

**Rust:**
```rust
tab.code = start_code.clone();
agent.current_step = 1;
```

**Haskell:**
```haskell
let tab' = tab { code = startCode, currentStep = 1 }
```

**Lua:**
```lua
file.code = start_code
agent.current_step = 1
```

**Erlang:**
```erlang
Tab2 = Tab#tab{code = StartCode, step = 1}.
```

**Go:**
```go
tab.Code = startCode
agent.CurrentStep = 1
```

**Clojure:**
```clojure
(def tab' (assoc tab :code start-code :current-step 1))
```

**C:**
```c
strcpy(tab.code, start_code);
agent.current_step = 1;
```

**Bash:**
```bash
# not idiomatic
```

</details>

---

### 16. Agent status

**Gets an agent's status.**

```true
:agent:main:status = result
```

**Python:**
```python
result = agents["main"].status
```

**Elixir:**
```elixir
result = Agent.get(:"main", & &1.status)
```

**Erlang** (topic: concurrency):
```erlang
Result = sys:get_state(MainPid).
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val result = agentStatus["main"]
```

**Rust:**
```rust
let result = main_handle.state().to_string();
```

**Haskell:**
```haskell
result <- readMVar mainStatus
```

**Lua:**
```lua
local result = coroutine.status(main)
```

**JavaScript:**
```javascript
const result = agents["main"].status;
```

**Go:**
```go
result := agents["main"].Status
```

**Clojure:**
```clojure
(def result (:status @main-agent))
```

**C:**
```c
const char *result = main_agent.status_name;
```

**Bash:**
```bash
result=$main_status
```

</details>

---

### 17. All agent statuses

**Gets all agents' statuses.**

```true
:agent:status = result
```

**Python:**
```python
result = ",".join(f"{k}:{v.status}" for k, v in agents.items())
```

**Elixir:**
```elixir
result = Enum.map_join(agents, ",", fn {k, v} -> "#{k}:#{v.status}" end)
```

**Erlang** (topic: concurrency):
```erlang
Result = string:join([atom_to_list(K) ++ ":" ++ atom_to_list(V#agent.status) || {K, V} <- Agents], ",").
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val result = agents.entries.joinToString(",") { "${it.key}:${it.value.status}" }
```

**Rust:**
```rust
let result = agents.iter().map(|(k, v)| format!("{}:{}", k, v.status)).collect::<Vec<_>>().join(",");
```

**Haskell:**
```haskell
let result = intercalate "," (map (\(k, v) -> k ++ ":" ++ show (agentStatus v)) (Map.toList agents))
```

**Lua:**
```lua
local parts = {}
for k, v in pairs(agents) do table.insert(parts, k .. ":" .. v.status) end
local result = table.concat(parts, ",")
```

**JavaScript:**
```javascript
const result = Object.entries(agents).map(([k, v]) => `${k}:${v.status}`).join(",");
```

**Go:**
```go
parts := []string{}
for k, v := range agents { parts = append(parts, k+":"+v.Status) }
result := strings.Join(parts, ",")
```

**Clojure:**
```clojure
(def result (clojure.string/join "," (map (fn [[k v]] (str k ":" (:status v))) @agents)))
```

**C:**
```c
/* manual assembly */
```

**Bash:**
```bash
result=$(for k in "${!agents[@]}"; do echo -n "$k:${agents[$k]},"; done | sed 's/,$//')
```

</details>

---

### 18. Agent wait

**Pauses the agent for N ms.**

```true
100 => :agent:wait
```

**Python:**
```python
import time
time.sleep(0.1)
```

**Elixir:**
```elixir
Process.sleep(100)
```

**JavaScript** (topic: async):
```javascript
await new Promise(r => setTimeout(r, 100));
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
Thread.sleep(100)
```

**Rust:**
```rust
std::thread::sleep(std::time::Duration::from_millis(100));
```

**Haskell:**
```haskell
threadDelay 100000
```

**Lua:**
```lua
-- via socket.sleep
```

**Erlang:**
```erlang
timer:sleep(100).
```

**Go:**
```go
time.Sleep(100 * time.Millisecond)
```

**Clojure:**
```clojure
(Thread/sleep 100)
```

**C:**
```c
usleep(100 * 1000);
```

**Bash:**
```bash
sleep 0.1
```

</details>

---

## <a name="destroy"></a>4. Destruction

### 21. Container delete

**Deletes a container (`-> nothing`).**

```true
a = 5
a -> nothing
```

**Python:**
```python
a = 5
del a
```

**Elixir:**
```elixir
a = 5
# no direct delete
```

**JavaScript** (topic: state):
```javascript
let a = 5;
a = null;
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
var a: Int? = 5
a = null
```

**Rust:**
```rust
let a = 5;
drop(a);
```

**Haskell:**
```haskell
let a = 5
```

**Lua:**
```lua
local a = 5
a = nil
```

**Erlang:**
```erlang
A = 5.
```

**Go:**
```go
a := 5
a = 0
```

**Clojure:**
```clojure
(def a 5)
```

**C:**
```c
int a = 5;
a = 0;
```

**Bash:**
```bash
a=5
unset a
```

</details>

---

### 22. File delete

**Deletes a file (`file:x -> nothing`).**

```true
file:other.cycle -> nothing
```

**Python:**
```python
import os
os.remove("other.cycle")
```

**Elixir:**
```elixir
File.rm!("other.cycle")
```

**Bash** (topic: files):
```bash
rm -f other.cycle
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
java.io.File("other.cycle").delete()
```

**Rust:**
```rust
std::fs::remove_file("other.cycle")?;
```

**Haskell:**
```haskell
removeFile "other.cycle"
```

**Lua:**
```lua
os.remove("other.cycle")
```

**Erlang:**
```erlang
file:delete("other.cycle").
```

**JavaScript:**
```javascript
require("fs").unlinkSync("other.cycle");
```

**Go:**
```goos.Remove("other.cycle")
```

**Clojure:**
```clojure
(clojure.java.io/delete-file "other.cycle")
```

**C:**
```c
remove("other.cycle");
```

</details>

---

## <a name="data"></a>5. Data

### 23. Size by axis

**Returns the size of a container by axis.**

```true
1, 2, 3 = arr
arr:y => :size = result
```
**Result:** `result = "3"`

**Python:**
```python
arr = [1, 2, 3]
result = len(arr)
```

**Elixir:**
```elixir
arr = [1, 2, 3]
result = length(arr)
```

**Clojure** (topic: collections):
```clojure
(def arr [1 2 3])
(def result (count arr))
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val arr = listOf(1, 2, 3)
val result = arr.size
```

**Rust:**
```rust
let arr = vec![1, 2, 3];
let result = arr.len();
```

**Haskell:**
```haskell
let arr = [1, 2, 3]
let result = length arr
```

**Lua:**
```lua
local arr = {1, 2, 3}
local result = #arr
```

**Erlang:**
```erlang
Arr = [1, 2, 3],
Result = length(Arr).
```

**JavaScript:**
```javascript
const arr = [1, 2, 3];
const result = arr.length;
```

**Go:**
```go
arr := []int{1, 2, 3}
result := len(arr)
```

**C:**
```c
int arr[] = {1, 2, 3};
int result = sizeof(arr) / sizeof(arr[0]);
```

**Bash:**
```bash
arr=(1 2 3)
result=${#arr[@]}
```

</details>

---

### 24. Push

**Appends a value to a container by axis.**

```true
1, 2 = arr
arr:y, 3 => :push = result
```
**Result:** `result = "1,2,3"`

**Python:**
```python
arr = [1, 2]
arr.append(3)
result = ",".join(str(x) for x in arr)
```

**Elixir:**
```elixir
arr = [1, 2] ++ [3]
result = Enum.join(arr, ",")
```

**JavaScript** (topic: collections):
```javascript
const arr = [1, 2];
arr.push(3);
const result = arr.join(",");
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val arr = mutableListOf(1, 2)
arr.add(3)
val result = arr.joinToString(",")
```

**Rust:**
```rust
let mut arr = vec![1, 2];
arr.push(3);
let result = arr.iter().map(|x| x.to_string()).collect::<Vec<_>>().join(",");
```

**Haskell:**
```haskell
let arr = [1, 2]
let result = intercalate "," (map show (arr ++ [3]))
```

**Lua:**
```lua
local arr = {1, 2}
table.insert(arr, 3)
local result = table.concat(arr, ",")
```

**Erlang:**
```erlang
Arr = [1, 2],
Arr2 = Arr ++ [3],
Result = string:join([integer_to_list(X) || X <- Arr2], ",").
```

**Go:**
```go
arr := []int{1, 2}
arr = append(arr, 3)
parts := []string{}
for _, v := range arr { parts = append(parts, strconv.Itoa(v)) }
result := strings.Join(parts, ",")
```

**Clojure:**
```clojure
(def arr [1 2])
(def result (clojure.string/join "," (conj arr 3)))
```

**C:**
```c
int arr[] = {1, 2}; int n = 2;
arr[n++] = 3;
```

**Bash:**
```bash
arr=(1 2)
arr+=(3)
result=$(IFS=,; echo "${arr[*]}")
```

</details>

---

### 25. Splitter

**Sets a splitter for an axis of a named container.**

```true
input.csv => :get = raw
. => raw:splitter:l:;
raw:l => :size = rows
```

**Python:**
```python
raw = open("input.csv").read()
rows = len(raw.split(";"))
```

**Elixir:**
```elixir
{:ok, raw} = File.read("input.csv")
rows = raw |> String.split(";") |> length()
```

**Bash** (topic: shell):
```bash
rows=$(tr ';' '\n' < input.csv | wc -l)
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val raw = java.io.File("input.csv").readText()
val rows = raw.split(";").size
```

**Rust:**
```rust
let raw = std::fs::read_to_string("input.csv")?;
let rows = raw.split(';').count();
```

**Haskell:**
```haskell
raw <- readFile "input.csv"
let rows = length (splitOn ";" raw)
```

**Lua:**
```lua
local f = io.open("input.csv", "r")
local raw = f:read("*a"); f:close()
local rows = 0
for _ in raw:gmatch("[^;]+") do rows = rows + 1 end
```

**Erlang:**
```erlang
{ok, Bin} = file:read_file("input.csv"),
Rows = length(string:split(binary_to_list(Bin), ";", all)).
```

**JavaScript:**
```javascript
const raw = require("fs").readFileSync("input.csv", "utf8");
const rows = raw.split(";").length;
```

**Go:**
```go
raw, _ := os.ReadFile("input.csv")
rows := len(strings.Split(string(raw), ";"))
```

**Clojure:**
```clojure
(def raw (slurp "input.csv"))
(def rows (count (clojure.string/split raw #";")))
```

**C:**
```c
/* manual split */
```

</details>

---

### 26. Range

**Generates a range of numbers, comma-separated.**

```true
1, 5 => :range = result
```
**Result:** `result = "1,2,3,4,5"`

**Python:**
```python
result = ",".join(str(n) for n in range(1, 6))
```

**Elixir:**
```elixir
result = Enum.join(1..5, ",")
```

**Clojure** (topic: collections):
```clojure
(def result (clojure.string/join "," (range 1 6)))
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val result = (1..5).joinToString(",")
```

**Rust:**
```rust
let result = (1..=5).map(|n| n.to_string()).collect::<Vec<_>>().join(",");
```

**Haskell:**
```haskell
let result = intercalate "," (map show [1..5])
```

**Lua:**
```lua
local t = {}
for i = 1, 5 do table.insert(t, tostring(i)) end
local result = table.concat(t, ",")
```

**Erlang:**
```erlang
Result = string:join([integer_to_list(N) || N <- lists:seq(1, 5)], ",").
```

**JavaScript:**
```javascript
const result = [...Array(5).keys()].map(n => n + 1).join(",");
```

**Go:**
```go
parts := []string{}
for i := 1; i <= 5; i++ { parts = append(parts, strconv.Itoa(i)) }
result := strings.Join(parts, ",")
```

**C:**
```c
/* manual assembly */
```

**Bash:**
```bash
result=$(seq -s, 1 5)
```

</details>

---

### 27. Repeat

**Executes a named action N times.**

```true
0 = a
a + 1 => a = action
action, 5 => :repeat
```
**Result:** `a = "5"`

**Python:**
```python
a = 0
for _ in range(5):
    a += 1
```

**Elixir:**
```elixir
a = Enum.reduce(1..5, 0, fn _, acc -> acc + 1 end)
```

**JavaScript** (topic: loops):
```javascript
let a = 0;
for (let i = 0; i < 5; i++) a += 1;
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
var a = 0
repeat(5) { a += 1 }
```

**Rust:**
```rust
let mut a = 0;
for _ in 0..5 { a += 1; }
```

**Haskell:**
```haskell
let a = foldl (\acc _ -> acc + 1) 0 [1..5]
```

**Lua:**
```lua
local a = 0
for i = 1, 5 do a = a + 1 end
```

**Erlang:**
```erlang
A = lists:foldl(fun(_, Acc) -> Acc + 1 end, 0, lists:seq(1, 5)).
```

**Go:**
```go
a := 0
for i := 0; i < 5; i++ { a++ }
```

**Clojure:**
```clojure
(def a (reduce (fn [acc _] (inc acc)) 0 (range 5)))
```

**C:**
```c
int a = 0;
for (int i = 0; i < 5; i++) a++;
```

**Bash:**
```bash
a=0
for i in $(seq 1 5); do a=$((a + 1)); done
```

</details>

---

### 28. Each (was foreach)

**Iterates over a container by axis.**

```true
1, 2, 3 = arr
0 = sum
arr:y, n => sum + n => :math = sum => :each
```
**Result:** `sum = "6"`

**Python:**
```python
arr = [1, 2, 3]
total = 0
for n in arr:
    total += n
```

**Elixir:**
```elixir
arr = [1, 2, 3]
total = Enum.reduce(arr, 0, fn n, acc -> acc + n end)
```

**JavaScript** (topic: loops):
```javascript
const arr = [1, 2, 3];
let total = 0;
arr.forEach(n => total += n);
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val arr = listOf(1, 2, 3)
var total = 0
arr.forEach { total += it }
```

**Rust:**
```rust
let arr = vec![1, 2, 3];
let total: i32 = arr.iter().sum();
```

**Haskell:**
```haskell
let arr = [1, 2, 3]
let total = foldl (+) 0 arr
```

**Lua:**
```lua
local arr = {1, 2, 3}
local total = 0
for _, n in ipairs(arr) do total = total + n end
```

**Erlang:**
```erlang
Arr = [1, 2, 3],
Total = lists:foldl(fun(X, Acc) -> Acc + X end, 0, Arr).
```

**Go:**
```go
arr := []int{1, 2, 3}
total := 0
for _, n := range arr { total += n }
```

**Clojure:**
```clojure
(def total (reduce + [1 2 3]))
```

**C:**
```c
int arr[] = {1, 2, 3};
int total = 0;
for (int i = 0; i < 3; i++) total += arr[i];
```

**Bash:**
```bash
total=0
for n in 1 2 3; do total=$((total + n)); done
```

</details>

---

### 29. Error stack

**Reads the error stack.**

```true
1 // 0 => :math
:error:lines = result
```
**Result:** `result = "1"`

**Python:**
```python
try:
    1 / 0
except ZeroDivisionError:
    result = "1"
```

**Elixir:**
```elixir
try do
  1 / 0
rescue
  _ -> result = "1"
end
```

**JavaScript** (topic: errors):
```javascript
let result = "0";
try { 1 / 0; } catch (e) { result = "1"; }
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val result = try { 1 / 0; "0" } catch (e: Exception) { "1" }
```

**Rust:**
```rust
let result = match std::panic::catch_unwind(|| 1 / 0) { Ok(_) => "0", Err(_) => "1" };
```

**Haskell:**
```haskell
-- via Control.Exception
```

**Lua:**
```lua
local ok = pcall(function() return 1 / 0 end)
local result = ok and "0" or "1"
```

**Erlang:**
```erlang
Result = try 1 / 0 of _ -> "0" catch _:_ -> "1" end.
```

**Go:**
```go
result := "0"
func() { defer func() { if r := recover(); r != nil { result = "1" } }(); _ = 1 / 0 }()
```

**Clojure:**
```clojure
(def result (try (do (/ 1 0) "0") (catch Exception _ "1")))
```

**C:**
```c
/* manual error handling */
```

**Bash:**
```bash
result=0
[ 0 -eq 0 ] || result=1
```

</details>

---

### 30. Error handler (actions)

**Sets `:error ? ^ :handle` as an action.**

```true
actions = ":error ? ^ :handle\n:run"
1 // 0 => :math
= handle
error => :log
```

**Python:**
```python
try:
    1 / 0
except Exception as e:
    print(e)
```

**Elixir:**
```elixir
try do
  1 / 0
rescue
  e -> IO.puts(Exception.message(e))
end
```

**JavaScript** (topic: errors):
```javascript
try { 1 / 0; } catch (e) { console.log(e); }
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
try { 1 / 0 } catch (e: Exception) { println(e.message) }
```

**Rust:**
```rust
// via ? operator and error propagation
```

**Haskell:**
```haskell
-- via catch
```

**Lua:**
```lua
local ok, err = pcall(function() return 1 / 0 end)
if not ok then print(err) end
```

**Erlang:**
```erlang
try 1 / 0 catch C:E -> io:format("~p:~p~n", [C, E]) end.
```

**Go:**
```go
defer func() { if r := recover(); r != nil { fmt.Println(r) } }()
_ = 1 / 0
```

**Clojure:**
```clojure
(try (/ 1 0) (catch Exception e (println (.getMessage e))))
```

**C:**
```c
/* manual */
```

**Bash:**
```bash
( exit 1 ) || echo "error"
```

</details>

---

## <a name="json"></a>6. JSON

### 31. JSON get

**Gets a value from JSON by key.**

```true
{'name':'test','value':42} = json
json, name => :json:get = result
```
**Result:** `result = "test"`

**Python:**
```python
import json
data = json.loads('{"name":"test","value":42}')
result = data["name"]
```

**Elixir:**
```elixir
data = Jason.decode!~s({"name":"test","value":42})
result = data["name"]
```

**JavaScript** (topic: JSON):
```javascript
const data = JSON.parse('{"name":"test","value":42}');
const result = data.name;
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val data = kotlinx.serialization.json.Json.parseToJsonElement("""{"name":"test","value":42}""").jsonObject
val result = data["name"]?.jsonPrimitive?.content
```

**Rust:**
```rust
let data: serde_json::Value = serde_json::from_str(r#"{"name":"test","value":42}"#)?;
let result = data["name"].as_str();
```

**Haskell:**
```haskell
-- via aeson
```

**Lua:**
```lua
local json = require("json")
local data = json.decode('{"name":"test","value":42}')
local result = data.name
```

**Erlang:**
```erlang
{ok, Bin} = file:read_file("/tmp/x.json"),
Data = jsx:decode(Bin),
Result = proplists:get_value(<<"name">>, Data).
```

**Go:**
```go
var data map[string]any
json.Unmarshal([]byte(`{"name":"test","value":42}`), &data)
result := data["name"]
```

**Clojure:**
```clojure
(def data (parse-string "{\"name\":\"test\",\"value\":42}" true))
(def result (:name data))
```

**C:**
```c
/* via jansson or similar */
```

**Bash:**
```bash
result=$(echo '{"name":"test","value":42}' | jq -r .name)
```

</details>

---

### 32. JSON names

**Gets all top-level keys of a JSON object.**

```true
{'a':1,'b':2} = json
json => :json:names = result
```
**Result:** `result = "a,b"`

**Python:**
```python
import json
data = json.loads('{"a":1,"b":2}')
result = ",".join(data.keys())
```

**Elixir:**
```elixir
data = Jason.decode!~s({"a":1,"b":2})
result = Map.keys(data) |> Enum.join(",")
```

**JavaScript** (topic: JSON):
```javascript
const data = JSON.parse('{"a":1,"b":2}');
const result = Object.keys(data).join(",");
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val data = kotlinx.serialization.json.Json.parseToJsonElement("""{"a":1,"b":2}""").jsonObject
val result = data.keys.joinToString(",")
```

**Rust:**
```rust
let data: serde_json::Value = serde_json::from_str(r#"{"a":1,"b":2}"#)?;
let result = data.as_object().unwrap().keys().cloned().collect::<Vec<_>>().join(",");
```

**Haskell:**
```haskell
-- via aeson
```

**Lua:**
```lua
local json = require("json")
local data = json.decode('{"a":1,"b":2}')
local keys = {}
for k in pairs(data) do table.insert(keys, k) end
local result = table.concat(keys, ",")
```

**Erlang:**
```erlang
Data = jsx:decode(<<"{\"a\":1,\"b\":2}">>),
Result = string:join([K || {K, _} <- Data], ",").
```

**Go:**
```go
var data map[string]any
json.Unmarshal([]byte(`{"a":1,"b":2}`), &data)
keys := make([]string, 0, len(data))
for k := range data { keys = append(keys, k) }
result := strings.Join(keys, ",")
```

**Clojure:**
```clojure
(def data (parse-string "{\"a\":1,\"b\":2}" true))
(def result (clojure.string/join "," (keys data)))
```

**C:**
```c
/* via jansson */
```

**Bash:**
```bash
result=$(echo '{"a":1,"b":2}' | jq -r 'keys_unsorted | join(",")')
```

</details>

---

## <a name="cond"></a>7. Conditions and jumps

### 33. Jump to line

**Jumps to line 5.**

```true
^ 5
```

**Python:**
```python
# no goto; use if/else or state machine
```

**Elixir:**
```elixir
# no goto; use case/cond
```

**C** (topic: goto):
```c
goto line5;
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
// no goto
```

**Rust:**
```rust
// no goto
```

**Haskell:**
```haskell
-- no goto
```

**Lua:**
```lua
goto line5
```

**Erlang:**
```erlang
% no goto
```

**JavaScript:**
```javascript
// no goto
```

**Go:**
```go
goto line5
```

**Clojure:**
```clojure
;; no goto
```

**Bash:**
```bash
# no goto
```

</details>

---

### 34. Condition with jump

**Jumps to `do` if `a < max`.**

```true
a < max ? ^ do
```

**Python:**
```python
if a < max:
    # jump to do
    pass
```

**Elixir:**
```elixir
if a < max, do: goto_do()
```

**C** (topic: goto):
```c
if (a < max) goto do_label;
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
if (a < max) doBlock()
```

**Rust:**
```rust
if a < max { do_block(); }
```

**Haskell:**
```haskell
when (a < max) doBlock
```

**Lua:**
```lua
if a < max then goto do_block end
```

**Erlang:**
```erlang
case A < Max of true -> do_block(); false -> ok end.
```

**JavaScript:**
```javascript
if (a < max) doBlock();
```

**Go:**
```go
if a < max { goto doBlock }
```

**Clojure:**
```clojure
(when (< a max) (do-block))
```

**Bash:**
```bash
if (( a < max )); then :; fi
```

</details>

---

### 35. FizzBuzz (classic, via `^`/`?`)

**Classic FizzBuzz with jumps and conditions.**

```true
= out
1 = i
15 = n
= loop
i > n ? ^ done
i % 15 == 0 ? ^ fizzbuzz
i % 3 == 0 ? ^ fizz
i % 5 == 0 ? ^ buzz
^ num
= fizzbuzz
FizzBuzz = out
i + 1 => :math = i
^ loop
= fizz
Fizz = out
i + 1 => :math = i
^ loop
= buzz
Buzz = out
i + 1 => :math = i
^ loop
= num
i = out
i + 1 => :math = i
^ loop
= done
```

**Python:**
```python
out = ""
i = 1
n = 15
while i <= n:
    if i % 15 == 0:
        out = "FizzBuzz"
    elif i % 3 == 0:
        out = "Fizz"
    elif i % 5 == 0:
        out = "Buzz"
    else:
        out = str(i)
    i += 1
```

**Elixir:**
```elixir
out =
  Enum.map(1..15, fn i ->
    cond do
      rem(i, 15) == 0 -> "FizzBuzz"
      rem(i, 3) == 0 -> "Fizz"
      rem(i, 5) == 0 -> "Buzz"
      true -> Integer.to_string(i)
    end
  end) |> Enum.join(",")
```

**JavaScript** (topic: loops):
```javascript
let out = [];
for (let i = 1; i <= 15; i++) {
  if (i % 15 === 0) out.push("FizzBuzz");
  else if (i % 3 === 0) out.push("Fizz");
  else if (i % 5 === 0) out.push("Buzz");
  else out.push(i);
}
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val out = (1..15).map {
    when {
        it % 15 == 0 -> "FizzBuzz"
        it % 3 == 0 -> "Fizz"
        it % 5 == 0 -> "Buzz"
        else -> it.toString()
    }
}
```

**Rust:**
```rust
let out: Vec<String> = (1..=15).map(|i| {
    if i % 15 == 0 { "FizzBuzz".to_string() }
    else if i % 3 == 0 { "Fizz".to_string() }
    else if i % 5 == 0 { "Buzz".to_string() }
    else { i.to_string() }
}).collect();
```

**Haskell:**
```haskell
let out = map fizzbuzz [1..15]
  where
    fizzbuzz i | i `mod` 15 == 0 = "FizzBuzz"
               | i `mod` 3 == 0 = "Fizz"
               | i `mod` 5 == 0 = "Buzz"
               | otherwise = show i
```

**Lua:**
```lua
local out = {}
for i = 1, 15 do
  if i % 15 == 0 then out[#out+1] = "FizzBuzz"
  elseif i % 3 == 0 then out[#out+1] = "Fizz"
  elseif i % 5 == 0 then out[#out+1] = "Buzz"
  else out[#out+1] = tostring(i) end
end
```

**Erlang:**
```erlang
Out = [case {I rem 15, I rem 3, I rem 5} of
        {0, _, _} -> "FizzBuzz";
        {_, 0, _} -> "Fizz";
        {_, _, 0} -> "Buzz";
        _ -> integer_to_list(I)
       end || I <- lists:seq(1, 15)].
```

**Go:**
```go
out := []string{}
for i := 1; i <= 15; i++ {
    switch {
    case i % 15 == 0: out = append(out, "FizzBuzz")
    case i % 3 == 0: out = append(out, "Fizz")
    case i % 5 == 0: out = append(out, "Buzz")
    default: out = append(out, strconv.Itoa(i))
    }
}
```

**Clojure:**
```clojure
(def out (map (fn [i]
                (cond
                  (zero? (mod i 15)) "FizzBuzz"
                  (zero? (mod i 3)) "Fizz"
                  (zero? (mod i 5)) "Buzz"
                  :else (str i)))
              (range 1 16)))
```

**C:**
```c
for (int i = 1; i <= 15; i++) {
    if (i % 15 == 0) printf("FizzBuzz\n");
    else if (i % 3 == 0) printf("Fizz\n");
    else if (i % 5 == 0) printf("Buzz\n");
    else printf("%d\n", i);
}
```

**Bash:**
```bash
for i in $(seq 1 15); do
  if (( i % 15 == 0 )); then echo FizzBuzz
  elif (( i % 3 == 0 )); then echo Fizz
  elif (( i % 5 == 0 )); then echo Buzz
  else echo $i; fi
done
```

</details>

---

### 36. FizzBuzz (short version via `:range` + `:each`)

**FizzBuzz in one loop, via `:each`.**

```true
1, 15 => :range = ns
ns:y, n => n % 15 == 0 ? FizzBuzz = out => :each
```

**Python:**
```python
out = ""
for n in range(1, 16):
    if n % 15 == 0:
        out = "FizzBuzz"
```

**Elixir:**
```elixir
out =
  Enum.map(1..15, fn n ->
    if rem(n, 15) == 0, do: "FizzBuzz", else: ""
  end) |> Enum.join(",")
```

**JavaScript** (topic: loops):
```javascript
let out = "";
[...Array(15).keys()].map(n => n + 1).forEach(n => {
  if (n % 15 === 0) out = "FizzBuzz";
});
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
var out = ""
(1..15).forEach { n -> if (n % 15 == 0) out = "FizzBuzz" }
```

**Rust:**
```rust
let mut out = String::new();
for n in 1..=15 { if n % 15 == 0 { out = "FizzBuzz".to_string(); } }
```

**Haskell:**
```haskell
let out = foldl (\acc n -> if n `mod` 15 == 0 then "FizzBuzz" else acc) "" [1..15]
```

**Lua:**
```lua
local out = ""
for n = 1, 15 do if n % 15 == 0 then out = "FizzBuzz" end end
```

**Erlang:**
```erlang
Out = lists:foldl(fun(N, Acc) -> case N rem 15 of 0 -> "FizzBuzz"; _ -> Acc end end, "", lists:seq(1, 15)).
```

**Go:**
```go
out := ""
for n := 1; n <= 15; n++ { if n % 15 == 0 { out = "FizzBuzz" } }
```

**Clojure:**
```clojure
(def out (reduce (fn [acc n] (if (zero? (mod n 15)) "FizzBuzz" acc)) "" (range 1 16)))
```

**C:**
```c
const char *out = "";
for (int n = 1; n <= 15; n++) if (n % 15 == 0) out = "FizzBuzz";
```

**Bash:**
```bash
out=""
for n in $(seq 1 15); do (( n % 15 == 0 )) && out="FizzBuzz"; done
```

</details>

---

## <a name="assert"></a>8. Assert

### 37. Assert via `:assert`

**Asserts that `a == b`. Success — green, failure — red. Does not pause by default.**

```true
5 = a
5 = b
a == b, "a must equal b" => :assert
```
**Result:** `:assert` = `"1"`, `LogEntry(type="assert", resolved=true)` — green.

**Python:**
```python
a = 5
b = 5
assert a == b, "a must equal b"
```

**Elixir:**
```elixir
a = 5
b = 5
if a != b, do: raise "a must equal b"
```

**JavaScript** (topic: errors):
```javascript
const a = 5;
const b = 5;
console.assert(a === b, "a must equal b");
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val a = 5
val b = 5
check(a == b) { "a must equal b" }
```

**Rust:**
```rust
let a = 5;
let b = 5;
assert_eq!(a, b, "a must equal b");
```

**Haskell:**
```haskell
let a = 5
let b = 5
if a /= b then error "a must equal b" else ()
```

**Lua:**
```lua
local a = 5
local b = 5
assert(a == b, "a must equal b")
```

**Erlang:**
```erlang
A = 5,
B = 5,
case A == B of true -> ok; false -> erlang:error("a must equal b") end.
```

**Go:**
```go
a := 5
b := 5
if a != b { panic("a must equal b") }
```

**Clojure:**
```clojure
(let [a 5 b 5] (assert (= a b) "a must equal b"))
```

**C:**
```c
int a = 5;
int b = 5;
assert(a == b && "a must equal b");
```

**Bash:**
```bash
a=5
b=5
[ "$a" = "$b" ] || { echo "a must equal b" >&2; exit 1; }
```

</details>

---

### 38. Assert with `:pauseAll`

**Asserts, then pauses all agents (whether success or failure).**

```true
5 = a
5 = b
a == b, "a must equal b" => :assert
:pauseAll
```

**Python:**
```python
a = 5
b = 5
assert a == b, "a must equal b"
# pause all agents
```

**Elixir:**
```elixir
a = 5
b = 5
if a != b, do: raise "a must equal b"
# pause all agents
```

**JavaScript** (topic: errors):
```javascript
const a = 5;
const b = 5;
console.assert(a === b, "a must equal b");
// pause all agents
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val a = 5
val b = 5
check(a == b) { "a must equal b" }
// pause all agents
```

**Rust:**
```rust
let a = 5;
let b = 5;
assert_eq!(a, b, "a must equal b");
// pause all agents
```

**Haskell:**
```haskell
let a = 5
let b = 5
if a /= b then error "a must equal b" else ()
-- pause all agents
```

**Lua:**
```lua
local a = 5
local b = 5
assert(a == b, "a must equal b")
-- pause all agents
```

**Erlang:**
```erlang
A = 5,
B = 5,
case A == B of true -> ok; false -> erlang:error("a must equal b") end,
% pause all agents
```

**Go:**
```go
a := 5
b := 5
if a != b { panic("a must equal b") }
// pause all agents
```

**Clojure:**
```clojure
(let [a 5 b 5] (assert (= a b) "a must equal b"))
;; pause all agents
```

**C:**
```c
int a = 5;
int b = 5;
assert(a == b && "a must equal b");
/* pause all agents */
```

**Bash:**
```bash
a=5
b=5
[ "$a" = "$b" ] || { echo "a must equal b" >&2; exit 1; }
# pause all agents
```

</details>

---

## <a name="func"></a>9. Functional

### 39. `:map` — apply to every element

**Doubles every element.**

```true
1, 2, 3 = arr
arr:y, n * 2 => :map = doubles
```
**Result:** `doubles = "2,4,6"`

**Python:**
```python
arr = [1, 2, 3]
doubles = [n * 2 for n in arr]
```

**Elixir:**
```elixir
arr = [1, 2, 3]
doubles = Enum.map(arr, &(&1 * 2))
```

**Clojure** (topic: collections):
```clojure
(def doubles (map #(* 2 %) [1 2 3]))
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val arr = listOf(1, 2, 3)
val doubles = arr.map { it * 2 }
```

**Rust:**
```rust
let arr = vec![1, 2, 3];
let doubles: Vec<i32> = arr.into_iter().map(|n| n * 2).collect();
```

**Haskell:**
```haskell
let arr = [1, 2, 3]
let doubles = map (*2) arr
```

**Lua:**
```lua
local arr = {1, 2, 3}
local doubles = {}
for _, v in ipairs(arr) do table.insert(doubles, v * 2) end
```

**Erlang:**
```erlang
Arr = [1, 2, 3],
Doubles = lists:map(fun(X) -> X * 2 end, Arr).
```

**JavaScript:**
```javascript
const arr = [1, 2, 3];
const doubles = arr.map(n => n * 2);
```

**Go:**
```go
arr := []int{1, 2, 3}
doubles := []int{}
for _, v := range arr { doubles = append(doubles, v * 2) }
```

**C:**
```c
int arr[] = {1, 2, 3};
int doubles[3];
for (int i = 0; i < 3; i++) doubles[i] = arr[i] * 2;
```

**Bash:**
```bash
arr=(1 2 3)
doubles=()
for n in "${arr[@]}"; do doubles+=($((n * 2))); done
```

</details>

---

### 40. `:reduce` — sum

**Sums a container via `:reduce`.**

```true
1, 2, 3 = arr
arr:y, 0, acc + n => :reduce = total
```
**Result:** `total = "6"`

**Python:**
```python
from functools import reduce
arr = [1, 2, 3]
total = reduce(lambda acc, n: acc + n, arr, 0)
```

**Elixir:**
```elixir
arr = [1, 2, 3]
total = Enum.reduce(arr, 0, fn n, acc -> acc + n end)
```

**Clojure** (topic: collections):
```clojure
(def total (reduce + 0 [1 2 3]))
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val arr = listOf(1, 2, 3)
val total = arr.fold(0) { acc, n -> acc + n }
```

**Rust:**
```rust
let arr = vec![1, 2, 3];
let total = arr.iter().fold(0, |acc, n| acc + n);
```

**Haskell:**
```haskell
let arr = [1, 2, 3]
let total = foldl (+) 0 arr
```

**Lua:**
```lua
local arr = {1, 2, 3}
local total = 0
for _, v in ipairs(arr) do total = total + v end
```

**Erlang:**
```erlang
Arr = [1, 2, 3],
Total = lists:foldl(fun(X, Acc) -> Acc + X end, 0, Arr).
```

**JavaScript:**
```javascript
const arr = [1, 2, 3];
const total = arr.reduce((acc, n) => acc + n, 0);
```

**Go:**
```go
arr := []int{1, 2, 3}
total := 0
for _, v := range arr { total += v }
```

**C:**
```c
int arr[] = {1, 2, 3};
int total = 0;
for (int i = 0; i < 3; i++) total += arr[i];
```

**Bash:**
```bash
total=0
for n in 1 2 3; do total=$((total + n)); done
```

</details>

---

## <a name="rec"></a>10. Recursion via stack

### 42. Tree traversal via stack

**Traverses a tree (via `:stack:push` / `:stack:pop`) without native recursion.**

```true
= stack
node = root
node => stack:y => :push
= loop
stack:y => :size = sz
sz == 0 ? ^ done
stack:y => :pop = cur
cur => :log
cur:left => stack:y => :push
cur:right => stack:y => :push
^ loop
= done
```

**Python:**
```python
stack = [root]
while stack:
    cur = stack.pop()
    print(cur)
    if cur.left: stack.append(cur.left)
    if cur.right: stack.append(cur.right)
```

**Elixir:**
```elixir
def walk(stack) do
  case stack do
    [] -> :ok
    [cur | rest] ->
      IO.puts(cur)
      walk(rest ++ [cur.left, cur.right])
  end
end
```

**Lua** (topic: games):
```lua
local stack = {root}
while #stack > 0 do
  local cur = table.remove(stack)
  print(cur)
  if cur.left then table.insert(stack, cur.left) end
  if cur.right then table.insert(stack, cur.right) end
end
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val stack = ArrayDeque<Node>()
stack.addLast(root)
while (stack.isNotEmpty()) {
    val cur = stack.removeLast()
    println(cur)
    cur.left?.let { stack.addLast(it) }
    cur.right?.let { stack.addLast(it) }
}
```

**Rust:**
```rust
let mut stack = vec![root];
while let Some(cur) = stack.pop() {
    println!("{:?}", cur);
    if let Some(l) = cur.left { stack.push(l); }
    if let Some(r) = cur.right { stack.push(r); }
}
```

**Haskell:**
```haskell
walk [] = return ()
walk (cur:rest) = do
    print cur
    walk (rest ++ [left cur, right cur])
```

**Erlang:**
```erlang
walk([]) -> ok;
walk([Cur | Rest]) ->
    io:format("~p~n", [Cur]),
    walk(Rest ++ [element(2, Cur), element(3, Cur)]).
```

**JavaScript:**
```javascript
const stack = [root];
while (stack.length) {
  const cur = stack.pop();
  console.log(cur);
  if (cur.left) stack.push(cur.left);
  if (cur.right) stack.push(cur.right);
}
```

**Go:**
```go
stack := []*Node{root}
for len(stack) > 0 {
    cur := stack[len(stack)-1]
    stack = stack[:len(stack)-1]
    fmt.Println(cur)
    if cur.Left != nil { stack = append(stack, cur.Left) }
    if cur.Right != nil { stack = append(stack, cur.Right) }
}
```

**Clojure:**
```clojure
(loop [stack [root]]
  (when-let [cur (peek stack)]
    (println cur)
    (recur (conj (pop stack) (:left cur) (:right cur)))))
```

**C:**
```c
struct Node *stack[100]; int top = 0;
stack[top++] = root;
while (top > 0) {
    struct Node *cur = stack[--top];
    printf("%d\n", cur->val);
    if (cur->left) stack[top++] = cur->left;
    if (cur->right) stack[top++] = cur->right;
}
```

**Bash:**
```bash
# not idiomatic
```

</details>

---

## <a name="games"></a>11. Games

### 43. Tic-tac-toe (text version)

**A minimal text tic-tac-toe: 3×3 board, two players alternate, win check.**

The board is a `y`-container of 9 cells (`x`, `o`, or `.`). Each move replaces one cell by index (1–9). Win check tests 8 lines.

```true
= board
= turn
= move
= winner
= result
x = turn
. . . . . . . . . = board
= loop
winner != "" ? ^ done
move => :log
move, 1 => :data:charAt = cell
cell == "." ? ^ play
^ next
= play
turn = board:y:move
turn == x ? o = turn
turn == o ? x = turn
^ next
= next
winner == "" ? ^ loop
= done
winner => :log
```

**Result:** board updated each move, `winner` = `"x"` / `"o"` / `"draw"`.

**Python:**
```python
board = ["."] * 9
turn = "x"
winner = ""
lines = [(0,1,2),(3,4,5),(6,7,8),(0,3,6),(1,4,7),(2,5,8),(0,4,8),(2,4,6)]
while not winner:
    move = int(input(f"{turn} move (1-9): ")) - 1
    if board[move] == ".":
        board[move] = turn
        turn = "o" if turn == "x" else "x"
    for a, b, c in lines:
        if board[a] == board[b] == board[c] != ".":
            winner = board[a]
    if "." not in board and not winner:
        winner = "draw"
print("Winner:", winner)
```

**Elixir:**
```elixir
defmodule TicTacToe do
  def play(board, turn) do
    move = String.to_integer(IO.gets("#{turn} move: ")) - 1
    if Enum.at(board, move) == "." do
      board = List.replace_at(board, move, turn)
      turn = if turn == "x", do: "o", else: "x"
      lines = [{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}]
      winner = Enum.find_value(lines, fn {a,b,c} ->
        va = Enum.at(board, a)
        if va == Enum.at(board, b) and va == Enum.at(board, c) and va != ".", do: va
      end)
      cond do
        winner -> IO.puts("Winner: #{winner}")
        not Enum.member?(board, ".") -> IO.puts("Draw")
        true -> play(board, turn)
      end
    else
      play(board, turn)
    end
  end
end
TicTacToe.play(List.duplicate(".", 9), "x")
```

**Lua** (topic: games):
```lua
local board = {"." , ".", ".", ".", ".", ".", ".", ".", "."}
local turn = "x"
local lines = {{1,2,3},{4,5,6},{7,8,9},{1,4,7},{2,5,8},{3,6,9},{1,5,9},{3,5,7}}
while true do
  io.write(turn .. " move (1-9): ")
  local move = tonumber(io.read())
  if board[move] == "." then
    board[move] = turn
    turn = (turn == "x") and "o" or "x"
    for _, l in ipairs(lines) do
      if board[l[1]] == board[l[2]] and board[l[2]] == board[l[3]] and board[l[1]] ~= "." then
        print("Winner: " .. board[l[1]]); os.exit()
      end
    end
  end
end
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val board = MutableList(9) { "." }
var turn = "x"
val lines = listOf(listOf(0,1,2), listOf(3,4,5), listOf(6,7,8), listOf(0,3,6), listOf(1,4,7), listOf(2,5,8), listOf(0,4,8), listOf(2,4,6))
while (true) {
    print("$turn move (1-9): ")
    val move = readLine()!!.toInt() - 1
    if (board[move] == ".") {
        board[move] = turn
        turn = if (turn == "x") "o" else "x"
        val w = lines.firstOrNull { (a, b, c) -> board[a] == board[b] && board[b] == board[c] && board[a] != "." }
        if (w != null) { println("Winner: ${board[w[0]]}"); break }
    }
}
```

**Rust:**
```rust
use std::io;
fn main() {
    let mut board = vec!["."; 9];
    let mut turn = "x";
    let lines = [(0,1,2),(3,4,5),(6,7,8),(0,3,6),(1,4,7),(2,5,8),(0,4,8),(2,4,6)];
    loop {
        println!("{} move (1-9):", turn);
        let mut s = String::new();
        io::stdin().read_line(&mut s).unwrap();
        let m: usize = s.trim().parse().unwrap();
        let m = m - 1;
        if board[m] == "." {
            board[m] = turn;
            turn = if turn == "x" { "o" } else { "x" };
            for &(a, b, c) in &lines {
                if board[a] == board[b] && board[b] == board[c] && board[a] != "." {
                    println!("Winner: {}", board[a]);
                    return;
                }
            }
        }
    }
}
```

**Haskell:**
```haskell
import Data.List (find)
main = play (replicate 9 '.') "x"
  where
    lines' = [(0,1,2),(3,4,5),(6,7,8),(0,3,6),(1,4,7),(2,5,8),(0,4,8),(2,4,6)]
    play board turn = do
      putStrLn (turn ++ " move (1-9):")
      m <- readLn :: IO Int
      let i = m - 1
      if board !! i == '.'
        then do
          let board' = take i board ++ [head turn] ++ drop (i+1) board
          case find (\(a,b,c) -> board' !! a == board' !! b && board' !! b == board' !! c && board' !! a /= '.') lines' of
            Just (a,_,_) -> putStrLn ("Winner: " ++ [board' !! a])
            Nothing -> play board' (if turn == "x" then "o" else "x")
        else play board turn
```

**Erlang:**
```erlang
-module(ttt).
-export([start/0]).
start() -> play(lists:duplicate(9, $.), $x).
play(Board, Turn) ->
    io:format("~c move: ", [Turn]),
    {ok, [M]} = io:fread("", "~d"),
    case lists:nth(M, Board) of
        $. ->
            B2 = setnth(M, Board, Turn),
            NextTurn = case Turn of $x -> $o; $o -> $x end,
            case check_win(B2) of
                {win, C} -> io:format("Winner: ~c~n", [C]);
                none -> play(B2, NextTurn)
            end;
        _ -> play(Board, Turn)
    end.
check_win(B) ->
    Lines = [{1,2,3},{4,5,6},{7,8,9},{1,4,7},{2,5,8},{3,6,9},{1,5,9},{3,5,7}],
    case [{A,Bv,C} || {A,Bv,C} <- Lines,
                      lists:nth(A,B) =:= lists:nth(Bv,B),
                      lists:nth(Bv,B) =:= lists:nth(C,B),
                      lists:nth(A,B) =/= $.] of
        [{A,_,_}|_] -> {win, lists:nth(A,B)};
        [] -> none
    end.
setnth(N, L, V) -> {A, [_|B]} = lists:nthtail(N-1, L), A ++ [V|B].
```

**JavaScript:**
```javascript
const board = Array(9).fill(".");
let turn = "x";
const lines = [[0,1,2],[3,4,5],[6,7,8],[0,3,6],[1,4,7],[2,5,8],[0,4,8],[2,4,6]];
while (true) {
  const m = parseInt(prompt(`${turn} move (1-9):`)) - 1;
  if (board[m] === ".") {
    board[m] = turn;
    turn = turn === "x" ? "o" : "x";
    const w = lines.find(([a,b,c]) => board[a] === board[b] && board[b] === board[c] && board[a] !== ".");
    if (w) { console.log("Winner:", board[w[0]]); break; }
  }
}
```

**Go:**
```go
package main
import ("fmt"; "os")
func main() {
    board := []string{".", ".", ".", ".", ".", ".", ".", ".", "."}
    turn := "x"
    lines := [][3]int{{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}}
    for {
        fmt.Printf("%s move (1-9): ", turn)
        var m int
        fmt.Fscan(os.Stdin, &m)
        m--
        if board[m] == "." {
            board[m] = turn
            if turn == "x" { turn = "o" } else { turn = "x" }
            for _, l := range lines {
                if board[l[0]] == board[l[1]] && board[l[1]] == board[l[2]] && board[l[0]] != "." {
                    fmt.Println("Winner:", board[l[0]])
                    return
                }
            }
        }
    }
}
```

**Clojure:**
```clojure
(def board (atom (vec (repeat 9 "."))))
(def turn (atom "x"))
(def lines [[0 1 2] [3 4 5] [6 7 8] [0 3 6] [1 4 7] [2 5 8] [0 4 8] [2 4 6]])
(loop []
  (println @turn "move (1-9):")
  (let [m (dec (read))]
    (when (= (get @board m) ".")
      (swap! board assoc m @turn)
      (swap! turn #(if (= % "x") "o" "x"))
      (when-let [w (first (filter (fn [[a b c]]
                                    (and (= (get @board a) (get @board b) (get @board c))
                                         (not= (get @board a) ".")))
                                  lines))]
        (println "Winner:" (get @board (first w)))
        (System/exit 0))))
  (recur))
```

**C:**
```c
#include <stdio.h>
int main() {
    char board[9]; for (int i = 0; i < 9; i++) board[i] = '.';
    char turn = 'x';
    int lines[8][3] = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
    while (1) {
        printf("%c move (1-9): ", turn);
        int m; scanf("%d", &m); m--;
        if (board[m] == '.') {
            board[m] = turn;
            turn = (turn == 'x') ? 'o' : 'x';
            for (int i = 0; i < 8; i++) {
                int a = lines[i][0], b = lines[i][1], c = lines[i][2];
                if (board[a] == board[b] && board[b] == board[c] && board[a] != '.') {
                    printf("Winner: %c\n", board[a]);
                    return 0;
                }
            }
        }
    }
}
```

**Bash:**
```bash
board=(. . . . . . . . .)
turn=x
while true; do
  read -p "$turn move (1-9): " m
  m=$((m - 1))
  if [ "${board[$m]}" = "." ]; then
    board[$m]=$turn
    [ "$turn" = "x" ] && turn=o || turn=x
  fi
done
```

</details>

**Note on True vs others:** in True, the board is a single `y`-container of 9 cells. The move is `board:y:N` (1-based). The win check is a separate loop with `^` / `?` — no native `for` loop. This is the "True way": explicit state machine, no closures.

---

## <a name="balance"></a>12. Game designer: RPG balance

### 44. RPG damage / balance table

**Compute expected DPS for a weapon, apply crit and armor, print a balance table.**

Given:
- base damage `dmg`
- crit chance `crit` (0–100)
- crit multiplier `critMult` (e.g. 1.5)
- enemy armor `armor` (0–100)
- target HP `hp`

Formula:
- `raw = dmg * (1 + crit/100 * (critMult - 1))`
- `final = raw * (1 - armor/100)`
- expected hits to kill a target with `hp` HP: `ceil(hp / final)`

```true
50 = dmg
25 = crit
1.5 = critMult
30 = armor
1000 = hp
crit, critMult, dmg, armor => :math = raw
raw, armor => :math = final
hp, final => :math = hits
raw, final, hits => :log
```

**Result:** `raw`, `final`, `hits` — balance table row.

**Python:**
```python
import math
dmg = 50
crit = 25
crit_mult = 1.5
armor = 30
hp = 1000
raw = dmg * (1 + crit / 100 * (crit_mult - 1))
final = raw * (1 - armor / 100)
hits = math.ceil(hp / final)
print(f"raw={raw:.2f} final={final:.2f} hits={hits}")
```

**Elixir:**
```elixir
dmg = 50
crit = 25
crit_mult = 1.5
armor = 30
hp = 1000
raw = dmg * (1 + crit / 100 * (crit_mult - 1))
final = raw * (1 - armor / 100)
hits = ceil(hp / final)
IO.puts("raw=#{raw} final=#{final} hits=#{hits}")
```

**Lua** (topic: games):
```lua
local dmg = 50
local crit = 25
local crit_mult = 1.5
local armor = 30
local hp = 1000
local raw = dmg * (1 + crit / 100 * (crit_mult - 1))
local final = raw * (1 - armor / 100)
local hits = math.ceil(hp / final)
print(string.format("raw=%.2f final=%.2f hits=%d", raw, final, hits))
```

<details>
<summary>Other languages (8)</summary>

**Kotlin:**
```kotlin
val dmg = 50.0
val crit = 25.0
val critMult = 1.5
val armor = 30.0
val hp = 1000.0
val raw = dmg * (1 + crit / 100 * (critMult - 1))
val final = raw * (1 - armor / 100)
val hits = kotlin.math.ceil(hp / final).toInt()
println("raw=$raw final=$final hits=$hits")
```

**Rust:**
```rust
let dmg = 50.0;
let crit = 25.0;
let crit_mult = 1.5;
let armor = 30.0;
let hp = 1000.0;
let raw = dmg * (1.0 + crit / 100.0 * (crit_mult - 1.0));
let final_dmg = raw * (1.0 - armor / 100.0);
let hits = (hp / final_dmg).ceil() as i32;
println!("raw={} final={} hits={}", raw, final_dmg, hits);
```

**Haskell:**
```haskell
let dmg = 50 :: Double
let crit = 25
let critMult = 1.5
let armor = 30
let hp = 1000
let raw = dmg * (1 + crit / 100 * (critMult - 1))
let final = raw * (1 - armor / 100)
let hits = ceiling (hp / final)
putStrLn ("raw=" ++ show raw ++ " final=" ++ show final ++ " hits=" ++ show hits)
```

**Erlang:**
```erlang
Dmg = 50.0,
Crit = 25.0,
CritMult = 1.5,
Armor = 30.0,
Hp = 1000.0,
Raw = Dmg * (1 + Crit / 100 * (CritMult - 1)),
Final = Raw * (1 - Armor / 100),
Hits = ceil(Hp / Final),
io:format("raw=~p final=~p hits=~p~n", [Raw, Final, Hits]).
```

**JavaScript:**
```javascript
const dmg = 50;
const crit = 25;
const critMult = 1.5;
const armor = 30;
const hp = 1000;
const raw = dmg * (1 + crit / 100 * (critMult - 1));
const final = raw * (1 - armor / 100);
const hits = Math.ceil(hp / final);
console.log(`raw=${raw} final=${final} hits=${hits}`);
```

**Go:**
```go
dmg := 50.0
crit := 25.0
critMult := 1.5
armor := 30.0
hp := 1000.0
raw := dmg * (1 + crit / 100 * (critMult - 1))
final := raw * (1 - armor / 100)
hits := int(math.Ceil(hp / final))
fmt.Printf("raw=%f final=%f hits=%d\n", raw, final, hits)
```

**Clojure:**
```clojure
(let [dmg 50.0 crit 25.0 crit-mult 1.5 armor 30.0 hp 1000.0
      raw (* dmg (+ 1 (* (/ crit 100) (- crit-mult 1))))
      final (* raw (- 1 (/ armor 100)))
      hits (long (Math/ceil (/ hp final)))]
  (println (str "raw=" raw " final=" final " hits=" hits)))
```

**C:**
```c
double dmg = 50, crit = 25, crit_mult = 1.5, armor = 30, hp = 1000;
double raw = dmg * (1 + crit / 100 * (crit_mult - 1));
double final = raw * (1 - armor / 100);
int hits = (int)ceil(hp / final);
printf("raw=%f final=%f hits=%d\n", raw, final, hits);
```

**Bash:**
```bash
dmg=50
crit=25
armor=30
hp=1000
raw=$(awk "BEGIN { print $dmg * (1 + $crit / 100 * 0.5) }")
final=$(awk "BEGIN { print $raw * (1 - $armor / 100) }")
hits=$(awk "BEGIN { print int(($hp / $final) + 0.999) }")
echo "raw=$raw final=$final hits=$hits"
```

</details>

**Note for game designers:** True's strength is **iteration via `^`/`?` + `:map`/`:reduce`/`:fold`/`:each`**. Balance tables are just containers — one row per weapon/enemy, all in a single file. Change a parameter, re-run, get a new table. **No import, no compile, no build** — the file is the spreadsheet.

---

## <a name="ui"></a>13. UI automation

### 49. Type into editor

**Types `hello` into the current editor, one character at a time.**

```true
hello, editor:current => :simple:type
:simple = typed
```

**Result:** `typed` in `Values` = `"1"`. The editor shows `h`, `he`, `hel`, `hell`, `hello` — each 30 ms apart.

**Why no comparison:** no direct equivalent in other languages — this is True + Simple specific (UI automation from code).

---

### 50. Type into search

**Types `world` into the search query field with no delay.**

```true
world, search:query, 0 => :simple:type
:simple => :log
```

**Result:** `1` in `Log`.

**Why no comparison:** no direct equivalent in other languages.

---

### 51. Click buttons

**Clicks `New Cycle`, then `Save`.**

```true
button:newCycle => :simple:click
button:save => :simple:click
:simple = saved
```

**Result:** `saved` in `Values`.

**Why no comparison:** no direct equivalent in other languages.

---

### 52. Click tab and agent

**Switches to `main.cycle`, then starts `agent-1`.**

```true
tab:main.cycle => :simple:click
agent:agent-1:play => :simple:click
:simple => :log
```

**Result:** `1` in `Log`.

**Why no comparison:** no direct equivalent in other languages.

---

### 53. Full scenario — click, type, click

**New cycle → type code → save → start all.**

```true
button:newCycle => :simple:click
1 = a, editor:current => :simple:type
=> :stop, editor:current => :simple:type
button:save => :simple:click
button:startAll => :simple:click
:simple => :log
```

**Result:** `1` in `Log`.

**Why no comparison:** no direct equivalent in other languages.

---

### 54. Type into terminal

**Opens a real OS terminal, sends a command, types another one character by character.**

```true
Console => :simple:openTerminal
ls -la, Console => :simple:sendTerminal
echo hello, Console => :simple:typeTerminal
Console => :simple:closeTerminal
:simple = done
```

**Result:** `done` in `Values`.

**Why no comparison:** no direct equivalent in other languages.

---

### 55. Click at coordinates (dangerous)

**Clicks at absolute screen coordinates via AWT Robot.**

```true
100, 200 => :simple:clickAt
```

**Result:** click at (100, 200) on screen.

**Warning:** dangerous, runtime only, no headless. May click anything — use only for tests.

---

## <a name="search"></a>14. Search

### 56. Search and log count

**Searches `hello`, logs the number of matching lines.**

```true
hello => :simple:search
:simple:search:count => :log
```

**Result:** `3` (or whatever) in `Log`.

**Why no comparison:** no direct equivalent in other languages.

---

### 57. Search, navigate, log current

**Searches `world`, moves to the next match, logs the current position.**

```true
world => :simple:search
:simple:search:next
:simple:search:current => :log
```

**Result:** `2` in `Log`.

**Why no comparison:** no direct equivalent in other languages.

---

### 58. Assert no TODOs

**Searches `TODO`, asserts the count is zero.**

```true
TODO => :simple:search
:simple:search:count = todoCount
todoCount == 0, "no TODOs left" => :assert
```

**Result:** `todoCount` in `Values`, `:assert` in `Log` — green if zero, red otherwise.

**Why no comparison:** no direct equivalent in other languages.

---

### 59. Clear search

**Clears the search query and matches.**

```true
=> :simple:search:clear
:simple => :log
```

**Result:** `1` in `Log`.

**Why no comparison:** no direct equivalent in other languages.