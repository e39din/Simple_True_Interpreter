# True — system functions

Language version: **0.11.0**

## How to read

- **Description** — one sentence.
- **True** — the sample. The result is in the container `:name`.
- **Inline comment** (only if True is 1 line **and** py/random are 1-line operations):
  - **short** — `\ py: \`...\` | <random>: \`...\`` (same line)
  - **long** — `\ py: \`...\`` on the **next line**
  - **2+ operations** or **multi-line** in py/random — comment omitted.
  - **random** — not py/rust/bash.
  - **no imports** (`import`, `require`, `#include`, `using`, `use`).
- **`<details>`** — all 12 languages (Python, Rust, Bash, Elixir, C, Kotlin, Haskell, Lua, Erlang, JavaScript, Go, Clojure).

**Naming rule:** `= name` is naming. `=>` is never naming. `chain => name` — error if `name` is not declared.

**Any subject starting with `:` is both a function and a container. No exceptions.** `:name` — substitution (empty string if not set). `:name => dst` / `:name = dst` — naming. `dst = :name` — error.

**System names are reserved** (`result`, `parameters`, `nothing`, `step`, `cycle`, `speed`, `color`, `error`, `log`, `message`, `size`, `s`, `math`, `data`, `json`, `file`, `api`, `console`, `simple`, `agent`, `run`, `range`, `repeat`, `activeTab`, `path`, `source`, `test`, `return`, `call`, `filter`, `matches`, `map`, `reduce`, `fold`, `each`).

## Table of contents

1. [Math — `:math`](#math)
2. [Data — `:data`](#data)
3. [Functional — `:map` / `:reduce` / `:fold` / `:each`](#func)
4. [JSON — `:json`](#json)
5. [Files — `:file`](#file)
6. [Network — `:api`](#api)
7. [Console — `:console`](#console)
8. [I/O — `:save` / `:load` / `:get` / `:post`](#io)
9. [Errors — `:error`](#error)
10. [Assert — `:assert`](#assert)
11. [Pause / Resume — `:pauseAll` / `:resumeAll`](#pause)
12. [Agents — `:agent`](#agent)
13. [Interpreter — `:simple`](#simple)
14. [Settings](#settings)
15. [Meta](#meta)

---

## <a name="math"></a>1. Math — `:math`

### `:math`

**Evaluates a numeric expression.**

```true
1 + 2 * 3 => :math \ py: `a = 1 + 2 * 3` | elixir: `a = 1 + 2 * 3`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = 1 + 2 * 3` |
| Rust | `let a = 1 + 2 * 3;` |
| Bash | `a=$((1 + 2 * 3))` |
| Elixir | `a = 1 + 2 * 3` |
| C | `int a = 1 + 2 * 3;` |
| Kotlin | `val a = 1 + 2 * 3` |
| Haskell | `let a = 1 + 2 * 3` |
| Lua | `local a = 1 + 2 * 3` |
| Erlang | `A = 1 + 2 * 3.` |
| JavaScript | `const a = 1 + 2 * 3;` |
| Go | `a := 1 + 2*3` |
| Clojure | `(def a (+ 1 (* 2 3)))` |

</details>

---

### `:sum` / `:math:sum`

**Sums all numbers.**

```true
1, 2, 3, 4, 5 => :sum \ py: `total = sum([1, 2, 3, 4, 5])` | kotlin: `val total = listOf(1, 2, 3, 4, 5).sum()`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `total = sum([1, 2, 3, 4, 5])` |
| Rust | `let total: i32 = [1, 2, 3, 4, 5].iter().sum();` |
| Bash | `total=0; for n in 1 2 3 4 5; do total=$((total+n)); done` |
| Elixir | `total = Enum.sum([1, 2, 3, 4, 5])` |
| C | `int total = 0; int arr[] = {1,2,3,4,5}; for (int i=0;i<5;i++) total += arr[i];` |
| Kotlin | `val total = listOf(1, 2, 3, 4, 5).sum()` |
| Haskell | `let total = sum [1, 2, 3, 4, 5]` |
| Lua | `local total = 1 + 2 + 3 + 4 + 5` |
| Erlang | `Total = lists:sum([1, 2, 3, 4, 5]).` |
| JavaScript | `const total = [1, 2, 3, 4, 5].reduce((x, y) => x + y, 0);` |
| Go | `total := 0; for _, v := range []int{1, 2, 3, 4, 5} { total += v }` |
| Clojure | `(def total (reduce + [1 2 3 4 5]))` |

</details>

---

### `:min` / `:math:min`

**Returns the minimum value.**

```true
3, 7, 2, 9, 4 => :min
\ py: `a = min([3, 7, 2, 9, 4])`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = min([3, 7, 2, 9, 4])` |
| Rust | `let a = [3, 7, 2, 9, 4].iter().min();` |
| Bash | `a=3; for n in 7 2 9 4; do [ "$n" -lt "$a" ] && a=$n; done` |
| Elixir | `a = Enum.min([3, 7, 2, 9, 4])` |
| C | `int a = 3; int arr[] = {3,7,2,9,4}; for (int i=1;i<5;i++) if (arr[i]<a) a=arr[i];` |
| Kotlin | `val a = listOf(3, 7, 2, 9, 4).minOrNull()` |
| Haskell | `let a = minimum [3, 7, 2, 9, 4]` |
| Lua | `local a = math.min(3, 7, 2, 9, 4)` |
| Erlang | `A = lists:min([3, 7, 2, 9, 4]).` |
| JavaScript | `const a = Math.min(...[3, 7, 2, 9, 4]);` |
| Go | `a := 3; for _, v := range []int{7, 2, 9, 4} { if v < a { a = v } }` |
| Clojure | `(def a (apply min [3 7 2 9 4]))` |

</details>

---

### `:max` / `:math:max`

**Returns the maximum value.**

```true
3, 7, 2, 9, 4 => :max
\ py: `a = max([3, 7, 2, 9, 4])`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = max([3, 7, 2, 9, 4])` |
| Rust | `let a = [3, 7, 2, 9, 4].iter().max();` |
| Bash | `a=3; for n in 7 2 9 4; do [ "$n" -gt "$a" ] && a=$n; done` |
| Elixir | `a = Enum.max([3, 7, 2, 9, 4])` |
| C | `int a = 3; int arr[] = {3,7,2,9,4}; for (int i=1;i<5;i++) if (arr[i]>a) a=arr[i];` |
| Kotlin | `val a = listOf(3, 7, 2, 9, 4).maxOrNull()` |
| Haskell | `let a = maximum [3, 7, 2, 9, 4]` |
| Lua | `local a = math.max(3, 7, 2, 9, 4)` |
| Erlang | `A = lists:max([3, 7, 2, 9, 4]).` |
| JavaScript | `const a = Math.max(...[3, 7, 2, 9, 4]);` |
| Go | `a := 3; for _, v := range []int{7, 2, 9, 4} { if v > a { a = v } }` |
| Clojure | `(def a (apply max [3 7 2 9 4]))` |

</details>

---

### `:avg` / `:math:avg`

**Returns the average.**

```true
1, 2, 3, 4, 5 => :avg
\ py: `a = sum([1, 2, 3, 4, 5]) / len([1, 2, 3, 4, 5])`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = sum([1, 2, 3, 4, 5]) / len([1, 2, 3, 4, 5])` |
| Rust | `let a: f64 = [1, 2, 3, 4, 5].iter().sum::<i32>() as f64 / 5.0;` |
| Bash | `a=$(awk "BEGIN { print (1+2+3+4+5)/5 }")` |
| Elixir | `a = Enum.sum([1,2,3,4,5]) / length([1,2,3,4,5])` |
| C | `int sum=0; int arr[]={1,2,3,4,5}; for(int i=0;i<5;i++) sum+=arr[i]; float a=(float)sum/5;` |
| Kotlin | `val a = listOf(1, 2, 3, 4, 5).average()` |
| Haskell | `let a = sum [1,2,3,4,5] / length [1,2,3,4,5]` |
| Lua | `local a = (1+2+3+4+5) / 5` |
| Erlang | `A = lists:sum([1,2,3,4,5]) / length([1,2,3,4,5]).` |
| JavaScript | `const a = [1,2,3,4,5].reduce((x,y)=>x+y,0) / 5;` |
| Go | `sum := 0; for _, v := range []int{1,2,3,4,5} { sum += v }; a := float64(sum) / 5` |
| Clojure | `(def a (/ (reduce + [1 2 3 4 5]) (count [1 2 3 4 5])))` |

</details>

---

### `:product` / `:math:product`

**Multiplies all numbers.**

```true
1, 2, 3, 4, 5 => :product \ py: `a = math.prod([1, 2, 3, 4, 5])` | haskell: `let a = product [1,2,3,4,5]`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = math.prod([1, 2, 3, 4, 5])` |
| Rust | `let a: i32 = [1, 2, 3, 4, 5].iter().product();` |
| Bash | `a=1; for n in 1 2 3 4 5; do a=$((a*n)); done` |
| Elixir | `a = Enum.reduce([1,2,3,4,5], 1, &(&1*&2))` |
| C | `int a=1; int arr[]={1,2,3,4,5}; for(int i=0;i<5;i++) a*=arr[i];` |
| Kotlin | `val a = listOf(1,2,3,4,5).fold(1) { x, y -> x * y }` |
| Haskell | `let a = product [1,2,3,4,5]` |
| Lua | `local a = 1*2*3*4*5` |
| Erlang | `A = lists:foldl(fun(X, Acc) -> X*Acc end, 1, [1,2,3,4,5]).` |
| JavaScript | `const a = [1,2,3,4,5].reduce((x,y)=>x*y,1);` |
| Go | `a := 1; for _, v := range []int{1,2,3,4,5} { a *= v }` |
| Clojure | `(def a (reduce * [1 2 3 4 5]))` |

</details>

---

### `:count` / `:math:count`

**Counts elements.**

```true
1, 2, 3, 4, 5 => :count \ py: `a = len([1, 2, 3, 4, 5])` | javascript: `const a = [1,2,3,4,5].length;`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = len([1, 2, 3, 4, 5])` |
| Rust | `let a = [1, 2, 3, 4, 5].len();` |
| Bash | `a=5` |
| Elixir | `a = length([1,2,3,4,5])` |
| C | `int a = 5;` |
| Kotlin | `val a = listOf(1,2,3,4,5).size` |
| Haskell | `let a = length [1,2,3,4,5]` |
| Lua | `local a = #{1,2,3,4,5}` |
| Erlang | `A = length([1,2,3,4,5]).` |
| JavaScript | `const a = [1,2,3,4,5].length;` |
| Go | `a := len([]int{1,2,3,4,5})` |
| Clojure | `(def a (count [1 2 3 4 5]))` |

</details>

---

### `:math:sort` (numbers)

**Sorts numbers ascending.**

```true
3, 1, 2 => :math:sort \ py: `a = sorted([3, 1, 2])` | clojure: `(def a (sort [3 1 2]))`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = sorted([3, 1, 2])` |
| Rust | `let mut a = [3, 1, 2]; a.sort();` |
| Bash | `a=($(printf '%s\n' 3 1 2 | sort -n))` |
| Elixir | `a = Enum.sort([3,1,2])` |
| C | `int a[]={3,1,2}; qsort(a,3,sizeof(int),cmp);` |
| Kotlin | `val a = listOf(3,1,2).sorted()` |
| Haskell | `let a = sort [3,1,2]` |
| Lua | `local a={3,1,2}; table.sort(a)` |
| Erlang | `A = lists:sort([3,1,2]).` |
| JavaScript | `const a = [3,1,2].sort((x,y)=>x-y);` |
| Go | `a := []int{3,1,2}; sort.Ints(a)` |
| Clojure | `(def a (sort [3 1 2]))` |

</details>

---

### `:math:popcount`

**Counts 1 bits.**

```true
popcount(255) => :math \ py: `a = bin(255).count("1")` | c: `int a = __builtin_popcount(255);`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = bin(255).count("1")` |
| Rust | `let a = 255u32.count_ones();` |
| Bash | `a=$(echo "obase=2;255" \| bc \| tr -cd 1 \| wc -c)` |
| Elixir | `a = 255 \|> Integer.digits(2) \|> Enum.count(&(&1==1))` |
| C | `int a = __builtin_popcount(255);` |
| Kotlin | `val a = 255.countOneBits()` |
| Haskell | `let a = popCount 255` |
| Lua | `local a = 8` |
| Erlang | `A = length([X \|\| <<X:1>> <= <<255>>, X == 1]).` |
| JavaScript | `const a = (255).toString(2).split("1").length - 1;` |
| Go | `a := bits.OnesCount(255)` |
| Clojure | `(def a (Long/bitCount 255))` |

</details>

---

## <a name="data"></a>2. Data — `:data`

### `:data:substring`

**Extracts a substring.**

```true
hello => :data:substring \ py: `out = "hello"[0:5]` | rust: `let out = &"hello"[0..5];`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "hello"[0:5]` |
| Rust | `let out = &"hello"[0..5];` |
| Bash | `out="${s:0:5}"` |
| Elixir | `out = String.slice("hello", 0..4)` |
| C | `char out[6]; strncpy(out, "hello", 5); out[5]='\0';` |
| Kotlin | `val out = "hello".substring(0, 5)` |
| Haskell | `let out = take 5 "hello"` |
| Lua | `local out = string.sub("hello", 1, 5)` |
| Erlang | `Out = string:substr("hello", 1, 5).` |
| JavaScript | `const out = "hello".substring(0, 5);` |
| Go | `out := "hello"[0:5]` |
| Clojure | `(def out (subs "hello" 0 5))` |

</details>

---

### `:data:indexOf`

**Finds a substring position (1-based).**

```true
hello, ll => :data:indexOf \ py: `out = "hello".find("ll") + 1` | lua: `local out = string.find("hello", "ll")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "hello".find("ll") + 1` |
| Rust | `let out = "hello".find("ll").map(|i| i + 1);` |
| Bash | `out=$(expr index "hello" ll)` |
| Elixir | `out = :binary.match("hello","ll") \|> elem(0) \|> Kernel.+(1)` |
| C | `char *p = strstr("hello","ll"); int out = p ? (p - "hello" + 1) : -1;` |
| Kotlin | `val out = "hello".indexOf("ll") + 1` |
| Haskell | `let out = 3` |
| Lua | `local out = string.find("hello", "ll")` |
| Erlang | `Out = string:str("hello", "ll").` |
| JavaScript | `const out = "hello".indexOf("ll") + 1;` |
| Go | `out := strings.Index("hello", "ll") + 1` |
| Clojure | `(def out (inc (.indexOf "hello" "ll")))` |

</details>

---

### `:data:replace`

**Replaces all occurrences.**

```true
hello, ll, LL => :data:replace \ py: `out = "hello".replace("ll", "LL")` | haskell: `let out = "heLLo"`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "hello".replace("ll", "LL")` |
| Rust | `let out = "hello".replace("ll", "LL");` |
| Bash | `out="${s//ll/LL}"` |
| Elixir | `out = String.replace("hello","ll","LL")` |
| C | `/* manual */` |
| Kotlin | `val out = "hello".replace("ll", "LL")` |
| Haskell | `let out = "heLLo"` |
| Lua | `local out = string.gsub("hello","ll","LL")` |
| Erlang | `Out = string:replace("hello","ll","LL",all).` |
| JavaScript | `const out = "hello".replace("ll", "LL");` |
| Go | `out := strings.Replace("hello", "ll", "LL", -1)` |
| Clojure | `(def out (clojure.string/replace "hello" "ll" "LL"))` |

</details>

---

### `:data:lower`

**Converts to lowercase.**

```true
HELLO => :data:lower \ py: `out = "HELLO".lower()` | lua: `local out = string.lower("HELLO")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "HELLO".lower()` |
| Rust | `let out = "HELLO".to_lowercase();` |
| Bash | `out="${s,,}"` |
| Elixir | `out = String.downcase("HELLO")` |
| C | `char out[6]; for(int i=0;i<5;i++) out[i]=tolower("HELLO"[i]); out[5]=0;` |
| Kotlin | `val out = "HELLO".lowercase()` |
| Haskell | `let out = map toLower "HELLO"` |
| Lua | `local out = string.lower("HELLO")` |
| Erlang | `Out = string:lowercase("HELLO").` |
| JavaScript | `const out = "HELLO".toLowerCase();` |
| Go | `out := strings.ToLower("HELLO")` |
| Clojure | `(def out (clojure.string/lower-case "HELLO"))` |

</details>

---

### `:data:upper`

**Converts to uppercase.**

```true
hello => :data:upper \ py: `out = "hello".upper()` | lua: `local out = string.upper("hello")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "hello".upper()` |
| Rust | `let out = "hello".to_uppercase();` |
| Bash | `out="${s^^}"` |
| Elixir | `out = String.upcase("hello")` |
| C | `char out[6]; for(int i=0;i<5;i++) out[i]=toupper("hello"[i]); out[5]=0;` |
| Kotlin | `val out = "hello".uppercase()` |
| Haskell | `let out = map toUpper "hello"` |
| Lua | `local out = string.upper("hello")` |
| Erlang | `Out = string:uppercase("hello").` |
| JavaScript | `const out = "hello".toUpperCase();` |
| Go | `out := strings.ToUpper("hello")` |
| Clojure | `(def out (clojure.string/upper-case "hello"))` |

</details>

---

### `:data:trim`

**Removes surrounding whitespace.**

```true
"  hi  " => :data:trim \ py: `out = "  hi  ".strip()` | kotlin: `val out = "  hi  ".trim()`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "  hi  ".strip()` |
| Rust | `let out = "  hi  ".trim();` |
| Bash | `out=$(echo "  hi  " \| xargs)` |
| Elixir | `out = String.trim("  hi  ")` |
| C | `/* manual trim */` |
| Kotlin | `val out = "  hi  ".trim()` |
| Haskell | `let out = trim "  hi  "` |
| Lua | `local out = "  hi  ":match("^%s*(.-)%s*$")` |
| Erlang | `Out = string:trim("  hi  ").` |
| JavaScript | `const out = "  hi  ".trim();` |
| Go | `out := strings.TrimSpace("  hi  ")` |
| Clojure | `(def out (clojure.string/trim "  hi  "))` |

</details>

---

### `:data:charAt`

**Gets a char by index (1-based).**

```true
hello, 2 => :data:charAt \ py: `out = "hello"[1]` | javascript: `const out = "hello".charAt(1);`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "hello"[1]` |
| Rust | `let out = "hello".chars().nth(1);` |
| Bash | `out="${s:1:1}"` |
| Elixir | `out = String.at("hello", 1)` |
| C | `char out = "hello"[1];` |
| Kotlin | `val out = "hello"[1]` |
| Haskell | `let out = "hello" !! 1` |
| Lua | `local out = string.sub("hello", 2, 2)` |
| Erlang | `Out = string:substr("hello", 2, 1).` |
| JavaScript | `const out = "hello".charAt(1);` |
| Go | `out := string("hello"[1])` |
| Clojure | `(def out (str (nth "hello" 1)))` |

</details>

---

### `:size` / `:s`

**Returns size by axis.**

```true
hello => :s \ py: `out = len("hello")` | rust: `let out = "hello".len();`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = len("hello")` |
| Rust | `let out = "hello".len();` |
| Bash | `out=${#s}` |
| Elixir | `out = String.length("hello")` |
| C | `int out = strlen("hello");` |
| Kotlin | `val out = "hello".length` |
| Haskell | `let out = length "hello"` |
| Lua | `local out = #"hello"` |
| Erlang | `Out = length("hello").` |
| JavaScript | `const out = "hello".length;` |
| Go | `out := len("hello")` |
| Clojure | `(def out (count "hello"))` |

</details>

---

### `:data:reverse`

**Reverses a container.**

```true
hello => :data:reverse \ py: `out = "hello"[::-1]` | elixir: `out = String.reverse("hello")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = "hello"[::-1]` |
| Rust | `let out: String = "hello".chars().rev().collect();` |
| Bash | `out=$(echo -n "hello" \| rev)` |
| Elixir | `out = String.reverse("hello")` |
| C | `/* manual */` |
| Kotlin | `val out = "hello".reversed()` |
| Haskell | `let out = reverse "hello"` |
| Lua | `local out = string.reverse("hello")` |
| Erlang | `Out = lists:reverse("hello").` |
| JavaScript | `const out = [..."hello"].reverse().join("");` |
| Go | `r:=[]rune("hello"); for i,j:=0,len(r)-1;i<j;i,j=i+1,j-1{r[i],r[j]=r[j],r[i]}; out:=string(r)` |
| Clojure | `(def out (clojure.string/reverse "hello"))` |

</details>

---

### `:push`

**Appends to a container.**

```true
1, 2 = arr
arr:y, 3 => :push
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `arr = [1, 2]; arr.append(3)` |
| Rust | `let mut arr = vec![1, 2]; arr.push(3);` |
| Bash | `arr=(1 2); arr+=(3)` |
| Elixir | `arr = [1, 2] ++ [3]` |
| C | `int arr[3]={1,2}; arr[2]=3;` |
| Kotlin | `val arr = mutableListOf(1, 2); arr.add(3)` |
| Haskell | `let arr = [1, 2] ++ [3]` |
| Lua | `local arr = {1, 2}; table.insert(arr, 3)` |
| Erlang | `Arr = [1, 2] ++ [3].` |
| JavaScript | `const arr = [1, 2]; arr.push(3);` |
| Go | `arr := []int{1, 2}; arr = append(arr, 3)` |
| Clojure | `(def arr (conj [1 2] 3))` |

</details>

---

### `:pop`

**Removes and returns the top.**

```true
1, 2, 3 = arr
arr:y => :pop
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `arr = [1, 2, 3]; top = arr.pop()` |
| Rust | `let mut arr = vec![1, 2, 3]; let top = arr.pop();` |
| Bash | `arr=(1 2 3); top="${arr[-1]}"; unset 'arr[-1]'` |
| Elixir | `arr = [1, 2, 3]; {top, rest} = List.pop_at(arr, -1)` |
| C | `int arr[3]={1,2,3}; int top=arr[2]; int n=2;` |
| Kotlin | `val arr = mutableListOf(1, 2, 3); val top = arr.removeAt(arr.size - 1)` |
| Haskell | `let arr = [1, 2, 3]; let (top, rest) = (last arr, init arr)` |
| Lua | `local arr = {1, 2, 3}; local top = table.remove(arr)` |
| Erlang | `{Top, Rest} = lists:split(length(Arr) - 1, Arr).` |
| JavaScript | `const arr = [1, 2, 3]; const top = arr.pop();` |
| Go | `arr := []int{1, 2, 3}; top := arr[len(arr)-1]; arr = arr[:len(arr)-1]` |
| Clojure | `(def arr [1 2 3]); (def top (peek arr)); (def rest (pop arr))` |

</details>

---

### `:peek`

**Returns top without removal.**

```true
1, 2, 3 = arr
arr:y => :peek
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `arr = [1, 2, 3]; top = arr[-1]` |
| Rust | `let arr = vec![1, 2, 3]; let top = arr.last();` |
| Bash | `arr=(1 2 3); top="${arr[-1]}"` |
| Elixir | `arr = [1, 2, 3]; top = List.last(arr)` |
| C | `int arr[3]={1,2,3}; int top=arr[2];` |
| Kotlin | `val arr = listOf(1, 2, 3); val top = arr.last()` |
| Haskell | `let arr = [1, 2, 3]; let top = last arr` |
| Lua | `local arr = {1, 2, 3}; local top = arr[#arr]` |
| Erlang | `Top = lists:last(Arr).` |
| JavaScript | `const arr = [1, 2, 3]; const top = arr[arr.length - 1];` |
| Go | `arr := []int{1, 2, 3}; top := arr[len(arr)-1]` |
| Clojure | `(def top (peek [1 2 3]))` |

</details>

---

## <a name="func"></a>3. Functional — `:map` / `:reduce` / `:fold` / `:each`

### `:map`

**Applies an expression to every element.**

```true
1, 2, 3 = arr
arr:y, n * 2 => :map
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `arr = [1, 2, 3]; doubles = [n * 2 for n in arr]` |
| Rust | `let doubles: Vec<i32> = arr.into_iter().map(\|n\| n * 2).collect();` |
| Bash | `arr=(1 2 3); doubles=(); for n in "${arr[@]}"; do doubles+=($((n * 2))); done` |
| Elixir | `doubles = Enum.map([1,2,3], &(&1 * 2))` |
| C | `int arr[]={1,2,3}; int doubles[3]; for(int i=0;i<3;i++) doubles[i]=arr[i]*2;` |
| Kotlin | `val doubles = listOf(1,2,3).map { it * 2 }` |
| Haskell | `let doubles = map (*2) [1,2,3]` |
| Lua | `local doubles = {}; for _,v in ipairs({1,2,3}) do table.insert(doubles, v*2) end` |
| Erlang | `Doubles = lists:map(fun(X) -> X*2 end, [1,2,3]).` |
| JavaScript | `const doubles = [1,2,3].map(n => n * 2);` |
| Go | `doubles := []int{}; for _,v := range []int{1,2,3} { doubles = append(doubles, v*2) }` |
| Clojure | `(def doubles (map #(* 2 %) [1 2 3]))` |

</details>

---

### `:reduce`

**Folds a container with an accumulator.**

```true
1, 2, 3 = arr
arr:y, 0, acc + n => :reduce
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `total = reduce(lambda acc, n: acc + n, [1,2,3], 0)` |
| Rust | `let total = [1,2,3].iter().fold(0, \|acc, n\| acc + n);` |
| Bash | `total=0; for n in 1 2 3; do total=$((total+n)); done` |
| Elixir | `total = Enum.reduce([1,2,3], 0, fn n, acc -> acc + n end)` |
| C | `int arr[]={1,2,3}; int total=0; for(int i=0;i<3;i++) total+=arr[i];` |
| Kotlin | `val total = listOf(1,2,3).fold(0) { acc, n -> acc + n }` |
| Haskell | `let total = foldl (+) 0 [1,2,3]` |
| Lua | `local total = 0; for _,v in ipairs({1,2,3}) do total = total + v end` |
| Erlang | `Total = lists:foldl(fun(X, Acc) -> Acc + X end, 0, [1,2,3]).` |
| JavaScript | `const total = [1,2,3].reduce((acc, n) => acc + n, 0);` |
| Go | `total := 0; for _,v := range []int{1,2,3} { total += v }` |
| Clojure | `(def total (reduce + 0 [1 2 3]))` |

</details>

---

### `:fold`

**Synonym for `:reduce`.**

```true
1, 2, 3 = arr
arr:y, 1, acc * n => :fold
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `prod = reduce(lambda acc, n: acc * n, [1,2,3], 1)` |
| Rust | `let prod = [1,2,3].iter().fold(1, \|acc, n\| acc * n);` |
| Bash | `prod=1; for n in 1 2 3; do prod=$((prod*n)); done` |
| Elixir | `prod = Enum.reduce([1,2,3], 1, fn n, acc -> acc * n end)` |
| C | `int arr[]={1,2,3}; int prod=1; for(int i=0;i<3;i++) prod*=arr[i];` |
| Kotlin | `val prod = listOf(1,2,3).fold(1) { acc, n -> acc * n }` |
| Haskell | `let prod = foldl (*) 1 [1,2,3]` |
| Lua | `local prod = 1; for _,v in ipairs({1,2,3}) do prod = prod * v end` |
| Erlang | `Prod = lists:foldl(fun(X, Acc) -> Acc * X end, 1, [1,2,3]).` |
| JavaScript | `const prod = [1,2,3].reduce((acc, n) => acc * n, 1);` |
| Go | `prod := 1; for _,v := range []int{1,2,3} { prod *= v }` |
| Clojure | `(def prod (reduce * 1 [1 2 3]))` |

</details>

---

### `:each`

**Executes an operation for every element.**

```true
1, 2, 3 = arr
arr:y, n => :log => :each
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `for n in [1,2,3]: print(n)` |
| Rust | `[1,2,3].iter().for_each(\|n\| println!("{}", n));` |
| Bash | `for n in 1 2 3; do echo "$n"; done` |
| Elixir | `Enum.each([1,2,3], fn n -> IO.puts(n) end)` |
| C | `int arr[]={1,2,3}; for(int i=0;i<3;i++) printf("%d\n", arr[i]);` |
| Kotlin | `listOf(1,2,3).forEach { println(it) }` |
| Haskell | `mapM_ print [1,2,3]` |
| Lua | `for _,v in ipairs({1,2,3}) do print(v) end` |
| Erlang | `lists:foreach(fun(X) -> io:format("~p~n", [X]) end, [1,2,3]).` |
| JavaScript | `[1,2,3].forEach(n => console.log(n));` |
| Go | `for _,v := range []int{1,2,3} { fmt.Println(v) }` |
| Clojure | `(doseq [n [1 2 3]] (println n))` |

</details>

---

## <a name="json"></a>4. JSON — `:json`

### `:json:get`

**Gets a value from JSON by key.**

```true
{'name':'test','value':42} = json
json, name => :json:get
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `result = loads('{"name":"test"}')["name"]` |
| Rust | `let result = data["name"].as_str();` |
| Bash | `result=$(echo '{"name":"test"}' \| jq -r .name)` |
| Elixir | `result = decode!("{\"name\":\"test\"}")["name"]` |
| C | `/* via jansson */` |
| Kotlin | `val result = Json.parseToJsonElement("""{"name":"test"}""").jsonObject["name"]?.toString()` |
| Haskell | `-- via aeson` |
| Lua | `local result = decode('{"name":"test"}').name` |
| Erlang | `Result = proplists:get_value(<<"name">>, jsx:decode(<<"{\"name\":\"test\"}">>)).` |
| JavaScript | `const result = JSON.parse('{"name":"test"}').name;` |
| Go | `var data map[string]any; json.Unmarshal([]byte(`{"name":"test"}`), &data); result := data["name"]` |
| Clojure | `(def result (:name (parse-string "{\"name\":\"test\"}" true)))` |

</details>

---

### `:json:set`

**Sets a value in JSON by key.**

```true
{'a':1} = json
json, a, 2 => :json:set
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `d = loads('{"a":1}'); d["a"]=2; result=dumps(d)` |
| Rust | `data["a"] = json!(2); let result = data.to_string();` |
| Bash | `result=$(echo '{"a":1}' \| jq '.a = 2')` |
| Elixir | `result = encode!(Map.put(decode!("{\"a\":1}"), "a", 2))` |
| C | `/* via jansson */` |
| Kotlin | `val obj = Json.parseToJsonElement("""{"a":1}""").jsonObject.toMutableMap(); obj["a"]=JsonPrimitive(2); val result = JsonObject(obj).toString()` |
| Haskell | `-- via aeson + lens` |
| Lua | `local d = decode('{"a":1}'); d.a=2; local result = encode(d)` |
| Erlang | `Result = jsx:encode([{<<"a">>, 2}]).` |
| JavaScript | `const d = JSON.parse('{"a":1}'); d.a=2; const result = JSON.stringify(d);` |
| Go | `data["a"] = 2; result, _ := json.Marshal(data)` |
| Clojure | `(def result (generate-string (assoc (parse-string "{\"a\":1}" true) :a 2)))` |

</details>

---

## <a name="file"></a>5. Files — `:file`

### `:file:create`

**Creates a new file with content.**

```true
/tmp/out.txt, hello => :file:create
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `open("/tmp/out.txt","x").write("hello")` |
| Rust | `OpenOptions::new().write(true).create_new(true).open("/tmp/out.txt")?.write_all(b"hello")?;` |
| Bash | `{ printf 'hello' > /tmp/out.txt; } 2>/dev/null` |
| Elixir | `File.write("/tmp/out.txt", "hello", [:exclusive])` |
| C | `FILE *f=fopen("/tmp/out.txt","wx"); if(f){fputs("hello",f);fclose(f);}` |
| Kotlin | `File("/tmp/out.txt").apply { if (exists()) throw IOException() }.writeText("hello")` |
| Haskell | `withFile "/tmp/out.txt" WriteMode (\h -> hPutStr h "hello")` |
| Lua | `local f = io.open("/tmp/out.txt", "wx"); if f then f:write("hello"); f:close() end` |
| Erlang | `{ok, F} = file:open("/tmp/out.txt", [write, exclusive]), file:write(F, <<"hello">>), file:close(F).` |
| JavaScript | `writeFileSync("/tmp/out.txt", "hello", { flag: "wx" });` |
| Go | `f, _ := os.OpenFile("/tmp/out.txt", os.O_CREATE\|os.O_EXCL\|os.O_WRONLY, 0644); f.WriteString("hello"); f.Close()` |
| Clojure | `(spit "/tmp/out.txt" "hello" :x)` |

</details>

---

### `:file:read`

**Reads a file.**

```true
/tmp/out.txt => :file:read
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = open("/tmp/out.txt").read()` |
| Rust | `let out = std::fs::read_to_string("/tmp/out.txt")?;` |
| Bash | `out=$(cat /tmp/out.txt)` |
| Elixir | `{:ok, out} = File.read("/tmp/out.txt")` |
| C | `FILE *f=fopen("/tmp/out.txt","r"); char out[1024]; fread(out,1,1024,f); fclose(f);` |
| Kotlin | `val out = File("/tmp/out.txt").readText()` |
| Haskell | `out <- readFile "/tmp/out.txt"` |
| Lua | `local f = io.open("/tmp/out.txt","r"); local out = f:read("*a"); f:close()` |
| Erlang | `{ok, Bin} = file:read_file("/tmp/out.txt"), Out = binary_to_list(Bin).` |
| JavaScript | `const out = readFileSync("/tmp/out.txt", "utf8");` |
| Go | `data, _ := os.ReadFile("/tmp/out.txt"); out := string(data)` |
| Clojure | `(def out (slurp "/tmp/out.txt"))` |

</details>

---

### `:file:update`

**Writes content to a file.**

```true
/tmp/out.txt, world => :file:update
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `open("/tmp/out.txt","w").write("world")` |
| Rust | `std::fs::write("/tmp/out.txt", "world")?;` |
| Bash | `printf 'world' > /tmp/out.txt` |
| Elixir | `File.write("/tmp/out.txt", "world")` |
| C | `FILE *f=fopen("/tmp/out.txt","w"); fputs("world",f); fclose(f);` |
| Kotlin | `File("/tmp/out.txt").writeText("world")` |
| Haskell | `writeFile "/tmp/out.txt" "world"` |
| Lua | `local f=io.open("/tmp/out.txt","w"); f:write("world"); f:close()` |
| Erlang | `file:write_file("/tmp/out.txt", <<"world">>).` |
| JavaScript | `writeFileSync("/tmp/out.txt", "world");` |
| Go | `os.WriteFile("/tmp/out.txt", []byte("world"), 0644)` |
| Clojure | `(spit "/tmp/out.txt" "world")` |

</details>

---

### `:file:delete`

**Deletes a file.**

```true
/tmp/out.txt => :file:delete
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `os.remove("/tmp/out.txt")` |
| Rust | `std::fs::remove_file("/tmp/out.txt")?;` |
| Bash | `rm -f /tmp/out.txt` |
| Elixir | `File.rm("/tmp/out.txt")` |
| C | `remove("/tmp/out.txt");` |
| Kotlin | `File("/tmp/out.txt").delete()` |
| Haskell | `removeFile "/tmp/out.txt"` |
| Lua | `os.remove("/tmp/out.txt")` |
| Erlang | `file:delete("/tmp/out.txt").` |
| JavaScript | `unlinkSync("/tmp/out.txt");` |
| Go | `os.Remove("/tmp/out.txt")` |
| Clojure | `(clojure.java.io/delete-file "/tmp/out.txt")` |

</details>

---

### `:file:exists`

**Checks if a file exists.**

```true
/tmp/out.txt => :file:exists
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `r = 1 if exists("/tmp/out.txt") else 0` |
| Rust | `let r = if std::path::Path::new("/tmp/out.txt").exists() { 1 } else { 0 };` |
| Bash | `[ -f /tmp/out.txt ] && r=1 \|\| r=0` |
| Elixir | `r = if File.exists?("/tmp/out.txt"), do: 1, else: 0` |
| C | `int r = (access("/tmp/out.txt", F_OK) == 0) ? 1 : 0;` |
| Kotlin | `val r = if (File("/tmp/out.txt").exists()) 1 else 0` |
| Haskell | `e <- doesFileExist "/tmp/out.txt"; let r = if e then 1 else 0` |
| Lua | `local f = io.open("/tmp/out.txt","r"); local r = f and 1 or 0; if f then f:close() end` |
| Erlang | `R = case filelib:is_file("/tmp/out.txt") of true -> 1; false -> 0 end.` |
| JavaScript | `const r = existsSync("/tmp/out.txt") ? 1 : 0;` |
| Go | `r := 0; if _, err := os.Stat("/tmp/out.txt"); err == nil { r = 1 }` |
| Clojure | `(def r (if (.exists (clojure.java.io/file "/tmp/out.txt")) 1 0))` |

</details>

---

### `:path`

**Returns the absolute path of a file.**

```true
:file:main.cycle => :path \ py: `p = abspath("main.cycle")` | elixir: `p = Path.expand("main.cycle")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `p = abspath("main.cycle")` |
| Rust | `let p = std::fs::canonicalize("main.cycle")?;` |
| Bash | `p=$(realpath main.cycle)` |
| Elixir | `p = Path.expand("main.cycle")` |
| C | `char p[PATH_MAX]; realpath("main.cycle", p);` |
| Kotlin | `val p = File("main.cycle").absolutePath` |
| Haskell | `p <- canonicalizePath "main.cycle"` |
| Lua | `local p = io.popen("realpath main.cycle"):read("*a")` |
| Erlang | `{ok, Cwd} = file:get_cwd(), P = filename:join(Cwd, "main.cycle").` |
| JavaScript | `const p = resolve("main.cycle");` |
| Go | `p, _ := filepath.Abs("main.cycle")` |
| Clojure | `(def p (.getAbsolutePath (clojure.java.io/file "main.cycle")))` |

</details>

---

## <a name="api"></a>6. Network — `:api`

### `:api:get`

**HTTP GET.**

```true
https://example.com/health => :api:get \ py: `out = get("https://example.com/health").text` | elixir: `{:ok, resp} = HTTPoison.get("https://example.com/health")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = get("https://example.com/health").text` |
| Rust | `let out = reqwest::get("https://example.com/health").await?.text().await?;` |
| Bash | `out=$(curl -s https://example.com/health)` |
| Elixir | `{:ok, resp} = HTTPoison.get("https://example.com/health"); out = resp.body` |
| C | `/* via libcurl */` |
| Kotlin | `val out = java.net.URL("https://example.com/health").readText()` |
| Haskell | `out <- getResponseBody <$> httpLBS "https://example.com/health"` |
| Lua | `local out = require("socket.http").request("https://example.com/health")` |
| Erlang | `{ok,{{_,200,_},_,Body}} = httpc:request("https://example.com/health"), Out = Body.` |
| JavaScript | `const out = await (await fetch("https://example.com/health")).text();` |
| Go | `resp,_ := http.Get("https://example.com/health"); body,_ := io.ReadAll(resp.Body); out := string(body)` |
| Clojure | `(def out (:body (http/get "https://example.com/health")))` |

</details>

---

### `:api:post`

**HTTP POST with body.**

```true
https://example.com/api, {"a":1} => :api:post
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `r = post("https://example.com/api", data='{"a":1}'); out = 1 if r.ok else -1` |
| Rust | `let resp = Client::new().post("https://example.com/api").body(r#"{"a":1}"#).send().await?; let out = if resp.status().is_success() { 1 } else { -1 };` |
| Bash | `code=$(curl -s -o /dev/null -w "%{http_code}" -X POST -d '{"a":1}' https://example.com/api); [ "$code" -lt 300 ] && out=1 \|\| out=-1` |
| Elixir | `{:ok, resp} = HTTPoison.post("https://example.com/api", ~s({"a":1})); out = if resp.status_code < 300, do: 1, else: -1` |
| C | `/* via libcurl */` |
| Kotlin | `val conn = java.net.URL("https://example.com/api").openConnection() as java.net.HttpURLConnection; conn.requestMethod = "POST"; conn.doOutput = true; conn.outputStream.write("""{"a":1}""".toByteArray()); val out = if (conn.responseCode < 300) 1 else -1` |
| Haskell | `-- same pattern as GET` |
| Lua | `-- via luasocket` |
| Erlang | `{ok,{{_,Code,_},_,_}} = httpc:request(post, {"https://example.com/api", [], "application/json", <<"{\"a\":1}">>}, [], []), Out = case Code < 300 of true -> 1; false -> -1 end.` |
| JavaScript | `fetch("https://example.com/api", { method: "POST", body: '{"a":1}' }).then(r => console.log(r.ok ? 1 : -1));` |
| Go | `resp, _ := http.Post("https://example.com/api", "application/json", strings.NewReader(`{"a":1}`)); out := -1; if resp.StatusCode < 300 { out = 1 }` |
| Clojure | `(def resp (http/post "https://example.com/api" {:body "{\"a\":1}"})); (def out (if (< (:status resp) 300) 1 -1))` |

</details>

---

## <a name="console"></a>7. Console — `:console`

### `:console`

**Executes an OS command.**

```true
ls -la => :console
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = check_output("ls -la", shell=True).decode()` |
| Rust | `let out = Command::new("sh").arg("-c").arg("ls -la").output()?.stdout;` |
| Bash | `out=$(ls -la)` |
| Elixir | `{out, _} = System.cmd("sh", ["-c", "ls -la"])` |
| C | `FILE *p = popen("ls -la", "r"); /* read p */ pclose(p);` |
| Kotlin | `val out = Runtime.getRuntime().exec("ls -la").inputStream.bufferedReader().readText()` |
| Haskell | `out <- readProcess "ls" ["-la"] ""` |
| Lua | `local p = io.popen("ls -la"); local out = p:read("*a"); p:close()` |
| Erlang | `Out = os:cmd("ls -la").` |
| JavaScript | `const out = execSync("ls -la").toString();` |
| Go | `outBytes, _ := exec.Command("sh", "-c", "ls -la").Output(); out := string(outBytes)` |
| Clojure | `(def out (:out (clojure.java.shell/sh "ls" "-la")))` |

</details>

---

## <a name="io"></a>8. I/O — `:save` / `:load` / `:get` / `:post`

### `:save`

**Saves data via HTTP POST.**

```true
/tmp/out.txt, hello => :save
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `r = post("/tmp/out.txt", data="hello"); out = 1 if r.ok else -1` |
| Rust | `let resp = Client::new().post("/tmp/out.txt").body("hello").send().await?; let out = if resp.status().is_success() { 1 } else { -1 };` |
| Bash | `curl -s -o /dev/null -X POST -d 'hello' /tmp/out.txt && out=1 \|\| out=-1` |
| Elixir | `{:ok, resp} = HTTPoison.post("/tmp/out.txt", "hello"); out = if resp.status_code < 300, do: 1, else: -1` |
| C | `/* via libcurl */` |
| Kotlin | `val conn = java.net.URL("/tmp/out.txt").openConnection() as java.net.HttpURLConnection; conn.requestMethod = "POST"; conn.doOutput = true; conn.outputStream.write("hello".toByteArray()); val out = if (conn.responseCode < 300) 1 else -1` |
| Haskell | `-- same pattern as POST` |
| Lua | `-- via luasocket` |
| Erlang | `httpc:request(post, {"/tmp/out.txt", [], "", "hello"}, [], []).` |
| JavaScript | `fetch("/tmp/out.txt", { method: "POST", body: "hello" }).then(r => console.log(r.ok ? 1 : -1));` |
| Go | `resp, _ := http.Post("/tmp/out.txt", "text/plain", strings.NewReader("hello")); out := -1; if resp.StatusCode < 300 { out = 1 }` |
| Clojure | `(http/post "/tmp/out.txt" {:body "hello"})` |

</details>

---

### `:load`

**Loads data via HTTP GET.**

```true
/tmp/in.txt => :load
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = get("/tmp/in.txt").text` |
| Rust | `let out = reqwest::get("/tmp/in.txt").await?.text().await?;` |
| Bash | `out=$(curl -s /tmp/in.txt)` |
| Elixir | `{:ok, resp} = HTTPoison.get("/tmp/in.txt"); out = resp.body` |
| C | `/* via libcurl */` |
| Kotlin | `val out = java.net.URL("/tmp/in.txt").readText()` |
| Haskell | `out <- getResponseBody <$> httpLBS "/tmp/in.txt"` |
| Lua | `local out = require("socket.http").request("/tmp/in.txt")` |
| Erlang | `{ok,{{_,200,_},_,Body}} = httpc:request("/tmp/in.txt"), Out = Body.` |
| JavaScript | `fetch("/tmp/in.txt").then(r => r.text()).then(console.log);` |
| Go | `resp, _ := http.Get("/tmp/in.txt"); body, _ := io.ReadAll(resp.Body); out := string(body)` |
| Clojure | `(def out (:body (http/get "/tmp/in.txt")))` |

</details>

---

## <a name="error"></a>9. Errors — `:error`

### `:error`

**Returns `"1"` if there are errors, `"0"` otherwise.**

```true
:error
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `dst = "1" if error_flag else "0"` |
| Rust | `let dst = if error_flag { "1" } else { "0" };` |
| Bash | `dst=$([ "$error_flag" = "1" ] && echo 1 \|\| echo 0)` |
| Elixir | `dst = if error_flag, do: "1", else: "0"` |
| C | `const char *dst = error_flag ? "1" : "0";` |
| Kotlin | `val dst = if (errorFlag) "1" else "0"` |
| Haskell | `let dst = if errorFlag then "1" else "0"` |
| Lua | `local dst = error_flag and "1" or "0"` |
| Erlang | `Dst = case ErrorFlag of true -> "1"; false -> "0" end.` |
| JavaScript | `const dst = errorFlag ? "1" : "0";` |
| Go | `dst := "0"; if errorFlag { dst = "1" }` |
| Clojure | `(def dst (if error-flag "1" "0"))` |

</details>

---

### `message => :error`

**Sets an error. Does not pause by default.**

```true
!(a == b) ? "a != b" => :error
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `if not (a == b): raise AssertionError("a != b")` |
| Rust | `assert_eq!(a, b, "a != b");` |
| Bash | `[ "$a" = "$b" ] \|\| { echo "a != b" >&2; exit 1; }` |
| Elixir | `if a != b, do: raise "a != b"` |
| C | `if (a != b) { fprintf(stderr, "a != b\n"); exit(1); }` |
| Kotlin | `if (a != b) throw AssertionError("a != b")` |
| Haskell | `if a /= b then error "a != b" else ()` |
| Lua | `if a ~= b then error("a != b") end` |
| Erlang | `case A == B of true -> ok; false -> erlang:error("a != b") end.` |
| JavaScript | `if (a !== b) throw new Error("a != b");` |
| Go | `if a != b { panic("a != b") }` |
| Clojure | `(when (not= a b) (throw (AssertionError. "a != b")))` |

</details>

---

### `0 => :error:resolve`

**Marks all errors as resolved.**

```true
0 => :error:resolve
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `for e in errors: e.resolved = True` |
| Rust | `for e in errors.iter_mut() { e.resolved = true; }` |
| Bash | `# not idiomatic` |
| Elixir | `Enum.each(errors, fn e -> Map.put(e, :resolved, true) end)` |
| C | `for (int i = 0; i < n; i++) errors[i].resolved = 1;` |
| Kotlin | `errors.forEach { it.resolved = true }` |
| Haskell | `let errors' = map (\e -> e { resolved = True }) errors` |
| Lua | `for _, e in ipairs(errors) do e.resolved = true end` |
| Erlang | `Errors2 = [E#err{resolved = true} \|\| E <- Errors].` |
| JavaScript | `errors.forEach(e => e.resolved = true);` |
| Go | `for i := range errors { errors[i].Resolved = true }` |
| Clojure | `(doseq [e errors] (swap! e assoc :resolved true))` |

</details>

---

## <a name="assert"></a>10. Assert — `:assert`

### `:assert`

**Asserts a condition (success — green, failure — red). Does not pause by default.**

```true
a == b, "a must equal b" => :assert \ py: `assert a == b, "a must equal b"` | elixir: `if a != b, do: raise "a must equal b"`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `assert a == b, "a must equal b"` |
| Rust | `assert_eq!(a, b, "a must equal b");` |
| Bash | `[ "$a" = "$b" ] \|\| { echo "a must equal b" >&2; exit 1; }` |
| Elixir | `if a != b, do: raise "a must equal b"` |
| C | `if (a != b) { fprintf(stderr, "a must equal b\n"); exit(1); }` |
| Kotlin | `check(a == b) { "a must equal b" }` |
| Haskell | `if a /= b then error "a must equal b" else ()` |
| Lua | `assert(a == b, "a must equal b")` |
| Erlang | `case A == B of true -> ok; false -> erlang:error("a must equal b") end.` |
| JavaScript | `console.assert(a === b, "a must equal b");` |
| Go | `if a != b { panic("a must equal b") }` |
| Clojure | `(assert (= a b) "a must equal b")` |

</details>

---

## <a name="pause"></a>11. Pause / Resume — `:pauseAll` / `:resumeAll`

### `:pauseAll`

**Pauses all agents.**

```true
:pauseAll
```

### `:resumeAll`

**Resumes all agents.**

```true
:resumeAll
```

---

## <a name="agent"></a>12. Agents — `:agent`

### `:agent:<name>:status`

**Gets a named agent's status.**

```true
:agent:main:status = dst \ py: `dst = agents["main"].status` | elixir: `dst = Agent.get(:"main", & &1.status)`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `dst = agents["main"].status` |
| Rust | `let dst = main_handle.state().to_string();` |
| Bash | `dst=$main_status` |
| Elixir | `dst = Agent.get(:"main", & &1.status)` |
| C | `const char *dst = main_agent.status_name;` |
| Kotlin | `val dst = agentStatus["main"]` |
| Haskell | `dst <- readMVar mainStatus` |
| Lua | `local dst = coroutine.status(main)` |
| Erlang | `Dst = sys:get_state(MainPid).` |
| JavaScript | `const dst = agents["main"].status;` |
| Go | `dst := agents["main"].Status` |
| Clojure | `(def dst (:status @main-agent))` |

</details>

---

### `:agent:status`

**Gets all agents' statuses.**

```true
:agent:status = dst
\ py: `dst = ",".join(f"{k}:{v.status}" for k, v in agents.items())`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `dst = ",".join(f"{k}:{v.status}" for k, v in agents.items())` |
| Rust | `let dst = agents.iter().map(\|(k, v)\| format!("{}:{}", k, v.status)).collect::<Vec<_>>().join(",");` |
| Bash | `dst=$(for k in "${!agents[@]}"; do echo -n "$k:${agents[$k]},"; done \| sed 's/,$//')` |
| Elixir | `dst = Enum.map_join(agents, ",", fn {k, v} -> "#{k}:#{v.status}" end)` |
| C | `/* manual assembly */` |
| Kotlin | `val dst = agents.entries.joinToString(",") { "${it.key}:${it.value.status}" }` |
| Haskell | `let dst = intercalate "," (map (\(k, v) -> k ++ ":" ++ show (agentStatus v)) (Map.toList agents))` |
| Lua | `local parts = {}; for k, v in pairs(agents) do table.insert(parts, k .. ":" .. v.status) end; local dst = table.concat(parts, ",")` |
| Erlang | `Dst = string:join([atom_to_list(K) ++ ":" ++ atom_to_list(V#agent.status) \|\| {K, V} <- Agents], ",").` |
| JavaScript | `const dst = Object.entries(agents).map(([k, v]) => `${k}:${v.status}`).join(",");` |
| Go | `parts := []string{}; for k, v := range agents { parts = append(parts, k+":"+v.Status) }; dst := strings.Join(parts, ",")` |
| Clojure | `(def dst (clojure.string/join "," (map (fn [[k v]] (str k ":" (:status v))) @agents)))` |

</details>

---

### `:agent:wait`

**Pauses the agent for N ms.**

```true
100 => :agent:wait
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `time.sleep(0.1)` |
| Rust | `std::thread::sleep(std::time::Duration::from_millis(100));` |
| Bash | `sleep 0.1` |
| Elixir | `Process.sleep(100)` |
| C | `usleep(100 * 1000);` |
| Kotlin | `Thread.sleep(100)` |
| Haskell | `threadDelay 100000` |
| Lua | `-- via socket.sleep` |
| Erlang | `timer:sleep(100).` |
| JavaScript | `await new Promise(r => setTimeout(r, 100));` |
| Go | `time.Sleep(100 * time.Millisecond)` |
| Clojure | `(Thread/sleep 100)` |

</details>

---

## <a name="simple"></a>13. Interpreter — `:simple`

### `:simple:open`

**Opens a file.**

```true
/tmp/main.cycle => :simple:open
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `Popen(["code", "/tmp/main.cycle"])` |
| Rust | `open::that("/tmp/main.cycle")?;` |
| Bash | `xdg-open /tmp/main.cycle &` |
| Elixir | `System.cmd("xdg-open", ["/tmp/main.cycle"])` |
| C | `system("xdg-open /tmp/main.cycle &");` |
| Kotlin | `Desktop.getDesktop().open(File("/tmp/main.cycle"))` |
| Haskell | `callCommand "xdg-open /tmp/main.cycle &"` |
| Lua | `os.execute("xdg-open /tmp/main.cycle &")` |
| Erlang | `os:cmd("xdg-open /tmp/main.cycle &").` |
| JavaScript | `spawn("xdg-open", ["/tmp/main.cycle"]);` |
| Go | `exec.Command("xdg-open", "/tmp/main.cycle").Start()` |
| Clojure | `(clojure.java.shell/sh "xdg-open" "/tmp/main.cycle")` |

</details>

---

### `:simple:click`

**Clicks a UI element.**

```true
button:startAll => :simple:click
```

**Targets:** `button:*`, `tab:*`, `agent:*`, `menu:*`, `file:*`.

---

### `:simple:clickAt`

**Clicks at absolute screen coordinates (AWT Robot). Dangerous, runtime only, no headless.**

```true
100, 200 => :simple:clickAt
```

---

### `:simple:type`

**Types text into a UI element, character by character (like a human).**

```true
hello, editor:current => :simple:type
hello, editor:current, 100 => :simple:type
```

- `delay` — ms between characters, default 30. `0` — no delay, but still character by character.

---

### `:simple:openTerminal`

**Opens a real OS terminal.**

```true
Console => :simple:openTerminal
```

---

### `:simple:closeTerminal`

**Closes a terminal.**

```true
Console => :simple:closeTerminal
```

---

### `:simple:sendTerminal`

**Sends a command to an open terminal.**

```true
ls -la, Console => :simple:sendTerminal
```

---

### `:simple:typeTerminal`

**Types text into an open terminal, character by character (to stdin).**

```true
echo hello, Console => :simple:typeTerminal
echo hello, Console, 100 => :simple:typeTerminal
```

---

### `:simple:search`

**Sets query and runs search.**

```true
hello => :simple:search
```

### `:simple:search:query`

**Current query.**

```true
:simple:search:query
```

### `:simple:search:matches`

**Lines with matches.**

```true
:simple:search:matches
```

### `:simple:search:current`

**Current match (1-based).**

```true
:simple:search:current
```

### `:simple:search:count`

**Number of matches.**

```true
:simple:search:count
```

### `:simple:search:next` / `:simple:search:prev`

**Next / previous match.**

```true
:simple:search:next
:simple:search:prev
```

### `:simple:search:clear`

**Clear search.**

```true
:simple:search:clear
```

---

**Result:** `:simple` = `"1"` (accepted, async). To use — `:simple` (substitution) or `:simple = name` (naming). **Never** `name = :simple` — system name on the right is forbidden.

---

## <a name="settings"></a>14. Settings

### `:historyLimit`

**Get or set the history limit (default 100).**

```true
:historyLimit
```

### `:logLimit`

**Get or set the log limit (default 1000).**

```true
:logLimit
```

### `:strictMode`

**Get or set strict mode.**

```true
:strictMode
```

### `:autoSync`

**Get or set auto-update of declarations.**

```true
:autoSync
```

---

## <a name="meta"></a>15. Meta

### `:run`

**Executes a line atomically. Without argument — the current line.**

```true
:run
```

**Container:** `:run` — empty string (if not set).

### `:log`

**Logs a line. Without argument — logs the current line.**

```true
:log
```

**Container:** `:log` — empty string.

### `:message`

**Writes a message to the log with type `message`.**

```true
hello => :message \ py: `log_message("hello")` | elixir: `Logger.info("hello")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `log_message("hello")` |
| Rust | `log::info!("hello");` |
| Bash | `echo "hello"` |
| Elixir | `Logger.info("hello")` |
| C | `printf("hello\n");` |
| Kotlin | `logMessage("hello")` |
| Haskell | `putStrLn "hello"` |
| Lua | `print("hello")` |
| Erlang | `io:format("~s~n", ["hello"]).` |
| JavaScript | `logMessage("hello");` |
| Go | `log.Println("hello")` |
| Clojure | `(println "hello")` |

</details>

---

### `:result`

**Stores the value as the function result.**

```true
42 => :result
```

**Container:** `:result` — the value.

### `:parameters`

**Returns the entry argument of a file-function.**

```true
:parameters
```

**Container:** `:parameters` — the value.

### `:range`

**Generates a range of numbers.**

```true
1, 5 => :range \ py: `out = ",".join(str(n) for n in range(1, 6))` | elixir: `out = Enum.join(1..5, ",")`
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `out = ",".join(str(n) for n in range(1, 6))` |
| Rust | `let out = (1..=5).map(\|n\| n.to_string()).collect::<Vec<_>>().join(",");` |
| Bash | `out=$(seq -s, 1 5)` |
| Elixir | `out = Enum.join(1..5, ",")` |
| C | `/* manual assembly */` |
| Kotlin | `val out = (1..5).joinToString(",")` |
| Haskell | `let out = intercalate "," (map show [1..5])` |
| Lua | `local t = {}; for i = 1, 5 do table.insert(t, tostring(i)) end; local out = table.concat(t, ",")` |
| Erlang | `Out = string:join([integer_to_list(N) \|\| N <- lists:seq(1, 5)], ",").` |
| JavaScript | `const out = [...Array(5).keys()].map(n => n + 1).join(",");` |
| Go | `parts := []string{}; for i := 1; i <= 5; i++ { parts = append(parts, strconv.Itoa(i)) }; out := strings.Join(parts, ",")` |
| Clojure | `(def out (clojure.string/join "," (range 1 6)))` |

</details>

---

### `:repeat`

**Executes a named action N times.**

```true
0 = a
a + 1 => a = action
action, 5 => :repeat
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `a = 0; exec("a += 1") for _ in range(5)` |
| Rust | `let mut a = 0; for _ in 0..5 { a += 1; }` |
| Bash | `a=0; for i in $(seq 1 5); do a=$((a + 1)); done` |
| Elixir | `a = Enum.reduce(1..5, 0, fn _, acc -> acc + 1 end)` |
| C | `int a = 0; for (int i = 0; i < 5; i++) a++;` |
| Kotlin | `var a = 0; repeat(5) { a += 1 }` |
| Haskell | `let a = foldl (\acc _ -> acc + 1) 0 [1..5]` |
| Lua | `local a = 0; for i = 1, 5 do a = a + 1 end` |
| Erlang | `A = lists:foldl(fun(_, Acc) -> Acc + 1 end, 0, lists:seq(1, 5)).` |
| JavaScript | `let a = 0; for (let i = 0; i < 5; i++) a += 1;` |
| Go | `a := 0; for i := 0; i < 5; i++ { a++ }` |
| Clojure | `(def a (reduce (fn [acc _] (inc acc)) 0 (range 5)))` |

</details>

---

### `:activeTab`

**Switches the active tab (1-based).**

```true
1 => :activeTab
```

<details>
<summary>Other languages</summary>

| Name | Code |
|---|---|
| Python | `tabs.select(0)` |
| Rust | `tabs.select(0);` |
| Bash | `# not idiomatic` |
| Elixir | `GenServer.cast(:ui, {:select_tab, 0})` |
| C | `select_tab(0);` |
| Kotlin | `tabs.select(0)` |
| Haskell | `selectTab 0` |
| Lua | `ui.select_tab(1)` |
| Erlang | `ui ! {select_tab, 1}.` |
| JavaScript | `tabs.select(0);` |
| Go | `ui.SelectTab(0)` |
| Clojure | `(ui/select-tab 0)` |

</details>